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
import java.util.HashMap;
import java.util.Map;

import net.sf.spindle.core.resources.ICoreResource;

public class ServletInfo
{

    String name;

    String classname;

    Map<String, String> parameters = new HashMap<String, String>();

    boolean isServletSubclass;

    ICoreResource applicationSpecLocation;

    public String toString()
    {
        StringBuffer buffer = new StringBuffer("ServletInfo(");
        buffer.append(name);
        buffer.append(")::");
        buffer.append("classname = ");
        buffer.append(classname);
        buffer.append(", params = ");
        buffer.append(parameters);
        buffer.append(" loc= ");
        buffer.append(applicationSpecLocation);
        return buffer.toString();
    }

    public ICoreResource getApplicationSpecLocation()
    {
        return applicationSpecLocation;
    }

    public String getClassname()
    {
        return classname;
    }

    public boolean isServletSubclass()
    {
        return isServletSubclass;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }
}