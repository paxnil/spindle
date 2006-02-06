package net.sf.spindle.core.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import net.sf.spindle.core.TapestryCore;

import org.apache.hivemind.Resource;

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
 * Common behaviour for all classpath roots (the main and the children)
 */
/* package */abstract class AbstractRoot implements IResourceRoot
{

    static final ResourceImpl[] EMPTY = new ResourceImpl[] {};
    
    static AbstractRoot[] growAndAddToArray(AbstractRoot[] array,
            AbstractRoot addition)
    {
        AbstractRoot[] old = array;
        array = new AbstractRoot[old.length + 1];
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

    static boolean arrayContains(AbstractRoot[] array, AbstractRoot possible)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i].equals(possible))
                return true;
        }
        return false;
    }

    abstract boolean isClasspathResource(ResourceImpl resource);

    abstract boolean isBinaryResource(ResourceImpl resource);

    abstract Resource getLocalization(ResourceImpl resource, Locale locale);

    boolean isFolder(String path)
    {
        return path.endsWith("/");
    }

    boolean isFolder(ResourceImpl resource)
    {
        return TapestryCore.isNull(resource.getName());

    }

    abstract URL getResourceURL(ResourceImpl resource);

    abstract boolean clashCkeck(ResourceImpl resource, ICoreResource resource2);

    InputStream getContents(ResourceImpl resource)
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

    abstract Resource newResource(String path);

    abstract boolean exists(ResourceImpl resource);

    abstract void lookup(ResourceImpl resource, IResourceAcceptor requestor);
}
