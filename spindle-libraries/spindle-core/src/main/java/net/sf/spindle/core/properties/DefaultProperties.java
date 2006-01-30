package net.sf.spindle.core.properties;

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
import java.util.HashMap;
import java.util.Map;

import net.sf.spindle.core.TapestryCore;

import org.apache.tapestry.engine.IPropertySource;

/**
 * @author geoff
 */
public class DefaultProperties implements IPropertySource
{

    private static DefaultProperties INSTANCE;

    public static synchronized DefaultProperties getInstance()
    {
        if (INSTANCE == null)
            INSTANCE = new DefaultProperties();
        return INSTANCE;
    }

    private Map<String, String> properties;

    private DefaultProperties()
    {
        properties = new HashMap<String, String>();
        properties.put("org.apache.tapestry.engine-class", "org.apache.tapestry.engine.BaseEngine");
        properties.put("org.apache.tapestry.default-script-language", "jython");
        properties.put("org.apache.tapestry.visit-class", "java.util.HashMap");
        properties.put("org.apache.tapestry.output-encoding", "UTF-8");
        properties.put("org.apache.tapestry.enhance.disable-abstract-method-validation", "false");
        properties.put(
                "org.apache.tapestry.default-page-class",
                "org.apache.tapestry.html.BasePage");
        properties.put("org.apache.tapestry.template-extension", "html");
        properties.put("org.apache.tapestry.jwcid-attribute-name", "jwcid");
    }

    public String getPropertyValue(String propertyName)
    {
        if (!TapestryCore.isNull(propertyName))
            return (String) properties.get(propertyName);
        return null;
    }

}
