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
package com.iw.plugins.spindle.ui.descriptors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.ui.ChooseWorkspaceModelDialog;
import com.iw.plugins.spindle.ui.EditableDialogCellEditor;

public class ChoosePageFromLibraryPropertyDescriptor extends PropertyDescriptor implements INeedsModelInitialization {

  String title;
  String message;
  IJavaProject project;
  
  public ChoosePageFromLibraryPropertyDescriptor(Object id, String displayName) {
  	this(id, displayName, null, null);
  }
  
  public ChoosePageFromLibraryPropertyDescriptor(Object id, String displayName, String dialogTitle, String dialogMessage) {
    super(id, displayName);
    this.title = dialogTitle;
    this.message = dialogMessage; 
  }

  public CellEditor createPropertyEditor(Composite parent) {
    return new ChoosePageCellEditor(parent, title, message);
  }
  
    /**
   * @see com.iw.plugins.spindle.ui.descriptors.INeedsModelInitialization#initialize(ITapestryModel)
   */
  public void initialize(ITapestryModel model) {
  	
    try {
      project = TapestryPlugin.getDefault().getJavaProjectFor(model);
      
    } catch (CoreException e) {
    	
    	ErrorDialog.openError(TapestryPlugin.getDefault().getActiveWorkbenchShell(), "Spindle Error", "could not create a cell editor", e.getStatus());
    }
  	
  }

  
  class ChoosePageCellEditor extends EditableDialogCellEditor {

  private String title = "Page Chooser";
  private String message = "choose a page";

  /**
   * Constructor for TypeDialogCellEditor
   */
  public ChoosePageCellEditor(Composite parent, String title, String message) {
    super(parent);
    if (title != null) {
      this.title = title;
    }
    if (message != null) {
      this.message = message;
    }
  }

  /**
   * @see DialogCellEditor#openDialogBox(Control)
   */
  protected Object openDialogBox(Control cellEditorWindow) {
    Object value = getValue();
    ChooseWorkspaceModelDialog dialog =
    
      ChooseWorkspaceModelDialog.createPageModelDialog(cellEditorWindow.getShell(),
      project, title, message);

    if (dialog.open() == dialog.OK) {
      return dialog.getResultPath();
    }
    return value;
  }

}

}