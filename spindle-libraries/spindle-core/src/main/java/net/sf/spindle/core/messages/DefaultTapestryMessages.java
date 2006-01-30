package net.sf.spindle.core.messages;

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
public class DefaultTapestryMessages
{
    protected static MessageFormatter _formatter = new MessageFormatter(
            "org.apache.tapestry.Tapestry", "TapestryStrings");

    public static String format(String key)
    {
        return format(key, null);
    }

    private static String format(String key, Object[] args)
    {
        return _formatter.format(key, args);
    }

    public static String format(String key, Object arg)
    {
        return format(key, new Object[]
        { arg });
    }

    public static String format(String key, Object arg1, Object arg2)
    {
        return format(key, new Object[]
        { arg1, arg2 });
    }

    public static String format(String key, Object arg1, Object arg2, Object arg3)
    {
        return format(key, new Object[]
        { arg1, arg2, arg3 });
    }
}