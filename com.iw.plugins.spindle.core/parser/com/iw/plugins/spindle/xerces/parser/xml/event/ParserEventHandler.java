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

import java.util.Stack;

public class ParserEventHandler
{

  private Stack fStack;
  private SimpleXMLEventInfo fCurrentAttribute;
  /**
   * Constructor for LocationHandler.
   */
  public ParserEventHandler()
  {
    fStack = new Stack();
  }

  public void startTagBegin(int lineNumber, int columnNumber)
  {
    ElementXMLEventInfo newInfo = new ElementXMLEventInfo();
    newInfo.fStartTagLocation = new SimpleXMLEventInfo();
    newInfo.fStartTagLocation.setBeginValues(lineNumber, columnNumber);
    fStack.push(newInfo);
  }

  public void startTagEnd(int lineNumber, int columnNumber)
  {
    ElementXMLEventInfo info = (ElementXMLEventInfo) peekCurrent();
    info.fStartTagLocation.setEndValues(lineNumber, columnNumber);
  }

  public void endTagBegin(int lineNumber, int columnNumber)
  {
    ElementXMLEventInfo info = (ElementXMLEventInfo) peekCurrent();
    info.fEndTagLocation = new SimpleXMLEventInfo();
    info.fEndTagLocation.setBeginValues(lineNumber, columnNumber);
  }

  public void endTagEnd(int lineNumber, int columnNumber)
  {
    ElementXMLEventInfo info = (ElementXMLEventInfo) peekCurrent();
    info.fEndTagLocation.setEndValues(lineNumber, columnNumber);
  }

  public void attributeBegin(String rawname, int lineNumber, int columnNumber)
  {
    if (!fStack.isEmpty())
    {
      fCurrentAttribute = new SimpleXMLEventInfo();
      fCurrentAttribute.setBeginValues(lineNumber, columnNumber);
      peekCurrent().getAttributeMap().put(rawname, fCurrentAttribute);
    }
  }

  public void attributeEnd(int lineNumber, int columnNumber)
  {
    if (fCurrentAttribute != null)
    {
      fCurrentAttribute.setEndValues(lineNumber, columnNumber);
    }
  }

  /**
   *  
   */
  public void emptyAttribute()
  {
    if (fCurrentAttribute != null)
    {
      fCurrentAttribute.setEndValues(
          fCurrentAttribute.getBeginLineNumber(),
          fCurrentAttribute.getBeginColumnNumber() + 1);
    }

  }

  public ElementXMLEventInfo popCurrent()
  {
    return (ElementXMLEventInfo) fStack.pop();
  }

  public ElementXMLEventInfo peekCurrent()
  {
    return (ElementXMLEventInfo) fStack.peek();
  }

}