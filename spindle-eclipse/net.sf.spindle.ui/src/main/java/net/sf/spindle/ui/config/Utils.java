/******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package net.sf.spindle.ui.config;

import java.io.IOException;
import java.io.InputStream;

import net.sf.spindle.ui.UIPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.Servlet;
import org.eclipse.jst.j2ee.webapplication.ServletMapping;
import org.eclipse.jst.j2ee.webapplication.ServletType;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;

public final class Utils
{
    /**
     * Returns the location of the web project's WEB-INF/lib directory.
     *
     * @param pj the web project
     * @return the location of the WEB-INF/lib directory
     */

    public static IFolder getWebInfLibDir( final IProject pj )
    {
        final IVirtualComponent vc = ComponentCore.createComponent( pj );
        final IVirtualFolder vf = vc.getRootFolder().getFolder( "WEB-INF/lib" );
        return (IFolder) vf.getUnderlyingFolder();
    }

    /**
     * Copies a resource from within the FormGen plugin to a destination in
     * the workspace.
     *
     * @param src the path of the resource within the plugin
     * @param dest the destination path within the workspace
     */

    public static void copyFromPlugin( final IPath src,
                                       final IFile dest )

        throws CoreException

    {
        try
        {
            final InputStream in = UIPlugin.getInstance().openStream( src );
            dest.create( in, true, null );
        }
        catch( IOException e )
        {
            throw new CoreException( UIPlugin.createErrorStatus( e.getMessage(), e ) );
        }
    }

    /**
     * Adds entries in the web.xml file for the FormGen servlet. Uses the
     * "*.form" for the URL pattern.
     *
     * @param pj the web application that the servlet is part of
     */

    public static void registerFormGenServlet( final IProject pj )
    {
        registerFormGenServlet( pj, "*.form" );
    }
    /**
     * Adds entries in the web.xml file for the FormGen servlet.
     *
     * @param pj the web application that the servlet is part of
     * @param urlPattern the url pattern for the servlet
     */

    public static void registerFormGenServlet( final IProject pj,
                                               final String urlPattern )
    {
        final WebArtifactEdit artifact
            = WebArtifactEdit.getWebArtifactEditForWrite( pj );

        final WebApp root = artifact.getWebApp();

        final Servlet servlet = WebapplicationFactory.eINSTANCE.createServlet();
        final ServletType servletType = WebapplicationFactory.eINSTANCE.createServletType();
        servletType.setClassName( "com.formgen.core.FormGenServlet" );
        servlet.setWebType( servletType );
        servlet.setServletName( "FormGenServlet" );
        root.getServlets().add( servlet );

        final ServletMapping mapping
            = WebapplicationFactory.eINSTANCE.createServletMapping();

        mapping.setServlet( servlet );
        mapping.setUrlPattern( urlPattern );
        root.getServletMappings().add( mapping );

        artifact.saveIfNecessary( null );
    }

}
