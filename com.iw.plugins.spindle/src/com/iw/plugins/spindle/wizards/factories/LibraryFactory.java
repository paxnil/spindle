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

package com.iw.plugins.spindle.wizards.factories;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import net.sf.tapestry.parse.SpecificationParser;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginLibrarySpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class LibraryFactory {


  /**
   * Method createLibrary.
   * @param root
   * @param pack
   * @param appname
   * @param monitor
   * @return IFile
   */
  public static IFile createLibrary(
    IPackageFragmentRoot root,
    IPackageFragment pack,
    String libraryName,
    IProgressMonitor monitor)
    throws CoreException, InterruptedException {

    monitor.beginTask(
      MessageUtil.getFormattedString("ApplicationFactory.operationdesc", libraryName),
      10);
    if (pack == null) {
      pack = root.getPackageFragment("");
    }
    if (!pack.exists()) {
      String packName = pack.getElementName();
      pack = root.createPackageFragment(packName, true, null);
      pack.save(new SubProgressMonitor(monitor, 1), true);
    }
    monitor.worked(1);
    IContainer folder = (IContainer) pack.getUnderlyingResource();
    IFile file = folder.getFile(new Path(libraryName + ".library"));

    InputStream contents = new ByteArrayInputStream(getLibraryContent().getBytes());
    file.create(contents, false, new SubProgressMonitor(monitor, 1));
    monitor.worked(1);
    monitor.done();
    return file;
  }

  static private String getLibraryContent() throws CoreException, InterruptedException {

    PluginLibrarySpecification librarySpec = new PluginLibrarySpecification();
    librarySpec.setPublicId(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID);
    StringWriter swriter = new StringWriter();
    PrintWriter pwriter = new PrintWriter(swriter);
    librarySpec.write(pwriter);
    return swriter.toString();
  }

}
