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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.builder;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;


import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;
import com.iw.plugins.spindle.core.util.eclipse.EclipsePluginUtils;
import com.iw.plugins.spindle.core.util.eclipse.Markers;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;

import core.TapestryCore;

/**
 * A job that gets scheduled to initiate a build.!
 * 
 * @author glongman@gmail.com
 *  
 */
public class BuildJob extends Job
{

  private final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi");

  private IProject fProject;

  /**
   * @param name
   */
  public BuildJob(IProject project)
  {
    super("Spindle project build job:" + project == null ? "unknown" : project.getName());
    fProject = project;
  }
  
  public IProject getProject() {
    return fProject;
  }
  
  public boolean belongsTo(Object family) {
    return family == TapestryArtifactManager.MANAGER_JOB_FAMILY;
 }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  protected IStatus run(IProgressMonitor monitor)
  {
    if (systemBundle.getState() == Bundle.STOPPING || !canBuild(fProject))
      throw new OperationCanceledException();

    try
    {
      fProject.build(
          IncrementalProjectBuilder.FULL_BUILD,
          TapestryCorePlugin.BUILDER_ID,
          new HashMap(),
          monitor);
    } catch (CoreException e)
    {
      TapestryCore.log(e);
    }
    if (systemBundle.getState() == Bundle.STOPPING)
      throw new OperationCanceledException();
    return new SpindleStatus();
  }

  private boolean canBuild(IProject project)
  {
    if (project == null || !project.isAccessible())
      return false;

    if (!EclipsePluginUtils.hasTapestryNature(project))
      return false;

    return Markers.getBrokenBuildProblemsFor(project).length == 0;
  }
}