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
package com.iw.plugins.spindle.ui.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.UpdateStatusContainer;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * @author GWL
 * @version Copyright 2002, Intelligent Works Incoporated All Rights Reserved
 */
public abstract class TapestryWizardPage extends WizardPage
{

  protected IStatus fCurrStatus = new SpindleStatus();

  private UpdateStatusContainer statusContainer = new UpdateStatusContainer();

  protected boolean fPageVisible;

  public TapestryWizardPage(String name)
  {
    super(name);
    fPageVisible = false;
  }

  // ---- WizardPage ----------------

  /*
   * @see WizardPage#becomesVisible
   */
  public void setVisible(boolean visible)
  {
    super.setVisible(visible);
    fPageVisible = visible;
    // policy: wizards are not allowed to come up with an error message
    //    if (visible && (fCurrStatus != null &&
    // fCurrStatus.matches(IStatus.ERROR)))
    //    {
    //      SpindleStatus status = new SpindleStatus();
    //      status.setError(""); //$NON-NLS-1$
    //      fCurrStatus = status;
    //    }
    updateStatus(fCurrStatus);
  }

  protected void updateStatus()
  {
    updateStatus(statusContainer.getStatus(true));
  }

  protected void refreshStatus()
  {
    statusContainer.refresh();
    updateStatus();
  }

  /**
   * Updates the status line and the ok button depending on the status
   */
  protected void updateStatus(IStatus status)
  {
    fCurrStatus = status;
    ;

    setPageComplete(fCurrStatus != null && !fCurrStatus.matches(IStatus.ERROR));
    if (fPageVisible)
    {
      applyToStatusLine(this, fCurrStatus);
    }
  }

  /**
   * Updates the status line and the ok button depending on the most severe
   * error. In case of two errors with the same severity, the status with lower
   * index is taken.
   */
  protected void updateStatus(IStatus[] status)
  {
    updateStatus(statusContainer.getStatus(true));
  }

  public IStatus getMoreSevere(IStatus s1, IStatus s2)
  {
    if (s1.getSeverity() > s2.getSeverity())
    {
      return s1;
    }
    return s2;

  }

  public void connect(DialogField field)
  {
    statusContainer.add(field);
  }

  public void disconnect(DialogField field)
  {
    statusContainer.remove(field);
  }

  /**
   * Finds the most severe status from a array of stati. An error is more severe
   * than a warning, and a warning is more severe than ok.
   */
  public IStatus getMostSevere(IStatus[] status)
  {
    IStatus max = null;
    for (int i = 0; i < status.length; i++)
    {
      IStatus curr = status[i];
      if (curr.matches(IStatus.ERROR))
      {
        return curr;
      }
      if (max == null || curr.getSeverity() > max.getSeverity())
      {
        max = curr;
      }
    }
    return max;
  }

  public void applyToStatusLine(WizardPage page, IStatus status)
  {
    if (status == null)
    {
      return;
    }
    String message = status.getMessage();
    switch (status.getSeverity())
    {
      case IStatus.OK :
        page.setErrorMessage(null);
        page.setMessage(message, NONE);
        break;
      case IStatus.WARNING :
        page.setErrorMessage(null);
        page.setMessage(message, WARNING);
        break;
      case IStatus.INFO :
        page.setErrorMessage(null);
        page.setMessage(message, INFORMATION);
        break;
      default :
        if (message.length() == 0)
        {
          message = null;
        }
        page.setErrorMessage(message);
        page.setMessage(null);
        break;
    }
  }

  protected void addControl(Control toBeAdded, Control parent, int verticalOffset)
  {
    UIUtils.addFormControl(toBeAdded, parent, verticalOffset);
  }

  protected void addControl(Control toBeAdded, Control parent)
  {
    UIUtils.addFormControl(toBeAdded, parent);
  }

  protected Control createSeparator(Composite container, Control parent)
  {
    return UIUtils.createFormSeparator(container, parent);
  }

  public IStatus getCurrentStatus()
  {
    return fCurrStatus;
  }

  public abstract IResource getResource();

  public abstract IRunnableWithProgress getRunnable(Object object);

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent)
  {
  }

}