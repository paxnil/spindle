package com.iw.plugins.spindle.core;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class TapestryCore extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "com.iw.plugins.spindle.core";
  public static final String NATURE_ID = PLUGIN_ID + ".tapestrynature";
  public static final String BUILDER_ID = PLUGIN_ID + ".tapestrybuilder";
  
  //The shared instance.
  private static TapestryCore plugin;
  //Resource bundle.
  private ResourceBundle resourceBundle;

  /**
   * The constructor.
   */
  public TapestryCore(IPluginDescriptor descriptor) {
    super(descriptor);
    plugin = this;
    try {
      resourceBundle =
        ResourceBundle.getBundle(
          "com.iw.plugins.spindle.core.resources");
    } catch (MissingResourceException x) {
      resourceBundle = null;
    }
  }

  /**
   * Returns the shared instance.
   */
  public static TapestryCore getDefault() {
    return plugin;
  }

  /**
   * Returns the workspace instance.
   */
  public static IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }

  /**
   * Returns the string from the plugin's resource bundle,
   * or 'key' if not found.
   */
  public static String getResourceString(String key) {
    ResourceBundle bundle = TapestryCore.getDefault().getResourceBundle();
    try {
      return bundle.getString(key);
    } catch (MissingResourceException e) {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  static public void log(String msg) {
    ILog log = TapestryCore.getDefault().getLog();
    Status status =
      new Status(
        IStatus.ERROR,
        TapestryCore.getDefault().getDescriptor().getUniqueIdentifier(),
        IStatus.ERROR,
        msg + "\n",
        null);
    log.log(status);
  }

  static public void log(Exception ex) {
    ILog log = TapestryCore.getDefault().getLog();
    StringWriter stringWriter = new StringWriter();
    ex.printStackTrace(new PrintWriter(stringWriter));
    String msg = stringWriter.getBuffer().toString();

    Status status =
      new Status(
        IStatus.ERROR,
        TapestryCore.getDefault().getDescriptor().getUniqueIdentifier(),
        IStatus.ERROR,
        msg,
        null);
    log.log(status);
  }
  public static void addNatureToProject(IProject project, String natureId) throws CoreException {
    IProject proj = project.getProject(); // Needed if project is a IJavaProject
    IProjectDescription description = proj.getDescription();
    String[] prevNatures = description.getNatureIds();

    int natureIndex = -1;
    for (int i = 0; i < prevNatures.length; i++) {
      if (prevNatures[i].equals(natureId)) {
        natureIndex = i;
        i = prevNatures.length;
      }
    }

    // Add nature only if it is not already there
    if (natureIndex == -1) {
      String[] newNatures = new String[prevNatures.length + 1];
      System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
      newNatures[prevNatures.length] = natureId;
      description.setNatureIds(newNatures);
      proj.setDescription(description, null);
    }
  }

  public static void removeNatureToProject(IProject project, String natureId)
    throws CoreException {
    IProject proj = project.getProject(); // Needed if project is a IJavaProject
    IProjectDescription description = proj.getDescription();
    String[] prevNatures = description.getNatureIds();

    int natureIndex = -1;
    for (int i = 0; i < prevNatures.length; i++) {
      if (prevNatures[i].equals(natureId)) {
        natureIndex = i;
        i = prevNatures.length;
      }
    }

    // Remove nature only if it exists...
    if (natureIndex != -1) {
      String[] newNatures = new String[prevNatures.length - 1];
      System.arraycopy(prevNatures, 0, newNatures, 0, natureIndex);
      System.arraycopy(
        prevNatures,
        natureIndex + 1,
        newNatures,
        natureIndex,
        prevNatures.length - (natureIndex + 1));
      description.setNatureIds(newNatures);
      proj.setDescription(description, null);
    }
  }

}
