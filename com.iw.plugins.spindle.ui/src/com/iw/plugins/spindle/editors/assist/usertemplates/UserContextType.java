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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.assist.usertemplates;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.iw.plugins.spindle.UIPlugin;

/**
 *  UserContextType
 * 
 * @author glongman@gmail.com
 *  
 */
public class UserContextType extends TemplateContextType
{
  /** This context's id's */
  public static final String USER_CONTEXT_TYPE = UIPlugin.PLUGIN_ID
      + ".templateContextType.user";

  /**
   * Creates a new XML context type.
   */
  public UserContextType()
  {
    addGlobalResolvers();
  }

  private void addGlobalResolvers()
  {
    addResolver(new GlobalTemplateVariables.Cursor());
    addResolver(new GlobalTemplateVariables.WordSelection());
    addResolver(new GlobalTemplateVariables.LineSelection());
    addResolver(new GlobalTemplateVariables.Dollar());
    addResolver(new GlobalTemplateVariables.Date());
    addResolver(new GlobalTemplateVariables.Year());
    addResolver(new GlobalTemplateVariables.Time());
    addResolver(new GlobalTemplateVariables.User());
  }
}