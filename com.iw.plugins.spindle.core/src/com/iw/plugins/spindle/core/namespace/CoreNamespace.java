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
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;

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

    private ILibrarySpecification _specification;
    private String _id;
    private String _extendedId;
    private INamespace parent;
    private boolean _frameworkNamespace;
    private boolean _applicationNamespace;

    private Map _pages = new HashMap();

    /**
     *  Map of {@link ComponentSpecification} keyed on
     *  component alias.
     * 
     **/

    private Map _components = new HashMap();

    /**
     *  Map, keyed on id, of {@link INamespace}.
     * 
     **/

    private Map _children = new HashMap();

    public CoreNamespace(String id, ILibrarySpecification specification)
    {
        _id = id;        
        _specification = specification;

        _applicationNamespace = (_id == null);
        _frameworkNamespace = FRAMEWORK_NAMESPACE.equals(_id);
    }
    
    

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getId()
     */
    public String getId()
    {
        return _id;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getExtendedId()
     */
    public String getExtendedId()
    {
        if (_applicationNamespace)
        {
            return null;
        }

        if (_extendedId == null)
        {
            _extendedId = buildExtendedId();
        }

        return _extendedId;
    }

    private String buildExtendedId()
    {
        if (parent == null)
        {
            return _id;
        }

        String parentId = parent.getExtendedId();

        // If immediate child of application namespace

        if (parentId == null)
        {
            return _id;
        }

        return parentId + "." + _id;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getNamespaceId()
     */
    public String getNamespaceId()
    {
        if (_frameworkNamespace)
        {
            return Tapestry.getString("Namespace.framework-namespace");
        }

        if (_applicationNamespace)
        {
            return Tapestry.getString("Namespace.application-namespace");
        }

        return Tapestry.getString("Namespace.nested-namespace", getExtendedId());
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getParentNamespace()
     */
    public INamespace getParentNamespace()
    {
        return parent;
    }
    
    public void setParentNamespace(ICoreNamespace parent){
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getChildNamespace(java.lang.String)
     */
    public INamespace getChildNamespace(String id)
    {
        return (INamespace) _children.get(id);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getChildIds()
     */
    public List getChildIds()
    {
        return _specification.getLibraryIds();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getPageSpecification(java.lang.String)
     */
    public IComponentSpecification getPageSpecification(String name)
    {
        return (IComponentSpecification) _pages.get(name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#containsPage(java.lang.String)
     */
    public boolean containsPage(String name)
    {
        return _pages.containsKey(name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getPageNames()
     */
    public List getPageNames()
    {
        return new ArrayList(_pages.keySet());
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getComponentSpecification(java.lang.String)
     */
    public IComponentSpecification getComponentSpecification(String name)
    {
        return (IComponentSpecification) _components.get(name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#containsComponentType(java.lang.String)
     */
    public boolean containsComponentType(String type)
    {
        return _components.containsKey(type);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getComponentTypes()
     */
    public List getComponentTypes()
    {
        return new ArrayList(_components.keySet());
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getServiceClassName(java.lang.String)
     */
    public String getServiceClassName(String name)
    {
        return _specification.getServiceClassName(name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getServiceNames()
     */
    public List getServiceNames()
    {
        return _specification.getServiceNames();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getSpecification()
     */
    public ILibrarySpecification getSpecification()
    {
        return _specification;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#constructQualifiedName(java.lang.String)
     */
    public String constructQualifiedName(String pageName)
    {
        String prefix = getExtendedId();

        if (prefix == null)
        {
            return pageName;
        }

        return prefix + SEPARATOR + pageName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getSpecificationLocation()
     */
    public IResourceLocation getSpecificationLocation()
    {
        return _specification.getSpecificationLocation();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#isApplicationNamespace()
     */
    public boolean isApplicationNamespace()
    {
        return _applicationNamespace;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#installPageSpecification(java.lang.String, org.apache.tapestry.spec.IComponentSpecification)
     */
    public void installPageSpecification(String pageName, IComponentSpecification specification)
    {
        _pages.put(pageName, specification);
        ((PluginComponentSpecification) specification).setNamespace(this);
    }

    public IComponentSpecification deinstallPageSpecification(String pageName)
    {
        PluginComponentSpecification result = (PluginComponentSpecification) _pages.get(pageName);
        if (result != null)
        {
            result.setNamespace(null);
        }
        return result;
    }

    /** @since 3.0 **/

    public synchronized void installComponentSpecification(String type, IComponentSpecification specification)
    {
        _components.put(type, specification);
        ((PluginComponentSpecification) specification).setNamespace(this);
    }

    public IComponentSpecification deinstallComponentSpecification(String type)
    {
        PluginComponentSpecification result = (PluginComponentSpecification) _components.get(type);
        if (result != null)
        {
            result.setNamespace(null);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.ILocatable#getLocation()
     */
    public ILocation getLocation()
    {
        if (_specification == null)
            return null;

        return _specification.getLocation();
    }

   

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#deinstallChildNamesspace(java.lang.String)
     */
    public INamespace deinstallChildNamespace(String id)
    {
       CoreNamespace result = (CoreNamespace)_children.get(id);
       if (result != null) {
           result.setParentNamespace(null);
       }
       return result;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#installChildNamespace(org.apache.tapestry.INamespace)
     */
    public void installChildNamespace(INamespace child)
    {
        // TODO Auto-generated method stub

    }

}
