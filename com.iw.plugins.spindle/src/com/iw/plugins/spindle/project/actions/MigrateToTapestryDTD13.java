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



package com.iw.plugins.spindle.project.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.WorkspaceAction;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.migrate.MigrationContext;
import com.iw.plugins.spindle.util.SpindleMultiStatus;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.wizards.migrate.MigrationWizard;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class MigrateToTapestryDTD13 extends AbstractTapestryProjectAction {

  private MigrationModelManager mgr = null;
  private IProject project;

  /**
   * Constructor for MigrateToTapestryDTD13.
   */
  public MigrateToTapestryDTD13() {
    super();
  }

  protected boolean checkSelection(IStructuredSelection selection) {

    return true;

  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {

    Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();

    IJavaProject jproject = (IJavaProject) selection.getFirstElement();

    project = jproject.getProject();

    try {
      if (!project.hasNature(TapestryPlugin.NATURE_ID)) {

        reportProblem(shell, "You must convert the project to a Tapestry project before migrating");

        return;

      }

    } catch (CoreException e) {

      reportError(e.getStatus());

      return;
    }

    if (!checkHasServletJars(jproject)) {

      MessageDialog.openInformation(
        shell,
        "Conversion Problem",
        "Can't continue with conversion.\n\nAdd:\n\n javax.servlet.jar\n\n to the project build path.\n\nThen try converting again");

      return;
    }


    if (!checkHasTapestryJars(jproject)) {

      MessageDialog.openInformation(
        shell,
        "Conversion Problem",
        "Can't continue with conversion.\n\nAdd:\n\n net.sf.tapestry.jar (2.2 or better)\n\n to the project build path.\n\nThen try converting again");

      return;
    }

    StructuredSelection actionSelection = new StructuredSelection(project);

    WorkspaceAction closeAction = new CloseResourceAction(shell);

    closeAction.selectionChanged(actionSelection);

    closeAction.run();

    if (project.isOpen()) {

      return;
    }

    WorkspaceAction openAction = new OpenResourceAction(shell);

    openAction.selectionChanged(actionSelection);

    openAction.run();

    ITapestryProject tproject = null;
    try {

      tproject = (ITapestryProject) project.getNature(TapestryPlugin.NATURE_ID);

    } catch (CoreException e) {

      //the action has already checked to ensure the project exists.
    }

    findAllModels();

    List badModels = mgr.checkForUnloadableModels();

    if (!badModels.isEmpty()) {

      reportModelErrors(badModels);
      return;

    }
    if (!checkForMultipleLibsApps(tproject)) {

      return;

    }

    launchWizard(shell, tproject);
    
    
  }

  // return true iff the migration should continue
  public boolean checkForMultipleLibsApps(ITapestryProject tproject) {

    ITapestryModel projectModel = null;

    try {

      projectModel = (ITapestryModel) mgr.getReadOnlyModel(tproject.getProjectStorage());

    } catch (CoreException e) {

      SpindleMultiStatus status = new SpindleMultiStatus();
      status.setError("Could not open Project Application or Library");

      status.addStatus(e.getStatus());

      reportError(status);

      return true;

    }

    List allApplications = mgr.getAllModels(null, "application");
    List allLibraries = mgr.getAllModels(null, "library");

    ArrayList badGuys = new ArrayList();

    if (allApplications != null) {

      allApplications.remove(projectModel);
      badGuys.addAll(allApplications);

    }

    if (allLibraries != null) {

      allLibraries.remove(projectModel);
      badGuys.addAll(allLibraries);

    }

    if (!badGuys.isEmpty()) {

      IWorkbenchWindow window = TapestryPlugin.getDefault().getActiveWorkbenchWindow();
      
      if (projectModel.getUnderlyingStorage() instanceof IResource) {
      	
      	Utils.selectAndReveal((IResource)projectModel.getUnderlyingStorage(), window);
      	
      }

      SpindleMultiStatus multi = new SpindleMultiStatus();

      multi.setError(
        "Tapestry Projects allow only one Application or Library.\nSee Details for offending files");

      for (Iterator iter = badGuys.iterator(); iter.hasNext();) {
        ITapestryModel element = (ITapestryModel) iter.next();

        multi.addStatus(
          new SpindleStatus(multi.ERROR, element.getUnderlyingStorage().getFullPath().toString()));

        IStorage underlier = element.getUnderlyingStorage();

        if (underlier instanceof IResource) {

          Utils.selectAndReveal((IResource) underlier, window);
        }

      }

      reportError(multi);

      return false;

    }

    return true;
  }

  public void reportProblem(Shell shell, String message) {
    MessageDialog.openInformation(shell, "Migration Problem", message);
  }

  /**
   * Method reportModelErrors.
   * @param modelWithErrors
   */
  private void reportModelErrors(List modelWithErrors) {

    final ArrayList statii = new ArrayList();

    if (!modelWithErrors.isEmpty()) {

      IWorkbenchWindow window = TapestryPlugin.getDefault().getActiveWorkbenchWindow();

      SpindleMultiStatus multiStatus = new SpindleMultiStatus();
      multiStatus.setError(
        "Files have parse errors. See Details and fix the problems before migrating.");

      for (Iterator iter = modelWithErrors.iterator(); iter.hasNext();) {
        IStorage element = (IStorage) iter.next();
        SpindleStatus status = new SpindleStatus();
        status.setError(element.getFullPath().toString());
        multiStatus.addStatus(status);

        if (element instanceof IResource) {

          Utils.selectAndReveal((IResource) element, window);
        }
      }

      reportError(multiStatus);
    }
  }

  private void reportError(IStatus status) {

    ErrorDialog.openError(
      TapestryPlugin.getDefault().getActiveWorkbenchShell(),
      "Migration Error",
      "Can't continue.",
      status);
  }

  private void findAllModels() {

    ProgressMonitorDialog dialog =
      new ProgressMonitorDialog(TapestryPlugin.getDefault().getActiveWorkbenchShell());

    ModelCollectorRunnable runnable = new ModelCollectorRunnable();

    try {

      dialog.run(false, false, runnable);
      mgr = runnable.getResult();

    } catch (InvocationTargetException e) {

    } catch (InterruptedException e) {
    }

  }

  class ModelCollectorRunnable implements IRunnableWithProgress {

    private MigrationModelManager result = null;

    public MigrationModelManager getResult() {

      return result;

    }

    public void run(IProgressMonitor monitor)
      throws InvocationTargetException, InterruptedException {

      result = new MigrationModelManager(project, "Collecting Tapestry Files for Migration...");
      result.buildModelDelegates();

    }

  }

  private void launchWizard(Shell shell, ITapestryProject tproject) {
  	
   try {
   	
      TapestryLibraryModel projectModel = (TapestryLibraryModel)mgr.getReadOnlyModel(tproject.getProjectStorage());
      
   	  MigrationContext context = 
   	  new MigrationContext("Spindle Migrator for Tapestry", projectModel, mgr, tproject.getLookup(), MigrationContext.MIGRATE_DTD, XMLUtil.DTD_1_3);
   	  
   	  WizardDialog dialog = new WizardDialog(shell, new MigrationWizard(context));
   	  
   	  if (dialog.open() != dialog.OK) {
   	  	
   	  	return;
   	  	
   	  }

      
    } catch (CoreException e) {
    	
    	e.printStackTrace();
    }
   
    

  }

  private IRunnableWithProgress createRunnable() {

    return null;
    //    final ITapestryModel[] useModelsToSave = migratedModelsToSave;
    //    return new IRunnableWithProgress() {
    //      public void run(IProgressMonitor pm) {
    //        TapestryProjectModelManager mgr = TapestryPlugin.getTapestryModelManager();
    //        pm.beginTask(MessageUtil.getFormattedString(NAME + ".migrationCount", Integer.toString(migratedModelsToSave.length)), 1); //$NON-NLS-1$
    //        for (int i = 0; i < useModelsToSave.length; i++) {
    //          Utils.saveModel(useModelsToSave[i], pm);
    //          mgr.disconnect(useModelsToSave[i].getUnderlyingStorage(), MigrateAllTo204PlusDTD.this);
    //          pm.worked(1);
    //        }
    //        pm.done();
    //      }
    //    };
  }

}
