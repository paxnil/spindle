package com.iw.plugins.spindle.xerces.parser.xml.event;
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

public class SimpleXMLEventInfo implements XMLEnityEventInfo
{
  /** Beginning line number. */
  protected int fBeginLineNumber = -1;

  /** Beginning column number. */
  protected int fBeginColumnNumber = -1;

  /** Ending line number. */
  protected int fEndLineNumber = -1;

  /** Ending column number. */
  protected int fEndColumnNumber = -1;

  /** Sets the values of this item. */
  public void setBeginValues(int beginLine, int beginColumn)
  {
    fBeginLineNumber = beginLine;
    fBeginColumnNumber = beginColumn;
  }

  /** Sets the values of this item. */
  public void setEndValues(int endLine, int endColumn)
  {
    fEndLineNumber = endLine;
    fEndColumnNumber = endColumn;
  }

  public void setValues(int beginLine, int beginColumn, int endLine, int endColumn)
  {
    setBeginValues(beginLine, beginColumn);
    setEndValues(endLine, endColumn);
  }

  /** @return the line number of the beginning of this event. */
  public int getBeginLineNumber()
  {
    return fBeginLineNumber;
  }

  /** @return the column number of the beginning of this event. */
  public int getBeginColumnNumber()
  {
    return fBeginColumnNumber;
  }

  /** @return the line number of the end of this event. */
  public int getEndLineNumber()
  {
    return fEndLineNumber;
  }

  /** @return the column number of the end of this event. */
  public int getEndColumnNumber()
  {
    return fEndColumnNumber;
  }

  /** @return a string representation of this object. */
  public String toString()
  {
    StringBuffer str = new StringBuffer();
    str.append(fBeginLineNumber);
    str.append(':');
    str.append(fBeginColumnNumber);
    str.append(':');
    str.append(fEndLineNumber);
    str.append(':');
    str.append(fEndColumnNumber);
    return str.toString();
  }

}