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
package com.iw.plugins.spindle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Convenience class for accessing shared images
 */
public class Images
{
  static private URL BASE_URL = null;
  static private ImageRegistry Registry = null;

  public static Image getSharedImage(String name)
  {
    ImageRegistry registry = getImageRegistry();
    URL imageURL = getImageURL(name);
    String urlString = null;
    if (imageURL != null)
    {
      urlString = imageURL.toString();
    } else
    {
      return getSharedImage("missing.gif");
    }
    Image result = registry.get(urlString);
    if (result == null)
    {
      ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
      Registry.put(urlString, descriptor.createImage());
      result = registry.get(urlString);
    }
    return result;
  }

  public static ImageDescriptor getImageDescriptor(String name)
  {
    return createImageDescriptor(getImageURL(name));
  }

  static private ImageRegistry getImageRegistry()
  {
    if (Registry == null)
    {
      Registry = UIPlugin.getDefault().getImageRegistry();
      Registry.put("missing", ImageDescriptor.getMissingImageDescriptor().createImage());
    }
    return Registry;
  }

  /**
   * Utility method to create an <code>ImageDescriptor</code> from a path to a
   * file.
   */
  public static ImageDescriptor createImageDescriptor(URL imageURL)
  {
    if (imageURL == null)
    {
      return ImageDescriptor.getMissingImageDescriptor();
    }
    return ImageDescriptor.createFromURL(imageURL);
  }

  public static URL getImageURL(String name)
  {
    String iconPath = "icons/full/" + name;

    try
    {
      return new URL(getBaseURL(), iconPath);

    } catch (MalformedURLException e)
    {}
    return null;
  }

  static private URL checkBase(URL candidate)
  {
    //    String current = candidate.getPath();
    //    int index = current.indexOf("_");
    //    if (index != -1)
    //    {
    //      try
    //      {
    //        current = current.substring(0, index) + "/";
    //        return new URL(candidate.getProtocol(), "", current);
    //      } catch (MalformedURLException mex)
    //      {
    //        UIPlugin.log(mex);
    //      }
    //    }
    return candidate;
  }

  private static synchronized final URL getBaseURL()
  {
    if (BASE_URL == null)
    {
      URL installUrl = UIPlugin.getDefault().getBundle().getEntry("/");
      try
      {
        BASE_URL = checkBase(Platform.resolve(installUrl));
      } catch (IOException e)
      {
        UIPlugin.log(e);
      }
    }
    return BASE_URL;
  }

}