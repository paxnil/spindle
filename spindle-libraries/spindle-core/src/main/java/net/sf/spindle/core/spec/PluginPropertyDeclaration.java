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
/**
 * Record <property>tags in a document These can only be validated at the time the document is
 * parsed/scanned.
 * 
 * @author glongman@gmail.com
 */
public class PluginPropertyDeclaration extends BaseSpecification
{
    String fKey;

    String fValue;

    boolean fValueIsFromAttribute;

    public PluginPropertyDeclaration(String key, String value)
    {
        super(BaseSpecification.PROPERTY_DECLARATION);
        setKey(key);
        fValue = value;
    }

    public String getKey()
    {
        return getIdentifier();
    }

    public void setKey(String key)
    {
        setIdentifier(key);
    }

    public String getValue()
    {
        return fValue;
    }

    /**
     * @return
     */
    public boolean isValueIsFromAttribute()
    {
        return fValueIsFromAttribute;
    }

    /**
     * @param b
     */
    public void setValueIsFromAttribute(boolean flag)
    {
        fValueIsFromAttribute = flag;
    }

}