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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author GWL
 * @version 
 * Copyright 2002, Intelligent Works Incoporated All Rights Reserved
 */
public class RadioDialogField extends DialogField
{

  private Button[] radioButtons;
  private String[] radioLabels;
  private Composite composite;
  private int orientation = SWT.HORIZONTAL;

  public RadioDialogField(String label, String[] radioLabels, int orientation)
  {
    this(label, -1, radioLabels, orientation);
  }

  public RadioDialogField(String label, int labelWidth, String[] radioLabels,
      int orientation)
  {
    super(label, labelWidth);
    this.radioLabels = radioLabels;
    this.orientation = orientation;
  }

  public Control getControl(Composite parent)
  {
    if (parent  == null)
      return composite;
    
    checkOrientation();
    composite= new Composite(parent, SWT.NULL);

    FormLayout layout = new FormLayout();
    composite.setLayout(layout);

    FormData formData;

    if (orientation == SWT.HORIZONTAL)
    {

      Label labelControl = getLabelControl(composite);
      Control[] radioControls = getRadioButtonControls(composite);

      formData = new FormData();
      formData.width = getLabelWidth();
      formData.top = new FormAttachment(0, 5);
      formData.left = new FormAttachment(0, 0);

      labelControl.setLayoutData(formData);

      formData = new FormData();
      formData.top = new FormAttachment(0, 5);
      formData.left = new FormAttachment(labelControl, 0);
      radioControls[0].setLayoutData(formData);

      for (int i = 1; i < radioControls.length; i++)
      {
        formData = new FormData();
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(radioControls[i - 1], 8);
        radioControls[i].setLayoutData(formData);
      }

    } else
    {

      Composite labelComp = new Composite(composite, SWT.NULL);
      formData = new FormData();
      formData.width = getLabelWidth();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      formData.bottom = new FormAttachment(100, 0);
      labelComp.setLayoutData(formData);

      layout = new FormLayout();
      labelComp.setLayout(layout);

      Label labelControl = getLabelControl(labelComp);

      formData = new FormData();
      formData.width = getLabelWidth();
      formData.top = new FormAttachment(0, 5);
      formData.left = new FormAttachment(0, 0);
      labelControl.setLayoutData(formData);

      Composite radioComp = new Composite(composite, SWT.NULL);
      formData = new FormData();

      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(labelComp, 0);
      formData.right = new FormAttachment(100, 0);
      formData.bottom = new FormAttachment(100, 0);
      radioComp.setLayoutData(formData);

      layout = new FormLayout();
      radioComp.setLayout(layout);

      Control[] radioControls = getRadioButtonControls(radioComp);
      formData = new FormData();
      formData.top = new FormAttachment(0, 4);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      radioControls[0].setLayoutData(formData);
      for (int i = 1; i < radioControls.length; i++)
      {
        formData = new FormData();
        formData.top = new FormAttachment(radioControls[i - 1], 4);
        formData.left = new FormAttachment(0, 0);
        radioControls[i].setLayoutData(formData);
      }
    }

    return composite;
  }
  
  

  public boolean isVisible()
  {  
    return composite != null && !composite.isDisposed() && composite.isVisible();
  }
  
  public void setVisible(boolean flag) {
    if (composite != null && !composite.isDisposed())
      composite.setVisible(flag);
  }
  
  public Control[] getRadioButtonControls(Composite parent)
  {
    if (radioButtons == null)
    {
      radioButtons = new Button[radioLabels.length];
      final DialogField field = this;
      for (int i = 0; i < radioLabels.length; i++)
      {
        radioButtons[i] = new Button(parent, SWT.RADIO);
        radioButtons[i].setText(radioLabels[i]);
        radioButtons[i].setData(new Integer(i));
        radioButtons[i].addSelectionListener(new SelectionListener()
        {

          public void widgetDefaultSelected(SelectionEvent e)
          {
            fireDialogButtonPressed(field);
          }

          public void widgetSelected(SelectionEvent e)
          {

            fireDialogButtonPressed(field);
          }

        });
      }
      return radioButtons;
    }

    return null;
  }

  private void checkOrientation()
  {
    if (orientation != SWT.HORIZONTAL && orientation != SWT.VERTICAL)
    {
      orientation = SWT.HORIZONTAL;
    }
  }

  public void setSelected(int index)
  {
    if (radioButtons[0] != null && !radioButtons[0].isDisposed())
    {
      radioButtons[index].setSelection(true);
    }
  }

  public int getSelectedIndex()
  {
    if (radioButtons[0] != null && !radioButtons[0].isDisposed())
    {
      for (int i = 0; i < radioButtons.length; i++)
      {
        if (radioButtons[i].getSelection())
        {
          return i;
        }
      }
    }
    return -1;
  }

  public void clearSelection()
  {
    if (radioButtons[0] != null && !radioButtons[0].isDisposed())
    {
      for (int i = 0; i < radioButtons.length; i++)
      {
        radioButtons[i].setSelection(false);
      }
    }

  }

}