package com.iw.plugins.spindle.ui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;

import com.iw.plugins.spindle.TapestryImages;

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