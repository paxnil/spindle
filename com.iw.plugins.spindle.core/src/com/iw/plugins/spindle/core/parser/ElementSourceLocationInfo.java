package com.iw.plugins.spindle.core.parser;
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.IResourceLocation;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.xml.event.ElementXMLEventInfo;
import com.iw.plugins.spindle.core.parser.xml.event.SimpleXMLEventInfo;
import com.iw.plugins.spindle.core.parser.xml.event.XMLEnityEventInfo;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.source.ISourceLocationResolver;
import com.iw.plugins.spindle.core.source.SourceLocation;

public class ElementSourceLocationInfo implements ISourceLocationInfo
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
  private IResourceLocation fResourceLocation;

  private Map attributes;

  public ElementSourceLocationInfo(String elementName, ElementXMLEventInfo eventInfo,
      ISourceLocationResolver resolver)
  {
    init(elementName, eventInfo, resolver);
  }

  public int getOffset()
  {
    return fSourceCharStart;
  }

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

  public boolean hasAttributes()
  {
    return attributes != null;
  }

  public boolean isEmptyTag()
  {
    return fIsEmpty;
  }

  /** return a location for the element - includes all wrapped by it* */
  public ISourceLocation getSourceLocation()
  {
    if (fSourceLocation == null)
      fSourceLocation = new SourceLocation(
          fSourceStartLine,
          fSourceCharStart,
          fSourceCharEnd);
    return fSourceLocation;
  }

  /** return a location for all wrapped by the element* */
  /*
   * <pre> <start/> - return null <start> </start> - return a location of the
   * closing > of start tag <start>^ hello <middle>text here </middle> ^
   * </start> - return range from (^ to ^) </pre>
   */
  public ISourceLocation getContentSourceLocation()
  {
    if (!isEmptyTag() && fContentsLocation == null)
    {
      fContentsLocation = new SourceLocation(
          fContentsStartLine,
          fContentsCharStart,
          fContentsCharEnd);
    }
    return fContentsLocation;
  }

  public ISourceLocation getStartTagSourceLocation()
  {
    if (fStartTagLocation == null)
      fStartTagLocation = new SourceLocation(
          fStartTagStartLine,
          fStartTagCharStart,
          fStartTagCharEnd);
    return fStartTagLocation;
  }

  public ISourceLocation getTagNameLocation()
  {
    return fTagNameLocation;
  }

  public ISourceLocation getEndTagSourceLocation()
  {
    if (fEndTagLocation == null)
      fEndTagLocation = new SourceLocation(
          fEndTagStartLine,
          fEndTagCharStart,
          fEndTagCharEnd);
    return fEndTagLocation;
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

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.source.ISourceLocationInfo#getAttributeNames()
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

  private void init(
      String elementName,
      ElementXMLEventInfo eventInfo,
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

    } else
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
      
      fContentsCharStart =  fStartTagCharEnd + (fStartTagCharEnd == fEndTagCharStart - 1 ? 0 : 1);
      fContentsCharEnd = Math.max(fStartTagCharEnd,  fEndTagCharStart - 1);
      fSourceCharEnd = fEndTagCharEnd;
            
      int [] trimmed = resolver.trim(fContentsCharStart, fContentsCharEnd);
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
    fTagNameLocation = resolver.getTagNameLocation(
        elementName,
        getStartTagSourceLocation());
  }

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
   * 
   * ILocation methods
   *  
   */

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.ILocation#getColumnNumber()
   */
  public int getColumnNumber()
  {
    return fLocationColumnNumber;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.ILocation#getLineNumber()
   */
  public int getLineNumber()
  {
    return fLocationLineNumber;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.ILocation#getResourceLocation()
   */
  public IResourceLocation getResourceLocation()
  {
    return fResourceLocation;
  }

  public void setResourceLocation(IResourceLocation location)
  {
    this.fResourceLocation = location;
  }

}