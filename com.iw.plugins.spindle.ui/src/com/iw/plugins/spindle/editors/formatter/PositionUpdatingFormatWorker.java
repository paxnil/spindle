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
 * The Original Code is XMLFormattingStrategy
 *
 * The Initial Developer of the Original Code is
 * Christian Sell <christian.sell@netcologne.de>.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *  christian.sell@netcologne.de
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.formatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.TypedPosition;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * 
 * default formatting strategy. It uses the partitioning of the underlying
 * document to determine the different elements for formatting
 * 
 * changed by GL to use a custom document partitioner.
 * 
 * @author cse
 * @version $Id: XMLFormattingStrategy.java,v 1.5 2004/05/16 05:14:03 glongman
 *                     Exp $
 */
public abstract class PositionUpdatingFormatWorker extends FormatWorker
{
  /**
   * Helper class to handle line information. The text input is partitioned into
   * individual lines such that:
   * <ul>
   * <li>any leading newline is discarded</li>
   * <li>all following lines are added, even if empty</li>
   * <li>if non-whitespace text remains after the last newline, it is trimmed
   * and added</li>
   * </ul>
   * The lines are managed as LineInfo objects, which also track the starting
   * offset of the line data into the underlying document.
   */
  protected class LineWalker
  {
    private List lines = new ArrayList();
    private Iterator iterator;

    public boolean isMultiline()
    {
      return lines.size() > 1;
    }

    public LineWalker(IDocument document, TypedPosition tposition, int[] positions)
        throws BadLocationException
    {
      ILineTracker lineTracker = new DefaultLineTracker();

      int textOffset = tposition.getOffset();
      String text = document.get(textOffset, tposition.getLength());
      lineTracker.set(text);

      int lineCount = lineTracker.getNumberOfLines();
      for (int i = 0; i < lineCount; i++)
      {
        int off = lineTracker.getLineOffset(i);
        String delimiter = lineTracker.getLineDelimiter(i);
        int length = lineTracker.getLineLength(i);

        if (delimiter != null)
        {
          if (i > 0 || length > delimiter.length())
          {
            LineInfo info = new LineInfo(textOffset + off, text.substring(off, off
                + length - delimiter.length()), delimiter.length());
            info.recordPositionIndex(positions);
            lines.add(info);
          }
        } else
        {
          //only the add last line if it contains non-whitespace text
          LineInfo info = new LineInfo(textOffset + off, text
              .substring(off, off + length), 0);
          info.trimData();
          info.recordPositionIndex(positions);
          lines.add(info);
          if (info.data.length() == 0)
            info.markEmpty();

          //                    if (info.data.length() > 0)
          //                    {
          //                        info.recordPositionIndex(positions);
          //                        lines.add(info);
          //                    }
        }
      }
      iterator = lines.iterator();
    }

    /**
     * @return whether there are any more lines
     */
    public boolean hasMoreLines()
    {
      return iterator.hasNext();
    }

    /**
     * @return the next LineInfo
     * @throws java.util.NoSuchElementException
     */
    public LineInfo nextLine()
    {
      return (LineInfo) iterator.next();
    }
  }

  protected int fOffset;
  protected TypedPosition[] fDocumentPositions;

  //  /*
  //   * (non-Javadoc)
  //   *
  //   * @see
  // com.iw.plugins.spindle.editors.XMLContentFormatter.FormatWorker#format(org.eclipse.jface.text.IDocument,
  //   * int, int, int[])
  //   */
  //  public String format(
  //      FormattingPreferences prefs,
  //      IDocument document,
  //      int offset,
  //      int length,
  //      int[] positions)
  //  {
  //
  //    fPreferences = prefs;
  //
  //    try
  //    {
  //      if (length < 2)
  //        return document.get(offset, length);
  //
  //      fDocument = document;
  //      fOffset = offset;
  //      fLineDelimiter = getLineDelimiter(fDocument);
  //      fInitialIndent = 0;
  //      fIndentLevel = 0;
  //      fPositions = positions;
  //      fLineInfos = new ArrayList();
  //
  //      fDocumentPositions = computeAffectedPartitions();
  //
  //      return doFormat(length);
  //
  //    } catch (BadLocationException e)
  //    {
  //      UIPlugin.log(e); //shouldnt happen
  //      return null;
  //    } catch (RuntimeException e)
  //    {
  //      UIPlugin.log(e);
  //      throw e;
  //    } finally
  //    {
  //      fDocument = null; //release to GC
  //      fPreferences = null;
  //      fPositions = null;
  //      fLineInfos = null;
  //      fDocumentPositions = null;
  //    }
  //  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.formatter.XMLContentFormatter.FormatWorker#format(com.iw.plugins.spindle.editors.formatter.FormattingPreferences,
   *              org.eclipse.jface.text.IDocument,
   *              org.eclipse.jface.text.TypedPosition, int[])
   */
  public Object format(
      FormattingPreferences prefs,
      IDocument document,
      TypedPosition partition,
      int[] positions)
  {
    fPreferences = prefs;

    try
    {
      if (partition.length < 2)
        return document.get(partition.offset, partition.length);

      fDocument = document;
      fOffset = partition.offset;
      fLineDelimiter = UIUtils.getLineDelimiter(fDocument);
      fInitialIndent = 0;
      fIndentLevel = 0;
      fPositions = positions;
      fLineInfos = new ArrayList();

      fDocumentPositions = computeAffectedPartitions(partition);

      return doFormat(partition);

    } catch (BadLocationException e)
    {
      UIPlugin.log(e); //shouldnt happen
      return null;
    } catch (RuntimeException e)
    {
      UIPlugin.log(e);
      throw e;
    } finally
    {
      fDocument = null; //release to GC
      fPreferences = null;
      fPositions = null;
      fLineInfos = null;
      fDocumentPositions = null;
    }
  }
  protected abstract TypedPosition[] computeAffectedPartitions(TypedPosition partition);

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.formatter.FormatWorker#usesEdits()
   */
  public boolean usesEdits()
  {
    return false;
  }
  /**
   * do the actual formatting, after all run variables have been initialized
   * This is for formatting a group of partitions
   * 
   * @param partition the partition being formatted
   * @return the ready formatted content string
   * @throws BadLocationException
   */
  protected abstract Object doFormat(TypedPosition partition) throws BadLocationException;

  protected int writeLine(
      String line,
      int initialColumns,
      int indentLevel,
      StringBuffer buffer)
  {
    return writeLine(line, initialColumns, indentLevel, buffer, true);
  }

  /**
   * Write the line data to the given buffer, using the appropriate indent.
   * Terminate with a line delimiter if indicated.
   * 
   * @param initialColumns initial columns to indent. Tabs or spaces, depending
   *                     on preference
   * @param indentLevel the number of indents (tabs or spaces) to add
   * @param buffer the buffer to write to
   */
  protected int writeLine(
      String line,
      int initialColumns,
      int indentLevel,
      StringBuffer buffer,
      boolean appendLineDelimiter)
  {
    int writeOffset = fOffset + buffer.length();
    writeOffset += writeColumns(initialColumns, buffer);
    writeOffset += writeIndent(indentLevel, buffer);

    buffer.append(line);
    if (appendLineDelimiter)
      buffer.append(fLineDelimiter);

    return writeOffset;
  }

  //append ensuring a leading whitespace
  protected int append(String line, StringBuffer buffer)
  {
    return append(line, buffer, true);
  }

  /**
   * Append the given line to the buffer. This effectively discards any
   * delimiter. no indent.
   */
  protected int append(String line, StringBuffer buffer, boolean ensureLeadingWhitespace)
  {
    char last = buffer.length() > 0 ? buffer.charAt(buffer.length() - 1) : 'x';
    if (ensureLeadingWhitespace && !Character.isWhitespace(last))
      buffer.append(' ');

    int writeOffset = fOffset + buffer.length();
    buffer.append(line);
    return writeOffset;
  }

  /**
   * Handle an empty line (a line that will not be output). This method exists
   * only so that correct offset in the result is available to update positions.
   * 
   * @param buffer the buffer to write to
   * @return the current offset
   */
  protected int writeEmpty(StringBuffer buffer)
  {
    return fOffset + buffer.length();
  }

}