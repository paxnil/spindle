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


/**
 * Localized messages for the org.apache.tapestry.parse package.
 * 
 * @author Howard Lewis Ship
 * @since 4.0
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