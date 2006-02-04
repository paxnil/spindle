package net.sf.spindle.xerces.parser;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.source.ISourceLocationInfo;
import net.sf.spindle.core.source.ISourceLocationResolver;
import net.sf.spindle.core.source.SourceLocation;
import net.sf.spindle.xerces.parser.xml.event.ElementXMLEventInfo;
import net.sf.spindle.xerces.parser.xml.event.SimpleXMLEventInfo;
import net.sf.spindle.xerces.parser.xml.event.XMLEnityEventInfo;

import org.apache.hivemind.Resource;


public class XercesDOMElementSourceLocationInfo implements ISourceLocationInfo
{
    public static final Comparator COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            int offset1 = ((ISourceLocationInfo) o1).getOffset();
            int offset2 = ((ISourceLocationInfo) o2).getOffset();
            return (offset1 > offset2) ? 1 : ((offset1 < offset2) ? -1 : 0);
        }
    };

    private static final int LINE = 0;

    private static final int CHAR_START = 1;

    private static final int CHAR_END = 2;

    private String fElementName;

    private boolean fIsEmpty;

    private int fSourceStartLine = 0;

    private int fSourceCharStart = 0;

    private int fSourceCharEnd = 0;

    private int fSourceEndLine = 0;

    private ISourceLocation fSourceLocation;

    private int fStartTagStartLine = 0;

    private int fStartTagCharStart = 0;

    private int fStartTagCharEnd = 0;

    private int fStartTagEndLine = 0;

    private ISourceLocation fStartTagLocation;

    private ISourceLocation fTagNameLocation;

    private int fEndTagStartLine = 0;

    private int fEndTagCharStart = 0;

    private int fEndTagCharEnd = 0;

    private int fEndTagEndLine = 0;

    private ISourceLocation fEndTagLocation;

    private int fContentsStartLine = 0;

    private int fContentsCharStart = 0;

    private int fContentsCharEnd = 0;

    private ISourceLocation fContentsLocation;

    private int fLocationColumnNumber;

    private int fLocationLineNumber;

    private Resource fResourceLocation;

    private Map attributes;

    public XercesDOMElementSourceLocationInfo(String elementName, ElementXMLEventInfo eventInfo,
            ISourceLocationResolver resolver)
    {
        init(elementName, eventInfo, resolver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getOffset()
     */
    public int getOffset()
    {
        return fSourceCharStart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getLength()
     */
    public int getLength()
    {
        int result = fSourceCharEnd - fSourceCharStart + 1;
        if (result < 0)
        {
            StringBuffer buf = new StringBuffer("error found, tag source length is negative!\n");
            buf.append(this.toString());
            TapestryCore.log(buf.toString());
            return 1;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#hasAttributes()
     */
    public boolean hasAttributes()
    {
        return attributes != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#isEmptyTag()
     */
    public boolean isEmptyTag()
    {
        return fIsEmpty;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getSourceLocation()
     */
    public ISourceLocation getSourceLocation()
    {
        if (fSourceLocation == null)
            fSourceLocation = new SourceLocation(fSourceStartLine, fSourceCharStart, fSourceCharEnd);
        return fSourceLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getContentSourceLocation()
     */
    public ISourceLocation getContentSourceLocation()
    {
        if (!isEmptyTag() && fContentsLocation == null)
        {
            fContentsLocation = new SourceLocation(fContentsStartLine, fContentsCharStart,
                    fContentsCharEnd);
        }
        return fContentsLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getStartTagSourceLocation()
     */
    public ISourceLocation getStartTagSourceLocation()
    {
        if (fStartTagLocation == null)
            fStartTagLocation = new SourceLocation(fStartTagStartLine, fStartTagCharStart,
                    fStartTagCharEnd);
        return fStartTagLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getTagNameLocation()
     */
    public ISourceLocation getTagNameLocation()
    {
        return fTagNameLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getEndTagSourceLocation()
     */
    public ISourceLocation getEndTagSourceLocation()
    {
        if (fEndTagLocation == null)
            fEndTagLocation = new SourceLocation(fEndTagStartLine, fEndTagCharStart, fEndTagCharEnd);
        return fEndTagLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getAttributeSourceLocation(java.lang.String)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#getAttributeNames()
     */
    public Set getAttributeNames()
    {
        return new HashSet(getAttributes().keySet());
    }

    private Map getAttributes()
    {
        if (attributes == null)
        {
            attributes = new HashMap();
        }
        return attributes;
    }

    private void init(String elementName, ElementXMLEventInfo eventInfo,
            ISourceLocationResolver resolver)
    {
        fElementName = elementName;
        XMLEnityEventInfo startTag = eventInfo.getStartTagLocation();
        XMLEnityEventInfo endTag = eventInfo.getEndTagLocation();

        fIsEmpty = endTag == null;

        fLocationLineNumber = startTag.getBeginLineNumber();

        fLocationColumnNumber = startTag.getBeginColumnNumber();
        int column = resolver.getColumnOffset(fLocationLineNumber, fLocationColumnNumber);

        fStartTagStartLine = fSourceStartLine = fLocationLineNumber;
        fStartTagCharStart = fSourceCharStart = column;

        int line = startTag.getEndLineNumber();
        column = resolver.getColumnOffset(line, startTag.getEndColumnNumber(), '>');

        fStartTagEndLine = line;
        fStartTagCharEnd = column;

        if (fIsEmpty)
        {

            fContentsStartLine = fContentsCharStart = fContentsCharEnd = -1;

            fEndTagStartLine = fSourceEndLine = fStartTagStartLine;
            fEndTagCharStart = fStartTagCharStart;
            fEndTagEndLine = fSourceEndLine = fStartTagEndLine;
            fEndTagCharEnd = fSourceCharEnd = fStartTagCharEnd;

        }
        else
        {

            fContentsStartLine = fStartTagStartLine;

            line = endTag.getBeginLineNumber();
            column = resolver.getColumnOffset(line, endTag.getBeginColumnNumber());

            fEndTagStartLine = line;
            fEndTagCharStart = column;

            line = endTag.getEndLineNumber();
            column = resolver.getColumnOffset(line, endTag.getEndColumnNumber(), '>');

            fEndTagEndLine = line;
            fEndTagCharEnd = column;

            fContentsCharStart = fStartTagCharEnd
                    + (fStartTagCharEnd == fEndTagCharStart - 1 ? 0 : 1);
            fContentsCharEnd = Math.max(fStartTagCharEnd, fEndTagCharStart - 1);
            fSourceCharEnd = fEndTagCharEnd;

            int[] trimmed = resolver.trim(fContentsCharStart, fContentsCharEnd);
            fContentsCharStart = trimmed[0];
            fContentsCharEnd = trimmed[1];
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
        fTagNameLocation = resolver.getTagNameLocation(elementName, getStartTagSourceLocation());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer("ElementLocation[\n");
        buffer.append("Name: " + fElementName);
        buffer.append(getTagNameLocation());
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
        }
        else
        {
            buffer.append("None, its an empty tag");
        }
        buffer.append("\n");
        if (attributes == null)
        {
            buffer.append("Not attribute info found");
        }
        else
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
        buffer.append("]\n");
        buffer.append("fSourceStart:" + fSourceCharStart);
        buffer.append(" fSourceCharEnd:" + fSourceCharEnd);
        buffer.append("\n");
        buffer.append("offset: " + getOffset());
        buffer.append(" length:");
        buffer.append(fSourceCharEnd - fSourceCharStart + 1);
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.ISourceLocationInfo#setResource(org.apache.hivemind.Resource)
     */
    public void setResource(Resource location)
    {
        this.fResourceLocation = location;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Location#getColumnNumber()
     */
    public int getColumnNumber()
    {
        return fLocationColumnNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Location#getLineNumber()
     */
    public int getLineNumber()
    {
        return fLocationLineNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Location#getResource()
     */
    public Resource getResource()
    {
        return fResourceLocation;
    }

}