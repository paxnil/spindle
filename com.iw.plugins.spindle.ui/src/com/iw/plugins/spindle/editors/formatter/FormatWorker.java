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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.formatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.xmen.xml.XMLNode;

/**
 * BaseFormatWorker gives access to the LineInfo type to subclasses.
 * 
 * @author glongman@intelligentworks.com
 * 
 */
public abstract class FormatWorker
{

  protected class TagWalker
  {
    private List chunks = new ArrayList();
    private ListIterator iterator;

    int totalLength = 0;
    String tagname = null;

    public TagWalker(IDocument document, XMLNode node) throws BadLocationException
    {

      int[] positions = new int[]{};

      int textOffset = node.getOffset();
      int offset = 0;
      String text = document.get(textOffset, node.getLength());

      // save the open bracket
      LineInfo info = new LineInfo(textOffset, text.substring(0, 1), 0);
      offset = 1;
      totalLength = 1;
      chunks.add(info);
      // the name if there is one.
      tagname = node.getName();
      if (tagname != null)
      {
        int endName = text.indexOf(tagname) + tagname.length();
        info = new LineInfo(textOffset + offset, text.substring(offset, endName), 0);
        chunks.add(info);
        offset += info.data.length();
        totalLength += tagname.length();
      } else
      {
        System.err.println("no name");
      }

      List attributes = node.getAttributes();
      for (Iterator iter = attributes.iterator(); iter.hasNext();)
      {
        XMLNode attr = (XMLNode) iter.next();
        int endAttr = (attr.offset - textOffset) + attr.length;

        info = new LineInfo(textOffset + offset, text.substring(offset, endAttr), 0);
        chunks.add(info);
        offset += info.data.length();
        totalLength += 1 + info.testTrim().length();
      }

      //add the remaining (should be the closeing braket/slashbraket).
      info = new LineInfo(textOffset + offset, text.substring(offset, text.length()), 0);
      chunks.add(info);
      totalLength += info.testTrim().length();

      iterator = chunks.listIterator();
    }

    public boolean nextIsLast()
    {
      return iterator.hasNext() && iterator.nextIndex() == chunks.size() - 1;
    }

    public boolean hasNext()
    {
      return iterator.hasNext();
    }

    /**
     * @return the next LineInfo
     * @throws java.util.NoSuchElementException
     */
    public LineInfo nextChunk()
    {
      return (LineInfo) iterator.next();
    }
  }

  protected class LineInfo
  {
    int offset, dataOffset, delimiterLength, posIndex1 = -1, posIndex2, delta;
    String data;
    int[] positions;
    boolean empty = false;

    LineInfo(int offset, String data, int delimiterLength)
    {
      this.offset = offset;
      this.dataOffset = offset;
      this.delimiterLength = delimiterLength;
      this.data = data;
    }

    public String toString()
    {
      return ">" + data + "<";
    }
    /**
     * remove leading whitespace (modifying the internal state)
     * 
     * @return the data string after removing whitespace
     */
    public String trimData()
    {
      int i = 0;
      for (; i < data.length() && data.charAt(i) <= ' '; i++);
      if (i > 0)
      {
        dataOffset += i;
        data = data.substring(i, data.length());
      }
      return data;
    }

    public String testTrim()
    {
      int i = 0;
      for (; i < data.length() && data.charAt(i) <= ' '; i++);
      if (i > 0)
      {
        return data.substring(i, data.length());
      }
      return data;
    }

    public String trimDataLeadingAndTrailing()
    {
      if (empty)
        return trimData();

      int i = data.length() - 1;
      for (; i >= 0 && data.charAt(i) <= ' '; i--);
      if (i > 0 && i < data.length() - 1)
      {
        i += 1;
        int newEnd = offset + i;
        data = data.substring(0, i);
        //update our postions 'cuz we shortend the string.

        if (posIndex1 > 0)
        {
          for (int j = posIndex1; j < posIndex2; j++)
          {
            if (positions[j] > newEnd)
              positions[j] = newEnd;
          }
        }
      }
      return trimData();
    }
    /**
     * @param positions the positions array
     */
    public void recordPositionIndex(int[] positions)
    {
      this.positions = positions;
      int i = 0;
      for (; i < positions.length && positions[i] < offset; i++);
      if (i < positions.length)
      {
        posIndex1 = i;
        int endOffset = dataOffset + data.length() + delimiterLength;
        for (; i < positions.length && positions[i] < endOffset; i++);
        posIndex2 = i;
      }
    }
    /**
     * record the write offset for later processing in
     * {@link #updatePositions()}
     * 
     * @param offset the offset at which this line was written into the
     *                     formatted output
     */
    public void setWriteOffset(int offset)
    {
      if (!empty)
      {
        delta = offset - dataOffset;
      } else
      {
        dataOffset = offset;
      }
    }

    public void markEmpty()
    {
      empty = true;
    }

    public boolean isEmpty()
    {
      return empty;
    }
    /**
     * update the positions within the range of this object such that they are
     * correct relative to the previously set write offset.
     */
    public void updatePositions()
    {
      if (posIndex1 >= 0)
      {
        for (int i = posIndex1; i < posIndex2; i++)
        {
          if (!empty)
          {
            positions[i] += delta;
          } else
          {
            positions[i] = dataOffset;
          }
        }
      }
    }
  }

  protected IDocument fDocument;
  protected FormattingPreferences fPreferences;
  protected int fInitialIndent;
  protected int fIndentLevel;
  protected int[] fPositions;

  public abstract Object format(
      FormattingPreferences prefs,
      IDocument document,
      TypedPosition partition,
      int[] positions);

  public boolean usesEdits()
  {
    return true;
  }

  /**
   * Returns the indentation of the line of the given offset.
   * 
   * @param offset the offset
   * @return the indentation of the line of the offset
   */
  protected int getIndent(int offset)
  {

    int tabSpaces = fPreferences.getTabWidth();

    try
    {
      int start = fDocument.getLineOfOffset(offset);
      start = fDocument.getLineOffset(start);

      int count = 0;
      for (int i = start; i < fDocument.getLength(); ++i)
      {
        char c = fDocument.getChar(i);
        if ('\t' == c)
          count += tabSpaces;
        else if (' ' == c)
          count++;
        else
          break;
      }
      return count;
    } catch (BadLocationException x)
    {
      return 0;
    }
  }

  /**
   * @param line
   * @return the number of character positions
   */
  protected int getIndent(String line)
  {

    int tabSpaces = fPreferences.getTabWidth();

    int count = 0;
    for (int i = 0; i < line.length(); ++i)
    {
      char c = line.charAt(i);
      if ('\t' == c)
        count += tabSpaces;
      else if (' ' == c)
        count++;
      else
        break;
    }
    return count;
  }

  protected List fLineInfos;
  protected String fLineDelimiter;

  /**
   * write a given number of whitespace columns, using tabs or spaces depending
   * on preference
   * 
   * @param columnCount the columns to write
   * @param buffer the buffer to write to
   * @return the number of characters inserted into the buffer
   */
  protected int writeColumns(int columnCount, StringBuffer buffer)
  {

    if (fPreferences.useSpacesInsteadOfTabs())
    {
      for (int i = 0; i < columnCount; i++)
        buffer.append(' ');

      return columnCount;
    } else
    {
      int tabSpaces = fPreferences.getTabWidth();
      int tabs = columnCount / tabSpaces;
      int spaces = columnCount % tabSpaces;

      for (int i = 0; i < tabs; i++)
        buffer.append('\t');
      for (int i = 0; i < spaces; i++)
        buffer.append(' ');

      return tabs + spaces;
    }

  }

  /**
   * write a given number of whitespace columns, using tabs or spaces depending
   * on preference
   * 
   * @param indentCount the number of indents to write
   * @param buffer the buffer to write to
   * @return the number of characters inserted into the buffer
   */
  protected int writeIndent(int indentCount, StringBuffer buffer)
  {

    if (fPreferences.useSpacesInsteadOfTabs())
    {
      int length = indentCount * fPreferences.getTabWidth();
      for (int i = 0; i < length; i++)
        buffer.append(' ');

      return length;
    } else

    {
      for (int i = 0; i < indentCount; i++)
        buffer.append('\t');

      return indentCount;
    }
  }

}

