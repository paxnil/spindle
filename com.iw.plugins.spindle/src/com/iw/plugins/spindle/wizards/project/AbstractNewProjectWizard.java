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
package com.iw.plugins.spindle.wizards.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.wizards.NewTapestryElementWizard;

public abstract class AbstractNewProjectWizard extends NewTapestryElementWizard {

  protected WizardNewProjectCreationPage mainPage;
  private NewJavaProjectWizardPage javaPage;

  private NewTapestryProjectPage tapestryPage;

  public AbstractNewProjectWizard() {
    super();
    setNeedsProgressMonitor(true);

  }
  
  public abstract NewTapestryProjectPage createTapestryPage(IWorkspaceRoot root);

  /**
   * @see Wizard#createPages
   */
  public void addPages() {
    mainPage = new WizardNewProjectCreationPage("Project Name");
    addPage(mainPage);

    javaPage = new NewJavaProjectWizardPage(ResourcesPlugin.getWorkspace().getRoot(), mainPage);
    addPage(javaPage);
    
    javaPage.setDescription(javaPage.getDescription()+" - add Tapestry and Servlet jars here.");

    IWorkspace workspace = TapestryPlugin.getDefault().getWorkspace();
    if (workspace == null) {
      throw new IllegalArgumentException();
    }
    tapestryPage = createTapestryPage(workspace.getRoot());
    addPage(tapestryPage);

  }

  /**
   * @see Wizard#performFinish()
   */
  public boolean performFinish() {

    if (finishPage(javaPage.getRunnable())) {

      IJavaProject project = javaPage.getNewJavaProject();
      try {
        project.open(null);
      } catch (JavaModelException e) {
      }

      if (finishPage(tapestryPage.getRunnable(javaPage.getNewJavaProject()))) {
        IFile file = (IFile) tapestryPage.getResource();
        try {
          selectAndReveal(file);
          openResource(file);
        } catch (Exception e) { // let pass, only reveal and open will fail
        }
      }

    }
    return true;
  }

  public IWizardPage getNextPage(IWizardPage page) {

    IWizardPage current = getContainer().getCurrentPage();
    if (current == javaPage) {

      IClasspathEntry[] entries = javaPage.getRawClassPath();
      IProject project = mainPage.getProjectHandle();
      if (entries.length > 0) {

        tapestryPage.init(entries, project);
      }

      return tapestryPage;
    }
    return super.getNextPage(page);
  }

}