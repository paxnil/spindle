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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

/**
 * Spindle aware concrete implementation of IContainedComponent
 * 
 * @author glongman@gmail.com
 */
public class PluginContainedComponent extends BasePropertyHolder implements IContainedComponent
{

    private String fType;

    private String fCopyOf;

    private boolean fInheritInformalParameters;

    protected Map<String, IBindingSpecification> fBindings;

    private List<PluginBindingSpecification> fDeclaredBindings;

    private String fPropertyName;

    /**
     * @param type
     */
    public PluginContainedComponent()
    {
        super(SpecType.CONTAINED_COMPONENT_SPEC);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#getBinding(java.lang.String)
     */
    public IBindingSpecification getBinding(String name)
    {
        return get(fBindings, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#getBindingNames()
     */
    public Collection getBindingNames()
    {
        return keys(fBindings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#getType()
     */
    public String getType()
    {
        if (fType != null && fCopyOf != null)
            return "";

        if (fCopyOf != null)
        {
            IComponentSpecification spec = (IComponentSpecification) getParent();
            IContainedComponent parentComponent = spec.getComponent(fCopyOf);

            if (parentComponent == null)
                return "";

            return parentComponent.getType();
        }

        return fType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#setBinding(java.lang.String,
     *      org.apache.tapestry.spec.IBindingSpecification)
     */
    public void setBinding(String name, IBindingSpecification spec)
    {
        if (fBindings == null)
        {
            fBindings = new HashMap<String, IBindingSpecification>();
            fDeclaredBindings = new ArrayList<PluginBindingSpecification>();
        }

        PluginBindingSpecification pluginSpec = (PluginBindingSpecification) spec;

        pluginSpec.setIdentifier(name);
        pluginSpec.setParent(this);

        fDeclaredBindings.add(pluginSpec);

        if (!fBindings.containsKey(name))
            fBindings.put(name, spec);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#setType(java.lang.String)
     */
    public void setType(String value)
    {
        fType = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#setCopyOf(java.lang.String)
     */
    public void setCopyOf(String id)
    {
        fCopyOf = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#getCopyOf()
     */
    public String getCopyOf()
    {
        return fCopyOf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#getInheritInformalParameters()
     */
    public boolean getInheritInformalParameters()
    {
        return fInheritInformalParameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IContainedComponent#setInheritInformalParameters(boolean)
     */
    public void setInheritInformalParameters(boolean value)
    {
        fInheritInformalParameters = value;
    }

    public void validate(Object parent, IScannerValidator validator)
    {

        PluginComponentSpecification spec = (PluginComponentSpecification) parent;

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();        

        try
        {
            if (fCopyOf == null)
            {
                validator.validateContainedComponent(spec, this, sourceInfo);
            }
        }
        catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
    }

    /**
     * @return List of bindings as declared in the XML source.
     */
    public List getDeclaredBindings()
    {
        if (fDeclaredBindings == null)
            return Collections.EMPTY_LIST;

        return Collections.unmodifiableList(fDeclaredBindings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.PropertyInjectable#getPropertyName()
     */
    public String getPropertyName()
    {
        return fPropertyName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.PropertyInjectable#setPropertyName(java.lang.String)
     */
    public void setPropertyName(String propertyName)
    {
        this.fPropertyName = propertyName;
    }
}