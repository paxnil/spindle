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
public class PluginPropertySpecification extends DescribableSpecification implements IPropertySpecification
{

    private String fName;
    private String fType = "java.lang.Object";
    private boolean fPersistent;
    private String fInitialValue;

    public PluginPropertySpecification()
    {
        super(BaseSpecification.PROPERTY_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#getInitialValue()
     */
    public String getInitialValue()
    {
        return fInitialValue;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#getName()
     */
    public String getName()
    {
        return fName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#isPersistent()
     */
    public boolean isPersistent()
    {
        return fPersistent;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#getType()
     */
    public String getType()
    {
        return fType;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setInitialValue(java.lang.String)
     */
    public void setInitialValue(String initialValue)
    {
        String old = fInitialValue;
        fInitialValue = initialValue;
        firePropertyChange("initialValue", old, fInitialValue);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setName(java.lang.String)
     */
    public void setName(String name)
    {
        String old = fName;
        fName = name;
        firePropertyChange("name", old, fName);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setPersistent(boolean)
     */
    public void setPersistent(boolean persistant)
    {
        boolean old = fPersistent;
        fPersistent = persistant;
        firePropertyChange("persistant", old, fPersistent);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setType(java.lang.String)
     */
    public void setType(String type)
    {
        String old = fType;
        fType = type;
        firePropertyChange("type", old, fType);
    }

  

}
