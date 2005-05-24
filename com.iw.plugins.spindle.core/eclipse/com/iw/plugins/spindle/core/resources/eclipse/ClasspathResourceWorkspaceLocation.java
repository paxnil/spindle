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
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.apache.hivemind.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.core.JarEntryFile;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.EclipseBuildInfrastructure;
import com.iw.plugins.spindle.core.resources.AbstractResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.IResourceAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.search.ISearch;

/**
 * Implementation of IResourceWorkspaceLocation for resources found within classpath.
 * 
 * @author glongman@gmail.com
 */
public class ClasspathResourceWorkspaceLocation extends AbstractResourceWorkspaceLocation implements IEclipseResource
{

    protected ClasspathResourceWorkspaceLocation(ClasspathRootLocation root, String path)
    {
        super(root, path);
    }

    protected ClasspathResourceWorkspaceLocation(ClasspathRootLocation root, IStorage storage)
    {
        super(root, root.findRelativePath(storage));
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

    public IStorage getStorage()
    {
        //if we are in a build, the storages get cached for speed.
        Map cache = EclipseBuildInfrastructure.getStorageCache();

        if (cache != null && cache.containsKey(this))
            return (IStorage) cache.get(this);

        IStorage result = ((ClasspathRootLocation) fRoot).findStorage(this);

        if (cache != null)
            cache.put(this, result);

        return result;
    }

    public boolean isWorkspaceResource()
    {
        IStorage storage = getStorage();
        return storage != null && storage instanceof IResource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isBinary()
     */
    public boolean isBinaryResource()
    {
        IStorage storage = getStorage();
        return storage != null && storage instanceof JarEntryFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getProject()
     */
    public IProject getProject()
    {
        IPackageFragment fragment = ((ClasspathRootLocation) fRoot).findExactPackageFragment(this);
        return fragment == null ? null : fragment.getJavaProject().getProject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getContents()
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
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#seek(com.iw.plugins.spindle.core.resources.IResourceLocationRequestor)
     */
    public void lookup(IResourceAcceptor requestor) 
    {
        String packageName = ((ClasspathRootLocation) fRoot).toPackageName(getPath());
        IPackageFragment[] fragments = ((ClasspathRootLocation) fRoot)
                .getAllPackageFragments(packageName);
        for (int i = 0; i < fragments.length; i++)
        {
            Object[] nonJavaResources = null;
            try
            {
                if (fragments[i].isReadOnly())
                {
                    //TODO - is this the correct check for a package in a jar file?
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
                IResourceWorkspaceLocation loc = new ClasspathResourceWorkspaceLocation(
                        ((ClasspathRootLocation) fRoot), getPath() + storage.getName());
                if (!requestor.accept(loc))
                    break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getSearch()
     */
    public ISearch getSearch() 
    {
        return fRoot.getSearch();
    }

}