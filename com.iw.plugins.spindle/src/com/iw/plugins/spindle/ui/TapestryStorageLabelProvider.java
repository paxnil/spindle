package com.iw.plugins.spindle.ui;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.TapestryImages;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class TapestryStorageLabelProvider extends LabelProvider {

  Image applicationImage = TapestryImages.getSharedImage("application16.gif");
  Image libraryImage = TapestryImages.getSharedImage("library16.gif");
  Image componentImage = TapestryImages.getSharedImage("component16.gif");
  Image pageImage = TapestryImages.getSharedImage("page16.gif");

  public Image getImage(Object element) {

    if (element instanceof IStorage) {

      String name = ((IStorage) element).getName();
      if (name.indexOf(".application") >= 0) {

        return applicationImage;

      } else if (name.indexOf(".library") >= 0) {

        return libraryImage;

      } else if (name.indexOf(".jwc") >= 0) {

        return componentImage;

      } else if (name.indexOf(".page") >= 0) {

        return pageImage;

      }
    }
    return super.getImage(element);
  }

  public String getText(Object element) {

    if (element instanceof IStorage) {
    	
      return ((IStorage) element).getName();
      
    }

    return super.getText(element);
  }

}
