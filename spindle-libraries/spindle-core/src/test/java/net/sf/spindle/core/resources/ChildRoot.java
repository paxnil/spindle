package net.sf.spindle.core.resources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.search.ISearch;

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
 * Base class for all child roots in an {@link net.sf.spindle.core.resources.ParentRoot}
 */
/* package */abstract class ChildRoot extends AbstractRoot
{

    protected static final int BINARY = 0;

    protected static final int SOURCE = 1;

    static ChildRoot[] growAndAddToArray(ChildRoot[] array, ChildRoot addition)
    {
        ChildRoot[] old = array;
        array = new ChildRoot[old.length + 1];
        System.arraycopy(old, 0, array, 0, old.length);
        array[old.length] = addition;
        return array;
    }

    static boolean arrayContains(ChildRoot[] array, ChildRoot possible)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i].equals(possible))
                return true;
        }
        return false;
    }

    protected ParentRoot parentRoot;

    private int type;

    protected File rootFile;

    protected URL rootUrl;

    ChildRoot(int type, ParentRoot parentRoot, File rootFile) throws MalformedURLException
    {
        this.type = type;
        this.parentRoot = parentRoot;
        this.rootFile = rootFile;
        this.rootUrl = intitializeUrl(rootFile);
    }

    protected abstract URL intitializeUrl(File file) throws MalformedURLException;

    Resource newResource(String path)
    {
        return parentRoot.newResource(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#exists()
     */
    public boolean exists()
    {
        return parentRoot.exists();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#exists(net.sf.spindle.core.resources.ClasspathResource)
     */
    boolean exists(ResourceImpl resource)
    {
        if (existsInThisRoot(resource.getPath()))
            return true;
        return parentRoot.exists(resource);
    }

    @Override
    boolean isBinaryResource(ResourceImpl resource)
    {
        return parentRoot.isBinaryResource(resource);
    }

    @Override
    boolean isClasspathResource(ResourceImpl resource)
    {
        return parentRoot.isClasspathResource(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#lookup(net.sf.spindle.core.resources.IResourceAcceptor)
     */
    public void lookup(IResourceAcceptor requestor)
    {
        parentRoot.lookup(requestor);
    }

    void lookup(ResourceImpl resource, IResourceAcceptor requestor)
    {
        parentRoot.lookup(resource, requestor);
    }

    
    final boolean performlookup(ResourceImpl resource, IResourceAcceptor requestor)
    {
        ICoreResource[] nonJavaResources = getNonJavaResources(resource);
        for (int i = 0; i < nonJavaResources.length; i++)
        {
            if (!requestor.accept(nonJavaResources[i]))
                return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#getRelativeResource(java.lang.String)
     */
    public Resource getRelativeResource(String path)
    {
        return parentRoot.getRelativeResource(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#getResourceURL(net.sf.spindle.core.resources.ClasspathResource)
     */
    @Override
    URL getResourceURL(ResourceImpl resource)
    {
        if (!existsInThisRoot(resource.getPath()))
            return parentRoot.getResourceURL(resource);

        return buildResourceURL(resource);
    }

    URL buildResourceURL(ResourceImpl resource)
    {
        try
        {
            return new URL(rootUrl, resource.getPath());
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#getLocalization(net.sf.spindle.core.resources.ClasspathResource,
     *      java.util.Locale)
     */
    Resource getLocalization(ResourceImpl resource, Locale locale)
    {
        return parentRoot.getLocalization(resource, locale);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#getSearch()
     */
    public ISearch getSearch() throws TapestryCoreException
    {
        return parentRoot.getSearch();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#clashCkeck(net.sf.spindle.core.resources.ClasspathResource,
     *      net.sf.spindle.core.resources.ICoreResource)
     */
    boolean clashCkeck(ResourceImpl resource, ICoreResource resource2)
    {
        return parentRoot.clashCkeck(resource, resource2);
    }

    abstract boolean existsInThisRoot(String path);

    abstract ICoreResource[] getNonJavaResources(ResourceImpl resource);

    int getType()
    {
        return type;
    }
    
    File getRootFile()
    {
        return rootFile;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof ChildRoot))
            return false;
        ChildRoot other = (ChildRoot) obj;
        return this.rootFile.equals(other.rootFile);
    }
}
