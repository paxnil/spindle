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
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarEntryFile;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.resources.ClasspathSearch;

/**
 * A factory that adapts IStorages and subtypes into TapestryProjects
 */
public class StorageToTapestryProjectAdapterFactory extends ResourceToTapestryProjectAdapterFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
        if (adaptableObject instanceof IResource)
            return super.getAdapter(adaptableObject, adapterType);
        
        if (!(adaptableObject instanceof JarEntryFile || adapterType != TapestryProject.class))
            return null;

        try
        {
            IProject project = getProjectFor((JarEntryFile) adaptableObject);
            if (project == null)
                return null;
            
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
//      TODO I think this is ignored when added via extension point
        return new Class[]
        { IStorage.class };
    }

    public IProject getProjectFor(JarEntryFile jarFile)
    {
        ClasspathSearch lookup = null;
    
        try
        {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject[] projects = workspace.getRoot().getProjects();
            for (int i = 0; i < projects.length; i++)
            {
                if (!projects[i].isAccessible())
                    continue;
    
                if (lookup == null)
    
                    lookup = new ClasspathSearch();
    
                IJavaProject jproject = JavaCore.create(projects[i]);
                if (jproject == null || !jproject.exists())
                    continue;
    
                lookup.configure(jproject);
                if (lookup.projectContainsJarEntry((JarEntryFile) jarFile))
                    return projects[i];
    
            }
        }
        catch (CoreException jmex)
        {
            jmex.printStackTrace();
        }
        return null;
    }
}