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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Label and Image provider for IStorage objects that are tapestry artifacts
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: StorageLabelProvider.java,v 1.1 2003/10/29 12:33:58 glongman
 *          Exp $
 */
public class StorageLabelProvider implements ILabelProvider
{

  private IStorage getStorage(Object element)
  {
    IStorage storage = null;
    if (element instanceof IStorage)
    {
      storage = (IStorage) element;
    } else if (element instanceof IAdaptable)
    {
      storage = (IStorage) ((IAdaptable) element).getAdapter(IStorage.class);
    }

    return storage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
   */
  public Image getImage(Object element)
  {
    IStorage storage = getStorage(element);
    if (storage != null)
    {
      String extension = storage.getFullPath().getFileExtension();
      if ("jwc".equals(extension))
      {
        return Images.getSharedImage("component16.gif");
      } else if ("page".equals(extension))
      {
        return Images.getSharedImage("page16.gif");
      } else if ("library".equals(extension))
      {
        return Images.getSharedImage("library16.gif");
      } else if ("application".equals(extension))
      {
        return Images.getSharedImage("application16.gif");
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  public String getText(Object element)
  {
    IStorage storage = getStorage(element);
    if (storage != null)
    {
      return storage.getName();
    }
    return element.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener)
  {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose()
  {
    // not needed - all images are shared
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
   *      java.lang.String)
   */
  public boolean isLabelProperty(Object element, String property)
  {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void removeListener(ILabelProviderListener listener)
  {
    // TODO Auto-generated method stub

  }

}