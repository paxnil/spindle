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
    private boolean required = false;
    private String type;

    /** @since 1.0.9 **/
    private String description;

    /** @since 2.0.3 **/
    private String propertyName;

    private Direction direction = Direction.CUSTOM;

  
    public PluginParameterSpecification()
    {
        super(BaseSpecification.PARAMETER_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#getType()
     */
    public String getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#isRequired()
     */
    public boolean isRequired()
    {
        return required;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setRequired(boolean)
     */
    public void setRequired(boolean value)
    {
        boolean old = this.required;
        this.required = value;
        firePropertyChange("required", old, this.required);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setType(java.lang.String)
     */
    public void setType(String value)
    {
        String old = this.type;
        this.type = value;
        firePropertyChange("type", old, this.type);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        String old = this.description;
        this.description = description;
        firePropertyChange("description", old, this.description);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setPropertyName(java.lang.String)
     */
    public void setPropertyName(String propertyName)
    {
        String old = this.propertyName;
        this.propertyName = propertyName;
        firePropertyChange("propertyName", old, this.propertyName);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#getPropertyName()
     */
    public String getPropertyName()
    {
         return propertyName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#getDirection()
     */
    public Direction getDirection()
    {
        return direction;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IParameterSpecification#setDirection(org.apache.tapestry.spec.Direction)
     */
    public void setDirection(Direction direction)
    {
        Direction old = this.direction;
        this.direction = direction;
        firePropertyChange("direction", old, this.direction);
    }

}
