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
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.editors.assist.usertemplates;
import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;

import com.iw.plugins.spindle.UIPlugin;

public class UserTemplateAccess
{
  /** Key to store custom templates. */
  private static final String CUSTOM_TEMPLATES_KEY = UIPlugin.PLUGIN_ID
      + ".customtemplates";

  /** The shared instance. */
  private static UserTemplateAccess fgInstance;

  /** The template store. */
  private TemplateStore fStore;

  /** The context type registry. */
  private ContributionContextTypeRegistry fRegistry;

  private UserTemplateAccess()
  {
  }

  /**
   * Returns the shared instance.
   * 
   * @return the shared instance
   */
  public static UserTemplateAccess getDefault()
  {
    if (fgInstance == null)
    {
      fgInstance = new UserTemplateAccess();
    }
    return fgInstance;
  }

  /**
   * Returns this plug-in's template store.
   * 
   * @return the template store of this plug-in instance
   */
  public TemplateStore getTemplateStore()
  {
    if (fStore == null)
    {
      fStore = new ContributionTemplateStore(getContextTypeRegistry(), UIPlugin
          .getDefault()
          .getPreferenceStore(), CUSTOM_TEMPLATES_KEY);
      try
      {
        fStore.load();
      } catch (IOException e)
      {
        UIPlugin.log(e);
      }
    }
    return fStore;
  }

  /**
   * Returns this plug-in's context type registry.
   * 
   * @return the context type registry for this plug-in instance
   */
  public ContextTypeRegistry getContextTypeRegistry()
  {
    if (fRegistry == null)
    {
      // create and configure the contexts available in the template editor
      fRegistry = new ContributionContextTypeRegistry();
      fRegistry.addContextType(XMLFileContextType.APPLICATION_FILE_CONTEXT_TYPE);
      fRegistry.addContextType(XMLFileContextType.LIBRARY_FILE_CONTEXT_TYPE);
      fRegistry.addContextType(XMLFileContextType.PAGE_FILE_CONTEXT_TYPE);
      fRegistry.addContextType(XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE);
      fRegistry.addContextType(XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE);
      fRegistry.addContextType(UserContextType.USER_CONTEXT_TYPE);
    }
    return fRegistry;
  }

  public IPreferenceStore getPreferenceStore()
  {
    return UIPlugin.getDefault().getPreferenceStore();
  }

  public void savePluginPreferences()
  {
    UIPlugin.getDefault().savePluginPreferences();
  }
}