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
import java.net.URL;
import java.util.Locale;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 *  Used for the roots
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class RootLocation implements IResourceWorkspaceLocation
{

    IContainer root;

    public RootLocation(IFolder folder)
    {
        root = folder;
    }

    public RootLocation(IJavaProject project)
    {
        root = project.getProject();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#exists()
     */
    public boolean exists()
    {
        return root.exists();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getStorage()
     */
    public IStorage getStorage()
    {
        
        throw new Error("can't get the storage for a folder!");
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isWorkspaceResource()
     */
    public boolean isWorkspaceResource()
    {
        return root instanceof IContainer;
    }
    
    public IContainer getContainer() {
        return (IContainer) root;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getProject()
     */
    public IProject getProject()
    {
        return root.getProject();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getContents()
     */
    public InputStream getContents() throws CoreException
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getResourceURL()
     */
    public URL getResourceURL()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getName()
     */
    public String getName()
    {
        return root.getName();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getLocalization(java.util.Locale)
     */
    public IResourceLocation getLocalization(Locale arg0)
    {        
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getRelativeLocation(java.lang.String)
     */
    public IResourceLocation getRelativeLocation(String path)
    {
        if (root instanceof IProject) {
            return new ClasspathResourceWorkspaceLocation(JavaCore.create((IProject)root), path);
        } else {
            return new ContextResourceWorkspaceLocation((IFolder)root, path);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getPath()
     */
    public String getPath()
    {       
        return "";
    }

}
