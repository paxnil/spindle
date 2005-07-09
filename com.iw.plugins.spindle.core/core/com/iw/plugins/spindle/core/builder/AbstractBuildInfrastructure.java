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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.engine.IPropertySource;

import com.iw.plugins.spindle.core.IJavaType;
import com.iw.plugins.spindle.core.IJavaTypeFinder;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.properties.CorePropertySource;
import com.iw.plugins.spindle.core.resources.IResourceRoot;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.IProblemPeristManager;

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

    protected static ThreadLocal TYPE_CACHE;

    protected static ThreadLocal PROPERTY_SOURCE_CACHE;

    static
    {
        TYPE_CACHE = new ThreadLocal();
        PROPERTY_SOURCE_CACHE = new ThreadLocal();
    }

    // TODO this is really ugly, but I need this fast.
    public static List fDeferredActions = new ArrayList();

    public static Map getTypeCache()
    {
        return (Map) TYPE_CACHE.get();
    }

    public static Map getPropertySourceCache()
    {
        return (Map) PROPERTY_SOURCE_CACHE.get();
    }

    ITapestryProject fTapestryProject;

    IResourceRoot fContextRoot;

    IResourceRoot fClasspathRoot;

    IProblemPeristManager fProblemPersister;

    boolean fValidateWebXML;

    BuildNotifier fNotifier;

    AbstractBuild fBuild;
   
    private CorePropertySource fProjectPropertySource;


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

        IJavaType result = fTapestryProject.findType(fullyQualifiedName);

        if (cache != null)
            cache.put(fullyQualifiedName, result);

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.IJavaTypeFinder#isCachingJavaTypes()
     */
    public final boolean isCachingJavaTypes()
    {
        return true;
    }

    public final void build(boolean requestIncremental, Map args)
    {
        TYPE_CACHE.set(new HashMap());
        PROPERTY_SOURCE_CACHE.set(new HashMap());
        try
        {
            executeBuild(requestIncremental, args);
        }
        finally
        {
            TYPE_CACHE.set(null);
            PROPERTY_SOURCE_CACHE.set(null);
        }
    }

    abstract void executeBuild(boolean requestIncremental, Map args);

    abstract void findAllTapestryArtifactsInClasspath(ArrayList found);

    abstract void findAllTapestryArtifactsInWebContext(ArrayList found);

    abstract State getLastState();

    abstract Object getClasspathMemento();

    abstract Object copyClasspathMemento(Object memento);

    abstract void persistState(State state);
    
    abstract WebXMLScanner createWebXMLScanner();

 
    public IPropertySource installBasePropertySource(WebAppDescriptor webAppDescriptor)
    {
        Assert.isTrue(fProjectPropertySource == null, "can't install twice!");
        fProjectPropertySource = new CorePropertySource(webAppDescriptor);
        return fProjectPropertySource;
    }

    public IPropertySource createPropertySource(INamespace namespace)
    {
        IPropertySource result = findPropertySource(namespace);
        if (result == null)
            result = cachePropertySource(fProjectPropertySource, namespace);

        return result;
    }

    public IPropertySource createPropertySource(PluginComponentSpecification spec)
    {
        CorePropertySource result = findPropertySource(spec);
        if (result == null)
        {
            INamespace namespace = spec.getNamespace();
            CorePropertySource nsPS = (CorePropertySource) createPropertySource(namespace);
            result = cachePropertySource(nsPS, spec);
        }
        return result;
    }

    private CorePropertySource findPropertySource(IPropertySource child)
    {
        Map map = (Map) PROPERTY_SOURCE_CACHE.get();
        return (CorePropertySource) map.get(child);
    }

    private CorePropertySource cachePropertySource(CorePropertySource parent, IPropertySource child)
    {
        Map map = (Map) PROPERTY_SOURCE_CACHE.get();
        CorePropertySource result = (CorePropertySource) map.get(child);
        if (result == null)
        {
            result = (CorePropertySource) parent.createChildPropertySource(child);
            map.put(child, result);
        }
        return result;
    }

}