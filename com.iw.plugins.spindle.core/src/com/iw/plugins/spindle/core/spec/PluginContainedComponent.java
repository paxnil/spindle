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
import java.util.Map;

import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IContainedComponent;

import com.iw.plugins.spindle.core.util.PropertyFiringMap;

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

    protected Map fBindings;

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
        return fType;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#setBinding(java.lang.String, org.apache.tapestry.spec.IBindingSpecification)
     */
    public void setBinding(String name, IBindingSpecification spec)
    {
        if (fBindings == null)
            fBindings = new PropertyFiringMap(this, "bindings");

        fBindings.put(name, spec);
    }

    public void removeBinding(String name)
    {
        remove(fBindings, name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#setType(java.lang.String)
     */
    public void setType(String value)
    {
        String old = fType;
        fType = value;
        firePropertyChange("type", old, this.fType);

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#setCopyOf(java.lang.String)
     */
    public void setCopyOf(String id)
    {
        String old = fCopyOf;
        fCopyOf = id;
        firePropertyChange("copyOf", old, this.fCopyOf);
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
        // TODO whaaaa?
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IContainedComponent#setInheritInformalParameters(boolean)
     */
    public void setInheritInformalParameters(boolean value)
    {
        // TODO whaaaa?

    }

}
