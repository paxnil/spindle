package com.iw.plugins.spindle.wizards.extra;

import java.util.Iterator;

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
public abstract class AbstractCreateFromTemplateAction
  extends Action
  implements IObjectActionDelegate {

  private IWorkbenchPart part;
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
  	
    if (selection != null) {

      try {
      	
        for (Iterator iter = selection.iterator(); iter.hasNext();) {

          IFile file = (IFile) iter.next();

          NewTapestryElementWizard wizard = getWizard();

          wizard.init(PlatformUI.getWorkbench(), selection, getName(file));

          WizardDialog wdialog =
            new WizardDialog(TapestryPlugin.getDefault().getActiveWorkbenchShell(), wizard);

          wdialog.open();

        }
      } catch (ClassCastException e) {
      	// do nothing
      }
    }
  }

  protected abstract NewTapestryElementWizard getWizard();

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
    boolean result = true;

    if (selection == null || selection.isEmpty()) {

      result = false;

    } else {

      try {

        for (Iterator iter = selection.iterator(); iter.hasNext();) {

          IFile candidateFile = (IFile) iter.next();

          if (checkJWCExists(candidateFile)) {

            result = false;
          }

        }
      } catch (ClassCastException e) {

        result = false;
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
   * Method checkJWCExists.
   * @param file
   * @return boolean
   */
  private boolean checkJWCExists(IFile file) {

    IContainer folder = file.getParent();
    String name = getName(file);

    if (folder.findMember(name + ".jwc") != null) {

      return true;
    }

    return false;
  }

}