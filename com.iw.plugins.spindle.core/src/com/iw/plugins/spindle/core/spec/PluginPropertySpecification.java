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

import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IPropertySpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.SpecificationScanner;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

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
        fInitialValue = initialValue;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setName(java.lang.String)
     */
    public void setName(String name)
    {
        fName = name;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setPersistent(boolean)
     */
    public void setPersistent(boolean persistant)
    {
        fPersistent = persistant;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IPropertySpecification#setType(java.lang.String)
     */
    public void setType(String type)
    {
        fType = type;
    }

    public void validate(Object parent, IScannerValidator validator)
    {

        IComponentSpecification component = (IComponentSpecification) parent;

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        if ("java.lang.Object".equals(fType))
            return;

        try
        {
            if (!SpecificationScanner.TYPE_LIST.contains(fType))
            {
                if (fType.endsWith("[]"))
                {
                    String fixedType = fType.substring(0, fType.length() - 2);
                    while (fixedType.endsWith("[]"))
                    {
                        fixedType = fixedType.substring(0, fixedType.length() - 2);
                    }
                    validator.validateTypeName(
                        (IResourceWorkspaceLocation) component.getSpecificationLocation(),
                        fixedType,
                        IProblem.ERROR,
                        sourceInfo.getAttributeSourceLocation("type"));

                } else
                {

                    validator.validateTypeName(
                        (IResourceWorkspaceLocation) component.getSpecificationLocation(),
                        fType,
                        IProblem.ERROR,
                        sourceInfo.getAttributeSourceLocation("type"));
                }
            }
        } catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
    }

}
