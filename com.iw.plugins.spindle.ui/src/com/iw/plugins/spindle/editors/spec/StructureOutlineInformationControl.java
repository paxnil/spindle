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

package com.iw.plugins.spindle.editors.spec;

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.core.spec.BaseSpecification;

/**
 * TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: StructureOutlineInformationControl.java,v 1.2.2.1 2004/06/10
 *          16:48:20 glongman Exp $
 */
public class StructureOutlineInformationControl extends TreeInformationControl
{
  static private ViewerSorter sorter;

  static
  {
    TapestryOutlinePage.AlphaCategorySorter alphaSort = new TapestryOutlinePage.AlphaCategorySorter();
    alphaSort.setUseCategorySort(true);
    sorter = alphaSort;
  }
  private SpecEditor fEditor;

  public StructureOutlineInformationControl(Shell parent, int shellStyle, int treeStyle,
      SpecEditor editor)
  {
    super(parent, shellStyle, treeStyle);
    setContentProvider(new TapestryOutlinePage.ContentProvider());
    setLabelProvider(new TapestryOutlinePage.BasicLabelProvider());
    setSorter(sorter);

    fEditor = editor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.spec.OutlineInformationControl#doSetInput(java.lang.Object)
   */
  protected void doSetInput(Object information)
  {
    if (information instanceof BaseSpecification)
      fTreeViewer.setInput(information);
    else
      fTreeViewer.setInput(null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.spec.OutlineInformationControl#doGotoSelectedElement(java.lang.Object)
   */
  protected boolean doHandleSelectedElement(Object selected)
  {
    try
    {
      fEditor.openTo(selected);
    } finally
    {

    }
    return true;
  }
}