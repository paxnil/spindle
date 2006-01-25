package core.builder;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hivemind.Resource;
import org.apache.tapestry.engine.IPropertySource;

import core.CoreMessages;
import core.ITapestryProject;
import core.TapestryCore;
import core.parser.dom.IDOMModelSource;
import core.properties.CorePropertySource;
import core.resources.ICoreResource;
import core.resources.IResourceRoot;
import core.source.DefaultProblem;
import core.source.IProblem;
import core.source.SourceLocation;
import core.types.IJavaType;
import core.types.IJavaTypeFinder;
import core.util.Assert;
import core.util.IProblemPeristManager;

/**
 * Represents the infrastructure needed to perfom a build.
 * <p>
 * It is intended that clients will subclass on a platform by platform basis.
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractBuildInfrastructure implements IJavaTypeFinder
{

    public static final String STRING_KEY = "builder-";

    public static final String APPLICATION_EXTENSION = "application";

    public static final String COMPONENT_EXTENSION = "jwc";

    public static final String PAGE_EXTENSION = "page";

    public static final String TEMPLATE_EXTENSION = "html";

    public static final String SCRIPT_EXTENSION = "script";

    public static final String LIBRARY_EXTENSION = "library";

    public static final String[] KnownExtensions = new String[]
    { APPLICATION_EXTENSION, COMPONENT_EXTENSION, PAGE_EXTENSION, TEMPLATE_EXTENSION,
    // SCRIPT_EXTENSION,
            LIBRARY_EXTENSION };

    public static final String APP_SPEC_PATH_PARAM = "org.apache.tapestry.application-specification";

    public static final String ENGINE_CLASS_PARAM = "org.apache.tapestry.engine-class";

    public static boolean DEBUG = true;

    private static final String TYPE_CACHE = "TYPE_CACHE";

    protected static ThreadLocal<Map> BUILD_CACHE;

    static
    {
        BUILD_CACHE = new ThreadLocal<Map>();
    }

    // TODO this is really ugly, but I need this fast.
    public static List fDeferredActions = new ArrayList();

    private static Map getBuildCache()
    {
        return (Map) BUILD_CACHE.get();
    }

    public static Map getTypeCache()
    {
        return getOrCreateCache(TYPE_CACHE);
    }

    @SuppressWarnings("unchecked")
	protected static Map getOrCreateCache(String key)
    {
        Map buildCache = getBuildCache();
        if (buildCache == null)
            return null;

        Map result = (Map) buildCache.get(key);
        if (result == null)
        {
            result = new HashMap();
            buildCache.put(key, result);
        }
        return result;
    }

    protected ITapestryProject tapestryProject;

    protected IResourceRoot contextRoot;

    protected IResourceRoot classpathRoot;

    protected IProblemPeristManager problemPersister;

    protected boolean validateWebXML;

    protected IBuildNotifier notifier;

    protected AbstractBuild build;

    protected IDOMModelSource domModelSource;

    protected CorePropertySource projectPropertySource;

    /**
     * Constructor for TapestryBuilder.
     */
    public AbstractBuildInfrastructure()
    {
        super();
    }

   
    public final void build(boolean requestIncremental, Map args)
    {
        BUILD_CACHE.set(new HashMap());
        try
        {
            executeBuild(requestIncremental, args);
        }
        finally
        {
            BUILD_CACHE.set(null);
        }
    }

    /**
     * Gives the infrastructure a chance to initialize itself set the resource roots etc.
     * <p>
     * Any problems here are fatal and must result in a {@link  BuilderException} being thrown. The
     * message of the exception will be recorder against the project.
     * 
     * @throws BuilderException
     */
    
    protected abstract void initialize() throws BuilderException;

    /**
     * last check before a build occurs. Up to the platform IDE implementor to decide if the project
     * is in a state where a build should proceed.
     * <p>
     * Fatal errors/state that need to be recorded against the project should result in a
     * {@link BuilderException}.
     * 
     * @return true if the project is worth building
     * @exception BuilderException
     *                for fatal errors that should be recorded against the project.
     */
    protected abstract boolean isWorthBuilding() throws BuilderException;

    private void executeBuild(boolean requestIncremental, Map args)
    {
        Assert.isNotNull(notifier);

        notifier.begin();

        boolean ok = false;

        try
        {
            initialize();
            notifier.checkCancel();

            if (isWorthBuilding())
            {
                // at this point the infrastructure must be complete.
                Assert.isNotNull(tapestryProject);
                Assert.isNotNull(contextRoot);
                Assert.isNotNull(classpathRoot);
                Assert.isNotNull(domModelSource);
                Assert.isNotNull(problemPersister);

                notifier.checkCancel();
                if (!requestIncremental)
                {
                    buildAll();
                }
                else
                {
                    buildIncremental();
                }
                ok = true;
            }
        }
        catch (BrokenWebXMLException e)
        {
            problemPersister.recordProblem(tapestryProject, new DefaultProblem(
                    IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR, e.getMessage(),
                    SourceLocation.FILE_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));
            if (AbstractBuildInfrastructure.DEBUG)
                System.err.println("Tapestry build aborted: " + e.getMessage());
        }
        catch (ClashException e)
        {
            problemPersister.removeAllProblems(tapestryProject);
            ICoreResource requestor = e.getRequestor();
            problemPersister.recordProblem(requestor, new DefaultProblem(
                    IProblem.TAPESTRY_CLASH_PROBLEM, IProblem.ERROR, "ACK-REQUESTOR",
                    SourceLocation.FILE_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));

            ICoreResource owner = e.getOwner();
            problemPersister.recordProblem(owner, new DefaultProblem(
                    IProblem.TAPESTRY_CLASH_PROBLEM, IProblem.ERROR, "ACK-OWNER",
                    SourceLocation.FILE_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));

            problemPersister.recordProblem(tapestryProject, new DefaultProblem(
                    IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR,
                    "Tapestry Build can't proceed due to namespace clashes",
                    SourceLocation.FOLDER_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));

            if (AbstractBuildInfrastructure.DEBUG)
                System.err.println("Tapestry build aborted: " + e.getMessage());
        }
        catch (BuilderException e)
        {
            problemPersister.removeAllProblems(tapestryProject);
            problemPersister.recordProblem(tapestryProject, new DefaultProblem(
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
            build.cleanUp();
            notifier.done();
            TapestryCore.buildOccurred();
        }
    }

    /**
     * Method buildAll.
     */
    private void buildAll() throws BuilderException
    {
        if (AbstractBuildInfrastructure.DEBUG)
            System.out.println("FULL Tapestry build");

        notifier.subTask(CoreMessages.format(AbstractBuildInfrastructure.STRING_KEY
                + "full-build-starting"));

        problemPersister.removeProblems(tapestryProject);

        this.build = createFullBuild();

        build.build();
    }

    private void buildIncremental() throws BuilderException
    {
        if (true) { //FIXME remove when incremental works again
            buildAll();
            return;
        }
        
        
        this.build = createIncrementalBuild();
        
        if (build == null) {
            buildAll();
            return;
        }            

        Assert.isLegal(build instanceof IIncrementalBuild);

        IIncrementalBuild incBuild = (IIncrementalBuild) this.build;

        if (incBuild.canIncrementalBuild())
        {
            if (!incBuild.needsIncrementalBuild())
                return;

            if (DEBUG)
                System.out.println("Incremental Tapestry build");

            notifier.subTask(CoreMessages.format(STRING_KEY + "incremental-build-starting"));
            incBuild.build();
        }
        else
        {
            buildAll();
        }
    }

    protected AbstractBuild createFullBuild()
    {
        return new FullBuild(this);
    }

    protected abstract AbstractBuild createIncrementalBuild();

    /**
     * Deep inside the build we need to find all the artifacts - for the purpose of provided
     * progress indications and to detect files that would be missed by Tapestry at runtime. We look
     * for .appliction, .library, .jwc, .page and template files that have the specified extensions.
     * <p>
     * It is up to the IDE platform implementor to decide if the results returned will include
     * tapestry files located in jar file. For example, searching in jars in Eclipse is slooooow so
     * Spindle doesn't look there, it just includes all source files in the web context and any
     * source folders.
     * <p>
     * It would not even be a sin to leave the found array empty - except that the progress
     * indications would be less informative.
     * 
     * @param knownTemplateExtensions
     * @param found
     *            all found artifacts are added to this list.
     */
    public abstract void findAllTapestrySourceFiles(Set<String> knownTemplateExtensions,
            ArrayList<Resource> found);

    /**
     * @return the state saved from the last build, if any
     */
    public abstract State getLastState();

    /**
     * Used by incremental builders to detect (via the {@link Object#equals(java.lang.Object)}
     * method) if the project classpath has changed. Incremental builds are normally disallowed when
     * the classpath has changed.
     * 
     * @return a memento of the state of the project's classpath
     */
    public abstract Object getClasspathMemento();

    public abstract Object copyClasspathMemento(Object memento);

    /**
     * Persist the state of this build for use in the next build (possibly incremental)
     * 
     * @param state
     */
    public abstract void persistState(State state);

    protected abstract void clearLastState();

    public abstract WebXMLScanner createWebXMLScanner();

    public IPropertySource installBasePropertySource(WebAppDescriptor webAppDescriptor)
    {
        Assert.isTrue(projectPropertySource == null, "can't install twice!");
        projectPropertySource = new CorePropertySource(webAppDescriptor);
        return projectPropertySource;
    }

    public abstract boolean projectSupportsAnnotations();
    
    /* (non-Javadoc)
     * @see core.IJavaTypeFinder#findType(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public final IJavaType findType(String fullyQualifiedName)
    {
        Map cache = getTypeCache();

        if (cache != null && cache.containsKey(fullyQualifiedName))
            return (IJavaType) cache.get(fullyQualifiedName);

        IJavaType result = tapestryProject.findType(fullyQualifiedName);

        if (cache != null)
            cache.put(fullyQualifiedName, result);

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaTypeFinder#isCachingJavaTypes()
     */
    public final boolean isCachingJavaTypes()
    {
        return true;
    }


}