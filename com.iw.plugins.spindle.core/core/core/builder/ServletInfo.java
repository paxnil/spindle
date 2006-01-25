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
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */

package core.builder;

import java.util.HashMap;
import java.util.Map;

import core.resources.ICoreResource;

public class ServletInfo
{

  String name;
  String classname;
  Map<String, String> parameters = new HashMap<String, String>();
  boolean isServletSubclass;
  ICoreResource applicationSpecLocation;
  public String toString()
  {
    StringBuffer buffer = new StringBuffer("ServletInfo(");
    buffer.append(name);
    buffer.append(")::");
    buffer.append("classname = ");
    buffer.append(classname);
    buffer.append(", params = ");
    buffer.append(parameters);
    buffer.append(" loc= ");
    buffer.append(applicationSpecLocation);
    return buffer.toString();
  }
}