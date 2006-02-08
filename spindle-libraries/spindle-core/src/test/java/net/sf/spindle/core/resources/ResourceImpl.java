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
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.search.ISearch;

import org.apache.hivemind.Resource;
import org.apache.hivemind.util.AbstractResource;

/**
 * an implementation of {@link net.sf.spindle.core.resources.ICoreResource}
 */
/* package */class ResourceImpl extends AbstractResource implements ICoreResource
{

    private IRootImplementation root;

    ResourceImpl(IRootImplementation root, String path)
    {
        super(path);
        this.root = root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.util.AbstractResource#newResource(java.lang.String)
     */
    @Override
    protected Resource newResource(String path)
    {
        return root.newResource(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ICoreResource#isClasspathResource()
     */
    public boolean isClasspathResource()
    {
        return root.isClasspathResource(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ICoreResource#isBinaryResource()
     */
    public boolean isBinaryResource()
    {
        return root.isBinaryResource(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ICoreResource#getContents()
     */
    public InputStream getContents()
    {
        return root.getContents(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ICoreResource#clashesWith(net.sf.spindle.core.resources.ICoreResource)
     */
    public boolean clashesWith(ICoreResource resource)
    {
        return root.clashCkeck(this, resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ICoreResource#isFolder()
     */
    public boolean isFolder()
    {
        return root.isFolder(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Resource#getResourceURL()
     */
    public URL getResourceURL()
    {
        return root.getResourceURL(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Resource#getLocalization(java.util.Locale)
     */
    public Resource getLocalization(Locale locale)
    {
        return root.getLocalization(this, locale);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#exists()
     */
    public boolean exists()
    {
        return root.exists(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#lookup(net.sf.spindle.core.resources.IResourceAcceptor)
     */
    public void lookup(IResourceAcceptor requestor)
    {
        root.lookup(this, requestor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#getSearch()
     */
    public ISearch getSearch() throws TapestryCoreException
    {
        return root.getSearch();
    }

    IRootImplementation getRoot()
    {
        return root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ICoreResource#getUnderlier()
     */
    public Object getUnderlier()
    {
        return root.getUnderlier(this);
    }

    /**
     * The root would never change in a jar but it could in a folder if a resource was deleted.
     */
    void setRoot(IRootImplementation root)
    {
        this.root = root;
    }

    @Override
    public boolean equals(Object arg0)
    {
        if (!super.equals(arg0))
            return false;
        if (!(arg0 instanceof ResourceImpl))
            return false;
        ResourceImpl other = (ResourceImpl) arg0;
        return getUnderlier().equals(other.getUnderlier());
    }

}
