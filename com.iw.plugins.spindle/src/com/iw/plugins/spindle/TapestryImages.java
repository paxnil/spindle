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
package com.iw.plugins.spindle;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
/**
 * Convenience class for accessing gettting shared images
 */
public class TapestryImages {
  static private URL BASE_URL = TapestryPlugin.getDefault().getDescriptor().getInstallURL();
  static private ImageRegistry Registry = null;

  public static Image getSharedImage(String name) {
    ImageRegistry registry = getImageRegistry();
    URL imageURL = getImageURL(name);
    String urlString = null;
    if (imageURL != null) {
    	urlString = imageURL.toString();
    } else {
    	return registry.get("missing");
    }
    Image result = registry.get(urlString);
    if (result == null) {    
        ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
        Registry.put(urlString, descriptor.createImage());    
        result = registry.get(urlString);
    }
    return result;
  }

  static private ImageRegistry getImageRegistry() {
    if (Registry == null) {
      Registry = TapestryPlugin.getDefault().getImageRegistry();
      Registry.put("missing", ImageDescriptor.getMissingImageDescriptor().createImage());
    }
    return Registry;
  }

  /**
   * Utility method to create an <code>ImageDescriptor</code>
   * from a path to a file.
   */
  public static ImageDescriptor createImageDescriptor(URL imageURL) {
    if (imageURL == null) {
      return ImageDescriptor.getMissingImageDescriptor();
    }
    return ImageDescriptor.createFromURL(imageURL);
  }

  public static URL getImageURL(String name) {
    checkBase();
    String iconPath;
    if (Display.getCurrent().getIconDepth() > 4)
      iconPath = "icons/full/";
    else
      iconPath = "icons/basic/";
    iconPath += name;
    try {
      return new URL(BASE_URL, iconPath);
      
    } catch (MalformedURLException e) {
    }
    return null;
  }

  static private void checkBase() {
    String current = BASE_URL.getPath();
    int index = current.indexOf("_");
    if (index != -1) {
      try {
        current = current.substring(0, index) + "/";
        BASE_URL = new URL(BASE_URL.getProtocol(), "", current);
      } catch (MalformedURLException mex) {
      }
    }
  }

}