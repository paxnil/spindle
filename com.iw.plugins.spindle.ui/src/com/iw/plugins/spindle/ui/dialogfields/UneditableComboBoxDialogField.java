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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.iw.plugins.spindle.ui.widgets.UneditableComboBox;

public class UneditableComboBoxDialogField extends DialogField {

  private UneditableComboBox combo;
  private String[] values;

  /**
   * Constructor for UneditableComboBoxDialogField.
   * @param labelText
   * @param labelWidth
   */
  public UneditableComboBoxDialogField(String labelText, int labelWidth, String[] values) {
    super(labelText, labelWidth);
    this.values = values;
  }

  public UneditableComboBoxDialogField(String labelText, String[] values) {
    this(labelText, -1, values);
  }
  // ------- layout helpers

  /**
    * @see DialogField#getControl(Composite)
    */
  public Control getControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NULL);
    FormLayout layout = new FormLayout();
    container.setLayout(layout);

    Label label = getLabelControl(container);
    UneditableComboBox combo = getUneditableComboBoxControl(container);

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    label.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    formData.left = new FormAttachment(label, 4);
    combo.setLayoutData(formData);

    return container;
  }

  /**
   * Get the combo
   */
  public String getSelectedValue() {
    return values[combo.getSelectionIndex()];
  }

  public int getSelectedIndex() {
    return combo.getSelectionIndex();
  }
  // ------- ui creation			

  public UneditableComboBox getUneditableComboBoxControl(Composite parent) {
    if (combo == null) {

      combo = new UneditableComboBox(parent, SWT.BORDER);
      combo.setItems(values);
      combo.select(0);
      combo.setFont(parent.getFont());
      final DialogField field = this;
      combo.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent event) {
          fireDialogButtonPressed(field);
        }

        public void widgetDefaultSelected(SelectionEvent event) {
          fireDialogButtonPressed(field);
        }

      });
      combo.setEnabled(isEnabled());
    }
    return combo;
  }

  public void setValues(String[] newValues) {
    combo.setItems(newValues);
  }

  public void clearSelection() {
    combo.clearSelection();
  }

  public boolean setFocus() {
    if (combo != null && !combo.isDisposed()) {
      combo.setFocus();
    }
    return true;
  }
  /**
   * Set the combo. Triggers an dialog-changed event
   */
  public void select(int index) {
    if (combo != null && !combo.isDisposed()) {
      combo.select(index);
    } else {
      fireDialogButtonPressed(this);
    }
  }

  public void select(String value) {
    if (combo != null && !combo.isDisposed()) {
      combo.select(value);
    }
  }

  /**
   * Set the combo without triggering a dialog-changed event
   */
  public void setValueWithoutUpdate(int index) {
    if (combo != null && !combo.isDisposed()) {
      combo.select(index);
    }
  }
  /**
   * @see com.iw.plugins.spindle.dialogfields.DialogField#setEnabled(boolean)
   */
  public void setEnabled(boolean flag) {
    if (combo != null && !combo.isDisposed()) {
      combo.setEnabled(flag);
    }
    super.setEnabled(flag);
  }

}