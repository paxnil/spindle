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
package com.iw.plugins.spindle.ui.text;

import org.eclipse.swt.graphics.RGB;

public interface IColorConstants
{

  RGB JWCID = new RGB(187, 0, 94);
  RGB XML_COMMENT = new RGB(128, 0, 0);
  RGB PROC_INSTR = new RGB(128, 128, 128);
  RGB STRING = new RGB(0, 128, 0);
  RGB DEFAULT = new RGB(0, 0, 0);
  RGB TAG = new RGB(0, 0, 128);

  String P_JWCID = "editor.color.jwcid_tag";
  String P_XML_COMMENT = "editor.color.xml_comment";
  String P_PROC_INSTR = "editor.color.instr";
  String P_STRING = "editor.color.string";
  String P_DEFAULT = "editor.color.default";
  String P_TAG = "editor.color.tag";

}