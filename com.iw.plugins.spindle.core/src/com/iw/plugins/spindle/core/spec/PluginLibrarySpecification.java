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
    private Map fComponents;

    private String fDescription;

    /**
     *  Map of extension name to {@link IExtensionSpecification}.
     * 
     **/

    private Map fExtensions;

    /**
     *  Map of library id to library specification path.
     * 
     **/

    private Map fLibraries;

    /**
     *  Map of page name to page specification path.
     * 
     **/

    private Map fPages;

    /**
     *  Map of service name to service class name.
     * 
     **/

    private Map fServices;

    /**
     *  Resource resolver TODO not used yet.
     * 
     **/

    private IResourceResolver fResourceResolver;

    private String fPublicId;

    /**
     *  Map of extension name to Object for instantiated extensions.
     * 
     **/

    public PluginLibrarySpecification()
    {
        super(BaseSpecification.LIBRARY_SPEC);
    }

    protected PluginLibrarySpecification(int type)
    {
        super(type);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#addExtensionSpecification(java.lang.String, org.apache.tapestry.spec.IExtensionSpecification)
     */
    public void addExtensionSpecification(String name, IExtensionSpecification extension)
    {
        if (fExtensions == null)
            fExtensions = new IIdentifiableMap(this, "extensions");

        fExtensions.put(name, extension);
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
        if (fComponents != null)
            return Collections.unmodifiableMap(fComponents);

        return Collections.EMPTY_MAP;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getComponentSpecificationPath(java.lang.String)
     */
    public String getComponentSpecificationPath(String type)
    {
        return (String) get(fComponents, type);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getComponentTypes()
     */
    public List getComponentTypes()
    {
        return keys(fComponents);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getDescription()
     */
    public String getDescription()
    {
        return fDescription;
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
        return keys(fExtensions);
    }

    public Map getExtensions()
    {
        if (fExtensions != null)
            return Collections.unmodifiableMap(fExtensions);

        return Collections.EMPTY_MAP;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getExtensionSpecification(java.lang.String)
     */
    public IExtensionSpecification getExtensionSpecification(String name)
    {
        return (IExtensionSpecification) get(fExtensions, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getExtensionSpecifications()
     */
    public Map getExtensionSpecifications()
    {
        if (fExtensions == null)
            return Collections.EMPTY_MAP;

        return Collections.unmodifiableMap(fExtensions);
    }

    public Map getLibraries()
    {
        if (fLibraries != null)
            return Collections.unmodifiableMap(fLibraries);

        return Collections.EMPTY_MAP;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getLibraryIds()
     */
    public List getLibraryIds()
    {
        return keys(fLibraries);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getLibrarySpecificationPath(java.lang.String)
     */
    public String getLibrarySpecificationPath(String id)
    {
        return (String) get(fLibraries, id);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getPageNames()
     */
    public List getPageNames()
    {
        return keys(fPages);
    }

    public Map getPages()
    {
        if (fPages != null)
            return Collections.unmodifiableMap(fPages);

        return Collections.EMPTY_MAP;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getPageSpecificationPath(java.lang.String)
     */
    public String getPageSpecificationPath(String name)
    {
        return (String) get(fPages, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getPublicId()
     */
    public String getPublicId()
    {
        return fPublicId;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getResourceResolver()
     */
    public IResourceResolver getResourceResolver()
    {
        return fResourceResolver;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getServiceClassName(java.lang.String)
     */
    public String getServiceClassName(String name)
    {
        return (String) get(fServices, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#getServiceNames()
     */
    public List getServiceNames()
    {
        return keys(fServices);
    }

    public Map getServices()
    {
        if (fServices != null)
            return Collections.unmodifiableMap(fServices);

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
        remove(fComponents, type);
    }

    public void removeExtensionSpecification(String name)
    {
        remove(fExtensions, name);
    }

    public void removeLibrarySpecificationPath(String name)
    {
        remove(fLibraries, name);
    }

    public void removePageSepcificationPath(String name)
    {
        remove(fPages, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setComponentSpecificationPath(java.lang.String, java.lang.String)
     */
    public void setComponentSpecificationPath(String type, String path)
    {
        if (fComponents == null)
            fComponents = new PropertyFiringMap(this, "components");

        fComponents.put(type, path);

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        String old = fDescription;
        fDescription = description;
        firePropertyChange("description", old, fDescription);

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setLibrarySpecificationPath(java.lang.String, java.lang.String)
     */
    public void setLibrarySpecificationPath(String id, String path)
    {
        if (fLibraries == null)
            fLibraries = new PropertyFiringMap(this, "libraries");

        fLibraries.put(id, path);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setPageSpecificationPath(java.lang.String, java.lang.String)
     */
    public void setPageSpecificationPath(String name, String path)
    {
        if (fPages == null)
            fPages = new PropertyFiringMap(this, "pages");

        fPages.put(name, path);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setPublicId(java.lang.String)
     */
    public void setPublicId(String value)
    {
        String old = fPublicId;
        fPublicId = value;
        firePropertyChange("publicId", old, fPublicId);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setResourceResolver(org.apache.tapestry.IResourceResolver)
     */
    public void setResourceResolver(IResourceResolver resolver)
    {
        this.fResourceResolver = resolver;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.ILibrarySpecification#setServiceClassName(java.lang.String, java.lang.String)
     */
    public void setServiceClassName(String name, String className)
    {
        if (fServices == null)
            fServices = new PropertyFiringMap(this, "services");

        fServices.put(name, className);
    }

}
