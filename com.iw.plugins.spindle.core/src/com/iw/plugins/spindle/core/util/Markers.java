package com.iw.plugins.spindle.core.util;
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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.IProblem;

/**
 * Marker utililties
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class Markers
{

  public static final String TAPESTRY_MARKER_TAG = ITapestryMarker.TAPESTRY_PROBLEM_MARKER;
  public static final String TAPESTRY_BUILBROKEN_TAG = ITapestryMarker.TAPESTRY_BUILDBROKEN_MARKER;
  public static final String TAPESTRY_FATAL = ITapestryMarker.TAPESTRY_FATAL_PROBLEM_MARKER;
  //    public static final String TAPESTRY_SOURCE =
  // ITapestryMarker.TAPESTRY_SOURCE_PROBLEM_MARKER;

  /**
   * Method addBuildBrokenProblemMarkerToResource.
   * 
   * @param iProject
   * @param string
   */
  public static void addBuildBrokenProblemMarkerToResource(
      IProject iProject,
      String message)
  {
    addProblemMarkerToResource(
        iProject,
        TAPESTRY_BUILBROKEN_TAG,
        message,
        IMarker.SEVERITY_ERROR,
        0,
        0,
        0);
  }

  public static void recordProblems(IStorage storage, IProblem[] problems)
  {
    IResource res = (IResource) storage.getAdapter(IResource.class);
    boolean workspace = res != null;
    for (int i = 0; i < problems.length; i++)
    {
      if (workspace)
      {
        Markers.addTapestryProblemMarkerToResource(res, problems[i]);
      } else
      {
        TapestryCore.logProblem(storage, problems[i]);
      }
    }
  }

  public static void recordProblems(
      IResourceWorkspaceLocation location,
      IProblem[] problems)
  {
    IResource res = CoreUtils.toResource(location);
    boolean workspace = res != null;
    for (int i = 0; i < problems.length; i++)
    {
      if (workspace)
      {
        addTapestryProblemMarkerToResource(res, problems[i]);
      } else
      {
        TapestryCore.logProblem(location.getStorage(), problems[i]);
      }
    }
  }

  public static void addTapestryProblemMarkersToResource(
      IResource resource,
      IProblem[] problems)
  {
    if (problems.length > 0)
      for (int i = 0; i < problems.length; i++)
        addTapestryProblemMarkerToResource(resource, problems[i]);

  }

  public static void addTapestryProblemMarkerToResource(
      IResource resource,
      IProblem problem)
  {
    try
    {
      IMarker marker = resource.createMarker(problem.getType());

      marker.setAttributes(new String[]{IMarker.MESSAGE, IMarker.SEVERITY,
          IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END,
          ITapestryMarker.TEMPORARY_FLAG}, new Object[]{problem.getMessage(),
          new Integer(problem.getSeverity()), new Integer(problem.getLineNumber()),
          new Integer(problem.getCharStart()), new Integer(problem.getCharEnd()),
          new Boolean(problem.isTemporary())});

    } catch (CoreException e)
    {
      TapestryCore.log(e);
    }

  }

  //    public static void addTapestryProblemMarkerToResource(
  //        IResource resource,
  //        String message,
  //        int severity,
  //        ISourceLocation source)
  //    {
  //
  //        addTapestryProblemMarkerToResource(
  //            resource,
  //            message,
  //            severity,
  //            source.getLineNumber(),
  //            source.getCharStart(),
  //            source.getCharEnd());
  //
  //    }

  public static void addTapestryProblemMarkerToResource(
      IResource resource,
      String message,
      int severity,
      int lineNumber,
      int charStart,
      int charEnd)
  {

    addProblemMarkerToResource(resource, TAPESTRY_MARKER_TAG, message, new Integer(
        severity), new Integer(lineNumber), new Integer(charStart), new Integer(charEnd));
  }

  public static void addProblemMarkerToResource(
      IResource resource,
      String markerTag,
      String message,
      int severity,
      int lineNumber,
      int charStart,
      int charEnd)
  {

    addProblemMarkerToResource(
        resource,
        markerTag,
        message,
        new Integer(severity),
        new Integer(lineNumber),
        new Integer(charStart),
        new Integer(charEnd));
  }

  private static void addProblemMarkerToResource(
      IResource resource,
      String markerTag,
      String message,
      Integer severity,
      Integer lineNumber,
      Integer charStart,
      Integer charEnd)
  {
    try
    {
      IMarker marker = resource.createMarker(markerTag);

      marker.setAttributes(new String[]{IMarker.MESSAGE, IMarker.SEVERITY,
          IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END}, new Object[]{
          message, severity, lineNumber, charStart, charEnd});
    } catch (CoreException e)
    {
      TapestryCore.log(e);
    }

  }

  public static IMarker[] getBrokenBuildProblemsFor(IProject project)
  {
    try
    {
      if (project != null && project.exists())
      {

        return project.findMarkers(
            Markers.TAPESTRY_BUILBROKEN_TAG,
            true,
            IResource.DEPTH_ZERO);
      }
    } catch (CoreException e)
    {} // assume there were no problems
    return new IMarker[0];
  }

  public static IMarker[] getProblemsFor(IResource resource)
  {
    try
    {
      if (resource != null && resource.exists())
        return resource.findMarkers(TAPESTRY_MARKER_TAG, false, IResource.DEPTH_INFINITE);
    } catch (CoreException e)
    {} // assume there are no problems
    return new IMarker[0];
  }

  public static IMarker[] getFatalProblemsFor(IResource resource)
  {
    try
    {
      if (resource != null && resource.exists())
        return resource.findMarkers(TAPESTRY_FATAL, false, IResource.DEPTH_INFINITE);
    } catch (CoreException e)
    {} // assume there are no problems
    return new IMarker[0];
  }

  public static void removeProblemsFor(IResource resource)
  {
    try
    {
      if (resource != null && resource.exists())
      {
        resource.deleteMarkers(
            Markers.TAPESTRY_MARKER_TAG,
            false,
            IResource.DEPTH_INFINITE);
        resource.deleteMarkers(Markers.TAPESTRY_FATAL, false, IResource.DEPTH_INFINITE);
      }
    } catch (CoreException e)
    {} // assume there were no problems
  }

  /**
   * Method removeProblemsForProject.
   * 
   * @param iProject
   */
  public static void removeProblemsForProject(IProject iProject)
  {
    try
    {
      if (iProject != null && iProject.exists())
      {

        iProject.deleteMarkers(
            Markers.TAPESTRY_MARKER_TAG,
            false,
            IResource.DEPTH_INFINITE);
        iProject.deleteMarkers(Markers.TAPESTRY_FATAL, false, IResource.DEPTH_INFINITE);
        iProject.deleteMarkers(
            Markers.TAPESTRY_BUILBROKEN_TAG,
            false,
            IResource.DEPTH_ZERO);
        //                iProject.deleteMarkers(Markers.TAPESTRY_SOURCE, false,
        // IResource.DEPTH_INFINITE);
      }
    } catch (CoreException e)
    {} // assume there were no problems
  }

  /**
   * @param fCurrentProject
   */
  public static void removeBuildProblemsForProject(IProject iProject)
  {
    try
    {
      if (iProject != null && iProject.exists())
      {
        iProject.deleteMarkers(
            Markers.TAPESTRY_BUILBROKEN_TAG,
            false,
            IResource.DEPTH_ZERO);
      }
    } catch (CoreException e)
    {} // assume there were no problems

  }

  public static void removeTemporaryProblemsForResource(IResource resource)
  {
    IMarker[] markers = getProblemsFor(resource);
    for (int i = 0; i < markers.length; i++)
    {
      if (markers[i].getAttribute(ITapestryMarker.TEMPORARY_FLAG, false))
      {
        try
        {
          markers[i].delete();
        } catch (CoreException e)
        {
          TapestryCore.log(e);
        }
      }

    }

  }

}