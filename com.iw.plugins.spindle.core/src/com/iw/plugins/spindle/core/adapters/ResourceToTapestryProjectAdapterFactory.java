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
package com.iw.plugins.spindle.core.adapters;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;

/**
 * A factory that adapts IStorages and subtypes into TapestryProjects
 */
public class ResourceToTapestryProjectAdapterFactory implements IAdapterFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
        if (!(adaptableObject instanceof IResource) || adapterType != TapestryProject.class)
            return null;

        IResource resource = (IResource) adaptableObject;
        IProject project = null;
        switch (resource.getType())
        {
            case IResource.FILE:
            case IResource.FOLDER:
                project = resource.getProject();
                break;
            case IResource.PROJECT:
                project = (IProject) resource;

            default:
                break;
        }

        if (project == null)
            return null;

        try
        {
            return project.getNature(TapestryCore.NATURE_ID);
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
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList()
    {
        // TODO I think this is ignored when added via extension point
        return new Class[]
        { IResource.class };
    }
}