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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.wizards.project;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.ClasspathEntry;

import com.iw.plugins.spindle.TapestryPlugin;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class NewProjectUtils {

  public static void fixClasspath(IJavaProject jproject, IProgressMonitor monitor) throws InterruptedException, CoreException {

    IType checkTapVersion = jproject.findType("net.sf.tapestry.ILibrarySpecification");

    if (checkTapVersion == null) {

      addToClasspath("net.sf.tapestry-2.2.jar", jproject, monitor);

    }

    IType checkServlet = jproject.findType("javax.servlet.http.HttpServlet");

    if (checkServlet == null) {

      addToClasspath("javax.servlet.jar", jproject, monitor);

    }

  }

  private static void addToClasspath(String jarFileName, IJavaProject jproject, IProgressMonitor monitor)
    throws InterruptedException, CoreException {

    URL installUrl = TapestryPlugin.getDefault().getDescriptor().getInstallURL();

    URL jarURL = null;
    try {

      jarURL = new URL(installUrl, jarFileName);

      jarURL = Platform.resolve(jarURL);

    } catch (IOException e) {
    }

    if (jarURL == null) {

      return;

    }

    IClasspathEntry[] classpath = jproject.getRawClasspath();

    IClasspathEntry[] newClasspath = new IClasspathEntry[classpath.length + 1];

    System.arraycopy(classpath, 0, newClasspath, 0, classpath.length);

    newClasspath[classpath.length] =
      new ClasspathEntry(
        IPackageFragmentRoot.K_BINARY,
        ClasspathEntry.CPE_LIBRARY,
        new Path(jarURL.getFile()),
        new Path[] {},
        null,
        null,
        null,
        false);

    jproject.setRawClasspath(newClasspath, monitor);

  }

}
