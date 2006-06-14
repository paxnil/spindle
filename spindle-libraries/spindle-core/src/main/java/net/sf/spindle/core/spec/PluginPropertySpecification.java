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
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.scanning.SpecificationScanner;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IPropertySpecification;

/**
 * Spindle aware concrete implementation of IPropertySpecification
 * 
 * @author glongman@gmail.com
 */
public class PluginPropertySpecification extends DescribableSpecification implements
        IPropertySpecification
{

    private String fName;

    private String fType = "java.lang.Object";

    private boolean fPersistent;

    private String fInitialValue;

    private String fPersistence;

    public PluginPropertySpecification()
    {
        super(SpecType.PROPERTY_SPEC);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#getInitialValue()
     */
    public String getInitialValue()
    {
        return fInitialValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#getName()
     */
    public String getName()
    {
        return fName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#isPersistent()
     */
    public boolean isPersistent()
    {
        return fPersistent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#getType()
     */
    public String getType()
    {
        return fType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#setInitialValue(java.lang.String)
     */
    public void setInitialValue(String initialValue)
    {
        fInitialValue = initialValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#setName(java.lang.String)
     */
    public void setName(String name)
    {
        fName = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#setPersistent(boolean)
     */
    public void setPersistent(boolean persistant)
    {
        fPersistent = persistant;
    }

    /*
     * (non-Javadoc)
     * 
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
                            (ICoreResource) component.getSpecificationLocation(),
                            fixedType,
                            IProblem.ERROR,
                            sourceInfo.getAttributeSourceLocation("type"));
                }
                else
                {
                    validator.validateTypeName(
                            (ICoreResource) component.getSpecificationLocation(),
                            fType,
                            IProblem.ERROR,
                            sourceInfo.getAttributeSourceLocation("type"));
                }
            }
        }
        catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#getPersistence()
     */
    public String getPersistence()
    {
        return fPersistence;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IPropertySpecification#setPersistence(java.lang.String)
     */
    public void setPersistence(String persistence)
    {
        fPersistence = persistence;
    }
}