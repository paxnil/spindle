package com.iw.plugins.spindle.core.parser.xml;
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

import org.apache.xerces.dom.AttrNSImpl;
import org.apache.xerces.dom.CoreDocumentImpl;

import com.iw.plugins.spindle.core.parser.IOffsetResolver;

public class AttributeNS extends AttrNSImpl implements ILocatable {

  LocationHelper helper = new LocationHelper();

  public AttributeNS(CoreDocumentImpl ownerDocument, String namespaceURI, String qualifiedName) {
    super(ownerDocument, namespaceURI, qualifiedName);
  }

  public AttributeNS(CoreDocumentImpl arg0, String arg1, String arg2, String arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public AttributeNS(CoreDocumentImpl ownerDocument, String value) {
    super(ownerDocument, value);
  }
  
  public void setLocation(XMLScanner.LocationItem location) {
    helper.setLocation(location);
  }

  /**
   * @see com.iw.plugins.spindle.core.parser.xml.ILocatable#getCharEnd(IOffsetResolver)
   */
  public int getCharEnd(IOffsetResolver resolver) {

    return helper.getCharEnd(resolver);
  }

  /**
   * @see com.iw.plugins.spindle.core.parser.xml.ILocatable#getCharStart(IOffsetResolver)
   */
  public int getCharStart(IOffsetResolver resolver) {
    return helper.getCharStart(resolver);
  }

  /**
   * @see com.iw.plugins.spindle.core.parser.xml.ILocatable#getEndLine()
   */
  public int getEndLine() {
    return helper.getEndLine();
  }

  /**
   * @see com.iw.plugins.spindle.core.parser.xml.ILocatable#getStartLine()
   */
  public int getStartLine() {
    return helper.getStartLine();
  }

}
