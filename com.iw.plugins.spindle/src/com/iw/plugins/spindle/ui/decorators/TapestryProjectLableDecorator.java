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
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.ITapestryProject;

public class TapestryProjectLableDecorator extends LabelProvider implements ILabelDecorator, IResourceChangeListener {

  static public String TAP_PROJECT_IMAGE = TapestryPlugin.ID_PLUGIN + ".TAP+PROJECT_IMAGE";

  private ImageDescriptorRegistry registry = new ImageDescriptorRegistry();

  public TapestryProjectLableDecorator() {

    TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_AUTO_BUILD);
  }

  public Image decorateImage(Image image, Object obj) {
    if (shouldDecorateImage(obj)) {
        ImageDescriptor baseImage = new WrappedImageDescriptor(image);
        Rectangle bounds = image.getBounds();
        return registry.get(new TapestryProjectImageDescriptor(baseImage, new Point(bounds.width, bounds.height)));

    }
    return image;
  }

  public boolean shouldDecorateImage(Object element) {

    ITapestryProject tproject = null;

    try {

      tproject = TapestryPlugin.getDefault().getTapestryProjectFor(element);

    } catch (CoreException e) {
    }

    if (tproject == null) {

      return false;

    }

    IStorage storage = null;

    try {

      storage = tproject.getProjectStorage();

    } catch (CoreException e) {

    }

    if (storage == null) {

      return false;
    }

    return true;

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

      return text;
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

          if (resource.getType() == IResource.PROJECT && delta.getKind() != IResourceDelta.REMOVED) {

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

  class WrappedImageDescriptor extends ImageDescriptor {

    private Image wrappedImage;

    public WrappedImageDescriptor(Image image) {
      super();
      wrappedImage = image;
    }

    public ImageData getImageData() {
      return wrappedImage.getImageData();
    }

    public boolean equals(Object obj) {
      return (obj != null)
        && getClass().equals(obj.getClass())
        && wrappedImage.equals(((WrappedImageDescriptor) obj).wrappedImage);
    }

    public int hashCode() {
      return wrappedImage.hashCode();
    }

  }

  class TapestryProjectImageDescriptor extends CompositeImageDescriptor {

    private ImageDescriptor useBaseImage;
    private Image tapestry_ovr = TapestryImages.getSharedImage("project_ovr.gif");
    private Point finalSize;

    /**
     * 
     * @param baseImage an image descriptor used as the base image
     * @param size the size of the resulting image
     */
    public TapestryProjectImageDescriptor(ImageDescriptor baseImage, Point size) {
      useBaseImage = baseImage;
      Assert.isNotNull(useBaseImage);
      finalSize = size;
      Assert.isNotNull(finalSize);
    }

    /**
     * Sets the size of the image created by calling <code>createImage()</code>.
     * 
     * @param size the size of the image returned from calling <code>createImage()</code>
     * @see ImageDescriptor#createImage()
     */
    public void setImageSize(Point size) {
      Assert.isNotNull(size);
      Assert.isTrue(size.x >= 0 && size.y >= 0);
      finalSize = size;
    }

    /**
     * Returns the size of the image created by calling <code>createImage()</code>.
     * 
     * @return the size of the image created by calling <code>createImage()</code>
     * @see ImageDescriptor#createImage()
     */
    public Point getImageSize() {
      return new Point(finalSize.x, finalSize.y);
    }

    /* (non-Javadoc)
     * Method declared in CompositeImageDescriptor
     */
    protected Point getSize() {
      return finalSize;
    }

    /* (non-Javadoc)
     * Method declared on Object.
     */
    public boolean equals(Object object) {
      if (!TapestryProjectImageDescriptor.class.equals(object.getClass()))
        return false;

      TapestryProjectImageDescriptor other = (TapestryProjectImageDescriptor) object;
      return (useBaseImage.equals(other.useBaseImage) && finalSize.equals(other.finalSize));
    }

    /* (non-Javadoc)
     * Method declared on Object.
     */
    public int hashCode() {
      return useBaseImage.hashCode() | finalSize.hashCode();
    }

    /* (non-Javadoc)
     * Method declared in CompositeImageDescriptor
     */
    protected void drawCompositeImage(int width, int height) {
      ImageData bg = useBaseImage.getImageData();
      drawImage(bg, 0, 0);
      drawTopRight();
    }

    private void drawTopRight() {
      int x = getSize().x;
      ImageData data = tapestry_ovr.getImageData();
      x -= data.width;
      drawImage(data, x, 0);
    }

  }
  
  public void dispose() {
    super.dispose();
    registry.dispose();
  }

}
