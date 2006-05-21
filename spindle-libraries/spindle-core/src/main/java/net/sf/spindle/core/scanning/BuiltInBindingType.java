package net.sf.spindle.core.scanning;

import java.util.EnumSet;

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
public enum BuiltInBindingType {
    EXPRESSION("ognl"),
    MESSAGE("message"),
    LITERAL(""),
    ASSET("asset"),
    BEAN("bean"),
    LISTENER("listener"),
    COMPONENT("component"),
    STATE("state"),
    TRANSLATOR("translator"),
    VALIDATOR("validator"),
    VALIDATORS("validators"),
    HIVEMIND("hivemind"),
    UNKNOWN(null);


    public static BuiltInBindingType get(String identifier)
    {
        for (BuiltInBindingType type : values())
        {
            if (type.identifier.equals(identifier))
                return type;
        }
        return UNKNOWN;
    }

    private String identifier;

    private BuiltInBindingType(String identifier)
    {
        this.identifier = identifier;
    }

    public String getIdentifier()
    {
        return identifier;
    }
}
