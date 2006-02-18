package net.sf.spindle.core.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sf.spindle.core.TapestryCore;

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
/**
 * Common behaviour for all roots (the main and the children)
 */
/* package */abstract class AbstractRoot implements IRootImplementation
{
    static final String EXTENSION_class = "class";

    static final String EXTENSION_java = "java";

    static final String SUFFIX_STRING_class = "." + EXTENSION_class;

    static final String SUFFIX_STRING_java = "." + EXTENSION_java;

    static boolean isJavaName(String name)
    {
        if (name == null)
            return false;
        if (name.trim().endsWith(SUFFIX_STRING_class))
            return true;
        if (name.trim().endsWith(SUFFIX_STRING_java))
            return true;
        return false;
    }

    static final ResourceImpl[] RESOURCE_EMPTY = new ResourceImpl[] {};

    static IRootImplementation[] growAndAddToArray(IRootImplementation[] array,
            IRootImplementation addition)
    {
        IRootImplementation[] old = array;
        array = new IRootImplementation[old.length + 1];
        System.arraycopy(old, 0, array, 0, old.length);
        array[old.length] = addition;
        return array;
    }

    static ResourceImpl[] growAndAddToArray(ResourceImpl[] array, ResourceImpl addition)
    {
        ResourceImpl[] old = array;
        array = new ResourceImpl[old.length + 1];
        System.arraycopy(old, 0, array, 0, old.length);
        array[old.length] = addition;
        return array;
    }

    static ResourceImpl[] growAndAddToArray(ResourceImpl[] array, ResourceImpl[] addition)
    {
        ResourceImpl[] old = array;
        array = new ResourceImpl[old.length + addition.length];
        System.arraycopy(old, 0, array, 0, old.length);
        System.arraycopy(addition, 0, array, old.length, addition.length);
        return array;
    }

    static boolean arrayContains(IRootImplementation[] array, IRootImplementation possible)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i].equals(possible))
                return true;
        }
        return false;
    }

    public boolean isFolder(String path)
    {
        return path.endsWith("/");
    }

    public boolean isFolder(ResourceImpl resource)
    {
        return TapestryCore.isNull(resource.getName());

    }

    public InputStream getContents(ResourceImpl resource)
    {
        try
        {
            URL resourceURL = getResourceURL(resource);
            if (resourceURL == null)
                return null;
            return resourceURL.openStream();
        }
        catch (IOException e)
        {
            TapestryCore.log(e);
            return null;
        }
    }
}
