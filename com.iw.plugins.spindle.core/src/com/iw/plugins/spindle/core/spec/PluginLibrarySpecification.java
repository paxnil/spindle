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

package com.iw.plugins.spindle.core.spec;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.IResourceResolver;
import org.apache.tapestry.spec.IExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

import com.iw.plugins.spindle.core.util.IIdentifiableMap;
import com.iw.plugins.spindle.core.util.PropertyFiringMap;

/**
 *  Spindle aware concrete implementation of ILibrarySpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginLibrarySpecification extends BaseSpecLocatable implements ILibrarySpecification
{

    /**
     *  Map of component alias to component specification path.
     * 
     **/
    private Map components;

    private String description;
    
    /**
     *  Map of extension name to {@link IExtensionSpecification}.
     * 
     **/

    private Map extensions;

    /**
     *  Map of library id to library specification path.
     * 
     **/

    private Map libraries;

    /**
     *  Map of page name to page specification path.
     * 
     **/

    private Map pages;

    private IResourceResolver resolver;

    /**
     *  Map of service name to service class name.
     * 
     **/

    private Map services;

    /**
     *  Resource resolver TODO not used yet.
     * 
     **/

    private String publicId;

    /**
     *  Map of extension name to Object for instantiated extensions.
     * 
     **/

    public PluginLibrarySpecification()
    {
        super(BaseSpecification.LIBRARY_SPEC);
    }
    
    protected  PluginLibrarySpecification(int type)
    {
        super(type);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#addExtensionSpecification(java.lang.String, org.apache.tapestry.spec.IExtensionSpecification)
     */
    public void addExtensionSpecification(String name, IExtensionSpecification extension)
    {
        if (extensions == null)
        {
            extensions = new IIdentifiableMap(this, "extensions");
        }

        extensions.put(name, extension);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#checkExtension(java.lang.String)
     */
    public boolean checkExtension(String name)
    {
        // TODO not used
        return true;
    }

    public Map getComponents()
    {
        if (components != null)
        {
            return Collections.unmodifiableMap(components);
        }
        return Collections.EMPTY_MAP;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getComponentSpecificationPath(java.lang.String)
     */
    public String getComponentSpecificationPath(String type)
    {
        return (String) get(components, type);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getComponentTypes()
     */
    public List getComponentTypes()
    {
        return keys(components);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getExtension(java.lang.String)
     */
    public Object getExtension(String name)
    {
        // TODO not used
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getExtension(java.lang.String, java.lang.Class)
     */
    public Object getExtension(String name, Class typeConstraint)
    {
        // TODO not used
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getExtensionNames()
     */
    public List getExtensionNames()
    {
        return keys(extensions);
    }

    public Map getExtensions()
    {
        if (extensions != null)
        {
            return Collections.unmodifiableMap(extensions);
        }
        return Collections.EMPTY_MAP;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getExtensionSpecification(java.lang.String)
     */
    public IExtensionSpecification getExtensionSpecification(String name)
    {
        return (IExtensionSpecification) get(extensions, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getExtensionSpecifications()
     */
    public Map getExtensionSpecifications()
    {
        if (extensions == null)
            return Collections.EMPTY_MAP;

        return Collections.unmodifiableMap(extensions);
    }

    public Map getLibraries()
    {
        if (libraries != null)
        {
            return Collections.unmodifiableMap(libraries);
        }
        return Collections.EMPTY_MAP;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getLibraryIds()
     */
    public List getLibraryIds()
    {
        return keys(libraries);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getLibrarySpecificationPath(java.lang.String)
     */
    public String getLibrarySpecificationPath(String id)
    {
        return (String) get(libraries, id);
    }

   
    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getPageNames()
     */
    public List getPageNames()
    {
        return keys(pages);
    }

    public Map getPages()
    {
        if (pages != null)
        {
            return Collections.unmodifiableMap(pages);
        }
        return Collections.EMPTY_MAP;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getPageSpecificationPath(java.lang.String)
     */
    public String getPageSpecificationPath(String name)
    {
        return (String) get(pages, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getPublicId()
     */
    public String getPublicId()
    {
        return publicId;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getResourceResolver()
     */
    public IResourceResolver getResourceResolver()
    {
        return resolver;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getServiceClassName(java.lang.String)
     */
    public String getServiceClassName(String name)
    {
        return (String) get(services, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getServiceNames()
     */
    public List getServiceNames()
    {
        return keys(services);
    }

    public Map getServices()
    {
        if (services != null)
        {
            return Collections.unmodifiableMap(services);
        }
        return Collections.EMPTY_MAP;
    }

   

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#instantiateImmediateExtensions()
     */
    public void instantiateImmediateExtensions()
    {
        // TODO not used
    }

    public void removeComponentSpecificationPath(String type)
    {
        remove(components, type);
    }

    public void removeExtensionSpecification(String name)
    {
        remove(extensions, name);
    }

    public void removeLibrarySpecificationPath(String name)
    {
        remove(libraries, name);
    }

    public void removePageSepcificationPath(String name)
    {
        remove(pages, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setComponentSpecificationPath(java.lang.String, java.lang.String)
     */
    public void setComponentSpecificationPath(String type, String path)
    {
        if (components == null)
        {
            components = new PropertyFiringMap(this, "components");
        }

        components.put(type, path);

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        this.description = description;

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setLibrarySpecificationPath(java.lang.String, java.lang.String)
     */
    public void setLibrarySpecificationPath(String id, String path)
    {
        if (libraries == null)
        {
            libraries = new PropertyFiringMap(this, "libraries");
        }

        libraries.put(id, path);
    }

  
    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setPageSpecificationPath(java.lang.String, java.lang.String)
     */
    public void setPageSpecificationPath(String name, String path)
    {
        if (pages == null)
        {
            pages = new PropertyFiringMap(this, "pages");
        }

        pages.put(name, path);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setPublicId(java.lang.String)
     */
    public void setPublicId(String value)
    {
        publicId = value;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setResourceResolver(org.apache.tapestry.IResourceResolver)
     */
    public void setResourceResolver(IResourceResolver resolver)
    {
        this.resolver = resolver;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setServiceClassName(java.lang.String, java.lang.String)
     */
    public void setServiceClassName(String name, String className)
    {
        if (services == null)
        {
            services = new PropertyFiringMap(this, "services");
        }

        services.put(name, className);
    }

   
}
