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

package com.iw.plugins.spindle.core.classpath;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.ClasspathEntry;

import com.iw.plugins.spindle.core.TapestryCore;

/**
 *  Tapestry library container - resolves a classpath container variable to the Tapestry
 *  Libraries.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class CoreClasspathContainer implements IClasspathContainer
{

    /**
     * Container path used to resolve to this Container
     */
    private IPath fPath = null;

    /**
     * Cache of Tapestry classpath entries per VM install.
     */
    private static IClasspathEntry[] fClasspathEntries = null;

    /**
     * Returns the classpath entries associated with the given VM.
     * 
     * @param plugin
     * @return classpath entries
     */
    private static IClasspathEntry[] getClasspathEntries(TapestryCore plugin)
    {
        if (fClasspathEntries == null)
            fClasspathEntries = computeClasspathEntries(plugin);

        return fClasspathEntries;
    }

    /**
     * Computes the Tapestry framework classpath entries associated with the core plugin.
     * 
     * @param vm
     * @return classpath entries
     */
    private static IClasspathEntry[] computeClasspathEntries(TapestryCore plugin)
    {
        List entries = new ArrayList();
        IPluginDescriptor descriptor = plugin.getDescriptor();
        URL installUrl = descriptor.getInstallURL();

        ILibrary[] libs = descriptor.getRuntimeLibraries();

        for (int i = 0; i < libs.length; i++)
        {
            if (libs[i].getType() == ILibrary.RESOURCE)
                continue;

            IPath path = libs[i].getPath();
            String jarName = path.lastSegment();

            if (jarName.equals("core.jar"))
                continue;
                
            if (jarName.equals("javax.servlet.jar"))
                continue;

            try
            {
                IPath sourceAttachmentPath = null;
                IPath sourceAttachmentRootPath = null;
                if (jarName.startsWith("tapestry-3"))
                {
                    sourceAttachmentPath = getSourceAttachmentPath(installUrl, "tapestry-src.jar");
                } else if (jarName.startsWith("tapestry-contrib"))
                {
                    sourceAttachmentPath = getSourceAttachmentPath(installUrl, "tapestry-contrib-src.jar");
                }

                if (sourceAttachmentPath != null)
                    sourceAttachmentRootPath = new Path("/");

                URL libUrl = new URL(installUrl, path.toString());
                libUrl = Platform.resolve(libUrl);
                entries.add(
                    new ClasspathEntry(
                        IPackageFragmentRoot.K_BINARY,
                        ClasspathEntry.CPE_LIBRARY,
                        new Path(libUrl.getFile()),
                        new Path[] {},
                sourceAttachmentPath,
                sourceAttachmentRootPath,
                        null,
                        false));
            } catch (MalformedURLException e)
            {
                TapestryCore.log(e);
            } catch (IOException e)
            {
                TapestryCore.log(e);
            }

        }

        return (IClasspathEntry[]) entries.toArray(new IClasspathEntry[entries.size()]);
    }

    private static IPath getSourceAttachmentPath(URL installUrl, String srcJar)
        throws MalformedURLException, IOException
    {
        URL temp = new URL(installUrl, srcJar);
        temp = Platform.resolve(temp);
        Path result = new Path(temp.getFile());
        return result;
    }

    /**
     * Constructs a Tapestry classpath container
     * 
     * @param path container path used to resolve this container
     */
    public CoreClasspathContainer(IPath path)
    {
        fPath = path;
    }

    /**
     * @see IClasspathContainer#getClasspathEntries()
     */
    public IClasspathEntry[] getClasspathEntries()
    {
        return getClasspathEntries(TapestryCore.getDefault());
    }

    /**
     * @see IClasspathContainer#getDescription()
     */
    public String getDescription()
    {
        return TapestryCore.getString("core-classpath-container-label");
    }

    /**
     * @see IClasspathContainer#getKind()
     */
    public int getKind()
    {
        return IClasspathContainer.K_APPLICATION;
    }

    /**
     * @see IClasspathContainer#getPath()
     */
    public IPath getPath()
    {
        return fPath;
    }

}