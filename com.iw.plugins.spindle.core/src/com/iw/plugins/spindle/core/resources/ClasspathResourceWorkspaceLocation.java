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
import java.util.Locale;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.util.LocalizedNameGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;

import com.iw.plugins.spindle.core.TapestryCore;

/**
 *  Implementation of IResourceWorkspaceLocation
 *  for resources found within classpath. 
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ClasspathResourceWorkspaceLocation extends AbstractResourceWorkspaceLocation
{

    IPackageFragment fragment;

    public ClasspathResourceWorkspaceLocation(IJavaProject jproject, String path)
    {
        super(path);
        initialize(jproject);
    }

    public ClasspathResourceWorkspaceLocation(IPackageFragment fragment, IStorage storage)
    {
        this(fragment.getJavaProject(), createPath(fragment, storage));
    }

    private static String createPath(IPackageFragment fragment, IStorage storage)
    {
        return "/" + fragment.getElementName().replace('.', '/') + "/" + storage.getName();
    }

    private void initialize(IJavaProject jproject)
    {
        IPath path = getIPath().makeRelative();
        IPath prefix = path.removeLastSegments(1);

        String packageName = prefix.toString();
        packageName = packageName.replace('/', '.');

        IPackageFragment[] fragments = null;
        try
        {
            NameLookup lookup = ((JavaProject) jproject).getNameLookup();
            fragments = lookup.findPackageFragments(packageName, false);

        } catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }

        if (fragments != null && fragments.length >= 0)
        {
            for (int i = 0; i < fragments.length; i++)
            {
                Object[] contents = getNonJavaResources(fragments[i]);

                for (int j = 0; j < contents.length; j++)
                {
                    IStorage storage = (IStorage) contents[i];
                    if (storage.getName().equals(getName()))
                    {
                        fragment = fragments[i];
                        return;
                    }
                }
            }

        }
    }

    public boolean exists()
    {
        return fragment != null;
    }

    private Object[] getNonJavaResources(IPackageFragment fragment)
    {
        try
        {
            return fragment.getNonJavaResources();
        } catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }
        return null;
    }

    private IStorage findStorage(String name)
    {
        if (fragment != null)
        {

            Object[] nonJavaResources = getNonJavaResources(fragment);
            for (int i = 0; i < nonJavaResources.length; i++)
            {
                IStorage storage = (IStorage) nonJavaResources[i];
                if (storage.getName().equals(name))
                {
                    return storage;
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.AbstractResourceWorkspaceLocation#buildNewResourceLocation(java.lang.String)
     */
    protected IResourceLocation buildNewResourceLocation(String path)
    {
        return new ClasspathResourceWorkspaceLocation(fragment.getJavaProject(), path);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getStorage()
     */
    public IStorage getStorage()
    {
        return findStorage(getName());
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isWorkspaceResource()
     */
    public boolean isWorkspaceResource()
    {
        if (fragment != null)
        {
            try
            {
                IPackageFragmentRoot root =
                    (IPackageFragmentRoot) fragment.getJavaProject().getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
                return root.getKind() == IPackageFragmentRoot.K_SOURCE;
            } catch (JavaModelException e)
            {
                TapestryCore.log(e);
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getProject()
     */
    public IProject getProject()
    {
        return fragment == null ? null : fragment.getJavaProject().getProject();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getContents()
     */
    public InputStream getContents() throws CoreException
    {
        IStorage storage = findStorage(getName());
        if (storage != null)
        {
            return storage.getContents();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getLocalization(java.util.Locale)
     */
    public IResourceLocation getLocalization(Locale locale)
    {
        if (fragment != null)
        {

            LocalizedClasspathResourceFinder finder = new LocalizedClasspathResourceFinder();

            String path = getPath();
            String localizedPath = finder.resolve(locale);

            if (localizedPath == null)
            {

                return null;
            }

            if (path.equals(localizedPath))
            {
                return this;
            }

            return new ClasspathResourceWorkspaceLocation(fragment.getJavaProject(), localizedPath);
        }
        return null;
    }

    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder(5591, 1009);

        builder.append(getPath());

        return builder.toHashCode();
    }

    public String toString()
    {
        return "classpath:" + getPath();
    }

    public class LocalizedClasspathResourceFinder
    {

        /**
         *  Resolves the resource, returning a path representing
         *  the closest match (with respect to the provided locale).
         *  Returns null if no match.
         * 
         *  <p>The provided path is split into a base path
         *  and a suffix (at the last period character).  The locale
         *  will provide different suffixes to the base path
         *  and the first match is returned.
         * 
         **/
        Object[] nonJavaResources;

        public LocalizedClasspathResourceFinder()
        {
            nonJavaResources = getNonJavaResources(fragment);
        }

        public String resolve(Locale locale)
        {
            if (nonJavaResources == null || nonJavaResources.length == 0)
            {
                return null;
            }

            IPath path = getIPath();
            String suffix = path.removeFileExtension().lastSegment();

            LocalizedNameGenerator generator = new LocalizedNameGenerator("", locale, suffix);

            while (generator.more())
            {
                String candidate = generator.next();

                if (isExistingResource(candidate))
                {

                    return "/" + fragment.getElementName().replace('.', '/') + "/" + candidate;
                }
            }

            return null;
        }

        private boolean isExistingResource(String candidate)
        {
            return findStorage(candidate) != null;
        }

    }

}
