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
package com.iw.plugins.spindle.editors.util;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmen.xml.XMLNode;

public class XMLNodeContentProvider implements ITreeContentProvider
{

  private XMLNode rootNode;
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object parentElement)
  {

    if (parentElement instanceof XMLNode)
    {
      Object[] result = ((XMLNode) parentElement).getChildren(parentElement);
      return result;
    }
    return new Object[0];
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(Object element)
  {
    if (element == rootNode)
      return null;
    return ((XMLNode) element).getParent();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object element)
  {
    return getChildren(element).length > 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement)
  {
    if (rootNode != null)
    {
      Object[] result = rootNode.getChildren(rootNode);
      return result;
    }
    return new Object[]{};
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose()
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
   *      java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    if (newInput instanceof XMLNode)
      rootNode = (XMLNode) newInput;
    else
      rootNode = null;
  }

}