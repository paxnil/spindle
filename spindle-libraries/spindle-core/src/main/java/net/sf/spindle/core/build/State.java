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
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.spec.BaseSpecification;
import net.sf.spindle.core.spec.PluginComponentSpecification;
import net.sf.spindle.core.types.IJavaType;

/**
 * An object intended to store the state of the build between builds. Normally, a builder's output
 * is the result of compiling source files, and the State is merely there to make things like
 * incremental builds possible.
 * <p>
 * This is also true for Tapestry but different in that the build state is *the* result of the
 * build!
 * <p>
 * TODO extend design so that States can be persisted!
 * <p>
 * Note: the fields used to be package protected as in the old Spindle only classes in the build
 * package accessed them during a build. A field access seemed to be faster than using a getter
 * method. Getters were added so that components in the UI could access them.
 * <p>
 * Now, it's possible that classes which live outside the build package will need to access the
 * state fields during a build. The Eclipse incremental build is one example. I'm niether here nor
 * there on this issue. Will make the fields public and leave the accessor methods alone. Bad
 * design? Of course. Mea cupla.
 * 
 * @param <E>
 *            the type a platform implementor decides use when implementing {@link ICoreResource}
 * @author glongman@gmail.com
 */
public abstract class State<E>
{

    /**
     * A static byte value that indicates the version of this state class. Expected to change rarely
     * and only between releases of the core. When the state is examined, the static value is
     * compared to the stored value in {@link #fVersion}. If they do not match then the state
     * should be thrown away and a new one created.
     * 
     * @see IIncrementalBuild#canIncrementalBuild()
     */
    public static byte VERSION = 0x0002;

    public byte fVersion = VERSION;

    /**
     * Stores the context root used in the last build. If the value in the project has changed since
     * the last build then incremental builds can not be done and a full build must occur.
     * 
     * @see IIncrementalBuild#canIncrementalBuild()
     */
    public IResourceRoot fContextRoot;

    public int fBuildNumber;

    /**
     * This is a token that is provided by a platform implementor to represent the state the project
     * classpath was in during the build where this instance was created. On subsequent builds, an
     * {@link Object#equals(Object)} check is made. if the check fails a incremental build is
     * contraindicated and a full build will occur.
     * 
     * @see IIncrementalBuild#canIncrementalBuild()
     */
    public Object classpathMemento;

    /**
     * Since we do not have a true index of all the artifacts referenced in the project (Java or
     * Tapestry), this list contains the {@link IJavaType} of every type encountered during the last build
     * whether is was found to exist or not. Incrementals builders are plaform implementation
     * specific but usualy depend on a delta of changed files. Use this list to check your delta to
     * see if any java file changes affect the Tapestry state and thus would indicate an incremental
     * build is needed. Not used in Full builds.
     * 
     * @see IIncrementalBuild#needsIncrementalBuild()
     */
    public List<IJavaType> javaTypeDependencies;

    /**
     * a subset of {@link #javaTypeDependencies} containing only the FQN's of java types that were
     * found not to exist during the last build. A shortcut device that can speed up incremental
     * build decision. ie. if your delta shows that only new Java types were added and no other
     * changed or removed then one can consult this, shorter, list.
     * 
     * @see IIncrementalBuild#needsIncrementalBuild()
     */
    public List<String> missingJavaTypes;

    /**
     * Map of platform object representing template files (in jar or out) to a component
     * specification (page or component).
     */
    public Map<E, PluginComponentSpecification> templateMap;

    /**
     * Map of platform object representing a physical 'file' (not in a jar) to any type of
     * specification object)
     */
    public Map<E, ? extends BaseSpecification> fileSpecificationMap;

    /**
     * Map of platform object representing a binary 'file' (found in a jar) to any type of
     * specification object)
     */
    public Map<E, ? extends BaseSpecification> binarySpecificationMap;

    // map containing all of the above
    private CompositeMap fSpecificationMap = new CompositeMap();

    /**
     * Template extensions are very configurable in Tapestry. In order to make an incremental build
     * decision a project delta must be consulted. The only way to know if a file in the delta is a
     * template is to compare its extension to this list
     * <p>
     * This set contains all extension for both types of resource, binary or physical
     * 
     * @see IIncrementalBuild#needsIncrementalBuild()
     */
    public Set<String> seenTemplateExtensions;

    /**
     * Subset of {@link #seenTemplateExtensions}. contains only extensions used in binary
     * namespaces.
     * 
     * @see IIncrementalBuild#needsIncrementalBuild()
     */
    public Set<String> fSeenTemplateExtensionsClasspath;

    /**
     * List of declared template extension seen during last build
     */
    public Set<String> declaredTemplateExtensions;

    /**
     * List of declared template extensions seen in binary namespaces during the last build. A
     * subset of {@link #declaredTemplateExtensions}
     */
    public Set<String> declaredTemplateExtensionsClasspath;

    /**
     * The result of parsing web.xml. Contains the location of the application spec. Used to check a
     * project delta to see if the app spec has been changed/moved etc. If it has, a full build must
     * occur
     * 
     * @see IIncrementalBuild#canIncrementalBuild()
     */
    public ServletInfo fApplicationServlet;

    // TODO why do we need this here?
    public WebAppDescriptor webAppDescriptor;

    /**
     * Map of all binary (jar) namespaces (libraries) constructed during the last build
     */
    public Map<ICoreResource, ICoreNamespace> fBinaryNamespaces = new HashMap<ICoreResource, ICoreNamespace>();

    /**
     * The primary namespace (not including Framework) found in the last build. Always the
     * application namespace at this point. Named thus as it is planned someday to allow projects
     * that contain a library (or libraries) but no application.
     */
    public ICoreNamespace primaryNamespace;

    /**
     * The resolved Framework namespace for this project. (as of the last build)
     */
    public ICoreNamespace frameworkNamespace;

    /**
     * Contains a list of platform specific objects representing files (binary or physical) which
     * are were found to be Tapestry templates in the last build but were also found to contain no
     * implicit contained components. These templates need not be reparsed during an incremental
     * build.
     */
    List<Object> fCleanTemplates;

    /**
     * Constructor for State.
     */
    protected State()
    {
        // do nothing.
    }

    /**
     * Constructor State.
     * 
     * @param builder
     */
    protected State(AbstractBuildInfrastructure infrastructure)
    {
        fContextRoot = infrastructure.contextRoot;
        fBuildNumber = 0;
    }

    public void markAsBrokenBuild()
    {
        fBuildNumber = -1;
    }

    // used by incremental builds only
    public void copyAndAdvanceBuildNumber(State<E> lastState,
            AbstractBuildInfrastructure infrastructure)
    {
        fContextRoot = lastState.fContextRoot;
        fBuildNumber = lastState.fBuildNumber + 1;
        fBinaryNamespaces = new HashMap<ICoreResource, ICoreNamespace>(lastState.fBinaryNamespaces);
        binarySpecificationMap = new HashMap<E, BaseSpecification>(
                lastState.binarySpecificationMap);
        classpathMemento = infrastructure.copyClasspathMemento(lastState.classpathMemento);
        fApplicationServlet = lastState.fApplicationServlet;
        webAppDescriptor = lastState.webAppDescriptor;
        primaryNamespace = lastState.primaryNamespace;
        frameworkNamespace = lastState.frameworkNamespace;
        declaredTemplateExtensions = lastState.declaredTemplateExtensions;
        declaredTemplateExtensionsClasspath = lastState.declaredTemplateExtensionsClasspath;
    }

    public void write(DataOutputStream out) throws IOException
    {
    }

    public static State read(DataInputStream in) throws IOException
    {
        return null;
    }

    public Map<E, PluginComponentSpecification> getTemplateMap()
    {
        return templateMap;
    }

    public Map<E, ? extends BaseSpecification> getSpecificationMap()
    {
        return fSpecificationMap;
    }

    class CompositeMap implements Map<E, BaseSpecification>
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
            return fBinaryNamespaces.containsKey(key) || fileSpecificationMap.containsKey(key);
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
        public Set<Entry<E, BaseSpecification>> entrySet()
        {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#get(java.lang.Object)
         */
        public BaseSpecification get(Object key)
        {
            BaseSpecification binary = binarySpecificationMap.get(key);
            if (binary != null)
                return binary;
            return fileSpecificationMap.get(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty()
        {
            return fBinaryNamespaces.isEmpty() && fileSpecificationMap.isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#keySet()
         */
        public Set<E> keySet()
        {
            HashSet<E> result = new HashSet<E>(binarySpecificationMap.keySet());
            result.addAll(fileSpecificationMap.keySet());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public BaseSpecification put(E key, BaseSpecification value)
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
        public BaseSpecification remove(Object key)
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

            return fBinaryNamespaces.size() + fileSpecificationMap.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#values()
         */
        public Collection<BaseSpecification> values()
        {
            throw new UnsupportedOperationException();
        }

    }
}