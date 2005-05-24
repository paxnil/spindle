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

import java.util.Locale;

import org.apache.hivemind.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.AbstractRootLocation;
import com.iw.plugins.spindle.core.resources.IResourceAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.search.ISearch;

/**
 * Used for the roots
 * 
 * @author glongman@gmail.com
 */
public class ContextRootLocation extends AbstractRootLocation implements IEclipseResource
{

    IFolder fRootFolder;

    ContextSearch fSearch;

    public ContextRootLocation(IFolder folder)
    {
        fRootFolder = folder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Resource#getLocale()
     */
    public Locale getLocale()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isBinaryResource()
     */
    public boolean isBinaryResource()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isWorkspaceResource()
     */
    public boolean isWorkspaceResource()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isClasspathResource()
     */
    public boolean isClasspathResource()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#lookup(com.iw.plugins.spindle.core.resources.IResourceAcceptor)
     */
    public void lookup(IResourceAcceptor requestor)
    {
        try
        {
            IResource[] members = fRootFolder.members(false);
            for (int i = 0; i < members.length; i++)
            {
                if (members[i] instanceof IContainer)
                    continue;

                if (!requestor.accept((IResourceWorkspaceLocation) getRelativeResource(members[i]
                        .getName())))
                    break;
            }
        }
        catch (CoreException e)
        {
            TapestryCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getSearch()
     */
    public ISearch getSearch()
    {
        if (fSearch == null)
        {
            fSearch = new ContextSearch();
            fSearch.configure(fRootFolder);
        }
        return fSearch;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#exists()
     */
    public boolean exists()
    {
        if (fRootFolder == null)
            return false;

        return fRootFolder.exists();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Resource#getRelativeResource(java.lang.String)
     */
    public Resource getRelativeResource(String name)
    {
        if (name.startsWith("/"))
        {
            if (getPath().equals(name))
            {
                return this;
            }
            else
            {
                return new ContextResourceWorkspaceLocation(this, new Path(name).makeAbsolute()
                        .toString());
            }
        }
        return new ContextResourceWorkspaceLocation(this, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.eclipse.IEclipseResource#getStorage()
     */
    public IStorage getStorage()
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.eclipse.IEclipseResource#getProject()
     */
    public IProject getProject()
    {
        return fRootFolder.getProject();
    }

    /**
     * @return
     */
    public IContainer getContainer()
    {
        return fRootFolder;
    }

    /**
     * @param resource
     * @return
     */
    public IResourceWorkspaceLocation getRelativeResource(IResource resource)
    {
        if (findRelativePath(resource) == null)
            return null;
        return new ContextResourceWorkspaceLocation(this, resource);
    }

    /**
     * @param resource
     * @return
     */
    public String findRelativePath(IResource resource)
    {
        IPath rootPath = fRootFolder.getFullPath();
        IPath resourcePath = resource.getFullPath();
        if (!rootPath.isPrefixOf(resourcePath))
            return null;

        IPath resultPath = resourcePath.removeFirstSegments(rootPath.segmentCount()).makeAbsolute();
        if (resource instanceof IContainer && resultPath.segmentCount() > 0)
            resultPath = resultPath.addTrailingSeparator();

        return resultPath.toString();
    }

    /**
     * @param location
     * @return
     */
    protected IContainer getContainer(ContextResourceWorkspaceLocation location)
    {
        IPath p = new Path(location.getPath());
        IFolder folder = fRootFolder.getFolder(p.removeTrailingSeparator());
        if (folder != null && folder.exists())
            return folder;

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "Context: " + fRootFolder.getFullPath() + "/ ";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.AbstractRootLocation#toHashString()
     */
    public String toHashString()
    {
        return toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (obj.getClass().equals(getClass()))
        {
            ContextRootLocation other = (ContextRootLocation) obj;
            return fRootFolder.equals(other.fRootFolder);
        }
        return false;
    }
}