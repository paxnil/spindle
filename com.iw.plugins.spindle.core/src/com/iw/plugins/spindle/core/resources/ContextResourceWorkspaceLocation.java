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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 *  Implementation of IResourceWorkspaceLocation
 *  for resources found within the context of a web application. 
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ContextResourceWorkspaceLocation extends AbstractResourceWorkspaceLocation
{

    private static String findPath(IFolder context, IResource resource)
    {

        IPath contextPath = context.getFullPath();
        IPath resourcePath = resource.getFullPath();
        IPath chopped = resourcePath.removeFirstSegments(contextPath.segmentCount()).makeAbsolute();
        return chopped.toString();
    }

    private IFolder contextRoot;

    /**
     * @param path
     */
    public ContextResourceWorkspaceLocation(IFolder contextRoot, String path)
    {
        super(path);
        this.contextRoot = contextRoot;        
    }

    public ContextResourceWorkspaceLocation(IFolder contextLocation, IResource resource)
    {
        this(contextLocation, findPath(contextLocation, resource));

    }
    
    public boolean exists() {
        return getStorage() != null;
    }

    private IFile getWorkspaceFile(IPath path)
    {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IFile file = root.getFile(path);
        if (file != null && file.exists()) {
            return file;
        }
        return null;
    }

    private IPath getCompletePath()
    {
        return contextRoot.getFullPath().append(getPath());
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.AbstractResourceWorkspaceLocation#buildNewResourceLocation(java.lang.String)
     */
    protected IResourceLocation buildNewResourceLocation(String path)
    {
        return new ContextResourceWorkspaceLocation(contextRoot, path);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getStorage()
     */
    public IStorage getStorage()
    {
        return getWorkspaceFile(getCompletePath());
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isWorkspaceResource()
     */
    public boolean isWorkspaceResource()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getProject()
     */
    public IProject getProject()
    {
        IFile found = getWorkspaceFile(getCompletePath());
        return found.getProject();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getContents()
     */
    public InputStream getContents() throws CoreException
    {
        IFile found = getWorkspaceFile(getCompletePath());
        if (found != null && found.exists())
        {
            return found.getContents();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceLocation#getLocalization(java.util.Locale)
     */
    public IResourceLocation getLocalization(Locale locale)
    {
        LocalizedContextResourceFinder finder = new LocalizedContextResourceFinder();

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

        return new ContextResourceWorkspaceLocation(contextRoot, localizedPath);
    }

    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder(5589, 1117);

        builder.append(getPath());

        return builder.toHashCode();
    }

    public String toString()
    {
        return "context:" + getPath();
    }

    public class LocalizedContextResourceFinder
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

        public String resolve(Locale locale)
        {
            IPath contextPath = contextRoot.getFullPath();
            IPath basePath = contextPath.removeLastSegments(1).addTrailingSeparator();
            IPath temp = contextPath.removeFileExtension();
            String suffix = temp.lastSegment();

            LocalizedNameGenerator generator = new LocalizedNameGenerator(basePath.toString(), locale, suffix);

            while (generator.more())
            {
                String candidatePath = generator.next();

                if (isExistingResource(new Path(candidatePath)))
                    return candidatePath;
            }

            return null;
        }

        private boolean isExistingResource(IPath path)
        {

            IFile found = getWorkspaceFile(path);
            return found != null && found.exists();
        }
    }

}
