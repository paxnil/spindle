/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@intelligentworks.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.wizards.factories;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.tapestry.parse.SpecificationParser;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.preference.IPreferenceStore;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginPageDeclaration;
import com.iw.plugins.spindle.core.util.IndentingWriter;
import com.iw.plugins.spindle.core.util.XMLUtil;

public class ApplicationFactory
{

  static private final String APP_NAME = "APP_NAME";
  static private final String ENGINE_CLASS = "ENGINE_CLASS";
  static private final String PAGE_PATH = "PAGE_PATH";

  private ApplicationFactory()
  {
  }

  static public IFile createApplication(
      IPackageFragmentRoot root,
      IPackageFragment pack,
      String appname,
      IType engineClass,
      IProgressMonitor monitor) throws CoreException, InterruptedException
  {

    String qualifiedEngineClassname = engineClass.getFullyQualifiedName();
    return createApplication(root, pack, appname, qualifiedEngineClassname, monitor);

  }

  static public IFile createApplication(
      IPackageFragmentRoot root,
      IPackageFragment pack,
      String appname,
      String qualifiedEngineClassname,
      IProgressMonitor monitor) throws CoreException, InterruptedException
  {

    monitor
        .beginTask(UIPlugin.getString("ApplicationFactory.operationdesc", appname), 10);
    if (pack == null)
    {
      pack = root.getPackageFragment("");
    }
    if (!pack.exists())
    {
      String packName = pack.getElementName();
      pack = root.createPackageFragment(packName, true, null);
      pack.save(new SubProgressMonitor(monitor, 1), true);
    }
    monitor.worked(1);
    IContainer folder = (IContainer) pack.getUnderlyingResource();
    IFile file = folder.getFile(new Path(appname + ".application"));

    InputStream contents = new ByteArrayInputStream(getApplicationContent(
        appname,
        qualifiedEngineClassname,
        pack.getElementName()).getBytes());
    file.create(contents, false, new SubProgressMonitor(monitor, 1));
    monitor.worked(1);
    monitor.done();
    return file;
  }

  static private String getApplicationContent(
      String appname,
      String qualifiedEngineClassname,
      String packageFragment) throws CoreException, InterruptedException
  {

    PluginApplicationSpecification appSpec = new PluginApplicationSpecification();
    appSpec.setPublicId(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID);
    appSpec.setName(appname);
    appSpec.setEngineClassName(qualifiedEngineClassname);
    String path = "/" + packageFragment.replace('.', '/') + "/pages/Home.page";
    appSpec.addPageDeclaration(new PluginPageDeclaration("Home", path, null));
    IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
    boolean useTabs = store.getBoolean(PreferenceConstants.FORMATTER_USE_TABS_TO_INDENT);
    int tabSize = store.getInt(PreferenceConstants.EDITOR_DISPLAY_TAB_WIDTH);
    StringWriter swriter = new StringWriter();
    IndentingWriter iwriter = new IndentingWriter(swriter, useTabs, tabSize, 0, null);
    XMLUtil.writeApplicationSpecification(iwriter, appSpec, 0);
    return swriter.toString();
    /*
     * StringReplacer replacer = new
     * StringReplacer(UIPlugin.getResourceFile("Templates.application"));
     * replacer.replace(APP_NAME, appname); replacer.replace(ENGINE_CLASS,
     * qualifiedEngineClassname); replacer.replace(PAGE_PATH, path); return
     * replacer.toString();
     */
  }

}