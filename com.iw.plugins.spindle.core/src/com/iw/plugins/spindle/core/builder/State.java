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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IClasspathEntry;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 * An object intended to store the state of the build between builds.
 * Normally, a builder's output is the result of compiling source files, and the State
 * is merely there to make things like incremental builds possible.
 * 
 * This is true for Tapestry but different in that the build state is *the* result
 * of the build!
 * 
 * TODO extend design so that States can be persisted!
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class State
{

    public static byte VERSION = 0x0001;

    String fProjectName;
    IResourceWorkspaceLocation fContextRoot;
    String fLibraryLocation;
    byte fVersion = VERSION;
    int fBuildNumber;
    Map fBinaryNamespaces = new HashMap();
    IClasspathEntry[] fLastKnownClasspath;

    // following are used to determine if an incremental Tapestry build is required at all.

    // list of IResources to java types in the project
    List fJavaDependencies;

    // list of fullyQualified names of types not found during a build
    List fMissingJavaTypes;

    // map template storages to components
    Map fTemplateMap;

    // map spec files to specification objects
    Map fFileSpecificationMap;
    // map binary spec files to specification objects
    Map fBinarySpecificationMap;
    // map containing all of the above
    private Map fSpecificationMap = new CompositeMap();

    //  list of known template extensions
    List fSeenTemplateExtensions;

    // the results of parsing web.xml
    ServletInfo fApplicationServlet;

    // the main namespace result of the last build.
    ICoreNamespace fPrimaryNamespace;

    // the frameowrk namespace for this project
    ICoreNamespace fFrameworkNamespace;

    //templates that do not contain implicit components and thus may not need reparsing
    //during a sunsequent incremental build.
    List fCleanTemplates;

    /**
     * Constructor for State.
     */
    State()
    {
        //do nothing.
    }

    /**
     * Constructor State.
     * @param builder
     */
    State(TapestryBuilder builder)
    {
        fProjectName = builder.getProject().getName();
        fContextRoot = builder.fTapestryProject.getWebContextLocation();
        fLibraryLocation = builder.fTapestryProject.getLibrarySpecPath();
        fBuildNumber = 0;
    }

    void markAsBrokenBuild()
    {
        fBuildNumber = -1;
    }

    void copyFrom(State lastState)
    {
        fProjectName = lastState.fProjectName;
        fContextRoot = lastState.fContextRoot;
        fLibraryLocation = lastState.fLibraryLocation;
        fBuildNumber = lastState.fBuildNumber + 1;
        fBinaryNamespaces = new HashMap(lastState.fBinaryNamespaces);
        fBinarySpecificationMap = new HashMap(lastState.fBinaryNamespaces);
        fLastKnownClasspath = new IClasspathEntry[lastState.fLastKnownClasspath.length];
        System.arraycopy(
            lastState.fLastKnownClasspath,
            0,
            fLastKnownClasspath,
            0,
            lastState.fLastKnownClasspath.length);
        fApplicationServlet = lastState.fApplicationServlet;
        fPrimaryNamespace = lastState.fPrimaryNamespace;
        fFrameworkNamespace = lastState.fFrameworkNamespace;

    }

    void write(DataOutputStream out) throws IOException
    {}

    static State read(DataInputStream in) throws IOException
    {
        return null;
    }

    public Map getTemplateMap()
    {
        return fTemplateMap;
    }

    public Map getSpecificationMap()
    {
        return fSpecificationMap;
    }

    class CompositeMap implements Map
    {
        /* (non-Javadoc)
        * @see java.util.Map#clear()
        */
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key)
        {
            return fBinaryNamespaces.containsKey(key) || fFileSpecificationMap.containsKey(key);
        }

        /* (non-Javadoc)
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value)
        {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.Map#entrySet()
         */
        public Set entrySet()
        {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.Map#get(java.lang.Object)
         */
        public Object get(Object key)
        {
            Object binary = fBinarySpecificationMap.get(key);
            if (binary != null)
                return binary;
            return fFileSpecificationMap.get(key);
        }

        /* (non-Javadoc)
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty()
        {
            return fBinaryNamespaces.isEmpty() && fFileSpecificationMap.isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.Map#keySet()
         */
        public Set keySet()
        {
            HashSet result = new HashSet(fBinarySpecificationMap.keySet());
            result.addAll(fFileSpecificationMap.keySet());
            return result;
        }

        /* (non-Javadoc)
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public Object put(Object key, Object value)
        {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(Map t)
        {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.Map#remove(java.lang.Object)
         */
        public Object remove(Object key)
        {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.Map#size()
         */
        public int size()
        {

            return fBinaryNamespaces.size() + fFileSpecificationMap.size();
        }

        /* (non-Javadoc)
         * @see java.util.Map#values()
         */
        public Collection values()
        {
            throw new UnsupportedOperationException();
        }

    }
}
