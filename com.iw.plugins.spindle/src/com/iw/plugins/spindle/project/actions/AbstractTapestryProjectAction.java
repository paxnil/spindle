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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.iw.plugins.spindle.TapestryPlugin;

/**
 * Copyright 2002 Intelligent Works Inc.
 * All rights reserved
 * 
 * @author gwl
 * @version $Id$
 */
public abstract class AbstractTapestryProjectAction
  extends Action
  implements IObjectActionDelegate {

  private IWorkbenchPart part;
  protected IStructuredSelection selection;

  /**
   * Constructor for AbstractCreateFromTemplateAction.
   */
  public AbstractTapestryProjectAction() {
    super();
  }

  /**
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    part = targetPart;
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

      if (selection != null && !selection.isEmpty() && selection.size() == 1) {

        if (checkProjectIsOpenAndHasJavaNature(selection)) {

          enable = checkSelection(selection);
        }
      }

    }
    if (enable) {

      this.selection = selection;
    }
    action.setEnabled(enable);
  }

  private boolean checkProjectIsOpenAndHasJavaNature(IStructuredSelection selection) {

    IJavaProject jproject = (IJavaProject) selection.getFirstElement();

    IProject project = jproject.getProject();

    return project.isOpen();

  }

  protected boolean checkSelection(IStructuredSelection selection) {

    IJavaProject jproject = (IJavaProject) selection.getFirstElement();

    IProject project = jproject.getProject();

    try {

      return !project.hasNature(TapestryPlugin.NATURE_ID);

    } catch (CoreException e) {

      return false;
    }

  }

  protected boolean checkHasServletJars(IJavaProject project) {

    try {

      return project.findType("javax.servlet.Servlet") != null;
    } catch (JavaModelException e) {

      Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();
      ErrorDialog.openError(
        shell,
        "Spindle error",
        "could not determine if required servlet jar is present",
        e.getStatus());
    }
    return false;

  }

  protected boolean checkHasTapestryJars(IJavaProject project) {

    try {

      return project.findType( "net.sf.tapestry.spec.IApplicationSpecification") != null;

    } catch (JavaModelException e) {

      Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();
      ErrorDialog.openError(
        shell,
        "Spindle error",
        "could not determine if required tapestry jar is present",
        e.getStatus());
    }

    return false;
  }

  private String getName(IProject project) {

    IPath path = project.getFullPath();
    path = path.removeFileExtension();
    return path.lastSegment();

  }

}