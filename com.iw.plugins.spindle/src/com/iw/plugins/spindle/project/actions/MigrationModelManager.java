package com.iw.plugins.spindle.project.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
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
        }

      });

    } catch (InvocationTargetException e) {
    } catch (InterruptedException e) {
    } finally {
    	
      
    }

  }

}
