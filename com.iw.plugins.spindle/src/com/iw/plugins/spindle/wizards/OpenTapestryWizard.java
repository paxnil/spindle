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
package com.iw.plugins.spindle.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.wizards.AbstractOpenWizardAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

import com.iw.plugins.spindle.TapestryPlugin;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public abstract class OpenTapestryWizard extends AbstractOpenWizardAction {

  /**
   * Constructor for OpenTapestryWizard.
   * @param label
   * @param acceptEmptySelection
   */
  public OpenTapestryWizard(String label, boolean acceptEmptySelection) {
    super(label, acceptEmptySelection);
  }

  /**
   * Constructor for OpenTapestryWizard.
   */
  protected OpenTapestryWizard() {
    super();
  }

  /**
   * Constructor for OpenTapestryWizard.
   * @param label
   * @param activatedOnTypes
   * @param acceptEmptySelection
   */
  public OpenTapestryWizard(String label, Class[] activatedOnTypes, boolean acceptEmptySelection) {
    super(label, activatedOnTypes, acceptEmptySelection);
  }
  
  public void run() {
    if (checkElement()) {
      super.run();
    }
  }

  protected boolean checkElement() {
    IJavaElement initElement = getInitElement();
    if (initElement == null) {
      MessageDialog.openError(
        TapestryPlugin.getDefault().getActiveWorkbenchShell(),
        "Error opening Spindle wizard!",
        "Unable to determine which project to operate on! \nChoose or edit a file in a java project, then re-launch the wizard.");
      return false;
    }
    return true;
  }

  protected IStructuredSelection getCurrentSelection() {
    IWorkbenchWindow window = TapestryPlugin.getDefault().getActiveWorkbenchWindow();
    if (window != null) {
      ISelection selection = window.getSelectionService().getSelection();
      if (selection instanceof IStructuredSelection) {
        return (IStructuredSelection) selection;
      }

    }
    return null;
  }
  
  protected IJavaElement getInitElement() {
    IStructuredSelection selection = getCurrentSelection();
    IJavaElement jelem = null;

    if (selection != null && !selection.isEmpty()) {
      Object selectedElement = selection.getFirstElement();
      if (selectedElement instanceof IAdaptable) {
        IAdaptable adaptable = (IAdaptable) selectedElement;

        jelem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
        if (jelem == null) {
          IResource resource = (IResource) adaptable.getAdapter(IResource.class);
          if (resource != null) {
            IProject proj = resource.getProject();
            if (proj != null) {
              jelem = JavaCore.create(proj);
            }
          }
        }
      }
    }
    if (jelem == null) {
      jelem = TapestryPlugin.getActiveEditorJavaInput();
    }
    return jelem;
  }

}