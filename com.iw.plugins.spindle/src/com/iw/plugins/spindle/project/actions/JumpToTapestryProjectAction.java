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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.util.SpindleMultiStatus;
import com.iw.plugins.spindle.util.Utils;

/**
 * Copyright 2002 Intelligent Works Inc.
 * All rights reserved
 * 
 * @author gwl
 * @version $Id$
 */
public class JumpToTapestryProjectAction extends AbstractTapestryProjectAction {

  /**
   * Constructor for OpenTapestryProjectAction.
   */
  public JumpToTapestryProjectAction() {
    super();
  }

  protected boolean checkSelection(IStructuredSelection selection) {

    return true;

  }

  public void run(IAction action) {

    Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();
    IJavaProject jproject = (IJavaProject) selection.getFirstElement();
    IProject project = jproject.getProject();

    ITapestryModel projectModel = null;
    try {

      ITapestryProject tproject = TapestryPlugin.getDefault().getTapestryProjectFor(project);

      projectModel = tproject.getProjectModel();

    } catch (CoreException e) {

      SpindleMultiStatus status = new SpindleMultiStatus(SpindleMultiStatus.ERROR, "could not open project resource");
      status.addStatus(e.getStatus());

      ErrorDialog.openError(shell, "Spindle Error", "open project application/library failed.", status);

    }

    if (projectModel != null) {

      IStorage storage = projectModel.getUnderlyingStorage();

      IEditorPart editor = Utils.getEditorFor(storage);

      if (editor != null) {

        TapestryPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().bringToTop(editor);

      } else {

        TapestryPlugin.getDefault().openTapestryEditor(storage);

      }
    }
  }

}