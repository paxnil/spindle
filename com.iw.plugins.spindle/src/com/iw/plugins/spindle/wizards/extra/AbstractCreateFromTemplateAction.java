package com.iw.plugins.spindle.wizards.extra;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.wizards.NewTapestryElementWizard;

/**
 * Copyright 2002 Intelligent Works Inc.
 * All rights reserved
 * 
 * @author gwl
 * @version $Id$
 */
public abstract class AbstractCreateFromTemplateAction extends Action implements IObjectActionDelegate {

  private IWorkbenchPart part;
  private String name;
  private IStructuredSelection selection;

  /**
   * Constructor for AbstractCreateFromTemplateAction.
   */
  public AbstractCreateFromTemplateAction() {
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
    NewTapestryElementWizard wizard = getWizard();
    wizard.init(PlatformUI.getWorkbench(), selection, name);
    WizardDialog wdialog = new WizardDialog(TapestryPlugin.getDefault().getActiveWorkbenchShell(), wizard);
    wdialog.open();
  }

  protected abstract NewTapestryElementWizard getWizard();

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection sel) {
    boolean enable = false;
    name = null;
    selection = null;

    if (sel instanceof IStructuredSelection) {

      IStructuredSelection selection = (IStructuredSelection) sel;

      if (!selection.isEmpty() && selection.size() == 1) {

        Object selected = selection.getFirstElement();
        this.selection = selection;

        if (selected instanceof IFile) {

          IFile candidateFile = (IFile) selected;

          if (!checkJWCExists(candidateFile)) {
            
            enable = true;
          }
        }

      }

    }
    action.setEnabled(enable);
  }

  /**
   * Method checkJWCExists.
   * @param file
   * @return boolean
   */
  private boolean checkJWCExists(IFile file) {  	
    IContainer folder = file.getParent();
    IPath path = file.getFullPath();
    path = path.removeFileExtension();
    String name = path.lastSegment();
    if (folder.findMember(name+".jwc") != null) {    	
    	return true;
    }
    this.name = name;
    return false;    
  }

}