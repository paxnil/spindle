package net.sf.spindle.core.scanning;

import org.apache.commons.lang.StringUtils;

/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/ Software distributed under the License is distributed on
 * an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License. The Original Code is
 * __Spindle, an Eclipse Plugin For Tapestry__. The Initial Developer of the Original Code is
 * _____Geoffrey Longman__. Portions created by _____Initial Developer___ are Copyright (C) _2004,
 * 2005, 2006__ __Geoffrey Longman____. All Rights Reserved. Contributor(s):
 * __glongman@gmail.com___.
 */
public enum BuiltInBindingType {
    EXPRESSION("ognl"),
    MESSAGE("message"),
    LITERAL("literal"),
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

    /**
     * Templates have default type of {@link #LITERAL}<br>
     * XML has the default type of {@link #EXPRESSION}
     * 
     * @param identifier
     *            the string part before the colon
     * @param defaultBindingType
     *            the binding type to return if the idenifier is null or an empty string
     * @return the type that corresponds to the identifier, or the default, or {@link #UNKNOWN}
     */
    public static BuiltInBindingType get(String identifier, BuiltInBindingType defaultBindingType)
    {
        if (StringUtils.isBlank(identifier))
            return defaultBindingType;

        for (BuiltInBindingType type : values())
        {
            if (identifier.equals(type.identifier))
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
