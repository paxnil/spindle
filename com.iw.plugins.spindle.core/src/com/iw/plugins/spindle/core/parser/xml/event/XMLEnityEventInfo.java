package com.iw.plugins.spindle.core.parser.xml.event;
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

/**
 * This interface is used to carry location information
 * 
 * 
 */
public interface XMLEnityEventInfo
{

  /** @return the line number of the beginning of this event. */
  public int getBeginLineNumber();

  /** @return the column number of the beginning of this event. */
  public int getBeginColumnNumber();

  /** @return the line number of the end of this event. */
  public int getEndLineNumber();

  /** @return the column number of the end of this event. */
  public int getEndColumnNumber();

}