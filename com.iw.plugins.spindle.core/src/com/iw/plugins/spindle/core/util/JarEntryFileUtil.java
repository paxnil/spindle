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

package com.iw.plugins.spindle.core.util;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.ui.IStorageEditorInput;

/**
 * TODO Add Type comment
 * 
 * @author glongman@gmail.com
 */
public class JarEntryFileUtil
{
    static private final int start = "JarEntryFile[".length();

    public static IStorage wrap(JarEntryFile entry)
    {
        Assert.isNotNull(entry);
        return new JarEntryFileWrapper(entry);
    }

    public static IStorage wrap(IStorage storage)
    {
       return wrap(storage, false);
    }
    
    public static IStorage wrap(IStorage storage, boolean mustBeJarEntryFile)
    {
        if (storage instanceof JarEntryFileWrapper)
            return storage;
        
        if (storage instanceof JarEntryFile)
            return wrap((JarEntryFile) storage);
        if (mustBeJarEntryFile)
            return null;
        return storage;
    }

    public static Object[] getNonJavaResources(IPackageFragment fragment) throws JavaModelException
    {
        Assert.isTrue(fragment.getKind() == IPackageFragmentRoot.K_BINARY);
        Object[] resources = fragment.getNonJavaResources();
        ArrayList result = new ArrayList();
        for (int i = 0; i < resources.length; i++)
        {
            result.add(new JarEntryFileWrapper((JarEntryFile) resources[i]));
        }
        return result.toArray();
    }

    public static class JarEntryFileWrapper implements IStorage
    {
        JarEntryFile entry;

        public JarEntryFileWrapper(JarEntryFile entry)
        {
            this.entry = entry;
        }

        public InputStream getContents() throws CoreException
        {
            return entry.getContents();
        }

        public IPath getFullPath()
        {
            return entry.getFullPath();
        }

        public String getName()
        {
            return entry.getName();
        }

        public boolean isReadOnly()
        {
            return entry.isReadOnly();
        }

        public Object getAdapter(Class adapter)
        {
            return entry.getAdapter(adapter);
        }

        public JarEntryFile getEntry()
        {
            return entry;
        }

        public boolean equals(Object obj)
        {
            if (obj == null)
                return false;

            if (!(obj instanceof JarEntryFileWrapper))
                return false;

            return this.toString().equals(((JarEntryFileWrapper) obj).toString());
        }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public String toString()
        {
            return entry.toString();
        }
    }

    public static String getJarPath(JarEntryFileWrapper entry)
    {
        String content = entry.toString();
        int stop = content.indexOf("::");
        return content.substring(start, stop);
    }

    public static String getPackageName(JarEntryFileWrapper entry)
    {
        String content = entry.toString();
        int start = content.indexOf("::");
        content = content.substring(start + 2, content.length() - 1);
        int nameLength = entry.getName().length();
        if (content.length() == nameLength)
            return "";
        int stop = content.lastIndexOf('/');
        return StringUtils.replace(content.substring(0, stop), "/", ".");
    }

    public static IPackageFragmentRoot getPackageFragmentRoot(IJavaProject project,
            JarEntryFileWrapper entry) throws CoreException
    {
        return getPackageFragmentRoot(project, entry, true);
    }

    private static IPackageFragmentRoot getPackageFragmentRoot(IJavaProject project,
            JarEntryFileWrapper entry, boolean includeOtherProjects) throws CoreException
    {
        String path = getJarPath(entry);
        IPackageFragmentRoot[] roots = includeOtherProjects ? project.getAllPackageFragmentRoots()
                : project.getPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++)
        {
            if (roots[i] instanceof JarPackageFragmentRoot)

                if (((JarPackageFragmentRoot) roots[i]).getJar().getName().equals(path))
                    return roots[i];

        }
        return null;
    }

    public static IPackageFragment getPackageFragment(IJavaProject project,
            JarEntryFileWrapper entry) throws CoreException
    {
        return getPackageFragment(project, entry, true);
    }

    private static IPackageFragment getPackageFragment(IJavaProject project,
            JarEntryFileWrapper entry, boolean includeOtherProjects) throws CoreException
    {
        IPackageFragmentRoot root = getPackageFragmentRoot(project, entry, includeOtherProjects);
        if (root == null)
            return null;

        String packageName = getPackageName(entry);
        IJavaElement[] elements = root.getChildren();
        for (int i = 0; i < elements.length; i++)
        {
            if (elements[i].getElementType() != IJavaElement.PACKAGE_FRAGMENT)
                continue;

            if (elements[i].getElementName().equals(packageName))
                return (IPackageFragment) elements[i];
        }
        return null;
    }

    public static IPackageFragment[] getPackageFragments(IWorkspaceRoot root,
            JarEntryFileWrapper entry) throws CoreException
    {
        ArrayList result = new ArrayList();
        IProject[] projects = root.getProjects();
        for (int i = 0; i < projects.length; i++)
        {
            if (!projects[i].isOpen() || !projects[i].hasNature(JavaCore.NATURE_ID))
                continue;

            IPackageFragment frag = getPackageFragment(JavaCore.create(projects[i]), entry, false);
            if (frag != null)
                result.add(frag);
        }

        return (IPackageFragment[]) result.toArray(new IPackageFragment[result.size()]);
    }

    public static boolean inputsEqual(IStorageEditorInput lhs, IStorageEditorInput rhs)
            throws CoreException
    {
        // if (lhs.equals(rhs)) can't do this with JarEditorInput as it calls JarEntry.equals()
        // return true;
        IStorage left = lhs.getStorage();
        IStorage right = rhs.getStorage();

        boolean leftIsResource = left instanceof IResource;
        boolean rightIsResource = right instanceof IResource;

        if (leftIsResource && rightIsResource)
            return left.equals(right);

        if ((!leftIsResource && rightIsResource) || (leftIsResource && !rightIsResource))
            return false;

        left = wrap(left, true);
        right = wrap(right, true);

        if (left != null && right != null)
            return left.equals(right);

        return false;
    }
}