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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.hivemind.impl.AbstractMessages;

public class MessageFormatter extends AbstractMessages
{
    private ResourceBundle _bundle;

    public MessageFormatter(ResourceBundle bundle)
    {
        _bundle = bundle;
    }

    public MessageFormatter(Class referenceClass, String name)
    {
        this(getResourceBundleName(referenceClass.getName(), name));
    }

    public MessageFormatter(String referenceClassName, String name)
    {
        this(getResourceBundleName(referenceClassName, name));
    }

    public MessageFormatter(String bundleName)
    {
        this(ResourceBundle.getBundle(bundleName));
    }

    protected String findMessage(String key)
    {
        try
        {
            return _bundle.getString(key);
        }
        catch (MissingResourceException ex)
        {
            return "!" + key + "!";
        }
    }

    protected Locale getLocale()
    {
        return Locale.getDefault();
    }

    private static String getResourceBundleName(String referenceClassName, String name)
    {
        int lastDotIndex = referenceClassName.lastIndexOf('.');
        String packageName = (lastDotIndex == -1 ? "" : referenceClassName.substring(
                0,
                lastDotIndex));

        return packageName.equals("") ? name : packageName + "." + name;
    }
}