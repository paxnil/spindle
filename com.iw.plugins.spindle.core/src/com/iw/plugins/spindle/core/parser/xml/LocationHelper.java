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

import com.iw.plugins.spindle.core.parser.IOffsetResolver;

public class LocationHelper implements ILocatable {

  int startLine = 0;
  int startColumn = 0;
  int endLine = 0;
  int endColumn = 0;

  /**
   * Constructor for LocationHelper.
   */
  public LocationHelper() {
    super();
  }

  public void setLocation(XMLScanner.LocationItem location) {
    this.startLine = location.fBeginLineNumber;
    this.startColumn = location.fBeginColumnNumber;
    this.endLine = location.fEndLineNumber;
    this.endColumn = location.fEndColumnNumber;
  }

  /**
   * @see com.iw.plugins.spindle.core.parser.xml.ILocatable#getStartLine()
   */
  public int getStartLine() {
    return startLine;
  }

  /**
   * @see com.iw.plugins.spindle.core.parser.xml.ILocatable#getEndLine()
   */
  public int getEndLine() {
    return endColumn;
  }

  /**
   * @see com.iw.plugins.spindle.core.parser.xml.ILocatable#getCharStart(IOffsetResolver)
   */
  public int getCharStart(IOffsetResolver resolver) {
    return resolver.getColumnOffset(startLine, startColumn);
  }

  /**
   * @see com.iw.plugins.spindle.core.parser.xml.ILocatable#getCharEnd(IOffsetResolver)
   */
  public int getCharEnd(IOffsetResolver resolver) {
    int start = getCharStart(resolver);
    int end = resolver.getColumnOffset(endLine, endColumn);
    return Math.max(end, start + 1);
  }

}
