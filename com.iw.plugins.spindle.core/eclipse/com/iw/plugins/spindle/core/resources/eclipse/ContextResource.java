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


import com.iw.plugins.spindle.core.builder.EclipseBuildInfrastructure;

import core.TapestryCore;
import core.resources.ICoreResource;
import core.resources.IResourceAcceptor;
import core.resources.PathUtils;
import core.resources.search.ISearch;

/**
 * Implementation of ICoreResource for resources found within the context of a web application (in
 * Eclipse)
 * 
 * @author glongman@gmail.com
 */
public class ContextResource extends AbstractResource implements IEclipseResource
{

    private ContextRoot fRoot;

    public ContextResource(ContextRoot root, String path)
    {
        super(path);
        fRoot = root;
    }

    public ContextResource(ContextRoot root, IResource resource)
    {
        this(root, root.findRelativePath(resource));
    }
    
    public boolean clashesWith(ICoreResource resource)
    {
        if (this.equals(resource))
            return true;
        
        if (resource.isClasspathResource())
            return false;
        
        IPath mine = new Path(this.getPath()).makeAbsolute();
        if (!TapestryCore.isNull(getName()))
            mine = mine.removeLastSegments(1);
        
        IPath other = new Path(resource.getPath()).makeAbsolute();
        if (!TapestryCore.isNull(resource.getName()))
            other = other.removeLastSegments(1);
        
        if (mine.equals(other))
            return true;
        
        if (mine.isPrefixOf(other))
            return true;
        
        if (other.isPrefixOf(mine))
            return true;
        
        return false;                
    }    

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.util.AbstractResource#newResource(java.lang.String)
     */
    protected Resource newResource(String path)
    {
        return new ContextResource(fRoot, path);
    }

    /**
     * @return the IContainer that corresponds to the path part of the Resource. May be null or not
     *         exist()
     */
    public IContainer getContainer()
    {
        return fRoot.getContainer(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#exists()
     */
    public boolean exists()
    {
        if (!isFolder())            
            return getStorage() != null;
        
        IContainer container = getContainer();
        return container != null && container.exists();
    }
    
    public boolean isFolder()
    {
        return TapestryCore.isNull(getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#getSearch()
     */
    public ISearch getSearch()
    {
        return fRoot.getSearch();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#isBinaryResource()
     */
    public boolean isBinaryResource()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#lookup(core.resources.IResourceAcceptor)
     */
    public void lookup(IResourceAcceptor requestor)
    {
        IContainer container = getContainer();
        if (container != null && container.exists())
            fRoot.performLookup(container, requestor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Resource#getLocalization(java.util.Locale)
     */
    public Resource getLocalization(Locale locale)
    {
        // TODO implement later
        throw new RuntimeException("not implemented");
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

    public IStorage getStorage()
    {
        // if we are in a build, the storages get cached for speed.
        Map cache = EclipseBuildInfrastructure.getStorageCache();

        if (cache != null && cache.containsKey(this))
            return (IStorage) cache.get(this);

        IStorage result = null;
        IContainer container = getContainer();
        String name;
        if (container != null && container.exists() && (name = getName()) != null)
        {
            IStorage storage = (IStorage) container.getFile(new Path(name));
            IResource resource = (IResource) storage.getAdapter(IResource.class);
            if (resource != null && resource.exists())
                result = storage;
        }

        if (cache != null)
            cache.put(this, result);

        return result;
    }

    // private IResource getResource()
    // {
    // IContainer container = getContainer();
    // if (container != null && getName() != null)
    // {
    // IResource resource = container.findMember(new Path(getName()));
    // if (resource == null || resource.exists())
    // return resource;
    // }
    // return null;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.eclipse.IEclipseResource#getProject()
     */
    public IProject getProject()
    {
        IFile found = (IFile) getStorage();
        if (found != null && found.exists())
            return found.getProject();
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.ICoreResource#isClasspathResource()
     */
    public boolean isClasspathResource()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Resource#getResourceURL()
     */
    public URL getResourceURL()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean equals(Object obj)
    {
        if (super.equals(obj))
            return fRoot.equals(((ContextResource) obj).fRoot);
        return false;
    }

    public int hashCode()
    {
        return 4197 & getPath().hashCode();
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
        buffer.append("context:" + path.makeRelative().toString());
        return buffer.toString();
    }

}