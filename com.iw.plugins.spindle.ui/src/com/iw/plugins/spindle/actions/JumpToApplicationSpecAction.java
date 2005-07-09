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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.actions;

import org.apache.tapestry.INamespace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.resources.eclipse.IEclipseResource;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * Copyright 2002 Intelligent Works Inc. All rights reserved
 * 
 * @author gwl
 */
public class JumpToApplicationSpecAction extends AbstractTapestryProjectAction
{

  /**
   * Constructor for OpenTapestryProjectAction.
   */
  public JumpToApplicationSpecAction()
  {
    super();
  }

  protected boolean checkSelection(IStructuredSelection selection)
  {
    return true;
  }

  public void run(IAction action)
  {

    IStorage storage = getApplicationStorage(selection);

    if (storage == null)
      return;

    IEditorPart editor = UIUtils.getEditorFor(storage);

    if (editor != null)
    {
      UIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().bringToTop(editor);
    } else
    {
      UIPlugin.getDefault().openTapestryEditor(storage);
    }
  }

  private IStorage getApplicationStorage(IStructuredSelection selection)
  {
    IJavaProject jproject = (IJavaProject) selection.getFirstElement();
    IProject project = jproject.getProject();

    INamespace namespace = TapestryArtifactManager
        .getTapestryArtifactManager()
        .getProjectNamespace(project);

    if (namespace == null)
      return null;

    IEclipseResource location = (IEclipseResource) namespace
        .getSpecificationLocation();

    return location.getStorage();

  }
}