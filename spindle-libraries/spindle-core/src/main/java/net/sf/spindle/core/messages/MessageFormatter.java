// Copyright 2004, 2005 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.sf.spindle.core.messages;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.hivemind.impl.AbstractMessages;

/**
 * A wrapper around {@link java.util.ResourceBundle}that makes it easier to access and format
 * messages. Based on HM formatter but crafted for Spindle
 * 
 * @author Howard Lewis Ship, gwl
 */
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