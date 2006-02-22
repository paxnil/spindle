package net.sf.spindle.core.parser.validator;

import net.sf.spindle.core.build.BuilderMessages;
import net.sf.spindle.core.messages.MessageFormatter;

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
public class ValidatorMessages
{

    private static MessageFormatter FORMATTER = new MessageFormatter(ValidatorMessages.class,
            "ValidatorMessages");

    public static String noDTD(String publicId)
    {
        return FORMATTER.format("dom-validator-error-no-DTD", publicId);
    }

    public static String errorParsingDTD(String error)
    {
        return FORMATTER.format("dom-validator-error-no-DTD-parse", error);
    }

    public static String noDocType(String publicId)
    {
        return FORMATTER.format("dom-validator-error-no-doctype", publicId != null ? publicId
                : FORMATTER.getMessage("found-null"));
    }

    public static String noRootElement()
    {
        return FORMATTER.getMessage("dom-validator-error-no-root");
    }

    public static String invalidRootElement()
    {
        return FORMATTER.getMessage("dom-validator-error-invalid-root");
    }

    public static String wrongRoot(String expected, String got)
    {
        return FORMATTER.format("dom-validator-error-wrong-root-element", expected, got);
    }

    public static String undeclaredElement(String name)
    {
        return FORMATTER.format("dom-validator-undeclared-element", name);
    }

    public static String invalidAttributeValue(String attribute, String expected)
    {
        return FORMATTER.format("dom-validator-invalid-attr-value", attribute, expected);
    }

    public static String missingAtrribute(String name, String element)
    {
        return FORMATTER.format("dom-validator-missing-attr", name, element);
    }

    public static String illegalAtttribute(String name, String element)
    {
        return FORMATTER.format("dom-validator-not-allowed-attr", name, element);
    }

    public static String textNotAllowed(String element)
    {
        return FORMATTER.format("dom-validator-text-not-allowed", element);
    }

    public static String childNotAllowed(String child, String parent)
    {
        return FORMATTER.format("dom-validator-child-not-allowed", child, parent);
    }

    public static String elementNotAllowed(String element, String expected)
    {
        return FORMATTER.format("dom-validator-element-not-allowed", element, expected);
    }

    public static String undeclaredAttribute(String name, String element)
    {
        return FORMATTER.format("dom-validator-undeclared-atttribute", name, element);
    }

    public static String uniqueIDAttribute()
    {
        return FORMATTER.getMessage("dom-validator-id-attribute-must-be-unique");
    }
}
