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

import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBindingSpecification;

/**
 *  Spindle aware concrete implementation of IBindingSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginBindingSpecfication extends DescribableSpecification implements IBindingSpecification
{

    private BindingType fBindingType;
    private String fValue;

    public PluginBindingSpecfication()
    {
        super(BaseSpecification.BINDING_SPEC);
    }

    protected PluginBindingSpecfication(int type)
    {
        super(type);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBindingSpecification#getType()
     */
    public BindingType getType()
    {
        return fBindingType;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBindingSpecification#getValue()
     */
    public String getValue()
    {
        return fValue;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBindingSpecification#setType(org.apache.tapestry.spec.BindingType)
     */
    public void setType(BindingType type)
    {
        BindingType old = fBindingType;
        fBindingType = type;
        firePropertyChange("bindingType", old, this.fBindingType);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBindingSpecification#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
        String old = fValue;
        fValue = value;
        firePropertyChange("value", old, this.fValue);
    }

   
}
