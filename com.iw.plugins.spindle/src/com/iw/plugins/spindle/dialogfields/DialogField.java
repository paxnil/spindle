package com.iw.plugins.spindle.dialogfields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class DialogField implements IDialogFieldChangedListener {

  private String labelText;
  private int labelWidth;
  private List listeners = new ArrayList();
  protected IStatus status = new DialogFieldStatus(); 
  private Label labelControl;

  private boolean enabled = true;

  public DialogField(String labelText) {

    this(labelText, -1);
    

  }

  public DialogField(String labelText, int labelWidth) {

    this.labelText = labelText == null ? "" :labelText;
    this.labelWidth = labelWidth;
    addListener(this);
  }

  public Control getControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NULL);
    FormLayout layout = new FormLayout();
    container.setLayout(layout);
    labelControl = getLabelControl(container);

    FormData formData = new FormData();

    formData.width = labelWidth;
    formData.top = new FormAttachment(0, 4);
    formData.left = new FormAttachment(0, 0);

    labelControl.setLayoutData(formData);

    return container;
  }

  protected int getLabelWidth() {
    return labelWidth;
  }

  public Label getLabelControl(Composite parent) {
    if (labelControl == null) {

      labelControl = new Label(parent, SWT.LEFT | SWT.WRAP);
      labelControl.setFont(parent.getFont());

      if (labelText != null && !"".equals(labelText)) {
        labelControl.setText(labelText);
      } else {

        labelControl.setText(".");
        labelControl.setVisible(false);
      }
    }
    return labelControl;
  }

  /**
   * Gets the labelText.
   * @return Returns a String
   */
  public String getLabelText() {
    if (labelControl != null && !labelControl.isDisposed()) {
      return labelControl.getText();
    }
    return labelText;
  }

  /**
   * Sets the labelText.
   * @param labelText The labelText to set
   */
  public void setLabelText(String labelText) {
    if (labelControl != null && !labelControl.isDisposed()) {
      labelControl.setText(labelText);
      fireDialogChanged(this);
    }
  }

  protected void fireDialogChanged(DialogField field) {
    for (Iterator iterator = getListeners().iterator(); iterator.hasNext();) {
      IDialogFieldChangedListener element = (IDialogFieldChangedListener) iterator.next();
      element.dialogFieldChanged(field);
    }
  }

  protected void fireDialogFieldStatusChanged(IStatus status, DialogField field) {
    for (Iterator iterator = getListeners().iterator(); iterator.hasNext();) {
      IDialogFieldChangedListener element = (IDialogFieldChangedListener) iterator.next();
      element.dialogFieldStatusChanged(status, field);
    }
  }

  public IStatus getStatus() {
    return status;
  }
  
  public void setStatus(IStatus newStatus) {
    status = newStatus;
    fireDialogFieldStatusChanged(status, this);
  }

  public void setEnabled(boolean flag) {
    if (labelControl != null && !labelControl.isDisposed()) {
      labelControl.setEnabled(flag);
    }
    enabled = flag;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void addListener(IDialogFieldChangedListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public void removeListener(IDialogFieldChangedListener listener) {
    listeners.remove(listener);
  }

  public List getListeners() {
    return Collections.unmodifiableList(listeners);
  }

  public Shell getShell() {
    if (labelControl != null && !labelControl.isDisposed()) {
      return labelControl.getShell();
    }
    return null;
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
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
   */
  public void dialogFieldStatusChanged(IStatus status, DialogField field) {
  }
  
  public boolean setFocus() {
    return true;
  }

}