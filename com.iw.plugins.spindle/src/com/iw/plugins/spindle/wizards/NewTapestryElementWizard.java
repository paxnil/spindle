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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.iw.plugins.spindle.TapestryPlugin;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public abstract class NewTapestryElementWizard extends BasicNewResourceWizard implements INewWizard {

  protected String prepopulateName = null;

  public NewTapestryElementWizard() {
    setNeedsProgressMonitor(true);
  }

  /*
   * @see BasicNewResourceWizard#initializeDefaultPageImageDescriptor
   */
  protected void initializeDefaultPageImageDescriptor() {
    // no action, we do not need the desktop default
  }

  protected void openResource(final IResource resource) {
    if (resource.getType() == IResource.FILE) {
      final IWorkbenchPage activePage = TapestryPlugin.getDefault().getActivePage();
      if (activePage != null) {
        final Display display = getShell().getDisplay();
        if (display != null) {
          display.asyncExec(new Runnable() {
            public void run() {
              try {
                activePage.openEditor((IFile) resource);
              } catch (PartInitException e) {
                TapestryPlugin.getDefault().logException(e);
              }
            }
          });
        }
      }
    }
  }

  /**
   * Run a runnable
   */
  protected boolean finishPage(IRunnableWithProgress runnable) {
    IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
    try {
      getContainer().run(false, true, op);
    } catch (InvocationTargetException e) {
      TapestryPlugin.getDefault().logException(e);
      return false;
    } catch (InterruptedException e) {
      return false;
    }
    return true;
  }

  protected IJavaElement getInitElement() {
    IStructuredSelection selection = getSelection();
    IJavaElement jelem = null;

    if (selection != null && !selection.isEmpty()) {
      Object selectedElement = selection.getFirstElement();
      if (selectedElement instanceof IAdaptable) {
        IAdaptable adaptable = (IAdaptable) selectedElement;

        jelem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
        if (jelem == null) {
          IResource resource = (IResource) adaptable.getAdapter(IResource.class);
          IResource parent = resource.getParent();
          jelem = (IJavaElement) ((IAdaptable) parent).getAdapter(IJavaElement.class);
          if (jelem == null) {
            if (resource != null) {
              IProject proj = resource.getProject();
              if (proj != null) {
                jelem = JavaCore.create(proj);
              }
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

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection, String prepopulateName) {
    super.init(workbench, selection);
    this.prepopulateName = prepopulateName;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish() {
    return false;
  }

}