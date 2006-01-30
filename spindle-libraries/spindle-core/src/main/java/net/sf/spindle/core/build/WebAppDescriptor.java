package net.sf.spindle.core.build;
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
import java.util.Map;

import net.sf.spindle.core.properties.DefaultProperties;

import org.apache.tapestry.engine.IPropertySource;

/**
 * Container of interesting things we scraped from web.xml
 */
public class WebAppDescriptor implements IPropertySource
{
    ServletInfo[] fServletInfos;

    Map fContextParameters;

    /**
     * @return Returns the contextParameters.
     */
    public Map getContextParameters()
    {
        return fContextParameters;
    }

    /**
     * @param contextParameters
     *            The contextParameters to set.
     */
    public void setContextParameters(Map contextParameters)
    {
        this.fContextParameters = contextParameters;
    }

    /**
     * @return Returns the servletInfos.
     */
    public ServletInfo[] getServletInfos()
    {
        return fServletInfos;
    }

    /**
     * @param servletInfos
     *            The servletInfos to set.
     */
    public void setServletInfos(ServletInfo[] servletInfos)
    {
        this.fServletInfos = servletInfos;
    }
    
    public String getPropertyValue(String propertyName)
    {
        String result = internalGetPropertyValue(propertyName);
        if (result != null)
            return result;
        
        return DefaultProperties.getInstance().getPropertyValue(propertyName);
    }

    public String internalGetPropertyValue(String propertyName)
    {
        if (fServletInfos == null || fServletInfos.length == 0)
            return null;
        String result = (String) fServletInfos[0].parameters.get(propertyName);
        if (result != null)
            return result;
        
        if (fContextParameters == null)
            return null;
        
        return (String) fContextParameters.get(propertyName);
    }
}
