package net.sf.spindle.core.util.eclipse;

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
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

import net.sf.spindle.core.CoreMessages;
import net.sf.spindle.core.IProblemPeristManager;
import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.eclipse.TapestryProject;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.eclipse.IEclipseResource;
import net.sf.spindle.core.source.IProblem;

import org.apache.hivemind.Resource;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;


/**
 * Marker utililties
 * 
 * @author glongman@gmail.com
 */
public class Markers implements IProblemPeristManager
{
    /**
     * Method addBuildBrokenProblemMarkerToResource.
     * 
     * @param iProject
     * @param string
     */
    public static void addBuildBrokenProblemMarkerToResource(IProject iProject, String message)
    {
        addProblemMarkerToResource(
                iProject,
                IProblem.TAPESTRY_BUILDBROKEN_MARKER,
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
            }
            else
            {
                logProblem(storage, problems[i]);
            }
        }
    }

    public static void recordProblems(ICoreResource location, IProblem[] problems)
    {
        IResource res = EclipseUtils.toResource(location);
        boolean workspace = res != null;
        for (int i = 0; i < problems.length; i++)
        {
            if (workspace)
            {
                addTapestryProblemMarkerToResource(res, problems[i]);
            }
            else
            {
                logProblem(((IEclipseResource) location).getStorage(), problems[i]);
            }
        }
    }

    static void logProblem(IStorage storage, IProblem problem)
    {
        TapestryCore.log(CoreMessages.format(
                "core-non-resource-problem",
                storage.toString(),
                problem.toString()));
    }

    /**
     * @param path
     * @param problems
     */
    public static void logProblems(IStorage storage, IProblem[] problems)
    {
        if (problems != null)
        {
            for (int i = 0; i < problems.length; i++)
            {
                logProblem(storage, problems[i]);
            }
        }
    }

    public static void addTapestryProblemMarkersToResource(IResource resource, IProblem[] problems)
    {
        if (problems.length > 0)
            for (int i = 0; i < problems.length; i++)
                addTapestryProblemMarkerToResource(resource, problems[i]);

    }

    public static void addTapestryProblemMarkerToResource(IResource resource, IProblem problem)
    {
        try
        {
            IMarker marker = resource.createMarker(problem.getType());

            marker.setAttributes(
                    new String[]
                    { IMarker.MESSAGE, IMarker.SEVERITY, IMarker.LINE_NUMBER, IMarker.CHAR_START,
                            IMarker.CHAR_END, IProblem.TEMPORARY_FLAG, IProblem.PROBLEM_CODE },
                    new Object[]
                    { problem.getMessage(), new Integer(problem.getSeverity()),
                            new Integer(problem.getLineNumber()),
                            new Integer(problem.getCharStart()), new Integer(problem.getCharEnd()),
                            new Boolean(problem.isTemporary()), new Integer(problem.getCode()) });

        }
        catch (CoreException e)
        {
            TapestryCore.log(e);
        }

    }

    // public static void addTapestryProblemMarkerToResource(
    // IResource resource,
    // String message,
    // int severity,
    // ISourceLocation source)
    // {
    //
    // addTapestryProblemMarkerToResource(
    // resource,
    // message,
    // severity,
    // source.getLineNumber(),
    // source.getCharStart(),
    // source.getCharEnd());
    //
    // }

    public static void addTapestryProblemMarkerToResource(IResource resource, String message,
            int severity, int lineNumber, int charStart, int charEnd)
    {

        addProblemMarkerToResource(
                resource,
                IProblem.TAPESTRY_PROBLEM_MARKER,
                message,
                new Integer(severity),
                new Integer(lineNumber),
                new Integer(charStart),
                new Integer(charEnd));
    }

    public static void addProblemMarkerToResource(IResource resource, String markerTag,
            String message, int severity, int lineNumber, int charStart, int charEnd)
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

    private static void addProblemMarkerToResource(IResource resource, String markerTag,
            String message, Integer severity, Integer lineNumber, Integer charStart, Integer charEnd)
    {
        try
        {
            IMarker marker = resource.createMarker(markerTag);

            marker.setAttributes(new String[]
            { IMarker.MESSAGE, IMarker.SEVERITY, IMarker.LINE_NUMBER, IMarker.CHAR_START,
                    IMarker.CHAR_END }, new Object[]
            { message, severity, lineNumber, charStart, charEnd });
        }
        catch (CoreException e)
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
                        IProblem.TAPESTRY_BUILDBROKEN_MARKER,
                        true,
                        IResource.DEPTH_ZERO);
            }
        }
        catch (CoreException e)
        {
        } // assume there were no problems
        return new IMarker[0];
    }

    public static IMarker[] getProblemsFor(IResource resource)
    {
        try
        {
            if (resource != null && resource.exists())
                return resource.findMarkers(
                        IProblem.TAPESTRY_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_INFINITE);
        }
        catch (CoreException e)
        {
        } // assume there are no problems
        return new IMarker[0];
    }

    public static IMarker[] getFatalProblemsFor(IResource resource)
    {
        try
        {
            if (resource != null && resource.exists())
                return resource.findMarkers(
                        IProblem.TAPESTRY_FATAL_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_INFINITE);
        }
        catch (CoreException e)
        {
        } // assume there are no problems
        return new IMarker[0];
    }

    public static void removeProblemsFor(IResource resource)
    {
        try
        {
            if (resource != null && resource.exists())
            {
                resource.deleteMarkers(
                        IProblem.TAPESTRY_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_INFINITE);
                resource.deleteMarkers(
                        IProblem.TAPESTRY_FATAL_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_INFINITE);
                resource.deleteMarkers(
                        IProblem.TAPESTRY_SOURCE_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_INFINITE);
            }
        }
        catch (CoreException e)
        {
        } // assume there were no problems
    }

    /**
     * Method removeProblemsForProject.
     * 
     * @param project
     */
    public static void removeProblemsForProject(IProject project)
    {
        try
        {
            if (project != null && project.isAccessible())
            {

                project.deleteMarkers(
                        IProblem.TAPESTRY_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_INFINITE);

                project.deleteMarkers(
                        IProblem.TAPESTRY_FATAL_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_INFINITE);

                project.deleteMarkers(
                        IProblem.TAPESTRY_BUILDBROKEN_MARKER,
                        false,
                        IResource.DEPTH_ZERO);
            }
        }
        catch (CoreException e)
        {
        } // assume there were no problems
    }

    /**
     * @param project
     */
    public static void removeAllProblems(IProject project)
    {
        if (project != null && project.isAccessible())
        {
            try
            {
                removeProblemsForProject(project);
                project.deleteMarkers(
                        IProblem.TAPESTRY_SOURCE_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_INFINITE);
            }
            catch (CoreException e)
            {
                // assume there were no problems
            }
        }

    }

    /**
     * @param fCurrentProject
     *            TODO remove?
     */
    public static void removeBuildProblemsForProject(IProject iProject)
    {
        try
        {
            if (iProject != null && iProject.exists())
            {
                iProject.deleteMarkers(
                        IProblem.TAPESTRY_BUILDBROKEN_MARKER,
                        false,
                        IResource.DEPTH_ZERO);
            }
        }
        catch (CoreException e)
        {
        } // assume there were no problems

    }

    public static void removeTemporaryProblemsForResource(IResource resource)
    {
        IMarker[] markers = getProblemsFor(resource);
        for (int i = 0; i < markers.length; i++)
        {
            if (markers[i].getAttribute(IProblem.TEMPORARY_FLAG, false))
            {
                try
                {
                    markers[i].delete();
                }
                catch (CoreException e)
                {
                    TapestryCore.log(e);
                }
            }

        }

    }

    public void recordProblem(ITapestryProject project, IProblem problem)
    {
        recordProblems(project, new IProblem[]
        { problem });
    }

    public void recordProblems(ITapestryProject project, IProblem[] problems)
    {
        addTapestryProblemMarkersToResource(((TapestryProject) project).getProject(), problems);

    }

    public void recordProblem(Resource resource, IProblem problem)
    {
        recordProblems(resource, new IProblem[]
        { problem });

    }

    public void recordProblems(Resource resource, IProblem[] problems)
    {
        IStorage storage = ((IEclipseResource) resource).getStorage();
        recordProblems(storage, problems);
    }

    public boolean hasBrokenBuildProblems(ITapestryProject project)
    {
        return getBrokenBuildProblemsFor(((TapestryProject) project).getProject()).length > 0;
    }

    public void removeProblems(ITapestryProject project)
    {
        removeProblemsForProject(((TapestryProject) project).getProject());
    }

    public void removeAllProblems(ITapestryProject project)
    {
        removeAllProblems(((TapestryProject) project).getProject());
    }

    public void removeTemporaryProblemsForResource(Resource resource)
    {
        removeTemporaryProblemsForResource(getResource(resource));
    }

    public void removeAllProblemsFor(Resource resource)
    {
        removeProblemsFor(getResource(resource));

    }

    private IResource getResource(Resource resource)
    {
        IEclipseResource res = (IEclipseResource) resource;
        IStorage storage = res.getStorage();
        return (IResource) storage.getAdapter(IResource.class);
    }

}