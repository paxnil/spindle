package net.sf.spindle.core.resources;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.search.ISearch;
import net.sf.spindle.core.resources.search.ISearchAcceptor;
import net.sf.spindle.core.util.Assert;

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
/*package*/abstract class ParentRoot extends AbstractRoot implements IChildRoot
{

    public static final int CLASSPATH = 0;

    public static final int CONTEXT = 1;

    final ResourceImpl defaultPackage;

    private int type;

    ArrayList<IChildRoot> roots = new ArrayList<IChildRoot>();

    private ISearch search = null;

    public ParentRoot(int type)
    {
        super();
        this.type = type;
        defaultPackage = new ResourceImpl(this, "/")
        {

            @SuppressWarnings("unused")
            public void setRoot(AbstractRoot root)
            {
                // the root never changes!
            }
        };
    }

    public void removeChildRoot(Object rootObject)
    {
        IChildRoot root = null;

        for (Iterator iter = roots.iterator(); iter.hasNext();)
        {
            IChildRoot child = (IChildRoot) iter.next();
            if (child.getRootObject().equals(rootObject))
            {
                iter.remove();
                root = child;
                break;
            }
        }

        if (root == null)
            return;

        Invoker invoker = (Invoker) Proxy.getInvocationHandler(root);
        invoker.setTarget(this);
    }

    public void addFolder(File folder)
    {
        try
        {
            IChildRoot newRoot = createProxy(new FolderRoot(this, folder));

            if (roots.contains(newRoot))
                TapestryCore.log("Classpath root already contains: " + folder.toString());
            else
                roots.add(newRoot);
        }
        catch (Exception e)
        {
            TapestryCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#isClasspathResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    public boolean isClasspathResource(ResourceImpl resource)
    {
        return type == CLASSPATH;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#isBinaryResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    public boolean isBinaryResource(ResourceImpl resource)
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#getLocalization(net.sf.spindle.core.resources.ResourceImpl,
     *      java.util.Locale)
     */
    public ResourceImpl getLocalization(ResourceImpl resource, Locale locale)
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
     * @see net.sf.spindle.core.resources.IRootImplementation#getResourceURL(net.sf.spindle.core.resources.ResourceImpl)
     */
    public URL getResourceURL(ResourceImpl resource)
    {
        if (exists(resource))
            return ((IChildRoot) resource.getRoot()).buildResourceURL(resource);

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#newResource(java.lang.String)
     */
    public ResourceImpl newResource(String path)
    {
        return new ResourceImpl(this, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IRootImplementation#exists(net.sf.spindle.core.resources.ResourceImpl)
     */
    public boolean exists(ResourceImpl resource)
    {
        IChildRoot root = findResourceRootFor(resource);
        if (root != null)
        {
            ((ResourceImpl) resource).setRoot(root);
            return true;
        }
        return false;
    }

    protected IChildRoot findResourceRootFor(ResourceImpl resource)
    {
        return findResourceRootFor(resource.getPath());
    }

    protected IChildRoot findResourceRootFor(String path)
    {
        for (IChildRoot child : roots)
        {
            if (child.existsInThisRoot(path))
                return child;
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
     * @see net.sf.spindle.core.resources.IRootImplementation#getUnderlier(net.sf.spindle.core.resources.ResourceImpl)
     */
    public Object getUnderlier(ResourceImpl resource)
    {
        IChildRoot root = findResourceRootFor(resource);
        if (root == null)
            return null;
        return root.findUnderlier(resource);
    }
    
    

    /* (non-Javadoc)
     * @see net.sf.spindle.core.resources.IRootImplementation#lookup(net.sf.spindle.core.resources.ResourceImpl, net.sf.spindle.core.resources.IResourceAcceptor, net.sf.spindle.core.resources.ResourceExtension.DEPTH)
     */
    public void lookup(ResourceImpl resource, IResourceAcceptor requestor, LookupDepth depth)
    {        
        ArrayList<ICoreResource> seenResources = new ArrayList<ICoreResource>();
        for (IChildRoot child : roots)
        {
            if (!child.performlookup(resource, requestor, seenResources, depth))
                return;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.spindle.core.resources.ResourceExtension#lookup(net.sf.spindle.core.resources.IResourceAcceptor, net.sf.spindle.core.resources.ResourceExtension.DEPTH)
     */
    public void lookup(IResourceAcceptor requestor, LookupDepth depth)
    {
        lookup(defaultPackage, requestor, depth);        
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

    int getKind()
    {
        return type;
    }

    IChildRoot createProxy(IChildRoot toBeProxied)
    {
        Invoker invoker = new Invoker();
        invoker.setTarget(toBeProxied);
        return (IChildRoot) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]
        { IChildRoot.class }, invoker);
    }

    class Invoker implements InvocationHandler
    {
        IChildRoot target;

        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
        {
            if (target != null)
                return invoke(proxy, m, args, target);
            else
                return invoke(proxy, m, args, ParentRoot.this);
        }

        private Object invoke(Object proxy, Method m, Object[] args, Object target)
                throws Throwable
        {
            try
            {
                return m.invoke(target, args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getCause();
            }
        }

        IRootImplementation getTarget()
        {
            return target;
        }

        void setTarget(IChildRoot target)
        {
            this.target = target;
        }
    }

    /**
     * a filter that enforces classpath lookup semantics for lookups.
     */
    class HiddenFileFilter implements IResourceAcceptor
    {

        IResourceAcceptor wrapped;

        ArrayList<ICoreResource> seenResources;

        public HiddenFileFilter(IResourceAcceptor wrapped)
        {
            this.wrapped = wrapped;
            this.seenResources = new ArrayList<ICoreResource>();
        }

        public boolean accept(ICoreResource location)
        {
            if (seenResources.contains(location))
                return true;

            seenResources.add(location);
            return wrapped.accept(location);
        }

        public ICoreResource[] getResults()
        {
            return wrapped.getResults();
        }
    }

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
            String suffix = "."+extension;

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
            return findResourceRootFor(path) != null;
        }
    }

    /* for the proxy replacement - must never called */

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#buildResourceURL(net.sf.spindle.core.resources.ResourceImpl)
     */
    public URL buildResourceURL(ResourceImpl resource)
    {
        Assert.isLegal(false);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#existsInThisRoot(java.lang.String)
     */
    public boolean existsInThisRoot(String path)
    {
        Assert.isLegal(false);
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#findUnderlier(net.sf.spindle.core.resources.ResourceImpl)
     */
    public Object findUnderlier(ResourceImpl resource)
    {
        Assert.isLegal(false);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#getNonJavaResources(net.sf.spindle.core.resources.ResourceImpl)
     */
    public ResourceImpl[] getNonJavaResources(ResourceImpl resource, LookupDepth depth)
    {
        Assert.isLegal(false);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#getRootFile()
     */
    public Object getRootObject()
    {
        Assert.isLegal(false);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#performlookup(net.sf.spindle.core.resources.ResourceImpl,
     *      net.sf.spindle.core.resources.IResourceAcceptor, java.util.ArrayList)
     */
    public boolean performlookup(ResourceImpl resource, IResourceAcceptor requestor,
            ArrayList<ICoreResource> seenResources, LookupDepth depth)
    {
        Assert.isLegal(false);
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#performSearch(net.sf.spindle.core.resources.search.ISearchAcceptor)
     */
    public void performSearch(ISearchAcceptor acceptor)
    {
        Assert.isLegal(false);

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.IChildRoot#getType()
     */
    public int getType()
    {
        Assert.isLegal(false);
        return 0;
    }
}
