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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.iw.plugins.spindle.core.TapestryCoreException;
import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;
import com.iw.plugins.spindle.core.eclipse.TapestryProject;
import com.iw.plugins.spindle.core.parser.dom.IDOMModelSource;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.resources.eclipse.ClasspathResource;
import com.iw.plugins.spindle.core.resources.eclipse.ClasspathRoot;
import com.iw.plugins.spindle.core.resources.eclipse.ContextResource;
import com.iw.plugins.spindle.core.resources.eclipse.ContextRoot;
import com.iw.plugins.spindle.core.resources.search.ISearch;
import com.iw.plugins.spindle.core.resources.search.eclipse.AbstractEclipseSearchAcceptor;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.SourceLocation;
import com.iw.plugins.spindle.core.util.Assert;
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

    private static String  PACKAGE_CACHE = "PACKAGE_CACHE";

    private static String STORAGE_CACHE = "STORAGE_CACHE"; 
    
    private static String CLASSPATH_SEARCH_CACHE = "CLASSPATH_SEARCH_CACHE"; 

    public static Map getPackageCache()
    {
        return getOrCreateCache(PACKAGE_CACHE);
    }

    public static Map getStorageCache()
    {
        return getOrCreateCache(STORAGE_CACHE);
    }
    
    public static Map getClasspathSearchCache() {
        return getOrCreateCache(CLASSPATH_SEARCH_CACHE);
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

    private List fExcludedFileNames;

    /**
     * Constructor for TapestryBuilder.
     */
    public EclipseBuildInfrastructure(IProject project, IProgressMonitor monitor,
            IResourceDelta delta, IDOMModelSource domModelSource)
    {
        super();
        fCurrentProject = project;
        fNotifier = new BuildNotifier(monitor, fCurrentProject);
        fDOMModelSource = domModelSource;
    }

    public void executeBuild(boolean requestIncremental, Map args)
    {
        fProblemPersister = new Markers();

        fNotifier.begin();        

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
                    IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR, e.getMessage(),
                    SourceLocation.FILE_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));
            if (AbstractBuildInfrastructure.DEBUG)
                System.err.println("Tapestry build aborted: " + e.getMessage());
        }
        catch (BuilderException e)
        {
            fProblemPersister.removeAllProblems(fTapestryProject);
            fProblemPersister.recordProblem(fTapestryProject, new DefaultProblem(
                    IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR, e.getMessage(),
                    SourceLocation.FOLDER_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));
            if (AbstractBuildInfrastructure.DEBUG)
                System.err.println("Tapestry build aborted: " + e.getMessage());

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
            if (!ok)
                // If the build failed, clear the previously built state,
                // forcing a full build next time.
                clearLastState();
            fBuild.cleanUp();
            fNotifier.done();
            fDeferredActions.clear();
            TapestryCore.buildOccurred();
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

        ICoreResource webXML = (ICoreResource) fContextRoot.getRelativeResource("WEB-INF/web.xml");

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

        fContextRoot = fTapestryProject.getWebContextLocation();
        if (fContextRoot == null || !fContextRoot.exists())
            throw new BuilderException("could not obtain the servlet context root folder");

        fClasspathRoot = fTapestryProject.getClasspathRoot();
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
    public void findAllTapestryArtifactsInClasspath(Set knownTemplateExtensions, final ArrayList found)
    {
        Assert.isLegal(knownTemplateExtensions != null && !knownTemplateExtensions.isEmpty());        
        ISearch searcher = null;
        try
        {
            searcher = fClasspathRoot.getSearch();
            searcher.search(new ArtifactCollector(knownTemplateExtensions,
                    getExcludedFileNames())
            {
                public boolean acceptTapestry(Object parent, Object leaf)
                {
                    IPackageFragment fragment = (IPackageFragment) parent;
                    ClasspathRoot root = (ClasspathRoot) fClasspathRoot;
                    ICoreResource location = new ClasspathResource(root, fragment, (IStorage) leaf);
                    found.add(location);

                    return keepGoing();
                }
            });
        }
        catch (TapestryCoreException e)
        {
            TapestryCore.log(e);
        }
    }

    /**
     * Find and add all files with Tapestry extensions found in the web context to a List.
     */
    public void findAllTapestryArtifactsInWebContext(Set knownTemplateExtensions, final ArrayList found)
    {
        ISearch searcher = null;
        try
        {
            searcher = fContextRoot.getSearch();
            searcher.search(new ArtifactCollector(knownTemplateExtensions,
                    getExcludedFileNames())
            {
                public boolean acceptTapestry(Object parent, Object leaf)
                {
                    IResource resource = (IResource) leaf;
                    ContextRoot contextRoot = (ContextRoot) fContextRoot;
                    ICoreResource coreResource = new ContextResource(contextRoot, resource);

                    if (coreResource.exists() && !conflictsWithJavaOutputDirectory(resource))
                        found.add(coreResource);

                    return keepGoing();

                }
            });
        }
        catch (TapestryCoreException e)
        {
            TapestryCore.log(e);
        }
    }

    // TODO this should be configurable.
    public List getExcludedFileNames()
    {
        if (fExcludedFileNames == null)
        {
            fExcludedFileNames = new ArrayList();
            fExcludedFileNames.add("package.html");
        }
        return fExcludedFileNames;
    }

    /**
     * A search acceptor that is used to find all the Tapestry artifacts in the web context or the
     * classpath.
     */
    abstract class ArtifactCollector extends AbstractEclipseSearchAcceptor
    {
        public ArtifactCollector(Set allowed, List excluded)
        {
            super(ACCEPT_ANY, allowed, excluded); 
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