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
 package com.iw.plugins.spindle.ui.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class StringField extends DialogField {

  private Text textControl;

  public StringField(String label) {
    super(label);
  }

  public StringField(String label, int labelWidth) {
    super(label, labelWidth);
  }

  public Control getControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NULL);
    FormLayout layout = new FormLayout();
    container.setLayout(layout);

    Label labelControl = getLabelControl(container);
    FormData formData = new FormData();
    formData.width = getLabelWidth();
    formData.top = new FormAttachment(0, 5);
    formData.left = new FormAttachment(0, 0);
    labelControl.setLayoutData(formData);

    Text textControl = getTextControl(container);
    formData = new FormData();
    formData.top = new FormAttachment(0, 3);
    formData.left = new FormAttachment(labelControl, 4);
    formData.right = new FormAttachment(100, 0);
    textControl.setLayoutData(formData);

    return container;
  }

  public Text getTextControl(Composite parent) {
    if (textControl == null) {
      final DialogField field = this;
      textControl = new Text(parent, SWT.BORDER);
      textControl.setFont(parent.getFont());
      textControl.addModifyListener(new ModifyListener() {

        public void modifyText(ModifyEvent e) {
          fireDialogChanged(field);
        }

      });

    }
    return textControl;
  }

  public void setTextValue(String value) {
    setTextValue(value, true);
  }

  public void setTextValue(String value, boolean update) {
    if (textControl != null && !textControl.isDisposed()) {
      textControl.setText(value);
      if (update) {
        fireDialogChanged(this);
      }
    }
  }

  public String getTextValue() {
    if (textControl != null && !textControl.isDisposed()) {
      return textControl.getText();
    }
    return null;
  }

  public void setEnabled(boolean flag) {
    if (textControl != null && !textControl.isDisposed()) {
      textControl.setEnabled(flag);
    }
    super.setEnabled(flag);
  }

  public boolean setFocus() {
    if (textControl != null && !textControl.isDisposed()) {
      textControl.setFocus();
      textControl.selectAll();
    }
    return true;
  }
  	

}