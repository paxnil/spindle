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
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;

import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.ISourceLocation;

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

    public static void addTapestryProblemMarkersToResource(IResource resource, IProblem[] problems)
    {
        if (problems.length > 0)
        {
            for (int i = 0; i < problems.length; i++)
            {
                addTapestryProblemMarkerToResource(resource, problems[i]);
            }
        }
    }

    public static void addTapestryProblemMarkerToResource(IResource resource, IProblem problem)
    {
        try
        {
            IMarker marker = resource.createMarker(problem.getType());

            marker.setAttributes(
                new String[] { IMarker.MESSAGE, IMarker.SEVERITY, IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END },
                new Object[] {
                    problem.getMessage(),
                    new Integer(problem.getSeverity()),
                    new Integer(problem.getLineNumber()),
                    new Integer(problem.getCharStart()),
                    new Integer(problem.getCharEnd())});
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }

    }

    public static void addTapestryProblemMarkerToResource(IResource resource, String message, int severity, ISourceLocation source)
    {

        addTapestryProblemMarkerToResource(
            resource,
            message,
            severity,
            source.getLineNumber(),
            source.getCharStart(),
            source.getCharEnd());

    }

    public static void addTapestryProblemMarkerToResource(
        IResource resource,
        String message,
        int severity,
        int lineNumber,
        int charStart,
        int charEnd)
    {

        addProblemMarkerToResource(
            resource,
            TAPESTRY_MARKER_TAG,
            message,
            new Integer(severity),
            new Integer(lineNumber),
            new Integer(charStart),
            new Integer(charEnd));
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

    public static void addProblemMarkerToResource(
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

            marker.setAttributes(
                new String[] { IMarker.MESSAGE, IMarker.SEVERITY, IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END },
                new Object[] { message, severity, lineNumber, charStart, charEnd });
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }

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

    public static void removeProblemsFor(IResource resource)
    {
        try
        {
            if (resource != null && resource.exists())
                resource.deleteMarkers(Markers.TAPESTRY_MARKER_TAG, false, IResource.DEPTH_INFINITE);
        } catch (CoreException e)
        {} // assume there were no problems
    }

    /**
     * Method removeProblemsForProject.
     * @param iProject
     */
    public static void removeProblemsForProject(IProject iProject)
    {
        try
        {
            if (iProject != null && iProject.exists())
                iProject.deleteMarkers(Markers.TAPESTRY_MARKER_TAG, false, IResource.DEPTH_ZERO);
        } catch (CoreException e)
        {} // assume there were no problems
    }

    /**
     * Method addBuildBrokenProblemMarkerToResource.
     * @param iProject
     * @param string
     */
    public static void addBuildBrokenProblemMarkerToResource(IProject iProject, String message)
    {
        addProblemMarkerToResource(iProject, TAPESTRY_BUILBROKEN_TAG, message, IMarker.SEVERITY_ERROR, 0, 0, 0);
    }

}
