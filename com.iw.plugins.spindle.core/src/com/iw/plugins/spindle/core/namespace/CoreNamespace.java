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

package com.iw.plugins.spindle.core.namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ILocation;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.lookup.ComponentLookup;
import com.iw.plugins.spindle.core.spec.lookup.PageLookup;

/**
 *  Tapestry Core implementation of org.apache.tapestry.INamespace
 * 
 *  All pages and components must be explicity installed.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class CoreNamespace implements ICoreNamespace
{

    private ILibrarySpecification fSpecification;
    private String fId;
    private String fExtendedId;
    private INamespace fParent;
    private boolean fIsFrameworkNamespace;
    private boolean fIsApplicationNamespace;

    private ComponentLookup fComponentLookup;
    private PageLookup fPageLookup;
    private ComponentSpecificationResolver fComponentResolver;
    private PageSpecificationResolver fPageResolver;

    private String fAppNameFromWebXML;

    private Map fPages = new HashMap();

    private NamespaceResourceLookup fLookup;

    /**
     *  Map of {@link ComponentSpecification} keyed on
     *  component alias.
     * 
     **/

    private Map components = new HashMap();

    /**
     *  Map, keyed on id, of {@link INamespace}.
     * 
     **/

    private Map children = new HashMap();

    public CoreNamespace(String id, ILibrarySpecification specification)
    {
        this.fId = id;
        this.fSpecification = specification;

        fIsApplicationNamespace = (id == null && specification instanceof IApplicationSpecification);
        fIsFrameworkNamespace = FRAMEWORK_NAMESPACE.equals(id);
        if (fIsFrameworkNamespace)
        {}
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getId()
     */
    public String getId()
    {
        return fId;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getExtendedId()
     */
    public String getExtendedId()
    {
        if (fIsApplicationNamespace)
            return null;

        if (fExtendedId == null)
            fExtendedId = buildExtendedId();

        return fExtendedId;
    }

    private String buildExtendedId()
    {
        if (fParent == null)
            return fId;

        String parentId = fParent.getExtendedId();

        // If immediate child of application namespace

        if (parentId == null)
            return fId;

        return parentId + "." + fId;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getNamespaceId()
     */
    public String getNamespaceId()
    {
        if (fIsFrameworkNamespace)
            return TapestryCore.getTapestryString("Namespace.framework-namespace");

        if (fIsApplicationNamespace)
            return TapestryCore.getTapestryString("Namespace.application-namespace");

        return TapestryCore.getTapestryString("Namespace.nested-namespace", getExtendedId());
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getParentNamespace()
     */
    public INamespace getParentNamespace()
    {
        return fParent;
    }

    public void setParentNamespace(ICoreNamespace parent)
    {
        this.fParent = parent;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getChildNamespace(java.lang.String)
     */
    public INamespace getChildNamespace(String id)
    {

        if (fId != null)
        {
            if (fId.equals(id))
                return this;
        } else if (id == null)
        {
            return this;
        }

        String firstId = id;
        String nextIds = null;

        // Split the id into first and next if it is a dot separated sequence
        int index = id.indexOf('.');
        if (index >= 0)
        {
            firstId = id.substring(0, index);
            nextIds = id.substring(index + 1);
        }

        // Get the first namespace
        INamespace result = (INamespace) children.get(firstId);

        if (result == null)
            return result;

        // If the id is a dot separated sequence, recurse to find 
        // the needed namespace
        if (result != null && nextIds != null)
            result = result.getChildNamespace(nextIds);

        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getChildIds()
     */
    public List getChildIds()
    {
        return fSpecification.getLibraryIds();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getPageSpecification(java.lang.String)
     */
    public IComponentSpecification getPageSpecification(String name)
    {
        return (IComponentSpecification) fPages.get(name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#containsPage(java.lang.String)
     */
    public boolean containsPage(String name)
    {
        return fPages.containsKey(name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getPageNames()
     */
    public List getPageNames()
    {
        return new ArrayList(fPages.keySet());
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getComponentSpecification(java.lang.String)
     */
    public IComponentSpecification getComponentSpecification(String name)
    {
        return (IComponentSpecification) components.get(name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#containsComponentType(java.lang.String)
     */
    public boolean containsComponentType(String type)
    {
        return components.containsKey(type);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getComponentTypes()
     */
    public List getComponentTypes()
    {
        return new ArrayList(components.keySet());
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getServiceClassName(java.lang.String)
     */
    public String getServiceClassName(String name)
    {
        return fSpecification.getServiceClassName(name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getServiceNames()
     */
    public List getServiceNames()
    {
        return fSpecification.getServiceNames();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getSpecification()
     */
    public ILibrarySpecification getSpecification()
    {
        return fSpecification;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#constructQualifiedName(java.lang.String)
     */
    public String constructQualifiedName(String pageName)
    {
        String prefix = getExtendedId();

        if (prefix == null)
            return pageName;

        return prefix + SEPARATOR + pageName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getSpecificationLocation()
     */
    public IResourceLocation getSpecificationLocation()
    {
        return fSpecification.getSpecificationLocation();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#isApplicationNamespace()
     */
    public boolean isApplicationNamespace()
    {
        return fIsApplicationNamespace;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#installPageSpecification(java.lang.String, org.apache.tapestry.spec.IComponentSpecification)
     */
    public void installPageSpecification(String pageName, IComponentSpecification specification)
    {
        fPages.put(pageName, specification);
        ((PluginComponentSpecification) specification).setNamespace(this);
    }

    public IComponentSpecification deinstallPageSpecification(String pageName)
    {
        PluginComponentSpecification result = (PluginComponentSpecification) fPages.get(pageName);
        if (result != null)
            result.setNamespace(null);

        return result;
    }

    /** @since 3.0 **/

    public synchronized void installComponentSpecification(String type, IComponentSpecification specification)
    {
        components.put(type, specification);
        ((PluginComponentSpecification) specification).setNamespace(this);
    }

    public IComponentSpecification deinstallComponentSpecification(String type)
    {
        PluginComponentSpecification result = (PluginComponentSpecification) components.get(type);
        if (result != null)
            result.setNamespace(null);

        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.ILocatable#getLocation()
     */
    public ILocation getLocation()
    {
        if (fSpecification == null)
            return null;

        return fSpecification.getLocation();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#deinstallChildNamesspace(java.lang.String)
     */
    public INamespace deinstallChildNamespace(String id)
    {
        CoreNamespace result = (CoreNamespace) children.get(id);
        if (result != null)
            result.setParentNamespace(null);

        return result;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#installChildNamespace(org.apache.tapestry.INamespace)
     */
    public void installChildNamespace(String id, INamespace child)
    {
        children.put(id, child);
        ((ICoreNamespace) child).setParentNamespace(this);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getComponentLookup()
     */
    public ComponentLookup getComponentLookup(ICoreNamespace framework)
    {
        if (fComponentLookup == null)
        {
            fComponentLookup = new ComponentLookup();
            if (isApplicationNamespace())
            {
                fComponentLookup.configure(this, framework, fAppNameFromWebXML);
            } else
            {
                fComponentLookup.configure(this, framework);
            }
        }
        return fComponentLookup;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getPageLookup()
     */
    public PageLookup getPageLookup(ICoreNamespace framework)
    {
        if (fPageLookup == null)
        {
            fPageLookup = new PageLookup();
            if (isApplicationNamespace())
            {
                fPageLookup.configure(this, framework, fAppNameFromWebXML);
            } else
            {
                fPageLookup.configure(this, framework);
            }
        }
        return fPageLookup;
    }

    public void setAppNameFromWebXML(String name)
    {
        fAppNameFromWebXML = name;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setResourceLookup(com.iw.plugins.spindle.core.namespace.NamespaceResourceLookup)
     */
    public void setResourceLookup(NamespaceResourceLookup lookup)
    {
        this.fLookup = lookup;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getResourceLookup()
     */
    public NamespaceResourceLookup getResourceLookup()
    {
        return fLookup;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getComponentResolver()
     */
    public ComponentSpecificationResolver getComponentResolver()
    {
        return fComponentResolver;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setComponentResolver(com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver)
     */
    public void setComponentResolver(ComponentSpecificationResolver resolver)
    {
        fComponentResolver = resolver;
    }

    public String toString()
    {
        return "Namepace(" + fId + "): " + getSpecificationLocation().toString();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getPageResolver()
     */
    public PageSpecificationResolver getPageResolver()
    {
        return fPageResolver;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setPageResolver(com.iw.plugins.spindle.core.namespace.PageSpecificationResolver)
     */
    public void setPageResolver(PageSpecificationResolver resolver)
    {
        fPageResolver = resolver;
    }

}
