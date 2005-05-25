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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.osgi.framework.Bundle;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryCorePlugin;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.eclipse.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.eclipse.ContextRootLocation;
import com.iw.plugins.spindle.core.resources.search.ISearch;
import com.iw.plugins.spindle.core.resources.search.eclipse.AbstractEclipseSearchAcceptor;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.util.eclipse.EclipsePluginUtils;
import com.iw.plugins.spindle.core.util.eclipse.Markers;

/**
 * The Tapestry Builder, kicks off full and incremental builds.
 * 
 * @author glongman@gmail.com
 */
public class EclipseBuildInfrastructure extends AbstractBuildInfrastructure
{

    private final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi");

    private static ThreadLocal PACKAGE_CACHE;

    private static ThreadLocal STORAGE_CACHE;

    static
    {
        PACKAGE_CACHE = new ThreadLocal();
        STORAGE_CACHE = new ThreadLocal();
    }

    public static Map getPackageCache()
    {
        return (Map) PACKAGE_CACHE.get();
    }

    public static Map getStorageCache()
    {
        return (Map) STORAGE_CACHE.get();
    }

    // TODO this is really ugly, but I need this fast.
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

    IWorkspaceRoot fWorkspaceRoot;

    IClasspathEntry[] fClasspath;

    IResourceDelta fDelta;

    private WebXMLScanner fWebXMLScanner;

    /**
     * Constructor for TapestryBuilder.
     */
    public EclipseBuildInfrastructure(IProject project, IProgressMonitor monitor,
            IResourceDelta delta)
    {
        super();
        fCurrentProject = project;
        fNotifier = new BuildNotifier(monitor, fCurrentProject);
    }

    public void executeBuild(boolean requestIncremental, Map args)
    {
        fProblemPersister = new Markers();

        fNotifier.begin();

        PACKAGE_CACHE.set(new HashMap());
        STORAGE_CACHE.set(new HashMap());

        boolean ok = false;

        try
        {
            fNotifier.checkCancel();
            initialize();
            if (isWorthBuilding())
            {

                if (!requestIncremental)
                {
                    buildAll();
                }
                else
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
        }
        catch (CoreException e)
        {
            ErrorDialog.openError(EclipsePluginUtils.getWorkbench().getActiveWorkbenchWindow()
                    .getShell(), CoreMessages.format("build-failed-core-title"), CoreMessages
                    .format("build-failed-core-message"), e.getStatus());
        }
        catch (BrokenWebXMLException e)
        {
            fProblemPersister.recordProblem(fTapestryProject, new DefaultProblem(
                    IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR, e.getMessage(), 0, 0, 0,
                    false, IProblem.NOT_QUICK_FIXABLE));
        }
        catch (BuilderException e)
        {
            fProblemPersister.removeAllProblems(fTapestryProject);
            fProblemPersister.recordProblem(fTapestryProject, new DefaultProblem(
                    IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR, e.getMessage(), 0, 0, 0,
                    false, IProblem.NOT_QUICK_FIXABLE));
        }
        catch (NullPointerException e)
        {
            TapestryCore.log(e);
            throw e;
        }
        catch (RuntimeException e)
        {
            TapestryCore.log(e);
            throw e;
        }
        finally
        {
            PACKAGE_CACHE.set(null);
            STORAGE_CACHE.set(null);
            if (!ok)
                // If the build failed, clear the previously built state,
                // forcing a full build next time.
                clearLastState();
            fBuild.cleanUp();
            fNotifier.done();
            fDeferredActions.clear();
            TapestryCore.getDefault().buildOccurred();
        }
    }

    public IProject[] getRequiredProjects(boolean includeBinaryPrerequisites)
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
                        if (workspaceProject.hasNature(TapestryCorePlugin.NATURE_ID))
                            p = workspaceProject;

                    }
                    if (p != null && !projects.contains(p))
                        projects.add(p);

                }
            }
        }
        catch (CoreException e)
        {
            return new IProject[0];
        }
        IProject[] result = new IProject[projects.size()];
        projects.toArray(result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.builder.AbstractBuildInfrastructure#getClasspathMemento()
     */
    Object getClasspathMemento()
    {
        return fClasspath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.builder.AbstractBuildInfrastructure#copyClasspathMemento()
     */
    Object copyClasspathMemento(Object memento)
    {
        IClasspathEntry[] source = (IClasspathEntry[]) memento;
        IClasspathEntry[] result = new IClasspathEntry[source.length];
        System.arraycopy(source, 0, result, 0, source.length);
        return result;
    }

    State getLastState()
    {
        return (State) TapestryArtifactManager.getTapestryArtifactManager().getLastBuildState(
                fCurrentProject,
                false);
    }

    void persistState(State state)
    {
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
                fCurrentProject,
                state);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.builder.AbstractBuildInfrastructure#createWebXMLScanner()
     */
    WebXMLScanner createWebXMLScanner()
    {
        return new EclipseWebXMLScanner(fBuild);
    }

    /**
     * Method clearLastState.
     */
    private void clearLastState()
    {
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
                fCurrentProject,
                null);
    }

    /**
     * Method buildAll.
     */
    private void buildAll() throws BuilderException, CoreException
    {
        if (AbstractBuildInfrastructure.DEBUG)
            System.out.println("FULL Tapestry build");

        fNotifier.subTask(CoreMessages.format(AbstractBuildInfrastructure.STRING_KEY
                + "full-build-starting"));

        fProblemPersister.removeProblems(fTapestryProject);

        fBuild = new FullBuild(this);

        fBuild.build();

    }

    private void buildIncremental() throws BuilderException, CoreException
    {

        IncrementalEclipseProjectBuild incBuild = new IncrementalEclipseProjectBuild(this, fDelta);

        if (incBuild.canIncrementalBuild())
        {
            if (!incBuild.needsIncrementalBuild())
                return;

            fBuild = incBuild;
            if (DEBUG)
                System.out.println("Incremental Tapestry build");

            fNotifier.subTask(CoreMessages.format(STRING_KEY + "incremental-build-starting"));
            incBuild.build();
        }
        else
        {
            buildAll();
        }
    }

    /**
     * Method isWorthBuilding.
     * 
     * @return boolean
     */
    private boolean isWorthBuilding()
    {

        if (fJavaProject == null)
            throw new BuilderException(CoreMessages.format(STRING_KEY + "non-java-projects"));

        try
        {
            fClasspath = fJavaProject.getResolvedClasspath(true);
        }
        catch (JavaModelException e3)
        {
            throw new BuilderException(CoreMessages.format(STRING_KEY + "classpath-not-determined"));
        }

        try
        {
            IResource resource = fJavaProject.getUnderlyingResource();
            IMarker[] jprojectMarkers = new IMarker[0];
            if (resource != null && resource.exists())
                jprojectMarkers = resource.findMarkers(
                        IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_ZERO);
            if (jprojectMarkers.length > 0)
                throw new BuilderException(CoreMessages.format(STRING_KEY + "java-builder-failed"));

        }
        catch (CoreException e)
        {
            // assume there are no Java builder problems
        }

        try
        {
            IPath outputPath = fJavaProject.getOutputLocation();
            IPath projectPath = fJavaProject.getPath();

            if (projectPath.equals(outputPath))
                throw new BuilderException(CoreMessages.format(STRING_KEY
                        + "abort-invalid-output-location", outputPath.toString()));

        }
        catch (JavaModelException e1)
        {
            throw new BuilderException(CoreMessages.format(STRING_KEY + "abort-no-output-location"));
        }

        // make sure all prereq projects have valid build states...
        IProject[] requiredProjects = getRequiredProjects(false);
        next: for (int i = 0, length = requiredProjects.length; i < length; i++)
        {
            IProject p = requiredProjects[i];
            if (getLastState() == null)
            {
                if (DEBUG)
                    System.out.println(CoreMessages.format(STRING_KEY + "abort-prereq-not-built", p
                            .getName()));
                throw new BuilderException(CoreMessages.format(STRING_KEY
                        + "abort-prereq-not-built", p.getName()));
            }
        }

        if (fTapestryProject.findType(CoreMessages.format(STRING_KEY
                + "applicationServletClassname")) == null)
            throw new BuilderException(CoreMessages.format(STRING_KEY + "tapestry-jar-missing"));

        if (fContextRoot == null || !fContextRoot.exists())
            throw new BuilderException(CoreMessages.format(STRING_KEY + "missing-context"));

        IResourceWorkspaceLocation webXML = (IResourceWorkspaceLocation) fContextRoot
                .getRelativeResource("WEB-INF/web.xml");

        if (!webXML.exists())
            throw new BuilderException(CoreMessages.format(
                    STRING_KEY + "abort-missing-web-xml",
                    webXML.toString()));

        return true;
    }

    /**
     * Method initializeBuilder.
     */
    private void initialize()
    {
        try
        {
            fJavaProject = (IJavaProject) fCurrentProject.getNature(JavaCore.NATURE_ID);

        }
        catch (CoreException e)
        {
            TapestryCore.log(e);
            throw new BuilderException("could not obtain the Java Project!");
        }

        try
        {
            TapestryProject project = (TapestryProject) fCurrentProject
                    .getNature(TapestryCorePlugin.NATURE_ID);
            fTapestryProject = project;
            project.clearMetadata();
        }
        catch (CoreException e)
        {
            TapestryCore.log(e);
            throw new BuilderException("could not obtain the Tapestry Project!");
        }

        fContextRoot = (IResourceWorkspaceLocation) fTapestryProject.getWebContextLocation();
        if (fContextRoot == null || !fContextRoot.exists())
            throw new BuilderException("could not obtain the servlet context root folder");

        fClasspathRoot = (IResourceWorkspaceLocation) fTapestryProject.getClasspathRoot();
        if (fClasspathRoot == null || !fClasspathRoot.exists())
            throw new BuilderException("could not obtain the Classpath Root!");

        fValidateWebXML = fTapestryProject.isValidatingWebXML();

    }

    boolean conflictsWithJavaOutputDirectory(IResource resource)
    {
        try
        {
            IPath containerPath = fJavaProject.getOutputLocation();
            return containerPath.isPrefixOf(resource.getFullPath());
        }
        catch (JavaModelException e)
        {
            // do nothing
        }
        return false;
    }

    /**
     * Find and add all files with Tapestry extensions found in the classpath to a List.
     */
    public void findAllTapestryArtifactsInClasspath(final ArrayList found)
    {
        ISearch searcher = null;
        try
        {
            searcher = fClasspathRoot.getSearch();
        }
        catch (RuntimeException e)
        {
            TapestryCore.log(e);
        }
        if (searcher != null)
        {
            searcher.search(new ArtifactCollector()
            {
                public boolean acceptTapestry(Object parent, Object leaf)
                {
                    IPackageFragment fragment = (IPackageFragment) parent;
                    IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) ((ClasspathRootLocation) fClasspathRoot)
                            .getRelativeLocation(fragment, (IStorage) leaf);
                    found.add(location);

                    return keepGoing();
                }
            });
        }
    }

    /**
     * Find and add all files with Tapestry extensions found in the web context to a List.
     */
    public void findAllTapestryArtifactsInWebContext(final ArrayList found)
    {
        ISearch searcher = null;
        try
        {
            searcher = fContextRoot.getSearch();
        }
        catch (RuntimeException e)
        {
            TapestryCore.log(e);
        }
        if (searcher != null)
        {
            searcher.search(new ArtifactCollector()
            {
                public boolean acceptTapestry(Object parent, Object leaf)
                {
                    IResource resource = (IResource) leaf;
                    IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) ((ContextRootLocation) fContextRoot)
                            .getRelativeResource(resource);

                    if (!conflictsWithJavaOutputDirectory(resource))
                        found.add(location);

                    return keepGoing();

                }
            });
        }
    }

    /**
     * A search acceptor that is used to find all the Tapestry artifacts in the web context or the
     * classpath.
     */
    abstract class ArtifactCollector extends AbstractEclipseSearchAcceptor
    {

        public ArtifactCollector()
        {
            super();
        }

        public boolean keepGoing()
        {
            try
            {
                fNotifier.checkCancel();
            }
            catch (OperationCanceledException e)
            {
                return false;
            }
            return true;
        }
    }
}