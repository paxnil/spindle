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

import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBindingSpecification;

/**
 * Spindle aware concrete implementation of IBindingSpecification
 * 
 * @author glongman@gmail.com
 */
public class PluginBindingSpecification extends DescribableSpecification implements
        IBindingSpecification
{

    private BindingType fBindingType;

    private String fValue;

    private String prefix;

    public PluginBindingSpecification()
    {
        super(BaseSpecification.BINDING_SPEC);
    }

    protected PluginBindingSpecification(int type)
    {
        super(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBindingSpecification#getType()
     */
    public BindingType getType()
    {
        return fBindingType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBindingSpecification#getValue()
     */
    public String getValue()
    {
        return fValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBindingSpecification#setType(org.apache.tapestry.spec.BindingType)
     */
    public void setType(BindingType type)
    {
        fBindingType = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBindingSpecification#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
        fValue = value;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public String getUnprefixedValue()
    {
        String value = getValue();
        if (fBindingType != BindingType.PREFIXED)
            return value;
        return value.substring(prefix.length() + 1);
    }
}