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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.ui.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * Descriptor wrapper for an existing image
 * 
 * @author glongman@gmail.com
 * @version $Id: WrappedImageDescriptor.java,v 1.1 2003/11/13 21:36:29 glongman
 *          Exp $
 */
public class WrappedImageDescriptor extends ImageDescriptor
{

  private Image wrappedImage;

  public WrappedImageDescriptor(Image image)
  {
    super();
    wrappedImage = image;
  }

  public ImageData getImageData()
  {
    return wrappedImage.getImageData();
  }

  public boolean equals(Object obj)
  {
    return (obj != null) && getClass().equals(obj.getClass())
        && wrappedImage.equals(((WrappedImageDescriptor) obj).wrappedImage);
  }

  public int hashCode()
  {
    return wrappedImage.hashCode();
  }

}