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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.tapestry.IResourceLocation;

import com.iw.plugins.spindle.core.parser.xml.event.ElementXMLEventInfo;
import com.iw.plugins.spindle.core.parser.xml.event.SimpleXMLEventInfo;
import com.iw.plugins.spindle.core.parser.xml.event.XMLEnityEventInfo;

public class ElementSourceLocationInfo implements  ISourceLocationInfo
{

    private static final int LINE = 0;
    private static final int CHAR_START = 1;
    private static final int CHAR_END = 2;

    private int sourceStartLine = 0;
    private int sourceCharStart = 0;
    private int sourceCharEnd = 0;
    private int sourceEndLine = 0;

    private int startTagStartLine = 0;
    private int startTagCharStart = 0;
    private int startTagCharEnd = 0;
    private int startTagEndLine = 0;

    private int endTagStartLine = 0;
    private int endTagCharStart = 0;
    private int endTagCharEnd = 0;
    private int endTagEndLine = 0;

    private int contentsStartLine = 0;
    private int contentsCharStart = 0;
    private int contentsCharEnd = 0;
    
    private int locationColumnNumber;
    private int locationLineNumber;
    private IResourceLocation resourceLocation;

    private Map attributes;

    public ElementSourceLocationInfo(ElementXMLEventInfo eventInfo, ISourceLocationResolver resolver)
    {
        init(eventInfo, resolver);
    }

    public boolean hasAttributes()
    {
        return attributes != null;
    }

    public boolean isEmptyTag()
    {
        return contentsStartLine < 1;
    }

    /** return a location for the element - includes all wrapped by it**/
    public ISourceLocation getSourceLocation()
    {
        return new SourceLocation(sourceStartLine, sourceCharStart, sourceCharEnd);
    }

    /** return a location for all wrapped by the element**/
    /*
     <pre>
     <start/> - return null
     <start></start> - return a location of the closing > of start tag
     <start>^
       hello
       <middle>text here</middle>
     ^</start> - return range from (^ to ^)
     </pre>
    */
    public ISourceLocation getContentSourceLocation()
    {
        if (!isEmptyTag())
        {
            return new SourceLocation(contentsStartLine, contentsCharStart, contentsCharEnd);
        }
        return null;
    }

    public ISourceLocation getStartTagSourceLocation()
    {
        return new SourceLocation(startTagStartLine, startTagCharStart, startTagCharEnd);
    }

    public ISourceLocation getEndTagSourceLocation()
    {
        return new SourceLocation(endTagStartLine, endTagCharStart, endTagCharEnd);
    }

    public ISourceLocation getAttributeSourceLocation(String rawname)
    {
        if (attributes != null)
        {
            int[] data = (int[]) attributes.get(rawname);
            if (data != null)
            {
                return new SourceLocation(data[LINE], data[CHAR_START], data[CHAR_END]);
            }
        }
        return null;
    }

    public int getStartTagStartLine()
    {
        return startTagStartLine;
    }

    public int getStartTagEndLine()
    {
        return startTagEndLine;
    }

    public int getEndTagStartLine()
    {
        return endTagStartLine;
    }

    public int getEndTagEndLine()
    {
        return endTagEndLine;
    }

    private Map getAttributes()
    {
        if (attributes == null)
        {
            attributes = new HashMap();
        }
        return attributes;
    }

    private void init(ElementXMLEventInfo eventInfo, ISourceLocationResolver resolver)
    {

        XMLEnityEventInfo startTag = eventInfo.getStartTagLocation();
        XMLEnityEventInfo endTag = eventInfo.getEndTagLocation();
        
        boolean isEmpty = endTag == null;

        locationLineNumber = startTag.getBeginLineNumber();
        
        locationColumnNumber = startTag.getBeginColumnNumber();
        int column = resolver.getColumnOffset(locationLineNumber, locationColumnNumber);

        startTagStartLine = sourceStartLine = locationLineNumber;
        startTagCharStart = sourceCharStart = column;

        int line = startTag.getEndLineNumber();
        column = resolver.getColumnOffset(line, startTag.getEndColumnNumber());

        startTagEndLine = line;
        startTagCharEnd = column;

        if (endTag == null)
        {

            contentsStartLine = contentsCharStart = contentsCharEnd = -1;

            endTagStartLine = sourceEndLine = startTagStartLine;
            endTagCharStart = startTagCharStart;
            endTagEndLine = sourceEndLine = startTagEndLine;
            endTagCharEnd = sourceCharEnd = startTagCharEnd;

        } else
        {

            contentsStartLine = startTagStartLine;
            contentsCharStart = startTagCharEnd;

            line = endTag.getBeginLineNumber();
            column = resolver.getColumnOffset(line, endTag.getBeginColumnNumber());

            endTagStartLine = line;
            endTagCharStart = column;

            line = endTag.getEndLineNumber();
            column = resolver.getColumnOffset(line, endTag.getEndColumnNumber());

            endTagEndLine = line;
            endTagCharEnd = column;

            contentsCharEnd = endTagCharStart;
        }

        Map attributeMap = eventInfo.getAttributeMap();
        if (!attributeMap.isEmpty())
        {
            int[] data = null;
            for (Iterator iter = attributeMap.keySet().iterator(); iter.hasNext();)
            {
                String rawname = (String) iter.next();
                SimpleXMLEventInfo info = (SimpleXMLEventInfo) attributeMap.get(rawname);
                data = new int[3];
                line = info.getBeginLineNumber();
                column = info.getBeginColumnNumber();
                data[LINE] = line;
                data[CHAR_START] = resolver.getColumnOffset(line, column);
                line = info.getEndLineNumber(); 
                column = info.getEndColumnNumber();
                data[CHAR_END] = resolver.getColumnOffset(line, column);
                getAttributes().put(rawname, data);
            }
        }
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer("ElementLocation[\n");
        buffer.append("Start Tag: ");
        buffer.append(getStartTagSourceLocation());
        buffer.append("\n");
        buffer.append("End Tag: ");
        buffer.append(getEndTagSourceLocation());
        buffer.append("\n");
        buffer.append("Contents: ");
        if (!isEmptyTag())
        {
            buffer.append(getContentSourceLocation());
        } else
        {
            buffer.append("None, its an empty tag");
        }
        buffer.append("\n");
        if (attributes == null)
        {
            buffer.append("Not attribute info found");
        } else
        {
            for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();)
            {
                String rawname = (String) iter.next();
                buffer.append("Attr(");
                buffer.append(rawname);
                buffer.append(")");
                buffer.append(attributes.get(rawname));

                buffer.append("\n");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }
    
    /*
     * 
     * ILocation methods
     * 
     */
     
    /* (non-Javadoc)
     * @see org.apache.tapestry.ILocation#getColumnNumber()
     */
    public int getColumnNumber()
    {
        return locationColumnNumber;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.ILocation#getLineNumber()
     */
    public int getLineNumber()
    {
        return locationLineNumber;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.ILocation#getResourceLocation()
     */
    public IResourceLocation getResourceLocation()
    {
        return resourceLocation;
    }
    
    public void setResourceLocation(IResourceLocation location) {
        this.resourceLocation = location;
    }
    
    

}
