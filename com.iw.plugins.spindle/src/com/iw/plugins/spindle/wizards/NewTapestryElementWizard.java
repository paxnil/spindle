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