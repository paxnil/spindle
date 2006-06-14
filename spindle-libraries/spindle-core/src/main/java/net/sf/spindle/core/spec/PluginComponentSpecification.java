package net.sf.spindle.core.spec;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.extensions.IComponentTypeResourceResolver;
import net.sf.spindle.core.extensions.SpindleExtensionException;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.hivemind.Resource;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBeanSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.spec.IPropertySpecification;
import org.apache.tapestry.spec.InjectSpecification;

/**
 * Spindle aware concrete implementation of IComponentSpecification
 * 
 * @author glongman@gmail.com
 */
public class PluginComponentSpecification extends BaseSpecLocatable implements
        IComponentSpecification
{
    public static IComponentTypeResourceResolver COMPONENT_TYPE_RESOURCE_RESOLVERS;

    private String fComponentClassName;

    /**
     * Keyed on component id, value is {@link IContainedComponent}.
     */

    protected Map<String, IContainedComponent> fComponents;

    private List<PluginContainedComponent> fComponentObjects;

    /**
     * Keyed on asset name, value is {@link IAssetSpecification}.
     */

    protected Map<String, IAssetSpecification> fAssets;

    private List<PluginAssetSpecification> fAssetObjects;

    /**
     * Defines all formal parameters. Keyed on parameter name, value is
     * {@link IParameterSpecification}.
     */

    protected Map<String, IParameterSpecification> fParameters;

    protected List<PluginParameterSpecification> fParameterObjects;

    private List<String> fRequiredParameterNames;

    /**
     * Defines all helper beans. Keyed on name, value is {@link IBeanSpecification}.
     * 
     * @since 1.0.4
     */

    protected Map<String, IBeanSpecification> fBeans;

    protected List<IBeanSpecification> fBeanSpecifications;

    /**
     * The names of all reserved informal parameter names (as lower-case). This allows the page
     * loader to filter out any informal parameters during page load, rather than during render.
     * 
     * @since 1.0.5
     */

    protected Set<String> fReservedParameterNames;

    /**
     * The locations and values of all reserved parameter declarations in a document. Immutable
     * after a parse/scan episode.
     */
    protected List<PluginReservedParameterDeclaration> fReservedParameterDeclarations;

    /**
     * Is the component allowed to have a body (that is, wrap other elements?).
     */

    private boolean fAllowBody = true;

    /**
     * Is the component allow to have informal parameter specified.
     */

    private boolean fAllowInformalParameters = true;

    /**
     * The XML Public Id used when the page or component specification was read (if applicable).
     * 
     * @since 2.2
     */

    private String fPublicId;

    /**
     * Indicates that the specification is for a page, not a component.
     * 
     * @since 2.2
     */

    private boolean fPageSpecification;

    /**
     * A Map of {@link IPropertySpecification}keyed on the name of the property.
     * 
     * @since 2.4
     */

    private Map<String, IPropertySpecification> fPropertySpecifications;

    private List<IPropertySpecification> fPropertySpecificationObjects;

    private Map<String, InjectSpecification> fInjectSpecifications;

    private List<InjectSpecification> fInjectSpecificationObjects;

    /**
     * The Namespace this component belongs to
     */
    private INamespace fNamespace;

    /**
     * A List of the resource locations of all the templates for this component
     */

    private List<Resource> fTemplates;

    public PluginComponentSpecification()
    {
        super(SpecType.COMPONENT_SPEC);
    }

    /**
     * Create a new specification configured the same as the parent, but with no children info
     * 
     * @param other
     *            the spec we are copying config info from
     */
    public PluginComponentSpecification(PluginComponentSpecification other)
    {
        super(SpecType.COMPONENT_SPEC);
        fComponentClassName = other.fComponentClassName;
        fPageSpecification = other.fPageSpecification;
        fPublicId = other.fPublicId;
        setLocation(other.getLocation());
        setSpecificationLocation(other.getSpecificationLocation());
        fAllowBody = other.fAllowBody;
        fAllowInformalParameters = other.fAllowInformalParameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#addAsset(java.lang.String,
     *      org.apache.tapestry.spec.IAssetSpecification)
     */
    public void addAsset(String name, IAssetSpecification asset)
    {
        if (fAssetObjects == null)
        {
            fAssetObjects = new ArrayList<PluginAssetSpecification>();
            fAssets = new HashMap<String, IAssetSpecification>();
        }

        PluginAssetSpecification pAsset = (PluginAssetSpecification) asset;
        pAsset.setIdentifier(name);
        pAsset.setParent(this);

        fAssetObjects.add(pAsset);

        if (!fAssets.containsKey(name))
            fAssets.put(name, asset);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#addComponent(java.lang.String,
     *      org.apache.tapestry.spec.IContainedComponent)
     */
    public void addComponent(String id, IContainedComponent component)
    {
        if (fComponents == null)
        {
            fComponents = new HashMap<String, IContainedComponent>();
            fComponentObjects = new ArrayList<PluginContainedComponent>();
        }

        PluginContainedComponent pluginContained = (PluginContainedComponent) component;
        pluginContained.setParent(this);
        pluginContained.setIdentifier(id);
        fComponentObjects.add(pluginContained);

        if (!fComponents.containsKey(id))
            fComponents.put(id, component);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#addParameter(java.lang.String,
     *      org.apache.tapestry.spec.IParameterSpecification)
     */
    public void addParameter(String name, IParameterSpecification spec)
    {
        if (fParameters == null)
        {
            fParameters = new HashMap<String, IParameterSpecification>();
            fParameterObjects = new ArrayList<PluginParameterSpecification>();
        }

        PluginParameterSpecification pluginParm = (PluginParameterSpecification) spec;
        pluginParm.setParent(this);
        pluginParm.setIdentifier(name);

        fParameterObjects.add(pluginParm);

        if (!fParameters.containsKey(name))
            fParameters.put(name, spec);
    }

    public Map getParameterMap()
    {
        if (fParameters == null)
            return Collections.EMPTY_MAP;

        return fParameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getAllowBody()
     */
    public boolean getAllowBody()
    {
        return fAllowBody;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getAllowInformalParameters()
     */
    public boolean getAllowInformalParameters()
    {
        return fAllowInformalParameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getAsset(java.lang.String)
     */
    public IAssetSpecification getAsset(String name)
    {
        return get(fAssets, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getAssetNames()
     */
    public List getAssetNames()
    {
        return keys(fAssets);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponent(java.lang.String)
     */
    public IContainedComponent getComponent(String id)
    {
        return (IContainedComponent) get(fComponents, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponentClassName()
     */
    public String getComponentClassName()
    {
        return fComponentClassName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getComponentIds()
     */
    public List getComponentIds()
    {
        return keys(fComponents);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getParameter(java.lang.String)
     */
    public IParameterSpecification getParameter(String name)
    {
        return (IParameterSpecification) get(fParameters, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getParameterNames()
     */
    public List getParameterNames()
    {
        return keys(fParameters);
    }

    public List<String> getRequiredParameterNames()
    {
        if (fParameters == null)
            return Collections.emptyList();

        if (fRequiredParameterNames == null)
        {
            fRequiredParameterNames = new ArrayList<String>();
            for (Iterator iter = getParameterNames().iterator(); iter.hasNext();)
            {
                String name = (String) iter.next();
                PluginParameterSpecification parm = (PluginParameterSpecification) fParameters
                        .get(name);

                if (parm.isRequired())
                    fRequiredParameterNames.add(name);
            }

        }
        return fRequiredParameterNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#setAllowBody(boolean)
     */
    public void setAllowBody(boolean value)
    {
        fAllowBody = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#setAllowInformalParameters(boolean)
     */
    public void setAllowInformalParameters(boolean value)
    {
        fAllowInformalParameters = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#setComponentClassName(java.lang.String)
     */
    public void setComponentClassName(String value)
    {
        fComponentClassName = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#addBeanSpecification(java.lang.String,
     *      org.apache.tapestry.spec.IBeanSpecification)
     */
    public void addBeanSpecification(String name, IBeanSpecification specification)
    {
        if (fBeans == null)
        {
            fBeanSpecifications = new ArrayList<IBeanSpecification>();
            fBeans = new HashMap<String, IBeanSpecification>();
        }

        PluginBeanSpecification pluginBean = (PluginBeanSpecification) specification;
        pluginBean.setIdentifier(name);

        fBeanSpecifications.add(specification);

        if (!fBeans.containsKey(name))
            fBeans.put(name, specification);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getBeanSpecification(java.lang.String)
     */
    public IBeanSpecification getBeanSpecification(String name)
    {
        return (IBeanSpecification) get(fBeans, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getBeanNames()
     */
    public Collection getBeanNames()
    {
        return keys(fBeans);
    }

    /*
     * (non-Javadoc) scanners should not call this method directly!
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#addReservedParameterName(java.lang.String)
     */
    public void addReservedParameterName(String value)
    {
        if (fReservedParameterNames == null)
            fReservedParameterNames = new HashSet<String>();

        fReservedParameterNames.add(value);
    }

    public void addReservedParameterDeclaration(PluginReservedParameterDeclaration decl)
    {
        if (fReservedParameterDeclarations == null)
            fReservedParameterDeclarations = new ArrayList<PluginReservedParameterDeclaration>();

        fReservedParameterDeclarations.add(decl);
        String reservedName = decl.getReservedName();
        if (!TapestryCore.isNull(reservedName))
            addReservedParameterName(reservedName);
    }

    public List getReservedParameterDeclarations()
    {
        if (fReservedParameterDeclarations == null)
            return Collections.EMPTY_LIST;

        return fReservedParameterDeclarations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#isReservedParameterName(java.lang.String)
     */
    public boolean isReservedParameterName(String value)
    {
        if (fReservedParameterNames != null)
            return fReservedParameterNames.contains(value);

        return false;
    }

    public Set<String> getReservedParameterNames()
    {
        if (fReservedParameterNames == null)
            return Collections.emptySet();

        return fReservedParameterNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getPublicId()
     */
    public String getPublicId()
    {
        return fPublicId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#setPublicId(java.lang.String)
     */
    public void setPublicId(String publicId)
    {
        fPublicId = publicId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#isPageSpecification()
     */
    public boolean isPageSpecification()
    {
        return fPageSpecification;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#setPageSpecification(boolean)
     */
    public void setPageSpecification(boolean pageSpecification)
    {
        this.fPageSpecification = pageSpecification;
        // no property change firing needed. This value
        // is immutable once set
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#addPropertySpecification(org.apache.tapestry.spec.IPropertySpecification)
     */
    public void addPropertySpecification(IPropertySpecification spec)
    {
        if (fPropertySpecifications == null)
        {
            fPropertySpecifications = new HashMap<String, IPropertySpecification>();
            fPropertySpecificationObjects = new ArrayList<IPropertySpecification>();
        }

        PluginPropertySpecification pluginSpec = (PluginPropertySpecification) spec;

        pluginSpec.setParent(this);

        String name = spec.getName();

        pluginSpec.setIdentifier(name);

        if (!fPropertySpecifications.containsKey(name))
            fPropertySpecifications.put(name, spec);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IComponentSpecification#getPropertySpecificationNames()
     */
    public List<String> getPropertySpecificationNames()
    {
        return keys(fPropertySpecifications);
    }

    /*
     * (non-Javadoc)
     * 
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

    public void addTemplate(Resource location)
    {
        if (fTemplates == null)
        {
            fTemplates = new ArrayList<Resource>();
        }
        fTemplates.add(location);
    }

    public List<Resource> getTemplateLocations()
    {
        if (fTemplates == null)
        {
            return Collections.emptyList();
        }
        return fTemplates;
    }

    /**
     * @param locations
     */
    public void setTemplateLocations(ICoreResource[] locations)
    {
        if (fTemplates == null)
        {
            fTemplates = new ArrayList<Resource>();
        }
        else
        {
            fTemplates.clear();
        }
        fTemplates.addAll(Arrays.asList(locations));
    }

    public void validateSelf(IScannerValidator validator) throws ScannerException
    {

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        if (fPageSpecification && "org.apache.tapestry.html.BasePage".equals(fComponentClassName))
            return;

        if (!fPageSpecification && "org.apache.tapestry.BaseComponent".equals(fComponentClassName))
            return;

        Object type = validator.validateTypeName(
                (ICoreResource) getSpecificationLocation(),
                fComponentClassName,
                IProblem.ERROR,
                sourceInfo.getAttributeSourceLocation("class"));

        if (type == null || COMPONENT_TYPE_RESOURCE_RESOLVERS == null)
            return;

        if (COMPONENT_TYPE_RESOURCE_RESOLVERS.canResolve(type))
        {
            try
            {
                COMPONENT_TYPE_RESOURCE_RESOLVERS.doResolve((ICoreResource) this
                        .getSpecificationLocation(), this);
            }
            catch (SpindleExtensionException e)
            {
                validator.addProblem(
                        IProblem.ERROR,
                        sourceInfo.getAttributeSourceLocation("class"),
                        e.getMessage(),
                        true,
                        IProblem.NOT_QUICK_FIXABLE);
            }
        }

    }

    public void validate(IScannerValidator validator)
    {
        if (isPlaceholder())
            return; // there is no validatable stuff here!
        try
        {
            validateSelf(validator);
        }
        catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }

        if (fParameterObjects != null)
        {
            for (int i = 0; i < fParameterObjects.size(); i++)
            {

                PluginParameterSpecification element = (PluginParameterSpecification) fParameterObjects
                        .get(i);
                element.validate(this, validator);
            }
        }

        if (fComponentObjects != null)
        {
            for (int i = 0; i < fComponentObjects.size(); i++)
            {

                PluginContainedComponent element = (PluginContainedComponent) fComponentObjects
                        .get(i);
                element.validate(this, validator);
            }
        }

        if (fAssetObjects != null)
        {
            for (int i = 0; i < fAssetObjects.size(); i++)
            {

                PluginAssetSpecification element = (PluginAssetSpecification) fAssetObjects.get(i);
                element.validate(this, validator);
            }
        }

        if (fBeanSpecifications != null)
        {
            for (int i = 0; i < fBeanSpecifications.size(); i++)
            {

                PluginBeanSpecification element = (PluginBeanSpecification) fBeanSpecifications
                        .get(i);
                element.validate(this, validator);
            }
        }

        if (fPropertySpecificationObjects != null)
        {
            for (int i = 0; i < fPropertySpecificationObjects.size(); i++)
            {

                PluginPropertySpecification element = (PluginPropertySpecification) fPropertySpecificationObjects
                        .get(i);
                element.validate(this, validator);
            }
        }
    }

    public void addInjectSpecification(InjectSpecification spec)
    {
        if (fInjectSpecifications == null)
        {
            fInjectSpecifications = new HashMap<String, InjectSpecification>();
            fInjectSpecificationObjects = new ArrayList<InjectSpecification>();
        }

        PluginInjectSpecification pluginSpec = (PluginInjectSpecification) spec;

        pluginSpec.setParent(this);

        String property = spec.getProperty();

        pluginSpec.setIdentifier(property);

        if (!fInjectSpecifications.containsKey(property))
            fInjectSpecifications.put(property, spec);

    }

    public List getInjectSpecifications()
    {
        throw new UnsupportedOperationException();

    }

    public List getInjectPropertyNames()
    {
        return keys(fInjectSpecifications);
    }

    public void addParameter(IParameterSpecification arg0)
    {
        // TODO Auto-generated method stub

    }

    public Collection getRequiredParameters()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isDeprecated()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setDeprecated(boolean arg0)
    {
        // TODO Auto-generated method stub

    }

}
