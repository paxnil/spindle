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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import com.iw.plugins.spindle.core.TapestryCore;

/**
 * Tapestry library container - resolves a classpath container variable to the
 * Tapestry Libraries.
 * 
 * @author glongman@intelligentworks.com
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
      fClasspathEntries = computeClasspathEntries(plugin.getBundle());

    return fClasspathEntries;
  }

  /**
   * Computes the Tapestry framework classpath entries associated with the core
   * plugin bundle.
   * 
   * @param bundle the Bundle associated with the plugin object.
   * @return an array of classpath entries.
   */
  private static IClasspathEntry[] computeClasspathEntries(Bundle bundle)
  {
    List entries = new ArrayList();

    URL installUrl = bundle.getEntry("/");

    try
    {
      ManifestElement[] elements = ManifestElement.parseHeader(
          Constants.BUNDLE_CLASSPATH,
          (String) bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH));

      for (int i = 0; i < elements.length; i++)
      {
        String jarName = elements[i].getValue();

        if (jarName.equals("core.jar"))
          continue;

        if (jarName.endsWith("javax.servlet.jar"))
          continue;

        if (jarName.equals("dtdparser.jar"))
          continue;

        IPath tempPath = new Path(jarName);
        String trueJarName = tempPath.lastSegment();
        try
        {
          IPath sourceAttachmentPath = null;
          IPath sourceAttachmentRootPath = null;
          if (trueJarName.startsWith("tapestry-3"))
          {
            sourceAttachmentPath = getSourceAttachmentPath(installUrl, "tapestry-src.jar");
          } else if (trueJarName.startsWith("tapestry-contrib"))
          {
            sourceAttachmentPath = getSourceAttachmentPath(
                installUrl,
                "tapestry-contrib-src.jar");
          } else
          {

            int index = trueJarName.lastIndexOf('-');
            String attachment = trueJarName.substring(0, index) + "-src.jar";
            sourceAttachmentPath = getSourceAttachmentPath(installUrl, attachment);
          }

          if (sourceAttachmentPath != null)
            sourceAttachmentRootPath = new Path("/");

          URL libUrl = new URL(installUrl, jarName);
          libUrl = Platform.resolve(libUrl);

          entries.add(JavaCore.newLibraryEntry(
              new Path(libUrl.getFile()),
              sourceAttachmentPath,
              sourceAttachmentRootPath,
              false));

        } catch (MalformedURLException e)
        {
          TapestryCore.log(e);
        } catch (IOException e)
        {
          TapestryCore.log(e);
        }

      }

    } catch (BundleException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return (IClasspathEntry[]) entries.toArray(new IClasspathEntry[entries.size()]);
  }

  /**
   * Locate the path of a jar containing the source code for a named library.
   * The location is relative to the plugin install directory.
   * 
   * @param installUrl the URL for the plugin install
   * @param srcJar the name of the expected src jar
   * @return a path to the source jar or null if no such jar is found.
   */
  private static IPath getSourceAttachmentPath(URL installUrl, String srcJar)
  {
    
    IPath path = new Path(srcJar);
    srcJar = path.lastSegment();
    URL temp;
    try
    {
      temp = new URL(installUrl, "libsrc/" + srcJar);
    } catch (MalformedURLException e1)
    {
      return null;
    }
    try
    {
      temp = Platform.resolve(temp);
      Path result = new Path(temp.getFile());
      return result;

    } catch (IOException e)
    {
      // Do nothing
    }
    return null;
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