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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hivemind.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryCoreException;
import com.iw.plugins.spindle.core.builder.EclipseBuildInfrastructure;
import com.iw.plugins.spindle.core.resources.IResourceAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceRoot;
import com.iw.plugins.spindle.core.resources.PathUtils;
import com.iw.plugins.spindle.core.resources.search.ISearch;
import com.iw.plugins.spindle.core.resources.search.ISearchAcceptor;
import com.iw.plugins.spindle.core.util.eclipse.JarEntryFileUtil;

/**
 * Used for the root of the Classpath
 * 
 * @author glongman@gmail.com
 */
public class ClasspathRoot implements IResourceRoot
{
    IJavaProject fJavaProject;

    ClasspathSearch fSearch;

    public ClasspathRoot(IJavaProject project)
    {
        fJavaProject = project;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceRoot#lookup(com.iw.plugins.spindle.core.resources.IResourceAcceptor)
     */
    public void lookup(IResourceAcceptor acceptor)
    {
        // TODO 
        throw new Error("not implemented yet");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.ICoreResource#getSearch()
     */
    public ISearch getSearch() throws TapestryCoreException 
    {
        if (fSearch == null)
        {
            fSearch = new ClasspathSearch();
            fSearch.configure(fJavaProject);
        }
    
        return fSearch;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceRoot#exists()
     */
    public boolean exists()
    {        
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.IResourceRoot#getRelativeResource(java.lang.String)
     */
    public Resource getRelativeResource(String path)
    {
        return new ClasspathResource(this, path);
    }

    public IJavaProject getJavaProject()
    {
        return fJavaProject;
    }

    public IContainer getContainer()
    {
        return fJavaProject.getProject();
    }


    protected String toPackageName(ClasspathResource resource)
    {
        String path = PathUtils.getFolderPath(resource);
        
        if (path != null)
        {
            if (path.startsWith("/"))
                path = path.substring(1, path.length());

            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);

            return path.replace('/', '.');
        }
        return null;
    }

    String toPath(String packageName)
    {
        return packageName.replace('.', '/') + "/";
    }

    String getPath(IPackageFragment fragment, IStorage storage)
    {
        return toPath(fragment.getElementName()) + storage.getName();
    }

    public String findRelativePath(IStorage storage)
    {
        String result = null;
        IPackageFragment fragment = null;
        if (storage instanceof IFile)
        {
            IFolder folder = (IFolder) ((IFile) storage).getParent();

            fragment = (IPackageFragment) JavaCore.create(folder);

        }
        else
        {
            fragment = findPackageFragment(storage);
        }
        if (fragment != null)
            result = fragment.getElementName().replace('.', '/') + "/";

        return result;
    }

    IPackageFragment findPackageFragment(IStorage storage)
    {
        if (storage instanceof IResource)
        {
            IContainer container = ((IResource) storage).getParent();
            return (IPackageFragment) JavaCore.create(container);
        }
        else if (JarEntryFileUtil.isJarEntryFile(storage))
        {
            try
            {
                return JarEntryFileUtil.getPackageFragment(fJavaProject, storage);
            }
            catch (CoreException e)
            {
                TapestryCore.log(e);
            }
        }
        return null;
    }

    void find(String packageName, String filename, ISearchAcceptor requestor)
    {
        IPackageFragment[] fragments = getAllPackageFragments(packageName);
        for (int i = 0; i < fragments.length; i++)
        {
            Object[] nonJavaResources = null;
            try
            {
                IPackageFragmentRoot root = (IPackageFragmentRoot) fragments[i].getParent();
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE)
                {
                    IFolder folder = (IFolder) fragments[i].getUnderlyingResource();
                    try
                    {
                        nonJavaResources = folder.members();
                    }
                    catch (CoreException e1)
                    {
                        // do nothing
                    }
                }
                else
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
            }
            catch (CoreException e)
            {
                TapestryCore.log(e);
            }
            if (nonJavaResources == null)
                continue;

            for (int j = 0; j < nonJavaResources.length; j++)
            {
                try
                {
                    if (nonJavaResources[j] instanceof IContainer)
                        continue;
                    IStorage storage = (IStorage) nonJavaResources[j];
                    if (!requestor.accept(fragments[i], storage))
                        return;
                }
                catch (ClassCastException e1)
                {
                    TapestryCore.log("[ 834756 ] Editing .xml files causes Eclipse to hang"
                            + nonJavaResources[j].toString());

                }
            }
        }
    }

    IStorage findStorage(ClasspathResource location)
    {
        String name = location.getName();
        if (name == null)
            return null;
        

        StorageAcceptor acceptor = new StorageAcceptor(name);
        find(toPackageName(location), name, acceptor);
        return acceptor.getResult();
    }

    IPackageFragment findExactPackageFragment(ClasspathResource location)
    {
        String name = location.getName();
        if (name == null)
            return null;

        FragmentAcceptor acceptor = new FragmentAcceptor(name);
        find(toPackageName(location), name, acceptor);
        return acceptor.getResult();
    }

    IPackageFragment[] getAllPackageFragments(String packageName)
    {
        Map cache = EclipseBuildInfrastructure.getPackageCache();

        if (cache != null && cache.containsKey(packageName))
            return (IPackageFragment[]) cache.get(packageName);

        List fragments = new ArrayList();
        try
        {
            IPackageFragmentRoot[] roots = fJavaProject.getAllPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++)
            {
                IPackageFragment frag = roots[i].getPackageFragment(packageName);
                if (frag != null && frag.exists())
                    fragments.add(frag);

            }
        }
        catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }

        IPackageFragment[] result = (IPackageFragment[]) fragments
                .toArray(new IPackageFragment[fragments.size()]);

        if (cache != null)
            cache.put(packageName, result);

        return result;
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
            ClasspathRoot other = (ClasspathRoot) obj;
            return this.fJavaProject.equals(other.fJavaProject);
        }

        return false;
    }

    public String toString()
    {
        return "Classpath ";
    }

    class FragmentAcceptor implements ISearchAcceptor
    {
        IPackageFragment searchResult = null;

        String searchName;

        public FragmentAcceptor(String searchName)
        {
            this.searchName = searchName;
        }

        public IPackageFragment getResult()
        {
            return searchResult;
        }

        public boolean accept(Object parent, Object leaf)
        {
            IStorage storage = (IStorage) leaf;
            if (storage.getName().equals(searchName))
            {
                searchResult = (IPackageFragment) parent;
                return false; // stop the search
            }
            return true; // keep search going
        }
    }

    class StorageAcceptor implements ISearchAcceptor
    {
        IStorage searchResult = null;

        String searchName;

        public StorageAcceptor(String searchName)
        {
            this.searchName = searchName;
        }

        public IStorage getResult()
        {
            return searchResult;
        }

        public boolean accept(Object parent, Object leaf)
        {
            IStorage storage = (IStorage) leaf;
            if (storage.getName().equals(searchName))
            {
                if (leaf instanceof IResource && !((IResource) leaf).exists())
                    return true;
                searchResult = storage;
                return false; // stop the search
            }
            return true; // keep search going
        }
    }
}