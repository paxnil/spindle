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
package com.iw.plugins.spindle.editors;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class FormCheckbox {
  private Button button;
  private boolean value;
  private boolean dirty;
  private Vector listeners = new Vector();
  boolean ignoreModify = false;

  public FormCheckbox(Composite parent, String label) {
    this.button = new Button(parent, SWT.CHECK);
    if (label != null) {
      button.setText(label);
    }
    this.value = button.getSelection();
    addListeners();
  }
  public void addFormCheckboxListener(IFormCheckboxListener listener) {
    listeners.addElement(listener);
  }
  private void addListeners() {
    button.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent event) {
        selectionOccured(event);
      }

      public void widgetDefaultSelected(SelectionEvent event) {
        selectionOccured(event);
      }
    });
  }

  public void commit() {
    if (dirty) {
      value = button.getSelection();
      //notify
      for (Iterator iter = listeners.iterator(); iter.hasNext();) {
        ((IFormCheckboxListener) iter.next()).booleanValueChanged(this);
      }
    }
    dirty = false;
  }
  protected void selectionOccured(SelectionEvent event) {
    if (ignoreModify)
      return;
    dirty = true;
    for (Iterator iter = listeners.iterator(); iter.hasNext();) {
      ((IFormCheckboxListener) iter.next()).valueDirty(this);
    }
  }
  public Button getControl() {
    return button;
  }
  public boolean getValue() {
    return value;
  }
  public boolean isDirty() {
    return dirty;
  }
  public void removeFormCheckboxListener(IFormCheckboxListener listener) {
    listeners.removeElement(listener);
  }
  public void setDirty(boolean newDirty) {
    dirty = newDirty;
  }
  public void setValue(boolean value) {
    if (button != null)
      button.setSelection(value);
    this.value = value;
  }
  public void setValue(boolean value, boolean blockNotification) {
    ignoreModify = blockNotification;
    setValue(value);
    ignoreModify = false;
  }
}