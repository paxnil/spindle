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
import com.iw.plugins.spindle.ui.migrate.MigrationWorkUnit;
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

    if (!checkHasTapestryJars(jproject)) {

      reportProblem(
        shell,
        "Can't continue with migration.\nAdd:\n\n javax.servlet.jar; and\n net.sf.tapestry.jar (2.2 or better)\n\n to the project build path.\n\nThen try converting again");

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

    MigrationWorkUnit[] workUnits = findDirtyWorkUnits(shell, tproject);
    
    if (workUnits == null) {
    	
    	return;
    	
    }

    if (workUnits.length == 0) {

      MessageDialog.openInformation(shell, "Migration Results", "No files needed migration");
      return;

    } else {

      //    try {
      //
      //      new ProgressMonitorDialog(shell).run(false, false, createRunnable());
      //
      //    } catch (InvocationTargetException e) {
      //
      //      MessageDialog.openError(
      //        shell.getShell(),
      //        "Migration Error",
      //        "Could not complete the migration");
      //
      //      e.printStackTrace();
      //      e.getTargetException().printStackTrace();
      //      return;
      //
      //    } catch (InterruptedException e) {
      //    }

    }

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

  private MigrationWorkUnit[] findDirtyWorkUnits(Shell shell, ITapestryProject tproject) {
  	
   try {
   	
      TapestryLibraryModel projectModel = (TapestryLibraryModel)mgr.getReadOnlyModel(tproject.getProjectStorage());
      
   	  MigrationContext context = 
   	  new MigrationContext("Spindle Migrator for Tapestry", projectModel, mgr, tproject.getLookup(), MigrationContext.MIGRATE_DTD, XMLUtil.DTD_1_3);
   	  
   	  WizardDialog dialog = new WizardDialog(shell, new MigrationWizard(context));
   	  
   	  if (dialog.open() != dialog.OK) {
   	  	
   	  	return null;
   	  	
   	  }

      
    } catch (CoreException e) {
    	
    	e.printStackTrace();
    }
   
    return new MigrationWorkUnit[0];

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
