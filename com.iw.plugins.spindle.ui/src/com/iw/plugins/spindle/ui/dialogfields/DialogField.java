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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.SpindleStatus;

/**
 * @author GWL
 * @version Copyright 2002, Intelligent Works Incoporated All Rights Reserved
 */
public class DialogField implements IDialogFieldChangedListener
{
  private IStatus status = new SpindleStatus();
  private String labelText;
  private int labelWidth;
  private List listeners = new ArrayList();
  private Label labelControl;

  private boolean enabled = true;

  public DialogField(String labelText)
  {

    this(labelText, -1);

  }

  public DialogField(String labelText, int labelWidth)
  {

    this.labelText = labelText == null ? "" : labelText;
    this.labelWidth = labelWidth;
    addListener(this);
  }

  public Control getControl(Composite parent)
  {

    Assert
        .isLegal(labelControl == null, "can't use FormLayout, already used GridLayout!");

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

  public void fillIntoGrid(Composite parent, int numcols)
  {
    Assert
        .isLegal(labelControl == null, "can't use GridLayout, already used FormLayout!");
    Assert.isTrue(numcols >= 1);

    labelControl = getLabelControl(parent);
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.horizontalSpan = 1;
    labelControl.setLayoutData(data);
  }

  protected int getLabelWidth()
  {
    return labelWidth;
  }

  public Label getLabelControl(Composite parent)
  {
    if (labelControl == null)
    {

      labelControl = new Label(parent, SWT.LEFT | SWT.WRAP);
      labelControl.setFont(parent.getFont());

      if (labelText != null && !"".equals(labelText))
      {
        labelControl.setText(labelText);
      } else
      {

        labelControl.setText(".");
        labelControl.setVisible(false);
      }
    }
    return labelControl;
  }

  /**
   * Gets the labelText.
   * 
   * @return Returns a String
   */
  public String getLabelText()
  {
    if (labelControl != null && !labelControl.isDisposed())
    {
      return labelControl.getText();
    }
    return labelText;
  }

  /**
   * Sets the labelText.
   * 
   * @param labelText The labelText to set
   */
  public void setLabelText(String labelText)
  {
    if (labelControl != null && !labelControl.isDisposed())
    {
      labelControl.setText(labelText);
      fireDialogFieldChanged(this);
    }
  }

  protected void fireDialogFieldChanged(DialogField field)
  {
    for (Iterator iterator = getListeners().iterator(); iterator.hasNext();)
    {
      IDialogFieldChangedListener element = (IDialogFieldChangedListener) iterator.next();
      element.dialogFieldChanged(field);
    }
  }

  protected void fireDialogButtonPressed(DialogField field)
  {
    for (Iterator iterator = getListeners().iterator(); iterator.hasNext();)
    {
      IDialogFieldChangedListener element = (IDialogFieldChangedListener) iterator.next();
      element.dialogFieldButtonPressed(field);
    }
  }

  protected void fireDialogFieldStatusChanged(IStatus status, DialogField field)
  {
    for (Iterator iterator = getListeners().iterator(); iterator.hasNext();)
    {
      IDialogFieldChangedListener element = (IDialogFieldChangedListener) iterator.next();
      element.dialogFieldStatusChanged(status, field);
    }
  }

  public IStatus getStatus()
  {
    return status;
  }

  public void setStatus(IStatus newStatus)
  {
    status = newStatus;
    fireDialogFieldStatusChanged(status, this);
  }

  public void setEnabled(boolean flag)
  {
    if (labelControl != null && !labelControl.isDisposed())
    {
      labelControl.setEnabled(flag);
    }
    enabled = flag;
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void addListener(IDialogFieldChangedListener listener)
  {
    if (!listeners.contains(listener))
    {
      listeners.add(listener);
    }
  }

  public void removeListener(IDialogFieldChangedListener listener)
  {
    listeners.remove(listener);
  }

  public List getListeners()
  {
    return Collections.unmodifiableList(listeners);
  }

  public Shell getShell()
  {
    if (labelControl != null && !labelControl.isDisposed())
    {
      return labelControl.getShell();
    }
    return null;
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field)
  {
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldChanged(DialogField)
   */
  public void dialogFieldChanged(DialogField field)
  {
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus,
   *              DialogField)
   */
  public void dialogFieldStatusChanged(IStatus status, DialogField field)
  {
  }

  public boolean setFocus()
  {
    return true;
  }

  /**
   * @return
   */
  public boolean isVisible()
  {
    return labelControl != null && !labelControl.isDisposed() && labelControl.isVisible();
  }

  public void setVisible(boolean flag)
  {
    if (labelControl != null && !labelControl.isDisposed())
      labelControl.setVisible(flag);
  }

  public void refreshStatus()
  {

  }

}