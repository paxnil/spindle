package com.iw.plugins.spindle.core.parser;
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
 *  Exception thrown if there is any kind of error parsing the
 *  an XML document. 
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */

public class DocumentParseException extends Exception {

  private int severity;
  private int lineNumber;
  private int charStart;
  private int charEnd;

  public DocumentParseException(
    String message,
    int severity,
    int lineNumber,
    int charStart,
    int charEnd,
    Throwable cause) {
    super(message, cause);
    this.severity = severity;
    this.lineNumber = lineNumber;
    this.charStart = charStart;
    this.charEnd = charEnd;
  }

  /**
   * Returns the charEnd.
   * @return Integer
   */
  public int getCharEnd() {
    return charEnd;
  }

  /**
   * Returns the charStart.
   * @return Integer
   */
  public int getCharStart() {
    return charStart;
  }

  /**
   * Returns the lineNumber.
   * @return Integer
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Returns the severity.
   * @return Integer
   */
  public int getSeverity() {
    return severity;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object arg0) {
    if (arg0 == null) {
      return false;
    }
    if (arg0.getClass() == this.getClass()) {
      DocumentParseException other = (DocumentParseException) arg0;
      return this.lineNumber == other.lineNumber
        && this.charStart == other.charStart
        && this.charEnd == other.charEnd
        && this.severity == other.severity
        && this.getMessage().equals(other.getMessage());
    }
    return false;

  }

}
