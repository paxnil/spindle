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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class DocumentationCellEditor extends EditableDialogCellEditor {

  private Label label;
  private String title;
  private String message;

 
  public DocumentationCellEditor(Composite parent, String dialogTitle, String dialogMessage) {
    super(parent);
    this.title = dialogTitle == null ? "Documentation" : dialogTitle;
    this.message = dialogMessage == null ? "Add some documentation" : dialogMessage;
  }

  /**
   * @see DialogCellEditor#openDialogBox(Control)
   */
  protected Object openDialogBox(Control cellEditorWindow) {
    Object value = getValue();
    TextAreaDialog dialog = new TextAreaDialog(cellEditorWindow.getShell());
    dialog.updateWindowTitle(title);
    dialog.updateMessage(message);
    if (dialog.open(value.toString()) == dialog.OK) {
      return dialog.getResult();
    }
    return value;
  }

}