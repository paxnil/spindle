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

package com.iw.plugins.spindle.actions;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;

/**
 * TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: AbstractTapestryResourceAction.java,v 1.1 2003/10/29 12:33:56
 *          glongman Exp $
 */
public abstract class AbstractTapestryResourceAction extends Action
    implements
      IObjectActionDelegate
{

  protected IWorkbenchPart fPart;
  protected IStructuredSelection fSelection;

  /**
   * Constructor for AbstractCreateFromTemplateAction.
   */
  public AbstractTapestryResourceAction()
  {
    super();
  }

  /**
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction,
   *      IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart)
  {
    fPart = targetPart;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection sel)
  {
    boolean enable = false;
    this.fSelection = null;

    IStructuredSelection selection = null;
    if (sel instanceof IStructuredSelection)
    {
      selection = (IStructuredSelection) sel;
      if (!selection.isEmpty())
        enable = checkSelection(selection);

    }
    if (enable)
      this.fSelection = selection;

    action.setEnabled(enable);
  }

  /**
   * Method checkMultiSelection.
   * 
   * @param selection
   * @return boolean
   */
  private boolean checkSelection(IStructuredSelection selection)
  {
    boolean result = true;

    if (selection == null || selection.isEmpty())
    {
      result = false;

    } else
    {

      for (Iterator iter = selection.iterator(); iter.hasNext();)
      {
        IFile candidateFile = (IFile) iter.next();

        IProject project = candidateFile.getProject();

        try
        {

          if (!project.isOpen() || !project.hasNature(TapestryCore.NATURE_ID))
            return false;

          if (project.findMarkers(
              ITapestryMarker.TAPESTRY_BUILDBROKEN_MARKER,
              false,
              IResource.DEPTH_INFINITE).length > 0)
            return false;

        } catch (CoreException e)
        {
          return false;
        }

        if (checkSpecificationExists(candidateFile))
          result = false;
      }
    }
    return result;
  }

  protected String getName(IFile file)
  {

    IPath path = file.getFullPath();
    path = path.removeFileExtension();
    return path.lastSegment();
  }

  private boolean checkSpecificationExists(IFile file)
  {
    Map templateMap = TapestryArtifactManager
        .getTapestryArtifactManager()
        .getTemplateMap(file.getProject());
    if (templateMap != null && templateMap.containsKey(file))
      return templateMap.get(file) != null;

    return false;
  }

}