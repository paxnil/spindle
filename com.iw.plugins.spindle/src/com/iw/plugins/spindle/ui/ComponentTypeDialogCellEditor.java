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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;

public class ComponentTypeDialogCellEditor extends EditableDialogCellEditor {

  private Label label;
  private ITapestryModel model;
  private String title = "Tapestry Component Chooser";
  private String message = "choose a component";

  /**
   * Constructor for TypeDialogCellEditor
   */
  public ComponentTypeDialogCellEditor(Composite parent, ITapestryModel model, String title, String message) {
    super(parent);
    this.model = model;
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
    IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
    ChooseComponentDialog dialog =
      new ChooseComponentDialog(
        cellEditorWindow.getShell(),
        jproject,
        title,
        message, true);
        
    if (dialog.open() == dialog.OK) {
      return dialog.getResultComponent();
    }
    return value;
  }

}