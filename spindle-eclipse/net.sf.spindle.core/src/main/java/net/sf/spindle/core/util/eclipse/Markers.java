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

import net.sf.spindle.core.IProblemPersistManager;
import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.eclipse.EclipseMessages;
import net.sf.spindle.core.eclipse.TapestryProject;
import net.sf.spindle.core.source.IProblem;

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
public class Markers implements IProblemPersistManager<IStorage>
{

    private static Markers INSTANCE = new Markers();

    public static Markers getInstance()
    {
        return INSTANCE;
    }

    private Markers()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPersistManager#recordProblem(java.lang.Object,
     *      net.sf.spindle.core.source.IProblem)
     */
    public void recordProblem(IStorage underlier, IProblem problem)
    {
        recordProblems((IStorage) underlier, new IProblem[]
        { problem });

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPersistManager#recordProblems(java.lang.Object,
     *      net.sf.spindle.core.source.IProblem[])
     */
    public void recordProblems(IStorage underlier, IProblem[] problems)
    {
        IResource res = (IResource) underlier.getAdapter(IResource.class);
        boolean workspace = res != null;
        for (int i = 0; i < problems.length; i++)
        {
            if (workspace)
            {
                addTapestryProblemMarkerToResource(res, problems[i]);
            }
            else
            {
                logProblem(underlier, problems[i]);
            }
        }
    }

    public void removeAllProblemsFor(IStorage underlier)
    {
        IResource resource = getResource(underlier);
        if (resource != null)
            removeProblemsFor(resource);
    }

    public void removeTemporaryProblemsFor(IStorage underlier)
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPersistManager#recordProblem(net.sf.spindle.core.ITapestryProject,
     *      net.sf.spindle.core.source.IProblem)
     */
    public void recordProblem(ITapestryProject project, IProblem problem)
    {
        recordProblems(project, new IProblem[]
        { problem });
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPersistManager#recordProblems(net.sf.spindle.core.ITapestryProject,
     *      net.sf.spindle.core.source.IProblem[])
     */
    public void recordProblems(ITapestryProject project, IProblem[] problems)
    {
        addTapestryProblemMarkersToResource(((TapestryProject) project).getProject(), problems);

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPersistManager#hasBrokenBuildProblems(net.sf.spindle.core.ITapestryProject)
     */
    public boolean hasBrokenBuildProblems(ITapestryProject project)
    {
        return getBrokenBuildProblemsFor(((TapestryProject) project).getProject()).length > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPersistManager#removeAllProblems(net.sf.spindle.core.ITapestryProject)
     */
    public void removeAllProblems(ITapestryProject project)
    {
        removeAllProblems(((TapestryProject) project).getProject());
    }

    private IResource getResource(IStorage storage)
    {
        return (IResource) storage.getAdapter(IResource.class);
    }

    /**
     * @param storage
     * @param problem
     */
    private static void logProblem(IStorage storage, IProblem problem)
    {
        TapestryCore.log(EclipseMessages.logProblemMessage(storage, problem));
    }

    /**
     * @param storage
     * @param problems
     */
    @SuppressWarnings("unused")
    private static void logProblems(IStorage storage, IProblem[] problems)
    {
        if (problems != null)
        {
            for (int i = 0; i < problems.length; i++)
            {
                logProblem(storage, problems[i]);
            }
        }
    }

    /**
     * @param resource
     * @param problems
     */
    private static void addTapestryProblemMarkersToResource(IResource resource, IProblem[] problems)
    {
        if (problems.length > 0)
            for (int i = 0; i < problems.length; i++)
                addTapestryProblemMarkerToResource(resource, problems[i]);

    }

    /**
     * @param resource
     * @param problem
     */
    private static void addTapestryProblemMarkerToResource(IResource resource, IProblem problem)
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

    /**
     * @param project
     * @return
     */
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

    /**
     * @param resource
     * @return
     */
    private static IMarker[] getProblemsFor(IResource resource)
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

    /**
     * @param resource
     */
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

    private static void removeProblemsForProject(IProject project)
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

    public static void removeTemporaryProblemsForEclipseResource(IResource resource)
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

}