package net.sf.spindle.core;

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
import net.sf.spindle.core.messages.MessageFormatter;

import org.apache.hivemind.Resource;

/**
 * @author gwl
 */
public class CoreMessages
{
    private static MessageFormatter FORMATTER = new MessageFormatter(CoreMessages.class,
            "CoreMessages");

    public static String resourceDoesNotExist(Resource location)
    {
        return FORMATTER.format("core-resource-does-not-exist", location.toString());
    }

    public static Object getXMLComment()
    {
        return FORMATTER.getMessage("TAPESTRY.xmlComment");
    }

    public static String invalidPublicID()
    {
        return FORMATTER.getMessage("error-invalid-spec-public-id");
    }

    public static String projectNamespace()
    {
        return FORMATTER.getMessage("project-namespace");
    }

}