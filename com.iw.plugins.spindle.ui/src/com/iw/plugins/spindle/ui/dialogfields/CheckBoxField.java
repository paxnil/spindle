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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.dialogfields;

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
 * Copyright 2002, Intelligent Works Incoporated All Rights Reserved
 */
public class CheckBoxField extends DialogField
{

  private Button fCheckboxControl;

  private DialogField[] fAttachedFields;

  private boolean fFireEvent = true;

  private boolean fLabelFirst;

  public CheckBoxField(String label)
  {
    this(label, false);
  }

  public CheckBoxField(String label, boolean labelFirst)
  {
    super(label);
    fLabelFirst = labelFirst;
  }

  public CheckBoxField(String label, int labelWidth, boolean labelFirst)
  {
    super(label, labelWidth);
    fLabelFirst = labelFirst;
  }
  
  

  public boolean isVisible()
  {   
    return super.isVisible() && fCheckboxControl.isVisible();
  }
  
  public void setVisible(boolean flag) {
    super.setVisible(flag);
    if (fCheckboxControl != null && !fCheckboxControl.isDisposed())
      fCheckboxControl.setVisible(flag);
  }
  public Control getControl(Composite parent)
  {

    Composite container = new Composite(parent, SWT.NULL);
    FormLayout layout = new FormLayout();
    container.setLayout(layout);

    FormData formData;

    if (fLabelFirst)
    {
      Label labelControl = getLabelControl(container);
      formData = new FormData();
      formData.width = getLabelWidth();
      formData.top = new FormAttachment(0, 3);
      formData.left = new FormAttachment(0, 0);
      labelControl.setLayoutData(formData);

      Button checkBox = getCheckBoxControl(container, SWT.CHECK);
      formData = new FormData();
      formData.top = new FormAttachment(0, 3);
      formData.left = new FormAttachment(labelControl, 4);
      formData.right = new FormAttachment(100, 0);
      checkBox.setLayoutData(formData);

    } else
    {

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
    }

    return container;
  }

  public Button getCheckBoxControl(Composite parent, int modifier)
  {
    if (fCheckboxControl == null)
    {

      fCheckboxControl = new Button(parent, modifier);
      final DialogField field = this;
      fCheckboxControl.addSelectionListener(new SelectionListener()
      {

        public void widgetDefaultSelected(SelectionEvent e)
        {
          updateAttachedFields();
          fireDialogButtonPressed(field);
        }
        public void widgetSelected(SelectionEvent e)
        {
          updateAttachedFields();
          fireDialogButtonPressed(field);
        }
      });
      fCheckboxControl.setFont(parent.getFont());

    }
    return fCheckboxControl;
  }

  public void setCheckBoxValue(boolean value, boolean fireEvent)
  {
    if (fCheckboxControl != null && !fCheckboxControl.isDisposed())
    {
      updateAttachedFields();

      fCheckboxControl.setSelection(value);
      if (fireEvent)
        fireDialogFieldChanged(this);

    }
  }

  public void setCheckBoxValue(boolean value)
  {
    setCheckBoxValue(value, true);
  }

  public boolean getCheckBoxValue()
  {
    if (fCheckboxControl != null && !fCheckboxControl.isDisposed())
    {
      return fCheckboxControl.getSelection();
    }
    return false;
  }

  public void setEnabled(boolean flag)
  {
    if (fCheckboxControl != null && !fCheckboxControl.isDisposed())
    {
      fCheckboxControl.setEnabled(flag);
    }
    super.setEnabled(flag);
  }

  public void attachDialogFields(DialogField[] fields)
  {
    fAttachedFields = fields;
  }

  private void updateAttachedFields()
  {
    if (fAttachedFields != null)
    {
      for (int i = 0; i < fAttachedFields.length; i++)
      {
        fAttachedFields[i].setEnabled(getCheckBoxValue());
      }
    }
  }
}