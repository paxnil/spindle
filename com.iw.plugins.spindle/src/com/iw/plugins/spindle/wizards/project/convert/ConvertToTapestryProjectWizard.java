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
package com.iw.plugins.spindle.wizards.project.convert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.actions.MigrateToTapestryDTD13;
import com.iw.plugins.spindle.wizards.NewTapestryElementWizard;
import com.iw.plugins.spindle.wizards.TapestryWizardPage;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class ConvertToTapestryProjectWizard extends NewTapestryElementWizard {

  static final public int CHOOSE_EXISTING = 0;
  static public final int CREATE_APPLICATION = 1;
  static public final int CREATE_LIBRARY = 2;
  static public final int ONE_EXISTS = 3;

  private ConversionWelcomePage welcomePage;
  private ChooseExistingPage chooseExistingPage;
  private CreateApplicationWizardPage newApplicationPage;
  private CreateLibraryWizardPage newLibraryPage;

  private QuestionPage questionPage;

  /**
   * Constructor for ConvertToTapestryProjectWizard.
   */
  public ConvertToTapestryProjectWizard() {
    super();
    setWindowTitle("Convert to Tapestry Project");
    setNeedsProgressMonitor(true);

  }

  /**
  * @see Wizard#createPages
  */
  public void addPages() {
    super.addPages();

    welcomePage = new ConversionWelcomePage("Welcome!", getSelection());
    chooseExistingPage =
      new ChooseExistingPage("Choose Existing Application or Library", getSelection());

    IWorkspace workspace = TapestryPlugin.getDefault().getWorkspace();
    if (workspace == null) {
      throw new IllegalArgumentException();
    }
    newApplicationPage = new CreateApplicationWizardPage(workspace.getRoot());
    newLibraryPage = new CreateLibraryWizardPage(workspace.getRoot());
    questionPage = new QuestionPage("Migrate Now?");

    addPage(welcomePage);
    addPage(chooseExistingPage);
    addPage(newApplicationPage);
    addPage(newLibraryPage);
    addPage(questionPage);
  }

  /**
   * @see Wizard#performFinish()
   */
  public boolean performFinish() {

    IRunnableWithProgress runnable = null;
    TapestryWizardPage runnablePage = null;

    switch (welcomePage.getState()) {

      case CREATE_APPLICATION :

        runnablePage = newApplicationPage;
        break;

      case CREATE_LIBRARY :

        runnablePage = newLibraryPage;
        break;

      default :

        runnablePage = welcomePage;
        break;

    }

    IFile projectResourceFile = null;

    if (runnablePage == welcomePage) {

      if (welcomePage.getState() == ONE_EXISTS) {

        projectResourceFile = (IFile) welcomePage.getResource();

      } else {

        projectResourceFile = (IFile) chooseExistingPage.getResource();

      }

    } else if (finishPage(runnablePage.getRunnable(null))) {

      projectResourceFile = (IFile) runnablePage.getResource();

    }

    if (finishPage(welcomePage.getRunnable(projectResourceFile))) {

      if (questionPage.getAnswer()) {

        runMigration();

      } else {

        try {

          selectAndReveal(projectResourceFile);
          openResource(projectResourceFile);

        } catch (Exception e) {
          // let pass, only reveal and open will fail
        }
      }

      return true;

    }

    return false;
  }

  /**
   * Method addJavaProjectNature.
   * @param file
   */
  private void addJavaProjectNature(IStorage file) throws CoreException {

    IJavaProject jproject = (IJavaProject) getSelection().getFirstElement();

  }

  /**
   * Method runMigration.
   */
  private void runMigration() {

      	MigrateToTapestryDTD13 migrationAction = new MigrateToTapestryDTD13();
      	
      	migrationAction.selectionChanged(migrationAction, getSelection());
      	
      	if (!migrationAction.isEnabled()) {
      		
      		MessageDialog.openInformation(getShell(), "Migration Aborted", "Migration refused to start");
      		
      	}
      	
      	migrationAction.run(null);

  }

  /**
   * @see IWizard#createPageControls(Composite)
   */
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
    IJavaElement initElement = getInitElement();
    newApplicationPage.init(initElement);
    newLibraryPage.init(initElement);
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getNextPage(IWizardPage)
   */
  public IWizardPage getNextPage(IWizardPage page) {
    if (page == welcomePage) {

      switch (welcomePage.getState()) {

        case CHOOSE_EXISTING :

          return chooseExistingPage;

        case CREATE_APPLICATION :

          return newApplicationPage;

        case CREATE_LIBRARY :

          return newLibraryPage;

        case ONE_EXISTS :

          return questionPage;

      }
    }

    if (page == newApplicationPage || page == newLibraryPage || page == chooseExistingPage) {

      return questionPage;

    }

    return super.getNextPage(page);
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#canFinish()
   */
  public boolean canFinish() {

    switch (welcomePage.getState()) {

      case CHOOSE_EXISTING :

        return chooseExistingPage.isPageComplete();

      case CREATE_APPLICATION :

        return newApplicationPage.isPageComplete();

      case CREATE_LIBRARY :

        return newLibraryPage.isPageComplete();

      case ONE_EXISTS :

        return true;

    }

    return false;

  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getStartingPage()
   */
  public IWizardPage getStartingPage() {
    return welcomePage;
  }

}
