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

package com.iw.plugins.spindle.ui.wizards.project;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.ui.wizards.NewTapestryElementWizard;

/**
 * Wizard for creating new Tapestry projects.
 * 
 * @author glongman@intelligentworks.com
 */
public class NewTapestryProjectWizard extends NewTapestryElementWizard
{
  private NewTapestryProjectPage fMainPage;
  private NewTapestryProjectJavaPage fJavaPage;
  private TemplateSelectionPage fTemplatePage;

  private boolean beenThere = false;

  public NewTapestryProjectWizard()
  {
    super();
    setNeedsProgressMonitor(true);
    setWindowTitle(UIPlugin.getString("new-project-wizard-window-title"));
  }

  /**
   * @see Wizard#createPages
   */
  public void addPages()
  {
    ImageDescriptor descriptor = Images.getImageDescriptor("applicationDialog.gif");

    fTemplatePage = new TemplateSelectionPage("FileGeneration");

    fMainPage = new NewTapestryProjectPage(UIPlugin
        .getString("new-project-wizard-page-title"), this, fTemplatePage);

    fMainPage.setImageDescriptor(descriptor);
    fMainPage.setDescription(UIPlugin.getString("new-project-wizard-page-title"));

    addPage(fMainPage);
    addPage(fTemplatePage);

    fJavaPage = new NewTapestryProjectJavaPage(fMainPage);
    fJavaPage.setImageDescriptor(descriptor);
    addPage(fJavaPage);
    // bug [ 843021 ] Is this what 3 Beta is supposed to do
    // for some reason sometimes the page's wizard is not set
    //
    // should have been set by addPagr()
    fJavaPage.setWizard(this);
  }

  /**
   * @see Wizard#performFinish()
   */
  public boolean performFinish()
  {
    if (finishPage(fJavaPage.getRunnable()))
    {
      IJavaProject jproject = fJavaPage.getJavaProject();
      try
      {
        jproject.open(null);
        finishPage(fMainPage.getRunnable(jproject));
        IResource[] reveal = fMainPage.getReveal();
        for (int i = 0; i < reveal.length; i++)
        {
          selectAndReveal(reveal[i]);
        }
      } catch (JavaModelException e)
      {
        UIPlugin.log(e);
      }
    }
    return true;
  }
  

  /*
   * (non-Javadoc)
   * 
   * @see IWizard#performCancel()
   */
  public boolean performCancel()
  {
    fJavaPage.performCancel();
    return super.performCancel();
  }

  /**
   * @return
   */
  public String getContextFolderName()
  {
    return fMainPage.getContextFolderName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.wizard.IWizard#canFinish()
   */
  public boolean canFinish()
  {
    IWizardPage currentPage = getContainer().getCurrentPage();
    if (currentPage == fMainPage || currentPage == fTemplatePage)
      return false;
    return super.canFinish();
  }

}