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

import net.sf.spindle.core.IProblemPeristManager;
import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.eclipse.EclipseMessages;
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

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#recordProblem(net.sf.spindle.core.ITapestryProject,
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
     * @see net.sf.spindle.core.IProblemPeristManager#recordProblems(net.sf.spindle.core.ITapestryProject,
     *      net.sf.spindle.core.source.IProblem[])
     */
    public void recordProblems(ITapestryProject project, IProblem[] problems)
    {
        addTapestryProblemMarkersToResource(((TapestryProject) project).getProject(), problems);

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#recordProblem(org.apache.hivemind.Resource,
     *      net.sf.spindle.core.source.IProblem)
     */
    public void recordProblem(Resource resource, IProblem problem)
    {
        recordProblems(resource, new IProblem[]
        { problem });

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#recordProblems(org.apache.hivemind.Resource,
     *      net.sf.spindle.core.source.IProblem[])
     */
    public void recordProblems(Resource resource, IProblem[] problems)
    {
        IStorage storage = (IStorage) ((ICoreResource) resource).getUnderlier();
        recordProblems(storage, problems);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#hasBrokenBuildProblems(net.sf.spindle.core.ITapestryProject)
     */
    public boolean hasBrokenBuildProblems(ITapestryProject project)
    {
        return getBrokenBuildProblemsFor(((TapestryProject) project).getProject()).length > 0;
    }

    /**
     * @param project
     */
    public void removeProblems(ITapestryProject project)
    {
        removeProblemsForProject(((TapestryProject) project).getProject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#removeAllProblems(net.sf.spindle.core.ITapestryProject)
     */
    public void removeAllProblems(ITapestryProject project)
    {
        removeAllProblems(((TapestryProject) project).getProject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#removeTemporaryProblemsForResource(org.apache.hivemind.Resource)
     */
    public void removeTemporaryProblemsForResource(Resource resource)
    {
        removeTemporaryProblemsFor(((ICoreResource)resource).getUnderlier());
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#removeAllProblemsFor(org.apache.hivemind.Resource)
     */
    public void removeAllProblemsFor(Resource resource)
    {
        removeProblemsFor(getResource(resource));

    }
    
    public void recordProblem(Object underlier, IProblem problem)
    {
        recordProblems((IStorage) underlier, new IProblem [] {});
        
    }

    public void removeAllProblemsFor(Object underlier)
    {
        removeProblemsFor(getResource((IStorage)underlier));
        
    }

    public void removeTemporaryProblemsFor(Object underlier)
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param resource
     * @return
     */
    private IResource getResource(Resource resource)
    {
        return getResource((IStorage) ((ICoreResource) resource).getUnderlier());
    }

    private IResource getResource(IStorage storage)
    {
        return (IResource) storage.getAdapter(IResource.class);
    }

    /**
     * Method addBuildBrokenProblemMarkerToResource.
     * 
     * @param iProject
     * @param string
     */
    private static void addBuildBrokenProblemMarkerToResource(IProject iProject, String message)
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

    /**
     * @param storage
     * @param problems
     */
    private static void recordProblems(IStorage storage, IProblem[] problems)
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

    /**
     * @param location
     * @param problems
     */
    private static void recordProblems(ICoreResource location, IProblem[] problems)
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

//    /**
//     * @param resource
//     * @param message
//     * @param severity
//     * @param lineNumber
//     * @param charStart
//     * @param charEnd
//     */
//    private static void addTapestryProblemMarkerToResource(IResource resource, String message,
//            int severity, int lineNumber, int charStart, int charEnd)
//    {
//
//        addProblemMarkerToResource(
//                resource,
//                IProblem.TAPESTRY_PROBLEM_MARKER,
//                message,
//                new Integer(severity),
//                new Integer(lineNumber),
//                new Integer(charStart),
//                new Integer(charEnd));
//    }

    /**
     * @param resource
     * @param markerTag
     * @param message
     * @param severity
     * @param lineNumber
     * @param charStart
     * @param charEnd
     */
    private static void addProblemMarkerToResource(IResource resource, String markerTag,
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

    /**
     * @param resource
     * @param markerTag
     * @param message
     * @param severity
     * @param lineNumber
     * @param charStart
     * @param charEnd
     */
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

    // /**
    // * @param resource
    // * @return
    // */
    // private static IMarker[] getFatalProblemsFor(IResource resource)
    // {
    // try
    // {
    // if (resource != null && resource.exists())
    // return resource.findMarkers(
    // IProblem.TAPESTRY_FATAL_PROBLEM_MARKER,
    // false,
    // IResource.DEPTH_INFINITE);
    // }
    // catch (CoreException e)
    // {
    // } // assume there are no problems
    //        return new IMarker[0];
    //    }

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

//    /**
//     * @param fCurrentProject
//     *            TODO remove?
//     */
//    private static void removeBuildProblemsForProject(IProject iProject)
//    {
//        try
//        {
//            if (iProject != null && iProject.exists())
//            {
//                iProject.deleteMarkers(
//                        IProblem.TAPESTRY_BUILDBROKEN_MARKER,
//                        false,
//                        IResource.DEPTH_ZERO);
//            }
//        }
//        catch (CoreException e)
//        {
//        } // assume there were no problems
//
//    }

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