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
import org.eclipse.jdt.core.IType;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;

public class ComponentFactory {

  private ComponentFactory() {
  }

  public static IFile createComponent(
    IPackageFragmentRoot root,
    IPackageFragment pack,
    String componentName,
    IType specClass,
    IProgressMonitor monitor)
    throws CoreException, InterruptedException {

    monitor.beginTask(MessageUtil.getFormattedString("ApplicationFactory.operationdesc", componentName), 10);
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
    IFile file = folder.getFile(new Path(componentName + ".jwc"));

    String qualifiedSpecClassname = specClass.getFullyQualifiedName();
    InputStream contents =
      new ByteArrayInputStream(
        getComponentContent(qualifiedSpecClassname).getBytes());
    file.create(contents, false, new SubProgressMonitor(monitor, 1));
    monitor.worked(1);
    monitor.done();
    return file;
  }
  
  static private String getComponentContent(String qualifiedSpecClassname) {
  	PluginComponentSpecification newSpec = new PluginComponentSpecification();
  	newSpec.setPublicId(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID);
  	newSpec.setAllowBody(true);
  	newSpec.setAllowInformalParameters(true);
  	newSpec.setComponentClassName(qualifiedSpecClassname);
  	StringWriter swriter = new StringWriter();
  	PrintWriter pwriter = new PrintWriter(swriter);
  	newSpec.write(pwriter);
  	return swriter.toString();
  }
}

