/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.iw.plugins.spindle.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

/**
 * Image provider for annotations based on problem markers.
 * 
 * TODO JavaAnnotationImageProvider
 * 
 *  
 */
public class ProblemAnnotationImageProvider implements IAnnotationImageProvider
{

  private final static int NO_IMAGE = 0;
  private final static int GRAY_IMAGE = 1;
  private final static int OVERLAY_IMAGE = 2;

  private static ImageRegistry IMAGE_REGISTRY;

  private int fCachedImageType;
  private Image fCachedImage;

  public ProblemAnnotationImageProvider()
  {
  }

  /*
   * @see org.eclipse.jface.text.source.IAnnotationImageProvider#getManagedImage(org.eclipse.jface.text.source.Annotation)
   */
  public Image getManagedImage(Annotation annotation)
  {
    if (annotation instanceof IProblemAnnotation)
    {
      IProblemAnnotation problemAnnotation = (IProblemAnnotation) annotation;
      int imageType = getImageType(problemAnnotation);
      return getImage(problemAnnotation, imageType, Display.getCurrent());
    }
    return null;
  }

  /*
   * @see org.eclipse.jface.text.source.IAnnotationImageProvider#getImageDescriptorId(org.eclipse.jface.text.source.Annotation)
   */
  public String getImageDescriptorId(Annotation annotation)
  {
    // unmanaged images are not supported
    return null;
  }

  /*
   * @see org.eclipse.jface.text.source.IAnnotationImageProvider#getImageDescriptor(java.lang.String)
   */
  public ImageDescriptor getImageDescriptor(String symbolicName)
  {
    // unmanaged images are not supported
    return null;
  }

  private ImageRegistry getImageRegistry(Display display)
  {
    if (IMAGE_REGISTRY == null)
      IMAGE_REGISTRY = new ImageRegistry(display);
    return IMAGE_REGISTRY;
  }

  private int getImageType(IProblemAnnotation annotation)
  {
    int imageType = NO_IMAGE;
    if (annotation.hasOverlay())
    {
      imageType = OVERLAY_IMAGE;
    } else if (annotation.isMarkedDeleted())
    {
      imageType = GRAY_IMAGE;
    }
    return imageType;
  }

  private Image getImage(IProblemAnnotation annotation, int imageType, Display display)
  {
    if (fCachedImageType == imageType && fCachedImage != null)
      return fCachedImage;

    Image image = null;
    switch (imageType)
    {
      case OVERLAY_IMAGE :
        IProblemAnnotation overlay = annotation.getOverlay();
        image = getManagedImage((Annotation) overlay);
        break;

      case GRAY_IMAGE :
      {
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        String annotationType = annotation.getType();
        if (ProblemMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(annotationType))
        {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (ProblemMarkerAnnotation.WARNING_ANNOTATION_TYPE.equals(annotationType))
        {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (ProblemMarkerAnnotation.INFO_ANNOTATION_TYPE.equals(annotationType))
        {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }

        if (image != null)
        {
          ImageRegistry registry = getImageRegistry(display);
          String key = Integer.toString(image.hashCode());
          Image grayImage = registry.get(key);
          if (grayImage == null)
          {
            grayImage = new Image(display, image, SWT.IMAGE_GRAY);
            registry.put(key, grayImage);
          }
          image = grayImage;
        }
        break;
      }
    }

    fCachedImageType = imageType;
    fCachedImage = image;
    return fCachedImage;
  }
}