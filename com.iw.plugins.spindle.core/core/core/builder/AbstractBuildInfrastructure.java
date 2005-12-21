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

import org.apache.tapestry.engine.IPropertySource;



import core.IJavaType;
import core.IJavaTypeFinder;
import core.ITapestryProject;
import core.parser.dom.IDOMModelSource;
import core.properties.CorePropertySource;
import core.resources.IResourceRoot;
import core.util.Assert;
import core.util.IProblemPeristManager;

/**
 * The Tapestry Builder, kicks off full and incremental builds.
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

    protected static ThreadLocal BUILD_CACHE;

    static
    {
        BUILD_CACHE = new ThreadLocal();
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

    protected BuildNotifier notifier;

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

    protected abstract void executeBuild(boolean requestIncremental, Map args);

    /**
     * Deep inside the build we need to find all the artifacts - mostly for the purpose of provided
     * progress indications and to detect files that would be missed by Tapestry at runtime. We look
     * for .application, .library, .jwc, .page and template files that have the specified
     * extensions.
     * <p>
     * This method searches in the classpath only.
     * 
     * @param knownTemplateExtensions
     * @param found
     *            all found artifacts are added to this list.
     */
    public abstract void findAllTapestryArtifactsInClasspath(Set knownTemplateExtensions,
            ArrayList found);

    /**
     * Deep inside the build we need to find all the artifacts - mostly for the purpose of provided
     * progress indications and to detect files that would be missed by Tapestry at runtime. We look
     * for .appliction, .library, .jwc, .page and template files that have the specified extensions.
     * <p>
     * This method searches in the web context only.
     * 
     * @param knownTemplateExtensions
     * @param found
     *            all found artifacts are added to this list.
     */
    public abstract void findAllTapestryArtifactsInWebContext(Set knownTemplateExtensions,
            ArrayList found);

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

    public abstract WebXMLScanner createWebXMLScanner();

    public IPropertySource installBasePropertySource(WebAppDescriptor webAppDescriptor)
    {
        Assert.isTrue(projectPropertySource == null, "can't install twice!");
        projectPropertySource = new CorePropertySource(webAppDescriptor);
        return projectPropertySource;
    }

    public abstract boolean projectSupportsAnnotations();

    public abstract List getAllAnnotatedComponentTypes(String packages);
    
    public abstract List getAllAnnotatedPageTypes(String packages);
}