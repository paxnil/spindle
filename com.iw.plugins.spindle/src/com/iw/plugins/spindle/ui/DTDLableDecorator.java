package com.iw.plugins.spindle.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
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
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;

/**
 * @author administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DTDLableDecorator extends LabelProvider implements ILabelDecorator, IResourceChangeListener {

  /**
   * Constructor for DTDLableDecorator.
   */
  public DTDLableDecorator() {
    TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_AUTO_BUILD);
  }

  private ITapestryModel getTapestryModel(Object element) {
    IStorage resource = null;
    try {
      resource = (IStorage) element;
    } catch (Exception e) {
      if (element instanceof IAdaptable) {
        resource = (IStorage) ((IAdaptable) element).getAdapter(IStorage.class);
      }
    }
    if (resource != null) {
      return TapestryPlugin.getTapestryModelManager().getModel(resource);
    }
    return null;
  }

  private String getDTDString(ITapestryModel model) {
    if (!model.isLoaded()) {
      return "undetermined";
    }
    String DTDversion = model.getDTDVersion();    
    if (DTDversion == null) {
      return "?";
    }
    return DTDversion;
  }

  /**
   * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(Image, Object)
   * we return null here as we are not decorating images (yet)
   */
  public Image decorateImage(Image image, Object element) {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(String, Object)
   */
  public String decorateText(String text, Object element) {
    ITapestryModel model = getTapestryModel(element);
    if (model != null) {
      return text + " (DTD " + getDTDString(model) + ")";
    }
    return null;
  }

  /**
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(IResourceChangeEvent)
   */
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
          IResource resource = delta.getResource();
          //skip workspace root
          if (resource.getType() == IResource.ROOT) {
            return true;
          }
          //don't care about deletions
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
              return false;
            }
          }
          return true;
        }
      });
    } catch (CoreException e) {
      TapestryPlugin.getDefault().logException(e);
    }
    //convert event list to array
    return (IResource[]) affectedResources.toArray(new IResource[affectedResources.size()]);
  }

  void fireLabelUpdates(final IResource[] affectedResources) {
    LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, affectedResources);
    fireLabelProviderChanged(event);
  }
}
