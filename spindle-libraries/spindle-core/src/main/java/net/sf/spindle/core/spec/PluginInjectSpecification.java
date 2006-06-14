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
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.tapestry.spec.InjectSpecification;

/**
 * @author gwl
 */
public class PluginInjectSpecification extends BaseSpecification implements InjectSpecification
{
    private String object;
    private String property;
    private String type;
    
    public PluginInjectSpecification()
    {
        super(SpecType.INJECT);       
    }

    /**
     * @return Returns the object.
     */
    public String getObject()
    {
        return object;
    }
    /**
     * @return Returns the property.
     */
    public String getProperty()
    {
        return property;
    }
    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return type;
    }
    /**
     * @param object The object to set.
     */
    public void setObject(String object)
    {
        this.object = object;
    }
    /**
     * @param property The property to set.
     */
    public void setProperty(String property)
    {
        this.property = property;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    public void validateSelf(IScannerValidator validator) throws ScannerException
    {
        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();
        
        PluginComponentSpecification spec = (PluginComponentSpecification)getParent();
        
        validator.validateXMLInject(spec, this, sourceInfo);       
    }

   

}
