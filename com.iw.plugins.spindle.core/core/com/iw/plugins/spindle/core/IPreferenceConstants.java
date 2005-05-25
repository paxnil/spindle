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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
/**
 * Preference contstants for the Core plugin.
 * 
 * @author glongman@gmail.com
 * 
 */
public interface IPreferenceConstants
{
  String CACHE_GRAMMAR_PREFERENCE = TapestryCore.IDENTIFIER + ".cachinggrammars";
  String BUILDER_MARKER_MISSES = TapestryCore.IDENTIFIER + ".BUILDER_MARKER_MISSES";
  String BUILDER_HANDLE_ASSETS = TapestryCore.IDENTIFIER + ".BUILDER_HANDLE_ASSETS";

  String CORE_STATUS_INFO = "info";
  String CORE_STATUS_WARN = "warn";
  String CORE_STATUS_ERROR = "error";
  String CORE_STATUS_IGNORE = "ignore";

  String[] CORE_STATUS_ARRAY = new String[]{CORE_STATUS_INFO, CORE_STATUS_WARN,
      CORE_STATUS_ERROR, CORE_STATUS_IGNORE};
  
  String TAPESTRY_LIBRARY = TapestryCore.IDENTIFIER+".tapestryLibrary";

}