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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 *  Spindle aware concrete implementation of IContainedComponent
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginContainedComponent extends BasePropertyHolder implements IContainedComponent
{

    private String fType;

    private String fCopyOf;

    private boolean fInheritInformalParameters;

    protected Map fBindings;
    private List fDeclaredBindings;

    /**
     * @param type
     */
    public PluginContainedComponent()
    {
        super(BaseSpecification.CONTAINED_COMPONENT_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#getBinding(java.lang.String)
     */
    public IBindingSpecification getBinding(String name)
    {
        return (IBindingSpecification) get(fBindings, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#getBindingNames()
     */
    public Collection getBindingNames()
    {
        return keys(fBindings);
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#setBinding(java.lang.String, org.apache.tapestry.spec.IBindingSpecification)
     */
    public void setBinding(String name, IBindingSpecification spec)
    {
        if (fBindings == null)
        {
            fBindings = new HashMap();
            fDeclaredBindings = new ArrayList();
        }

        PluginBindingSpecification pluginSpec = (PluginBindingSpecification) spec;

        pluginSpec.setIdentifier(name);
        pluginSpec.setParent(this);

        fDeclaredBindings.add(spec);

        if (!fBindings.containsKey(name))
            fBindings.put(name, spec);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#setType(java.lang.String)
     */
    public void setType(String value)
    {
        fType = value;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#setCopyOf(java.lang.String)
     */
    public void setCopyOf(String id)
    {
        fCopyOf = id;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#getCopyOf()
     */
    public String getCopyOf()
    {
        return fCopyOf;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#getInheritInformalParameters()
     */
    public boolean getInheritInformalParameters()
    {
        return fInheritInformalParameters;
    }

    /* (non-Javadoc)
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

        String id = getIdentifier();

        try
        {
            if (fCopyOf == null)
            {
                validator.validateContainedComponent(spec, this, sourceInfo);
            }
        } catch (ScannerException e)
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

}
