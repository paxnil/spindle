/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */



package com.iw.plugins.spindle.ui.decorators;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.ui.TapestryProjectImageDescriptor;


public class TapestryProjectLableDecorator
  extends LabelProvider
  implements ILabelDecorator, IResourceChangeListener {

  private ImageDescriptorRegistry imageDescriptorRegistry;

  // these are shared, no need to dispose in this class
  private Image applicationImage = TapestryImages.getSharedImage("application16.gif");
  private Image libraryImage = TapestryImages.getSharedImage("library16.gif");

  /**
   * Constructor for DTDLableDecorator.
   */
  public TapestryProjectLableDecorator() {

    imageDescriptorRegistry = new ImageDescriptorRegistry();

    TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(
      this,
      IResourceChangeEvent.POST_AUTO_BUILD);
  }

  public Image decorateImage(Image image, Object element) {

    Image useDecorator = applicationImage;

    ITapestryProject tproject = null;

    try {

      tproject = TapestryPlugin.getDefault().getTapestryProjectFor(element);

    } catch (CoreException e) {
    }

    if (tproject == null) {

      return image;

    }

    IStorage storage = null;

    try {

      storage = tproject.getProjectStorage();

    } catch (CoreException e) {

    }

    if (storage != null && storage.getName().endsWith(".library")) {

      useDecorator = libraryImage;

    }

    ImageDescriptor baseImage = new ImageImageDescriptor(image);

    Rectangle baseBounds = image.getBounds();

    TapestryProjectImageDescriptor synthetic =
      new TapestryProjectImageDescriptor(baseImage, useDecorator, baseBounds);

    return imageDescriptorRegistry.get(synthetic);

  }

  public void dispose() {

    imageDescriptorRegistry.dispose();

  }

  public String decorateText(String text, Object element) {

    ITapestryProject tproject = null;

    try {

      tproject = TapestryPlugin.getDefault().getTapestryProjectFor(element);

    } catch (CoreException e) {
    }

    if (tproject == null) {

      return text;

    }

    IStorage storage = null;

    try {

      storage = tproject.getProjectStorage();

    } catch (CoreException e) {

    }

    if (storage == null) {

      return text + " (Invalid Tapestry Project)";
    }

    return text + " (" + storage.getName() + ")";
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

          IResource resource = delta.getResource(); //skip all but projects

          if (resource.getType() == IResource.ROOT) {

            return true;

          }

          if (resource.getType() == IResource.PROJECT
            && delta.getKind() != IResourceDelta.REMOVED) {

            if (TapestryPlugin.getDefault().getTapestryProjectFor(resource) != null) {

              affectedResources.add(resource);

            }
          }
          return false;
        }
      });
    } catch (CoreException e) {
      
    }

    //convert event list to array
    return (IResource[]) affectedResources.toArray(new IResource[affectedResources.size()]);
  }

  void fireLabelUpdates(final IResource[] affectedResources) {

    LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, affectedResources);
    fireLabelProviderChanged(event);

  }
}
