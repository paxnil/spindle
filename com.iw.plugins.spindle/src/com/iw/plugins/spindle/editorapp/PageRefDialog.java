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
package com.iw.plugins.spindle.editorapp;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.dialogfields.DialogField;
import com.iw.plugins.spindle.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.dialogfields.StringButtonField;
import com.iw.plugins.spindle.dialogfields.StringField;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.ui.AbstractDialog;
import com.iw.plugins.spindle.ui.ChooseComponentDialog;

public class PageRefDialog extends AbstractDialog {

  private boolean editing;
  private StringField pageName;
  private StringButtonField pageRef;
  private ITapestryModel model;
  private IJavaProject jproject;
  private DialogAdapter adapter = new DialogAdapter();

  private Collection existingNames;

  private String resultName;
  private String resultComponent;
  private String editingName;

  /**
   * Constructor for PageRefDialog
   */
  public PageRefDialog(Shell shell, ITapestryModel model, Collection existingNames) {
    super(shell);
    String windowTitle = (editing ? "Edit Page Reference" : "New Page Reference");
    String description = (editing ? "This will replace the existing reference" : "Enter the new information");
    updateWindowTitle(windowTitle);
    updateMessage(description);
    jproject = TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
    this.existingNames = existingNames;
  }

  /**
   * @see AbstractDialog#createAreaContents(Composite)
   */
  protected Composite createAreaContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    FormLayout layout = new FormLayout();
    layout.marginHeight = 4;
    layout.marginWidth= 4;
    container.setLayout(layout);

    pageName = new StringField("Page name:", 100);
    pageName.addListener(adapter);
    Control pageNameControl = pageName.getControl(container);
    
    FormData formData = new FormData();
    formData.top = new FormAttachment(0,0);
    formData.left = new FormAttachment(0,0);
    formData.right = new FormAttachment(100,0);
    pageNameControl.setLayoutData(formData);

    pageRef = new StringButtonField("Page component:", 100);
    pageRef.addListener(adapter);
    Control pageRefControl = pageRef.getControl(container);
    
    formData = new FormData();
    formData.top = new FormAttachment(pageNameControl,4);
    formData.left = new FormAttachment(0,0);
    formData.right = new FormAttachment(100,0);
    pageRefControl.setLayoutData(formData);
   

    return container;
  }

  /**
   * @see AbstractDialog#performCancel()
   */
  protected boolean performCancel() {
    setReturnCode(CANCEL);
    return true;

  }

  protected void cancelPressed() {
    performCancel();
    close();
  }

  public boolean close() {
    return hardClose();
  }

  protected boolean okToClose() {
    resultName = pageName.getTextValue().trim();
    if ("".equals(resultName)) {

      setReturnCode(CANCEL);
      resultName = null;
      return false;
    } else if (existingNames.contains(resultName)) {
      if (editing && resultName.equals(editingName)) {
        return true;
      }
      setErrorMessage("There is already a page by the name '" + resultName + "'. Try another");
      setReturnCode(CANCEL);
      resultName = null;
      return false;
    } else {
      setErrorMessage(null);
    }
    resultComponent = pageRef.getTextValue().trim();
    if ("".equals(resultComponent)) {
      setReturnCode(CANCEL);
      resultComponent = null;
      return false;
    }
    return true;
  }

  public int open(String existingName, String existingComponent) {
    editing = true;
    editingName = existingName;
    pageName.setTextValue(existingName);
    pageRef.setTextValue(existingComponent);
    return super.open();
  }

  public String getResultName() {
    return resultName;
  }

  public String getResultComponent() {
    return resultComponent;
  }

  protected class DialogAdapter implements IDialogFieldChangedListener {

    /**
    * @see IStringButtonAdapter#changeControlPressed(DialogField)    
    */
    public void changeControlPressed(DialogField field) {

    }

    public void dialogFieldChanged(DialogField field) {
      update();
    }
    /**
     * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
     */
    public void dialogFieldButtonPressed(DialogField field) {
      ChooseComponentDialog dialog =
        new ChooseComponentDialog(getShell(), jproject, "Page Component Reference", "Choose a page component", false);
      dialog.create();
      int result = dialog.open();
      if (result == PageRefDialog.OK) {
        String value = (String) dialog.getResultComponent();
        pageRef.setTextValue(value);
      }
    }

    /**
     * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
     */
    public void dialogFieldStatusChanged(IStatus status, DialogField field) {
    }

  }
}