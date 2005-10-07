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

package com.iw.plugins.spindle.core.classpath;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;


import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;

import core.CoreMessages;
import core.TapestryCore;

/**
 * Tapestry library container - resolves a classpath container variable to the Tapestry Libraries.
 * 
 * @author glongman@gmail.com
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
    private IClasspathEntry[] fClasspathEntries = null;

    /**
     * Returns the classpath entries associated with the given VM.
     * 
     * @param plugin
     * @return classpath entries
     */
    private IClasspathEntry[] getClasspathEntries(TapestryCorePlugin plugin, IPath containerPath)
    {
        if (fClasspathEntries == null)
        {
            boolean includeContrib = true;
            boolean includePortlet = false;

            String[] segments = containerPath.segments();
            for (int i = 1; i < segments.length; i++)
            {
                if ("contrib".equals(segments[i]))
                    includeContrib = true;

                else if ("portlet".equals(segments[i]))
                    includePortlet = true;
            }

            fClasspathEntries = computeClasspathEntries(
                    plugin.getBundle(),
                    includeContrib,
                    includePortlet);
        }

        return fClasspathEntries;
    }

    /**
     * Computes the Tapestry framework classpath entries associated with the core plugin bundle.
     * 
     * @param bundle
     *            the Bundle associated with the plugin object.
     * @return an array of classpath entries.
     */
    private IClasspathEntry[] computeClasspathEntries(Bundle bundle, final boolean includeContrib,
            final boolean includePortlet)
    {
        List entries = new ArrayList();

        URL installUrl = bundle.getEntry("/");

        try
        {
            URL tapLibUrl = new URL(installUrl, "lib/tapestry");
            tapLibUrl = Platform.resolve(tapLibUrl);

            File tapLibs = new File(new URI(tapLibUrl.toString()));

            if (!tapLibs.exists() && tapLibs.isDirectory())
            {
                TapestryCore
                        .log("Tapestry Framework Library Problem: Unable to locate tapestry jars");
            }
            else
            {
                String[] tapJars = tapLibs.list(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        if (!name.endsWith(".jar"))
                            return false;

                        if (!includeContrib && name.startsWith("tapestry-contrib"))
                            return false;

                        if (!includePortlet && name.startsWith("tapestry-portlet"))
                            return false;

                        return true;
                    }
                });

                collectCPEntries(entries, tapLibs, tapJars);
            }

            URL externalLibsUrl = new URL(tapLibUrl, "ext-package/lib");
            externalLibsUrl = Platform.resolve(externalLibsUrl);

            File externalLibs = new File(new URI(externalLibsUrl.toString()));

            if (!externalLibs.exists() && externalLibs.isDirectory())
            {
                TapestryCore
                        .log("Tapestry Framework Library Problem: Unable to locate external jars");
            }
            else
            {
                String[] externalJars = externalLibs.list(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        if (!name.endsWith(".jar"))
                            return false;

                        return true;
                    }
                });

                collectCPEntries(entries, externalLibs, externalJars);
            }
        }
        catch (Exception e)
        {
            TapestryCore.log(e);
        }
        return (IClasspathEntry[]) entries.toArray(new IClasspathEntry[entries.size()]);
    }

    private static void collectCPEntries(List target, File directory, String[] fileNames)
    {
        IPath srcAttachmentRootPath = new Path("/");
        for (int i = 0; i < fileNames.length; i++)
        {
            IPath jarPath = null;
            IPath srcAttachmentPath = null;

            jarPath = new Path(new File(directory, fileNames[i]).getAbsolutePath());
            File srcFile = new File(directory, "src/"
                    + (jarPath.removeFileExtension().lastSegment() + "-src.jar"));

            if (srcFile.exists())
                srcAttachmentPath = new Path(srcFile.getAbsolutePath());

            target.add(JavaCore.newLibraryEntry(
                    jarPath,
                    srcAttachmentPath,
                    srcAttachmentPath == null ? null : srcAttachmentRootPath,
                    false));

        }
    }

    /**
     * Constructs a Tapestry classpath container
     * 
     * @param path
     *            container path used to resolve this container
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
        return getClasspathEntries(TapestryCorePlugin.getDefault(), fPath);
    }

    /**
     * @see IClasspathContainer#getDescription()
     */
    public String getDescription()
    {
        return CoreMessages.format("core-classpath-container-label");
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