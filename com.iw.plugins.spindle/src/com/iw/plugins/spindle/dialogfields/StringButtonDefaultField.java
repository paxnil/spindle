package com.iw.plugins.spindle.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
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
public class StringButtonDefaultField extends StringButtonField {

  private Label defaultLabelControl;
  private String defaultValue;
  private int defaultLableWidth;

  public StringButtonDefaultField(String label) {
    this(label, "(default)", -1);
  }

  public StringButtonDefaultField(String label, String defaultLabel) {
    this(label, defaultLabel, -1);
  }

  public StringButtonDefaultField(String label, int labelWidth) {
    super(label, labelWidth);
  }

  public StringButtonDefaultField(String label, String defaultLabel, int labelWidth) {
    super(label, labelWidth);
    this.defaultValue = defaultValue;
    this.defaultLableWidth = defaultLableWidth;
  }

  public Control getControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NULL);
    FormLayout layout = new FormLayout();
    container.setLayout(layout);

    Label labelControl = getLabelControl(container);
    Text textControl = getTextControl(container);
    Label defaultLabel = getDefaultLabelControl(container);
    Button buttonControl = getButtonControl(container);

    FormData formData;

    formData = new FormData();
    formData.height = 20;
    formData.width = getLabelWidth();
    formData.top = new FormAttachment(0, 5);
    formData.left = new FormAttachment(0, 0);
    //formData.right = new FormAttachment(text, SWT.CENTER);
    labelControl.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 3);
    formData.left = new FormAttachment(labelControl, 4);
    formData.right = new FormAttachment(defaultLabel, -4);
    textControl.setLayoutData(formData);

    formData = new FormData();
    formData.height = 20;
    formData.top = new FormAttachment(0, 5);
    formData.right = new FormAttachment(buttonControl, -4);
    defaultLabel.setLayoutData(formData);

    buttonControl.setText("Browse...");
    formData = new FormData();
    formData.width = 75;
    formData.height = 25;
    //formData.top = new FormAttachment(0, 60);
    formData.right = new FormAttachment(100, 0);
    buttonControl.setLayoutData(formData);

    return container;

  }

  public Label getDefaultLabelControl(Composite parent) {
    if (defaultLabelControl == null) {

      defaultLabelControl = new Label(parent, SWT.LEFT | SWT.WRAP);
      defaultLabelControl.setFont(parent.getFont());

      if (defaultValue != null && !"".equals(defaultValue)) {
        defaultLabelControl.setText(defaultValue);
      } else {

        defaultLabelControl.setText("(default)");

      }
    }
    return defaultLabelControl;
  }

  public void setEnabled(boolean flag) {
    if (defaultLabelControl != null && !defaultLabelControl.isDisposed()) {
      defaultLabelControl.setEnabled(flag);
    }
    super.setEnabled(flag);
  }

  public void setDefaultVisible(boolean flag) {
    if (defaultLabelControl != null && !defaultLabelControl.isDisposed()) {
      defaultLabelControl.setVisible(flag);
    }
  }

  /**
   * Gets the defaultLabelControl.
   * @return Returns a Label
   */
  public String getDefaultLabelText() {
    if (defaultLabelControl != null && !defaultLabelControl.isDisposed()) {
      return defaultLabelControl.getText();
    }
    return null;
  }

  /**
   * Sets the defaultLabelControl.
   * @param defaultLabelControl The defaultLabelControl to set
   */
  public void setDefaultLabelText(String text) {
    if (defaultLabelControl != null && !defaultLabelControl.isDisposed()) {
      defaultLabelControl.setText(text);
    }
  }

}