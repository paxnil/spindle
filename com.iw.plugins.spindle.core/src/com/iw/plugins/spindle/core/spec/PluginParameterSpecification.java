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

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.Direction;
import org.apache.tapestry.spec.IParameterSpecification;

/**
 *  Spindle aware concrete implementation of ILibrarySpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginParameterSpecification extends BaseSpecification implements IParameterSpecification
{
    private boolean fRequired = false;
    private String fType;

    /** @since 1.0.9 **/
    private String fDescription;

    /** @since 2.0.3 **/
    private String fPropertyName;

    private Direction fDirection = Direction.CUSTOM;
    
    private String fDefaultValue;


    public PluginParameterSpecification()
    {
        super(BaseSpecification.PARAMETER_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#getType()
     */
    public String getType()
    {
        return fType;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#isRequired()
     */
    public boolean isRequired()
    {
        return fRequired;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setRequired(boolean)
     */
    public void setRequired(boolean value)
    {
        boolean old = fRequired;
        fRequired = value;
        firePropertyChange("required", old, fRequired);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setType(java.lang.String)
     */
    public void setType(String value)
    {
        String old = fType;
        fType = value;
        firePropertyChange("type", old, fType);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#getDescription()
     */
    public String getDescription()
    {
        return fDescription;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        String old = fDescription;
        fDescription = description;
        firePropertyChange("description", old, fDescription);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setPropertyName(java.lang.String)
     */
    public void setPropertyName(String propertyName)
    {
        String old = fPropertyName;
        fPropertyName = propertyName;
        firePropertyChange("propertyName", old, fPropertyName);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#getPropertyName()
     */
    public String getPropertyName()
    {
        return fPropertyName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#getDirection()
     */
    public Direction getDirection()
    {
        return fDirection;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setDirection(org.apache.tapestry.spec.Direction)
     */
    public void setDirection(Direction direction)
    {
        Direction old = fDirection;
        fDirection = direction;
        firePropertyChange("direction", old, fDirection);
    }

    /**
     * @see org.apache.tapestry.spec.IParameterSpecification#getDefaultValue()
     */
    public String getDefaultValue()
    {
        return fDefaultValue;
    }

    /**
     * @see org.apache.tapestry.spec.IParameterSpecification#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String defaultValue)
    {
        String old = fDefaultValue;
        fDefaultValue = defaultValue;
        firePropertyChange("defaultValue", old, fDefaultValue);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.spec.BaseSpecification#setNamespace(org.apache.tapestry.INamespace)
     */
    public void setNamespace(INamespace ns)
    {}

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.spec.BaseSpecification#getNamespace()
     */
    public INamespace getNamespace()
    {
        return null;
    }

}
