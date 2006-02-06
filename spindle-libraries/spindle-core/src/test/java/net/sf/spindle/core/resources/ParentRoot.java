package net.sf.spindle.core.resources;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.search.ISearch;

import org.apache.hivemind.Resource;
import org.apache.hivemind.util.LocalizedNameGenerator;
import org.apache.hivemind.util.LocalizedResource;

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
/*package*/abstract class ParentRoot extends AbstractRoot
{

    public static final int CLASSPATH = 0;

    public static final int CONTEXT = 1;

    final ResourceImpl defaultPackage;

    private int type;

    ChildRoot[] roots = new ChildRoot[] {};

    private ISearch search = null;

    public ParentRoot(int type)
    {
        super();
        this.type = type;
        defaultPackage = new ResourceImpl(this, "/")
        {

            public void setRoot(AbstractRoot root)
            {
                // the root never changes!
            }
        };
    }

    public void addFolder(File folder)
    {
        try
        {
            FolderRoot newRoot = new FolderRoot(this, folder);
            if (ChildRoot.arrayContains(roots, newRoot))
                TapestryCore.log("Classpath root already contains: " + folder.toString());
            else
                roots = ChildRoot.growAndAddToArray(roots, newRoot);
        }
        catch (Exception e)
        {
            TapestryCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractRoot#isClasspathResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    @Override
    boolean isClasspathResource(ResourceImpl resource)
    {
        return type == CLASSPATH;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractRoot#isBinaryResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    @Override
    boolean isBinaryResource(ResourceImpl resource)
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractRoot#getLocalization(net.sf.spindle.core.resources.ResourceImpl,
     *      java.util.Locale)
     */
    @Override
    ResourceImpl getLocalization(ResourceImpl resource, Locale locale)
    {
        LocalizedResourceFinder finder = new LocalizedResourceFinder();

        String path = resource.getPath();
        LocalizedResource localizedResource = finder.resolve(path, locale);

        if (localizedResource == null)
            return null;

        String localizedPath = localizedResource.getResourcePath();

        if (localizedPath == null)
            return null;

        if (path.equals(localizedPath))
            return resource;

        return new ResourceImpl(this, localizedPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#getResourceURL(net.sf.spindle.core.resources.ClasspathResource)
     */
    @Override
    URL getResourceURL(ResourceImpl resource)
    {
        if (exists(resource))
            return ((ChildRoot) resource.getRoot()).buildResourceURL(resource);

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#clashCkeck(net.sf.spindle.core.resources.ClasspathResource,
     *      net.sf.spindle.core.resources.ICoreResource)
     */
    @Override
    boolean clashCkeck(ResourceImpl resource, ICoreResource resource2)
    {
        // TODO in or out ? - not sure yet
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#newResource(java.lang.String)
     */
    @Override
    ResourceImpl newResource(String path)
    {
        return new ResourceImpl(this, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#exists(net.sf.spindle.core.resources.ClasspathResource)
     */
    @Override
    boolean exists(ResourceImpl resource)
    {

        ChildRoot root = findResourceRootFor(resource);
        if (root != null)
        {
            ((ResourceImpl) resource).setRoot(root);
            return true;
        }
        return false;
    }

    protected ChildRoot findResourceRootFor(ResourceImpl resource)
    {
        return findResourceFor(resource.getPath());
    }

    protected ChildRoot findResourceFor(String path)
    {
        for (int i = 0; i < roots.length; i++)
        {
            if (roots[i].existsInThisRoot(path))
                return roots[i];
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#getRelativeResource(java.lang.String)
     */
    public Resource getRelativeResource(String path)
    {
        path = (path.startsWith("/") ? path : '/' + path);
        return new ResourceImpl(this, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#exists()
     */
    public boolean exists()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#lookup(net.sf.spindle.core.resources.IResourceAcceptor)
     */
    public void lookup(IResourceAcceptor requestor)
    {
        lookup(defaultPackage, requestor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#lookup(net.sf.spindle.core.resources.ClasspathResource,
     *      net.sf.spindle.core.resources.IResourceAcceptor)
     */
    @Override
    void lookup(ResourceImpl resource, IResourceAcceptor requestor)
    {
        for (int i = 0; i < roots.length; i++)
        {
            if (!roots[i].performlookup(resource, requestor))
                return;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#getSearch()
     */
    public ISearch getSearch() throws TapestryCoreException
    {
        if (search == null)
            search = createSearch();
        return search;
    }

    abstract ISearch createSearch();

    class LocalizedResourceFinder
    {
        public LocalizedResource resolve(String path, Locale locale)
        {
            PathUtils utils = new PathUtils(path);
            String extension = utils.getFileExtension();
            if (extension != null)
                utils = utils.removeFileExtension();
            else
                extension = "";

            String basePath = utils.toString();
            String suffix = extension;          

            LocalizedNameGenerator generator = new LocalizedNameGenerator(basePath, locale, suffix);

            while (generator.more())
            {
                String candidatePath = generator.next();

                if (isExistingResource(candidatePath))
                    return new LocalizedResource(candidatePath, generator.getCurrentLocale());
            }

            return null;
        }

        private boolean isExistingResource(String path)
        {
            return findResourceFor(path) != null;
        }
    }

    int getType()
    {
        return type;
    }
}
