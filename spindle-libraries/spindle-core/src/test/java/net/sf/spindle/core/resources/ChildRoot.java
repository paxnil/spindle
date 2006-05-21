package net.sf.spindle.core.resources;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.search.ISearch;

import org.apache.hivemind.Resource;

/**
 * Base class for all child roots in an {@link net.sf.spindle.core.resources.ParentRoot}
 */
/* package */abstract class ChildRoot extends AbstractRoot implements IChildRoot
{

    protected ParentRoot parentRoot;

    private int type;

    protected Object rootObject;

    protected URL rootUrl;

    ChildRoot(int type, ParentRoot parentRoot, Object rootObject) throws MalformedURLException
    {
        this.type = type;
        this.parentRoot = parentRoot;
        this.rootObject = rootObject;
        this.rootUrl = intitializeUrl();
    }

    protected abstract URL intitializeUrl() throws MalformedURLException;

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#newResource(java.lang.String)
     */
    public Resource newResource(String path)
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
     * @see net.sf.spindle.core.resources.IRootImplementation#exists(net.sf.spindle.core.resources.ResourceImpl)
     */
    public boolean exists(ResourceImpl resource)
    {
        if (existsInThisRoot(resource.getPath()))
            return true;
        return parentRoot.exists(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#isBinaryResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    public boolean isBinaryResource(ResourceImpl resource)
    {
        return parentRoot.isBinaryResource(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#isClasspathResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    public boolean isClasspathResource(ResourceImpl resource)
    {
        return parentRoot.isClasspathResource(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#lookup(net.sf.spindle.core.resources.IResourceAcceptor,
     *      net.sf.spindle.core.resources.ResourceExtension.DEPTH)
     */
    public void lookup(IResourceAcceptor requestor, LookupDepth depth)
    {
        parentRoot.lookup(requestor, depth);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#lookup(net.sf.spindle.core.resources.ResourceImpl,
     *      net.sf.spindle.core.resources.IResourceAcceptor,
     *      net.sf.spindle.core.resources.ResourceExtension.DEPTH)
     */
    public void lookup(ResourceImpl resource, IResourceAcceptor requestor, LookupDepth depth)
    {
        parentRoot.lookup(resource, requestor, depth);
    }
    
    /*
     * (non-Javadoc) called by parent root
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#performlookup(net.sf.spindle.core.resources.ResourceImpl,
     *      net.sf.spindle.core.resources.IResourceAcceptor, java.util.ArrayList)
     */
    public final boolean performlookup(ResourceImpl resource, IResourceAcceptor requestor,
            ArrayList<ICoreResource> seenResources, LookupDepth depth)
    {
        ICoreResource[] nonJavaResources = getNonJavaResources(resource, depth);
        for (int i = 0; i < nonJavaResources.length; i++)
        {
            if (seenResources != null && seenResources.contains(nonJavaResources[i]))
                continue;
            seenResources.add(nonJavaResources[i]);
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
     * @see net.sf.spindle.core.resources.IRootImplementation#getResourceURL(net.sf.spindle.core.resources.ResourceImpl)
     */
    public URL getResourceURL(ResourceImpl resource)
    {
        if (!existsInThisRoot(resource.getPath()))
            return parentRoot.getResourceURL(resource);

        return buildResourceURL(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#buildResourceURL(net.sf.spindle.core.resources.ResourceImpl)
     */
    public URL buildResourceURL(ResourceImpl resource)
    {
        try
        {
            PathUtils rpath = new PathUtils(resource.getPath());
            return new URL(rootUrl, rpath.makeRelative().toString());
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#getLocalization(net.sf.spindle.core.resources.ResourceImpl,
     *      java.util.Locale)
     */
    public Resource getLocalization(ResourceImpl resource, Locale locale)
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
     * @see net.sf.spindle.core.resources.IChildRoot#getType()
     */
    public int getType()
    {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#getRootFile()
     */
    public Object getRootObject()
    {
        return rootObject;
    }
    
    

    public String getToStringPrefix()
    {       
        return parentRoot.getToStringPrefix();
    }
    
    

    @Override
    public String toString()
    {        
        return "childRoot:"+getToStringPrefix()+":"+rootObject.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof ChildRoot))
            return false;
        ChildRoot other = (ChildRoot) obj;
        return this.rootObject.equals(other.rootObject);
    }
}
