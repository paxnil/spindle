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
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.wutka.dtd;

import org.apache.commons.lang.enums.Enum;

/**
 * An easy way to determine the type of a DTDContainer
 * 
 * @author glongman@gmail.com
 * 
 */
public class DTDItemType extends Enum
{
  public static final DTDItemType DTD_NAME = new DTDItemType("DTD_NAME");
  public static final DTDItemType DTD_EMPTY = new DTDItemType("DTD_EMPTY");
  public static final DTDItemType DTD_PCDATA = new DTDItemType("DTD_PCDATA");
  public static final DTDItemType DTD_ANY = new DTDItemType("DTD_ANY");
  public static final DTDItemType DTD_CHOICE = new DTDItemType("DTD_CHOICE");
  public static final DTDItemType DTD_MIXED = new DTDItemType("DTD_MIXED");
  public static final DTDItemType DTD_SEQUENCE = new DTDItemType("DTD_SEQUENCE");

  public DTDItemType(String name)
  {
    super(name);
  }

}