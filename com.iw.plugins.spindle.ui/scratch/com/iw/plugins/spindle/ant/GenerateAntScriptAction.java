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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ant;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;

/**
 * GenerateAntScript TODO add something here
 * 
 * @author glongman@gmail.com
 *  
 */
public class GenerateAntScriptAction implements IWorkbenchWindowActionDelegate
{

  private ISelection fSelection;
  private Shell fCurrentShell;

  public void dispose()
  {
  }

  public void init(IWorkbenchWindow window)
  {
    fCurrentShell = window.getShell();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action)
  {
    if (fSelection.isEmpty())
      return;

    Object selection = ((IStructuredSelection) fSelection).getFirstElement();

    if (!(selection instanceof IAdaptable))
    {
      UIPlugin.log("AntScriptGenerator can't handle selection: "
          + selection.getClass().getName());
      return;
    }

    IAdaptable adaptable = (IAdaptable) selection;

    IProject project = (IProject) adaptable.getAdapter(IProject.class);

    if (project == null)
      return;

    AntScriptGenerator gen = new AntScriptGenerator(TapestryCore
        .getDefault()
        .getTapestryProjectFor(project));

    gen.generate(new NullProgressMonitor());

  }

  public void selectionChanged(IAction action, ISelection selection)
  {
    fSelection = selection;
  }

}