package net.sf.spindle.core.build;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.resources.IResourceRoot;

/**
 * An object intended to store the state of the build between builds. Normally, a builder's output
 * is the result of compiling source files, and the State is merely there to make things like
 * incremental builds possible.
 * <p>
 * This is also true for Tapestry but different in that the build state is *the* result of the build!
 * <p>
 * TODO extend design so that States can be persisted!
 * 
 * @author glongman@gmail.com
 */
public class State
{

    public static byte VERSION = 0x0002;

    public IResourceRoot fContextRoot;

    public byte fVersion = VERSION;

    public int fBuildNumber;

    public Map fBinaryNamespaces = new HashMap();

    public Object fLastKnownClasspath;

    // following are used to determine if an incremental Tapestry build is
    // required at all.

    // list of IResources to java types in the project
    public List fJavaDependencies;

    // list of fullyQualified names of types not found during a build
    public List fMissingJavaTypes;

    // map template storages to components
    public Map fTemplateMap;

    // map spec files to specification objects
    public Map fFileSpecificationMap;

    // map binary spec files to specification objects
    public Map fBinarySpecificationMap;

    // map containing all of the above
    private Map fSpecificationMap = new CompositeMap();

    // list of known template extensions (in the context)
    public Set fSeenTemplateExtensions;

    // list of known template extensions (in the classpath)
    public Set fSeenTemplateExtensionsClasspath;

    // the results of parsing web.xml
    public ServletInfo fApplicationServlet;

    public WebAppDescriptor fWebAppDescriptor;

    // the main namespace result of the last build.
    public ICoreNamespace fPrimaryNamespace;

    // the frameowrk namespace for this project
    public ICoreNamespace fFrameworkNamespace;

    // templates that do not contain implicit components and thus may not need
    // reparsing
    // during a subsequent incremental build.
    List fCleanTemplates;

    public Set fDeclatedTemplateExtensions;

    public Set fDeclaredTemplateExtensionsClasspath;

    /**
     * Constructor for State.
     */
    public State()
    {
        // do nothing.
    }

    /**
     * Constructor State.
     * 
     * @param builder
     */
    public State(AbstractBuildInfrastructure infrastructure)
    {
        fContextRoot = infrastructure.contextRoot;
        fBuildNumber = 0;
    }

    public void markAsBrokenBuild()
    {
        fBuildNumber = -1;
    }

    // used by incremental builds only
    public void copyFrom(State lastState, AbstractBuildInfrastructure infrastructure)
    {
        fContextRoot = lastState.fContextRoot;
        fBuildNumber = lastState.fBuildNumber + 1;
        fBinaryNamespaces = new HashMap(lastState.fBinaryNamespaces);
        fBinarySpecificationMap = new HashMap(lastState.fBinarySpecificationMap);
        fLastKnownClasspath = infrastructure.copyClasspathMemento(lastState.fLastKnownClasspath);
        fApplicationServlet = lastState.fApplicationServlet;
        fWebAppDescriptor = lastState.fWebAppDescriptor;
        fPrimaryNamespace = lastState.fPrimaryNamespace;
        fFrameworkNamespace = lastState.fFrameworkNamespace;
        fDeclatedTemplateExtensions = lastState.fDeclatedTemplateExtensions;
        fDeclaredTemplateExtensionsClasspath = lastState.fDeclaredTemplateExtensionsClasspath;

    }

    public void write(DataOutputStream out) throws IOException
    {
    }

    public static State read(DataInputStream in) throws IOException
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
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#clear()
         */
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key)
        {
            return fBinaryNamespaces.containsKey(key) || fFileSpecificationMap.containsKey(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value)
        {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#entrySet()
         */
        public Set entrySet()
        {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#get(java.lang.Object)
         */
        public Object get(Object key)
        {
            Object binary = fBinarySpecificationMap.get(key);
            if (binary != null)
                return binary;
            return fFileSpecificationMap.get(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty()
        {
            return fBinaryNamespaces.isEmpty() && fFileSpecificationMap.isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#keySet()
         */
        public Set keySet()
        {
            HashSet result = new HashSet(fBinarySpecificationMap.keySet());
            result.addAll(fFileSpecificationMap.keySet());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public Object put(Object key, Object value)
        {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(Map t)
        {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#remove(java.lang.Object)
         */
        public Object remove(Object key)
        {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#size()
         */
        public int size()
        {

            return fBinaryNamespaces.size() + fFileSpecificationMap.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#values()
         */
        public Collection values()
        {
            throw new UnsupportedOperationException();
        }

    }
}