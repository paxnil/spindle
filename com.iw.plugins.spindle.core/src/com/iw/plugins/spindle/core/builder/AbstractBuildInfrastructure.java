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
import java.util.List;
import java.util.Map;

import com.iw.plugins.spindle.core.IJavaType;
import com.iw.plugins.spindle.core.IJavaTypeFinder;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
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
    
    private static ThreadLocal TYPE_CACHE;

    // TODO this is really ugly, but I need this fast.
    public static List fDeferredActions = new ArrayList();
    
    public static Map getTypeCache()
    {
        return (Map) TYPE_CACHE.get();
    }
        
    ITapestryProject fTapestryProject;    

    IResourceWorkspaceLocation fContextRoot;

    IResourceWorkspaceLocation fClasspathRoot;

    IProblemPeristManager fProblemPersister;  

    boolean fValidateWebXML;
      
    BuildNotifier fNotifier;
    
    IBuild fBuild;

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
    
    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.IJavaTypeFinder#isCachingJavaTypes()
     */
    public final boolean isCachingJavaTypes()
    {
        return true;
    }
    
    public abstract void build(boolean requestIncremental, Map args);
    abstract void findAllTapestryArtifactsInClasspath(ArrayList found);
    abstract void findAllTapestryArtifactsInWebContext(ArrayList found);    
    abstract State getLastState();
    abstract Object getClasspathMemento();
    abstract Object copyClasspathMemento(Object memento);
    abstract void persistState(State state);
    
}