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

package com.iw.plugins.spindle.core.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.tapestry.ILocation;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.runtime.Path;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.namespace.NamspaceResourceLookup;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.spec.lookup.ComponentLookup;
import com.iw.plugins.spindle.core.spec.lookup.PageLookup;

/**
 *  Resolver for a Namespace
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class NamespaceResolver
{
    private String applicationNameFromWebXML;

    private Build build;

    private ICoreNamespace resultNamespace;
    private ICoreNamespace frameworkNamespace;
    private IResourceWorkspaceLocation specLocation;
    private String namespaceId;

    private Map jwcFiles;
    private ICoreNamespace temporaryNamespace;

    // a stack of the components being resolved
    // its an error for a component to be in the stack more than 
    // once!
    private Stack componentStack;

    private boolean working;
    private boolean resolvingFramework;

    /**
     * 
     * To resolve a namespace you need:
     * 
     *  Given, the framework namespace, or null if this is the framework
     *  Given, the resource location of the library spec.
     *  Given, the servlet name from web.xml (ignored if its not an application)
     * 
     *  - parse the library spec
     *  - figure out if its a library or an application
     *  - for each <pre><library></pre> tag create and run an new NamespaceResolver
     *    and install the results in this namespace as children.
     * 
     *  - need to keep track somehow of templates and thier owners.
     * 
     *  component resolve rules (application) 
     * 
     * <ul>
     *  <li>As declared in the application specification</li>
     *  <li>*.jwc in the same folder as the application specification</li>
     *  <li>* jwc in the WEB-INF/<i>servlet-name</i> directory of the context root</li>
     *  <li>*.jwc in WEB-INF</li>
     *  <li>*.jwc in the application root (within the context root)</li>
     * </ul>
     * 
     * component resolve rules (library)
     * <ul>
     *   <li>As declared in the library specification</li>
     *   <li>*.jwc in the same folder as the library specification</li>
     * </ul>
     * 
     *  page resolve rules (application namespace):
     * 
     * <ul>        
     *  <li>As declared in the application specification</li>
     *  <li>*.page in the same folder as the application specification</li>
     *  <li>*.page page in the WEB-INF/<i>servlet-name</i> directory of the context root</li>
     *  <li>*.page in WEB-INF</li>
     *  <li>*.page in the application root (within the context root)</li>
     *  <li>*.html as a template in the application root</li>
     * </ul> 
     * 
     *  page resolve rules (ordinary library)
     * 
     *  <ul>        
     *    <li>As declared in the library specification</li>
     *    <li>*.page in the same folder as the library specification</li>
     * </ul>
     * 
     *  template lookup rules:
     * 
     * <ul>
     *  <li>If the component has a $template asset, use that</li>
     *  <li>Look for a template in the same folder as the component</li>
     *  <li>If a page in the application namespace, search in the application root</li>
     * </ul>
     * 
     */
    public NamespaceResolver(Build build)
    {
        super();
        this.build = build;
    }

    public ICoreNamespace resolveFrameworkNamespace()
    {
        IResourceWorkspaceLocation frameworkLocation =
            (IResourceWorkspaceLocation) build.tapestryBuilder.classpathRoot.getRelativeLocation(
                "/org/apache/tapestry/Framework.library");
        resolve(null, ICoreNamespace.FRAMEWORK_NAMESPACE, frameworkLocation);
        return resultNamespace;
    }

    public ICoreNamespace resolveApplicationNamespace(
        ICoreNamespace framework,
        IResourceWorkspaceLocation location,
        String appNameFromWebXML)
    {
        reset();
        this.applicationNameFromWebXML = appNameFromWebXML;
        resolve(framework, null, location);
        return resultNamespace;
    }

    public ICoreNamespace resolveLibrary(
        ICoreNamespace framework,
        String libraryId,
        IResourceWorkspaceLocation location)
    {
        reset();
        resolve(framework, libraryId, location);
        return resultNamespace;
    }

    protected ICoreNamespace resolve(ICoreNamespace framework, String namespaceId, IResourceWorkspaceLocation location)
    {
        this.frameworkNamespace = framework;
        this.namespaceId = namespaceId;
        this.specLocation = location;
        doResolve();
        return resultNamespace;
    }

    protected void reset()
    {
        componentStack.clear();
        this.frameworkNamespace = null;
        this.namespaceId = null;
        this.specLocation = null;
        this.applicationNameFromWebXML = null;
        this.resolvingFramework = false;
        this.working = false;
        this.jwcFiles = null;
    }

    protected void doResolve()
    {
        if (working)
        {
            throw new RuntimeException("can't call resolve while resolving!");
        }
        working = true;
        resultNamespace = build.createNamespace(namespaceId, specLocation);
        resultNamespace.getComponentLookup(frameworkNamespace);
        resultNamespace.getPageLookup(frameworkNamespace);
        if (applicationNameFromWebXML != null)
        {
            resultNamespace.setAppNameFromWebXML(applicationNameFromWebXML);
        }
        temporaryNamespace = new TemporaryNamespace();
        if (resultNamespace != null)
        {

            resolveChildNamespaces();
            resolveComponents();
            List definitelyNotSpeclessPages = getAllComponentTemplates();
            resolvePages(definitelyNotSpeclessPages);
        }
        working = false;

    }

    private List getAllComponentTemplates()
    {
        List result = new ArrayList();
        for (Iterator iter = resultNamespace.getComponentTypes().iterator(); iter.hasNext();)
        {
            String type = (String) iter.next();
            PluginComponentSpecification spec =
                (PluginComponentSpecification) resultNamespace.getComponentSpecification(type);
            result.addAll(spec.getTemplateLocations());
        }
        return result;
    }

    /**
     * resolve/build all the child namespaces declare in the library specification
     */
    private void resolveChildNamespaces()
    {
        ILibrarySpecification spec = resultNamespace.getSpecification();
        List ids = spec.getLibraryIds();
        if (!ids.isEmpty())
        {
            NamespaceResolver childResolver = new NamespaceResolver(build);
            for (Iterator iter = ids.iterator(); iter.hasNext();)
            {
                String libraryId = (String) iter.next();
                if (resultNamespace.getChildNamespace(libraryId) == null)
                {
                    continue;
                }
                IResourceWorkspaceLocation libLocation =
                    (IResourceWorkspaceLocation) build.tapestryBuilder.classpathRoot.getRelativeLocation(
                        spec.getLibrarySpecificationPath(libraryId));
                if (specLocation.exists())
                {
                    ICoreNamespace childNamespace =
                        childResolver.resolveLibrary(frameworkNamespace, libraryId, libLocation);
                    if (childNamespace != null)
                    {
                        resultNamespace.installChildNamespace(libraryId, childNamespace);
                    }
                }
            }
        }
    }

    /**
     * resolve all the components declared in the spec (or found in the appropriate locations!)
     */
    private void resolveComponents()
    {
        jwcFiles = getAllJWCFilesForNamespace();
        for (Iterator iter = jwcFiles.keySet().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) jwcFiles.get(name);
            resolveComponent(name, location);
        }
    }

    /**
     * resolve a single component
     * 
     * as this method is called recursively, we keep a stack recording the components being resolved.
     * If we are asked to resolve a component that is already on the stack, we throw a runtime exception
     * as this is an indication that there is a circular dependancy between components.
     */
    private IComponentSpecification resolveComponent(String name, IResourceWorkspaceLocation location)
    {
        IComponentSpecification result = resultNamespace.getComponentSpecification(name);
        if (result != null || location == null)
        {
            return result;
        }

        result = null;

        if (componentStack.contains(location))
        {
            throw new RuntimeException("poo");
        }
        componentStack.push(location);
        result = build.resolveIComponentSpecification(temporaryNamespace, location);
        resultNamespace.installComponentSpecification(name, result);
        ((PluginComponentSpecification) result).setNamespace(resultNamespace);
        componentStack.pop();
        return result;
    }

    /**
     * @return
     */
    private Map getAllJWCFilesForNamespace()
    {
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) resultNamespace.getSpecificationLocation();

        NamspaceResourceLookup lookup = new NamspaceResourceLookup();
        if (resultNamespace.isApplicationNamespace())
        {
            lookup.configure(
                (PluginApplicationSpecification) resultNamespace.getSpecification(),
                applicationNameFromWebXML);
        } else
        {
            lookup.configure((PluginLibrarySpecification) resultNamespace.getSpecification());
        }

        List jwcs = new ArrayList(Arrays.asList(lookup.find("*", false, lookup.ACCEPT_JWC)));

        Map result = new HashMap();
        ILibrarySpecification spec = resultNamespace.getSpecification();

        // pull the ones that are defined in the spec.
        for (Iterator iter = spec.getComponentTypes().iterator(); iter.hasNext();)
        {
            String type = (String) iter.next();
            IResourceLocation specLoc = location.getRelativeLocation(spec.getComponentSpecificationPath(type));
            if (jwcs.contains(specLoc))
            {
                if (!result.containsKey(type))
                {
                    result.put(type, specLoc);
                }
                //otherwise ignore the duplicate definition - the scanners will catch it!
            }
        }

        jwcs.removeAll(result.entrySet());

        // remaining typed by thier filename
        for (Iterator iter = jwcs.iterator(); iter.hasNext();)
        {
            IResourceLocation element = (IResourceLocation) iter.next();
            String type = new Path(element.getName()).removeFileExtension().toString();
            if (!result.containsKey(type))
            {
                result.put(type, element);
            } else
            {
                //this is a problem a component is being hidden by one defined in the spec, or some place else
                //in the lookup path.
                //TODO handle this error! - need to mark the resource if we can
                throw new Error("fix me!");
            }
        }
        return result;
    }

    /**
     * 
     */
    private void resolvePages(List definitelyNotSpeclessPages)
    {
        // TODO Auto-generated method stub

    }

    public class TemporaryNamespace implements ICoreNamespace
    {

        /* (non-Javadoc)
        * @see org.apache.tapestry.INamespace#constructQualifiedName(java.lang.String)
        */
        public String constructQualifiedName(String name)
        {
            return resultNamespace.constructQualifiedName(name);
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#containsComponentType(java.lang.String)
         */
        public boolean containsComponentType(String type)
        {
            return resultNamespace.containsComponentType(type);
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#containsPage(java.lang.String)
         */
        public boolean containsPage(String name)
        {
            return resultNamespace.containsPage(name);
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#deinstallChildNamespace(java.lang.String)
         */
        public INamespace deinstallChildNamespace(String id)
        {
            return resultNamespace.deinstallChildNamespace(id);
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#deinstallComponentSpecification(java.lang.String)
         */
        public IComponentSpecification deinstallComponentSpecification(String type)
        {
            return resultNamespace.deinstallComponentSpecification(type);
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#deinstallPageSpecification(java.lang.String)
         */
        public IComponentSpecification deinstallPageSpecification(String pageName)
        {
            return resultNamespace.deinstallPageSpecification(pageName);
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getChildIds()
         */
        public List getChildIds()
        {
            return resultNamespace.getChildIds();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getChildNamespace(java.lang.String)
         */
        public INamespace getChildNamespace(String id)
        {
            return resultNamespace.getChildNamespace(id);
        }

        /* 
         * Callers of this method should catch RuntimeException (for now)
         * which is an indication of a circular dependency!
         * 
         * (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getComponentSpecification(java.lang.String)
         */
        public IComponentSpecification getComponentSpecification(String type)
        {
            IComponentSpecification specification = getComponentLookup(frameworkNamespace).lookupSpecification(type);
            if (specification == null && type.indexOf(':') < 0)
            {
                if (jwcFiles.containsKey(type))
                {
                    specification = resolveComponent(type, (IResourceWorkspaceLocation) jwcFiles.get(type));
                }
            }
            return specification;
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getComponentTypes()
         */
        public List getComponentTypes()
        {
            return resultNamespace.getComponentTypes();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getExtendedId()
         */
        public String getExtendedId()
        {
            return resultNamespace.getExtendedId();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getId()
         */
        public String getId()
        {
            return resultNamespace.getId();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.ILocatable#getLocation()
         */
        public ILocation getLocation()
        {
            return resultNamespace.getLocation();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getNamespaceId()
         */
        public String getNamespaceId()
        {
            return resultNamespace.getId();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getPageNames()
         */
        public List getPageNames()
        {
            return resultNamespace.getPageNames();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getPageSpecification(java.lang.String)
         */
        public IComponentSpecification getPageSpecification(String pageName)
        {
            //TODO may not need to build it
            IComponentSpecification specification = getPageLookup(frameworkNamespace).lookupSpecification(pageName);
            return specification;
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getParentNamespace()
         */
        public INamespace getParentNamespace()
        {
            return resultNamespace.getParentNamespace();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getServiceClassName(java.lang.String)
         */
        public String getServiceClassName(String id)
        {
            return resultNamespace.getServiceClassName(id);
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getServiceNames()
         */
        public List getServiceNames()
        {
            return resultNamespace.getServiceNames();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getSpecification()
         */
        public ILibrarySpecification getSpecification()
        {
            return resultNamespace.getSpecification();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#getSpecificationLocation()
         */
        public IResourceLocation getSpecificationLocation()
        {
            return resultNamespace.getSpecificationLocation();
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#installChildNamespace(org.apache.tapestry.INamespace)
         */
        public void installChildNamespace(String id, INamespace child)
        {
            resultNamespace.installChildNamespace(id, child);
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#installComponentSpecification(java.lang.String, org.apache.tapestry.spec.IComponentSpecification)
         */
        public void installComponentSpecification(String type, IComponentSpecification spec)
        {
            resultNamespace.installComponentSpecification(type, spec);
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#installPageSpecification(java.lang.String, org.apache.tapestry.spec.IComponentSpecification)
         */
        public void installPageSpecification(String pageName, IComponentSpecification spec)
        {
            resultNamespace.installPageSpecification(pageName, spec);
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.INamespace#isApplicationNamespace()
         */
        public boolean isApplicationNamespace()
        {

            return resultNamespace.isApplicationNamespace();
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setParentNamespace(com.iw.plugins.spindle.core.namespace.ICoreNamespace)
         */
        public void setParentNamespace(ICoreNamespace parent)
        {
            resultNamespace.setParentNamespace(parent);
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getComponentLookup(com.iw.plugins.spindle.core.namespace.ICoreNamespace)
         */
        public ComponentLookup getComponentLookup(ICoreNamespace framework)
        {
            return resultNamespace.getComponentLookup(framework);
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getPageLookup(com.iw.plugins.spindle.core.namespace.ICoreNamespace)
         */
        public PageLookup getPageLookup(ICoreNamespace framework)
        {
            return resultNamespace.getPageLookup(framework);
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setAppNameFromWebXML(java.lang.String)
         */
        public void setAppNameFromWebXML(String name)
        {
            resultNamespace.setAppNameFromWebXML(name);
        }

    }
}
