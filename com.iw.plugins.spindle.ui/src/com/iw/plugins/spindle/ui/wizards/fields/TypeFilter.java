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

package com.iw.plugins.spindle.ui.wizards.fields;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import core.util.Assert;

/**
 * Allows views to filter by type.
 * 
 * @author glongman@gmail.com
 * 
 */
public class TypeFilter extends ViewerFilter
{

  private Class[] fAcceptedTypes;
  private Object[] fRejected;

  public TypeFilter(Class[] acceptedTypes)
  {
    this(acceptedTypes, null);
  }

  public TypeFilter(Class[] acceptedTypes, Object[] rejectedElements)
  {
    Assert.isNotNull(acceptedTypes);
    fAcceptedTypes = acceptedTypes;
    fRejected = rejectedElements;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
   *      java.lang.Object, java.lang.Object)
   */
  public boolean select(Viewer viewer, Object parentElement, Object element)
  {
    if (fRejected != null)
    {
      for (int i = 0; i < fRejected.length; i++)
      {
        if (element.equals(fRejected[i]))
        {
          return false;
        }
      }
    }
    for (int i = 0; i < fAcceptedTypes.length; i++)
    {
      if (fAcceptedTypes[i].isInstance(element))
      {
        return true;
      }
    }
    return false;
  }

}