package com.iw.plugins.spindle.project.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MigrationModelManager extends TapestryProjectModelManager {

  /**
   * Constructor for MigrationModelManager.
   * @param project
   */
  public MigrationModelManager(IProject project, String message) {
    super(project, message);
  }

  /**
   * @see com.iw.plugins.spindle.model.manager.TapestryProjectModelManager#initializeProjectTapestryModels()
   */
  protected void initializeProjectTapestryModels() {

    if (initialized) {
      return;
    }

    initialized = true;

    allModels = new ArrayList();

    Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();

    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

    try {

      dialog.run(false, false, new IRunnableWithProgress() {

        public void run(IProgressMonitor monitor)
          throws InvocationTargetException, InterruptedException {

          populateAllModels(
            project,
            TapestryLookup.ACCEPT_TAPESTRY_PROJECTS_ONLY | TapestryLookup.WRITEABLE,
            monitor);

          try {

            int acceptFlags = TapestryLookup.ACCEPT_LIBRARIES | TapestryLookup.FULL_TAPESTRY_PATH;

            IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(project);
            TapestryLookup lookup = new TapestryLookup();
            lookup.configure(jproject);

            IStorage storage = lookup.findDefaultLibrary();

            if (storage != null) {

              TapestryLibraryModel framework = new TapestryLibraryModel(storage);
              framework.load();
              setDefaultLibrary(framework);

            }

          } catch (CoreException e) {

            ErrorDialog.openError(
              TapestryPlugin.getDefault().getActiveWorkbenchShell(),
              "Migration Error",
              "could not find the Tapestry Project",
              e.getStatus());
          }
        }

      });

    } catch (InvocationTargetException e) {
    } catch (InterruptedException e) {
    } finally {

    }

  }

}
