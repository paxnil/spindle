package com.iw.plugins.spindle.project.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.WorkspaceAction;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.ui.migrate.MigrateTo204;
import com.iw.plugins.spindle.ui.migrate.MigrationWorker;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class MigrateToTapestryDTD13 extends AbstractTapestryProjectAction {

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

    IProject project = jproject.getProject();

    try {
      if (!project.hasNature(TapestryPlugin.NATURE_ID)) {

        MessageDialog.openInformation(
          shell,
          "Migration Problem",
          "You must convert the project to a Tapestry project before migrating");

        return;

      }
    } catch (CoreException e) {

      ErrorDialog.openError(shell, "Migration Error", e.getMessage(), e.getStatus());

      return;
    }

    if (!checkHasTapestryJars(jproject)) {

      MessageDialog.openInformation(
        shell,
        "Migration Problem",
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
    }

    ITapestryModel[] changedModels = findMigratedModels(tproject);

    if (changedModels.length == 0) {

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

  private ITapestryModel[] findMigratedModels(ITapestryProject tproject) {
    try {

      TapestryLookup lookup = tproject.getLookup();
      MigrationWorker worker = new MigrationWorker(new MigrateTo204(), tproject.getModelManager());
      lookup.findAll(
        "*",
        true,
        TapestryLookup.ACCEPT_APPLICATIONS | TapestryLookup.ACCEPT_COMPONENTS,
        worker);
      return worker.getResults();
    } catch (CoreException e) {

      ErrorDialog.openError(
        TapestryPlugin.getDefault().getActiveWorkbenchShell(),
        "MigrationError",
        "Can't continue migration",
        e.getStatus());
    }
    return new ITapestryModel[0];

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
