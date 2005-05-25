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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.spec;

import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IParameterSpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.SpecificationScanner;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 * Spindle aware concrete implementation of ILibrarySpecification
 * 
 * @author glongman@gmail.com
 */
public class PluginParameterSpecification extends DescribableSpecification implements
        IParameterSpecification
{
    private boolean fRequired = false;

    private String fType;

    private boolean fCache;

    private String fDefaultBindingType;

    /** @since 2.0.3 * */
    private String fPropertyName;

    //  private Direction fDirection = Direction.CUSTOM;

    private String fDefaultValue;

    public PluginParameterSpecification()
    {
        super(BaseSpecification.PARAMETER_SPEC);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IParameterSpecification#getType()
     */
    public String getType()
    {
        return fType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IParameterSpecification#isRequired()
     */
    public boolean isRequired()
    {
        return fRequired;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IParameterSpecification#setRequired(boolean)
     */
    public void setRequired(boolean value)
    {
        fRequired = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IParameterSpecification#setType(java.lang.String)
     */
    public void setType(String value)
    {
        fType = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IParameterSpecification#setPropertyName(java.lang.String)
     */
    public void setPropertyName(String propertyName)
    {
        fPropertyName = propertyName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IParameterSpecification#getPropertyName()
     */
    public String getPropertyName()
    {
        return fPropertyName;
    }

    //  /*
    //   * (non-Javadoc)
    //   *
    //   * @see org.apache.tapestry.spec.IParameterSpecification#getDirection()
    //   */
    //  public Direction getDirection()
    //  {
    //    return fDirection;
    //  }
    //
    //  /*
    //   * (non-Javadoc)
    //   *
    //   * @see
    // org.apache.tapestry.spec.IParameterSpecification#setDirection(org.apache.tapestry.spec.Direction)
    //   */
    //  public void setDirection(Direction direction)
    //  {
    //    fDirection = direction;
    //  }

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
        fDefaultValue = defaultValue;
    }

    public void validate(Object parent, IScannerValidator validator)
    {

        IComponentSpecification component = (IComponentSpecification) parent;
        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        if (!"java.lang.Object".equals(fType))
        {

            String typeAttr = "type";

            if (!SpecificationScanner.TYPE_LIST.contains(fType))
            {
                try
                {
                    validateTypeSpecial(validator, (IResourceWorkspaceLocation) component
                            .getSpecificationLocation(), fType, IProblem.ERROR, sourceInfo
                            .getAttributeSourceLocation(typeAttr));
                }
                catch (ScannerException e)
                {
                    TapestryCore.log(e);
                    e.printStackTrace();
                }
            }
        }
    }

    private Object validateTypeSpecial(IScannerValidator validator,
            IResourceWorkspaceLocation dependant, String typeName, int severity,
            ISourceLocation location) throws ScannerException
    {
        String useName = typeName;
        if (useName.indexOf(".") < 0)
            useName = "java.lang." + useName;

        return validator.validateTypeName(dependant, useName, severity, location);

    }

    /**
     * @return Returns the cache.
     */
    public boolean getCache()
    {
        return fCache;
    }

    /**
     * @param cache
     *            The cache to set.
     */
    public void setCache(boolean cache)
    {
        this.fCache = cache;
    }

    /**
     * @return Returns the defaultBindingType.
     */
    public String getDefaultBindingType()
    {
        return fDefaultBindingType;
    }

    /**
     * @param defaultBindingType
     *            The defaultBindingType to set.
     */
    public void setDefaultBindingType(String defaultBindingType)
    {
        this.fDefaultBindingType = defaultBindingType;
    }
}