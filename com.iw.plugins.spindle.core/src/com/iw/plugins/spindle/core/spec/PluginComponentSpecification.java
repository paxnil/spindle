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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBeanSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.spec.IPropertySpecification;

import com.iw.plugins.spindle.core.util.IIdentifiableMap;
import com.iw.plugins.spindle.core.util.PropertyFiringMap;
import com.iw.plugins.spindle.core.util.PropertyFiringSet;

/**
 *  Spindle aware concrete implementation of IComponentSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginComponentSpecification extends BaseSpecLocatable implements IComponentSpecification
{
    private String componentClassName;

    /** @since 1.0.9 **/

    private String description;

    /**
     *  Keyed on component id, value is {@link IContainedComponent}.
     *
     **/

    protected Map components;

    /**
     *  Keyed on asset name, value is {@link IAssetSpecification}.
     *
     **/

    protected Map assets;

    /**
     *  Defines all formal parameters.  Keyed on parameter name, value is
     * {@link IParameterSpecification}.
     *
     **/

    protected Map parameters;

    /**
     *  Defines all helper beans.  Keyed on name, value is {@link IBeanSpecification}.
     *
     *  @since 1.0.4
     **/

    protected Map beans;

    /**
     *  The names of all reserved informal parameter names (as lower-case).  This
     *  allows the page loader to filter out any informal parameters during page load,
     *  rather than during render.
     *
     *   @since 1.0.5
     *
     **/

    protected Set reservedParameterNames;

    /**
     *  Is the component allowed to have a body (that is, wrap other elements?).
     *
     **/

    private boolean allowBody = true;

    /**
     *  Is the component allow to have informal parameter specified.
     *
     **/

    private boolean allowInformalParameters = true;

    /**
     *  The XML Public Id used when the page or component specification was read
     *  (if applicable).
     * 
     *  @since 2.2
     * 
     **/

    private String publicId;

    /**
     *  Indicates that the specification is for a page, not a component.
     * 
     *  @since 2.2
     * 
     **/

    private boolean pageSpecification;


    /**
     *  A Map of {@link IPropertySpecification} keyed on the name
     *  of the property.
     *
     *  @since 2.4
     * 
     **/

    private Map propertySpecifications;
    
    private INamespace namespace;
    
    public PluginComponentSpecification()
    {
        super(BasePropertyHolder.COMPONENT_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addAsset(java.lang.String, org.apache.tapestry.spec.IAssetSpecification)
     */
    public void addAsset(String name, IAssetSpecification asset)
    {
        if (assets == null)
        {
            assets = new IIdentifiableMap(this, "assets");
        }

        assets.put(name, asset);

    }

    public void removeAsset(String name)
    {
        remove(assets, name);
    }

    public void setAsset(String name, IAssetSpecification asset)
    {
        if (assets == null)
        {
            assets = new IIdentifiableMap(this, "assets");
        }
        assets.put(name, asset);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addComponent(java.lang.String, org.apache.tapestry.spec.IContainedComponent)
     */
    public void addComponent(String id, IContainedComponent component)
    {
        if (components == null)
        {
            components = new IIdentifiableMap(this, "components");
        }

        components.put(id, component);
    }

    public void removeComponent(String type)
    {
        remove(components, type);
    }

    public void setComponent(String id, IContainedComponent component)
    {
        if (components == null)
        {
            components = new IIdentifiableMap(this, "components");
        }

        components.put(id, component);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addParameter(java.lang.String, org.apache.tapestry.spec.IParameterSpecification)
     */
    public void addParameter(String name, IParameterSpecification spec)
    {
        if (parameters == null)
        {
            parameters = new IIdentifiableMap(this, "parameters");
        }

        parameters.put(name, spec);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getAllowBody()
     */
    public boolean getAllowBody()
    {
        return allowBody;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getAllowInformalParameters()
     */
    public boolean getAllowInformalParameters()
    {
        return allowInformalParameters;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getAsset(java.lang.String)
     */
    public IAssetSpecification getAsset(String name)
    {
        return (IAssetSpecification)get(assets, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getAssetNames()
     */
    public List getAssetNames()
    {        
        return keys(assets);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponent(java.lang.String)
     */
    public IContainedComponent getComponent(String id)
    {       
        return (IContainedComponent)keys(components);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponentClassName()
     */
    public String getComponentClassName()
    {
        return componentClassName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponentIds()
     */
    public List getComponentIds()
    {        
        return keys(components);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getParameter(java.lang.String)
     */
    public IParameterSpecification getParameter(String name)
    {        
        return (IParameterSpecification)get(parameters,name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getParameterNames()
     */
    public List getParameterNames()
    {
        return keys(parameters);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setAllowBody(boolean)
     */
    public void setAllowBody(boolean value)
    {
        boolean old = allowBody;
        allowBody = value;
        firePropertyChange("allowBody", old, allowBody);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setAllowInformalParameters(boolean)
     */
    public void setAllowInformalParameters(boolean value)
    {
        boolean old = allowInformalParameters;
        allowInformalParameters = value;
        firePropertyChange("allowInformalParameters", old, allowInformalParameters);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setComponentClassName(java.lang.String)
     */
    public void setComponentClassName(String value)
    {
        String old = componentClassName;
        componentClassName = value;
        firePropertyChange("componentClassName", old, componentClassName);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addBeanSpecification(java.lang.String, org.apache.tapestry.spec.IBeanSpecification)
     */
    public void addBeanSpecification(String name, IBeanSpecification specification)
    {
        if (beans == null)
        {
            beans = new IIdentifiableMap(this, "beans");
        }

        parameters.put(name, specification);
    }
    
    public void removeBeanSpecification(String name) {
        remove(beans, name);
    }
    
    public void setBeanSpecification(String name, IBeanSpecification specification)
    {
        if (beans == null)
        {
            beans = new IIdentifiableMap(this, "beans");
        }

        parameters.put(name, specification);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getBeanSpecification(java.lang.String)
     */
    public IBeanSpecification getBeanSpecification(String name)
    {
        return (IBeanSpecification)get(beans, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getBeanNames()
     */
    public Collection getBeanNames()
    {
        return keys(beans);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addReservedParameterName(java.lang.String)
     */
    public void addReservedParameterName(String value)
    {
        if (reservedParameterNames == null)
        {
            reservedParameterNames = new PropertyFiringSet(this, "reservedParameters");
        }

        reservedParameterNames.add(value);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#isReservedParameterName(java.lang.String)
     */
    public boolean isReservedParameterName(String value)
    {
        if (reservedParameterNames != null) {
            return reservedParameterNames.contains(value);
        }
        return false;
    }
    
    public boolean removeReservedParameterName(String value) {
        if (reservedParameterNames != null) {
            return remove(reservedParameterNames, value);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getDescription()
     */
    public String getDescription()
    {        
        return description;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        String old = this.description;
        this.description = description;
        firePropertyChange("description", old, this.description);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getPublicId()
     */
    public String getPublicId()
    {
        return publicId;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setPublicId(java.lang.String)
     */
    public void setPublicId(String publicId)
    {
        String old = this.publicId;
        this.publicId = publicId;
        firePropertyChange("publicId", old, this.publicId);

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#isPageSpecification()
     */
    public boolean isPageSpecification()
    {
        return pageSpecification;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setPageSpecification(boolean)
     */
    public void setPageSpecification(boolean pageSpecification)
    {
        this.pageSpecification = pageSpecification;
        //no property change firing needed. This valiue
        //is immutable once set

    }

  
    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addPropertySpecification(org.apache.tapestry.spec.IPropertySpecification)
     */
    public void addPropertySpecification(IPropertySpecification spec)
    {
        if (propertySpecifications == null) {
            propertySpecifications = new PropertyFiringMap(this, "tapestryProperties");
        }
        
        String name = spec.getName();
        
        propertySpecifications.put(name, spec);
    }
    
    public void removePropertSpecification(String name){
        remove(propertySpecifications, name);
    }
    
    public void setPropertySpecification(IPropertySpecification spec)
    {
        if (propertySpecifications == null) {
            propertySpecifications = new PropertyFiringMap(this, "tapestryProperties");
        }
        
        String name = spec.getName();
        
        propertySpecifications.put(name, spec);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getPropertySpecificationNames()
     */
    public List getPropertySpecificationNames()
    {
        
        return keys(propertySpecifications);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getPropertySpecification(java.lang.String)
     */
    public IPropertySpecification getPropertySpecification(String name)
    {       
        return (IPropertySpecification)get(propertySpecifications, name);
    }

    
    public INamespace getNamespace()
    {
        return namespace;
    }

    
    public void setNamespace(INamespace namespace)
    {
        this.namespace = namespace;
    }
    
    public List getTemplateLocations() {
        //TODO implement getTemplateLocations()
        return Collections.EMPTY_LIST;
    }

}
