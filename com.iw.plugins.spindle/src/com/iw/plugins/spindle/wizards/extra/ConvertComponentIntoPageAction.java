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

package com.iw.plugins.spindle.wizards.extra;

import java.util.Iterator;

import net.sf.tapestry.parse.SpecificationParser;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;

/**
 * Copyright 2002 Intelligent Works Inc.
 * All rights reserved
 * 
 * @author gwl
 * @version $Id$
 */
public class ConvertComponentIntoPageAction
  extends Action
  implements IObjectActionDelegate {

  private IWorkbenchPart part;
  private IStructuredSelection selection;
  private IPackageFragment fragment;

  /**
   * Constructor for AbstractCreateFromTemplateAction.
   */
  public ConvertComponentIntoPageAction() {
    super();
  }

  /**
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    part = targetPart;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {

    final Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();

    if (selection != null) {

      final IStructuredSelection useSelection = selection;

      IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

        public void run(IProgressMonitor monitor) throws CoreException {

          try {

            for (Iterator iter = useSelection.iterator(); iter.hasNext();) {

              IFile file = (IFile) iter.next();

              if (!checkPageExists(file)) {

                convertJWCToPage(file);
              }

            }
          } catch (ClassCastException e) {
            // do nothing
          } catch (CoreException e) {

            ErrorDialog.openError(shell, "Spindle Error", "Convert failed", e.getStatus());

            throw (e);
          }
        }
      };

      try {
        TapestryPlugin.getDefault().getWorkspace().run(runnable, null);
      } catch (CoreException e) {
      }
    }
  }

  /**
   * Method convertJWCToPage.
   * @param file
   */
  private void convertJWCToPage(IFile file) throws CoreException {

    ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(file);

    TapestryProjectModelManager mgr = project.getModelManager();

    TapestryLibraryModel projectModel = (TapestryLibraryModel) project.getProjectModel();

    TapestryComponentModel cmodel = null;

    if (mgr != null && projectModel != null) {

      cmodel = (TapestryComponentModel) mgr.getReadOnlyModel(file);

      if (cmodel.isLoaded()) {

        IEditorPart editor = Utils.getEditorFor(file);

        IStatus status = null;

        if (editor != null) {

          status = doConvertInEditor(editor, mgr, projectModel, cmodel);

        } else {

          status = doConvertInWorkspace(mgr, projectModel, cmodel);
        }

        if (!status.isOK()) {

          throw new CoreException(status);

        }
      }

    }
  }

  /**
   * Method doConvertInEditor.
   * @param project
   * @param projectModel
   * @param mgr
   * @param cmodel
   */
  private IStatus doConvertInEditor(
    IEditorPart editor,
    TapestryProjectModelManager mgr,
    TapestryLibraryModel projectModel,
    TapestryComponentModel cmodel)
    throws CoreException {

    SpindleStatus status = new SpindleStatus();

    if (editor.isDirty()) {

      RequiredSaveEditorAction saver = new RequiredSaveEditorAction(editor);
      if (!saver.save()) {
        return status;
      }
    }

    editor.getEditorSite().getPage().closeEditor(editor, true);

    TapestryComponentModel useModel =
      (TapestryComponentModel) ((SpindleMultipageEditor) editor).getModel();

    IResource newPage = convertModel(mgr, projectModel, useModel, status);

    if (newPage != null) {

      Utils.selectAndReveal(newPage, TapestryPlugin.getDefault().getActiveWorkbenchWindow());

    }

    return status;
  }

  /**
   * Method doConvertInWorkspace.
   * @param project
   * @param projectModel
   * @param mgr
   * @param cmodel
   */
  private IStatus doConvertInWorkspace(
    TapestryProjectModelManager mgr,
    TapestryLibraryModel projectModel,
    TapestryComponentModel cmodel)
    throws CoreException {

    Object consumer = new Object();
    SpindleStatus status = new SpindleStatus();

    TapestryComponentModel useModel =
      (TapestryComponentModel) mgr.getEditableModel(cmodel.getUnderlyingStorage(), consumer);

    IResource newPage = null;
    try {

      newPage = convertModel(mgr, projectModel, useModel, status);

    } finally {

      mgr.disconnect(cmodel.getUnderlyingStorage(), consumer);
      
      
    }

    if (newPage != null) {

      Utils.selectAndReveal(newPage, TapestryPlugin.getDefault().getActiveWorkbenchWindow());

    }

    return status;

  }

  /**
   * Method convertModel.
   * @param project
   * @param projectModel
   * @param useModel
   * @param status
   */
  private IResource convertModel(
    TapestryProjectModelManager mgr,
    TapestryLibraryModel projectModel,
    TapestryComponentModel useModel,
    SpindleStatus status) {

    int DTDVersion = XMLUtil.getDTDVersion(useModel.getPublicId());

    if (DTDVersion < XMLUtil.DTD_1_3) {

      useModel.setPublicId(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID);

    }

    IFile file = (IFile) useModel.getUnderlyingStorage();

    IPath jwcPath = file.getFullPath();

    IPath pagePath = jwcPath.uptoSegment(jwcPath.segmentCount() - 1);
    pagePath = pagePath.append(getName(file) + ".page");
    
    PluginComponentSpecification cspec = (PluginComponentSpecification)useModel.getComponentSpecification();
    
    cspec.setPageSpecification(true);
        
    
    Utils.saveModel(useModel, null);

    try {

      file.move(pagePath, true, null);

    } catch (CoreException e) {

      status.setError(e.getMessage());
      return null;
    }

    String tapestryPath = "/" + fragment.getElementName().replace('.', '/') + "/";
    tapestryPath += getName(file);

    updateProjectModel(mgr, projectModel, useModel, tapestryPath);

    return file;

  }

  /**
   * Method updateProjectModel.
   * @param project
   * @param projectModel
   * @param pagePath
   */
  private void updateProjectModel(
    TapestryProjectModelManager mgr,
    TapestryLibraryModel projectModel,
    TapestryComponentModel cmodel,
    String pagePath) {

    IStorage projectModelStorage = projectModel.getUnderlyingStorage();

    if (!projectModelStorage.isReadOnly()) {

      IFile projectFile = (IFile) projectModelStorage;

      IEditorPart editor = Utils.getEditorFor(projectFile);

      if (editor != null) {

        doUpdateProjectModelInEditor(editor, mgr, projectModel, cmodel, pagePath);

      } else {

        doUpdateProjectModelInWorkspace(mgr, projectModel, cmodel, pagePath);

      }

    }

  }

  /**
   * Method doUpdateProjectModelInEditor.
   * @param project
   * @param projectModel
   * @param pagePath
   */
  private void doUpdateProjectModelInEditor(
    IEditorPart editor,
    TapestryProjectModelManager mgr,
    TapestryLibraryModel projectModel,
    TapestryComponentModel cmodel,
    String pagePath) {

    if (!(editor instanceof SpindleMultipageEditor)) {

      return;

    }

    if (editor.isDirty()) {

      RequiredSaveEditorAction saver = new RequiredSaveEditorAction(editor);
      if (!saver.save()) {
        return;
      }
    }

   //ditor.getEditorSite().getPage().closeEditor(editor, true);

    TapestryLibraryModel useModel =
      (TapestryLibraryModel) ((SpindleMultipageEditor) editor).getModel();

    performUpdateProjectModel(useModel, cmodel, pagePath);
    
    editor.doSave(null);
  }

  /**
   * Method doUpdateProjectModelInWorkspace.
   * @param project
   * @param projectModel
   * @param pagePath
   */
  private void doUpdateProjectModelInWorkspace(
    TapestryProjectModelManager mgr,
    TapestryLibraryModel projectModel,
    TapestryComponentModel cmodel,
    String pagePath) {

    Object consumer = new Object();
    SpindleStatus status = new SpindleStatus();

    TapestryLibraryModel useModel =
      (TapestryLibraryModel) mgr.getEditableModel(projectModel.getUnderlyingStorage(), consumer);

    IResource newPage = null;
    try {

      performUpdateProjectModel(useModel, cmodel, pagePath);
      
      Utils.saveModel(useModel, null);

    } finally {

      mgr.disconnect(projectModel.getUnderlyingStorage(), consumer);
    }

  }

  /**
   * Method performUpdateProjectModel.
   * @param useModel
   * @param mgr
   * @param pagePath
   */
  private void performUpdateProjectModel(
    TapestryLibraryModel projectModel,
    TapestryComponentModel cmodel,
    String path) {

    String jwcPath = path + ".jwc";
    String pagePath = path + ".page";

    String componentAlias = projectModel.findComponentAlias(jwcPath);
    String newPageName;
    if (componentAlias == null) {

      newPageName = getName((IFile) cmodel.getUnderlyingStorage());

    } else {

      newPageName = componentAlias;

    }

    IPluginLibrarySpecification projectModelSpec =
      (IPluginLibrarySpecification) projectModel.getSpecification();

    if (projectModelSpec.getPageSpecificationPath(newPageName) != null) {

      int counter = 1;

      while (projectModelSpec.getPageSpecificationPath(newPageName + counter) != null) {

        counter++;

      }

      newPageName += counter;

    }
    
    if (componentAlias != null) {
    	
    	projectModelSpec.removeComponentSpecificationPath(componentAlias);
    	
    }
    
    projectModelSpec.setPageSpecificationPath(newPageName, pagePath);

  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection sel) {
    boolean enable = false;
    this.selection = null;

    IStructuredSelection selection = null;

    if (sel instanceof IStructuredSelection) {

      selection = (IStructuredSelection) sel;

      if (!selection.isEmpty()) {

        enable = checkSelection(selection);
      }

    }
    if (enable) {

      this.selection = selection;
    }
    action.setEnabled(enable);
  }

  /**
   * Method checkMultiSelection.
   * @param selection
   * @return boolean
   */
  private boolean checkSelection(IStructuredSelection selection) {
    boolean result = false;

    if (selection != null && !selection.isEmpty() && selection.size() == 1) {

      try {

        for (Iterator iter = selection.iterator(); iter.hasNext();) {

          IFile candidateFile = (IFile) iter.next();

          if (!candidateFile.isReadOnly() && !checkPageExists(candidateFile)) {

            try {

              ITapestryProject project =
                TapestryPlugin.getDefault().getTapestryProjectFor(candidateFile);

              TapestryProjectModelManager mgr = project.getModelManager();

              TapestryLibraryModel projectModel = (TapestryLibraryModel) project.getProjectModel();

              TapestryComponentModel cmodel = null;

              if (mgr != null && projectModel != null) {

                IPackageFragment modelFragment =
                  project.getLookup().findPackageFragment(candidateFile);

                if (modelFragment != null) {

                  fragment = modelFragment;

                  cmodel = (TapestryComponentModel) mgr.getReadOnlyModel(candidateFile);

                  if (cmodel.isLoaded()) {

                    result = true;

                  }
                }

              }

            } catch (CoreException e) {

            }
          }

        }
      } catch (ClassCastException e) {

      }
    }
    return result;
  }

  private String getName(IFile file) {

    IPath path = file.getFullPath();
    path = path.removeFileExtension();
    return path.lastSegment();
  }

  /**
   * Method checkPageExists.
   * @param file
   * @return boolean
   */
  private boolean checkPageExists(IFile file) {

    IContainer folder = file.getParent();
    String name = getName(file);

    return folder.findMember(name + ".page") != null;
  }

}