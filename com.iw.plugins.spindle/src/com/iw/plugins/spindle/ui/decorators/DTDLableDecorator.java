package com.iw.plugins.spindle.ui.decorators;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.TapestryProject;
import com.iw.plugins.spindle.spec.XMLUtil;

/**
 * @author GWL
 * @version $Id$
 *
 */
public class DTDLableDecorator
  extends LabelProvider
  implements ILabelDecorator, IResourceChangeListener {

  /**
   * Constructor for DTDLableDecorator.
   */
  public DTDLableDecorator() {
    TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(
      this,
      IResourceChangeEvent.POST_AUTO_BUILD);
  }

  private ITapestryModel getTapestryModel(Object element) {
    IStorage storage = null;
    try {
      storage = (IStorage) element;
    } catch (Exception e) {
      if (element instanceof IAdaptable) {
        storage = (IStorage) ((IAdaptable) element).getAdapter(IStorage.class);
      }
    }
    try {

      if (storage != null) {

        TapestryProject tproject =
          (TapestryProject) TapestryPlugin.getDefault().getTapestryProjectFor(storage);

        if (tproject != null && tproject.getProject().isOpen()) {

          TapestryProjectModelManager mgr = tproject.getModelManager();
          return mgr.getReadOnlyModel(storage, true);

        }

      }

    } catch (CoreException e) {
    }
    return null;
  }

  private String getDTDString(ITapestryModel model) {
    if (!model.isLoaded()) {
      try {
        model.load();
      } catch (CoreException e) {
      }
      if (!model.isLoaded()) {
        return "undetermined - could not load model";
      }
    }

    int DTDVersion = XMLUtil.getDTDVersion(model.getPublicId());

    switch (DTDVersion) {

      case XMLUtil.DTD_1_1 :
        return "1.1";

      case XMLUtil.DTD_1_2 :
        return "1.2";

      case XMLUtil.DTD_1_3 :
        return "1.3";

      default :
        return "?";
    }

  } 
  
  public Image decorateImage(Image image, Object element) {
    return null;
  } 
  
  
  public String decorateText(String text, Object element) {
    ITapestryModel model = getTapestryModel(element);
    if (model != null) {
      return text + " (DTD " + getDTDString(model) + ")";
    }
    return null;
  }
  
  
  public void resourceChanged(IResourceChangeEvent event) {
    //first collect the label change events
    final IResource[] affectedResources = processDelta(event.getDelta());
    //now post the change events to the UI thread
    if (affectedResources.length > 0) {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          fireLabelUpdates(affectedResources);
        }
      });
    }
  }

  protected IResource[] processDelta(IResourceDelta delta) {
    final ArrayList affectedResources = new ArrayList();
    try {

      delta.accept(new IResourceDeltaVisitor() {

        public boolean visit(IResourceDelta delta) throws CoreException {

          IResource resource = delta.getResource(); //skip workspace root
          boolean result = true;

          if (resource.getType() == IResource.ROOT) {

            return true;

          } //don't care about deletions
          if (delta.getKind() == IResourceDelta.REMOVED) {

            return false;

          }
          IStorage storage = null;
          try {

            storage = (IStorage) resource;

          } catch (Exception e) {

            if (resource instanceof IAdaptable) {

              storage = (IStorage) ((IAdaptable) resource).getAdapter(IStorage.class);
            }
          }
          if (resource != null) {

            ITapestryModel model = getTapestryModel(resource);
            if (model != null) {

              if (model.isLoaded()) {

                model.reload();

              } else {

                model.load();

              }
              affectedResources.add(resource);
              result = false;
            }

          }
          return result;
        }
      });
    } catch (CoreException e) {
      TapestryPlugin.getDefault().logException(e);
    } //convert event list to array
    return (IResource[]) affectedResources.toArray(new IResource[affectedResources.size()]);
  }

  void fireLabelUpdates(final IResource[] affectedResources) {
    LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, affectedResources);
    fireLabelProviderChanged(event);
  }
}
