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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.TapestryBuilder;
import com.iw.plugins.spindle.core.resources.search.ISearch;

/**
 *  Implementation of IResourceWorkspaceLocation
 *  for resources found within classpath. 
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ClasspathResourceWorkspaceLocation extends AbstractResourceWorkspaceLocation
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
            } else
            {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getStorage()
     */
    public IStorage getStorage()
    {
        //if we are in a build, the storages get cached for speed.
        Map cache = TapestryBuilder.getStorageCache();
        
        if (cache != null && cache.containsKey(this))
            return (IStorage) cache.get(this);
            
        IStorage result =  ((ClasspathRootLocation) fRoot).findStorage(this);
        
        if (cache != null)
            cache.put(this, result);
            
       return result;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isWorkspaceResource()
     */
    public boolean isWorkspaceResource()
    {
        IStorage storage = getStorage();
        if (storage != null)
        {
            return storage instanceof IResource;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isBinary()
     */
    public boolean isBinary()
    {
        IStorage storage = getStorage();
        if (storage == null)
            return false;

        if (storage instanceof JarEntryFile)
            return true;

        return false;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getProject()
     */
    public IProject getProject()
    {
        IPackageFragment fragment = ((ClasspathRootLocation) fRoot).findExactPackageFragment(this);
        return fragment == null ? null : fragment.getJavaProject().getProject();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getContents()
     */
    public InputStream getContents() throws CoreException
    {
        IStorage storage = getStorage();
        if (storage != null)
            return storage.getContents();

        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getLocalization(java.util.Locale)
     */
    public IResourceLocation getLocalization(Locale locale)
    {
        // TODO implement later
        throw new RuntimeException("not implemented");
    }

    //    public int hashCode()
    //    {
    //        HashCodeBuilder builder = new HashCodeBuilder(5591, 1009);
    //
    //        builder.append(getPath());
    //        builder.append(getName());
    //        builder.append(fRoot);
    //
    //        return builder.toHashCode();
    //    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#seek(com.iw.plugins.spindle.core.resources.IResourceLocationRequestor)
     */
    public void lookup(IResourceLocationAcceptor requestor) throws CoreException
    {
        String packageName = ((ClasspathRootLocation) fRoot).toPackageName(getPath());
        IPackageFragment[] fragments = ((ClasspathRootLocation) fRoot).getAllPackageFragments(packageName);
        for (int i = 0; i < fragments.length; i++)
        {
            Object[] nonJavaResources = null;
            try
            {
                if (fragments[i].isReadOnly())
                {
                    //TODO - is this the correct check for a package in a jar file?
                    nonJavaResources = fragments[i].getNonJavaResources();
                } else
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
            } catch (JavaModelException e)
            {
                TapestryCore.log(e);
            }
            if (nonJavaResources == null)
                continue;

            for (int j = 0; j < nonJavaResources.length; j++)
            {
                IStorage storage = (IStorage) nonJavaResources[j];
                IResourceWorkspaceLocation loc =
                    new ClasspathResourceWorkspaceLocation(
                        ((ClasspathRootLocation) fRoot),
                        getPath() + storage.getName());
                if (!requestor.accept(loc))
                    break;
            }
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getSearch()
     */
    public ISearch getSearch() throws CoreException
    {
        return fRoot.getSearch();
    }

}
