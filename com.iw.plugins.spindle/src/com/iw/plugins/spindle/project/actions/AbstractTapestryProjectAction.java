package com.iw.plugins.spindle.project.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
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
  
  protected boolean checkHasTapestryJars(IJavaProject project) {
  	
    try {
    	
      if (Utils.findType(project, "javax.servlet.Servlet") != null) {
      
        return Utils.findType(project, "net.sf.tapestry.spec.IApplicationSpecification") != null;  	
        
      }
      
    } catch (JavaModelException e) {
    	
    	Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();
    	ErrorDialog.openError(shell, "Spindle error", "could not determine if required jars are present", e.getStatus());
    }
  	
  	return false;
  }

  private String getName(IProject project) {

    IPath path = project.getFullPath();
    path = path.removeFileExtension();
    return path.lastSegment();
    
  }


}