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

package net.sf.spindle.core.spec;

/**
 * Record <property>tags in a document
 * 
 * These can only be validated at the time the document is parsed/scanned.
 * 
 * @author glongman@gmail.com

 */
public class PluginPropertyDeclaration extends BaseSpecification
{
  String fKey;
  String fValue;
  boolean fValueIsFromAttribute;

  public PluginPropertyDeclaration(String key, String value)
  {
    super(BaseSpecification.PROPERTY_DECLARATION);
    setKey(key);
    fValue = value;
  }

  public String getKey()
  {
    return getIdentifier();
  }

  public void setKey(String key)
  {
    setIdentifier(key);
  }

  public String getValue()
  {
    return fValue;
  }

  /**
   * @return
   */
  public boolean isValueIsFromAttribute()
  {
    return fValueIsFromAttribute;
  }

  /**
   * @param b
   */
  public void setValueIsFromAttribute(boolean flag)
  {
    fValueIsFromAttribute = flag;
  }

}