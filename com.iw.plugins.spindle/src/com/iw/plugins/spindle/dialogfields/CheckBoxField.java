package com.iw.plugins.spindle.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class CheckBoxField extends DialogField {

  private Button checkboxControl;

  private DialogField[] attachedFields;

  private boolean fireEvent = true;

  public CheckBoxField(String label) {
    super(label);
  }

  public CheckBoxField(String label, int labelWidth) {
    super(label, labelWidth);
  }

  public Control getControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NULL);
    FormLayout layout = new FormLayout();
    container.setLayout(layout);

    FormData formData;

    Button checkBox = getCheckBoxControl(container, SWT.CHECK);
    formData = new FormData();
    formData.top = new FormAttachment(0, 3);
    formData.left = new FormAttachment(0, 0);
    checkBox.setLayoutData(formData);

    Label labelControl = getLabelControl(container);
    formData = new FormData();
    formData.width = getLabelWidth();
    formData.top = new FormAttachment(0, 3);
    formData.left = new FormAttachment(checkBox, 10);
    formData.right = new FormAttachment(100, 0);
    labelControl.setLayoutData(formData);

    return container;
  }

  public Button getCheckBoxControl(Composite parent, int modifier) {
    if (checkboxControl == null) {

      checkboxControl = new Button(parent, modifier);
      final DialogField field = this;
      checkboxControl.addSelectionListener(new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {
          updateAttachedFields();
          fireDialogChanged(field);
        }
        public void widgetSelected(SelectionEvent e) {
          updateAttachedFields();
          fireDialogChanged(field);
        }
      });
      checkboxControl.setFont(parent.getFont());

    }
    return checkboxControl;
  }

  public void setCheckBoxValue(boolean value, boolean fireEvent) {
    if (checkboxControl != null && !checkboxControl.isDisposed()) {
      updateAttachedFields();     
      this.fireEvent = fireEvent;
      checkboxControl.setSelection(value);
      this.fireEvent = true;
    }
  }

  public void setCheckBoxValue(boolean value) {
    setCheckBoxValue(value, true);
  }

  public boolean getCheckBoxValue() {
    if (checkboxControl != null && !checkboxControl.isDisposed()) {
      return checkboxControl.getSelection();
    }
    return false;
  }

  public void setEnabled(boolean flag) {
    if (checkboxControl != null && !checkboxControl.isDisposed()) {
      checkboxControl.setEnabled(flag);
    }
    super.setEnabled(flag);
  }

  public void attachDialogFields(DialogField[] fields) {
    attachedFields = fields;
  }
  
  private void updateAttachedFields() {
     if (attachedFields != null) {
      for (int i = 0; i < attachedFields.length; i++) {
        attachedFields[i].setEnabled(getCheckBoxValue());
      }
    }
  }

  /**
   * @see DialogField#fireDialogChanged(DialogField)
   */
  protected void fireDialogChanged(DialogField field) {
    
    if (fireEvent) {
      super.fireDialogChanged(field);
    }
  }

}