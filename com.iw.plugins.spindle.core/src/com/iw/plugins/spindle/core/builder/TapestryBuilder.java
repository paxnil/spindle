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
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
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
    public static final String TAPESTRY_CLASS_NOT_FOUND = "unable-to-resolve-class";
    public static final String STRING_KEY = "builder-";
    public static final String APPLICATION_SERVLET_NAME = STRING_KEY + "applicationServletClassname";
    public static final String STARTING = STRING_KEY + "full-build-starting";
    public static final String SCANNING = STRING_KEY + "scanning";
    public static final String TAPESTRY_JAR_MISSING = STRING_KEY + "tapestry-jar-missing";
    public static final String LOCATING_ARTIFACTS = STRING_KEY + "locating-artifacts";
    public static final String MISSING_CONTEXT = STRING_KEY + "missing-context";
    public static final String NON_JAVA_PROJECTS = STRING_KEY + "non-java-projects";
    public static final String JAVA_BUILDER_FAILED = STRING_KEY + "java-builder-failed";
    public static final String ABORT_PREREQ_NOT_BUILT = STRING_KEY + "abort-prereq-not-built";
    public static final String ABORT_MISSING_WEB_XML = STRING_KEY + "abort-missing-web-xml";
    public static final String ABORT_INVALID_OUTPUT_LOCATION = STRING_KEY + "abort-invalid-output-location";
    public static final String ABORT_NO_OUTPUT_LOCATION = STRING_KEY + "abort-no-output-location";
    public static final String ABORT_MISSING_LIBRARY_SPEC = STRING_KEY + "abort-missing-library-spec";
    public static final String ABORT_LIBRARY_SPEC_NOT_ON_SOURCE_PATH =
        STRING_KEY + "-abort-library-spec-not-on-classpath";
    public static final String ABORT_LIBRARY_SPEC_IN_WRONG_PROJECT = STRING_KEY + "abort-library-not-in-this-project";
    public static final String ABORT_APPLICATION_NO_SERVLETS = STRING_KEY + "abort-no-valid-application-servlets-found";
    public static final String ABORT_APPLICATION_ONE_SERVLET_ONLY = STRING_KEY + "abort-too-many-valid-servlets-found";

    public static final String APPLICATION_EXTENSION = "application";
    public static final String COMPONENT_EXTENSION = "jwc";
    public static final String PAGE_EXTENSION = "page";
    public static final String TEMPLATE_EXTENSION = "html";
    public static final String SCRIPT_EXTENSION = "script";
    public static final String[] KnownExtensions =
        new String[] {
            APPLICATION_EXTENSION,
            COMPONENT_EXTENSION,
            PAGE_EXTENSION,
            TEMPLATE_EXTENSION,
            SCRIPT_EXTENSION };
    public static final String APP_SPEC_PATH_PARAM = "org.apache.tapestry.application-specification";
    public static final String ENGINE_CLASS_PARAM = "org.apache.tapestry.engine-class";

    public static boolean DEBUG = true;

    IProject currentProject;
    IJavaProject javaProject;
    TapestryProject tapestryProject;
    IWorkspaceRoot workspaceRoot;
    ContextRootLocation contextRoot;
    ClasspathRootLocation classpathRoot;
    int projectType;

    private IBuild build;

    BuildNotifier notifier;

    public static State readState(DataInputStream in) throws IOException
    {
        return State.read(in);
    }

    public static void writeState(Object state, DataOutputStream out) throws IOException
    {
        ((State) state).write(out);
    }

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
        this.currentProject = getProject();
        if (currentProject == null || !currentProject.isAccessible())
        {
            return new IProject[0];
        }

        if (true)
            System.out.println(
                "\nStarting build of " + currentProject.getName() + " @ " + new Date(System.currentTimeMillis()));
        this.notifier = new BuildNotifier(monitor, currentProject);
        notifier.begin();
        boolean ok = false;
        try
        {
            notifier.checkCancel();
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
                //          if ((this.lastState = getLastState(currentProject)) == null) {
                //            if (DEBUG)
                //              System.out.println("Performing full build since last saved state was not found"); //$NON-NLS-1$
                //            buildAll();
                //          } else if (hasClasspathChanged() || hasOutputLocationChanged()) {
                //            // if the output location changes, do not delete the binary files from old location
                //            // the user may be trying something
                //            buildAll();
                //          } else if (
                //            sourceFolders.length > 0) {
                //            // if there is no source to compile & no classpath changes then we are done
                //            SimpleLookupTable deltas = findDeltas();
                //            if (deltas == null)
                //              buildAll();
                //            else if (deltas.elementSize > 0)
                //              buildDeltas(deltas);
                //            else if (DEBUG)
                //              System.out.println("Nothing to build since deltas were empty"); //$NON-NLS-1$
                //          } else {
                //            if (hasBinaryDelta()) { // double check that a jar file didn't get replaced
                //              buildAll();
                //            } else {
                //              if (DEBUG)
                //                System.out.println("Nothing to build since there are no source folders and no deltas"); //$NON-NLS-1$
                //              this.lastState.tagAsNoopBuild();
                //            }
                //          }
                //        }

            }
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
            notifier.done();
            cleanup();
        }
        IProject[] requiredProjects = getRequiredProjects(true);
        if (DEBUG)
            System.out.println(
                "Finished build of " + currentProject.getName() + " @ " + new Date(System.currentTimeMillis()));
        return requiredProjects;
    }

    /**
     * Method cleanup.
     */
    private void cleanup()
    {
        if (build != null)
        {
            build.cleanUp();
        }
    }

    private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites)
    {
        if (javaProject == null || workspaceRoot == null)
            return new IProject[0];

        ArrayList projects = new ArrayList();
        try
        {
            IClasspathEntry[] entries = ((JavaProject) javaProject).getExpandedClasspath(true);
            for (int i = 0, length = entries.length; i < length; i++)
            {
                IClasspathEntry entry = JavaCore.getResolvedClasspathEntry(entries[i]);
                if (entry != null)
                {
                    IPath path = entry.getPath();
                    IProject p = null;
                    if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
                    {
                        IProject workspaceProject = workspaceRoot.getProject(path.lastSegment());
                        if (workspaceProject.hasNature(TapestryCore.NATURE_ID))
                        {
                            p = workspaceProject;
                        }

                    }
                    if (p != null && !projects.contains(p))
                    {
                        projects.add(p);
                    }
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

    private State getLastState(IProject project)
    {
        //    return (State) TapestryModelManager.getTapestryModelManager().getLastBuiltState(
        //      project,
        //      notifier.monitor);
        return null;
    }

    /**
     * Method clearLastState.
     */
    private void clearLastState()
    {}

    /**
     * Method buildAll.
     */
    private void buildAll() throws BuilderException
    {
        int type = tapestryProject.getProjectType();
        switch (type)
        {
            case TapestryProject.APPLICATION_PROJECT_TYPE :
                build = new FullBuild(this);
                break;

            case TapestryProject.LIBRARY_PROJECT_TYPE :

            default :
                break;
        }
        if (build != null)
        {
            build.build();
        }
    }

    private void buildIncremental() throws BuilderException
    {
        int type = tapestryProject.getProjectType();
        IIncrementalBuild inc = null;
        switch (type)
        {
            case TapestryProject.APPLICATION_PROJECT_TYPE :
                inc = new IncrementalApplicationBuild(this);
                break;

            case TapestryProject.LIBRARY_PROJECT_TYPE :
                inc = new IncrementalLibraryBuild(this);

            default :
                break;
        }
        if (inc != null && inc.canIncrementalBuild(getDelta(tapestryProject.getProject())))
        {
            build = inc;
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

        if (javaProject == null)
        {
            Markers.removeProblemsForProject(getProject());
            Markers.addBuildBrokenProblemMarkerToResource(getProject(), TapestryCore.getString(NON_JAVA_PROJECTS));
            return false;
        }

        try
        {
            IResource resource = javaProject.getUnderlyingResource();
            IMarker[] jprojectMarkers = new IMarker[0];
            if (resource != null && resource.exists())
                jprojectMarkers =
                    resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            if (jprojectMarkers.length > 0)
            {
                Markers.removeProblemsForProject(getProject());
                Markers.addBuildBrokenProblemMarkerToResource(
                    getProject(),
                    TapestryCore.getString(JAVA_BUILDER_FAILED));
                return false;
            }
        } catch (CoreException e)
        {
            // assume there are no Java builder problems 
        }

        try
        {
            IPath outputPath = javaProject.getOutputLocation();
            IPath projectPath = javaProject.getPath();
            if (projectPath.equals(outputPath))
            {
                Markers.removeProblemsFor(currentProject);
                Markers.addBuildBrokenProblemMarkerToResource(
                    currentProject,
                    TapestryCore.getString(ABORT_INVALID_OUTPUT_LOCATION, outputPath.toString()));
                return false;
            }
        } catch (JavaModelException e1)
        {
            Markers.removeProblemsFor(currentProject);
            Markers.addBuildBrokenProblemMarkerToResource(
                currentProject,
                TapestryCore.getString(ABORT_NO_OUTPUT_LOCATION));
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
                    System.out.println(TapestryCore.getString(ABORT_PREREQ_NOT_BUILT, p.getName()));
                Markers.removeProblemsFor(currentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    currentProject,
                    TapestryCore.getString(ABORT_PREREQ_NOT_BUILT, p.getName()));

                return false;
            }
        }

        if (getType(TapestryCore.getString(APPLICATION_SERVLET_NAME)) == null)
        {
            Markers.removeProblemsForProject(getProject());
            Markers.addBuildBrokenProblemMarkerToResource(
                currentProject,
                TapestryCore.getString(TapestryBuilder.TAPESTRY_JAR_MISSING));
            return false;
        }

        int projectType = tapestryProject.getProjectType();

        if (projectType == TapestryProject.APPLICATION_PROJECT_TYPE)
        {
            if (contextRoot == null || !contextRoot.exists())
            {
                Markers.removeProblemsFor(currentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(currentProject, TapestryCore.getString(MISSING_CONTEXT));
                return false;
            }

            IResourceWorkspaceLocation webXML =
                (IResourceWorkspaceLocation) contextRoot.getRelativeLocation("WEB-INF/web.xml");
            if (!webXML.exists())
            {
                Markers.removeProblemsFor(currentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    currentProject,
                    TapestryCore.getString(ABORT_MISSING_WEB_XML, webXML.toString()));
                return false;
            }
        } else if (projectType == TapestryProject.LIBRARY_PROJECT_TYPE)
        {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IFile librarySpec = root.getFile(new Path(tapestryProject.getLibrarySpecPath()));
            if (librarySpec == null || !librarySpec.exists())
            {
                Markers.removeProblemsFor(currentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    currentProject,
                    TapestryCore.getString(ABORT_MISSING_LIBRARY_SPEC, librarySpec.getFullPath()));
                return false;
            }
            IPackageFragment fragment = null;
            boolean isBinaryPackage = false;
            try
            {
                IFolder container = (IFolder) librarySpec.getParent();
                fragment = (IPackageFragment) JavaCore.create(container);
                if (fragment != null)
                {
                    IPackageFragmentRoot fragRoot = (IPackageFragmentRoot) fragment.getParent();
                    isBinaryPackage = fragRoot.getKind() == IPackageFragmentRoot.K_BINARY;
                }
            } catch (JavaModelException e2)
            {
                // do nothing
            }

            if (fragment == null || isBinaryPackage)
            {
                Markers.removeProblemsFor(currentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    currentProject,
                    TapestryCore.getString(ABORT_LIBRARY_SPEC_NOT_ON_SOURCE_PATH, librarySpec.getFullPath()));
                return false;
            } else if (!fragment.getJavaProject().equals(javaProject))
            {
                Markers.removeProblemsFor(currentProject); // make this the only problem for this project
                Markers.addBuildBrokenProblemMarkerToResource(
                    currentProject,
                    TapestryCore.getString(ABORT_LIBRARY_SPEC_IN_WRONG_PROJECT, librarySpec.getFullPath()));
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
            javaProject = (IJavaProject) currentProject.getNature(JavaCore.NATURE_ID);
            classpathRoot = new ClasspathRootLocation(javaProject);
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
        try
        {
            tapestryProject = (TapestryProject) currentProject.getNature(TapestryCore.NATURE_ID);
            contextRoot = new ContextRootLocation(tapestryProject.getWebContextFolder());
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }

    }

    protected IType getType(String fullyQualifiedName)
    {
        try
        {
            return javaProject.findType(fullyQualifiedName);
        } catch (JavaModelException e)
        {
            return null;
        }
    }

}
