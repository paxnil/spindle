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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.ui.dialogfields.RadioDialogField;

public class ChooseFromListDialog extends Dialog implements IDialogFieldChangedListener {

  RadioDialogField buttonGroup;

  String[] buttonNames;
  Object[] results;
  String title = "Choose:";
  int resultIndex = -1;

  /**
   * Constructor for ChooseAssetTypeDialog
   */
  public ChooseFromListDialog(Shell shell, String[] buttonNames, Object[] results, String title) {
    super(shell);
    this.buttonNames = buttonNames;
    this.results = results;
    this.title = title;
    int shellStyle= getShellStyle();
    setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(title);
  }

  protected Control createDialogArea(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);

    FormLayout layout = new FormLayout();
    layout.marginHeight = 4;
    layout.marginWidth = 4;
    container.setLayout(layout);

    Control buttonGroupControl = createButtonGroup(container);

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);

    buttonGroupControl.setLayoutData(formData);

    return container;
  }

  protected Control createButtonGroup(Composite parent) {

    buttonGroup = new RadioDialogField("Choose:", 64, buttonNames, SWT.HORIZONTAL);
    buttonGroup.addListener(this);

    Control result = buttonGroup.getControl(parent);
    buttonGroup.setSelected(0);

    return result;

  }

  public Object getSelectedResult() {
    if (resultIndex >= 0) {
      return results[resultIndex];
    }
    return results[0];
  }
  /**
   * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field) {
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldChanged(DialogField)
   */
  public void dialogFieldChanged(DialogField field) {
    resultIndex = buttonGroup.getSelectedIndex();
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
   */
  public void dialogFieldStatusChanged(IStatus status, DialogField field) {
  }

}