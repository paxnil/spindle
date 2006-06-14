package net.sf.spindle.core.builder;

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
 * Portions created by the Initial Developer are Copyright (C) 2001-2005, 2006
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.spindle.core.CoreMessages;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.build.AbstractBuild;
import net.sf.spindle.core.build.AbstractBuildInfrastructure;
import net.sf.spindle.core.build.BuilderException;
import net.sf.spindle.core.build.FullBuild;
import net.sf.spindle.core.build.State;
import net.sf.spindle.core.build.WebXMLScanner;
import net.sf.spindle.core.eclipse.EclipseMessages;
import net.sf.spindle.core.eclipse.TapestryCorePlugin;
import net.sf.spindle.core.eclipse.TapestryProject;
import net.sf.spindle.core.parser.IDOMModelSource;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.eclipse.ContextResource;
import net.sf.spindle.core.resources.eclipse.ContextRoot;
import net.sf.spindle.core.resources.search.ISearch;
import net.sf.spindle.core.resources.search.eclipse.AbstractEclipseSearchAcceptor;
import net.sf.spindle.core.util.eclipse.Markers;

import org.apache.hivemind.Resource;
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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * The Tapestry Builder, kicks off full and incremental builds.
 * 
 * @author glongman@gmail.com
 */
/**
 * @author GLONGMAN
 */
public class EclipseBuildInfrastructure extends AbstractBuildInfrastructure
{

    public static class EclipseState extends State<IStorage>
    {

        public EclipseState()
        {
            super();
        }

        public EclipseState(AbstractBuildInfrastructure infrastructure)
        {
            super(infrastructure);
        }
    }

   
    private static String PACKAGE_CACHE = "PACKAGE_CACHE";

    private static String STORAGE_CACHE = "STORAGE_CACHE";

    private static String CLASSPATH_SEARCH_CACHE = "CLASSPATH_SEARCH_CACHE";

    public static Map getClasspathSearchCache()
    {
        return getOrCreateCache(CLASSPATH_SEARCH_CACHE);
    }

    public static Map getPackageCache()
    {
        return getOrCreateCache(PACKAGE_CACHE);
    }

    public static Map getStorageCache()
    {
        return getOrCreateCache(STORAGE_CACHE);
    }

    public static State readState(DataInputStream in) throws IOException
    {
        return State.read(in);
    }

    public static void writeState(Object state, DataOutputStream out) throws IOException
    {
        ((State) state).write(out);
    }

    private IProject currentIProject;

    private IJavaProject javaProject;

    private IWorkspaceRoot workspaceRoot;

    private IClasspathEntry[] classpathEntries;

    private IResourceDelta fDelta;

    private List<String> excludedFileNames;

    private boolean projectSupportsAnnotations = false;

    /**
     * Constructor for TapestryBuilder.
     */
    public EclipseBuildInfrastructure(IProject project, IProgressMonitor monitor,
            IResourceDelta delta, IDOMModelSource domModelSource)
    {
        super();
        currentIProject = project;
        notifier = new EclipseBuildNotifier(monitor);
        this.domModelSource = domModelSource;
        projectSupportsAnnotations = doesProjectSupportJavaAnnotations(project);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildInfrastructure#copyClasspathMemento(java.lang.Object)
     */
    public Object copyClasspathMemento(Object memento)
    {
        IClasspathEntry[] source = (IClasspathEntry[]) memento;
        IClasspathEntry[] result = new IClasspathEntry[source.length];
        System.arraycopy(source, 0, result, 0, source.length);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildInfrastructure#findAllTapestrySourceFiles(java.util.Set,
     *      java.util.ArrayList)
     */
    public void findAllTapestrySourceFiles(Set<String> knownTemplateExtensions,
            final ArrayList<Resource> found)
    {
        // first in the web context
        ISearch searcher = null;
        try
        {
            searcher = contextRoot.getSearch();
            searcher.search(new ArtifactCollector(knownTemplateExtensions, getExcludedFileNames())
            {
                public boolean acceptTapestry(Object parent, Object leaf)
                {
                    IResource resource = (IResource) leaf;
                    ContextRoot ctxRoot = (ContextRoot) contextRoot;
                    ICoreResource coreResource = new ContextResource(ctxRoot, resource);

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

        // now in any source folders.

        // TODO implement
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildInfrastructure#getClasspathMemento()
     */
    public Object getClasspathMemento()
    {
        return classpathEntries;
    }

    // TODO this should be configurable.
    public List<String> getExcludedFileNames()
    {
        if (excludedFileNames == null)
        {
            excludedFileNames = new ArrayList<String>();
            excludedFileNames.add("package.html");
        }
        return excludedFileNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildInfrastructure#getLastState()
     */
    public State getLastState()
    {
        return (State) TapestryArtifactManager.getTapestryArtifactManager().getLastBuildState(
                currentIProject,
                false);
    }

    public IProject[] getRequiredProjects(boolean includeBinaryPrerequisites)
    {
        if (javaProject == null || workspaceRoot == null)
            return new IProject[0];

        ArrayList<IProject> projects = new ArrayList<IProject>();
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
     * @see core.builder.AbstractBuildInfrastructure#persistState(core.builder.State)
     */
    public void persistState(State state)
    {
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
                currentIProject,
                (EclipseState) state);
    }

    public boolean projectSupportsAnnotations()
    {
        return projectSupportsAnnotations;
    }

    boolean conflictsWithJavaOutputDirectory(IResource resource)
    {
        try
        {
            IPath containerPath = javaProject.getOutputLocation();
            return containerPath.isPrefixOf(resource.getFullPath());
        }
        catch (JavaModelException e)
        {
            // do nothing
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildInfrastructure#clearLastState()
     */
    protected void clearLastState()
    {
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
                currentIProject,
                null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildInfrastructure#createIncrementalBuild()
     */
    @Override
    protected AbstractBuild createIncrementalBuild()
    {
        @SuppressWarnings("unused")
        IResourceDelta delta = fDelta;
        return null; // FIXME when incremental is fixed.
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildInfrastructure#initialize()
     */
    protected void initialize()
    {
        problemPersister = Markers.getInstance();

        // is it a java project at all?
        try
        {
            javaProject = (IJavaProject) currentIProject.getNature(JavaCore.NATURE_ID);

        }
        catch (CoreException e)
        {
            TapestryCore.log(e);
            throw new BuilderException(EclipseMessages.mustBeAJavaProject());
        }

        // is it a Tapestry project?
        try
        {
            TapestryProject project = (TapestryProject) currentIProject
                    .getNature(TapestryCorePlugin.NATURE_ID);
            tapestryProject = project;
            project.clearMetadata();
        }
        catch (CoreException e)
        {
            TapestryCore.log(e);
            throw new BuilderException(EclipseMessages.tapestryProjectNotExist());
        }

        // better have a context root
        contextRoot = tapestryProject.getWebContextLocation();
        if (contextRoot == null || !contextRoot.exists())
            throw new BuilderException(CoreMessages.contextRootNotExist(contextRoot));

        // better have a classpath root!
        classpathRoot = tapestryProject.getClasspathRoot();
        if (classpathRoot == null || !classpathRoot.exists())
            throw new BuilderException(CoreMessages.classpathRootNotExist(classpathRoot));

        validateWebXML = tapestryProject.isValidatingWebXML();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildInfrastructure#isWorthBuilding()
     */
    protected boolean isWorthBuilding()
    {
        // project must exist
        if (javaProject == null || !javaProject.exists())
            throw new BuilderException(EclipseMessages.mustBeAJavaProject());

        // must have a vlid classpath
        try
        {
            classpathEntries = javaProject.getResolvedClasspath(true);
        }
        catch (JavaModelException e3)
        {
            throw new BuilderException(EclipseMessages.unableToDetermineClasspath());
        }

        // must not have fatal compiler problems
        try
        {
            IResource resource = javaProject.getUnderlyingResource();
            IMarker[] jprojectMarkers = new IMarker[0];
            if (resource != null && resource.exists())
                jprojectMarkers = resource.findMarkers(
                        IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
                        false,
                        IResource.DEPTH_ZERO);
            if (jprojectMarkers.length > 0)
                throw new BuilderException(EclipseMessages.javaBuilderFailed());

        }
        catch (CoreException e)
        {
            // assume there are no Java builder problems
        }

        // the project must not be the compiler output location
        try
        {
            IPath outputPath = javaProject.getOutputLocation();
            IPath projectPath = javaProject.getPath();

            if (projectPath.equals(outputPath))
                throw new BuilderException(EclipseMessages.invalidCompilerOutputPath(outputPath
                        .toString()));
        }
        catch (JavaModelException e1)
        {
            throw new BuilderException(EclipseMessages.unableToDetermineCompilerOutputPath());
        }

        // make sure all prereq projects have valid build states...
        IProject[] requiredProjects = getRequiredProjects(false);
        for (int i = 0, length = requiredProjects.length; i < length; i++)
        {
            IProject p = requiredProjects[i];
            if (getLastState() == null)
            {
                String message = EclipseMessages.prerequisiteProjectNotBuilt(p);
                if (DEBUG)
                    System.out.println(message);
                throw new BuilderException(message);
            }
        }

        // tapestry must be on the classpath!
        if (tapestryProject.findType(CoreMessages.getTapestryServletClassname()) == null)
            throw new BuilderException(CoreMessages.tapestryJarsMissing());

        // the context root must exist
        if (contextRoot == null || !contextRoot.exists())
            throw new BuilderException(CoreMessages.contextRootNotExist(contextRoot));

        ICoreResource webXML = (ICoreResource) contextRoot.getRelativeResource("WEB-INF/web.xml");

        // web.xml must exist in the context at the expected place.
        if (!webXML.exists())
            throw new BuilderException(CoreMessages.missingWebXMLFile(webXML));

        return true;
    }

    private boolean doesProjectSupportJavaAnnotations(IProject project)
    {
        if (true)
            return false; // FIXME get rid of this when we figure out how to handle annotations!

        IJavaProject jproject = JavaCore.create(project);

        if (jproject == null || !jproject.exists() || !project.isAccessible())
            return false;

        Map options = jproject.getOptions(true);

        String target = (String) options.get(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM);

        if (target == null)
            return false;

        float version = Float.parseFloat(target);

        if (version < 1.5)
            return false;

        System.out.println("target: " + target);

        String sourceCompatibility = (String) options.get(JavaCore.COMPILER_SOURCE);

        if (sourceCompatibility == null)
            return false;

        float compat = Float.parseFloat(sourceCompatibility);

        if (compat < 1.5)
            return false;

        System.out.println("sourceCompatibility: " + sourceCompatibility);

        return true;

    }

    /**
     * A search acceptor that is used to find all the Tapestry artifacts in the web context
     */
    abstract class ArtifactCollector extends AbstractEclipseSearchAcceptor
    {
        public ArtifactCollector(Set<String> allowed, List<String> excluded)
        {
            super(ACCEPT_ANY, allowed, excluded);
        }

        public boolean keepGoing()
        {
            try
            {
                notifier.checkCancel();
            }
            catch (OperationCanceledException e)
            {
                return false;
            }
            return true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.build.AbstractBuildInfrastructure#createEmptyState()
     */
    @Override
    public State createEmptyState()
    {
        return new EclipseState(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.build.AbstractBuildInfrastructure#createWebXMLScanner(net.sf.spindle.core.build.FullBuild)
     */
    @Override
    public WebXMLScanner createWebXMLScanner(FullBuild build)
    {
        return new EclipseWebXMLScanner(build);
    }

}