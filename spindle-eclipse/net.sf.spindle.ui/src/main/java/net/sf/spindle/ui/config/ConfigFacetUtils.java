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
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package net.sf.spindle.ui.config;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.spindle.core.eclipse.TapestryCorePlugin;
import net.sf.spindle.ui.UIPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.Servlet;
import org.eclipse.jst.j2ee.webapplication.ServletMapping;
import org.eclipse.jst.j2ee.webapplication.ServletType;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.osgi.framework.Bundle;

/**
 * Inspired by this article: https://bugs.eclipse.org/bugs/show_bug.cgi?id=113137
 */
public final class ConfigFacetUtils
{
    protected static final List<IPath> CORE_FILES = new ArrayList<IPath>();

    protected static final List<IPath> ANNOTATION_FILES = new ArrayList<IPath>();

    protected static final List<IPath> PORTLET_FILES = new ArrayList<IPath>();

    private static boolean FILES_POPULATED = false;
    /**
     * Returns the location of the web project's WEB-INF/lib directory.
     * 
     * @param project
     *            the web project
     * @return the location of the WEB-INF/lib directory
     */

    public static IFolder getWebInfLibDir(IProject project)
    {
        final IVirtualComponent vc = ComponentCore.createComponent(project);
        final IVirtualFolder vf = vc.getRootFolder().getFolder("WEB-INF/lib");
        return (IFolder) vf.getUnderlyingFolder();
    }

    /**
     * Copies a resource from within a Bundle to a destination in the workspace.
     * 
     * @param bundle
     *            the bundle that is the base for the copy operation
     * @param src
     *            the path of the resource within the plugin
     * @param dest
     *            the destination path within the workspace
     */

    /**
     * @param src
     * @param dest
     * @throws CoreException
     */
    public static void copyFromPlugin(Bundle bundle, IPath src, IFile dest) throws CoreException
    {
        try
        {
            final InputStream in = FileLocator.openStream(bundle, src, false);
            dest.create(in, true, null);
        }
        catch (IOException e)
        {
            throw new CoreException(UIPlugin.createErrorStatus(e.getMessage(), e));
        }
    }

    /**
     * Add entries to web.xml for the Tapestry servlet.
     * 
     * @param project
     *            the web application project to add the servlet to
     * @param appName
     *            the name of the tapestry application
     * @param monitor
     *            a progress monitor
     */
    @SuppressWarnings("unchecked")
    public static void registerTapestryServlet(IProject project, String appName,
            IProgressMonitor monitor)
    {
        final WebArtifactEdit artifact = WebArtifactEdit.getWebArtifactEditForWrite(project);

        final WebApp root = artifact.getWebApp();

        final Servlet servlet = WebapplicationFactory.eINSTANCE.createServlet();
        final ServletType servletType = WebapplicationFactory.eINSTANCE.createServletType();
        servletType.setClassName("org.apache.tapestry.ApplicationServlet");
        servlet.setWebType(servletType);
        servlet.setServletName("appName");
        root.getServlets().add(servlet);

        final ServletMapping mapping = WebapplicationFactory.eINSTANCE.createServletMapping();

        mapping.setServlet(servlet);
        mapping.setUrlPattern("/" + appName);
        root.getServletMappings().add(mapping);

        artifact.saveIfNecessary(monitor);
    }


    public static void uninstallJars(IProject project, List<IPath> files, IProgressMonitor monitor)
            throws CoreException
    {
        if (files.isEmpty())
        {
            populateFileLists(TapestryCorePlugin.getDefault().getBundle());
        }

        IFolder webInfLib = ConfigFacetUtils.getWebInfLibDir(project);
        Assert.isNotNull(webInfLib);
        monitor.beginTask("", files.size());

        try
        {
            for (IPath path : files)
            {
                IFile file = webInfLib.getFile(path.lastSegment());
                if (file.exists())
                    file.delete(true, monitor);
                monitor.worked(1);
            }
        }
        finally
        {
            monitor.done();
        }

    }

    public static void installJars(IProject project, Bundle coreBundle, List<IPath> files,
            IProgressMonitor monitor) throws CoreException
    {
        if (files.isEmpty())
            populateFileLists(coreBundle);

        IFolder webInfLib = ConfigFacetUtils.getWebInfLibDir(project);
        Assert.isNotNull(webInfLib);
        monitor.beginTask("", files.size());
        try
        {
            for (IPath path : files)
            {
                IFile file = webInfLib.getFile(path.lastSegment());
                if (file.exists())
                    continue;

                ConfigFacetUtils.copyFromPlugin(coreBundle, path, file);
                monitor.worked(1);
            }
        }
        finally
        {
            monitor.done();
        }

    }

    private static void populateFileLists(Bundle coreBundle) throws CoreException
    {
        if (FILES_POPULATED)
            return;
        URL libDirUrl = FileLocator.find(coreBundle, new Path("/lib"), null);
        Assert.isNotNull(libDirUrl);

        try
        {            
            File libDir = new File(FileLocator.toFileURL(libDirUrl).toURI());
            Assert.isLegal(
                    libDir.exists() && libDir.isDirectory(),
                    "lib dir does not exist or is not a directory: " + libDirUrl.toString());
            libDir.list(new FilenameFilter()
            {

                public boolean accept(File dir, String name)
                {
                    IPath path = new Path("lib/"+name);
                    String nameLower = name.toLowerCase();
                    if (nameLower.indexOf("xerces") >=0 || nameLower.indexOf("spindle") >= 0 || !"jar".equals(path.getFileExtension()))
                        return false;
                    if (nameLower.startsWith("tapestry-annotations"))
                        ANNOTATION_FILES.add(path);
                    else if (nameLower.startsWith("tapestry-portlet"))
                        PORTLET_FILES.add(path);
                    else
                        CORE_FILES.add(path);
                    return false;
                }
            });
            FILES_POPULATED = true;
        }
        catch (Exception e)
        {
            throw new CoreException(UIPlugin.createErrorStatus(e.getMessage(), e));
        }
       
    }
}
