package com.iw.plugins.spindle.core.builder;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.artifacts.TapestryArtifactManager;
import com.iw.plugins.spindle.core.resources.ClasspathResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.ContextRootLocation;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.Markers;

/**
 * The Tapestry Builder, kicks off full and incremental builds.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TapestryBuilder extends IncrementalProjectBuilder
{
    public static final String STRING_KEY = "builder-";
    public static final String APPLICATION_EXTENSION = "application";
    public static final String COMPONENT_EXTENSION = "jwc";
    public static final String PAGE_EXTENSION = "page";
    public static final String TEMPLATE_EXTENSION = "html";
    public static final String SCRIPT_EXTENSION = "script";
    public static final String LIBRARY_EXTENSION = "library";
    public static final String[] KnownExtensions =
        new String[] {
            APPLICATION_EXTENSION,
            COMPONENT_EXTENSION,
            PAGE_EXTENSION,
            TEMPLATE_EXTENSION,
            SCRIPT_EXTENSION,
            LIBRARY_EXTENSION };
    public static final String APP_SPEC_PATH_PARAM = "org.apache.tapestry.application-specification";
    public static final String ENGINE_CLASS_PARAM = "org.apache.tapestry.engine-class";

    public static boolean DEBUG = true;

    //TODO this is really ugly, but I need this fast.
    public static List fDeferredActions = new ArrayList();

    public static State readState(DataInputStream in) throws IOException
    {
        return State.read(in);
    }

    public static void writeState(Object state, DataOutputStream out) throws IOException
    {
        ((State) state).write(out);
    }

    IProject fCurrentProject;
    IJavaProject fJavaProject;
    TapestryProject fTapestryProject;
    IWorkspaceRoot fWorkspaceRoot;
    ContextRootLocation fContextRoot;
    ClasspathRootLocation fClasspathRoot;
    int fProjectType;

    private IBuild fBuild;

    BuildNotifier fNotifier;

    IClasspathEntry[] fClasspath;

    /**
     * Constructor for TapestryBuilder.
     */
    public TapestryBuilder()
    {
        super();
    }

    /**
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int, Map, IProgressMonitor)
     */
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
    {
        fCurrentProject = getProject();
        Markers.removeProblemsForProject(fCurrentProject);
        if (fCurrentProject == null || !fCurrentProject.isAccessible())
            return new IProject[0];

        long start = System.currentTimeMillis();
        if (DEBUG)
            System.out.println(
                "\nStarting build of " + fCurrentProject.getName() + " @ " + new Date(System.currentTimeMillis()));
        this.fNotifier = new BuildNotifier(monitor, fCurrentProject);
        fNotifier.begin();
        boolean ok = false;
        try
        {
            fNotifier.checkCancel();
            initializeBuilder();

            if (isWorthBuilding())
            {
                if (kind == FULL_BUILD)
                {
                    buildAll();
                } else
                {
                    buildIncremental();
                }
                for (Iterator iter = fDeferredActions.iterator(); iter.hasNext();)
                {
                    IBuildAction action = (IBuildAction) iter.next();
                    action.run();
                }
                ok = true;
            }
        } catch (CoreException e)
        {
            ErrorDialog.openError(
                TapestryCore.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
                TapestryCore.getString("build-failed-core-title"),
                TapestryCore.getString("build-failed-core-message"),
                e.getStatus());
        } catch (BuilderException e)
        {
            Markers.addBuildBrokenProblemMarkerToResource(getProject(), e.getMessage());
            //    } catch (CoreException e) {
            //      Util.log(e, "JavaBuilder handling CoreException"); //$NON-NLS-1$
            //      IMarker marker = currentProject.createMarker(ProblemMarkerTag);
            //      marker.setAttribute(IMarker.MESSAGE, Util.bind("build.inconsistentProject")); //$NON-NLS-1$
            //      marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            //    } catch (ImageBuilderInternalException e) {
            //      Util.log(e.getThrowable(), "JavaBuilder handling ImageBuilderInternalException"); //$NON-NLS-1$
            //      IMarker marker = currentProject.createMarker(ProblemMarkerTag);
            //      marker.setAttribute(IMarker.MESSAGE, Util.bind("build.inconsistentProject")); //$NON-NLS-1$
            //      marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            //    } catch (MissingClassFileException e) {
            //      // do not log this exception since its thrown to handle aborted compiles because of missing class files
            //      if (DEBUG)
            //        System.out.println(Util.bind("build.incompleteClassPath", e.missingClassFile)); //$NON-NLS-1$
            //      IMarker marker = currentProject.createMarker(ProblemMarkerTag);
            //      marker.setAttribute(IMarker.MESSAGE, Util.bind("build.incompleteClassPath", e.missingClassFile)); //$NON-NLS-1$
            //      marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            //    } catch (MissingSourceFileException e) {
            //      // do not log this exception since its thrown to handle aborted compiles because of missing source files
            //      if (DEBUG)
            //        System.out.println(Util.bind("build.missingSourceFile", e.missingSourceFile)); //$NON-NLS-1$
            //      removeProblemsFor(currentProject); // make this the only problem for this project
            //      IMarker marker = currentProject.createMarker(ProblemMarkerTag);
            //      marker.setAttribute(IMarker.MESSAGE, Util.bind("build.missingSourceFile", e.missingSourceFile)); //$NON-NLS-1$
            //      marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

        } finally
        {
            if (!ok)
                // If the build failed, clear the previously built state, forcing a full build next time.
                clearLastState();
            fNotifier.done();
            fDeferredActions.clear();
            cleanup();
        }
        IProject[] requiredProjects = getRequiredProjects(true);
        long stop = System.currentTimeMillis();
        if (DEBUG)
            System.out.println("Finished build of " + fCurrentProject.getName() + " @ " + new Date(stop));
        System.out.println("elapsed (ms) = " + (stop - start));
        return requiredProjects;
    }

    /**
     * Method cleanup.
     */
    private void cleanup()
    {
        if (fBuild != null)
            fBuild.cleanUp();

        fClasspath = null;
    }

    private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites)
    {
        if (fJavaProject == null || fWorkspaceRoot == null)
            return new IProject[0];

        ArrayList projects = new ArrayList();
        try
        {
            IClasspathEntry[] entries = ((JavaProject) fJavaProject).getExpandedClasspath(true);
            for (int i = 0, length = entries.length; i < length; i++)
            {
                IClasspathEntry entry = JavaCore.getResolvedClasspathEntry(entries[i]);
                if (entry != null)
                {
                    IPath path = entry.getPath();
                    IProject p = null;
                    if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
                    {
                        IProject workspaceProject = fWorkspaceRoot.getProject(path.lastSegment());
                        if (workspaceProject.hasNature(TapestryCore.NATURE_ID))
                            p = workspaceProject;

                    }
                    if (p != null && !projects.contains(p))
                        projects.add(p);

                }
            }
        } catch (CoreException e)
        {
            return new IProject[0];
        }
        IProject[] result = new IProject[projects.size()];
        projects.toArray(result);
        return result;
    }

    State getLastState(IProject project)
    {
        return (State) TapestryArtifactManager.getTapestryArtifactManager().getLastBuildState(fCurrentProject);
    }

    /**
     * Method clearLastState.
     */
    private void clearLastState()
    {
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(fCurrentProject, null);
    }

    /**
     * Method buildAll.
     */
    private void buildAll() throws BuilderException, CoreException
    {
        if (TapestryBuilder.DEBUG)
            System.out.println("FULL Tapestry build");

        fNotifier.subTask(TapestryCore.getString(TapestryBuilder.STRING_KEY + "full-build-starting"));
        Markers.removeProblemsForProject(fCurrentProject);

        int type = fTapestryProject.getProjectType();
        switch (type)
        {
            case TapestryProject.APPLICATION_PROJECT_TYPE :
                fBuild = new FullBuild(this);
                break;

            case TapestryProject.LIBRARY_PROJECT_TYPE :
                fBuild = new LibraryBuild(this);
            default :
                break;
        }
        if (fBuild != null)
            fBuild.build();

    }

    private void buildIncremental() throws BuilderException, CoreException
    {

        int type = fTapestryProject.getProjectType();
        IIncrementalBuild inc = null;
        IResourceDelta delta = getDelta(fTapestryProject.getProject());
        switch (type)
        {
            case TapestryProject.APPLICATION_PROJECT_TYPE :
                inc = new IncrementalApplicationBuild(this, delta);
                break;

            case TapestryProject.LIBRARY_PROJECT_TYPE :
                inc = new IncrementalLibraryBuild(this, delta);

            default :
                break;
        }
        if (inc == null)
            throw new Error("no builder!");

        if (inc.canIncrementalBuild())
        {
            if (!inc.needsIncrementalBuild())
                return;

            fBuild = inc;
            if (TapestryBuilder.DEBUG)
                System.out.println("Incremental Tapestry build");

            fNotifier.subTask(TapestryCore.getString(TapestryBuilder.STRING_KEY + "incremental-build-starting"));
            // TODO shouldn't do this for a true incremental build!
            Markers.removeProblemsForProject(fCurrentProject);
            inc.build();
        } else
        {
            buildAll();
        }
    }

    /**
     * Method isWorthBuilding.
     * @return boolean
     */
    private boolean isWorthBuilding()
    {

        if (fJavaProject == null)
        {
            Markers.removeProblemsForProject(getProject());
            Markers.addBuildBrokenProblemMarkerToResource(
                getProject(),
                TapestryCore.getString(STRING_KEY + "non-java-projects"));
            return false;
        }

        try
        {
            fClasspath = fJavaProject.getResolvedClasspath(true);
        } catch (JavaModelException e3)
        {
            Markers.removeProblemsForProject(getProject());
            Markers.addBuildBrokenProblemMarkerToResource(
                getProject(),
                TapestryCore.getString(STRING_KEY + "classpath-not-determined"));
            return false;
        }

        try
        {
            IResource resource = fJavaProject.getUnderlyingResource();
            IMarker[] jprojectMarkers = new IMarker[0];
            if (resource != null && resource.exists())
                jprojectMarkers =
                    resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            if (jprojectMarkers.length > 0)
            {
                Markers.removeProblemsForProject(getProject());
                Markers.addBuildBrokenProblemMarkerToResource(
                    getProject(),
                    TapestryCore.getString(STRING_KEY + "java-builder-failed"));
                return false;
            }
        } catch (CoreException e)
        {
            // assume there are no Java builder problems 
        }

        try
        {
            IPath outputPath = fJavaProject.getOutputLocation();
            IPath projectPath = fJavaProject.getPath();
            if (projectPath.equals(outputPath))
            {
                Markers.removeProblemsFor(fCurrentProject);
                Markers.addBuildBrokenProblemMarkerToResource(
                    fCurrentProject,
                    TapestryCore.getString(STRING_KEY + "abort-invalid-output-location", outputPath.toString()));
                return false;
            }
        } catch (JavaModelException e1)
        {
            Markers.removeProblemsFor(fCurrentProject);
            Markers.addBuildBrokenProblemMarkerToResource(
                fCurrentProject,
                TapestryCore.getString(STRING_KEY + "abort-no-output-location"));
            return false;
        }

        // make sure all prereq projects have valid build states... 
        IProject[] requiredProjects = getRequiredProjects(false);
        next : for (int i = 0, length = requiredProjects.length; i < length; i++)
        {
            IProject p = requiredProjects[i];
            if (getLastState(p) == null)
            {
                if (DEBUG)
                    System.out.println(TapestryCore.getString(STRING_KEY + "abort-prereq-not-built", p.getName()));
                Markers.removeProblemsFor(fCurrentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    fCurrentProject,
                    TapestryCore.getString(STRING_KEY + "abort-prereq-not-built", p.getName()));

                return false;
            }
        }

        if (getType(TapestryCore.getString(STRING_KEY + "applicationServletClassname")) == null)
        {
            Markers.removeProblemsForProject(getProject());
            Markers.addBuildBrokenProblemMarkerToResource(
                fCurrentProject,
                TapestryCore.getString(STRING_KEY + "tapestry-jar-missing"));
            return false;
        }

        int projectType = fTapestryProject.getProjectType();

        if (projectType == TapestryProject.APPLICATION_PROJECT_TYPE)
        {
            if (fContextRoot == null || !fContextRoot.exists())
            {
                Markers.removeProblemsFor(fCurrentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    fCurrentProject,
                    TapestryCore.getString(STRING_KEY + "missing-context"));
                return false;
            }

            IResourceWorkspaceLocation webXML =
                (IResourceWorkspaceLocation) fContextRoot.getRelativeLocation("WEB-INF/web.xml");
            if (!webXML.exists())
            {
                Markers.removeProblemsFor(fCurrentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    fCurrentProject,
                    TapestryCore.getString(STRING_KEY + "abort-missing-web-xml", webXML.toString()));
                return false;
            }
        } else if (projectType == TapestryProject.LIBRARY_PROJECT_TYPE)
        {
            ClasspathResourceWorkspaceLocation libLoc = null;
            try
            {
                libLoc = (ClasspathResourceWorkspaceLocation) fTapestryProject.getLibraryLocation();
            } catch (CoreException e2)
            {}
            if (libLoc == null || !libLoc.exists())
            {
                Markers.removeProblemsFor(fCurrentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    fCurrentProject,
                    TapestryCore.getString(STRING_KEY + "abort-missing-library-spec", libLoc.toString()));
                return false;
            }
            //            IPackageFragment fragment = null;
            //            boolean isBinaryPackage = false;
            //            try
            //            {
            //                IFolder container = (IFolder) librarySpec.getParent();
            //                fragment = (IPackageFragment) JavaCore.create(container);
            //                if (fragment != null)
            //                {
            //                    IPackageFragmentRoot fragRoot = (IPackageFragmentRoot) fragment.getParent();
            //                    isBinaryPackage = fragRoot.getKind() == IPackageFragmentRoot.K_BINARY;
            //                }
            //            } catch (JavaModelException e2)
            //            {
            //                // do nothing
            //            }
            //
            //            if (fragment == null || isBinaryPackage)
            //            {
            //                Markers.removeProblemsFor(fCurrentProject); // make this the only problem for this project
            //                Markers.addBuildBrokenProblemMarkerToResource(
            //                    fCurrentProject,
            //                    TapestryCore.getString(
            //                        STRING_KEY + "-abort-library-spec-not-on-classpath",
            //                        librarySpec.getFullPath()));
            //                return false;
            if (!libLoc.getProject().equals(fJavaProject.getProject()))
            {
                Markers.removeProblemsFor(fCurrentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    fCurrentProject,
                    TapestryCore.getString(STRING_KEY + "abort-library-not-in-this-project", libLoc.toString()));
                return false;
            }
        }

        return true;
    }

    /**
     * Method initializeBuilder.
     */
    private void initializeBuilder()
    {

        try
        {
            fJavaProject = (IJavaProject) fCurrentProject.getNature(JavaCore.NATURE_ID);

        } catch (CoreException e)
        {
            TapestryCore.log(e);
            throw new BuilderException("could not obtain the Java Project!");
        }

        try
        {
            fTapestryProject = (TapestryProject) fCurrentProject.getNature(TapestryCore.NATURE_ID);
        } catch (CoreException e)
        {
            TapestryCore.log(e);
            throw new BuilderException("could not obtain the Tapestry Project!");
        }
        fContextRoot = new ContextRootLocation(fTapestryProject.getWebContextFolder());

        try
        {
            fClasspathRoot = fTapestryProject.getClasspathRoot();
        } catch (CoreException e1)
        {

            throw new BuilderException("could not obtain the Classpath Root!");
        }

    }

    protected IType getType(String fullyQualifiedName)
    {
        try
        {
            return fJavaProject.findType(fullyQualifiedName);
        } catch (JavaModelException e)
        {
            return null;
        }
    }

}
