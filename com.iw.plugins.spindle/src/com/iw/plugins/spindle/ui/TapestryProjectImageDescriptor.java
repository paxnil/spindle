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



package com.iw.plugins.spindle.ui;

import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class TapestryProjectImageDescriptor extends CompositeImageDescriptor {

  private ImageDescriptor baseImage;
  private Rectangle baseBounds;
  private ImageDescriptor decorateImage;
  private Rectangle decorateBounds;

  private Point compositeSize;

  public TapestryProjectImageDescriptor(
    ImageDescriptor baseImage,
    Image decorateImage,
    Rectangle baseBounds) {
    	
    	
    this.baseImage = baseImage;
    Assert.isNotNull(baseImage);
    this.baseBounds = baseBounds;
    Assert.isNotNull(decorateImage);
    this.decorateImage = new ImageImageDescriptor(decorateImage);
    decorateBounds = decorateImage.getBounds();
    Assert.isNotNull(baseBounds);
    Assert.isNotNull(decorateBounds);
  }

  //  public void setImageSize(Point size) {
  //    Assert.isNotNull(size);
  //    Assert.isTrue(size.x >= 0 && size.y >= 0);
  //    size = size;
  //  }

  //  /**
  //   * @return the size of the image created by calling <code>createImage()</code>
  //   * @see ImageDescriptor#createImage()
  //   */
  //  public Point getImageSize() {
  //    return new Point(baseBounds.x, baseBounds.y);
  //  }

  /* (non-Javadoc)
   * Method declared in CompositeImageDescriptor
   */
  protected Point getSize() {

    if (compositeSize == null) {

      int x = baseBounds.width + 5 + decorateBounds.width;
      int y = Math.max(baseBounds.height, decorateBounds.height);

      compositeSize = new Point(x, y);

    }
    return compositeSize;
  }

  /* (non-Javadoc)
   * Method declared on Object.
   */
  public boolean equals(Object object) {

    if (!TapestryProjectImageDescriptor.class.equals(object.getClass())) {
      return false;

    }

    TapestryProjectImageDescriptor other = (TapestryProjectImageDescriptor) object;
    return (baseImage.equals(other.baseImage) && baseBounds.equals(other.baseBounds));
  }

  /* (non-Javadoc)
   * Method declared on Object.
   */
  public int hashCode() {
    return baseImage.hashCode() | baseBounds.hashCode() | decorateImage.hashCode() | decorateBounds.hashCode();
  }

  /* (non-Javadoc)
   * Method declared in CompositeImageDescriptor
   */
  protected void drawCompositeImage(int width, int height) {

    ImageDescriptor taller = baseImage;

    if (decorateBounds.height > baseBounds.height) {

      taller = decorateImage;

    }

    int baseOffset = 0;
    int decorateOffset = 0;

    if (baseBounds.height > decorateBounds.height) {

      baseOffset = decorateBounds.height - baseBounds.height;

    } else {

      decorateOffset = baseBounds.height - decorateBounds.height;

    }

    ImageData imageData;

    if ((imageData = baseImage.getImageData()) == null) {

      imageData = DEFAULT_IMAGE_DATA;

    }

    drawImage(imageData, 0, baseOffset);

    if ((imageData = decorateImage.getImageData()) == null) {

      imageData = DEFAULT_IMAGE_DATA;

    }

    drawImage(imageData, baseBounds.width + 5, decorateOffset);

  }

}