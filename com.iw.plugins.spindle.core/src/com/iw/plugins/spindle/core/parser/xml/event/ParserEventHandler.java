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

import java.util.Stack;

public class ParserEventHandler
{

    private Stack stack;
    private SimpleXMLEventInfo currentAttribute;
    /**
     * Constructor for LocationHandler.
     */
    public ParserEventHandler()
    {
        stack = new Stack();
    }

    public void startTagBegin(int lineNumber, int columnNumber)
    {
        ElementXMLEventInfo newInfo = new ElementXMLEventInfo();
        newInfo.startTagLocation = new SimpleXMLEventInfo();
        newInfo.startTagLocation.setBeginValues(lineNumber, columnNumber);
        stack.push(newInfo);
    }

    public void startTagEnd(int lineNumber, int columnNumber)
    {
        ElementXMLEventInfo info = (ElementXMLEventInfo) peekCurrent();
        info.startTagLocation.setEndValues(lineNumber, columnNumber);
    }

    public void endTagBegin(int lineNumber, int columnNumber)
    {
        ElementXMLEventInfo info = (ElementXMLEventInfo) peekCurrent();
        info.endTagLocation = new SimpleXMLEventInfo();
        info.endTagLocation.setBeginValues(lineNumber, columnNumber);
    }

    public void endTagEnd(int lineNumber, int columnNumber)
    {
        ElementXMLEventInfo info = (ElementXMLEventInfo) peekCurrent();
        info.endTagLocation.setEndValues(lineNumber, columnNumber);
    }

    public void attributeBegin(String rawname, int lineNumber, int columnNumber)
    {
        if (!stack.isEmpty())
        {
            currentAttribute = new SimpleXMLEventInfo();
            currentAttribute.setBeginValues(lineNumber, columnNumber);
            peekCurrent().getAttributeMap().put(rawname, currentAttribute);
        }
    }

    public void attributeEnd(int lineNumber, int columnNumber)
    {
        if (currentAttribute != null)
        {
            currentAttribute.setEndValues(lineNumber, columnNumber);
        } else
        {
            System.err.print("peh:");
        }
    }
    
    /**
     * 
     */
    public void emptyAttribute()
    {
        if (currentAttribute != null)
        {
            currentAttribute.setEndValues(currentAttribute.getBeginLineNumber(), currentAttribute.getBeginColumnNumber()+1);
        } else
        {
            System.err.print("peh2:");
        }
        
    }

    public ElementXMLEventInfo popCurrent()
    {
        return (ElementXMLEventInfo) stack.pop();
    }

    public ElementXMLEventInfo peekCurrent()
    {
        return (ElementXMLEventInfo) stack.peek();
    }



}
