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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.util.HierarchyScope;

public class TypeDialogCellEditor extends DialogCellEditor {

  private Label label;
  private IPackageFragmentRoot root;
  private String hierarchyRoot;
  private int searchFlags;

  /**
   * Constructor for TypeDialogCellEditor
   * 
   * searchFlags come from IJavaElementSearchConstants
   */
  public TypeDialogCellEditor(Composite parent, IPackageFragmentRoot root, int searchFlags) {
    super(parent);
    this.root = root;
    this.searchFlags = searchFlags;
  }

  public TypeDialogCellEditor(Composite parent, IPackageFragmentRoot root, int searchFlags, String hierarchyRoot) {
    this(parent, root, searchFlags);
    this.hierarchyRoot = hierarchyRoot;
  }

  private IJavaSearchScope createSearchScope() {
    IJavaProject jproject = root.getJavaProject();
    IJavaSearchScope result = null;
    IType hrootElement = null;
    try {
      if (hierarchyRoot != null) {
        hrootElement = jproject.findType(hierarchyRoot);
      }
      if (hrootElement != null) {
        result = new HierarchyScope(hrootElement, jproject);
      }
    } catch (JavaModelException jmex) {
      //ignore
      jmex.printStackTrace();
    }
    if (result == null) {
      IJavaElement[] elements = new IJavaElement[] { jproject };
      result = SearchEngine.createJavaSearchScope(elements);
    }
    return result;
  }

  /**
   * @see DialogCellEditor#openDialogBox(Control)
   */
  protected Object openDialogBox(Control cellEditorWindow) {
    Object value = getValue();
    try {
      SelectionDialog dialog =
        JavaUI.createTypeDialog(
          cellEditorWindow.getShell(),
          new ProgressMonitorDialog(cellEditorWindow.getShell()),
          createSearchScope(),
          searchFlags,
          false);
      dialog.setTitle("Type");
      dialog.setMessage(
        hierarchyRoot == null ? "Choose the type" : "Choose a type (extends/implements " + hierarchyRoot + ")");
      if (dialog.open() == dialog.OK) {
        return ((IType) dialog.getResult()[0]).getFullyQualifiedName(); //getFirstResult());
      }
    } catch (JavaModelException jmex) {
      TapestryPlugin.getDefault().logException(jmex);
    }

    return value;
  }

}