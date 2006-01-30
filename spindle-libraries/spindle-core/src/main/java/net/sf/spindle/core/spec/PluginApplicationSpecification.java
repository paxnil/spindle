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
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.tapestry.spec.IApplicationSpecification;

/**
 * Spindle implementation of IApplicationSpecification
 * 
 * @author glongman@gmail.com
 */
public class PluginApplicationSpecification extends PluginLibrarySpecification implements
        IApplicationSpecification
{
    private String fName;

    private String fEngineClassName;

    public PluginApplicationSpecification()
    {
        super(BaseSpecification.APPLICATION_SPEC);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IApplicationSpecification#getName()
     */
    public String getName()
    {
        return fName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IApplicationSpecification#setEngineClassName(java.lang.String)
     */
    public void setEngineClassName(String value)
    {
        this.fEngineClassName = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IApplicationSpecification#getEngineClassName()
     */
    public String getEngineClassName()
    {
        return fEngineClassName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IApplicationSpecification#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.fName = name;
    }

    public void validateSelf(IScannerValidator validator)
    {
        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        if (fEngineClassName != null)
        {
            try
            {
                validator.validateTypeName(
                        (ICoreResource) getSpecificationLocation(),
                        fEngineClassName,
                        IProblem.ERROR,
                        sourceInfo.getAttributeSourceLocation("engine-class"));

            }
            catch (ScannerException e)
            {
                TapestryCore.log(e);
                e.printStackTrace();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.spec.PluginLibrarySpecification#validate(core.scanning.IScannerValidator)
     */
    public void validate(IScannerValidator validator)
    {
        validateSelf(validator);
        super.validate(validator);
    }

}