
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
package com.iw.plugins.spindle.ui.migrate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.tapestry.link.Action;
import net.sf.tapestry.spec.ApplicationSpecification;
import net.sf.tapestry.spec.ComponentSpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryModelManager;
import com.iw.plugins.spindle.spec.IMigratable;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.TapestryLookup;
import com.iw.plugins.spindle.util.Utils;


public class MigrateAllTo204PlusDTD extends Action implements IWorkbenchWindowActionDelegate {
	
  static public final String NAME = "MigrateAllTo204PlusDTD";

  IJavaProject jproject = null;
  IWorkbenchWindow window = null;
  ITapestryModel[] migratedModelsToSave = null;

  /**
   * Constructor for MigrateAllTo204PlusDTD. 
   */
  public MigrateAllTo204PlusDTD() {
    super();
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    this.window = window;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    if (Utils.getOpenEditors().length > 0) {
      MessageDialog.openError(
        window.getShell(),
        MessageUtil.getString(NAME+".errorTitle"),
        MessageUtil.getString(NAME+".errorCloseEditors"));
      return;
    }
    
    if (!MessageDialog.openQuestion(window.getShell(),MessageUtil.getString(NAME+".proceedQuestionTitle") ,MessageUtil.getString(NAME+".proceedQuestion"))) {
    	return;
    }

    migratedModelsToSave = findMigratedModels();
    if (migratedModelsToSave == null || migratedModelsToSave.length == 0) {
      MessageDialog.openInformation(
        window.getShell(),
        MessageUtil.getString(NAME+".errorTitle"),
        MessageUtil.getString(NAME+".errorNoMigratableFound"));
      return;
    }
    try {
      new ProgressMonitorDialog(window.getShell()).run(false, false, createRunnable());
    } catch (InvocationTargetException e) {
      MessageDialog.openError(
        window.getShell(),
        MessageUtil.getString(NAME+".errorTitle"),
        MessageUtil.getString(NAME+".errorStartingMigration"));

      e.printStackTrace();
      e.getTargetException().printStackTrace();
      return;
    } catch (InterruptedException e) {
    }

  }

  /**
   * Method findMigratableModels.
   * @return ITapestryModel[]
   */
  private ITapestryModel[] findMigratedModels() {
    try {
      TapestryLookup lookup = new TapestryLookup();
      lookup.configure(jproject);
      MigrationWorker requestor = new MigrationWorker(new MigrateTo204());
      lookup.findAll("*", true, TapestryLookup.ACCEPT_APPLICATIONS | TapestryLookup.ACCEPT_COMPONENTS, requestor);
      return requestor.getResults();
    } catch (JavaModelException e) {
    }
    return new ITapestryModel[0];

  }

  private IRunnableWithProgress createRunnable() {
    final ITapestryModel[] useModelsToSave = migratedModelsToSave;
    return new IRunnableWithProgress() {
      public void run(IProgressMonitor pm) {      	
        pm.beginTask(MessageUtil.getFormattedString(NAME+".migrationCount", Integer.toString(migratedModelsToSave.length)), 1); //$NON-NLS-1$
        for (int i = 0; i < useModelsToSave.length; i++) {
          Utils.saveModel(useModelsToSave[i], pm);
          pm.worked(1);
        }
        pm.done();
      }
    };
  }

  

  
  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    jproject = null;
    if (!selection.isEmpty() && selection instanceof IStructuredSelection) {

      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      if (structuredSelection.size() == 1) {

        Object first = structuredSelection.getFirstElement();
        IResource resource = null;
        try {

          resource = (IResource) first;

        } catch (Exception e) {

          if (first instanceof IAdaptable) {
            resource = (IResource) ((IAdaptable) first).getAdapter(IResource.class);
          }
        }
        if (resource != null) {

          if (resource.getType() == IResource.PROJECT) {

            jproject = (IJavaProject) JavaCore.create(resource);

          }

        }

      }

    }
    action.setEnabled(jproject != null);
  }

  protected class MigrationWorker implements ITapestryLookupRequestor {

    List results = new ArrayList();
    TapestryModelManager manager = TapestryPlugin.getTapestryModelManager();
    IModelMigrator migrator;
    /**
     * Constructor for MigrationLookupRequestor.
     */
    public MigrationWorker(IModelMigrator migrator) {
      this.migrator = migrator;
    }

    /**
    * @see com.iw.plugins.spindle.util.ITapestryLookupRequestor#accept(IStorage, IPackageFragment)
    */
    public boolean accept(IStorage storage, IPackageFragment frgament) {
      if (storage.isReadOnly()) {
      	return false;
      }
      ITapestryModel model = manager.getModel(storage);
      if (model != null && model.isLoaded() && migrator.migrate(model)) {
        results.add(model);
      }
      return true;
    }

    /**
     * @see com.iw.plugins.spindle.util.ITapestryLookupRequestor#isCancelled()
     */
    public boolean isCancelled() {
      return false;
    }

    public ITapestryModel[] getResults() {
      return (ITapestryModel[]) results.toArray(new ITapestryModel[results.size()]);
    }

  }

}
