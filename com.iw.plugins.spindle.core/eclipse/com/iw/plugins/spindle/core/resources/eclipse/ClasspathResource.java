/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.resources.eclipse;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.apache.hivemind.Resource;
import org.apache.hivemind.util.AbstractResource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;


import com.iw.plugins.spindle.core.builder.EclipseBuildInfrastructure;
import com.iw.plugins.spindle.core.util.eclipse.JarEntryFileUtil;

import core.TapestryCore;
import core.TapestryCoreException;
import core.resources.ICoreResource;
import core.resources.IResourceAcceptor;
import core.resources.PathUtils;
import core.resources.search.ISearch;

/**
 * Implementation of ICoreResource for resources found within classpath.
 * 
 * @author glongman@gmail.com
 */
public class ClasspathResource extends AbstractResource implements IEclipseResource
{
    ClasspathRoot fRoot;

    public ClasspathResource(ClasspathRoot root, String path)
    {
        super(path);
        fRoot = root;
    }

    public ClasspathResource(ClasspathRoot root, IStorage storage)
    {
        super(root.findRelativePath(storage));
        fRoot = root;
    }

    public ClasspathResource(ClasspathRoot root, IPackageFragment fragment, IStorage storage)
    {
        super(root.getPath(fragment, storage));
        fRoot = root;
    }    

    public boolean clashesWith(ICoreResource resource)
    {        
        if (this == resource)
            return true;
        
        if (!resource.isClasspathResource())
            return false;
               
        IPath mine = new Path(this.getPath());
        IPath other = new Path(resource.getPath());
        
        if (mine.equals(other))
            return true;
        
        if (mine.isPrefixOf(other))
            return true;
        
        if (other.isPrefixOf(mine))
            return true;
        
        return false;                
    }    

    protected Resource newResource(String path)
    {
        return new ClasspathResource(fRoot, path);
    }

    public boolean exists()
    {
        IStorage storage = getStorage();
        if (storage != null)
        {
            if (storage instanceof IResource)
            {
                IResource resource = (IResource) storage;
                return resource.exists();
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#isBinary()
     */
    public boolean isBinaryResource()
    {
        IStorage storage = getStorage();
        return storage != null && JarEntryFileUtil.isJarEntryFile(storage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#isClasspathResource()
     */
    public boolean isClasspathResource()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#getContents()
     */
    public InputStream getContents()
    {
        try
        {
            IStorage storage = getStorage();
            if (storage != null)
                return storage.getContents();
        }
        catch (CoreException e)
        {
            TapestryCore.log(e);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#seek(core.resources.IResourceLocationRequestor)
     */
    public void lookup(IResourceAcceptor requestor)
    {
        String packageName = fRoot.toPackageName(this);
        IPackageFragment[] fragments = fRoot.getAllPackageFragments(packageName);
        for (int i = 0; i < fragments.length; i++)
        {
            Object[] nonJavaResources = null;
            try
            {
                if (fragments[i].isReadOnly())
                {
                    // TODO - is this the correct check for a package in a jar file?
                    nonJavaResources = fragments[i].getNonJavaResources();
                }
                else
                {
                    IContainer container = (IContainer) fragments[i].getUnderlyingResource();
                    if (container != null && container.exists())
                    {
                        IResource[] members = container.members(false);
                        ArrayList resultList = new ArrayList();
                        for (int j = 0; j < members.length; j++)
                        {
                            if (members[j] instanceof IFile)
                                resultList.add(members[j]);
                        }
                        nonJavaResources = resultList.toArray();
                    }
                }
            }
            catch (CoreException e)
            {
                TapestryCore.log(e);
            }
            if (nonJavaResources == null)
                continue;

            for (int j = 0; j < nonJavaResources.length; j++)
            {
                IStorage storage = (IStorage) nonJavaResources[j];
                ICoreResource loc = new ClasspathResource(fRoot, getPath() + storage.getName());
                if (!requestor.accept(loc))
                    break;
            }
        }
    }

    public ISearch getSearch() throws TapestryCoreException
    {
        return fRoot.getSearch();
    }

    public URL getResourceURL()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.IResourceLocation#getLocalization(java.util.Locale)
     */
    public Resource getLocalization(Locale locale)
    {
        // TODO implement later
        throw new RuntimeException("not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#getProject()
     */
    public IProject getProject()
    {
        IPackageFragment fragment = fRoot.findExactPackageFragment(this);
        return fragment == null ? null : fragment.getJavaProject().getProject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.eclipse.IEclipseResource#getStorage()
     */
    public IStorage getStorage()
    {
        // if we are in a build, the storages get cached for speed.
        Map cache = EclipseBuildInfrastructure.getStorageCache();

        if (cache != null && cache.containsKey(this))
            return (IStorage) cache.get(this);

        IStorage result = fRoot.findStorage(this);

        if (cache != null)
            cache.put(this, result);

        return result;
    }

    public boolean equals(Object obj)
    {
        if (super.equals(obj))
            return fRoot.equals(((ClasspathResource) obj).fRoot);
        return false;
    }

    public int hashCode()
    {
        return 4783 & getPath().hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        PathUtils path = new PathUtils(getPath());
        String name = getName();
        if (getName() != null)
            path.append(new PathUtils(name));
        buffer.append("classpath:" + path.makeRelative().toString());
        return buffer.toString();
    }

}