package com.iw.plugins.spindle.core.util;
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

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 * A Utility class
 *
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class Utils
{

    /**
     * Answer true iff the candidate type is a subclass of the base Type
     * @param candidate the supposed subclass of the base Type
     * @param baseType the supposed superclass of the candidate Type
     * @return true iff the candidate type is a subclass of the base Type
     * @throws JavaModelException
     */
    public static boolean extendsType(IType candidate, IType baseType) throws JavaModelException
    {
        Assert.isNotNull(candidate);
        Assert.isNotNull(baseType);

        boolean match = false;
        ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
        if (hierarchy.exists())
        {
            IType[] superClasses = hierarchy.getAllSupertypes(candidate);
            for (int i = 0; i < superClasses.length; i++)
            {
                match = superClasses[i].equals(baseType);
                if (match)
                    break;

            }
        }
        return match;
    }

    /**
     * Answer true iff the IResourceLocation in question referes to a workbench resource
     * @param loc the IResourceLocation in question
     * @return true iff the location in question referes to a workbench resource (IResource)
     */
    public static IResource toResource(IResourceLocation loc)
    {
        try
        {
            IResourceWorkspaceLocation use_loc = (IResourceWorkspaceLocation) loc;
            if (!use_loc.exists())
                return null;

            return (IResource) use_loc.getStorage();
        } catch (RuntimeException e)
        {
            return null;
        }
    }

    /**
     * Answers true iff the candidate type implements the interface
     * @param candidate IType the candidate
     * @param interfaceName String the fully qualified name of an interface
     * @return true iff the candidate type implements the interface.
     * @throws JavaModelException
     */
    public static boolean implementsInterface(IType candidate, String interfaceName) throws JavaModelException
    {
        boolean match = false;
        String[] superInterfaces = candidate.getSuperInterfaceNames();
        if (superInterfaces != null && superInterfaces.length > 0)
        {
            for (int i = 0; i < superInterfaces.length; i++)
            {
                if (candidate.isBinary() && interfaceName.endsWith(superInterfaces[i]))
                {
                    match = true;
                } else
                {
                    match = interfaceName.equals(superInterfaces[i]);
                }
            }
        } else
        {
            match = false;
        }
        return match;
    }

    /**
     * Evaluates if a member (possible from another package) is visible from
     * elements in a package.
     * @param member The member to test the visibility for
     * @param pack The package in focus
     */
    public static boolean isVisible(IMember member, IPackageFragment pack) throws JavaModelException
    {
        int otherflags = member.getFlags();

        if (Flags.isPublic(otherflags) || Flags.isProtected(otherflags))
        {
            return true;
        } else if (Flags.isPrivate(otherflags))
        {
            return false;
        }

        IPackageFragment otherpack = (IPackageFragment) findElementOfKind(member, IJavaElement.PACKAGE_FRAGMENT);
        return (pack != null && pack.equals(otherpack));
    }

    /**
     * Returns the first java element that conforms to the given type walking the
     * java element's parent relationship. If the given element alrady conforms to
     * the given kind, the element is returned.
     * Returns <code>null</code> if no such element exits.
     */
    public static IJavaElement findElementOfKind(IJavaElement element, int kind)
    {
        while (element != null && element.getElementType() != kind)
            element = element.getParent();
        return element;
    }

    /**
     * Returns true if the element is on the build path of the given project
     */
    public static boolean isOnBuildPath(IJavaProject jproject, IJavaElement element) throws JavaModelException
    {
        IPath rootPath;
        if (element.getElementType() == IJavaElement.JAVA_PROJECT)
        {
            rootPath = ((IJavaProject) element).getProject().getFullPath();
        } else
        {
            IPackageFragmentRoot root = getPackageFragmentRoot(element);
            if (root == null)
            {
                return false;
            }
            rootPath = root.getPath();
        }
        return jproject.findPackageFragmentRoot(rootPath) != null;
    }

    /**
     * Returns the package fragment root of <code>IJavaElement</code>. If the given
     * element is already a package fragment root, the element itself is returned.
     */
    public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element)
    {
        return (IPackageFragmentRoot) findElementOfKind(element, IJavaElement.PACKAGE_FRAGMENT_ROOT);
    }

  

}
