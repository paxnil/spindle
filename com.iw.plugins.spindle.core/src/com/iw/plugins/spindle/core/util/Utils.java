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
                if (superClasses[i].equals(baseType))
                {
                    match = true;
                }
            }
        }
        return match;
    }

    public static IResource toResource(IResourceLocation loc)
    {
        try
        {
            IResourceWorkspaceLocation use_loc = (IResourceWorkspaceLocation) loc;
            if (!use_loc.exists())
            {
                return null;
            }
            return (IResource) use_loc.getStorage();
        } catch (RuntimeException e)
        {
            return null;
        }
    }

}
