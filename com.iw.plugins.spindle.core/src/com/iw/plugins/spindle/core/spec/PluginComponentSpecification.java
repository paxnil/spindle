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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBeanSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.spec.IPropertySpecification;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.IIdentifiableMap;
import com.iw.plugins.spindle.core.util.PropertyFiringList;
import com.iw.plugins.spindle.core.util.PropertyFiringSet;

/**
 *  Spindle aware concrete implementation of IComponentSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginComponentSpecification extends BaseSpecLocatable implements IComponentSpecification
{
    private String fComponentClassName;

    /**
     *  Keyed on component id, value is {@link IContainedComponent}.
     *
     **/

    protected Map fComponents;

    /**
     *  Keyed on asset name, value is {@link IAssetSpecification}.
     *
     **/

    protected Map fAssets;

    /**
     *  Defines all formal parameters.  Keyed on parameter name, value is
     * {@link IParameterSpecification}.
     *
     **/

    protected Map fParameters;

    /**
     *  Defines all helper beans.  Keyed on name, value is {@link IBeanSpecification}.
     *
     *  @since 1.0.4
     **/

    protected Map fBeans;

    /**
     *  The names of all reserved informal parameter names (as lower-case).  This
     *  allows the page loader to filter out any informal parameters during page load,
     *  rather than during render.
     *
     *   @since 1.0.5
     *
     **/

    protected Set fReservedParameterNames;

    /**
     *  The locations and values of all reserved parameter declarations in a document.
     *  Immutable after a parse/scan episode.
     */
    protected List fReservedParameterDeclarations;

    /**
     *  Is the component allowed to have a body (that is, wrap other elements?).
     *
     **/

    private boolean fAllowBody = true;

    /**
     *  Is the component allow to have informal parameter specified.
     *
     **/

    private boolean fAllowInformalParameters = true;

    /**
     *  The XML Public Id used when the page or component specification was read
     *  (if applicable).
     * 
     *  @since 2.2
     * 
     **/

    private String fPublicId;

    /**
     *  Indicates that the specification is for a page, not a component.
     * 
     *  @since 2.2
     * 
     **/

    private boolean fPageSpecification;

    /**
     *  A Map of {@link IPropertySpecification} keyed on the name
     *  of the property.
     *
     *  @since 2.4
     * 
     **/

    private Map fPropertySpecifications;

    /**
     * The Namespace this component belongs to
     */
    private INamespace fNamespace;

    /**
     * A List of the resource locations of all the templates
     * for this component    
     */

    private List fTemplates;

    public PluginComponentSpecification()
    {
        super(BasePropertyHolder.COMPONENT_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addAsset(java.lang.String, org.apache.tapestry.spec.IAssetSpecification)
     */
    public void addAsset(String name, IAssetSpecification asset)
    {
        if (fAssets == null)
            fAssets = new IIdentifiableMap(this, "assets");

        fAssets.put(name, asset);

    }

    public void removeAsset(String name)
    {
        remove(fAssets, name);
    }

    public void setAsset(String name, IAssetSpecification asset)
    {
        if (fAssets == null)
            fAssets = new IIdentifiableMap(this, "assets");

        fAssets.put(name, asset);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addComponent(java.lang.String, org.apache.tapestry.spec.IContainedComponent)
     */
    public void addComponent(String id, IContainedComponent component)
    {
        if (fComponents == null)
            fComponents = new IIdentifiableMap(this, "components");

        fComponents.put(id, component);
    }

    public void removeComponent(String type)
    {
        remove(fComponents, type);
    }

    public void setComponent(String id, IContainedComponent component)
    {
        if (fComponents == null)
            fComponents = new IIdentifiableMap(this, "components");

        fComponents.put(id, component);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addParameter(java.lang.String, org.apache.tapestry.spec.IParameterSpecification)
     */
    public void addParameter(String name, IParameterSpecification spec)
    {
        if (fParameters == null)
            fParameters = new IIdentifiableMap(this, "parameters");

        fParameters.put(name, spec);
    }

    public Map getParameterMap()
    {
        if (fParameters == null)
            return Collections.EMPTY_MAP;

        return fParameters;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getAllowBody()
     */
    public boolean getAllowBody()
    {
        return fAllowBody;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getAllowInformalParameters()
     */
    public boolean getAllowInformalParameters()
    {
        return fAllowInformalParameters;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getAsset(java.lang.String)
     */
    public IAssetSpecification getAsset(String name)
    {
        return (IAssetSpecification) get(fAssets, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getAssetNames()
     */
    public List getAssetNames()
    {
        return keys(fAssets);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponent(java.lang.String)
     */
    public IContainedComponent getComponent(String id)
    {
        return (IContainedComponent) get(fComponents, id);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponentClassName()
     */
    public String getComponentClassName()
    {
        return fComponentClassName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponentIds()
     */
    public List getComponentIds()
    {
        return keys(fComponents);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getParameter(java.lang.String)
     */
    public IParameterSpecification getParameter(String name)
    {
        return (IParameterSpecification) get(fParameters, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getParameterNames()
     */
    public List getParameterNames()
    {
        return keys(fParameters);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setAllowBody(boolean)
     */
    public void setAllowBody(boolean value)
    {
        boolean old = fAllowBody;
        fAllowBody = value;
        firePropertyChange("allowBody", old, fAllowBody);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setAllowInformalParameters(boolean)
     */
    public void setAllowInformalParameters(boolean value)
    {
        boolean old = fAllowInformalParameters;
        fAllowInformalParameters = value;
        firePropertyChange("allowInformalParameters", old, fAllowInformalParameters);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setComponentClassName(java.lang.String)
     */
    public void setComponentClassName(String value)
    {
        String old = fComponentClassName;
        fComponentClassName = value;
        firePropertyChange("componentClassName", old, fComponentClassName);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addBeanSpecification(java.lang.String, org.apache.tapestry.spec.IBeanSpecification)
     */
    public void addBeanSpecification(String name, IBeanSpecification specification)
    {
        if (fBeans == null)
            fBeans = new IIdentifiableMap(this, "beans");

        fBeans.put(name, specification);
    }

    public void removeBeanSpecification(String name)
    {
        remove(fBeans, name);
    }

    public void setBeanSpecification(String name, IBeanSpecification specification)
    {
        if (fBeans == null)
            fBeans = new IIdentifiableMap(this, "beans");

        fParameters.put(name, specification);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getBeanSpecification(java.lang.String)
     */
    public IBeanSpecification getBeanSpecification(String name)
    {
        return (IBeanSpecification) get(fBeans, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getBeanNames()
     */
    public Collection getBeanNames()
    {
        return keys(fBeans);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addReservedParameterName(java.lang.String)
     */
    public void addReservedParameterName(String value)
    {
        if (fReservedParameterNames == null)
            fReservedParameterNames = new PropertyFiringSet(this, "reservedParameters");

        fReservedParameterNames.add(value);
    }

    public void addReservedParameterDeclaration(PluginReservedParameterDeclaration decl)
    {
        if (fReservedParameterDeclarations == null)
            fReservedParameterDeclarations = new ArrayList();
        fReservedParameterDeclarations.add(decl);
    }

    public List getReservedParameterDeclarations()
    {
        if (fReservedParameterDeclarations == null)
            return Collections.EMPTY_LIST;

        return fReservedParameterDeclarations;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#isReservedParameterName(java.lang.String)
     */
    public boolean isReservedParameterName(String value)
    {
        if (fReservedParameterNames != null)
            return fReservedParameterNames.contains(value);

        return false;
    }

    public boolean removeReservedParameterName(String value)
    {
        if (fReservedParameterNames != null)
            return remove(fReservedParameterNames, value);

        return false;
    }

    public Set getReservedParameterNames()
    {
        if (fReservedParameterNames == null)
            return Collections.EMPTY_SET;

        return fReservedParameterNames;
    } 

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getPublicId()
     */
    public String getPublicId()
    {
        return fPublicId;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setPublicId(java.lang.String)
     */
    public void setPublicId(String publicId)
    {
        String old = fPublicId;
        fPublicId = publicId;
        firePropertyChange("publicId", old, this.fPublicId);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#isPageSpecification()
     */
    public boolean isPageSpecification()
    {
        return fPageSpecification;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#setPageSpecification(boolean)
     */
    public void setPageSpecification(boolean pageSpecification)
    {
        this.fPageSpecification = pageSpecification;
        //no property change firing needed. This value
        //is immutable once set
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#addPropertySpecification(org.apache.tapestry.spec.IPropertySpecification)
     */
    public void addPropertySpecification(IPropertySpecification spec)
    {
        if (fPropertySpecifications == null)
            fPropertySpecifications = new IIdentifiableMap(this, "tapestryProperties");

        String name = spec.getName();

        fPropertySpecifications.put(name, spec);
    }

    public void removePropertSpecification(String name)
    {
        remove(fPropertySpecifications, name);
    }

    public void setPropertySpecification(IPropertySpecification spec)
    {
        if (fPropertySpecifications == null)
            fPropertySpecifications = new IIdentifiableMap(this, "tapestryProperties");

        String name = spec.getName();

        fPropertySpecifications.put(name, spec);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getPropertySpecificationNames()
     */
    public List getPropertySpecificationNames()
    {
        return keys(fPropertySpecifications);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IComponentSpecification#getPropertySpecification(java.lang.String)
     */
    public IPropertySpecification getPropertySpecification(String name)
    {
        return (IPropertySpecification) get(fPropertySpecifications, name);
    }

    public INamespace getNamespace()
    {
        return fNamespace;
    }

    public void setNamespace(INamespace namespace)
    {
        this.fNamespace = namespace;
    }

    public void addTemplate(IResourceLocation location)
    {
        if (fTemplates == null)
        {
            fTemplates = new PropertyFiringList(this, "templates");
        }
        fTemplates.add(location);
    }

    public List getTemplateLocations()
    {
        if (fTemplates == null)
        {
            return Collections.EMPTY_LIST;
        }
        return fTemplates;
    }

    /**
     * @param locations
     */
    public void setTemplateLocations(IResourceWorkspaceLocation[] locations)
    {
        if (fTemplates == null)
        {
            fTemplates = new ArrayList();
        } else
        {
            fTemplates.clear();
        }
        fTemplates.addAll(Arrays.asList(locations));
    }

}
