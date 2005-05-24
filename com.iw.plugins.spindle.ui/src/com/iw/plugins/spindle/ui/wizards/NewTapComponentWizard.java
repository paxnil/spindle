/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.wizards;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;

public class NewTapComponentWizard extends NewTapestryElementWizard
{

  protected NewTapComponentWizardPage fPage1;
  protected TypeChooseWizardPage fPage2;

  public NewTapComponentWizard()
  {
    super();
  }

  /**
   * @see Wizard#createPages
   */
  public void addPages()
  {
    super.addPages();
    IWorkspace workspace = UIPlugin.getWorkspace();
    if (workspace == null)
    {
      throw new IllegalArgumentException();
    }
    fPage1 = new NewTapComponentWizardPage(
        workspace.getRoot(),
        "FirstComponentWizardPage");
    addPage(fPage1);
    fPage2 = new TypeChooseWizardPage("SecondComponentWizardPage", fPage1);
    addPage(fPage2);
  }

  /**
   * @see Wizard#performFinish()
   */
  public boolean performFinish()
  {
    IFile template = null;
    IFile spec = null;
    IFile java = null;

    spec = (IFile) fPage1.getResource();
    template = (IFile) fPage1.getTemplateFile();

    ResourceCreationRunnable createRunnable = new ResourceCreationRunnable();
    // the new spec file;
    createRunnable.addFile(spec);
    if (template != null)
      createRunnable.addFile(template);
    
    

    IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(createRunnable);
    try
    {
      getContainer().run(false, true, op);
    } catch (Exception e)
    {
      Throwable cause = e.getCause();
      if (cause != null && cause instanceof CoreException)
      {
        ErrorDialog.openError(
            getShell(),
            "Operation Failed",
            "see details",
            ((CoreException) cause).getStatus());
      }
      //undo
      try
      {
        createRunnable.setMode(createRunnable.UNDO);
        getContainer().run(false, true, op);
      } catch (Exception e1)
      {
        //eat it
        UIPlugin.log_it(e1);
      } finally
      {
        fPage1.clearResource();
        fPage1.clearTemplateFile();
      }
      return false;
    }

    try
    {
      IRunnableWithProgress autoAdd = null;
      try
      {
        autoAdd = fPage1.getAutoAddRunnable();
      } catch (CoreException e1)
      {
        ErrorDialog.openError(getShell(), "Operation Failed", null, e1.getStatus());
        return false;
      } catch (IOException e1)
      {
        SpindleStatus status = new SpindleStatus();
        status.setError(e1.getMessage());
        ErrorDialog.openError(
            getShell(),
            "Operation Failed",
            e1.getClass().getName(),
            status);
      }

      if (finishPage(fPage2.getRunnable(null)))
      {
        java = fPage2.getGeneratedJavaFile();

        if (finishPage(fPage1.getRunnable(fPage2.getFinalSpecClass())))
        {
          spec = (IFile) fPage1.getResource();
          template = fPage1.getTemplateFile();
          finishPage(autoAdd);
        }
      }

      if (java != null)
      {
        try
        {
          openResource(java);
        } catch (Exception e)
        { // let pass, only reveal and open will fail
        }
      }
      if (template != null)
      {
        try
        {
          openResource(template);
        } catch (Exception e)
        { // let pass, only reveal and open will fail
        }
      }

      try
      {
        selectAndReveal(spec);
        openResource(spec);
      } catch (Exception e)
      { // let pass, only reveal and open will fail
      }

      fPage1.performFinish();
      fPage2.performFinish();
      return true;
    } catch (Exception e)
    {
      UIPlugin.log_it(e);
      MessageDialog.openError(
          getShell(),
          "Operation Failed",
          "Could not complete the 'Finish'. Please check the log.");
      return false;
    }
  }
  /**
   * @see IWizard#createPageControls(Composite)
   */
  public void createPageControls(Composite pageContainer)
  {
    super.createPageControls(pageContainer);
    setWindowTitle(UIPlugin.getString("NewTapComponentWizard.windowtitle"));
    fPage1.init(getInitJavaElement(), getInitResource(), prepopulateName);    
  }

}