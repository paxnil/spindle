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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.ui.widgets.PixelConverter;

/**
 * @author GWL
 * @version Copyright 2002, Intelligent Works Incoporated All Rights Reserved
 */
public class StringButtonDefaultField extends StringButtonField
{

  private Label defaultLabelControl;
  private String defaultValue;
  private int defaultLableWidth;

  public StringButtonDefaultField(String label)
  {
    this(label, "(default)", -1);
  }

  public StringButtonDefaultField(String label, String defaultLabel)
  {
    this(label, defaultLabel, -1);
  }

  public StringButtonDefaultField(String label, int labelWidth)
  {
    super(label, labelWidth);
  }

  public StringButtonDefaultField(String label, String defaultLabel, int labelWidth)
  {
    super(label, labelWidth);
    this.defaultValue = defaultLabel;
    this.defaultLableWidth = labelWidth;
  }

  public Control getControl(Composite parent)
  {
    Assert.isLegal(
        defaultLabelControl == null,
        "can't use FormLayout, already used GridLayout!");

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

  public void fillIntoGrid(Composite parent, int numcols)
  {

    Assert.isLegal(
        defaultLabelControl == null,
        "can't use GridLayout, already used FormLayout!");

    Assert.isTrue(numcols >= 5);

    GridData data;

    PixelConverter converter = new PixelConverter(parent);

    Label labelControl = getLabelControl(parent);
    data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.horizontalSpan = 1;
    labelControl.setLayoutData(data);

    Text textControl = getTextControl(parent);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = numcols - 3;
    textControl.setLayoutData(data);

    Label defaultLabel = getDefaultLabelControl(parent);
    defaultLabel.setText("(default)");
    defaultLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
    data = new GridData(GridData.HORIZONTAL_ALIGN_END);
    data.horizontalSpan = 1;   
    data.widthHint = Math.max(converter.convertHorizontalDLUsToPixels("(default)"
        .length()), defaultLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    defaultLabel.setLayoutData(data);

    int heightHint = converter
        .convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
    int widthHint = converter
        .convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);

    Button buttonControl = getButtonControl(parent);
    data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.horizontalSpan = 1;
    data.heightHint = heightHint;
    data.widthHint = Math.max(widthHint, buttonControl.computeSize(
        SWT.DEFAULT,
        SWT.DEFAULT,
        true).x);
    buttonControl.setLayoutData(data);
  }

  public Label getDefaultLabelControl(Composite parent)
  {
    if (defaultLabelControl == null)
    {

      defaultLabelControl = new Label(parent, SWT.LEFT | SWT.WRAP);
      defaultLabelControl.setFont(parent.getFont());

      if (defaultValue != null && !"".equals(defaultValue))
      {
        defaultLabelControl.setText(defaultValue);
      } else
      {

        defaultLabelControl.setText("(default)");

      }
    }
    return defaultLabelControl;
  }

  public void setEnabled(boolean flag)
  {
    if (defaultLabelControl != null && !defaultLabelControl.isDisposed())
    {
      defaultLabelControl.setEnabled(flag);
    }
    super.setEnabled(flag);
  }

  public void setDefaultVisible(boolean flag)
  {
    if (defaultLabelControl != null && !defaultLabelControl.isDisposed())
    {
      defaultLabelControl.setVisible(flag);
    }
  }

  /**
   * Gets the defaultLabelControl.
   * 
   * @return Returns a Label
   */
  public String getDefaultLabelText()
  {
    if (defaultLabelControl != null && !defaultLabelControl.isDisposed())
    {
      return defaultLabelControl.getText();
    }
    return null;
  }

  /**
   * Sets the defaultLabelControl.
   * 
   * @param defaultLabelControl The defaultLabelControl to set
   */
  public void setDefaultLabelText(String text)
  {
    if (defaultLabelControl != null && !defaultLabelControl.isDisposed())
    {
      defaultLabelControl.setText(text);
    }
  }

}