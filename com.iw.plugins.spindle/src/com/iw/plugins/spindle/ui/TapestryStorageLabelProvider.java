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

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
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
	
  boolean longNames = false;

  Image applicationImage = TapestryImages.getSharedImage("application16.gif");
  Image libraryImage = TapestryImages.getSharedImage("library16.gif");
  Image componentImage = TapestryImages.getSharedImage("component16.gif");
  Image pageImage = TapestryImages.getSharedImage("page16.gif");

  public TapestryStorageLabelProvider() {
    super();
  }
  
  public TapestryStorageLabelProvider(boolean longNames) {
    super();
    this.longNames = longNames;
  }

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
    	
      IStorage storage = (IStorage)element;
      String result = storage.getName();
      
      if (longNames) {
      	
      	IPath path = storage.getFullPath();
      	result = result + " " +path.removeLastSegments(1).toString();
      }
    	
      return result;
      
    }

    return super.getText(element);
  }

}
