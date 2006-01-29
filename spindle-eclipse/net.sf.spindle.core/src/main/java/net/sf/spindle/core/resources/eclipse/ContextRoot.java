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

package net.sf.spindle.core.resources.eclipse;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.IResourceAcceptor;
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.resources.search.ISearch;

import org.apache.hivemind.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Used for the roots
 * 
 * @author glongman@gmail.com
 */
/**
 * @author Administrator
 */
public class ContextRoot implements IResourceRoot
{

    IContainer fRootContainer;

    ContextSearch fSearch;

    public ContextRoot(IContainer container)
    {
        fRootContainer = container;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.IResourceRoot#lookup(core.resources.IResourceAcceptor)
     */
    public void lookup(IResourceAcceptor requestor)
    {
        performLookup(fRootContainer, requestor);
    }
    
    void performLookup(IContainer container, IResourceAcceptor requestor) {
        try
        {
            IResource[] members = container.members(false);
            for (int i = 0; i < members.length; i++)
            {
                if (members[i] instanceof IContainer)
                    continue;

                ICoreResource relativeResource = (ICoreResource) getRelativeResource(members[i].getName());
                if (!requestor.accept(relativeResource))
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
     * @see core.resources.IResourceRoot#getSearch()
     */
    public ISearch getSearch()
    {
        if (fSearch == null)
        {
            fSearch = new ContextSearch();
            fSearch.configure(fRootContainer);
        }
        return fSearch;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.IResourceRoot#exists()
     */
    public boolean exists()
    {
        if (fRootContainer == null)
            return false;

        return fRootContainer.exists();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.IResourceRoot#getRelativeResource(java.lang.String)
     */
    public Resource getRelativeResource(String path)
    {
        return new ContextResource(this, path);
    }

    public IContainer getContainer()
    {
        return fRootContainer;
    }

    // /**
    // * Convert an Eclipse workspace IResource into a ContextResource.
    // * <p>
    // * Boundary case: an IResource is presented that is not a child of
    // * this root.
    // * Expected result: an instance of ICoreResource will be returned but
    // * calls to exists() etc will return negative results.
    // *
    // * @param resource an IResource in the workspace
    // * @return a ContextResource corresponding to the input IResource
    // */
    // public ICoreResource getRelativeResource(IResource resource)
    // {
    // if (findRelativePath(resource) == null)
    // return null;
    // return new ContextResource(this, resource);
    // }

    /**
     * @param resource
     * @return
     */
    public String findRelativePath(IResource resource)
    {
        IPath rootPath = fRootContainer.getFullPath();
        IPath resourcePath = resource.getFullPath();
        if (!rootPath.isPrefixOf(resourcePath))
            return forceRelative(resourcePath, rootPath);

        IPath resultPath = resourcePath.removeFirstSegments(rootPath.segmentCount()).makeAbsolute();
        if (resource instanceof IContainer && resultPath.segmentCount() > 0)
            resultPath = resultPath.addTrailingSeparator();

        return resultPath.toString();
    }

    private String forceRelative(IPath resourcePath, IPath rootPath)
    {
        int backCount;
        IPath newRoot = null;
        if (resourcePath.segmentCount() == 0)
        {
            backCount = rootPath.segmentCount();
        }
        else
        {
            backCount = 1;
            newRoot = rootPath.removeLastSegments(1);
            while (!newRoot.isPrefixOf(resourcePath))
                backCount++;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < backCount; i++)
            buffer.append("../");

        buffer.append(resourcePath.removeFirstSegments(newRoot.segmentCount()).makeRelative());
        return buffer.toString();
    }

    /**
     * @param location
     * @return a handle to the container defined by the location
     */
    protected IContainer getContainer(ContextResource location)
    {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        return internalGetContainer(location.getPath(), workspaceRoot);
    }

    IContainer internalGetContainer(String path, IWorkspaceRoot workspaceRoot)
    {
        int lastSlashx = path.lastIndexOf('/');

        String folderPath = path.substring(0, lastSlashx + 1);
        IPath rootPath = fRootContainer.getFullPath();
        IPath workspacePath = new Path(rootPath.toString());
        workspacePath = workspacePath.append(folderPath);
        if (rootPath.isPrefixOf(workspacePath))
            return workspaceRoot.getFolder(workspacePath);

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "Context: " + fRootContainer.getFullPath() + "/ ";
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
            ContextRoot other = (ContextRoot) obj;
            return fRootContainer.equals(other.fRootContainer);
        }
        return false;
    }
}