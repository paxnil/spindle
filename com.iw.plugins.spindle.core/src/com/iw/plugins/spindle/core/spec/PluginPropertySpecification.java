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

import org.apache.tapestry.spec.IPropertySpecification;

/**
 *  Spindle aware concrete implementation of IPropertySpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginPropertySpecification extends BaseSpecification implements IPropertySpecification
{

    private String _name;
    private String _type = "java.lang.Object";
    private boolean _persistent;
    private String _initialValue;

    public PluginPropertySpecification()
    {
        super(BaseSpecification.PROPERTY_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#getInitialValue()
     */
    public String getInitialValue()
    {
        return _initialValue;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#getName()
     */
    public String getName()
    {
        return _name;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#isPersistent()
     */
    public boolean isPersistent()
    {
        return _persistent;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#getType()
     */
    public String getType()
    {
        return _type;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setInitialValue(java.lang.String)
     */
    public void setInitialValue(String initialValue)
    {
        String old = this._initialValue;
        this._initialValue = initialValue;
        firePropertyChange("initialValue", old, this._initialValue);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setName(java.lang.String)
     */
    public void setName(String name)
    {
        String old = this._name;
        this._name = name;
        firePropertyChange("name", old, this._name);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setPersistent(boolean)
     */
    public void setPersistent(boolean persistant)
    {
        boolean old = this._persistent;
        this._persistent = persistant;
        firePropertyChange("persistant", old, this._persistent);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setType(java.lang.String)
     */
    public void setType(String type)
    {
        String old = this._type;
        this._type = type;
        firePropertyChange("type", old, this._type);
    }

}
