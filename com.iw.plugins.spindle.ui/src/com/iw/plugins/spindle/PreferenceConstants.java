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

package com.iw.plugins.spindle;

import com.iw.plugins.spindle.editors.assist.usertemplates.XMLFileContextType;

/**
 * Preference constants used by Spindle
 * 
 * @author glongman@intelligentworks.com
 *  
 */
public interface PreferenceConstants
{

  String AUTO_ACTIVATE_CONTENT_ASSIST = UIPlugin.PLUGIN_ID
      + ".AUTO_ACTIVATE_CONTENT_ASSIST";

  String P_HTML_TO_GENERATE = UIPlugin.PLUGIN_ID + ".P_HTML_TO_GENERATE";

  String EDITOR_USE_SMART_INDENT = UIPlugin.PLUGIN_ID + ".EDITOR_USE_SMART_INDENT";

  String TEMPLATE_EDITOR_HTML_SHOW_XHTML = UIPlugin.PLUGIN_ID
      + ".TEMPLATE_EDITOR_HTML_SHOW_XHTML_STRICT";

  String FORMATTER_PRESERVE_BLANK_LINES = UIPlugin.PLUGIN_ID
      + ".FORMATTER_PRESERVE_BLANK_LINES";

  String FORMATTER_MAX_LINE_LENGTH = UIPlugin.PLUGIN_ID + ".FORMATTER_MAX_LINE_LENGTH";

  String FORMATTER_WRAP_LONG = UIPlugin.PLUGIN_ID + ".FORMATTER_WRAP_LONG";

  String FORMATTER_ALIGN = UIPlugin.PLUGIN_ID + ".FORMATTER_ALIGN";

  String FORMATTER_TAB_SIZE = UIPlugin.PLUGIN_ID + ".EDITOR_DISPLAY_TAB_WIDTH";

  String FORMATTER_TAB_CHAR = UIPlugin.PLUGIN_ID + ".FORMATTER_USE_TABS_TO_INDENT";

  String APP_TEMPLATE = XMLFileContextType.APPLICATION_FILE_CONTEXT_TYPE;
  String LIB_TEMPLATE = XMLFileContextType.LIBRARY_FILE_CONTEXT_TYPE;
  String PAGE_TEMPLATE = XMLFileContextType.PAGE_FILE_CONTEXT_TYPE;
  String COMPONENT_TEMPLATE = XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE;
  String TAP_TEMPLATE_TEMPLATE = XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE;

}