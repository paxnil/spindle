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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.search.ISearch;
import com.iw.plugins.spindle.core.resources.search.ISearchAcceptor;
import com.iw.plugins.spindle.core.util.JarEntryFileUtil;

/**
 *  Used for the root of the Classpath
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ClasspathRootLocation extends AbstractRootLocation
{

    IJavaProject fJavaProject;
    ClasspathSearch fSearch;

    public ClasspathRootLocation(IJavaProject project)
    {
        fJavaProject = project;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#exists()
     */
    public boolean exists()
    {
        return fJavaProject.exists();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isWorkspaceResource()
     */
    public boolean isWorkspaceResource()
    {
        return false;
    }

    public IContainer getContainer()
    {
        return getProject();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getProject()
     */
    public IProject getProject()
    {
        return fJavaProject.getProject();
    }

    protected String toPackageName(String path)
    {
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

    protected String toPath(String packageName)
    {
        return packageName.replace('.', '/') + "/";
    }

    public IResourceWorkspaceLocation getRelativeLocation(IStorage storage)
    {
        if (findRelativePath(storage) == null)
            return null;
        return new ClasspathResourceWorkspaceLocation(this, storage);
    }

    public IResourceWorkspaceLocation getRelativeLocation(IPackageFragment fragment, IStorage storage)
    {
        return new ClasspathResourceWorkspaceLocation(this, toPath(fragment.getElementName()) + storage.getName());
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getRelativeLocation(java.lang.String)
     */
    public IResourceLocation getRelativeLocation(String path)
    {
        if (path.startsWith("/"))
        {
            if (getPath().equals(path))
            {
                return this;
            } else
            {
                return new ClasspathResourceWorkspaceLocation(this, new Path(path).makeRelative().toString());
            }
        }
        return new ClasspathResourceWorkspaceLocation(this, path);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#seek()
     */
    public void lookup(IResourceLocationAcceptor requestor) throws CoreException
    {
        throw new RuntimeException(" don't support the default package!");
    }

    public String findRelativePath(IStorage storage)
    {
        String result = null;
        IPackageFragment fragment = null;
        if (storage instanceof IFile)
        {
            IFolder folder = (IFolder) ((IFile) storage).getParent();

            fragment = (IPackageFragment) JavaCore.create(folder);

        } else
        {
            fragment = findPackageFragment(storage);
        }
        if (fragment != null)
            result = fragment.getElementName().replace('.', '/') + "/";

        return result;
    }

    private IPackageFragment findPackageFragment(IStorage storage)
    {
        if (storage instanceof IResource)
        {
            IContainer container = ((IResource) storage).getParent();
            return (IPackageFragment) JavaCore.create(container);
        } else if (storage instanceof JarEntryFile)
        {
            try
            {
                return JarEntryFileUtil.getPackageFragment(fJavaProject, (JarEntryFile) storage);
            } catch (CoreException e)
            {
                TapestryCore.log(e);
            }
        }
        return null;
    }

    public void find(String packageName, String filename, ISearchAcceptor requestor)
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
                    } catch (CoreException e1)
                    {
                        // do nothing
                    }
                } else
                {
                    nonJavaResources = fragments[i].getNonJavaResources();
                }
            } catch (JavaModelException e)
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
                } catch (ClassCastException e1)
                {
                    TapestryCore.log(
                        "[ 834756 ] Editing .xml files causes Eclipse to hang" + nonJavaResources[j].toString());

                }
            }
        }
    }

    public IStorage findStorage(ClasspathResourceWorkspaceLocation location)
    {
        String name = location.getName();
        if (name == null)
            return null;

        StorageAcceptor acceptor = new StorageAcceptor(name);
        find(toPackageName(location.getPath()), name, acceptor);
        return acceptor.getResult();
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
        public boolean accept(Object parent, IStorage storage)
        {
            if (storage.getName().equals(searchName))
            {
                searchResult = storage;
                return false; //stop the search
            }
            return true; //keep search going
        }
    }

    public IPackageFragment findExactPackageFragment(ClasspathResourceWorkspaceLocation location)
    {
        String name = location.getName();
        if (name == null)
            return null;

        FragmentAcceptor acceptor = new FragmentAcceptor(name);
        find(toPackageName(location.getPath()), name, acceptor);
        return acceptor.getResult();
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
        public boolean accept(Object parent, IStorage storage)
        {
            if (storage.getName().equals(searchName))
            {
                searchResult = (IPackageFragment) parent;
                return false; //stop the search
            }
            return true; //keep search going
        }
    }

    public IPackageFragment[] getAllPackageFragments(String packageName)
    {
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
        } catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }
        return (IPackageFragment[]) fragments.toArray(new IPackageFragment[fragments.size()]);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getSearch()
     */
    public ISearch getSearch() throws CoreException
    {
        if (fSearch == null)
        {
            fSearch = new ClasspathSearch();
            fSearch.configure(fJavaProject);
        }
        return fSearch;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isOnClasspath()
     */
    public boolean isOnClasspath()
    {
        return true;
    }

    public String toString()
    {
        return "cp(" + fJavaProject.getProject().getName() + ")/";
    }

    String toHashString()
    {
        return toString();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isBinary()
     */
    public boolean isBinary()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (obj.getClass().equals(getClass()))
        {
            ClasspathRootLocation other = (ClasspathRootLocation) obj;
            return this.fJavaProject.equals(other.fJavaProject);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getLocale()
     */
    public Locale getLocale()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
