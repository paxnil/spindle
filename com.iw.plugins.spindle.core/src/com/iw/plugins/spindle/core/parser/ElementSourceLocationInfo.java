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

    private int fSourceStartLine = 0;
    private int fSourceCharStart = 0;
    private int fSourceCharEnd = 0;
    private int fSourceEndLine = 0;

    private int fStartTagStartLine = 0;
    private int fStartTagCharStart = 0;
    private int fStartTagCharEnd = 0;
    private int fStartTagEndLine = 0;

    private int fEndTagStartLine = 0;
    private int fEndTagCharStart = 0;
    private int fEndTagCharEnd = 0;
    private int fEndTagEndLine = 0;

    private int fContentsStartLine = 0;
    private int fContentsCharStart = 0;
    private int fContentsCharEnd = 0;
    
    private int fLocationColumnNumber;
    private int fLocationLineNumber;
    private IResourceLocation fResourceLocation;

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
        return fContentsStartLine < 1;
    }

    /** return a location for the element - includes all wrapped by it**/
    public ISourceLocation getSourceLocation()
    {
        return new SourceLocation(fSourceStartLine, fSourceCharStart, fSourceCharEnd);
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
            return new SourceLocation(fContentsStartLine, fContentsCharStart, fContentsCharEnd);
        }
        return null;
    }

    public ISourceLocation getStartTagSourceLocation()
    {
        return new SourceLocation(fStartTagStartLine, fStartTagCharStart, fStartTagCharEnd);
    }

    public ISourceLocation getEndTagSourceLocation()
    {
        return new SourceLocation(fEndTagStartLine, fEndTagCharStart, fEndTagCharEnd);
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
        return fStartTagStartLine;
    }

    public int getStartTagEndLine()
    {
        return fStartTagEndLine;
    }

    public int getEndTagStartLine()
    {
        return fEndTagStartLine;
    }

    public int getEndTagEndLine()
    {
        return fEndTagEndLine;
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

        fLocationLineNumber = startTag.getBeginLineNumber();
        
        fLocationColumnNumber = startTag.getBeginColumnNumber();
        int column = resolver.getColumnOffset(fLocationLineNumber, fLocationColumnNumber);

        fStartTagStartLine = fSourceStartLine = fLocationLineNumber;
        fStartTagCharStart = fSourceCharStart = column;

        int line = startTag.getEndLineNumber();
        column = resolver.getColumnOffset(line, startTag.getEndColumnNumber());

        fStartTagEndLine = line;
        fStartTagCharEnd = column;

        if (endTag == null)
        {

            fContentsStartLine = fContentsCharStart = fContentsCharEnd = -1;

            fEndTagStartLine = fSourceEndLine = fStartTagStartLine;
            fEndTagCharStart = fStartTagCharStart;
            fEndTagEndLine = fSourceEndLine = fStartTagEndLine;
            fEndTagCharEnd = fSourceCharEnd = fStartTagCharEnd;

        } else
        {

            fContentsStartLine = fStartTagStartLine;
            fContentsCharStart = fStartTagCharEnd;

            line = endTag.getBeginLineNumber();
            column = resolver.getColumnOffset(line, endTag.getBeginColumnNumber());

            fEndTagStartLine = line;
            fEndTagCharStart = column;

            line = endTag.getEndLineNumber();
            column = resolver.getColumnOffset(line, endTag.getEndColumnNumber());

            fEndTagEndLine = line;
            fEndTagCharEnd = column;

            fContentsCharEnd = fEndTagCharStart;
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
        return fLocationColumnNumber;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.ILocation#getLineNumber()
     */
    public int getLineNumber()
    {
        return fLocationLineNumber;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.ILocation#getResourceLocation()
     */
    public IResourceLocation getResourceLocation()
    {
        return fResourceLocation;
    }
    
    public void setResourceLocation(IResourceLocation location) {
        this.fResourceLocation = location;
    }
    
    

}
