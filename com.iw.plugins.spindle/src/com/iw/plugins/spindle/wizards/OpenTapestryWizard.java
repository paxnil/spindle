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
import org.eclipse.jface.wizard.Wizard;
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