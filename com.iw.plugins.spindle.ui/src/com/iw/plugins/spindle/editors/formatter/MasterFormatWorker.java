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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TypedPosition;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.internal.ui.text.XMLReconciler;
import org.xmen.xml.XMLNode;

/**
 * MasterFormatter formats a whole document or a section of a document.
 * Depending on preference settings, start tags are collapsed to one line.
 * (leaving them in a state where slave formatters can do thier work!).
 * 
 * @author glongman@intelligentworks.com
 * 
 */
public class MasterFormatWorker extends PositionUpdatingFormatWorker
{

  protected TypedPosition[] computeAffectedPartitions(TypedPosition partition)
  {
    XMLReconciler reconciler = new XMLReconciler();
    reconciler.createTree(fDocument);
    List stored = reconciler.getStoredPos();
    return (TypedPosition[]) stored.toArray(new TypedPosition[stored.size()]);
  }

  protected Object doFormat(TypedPosition partition) throws BadLocationException
  {

    //determine the enclosing element and the appropriate indent
    TypedPositionWalker walker = new TypedPositionWalker(fDocumentPositions, fOffset);

    for (TypedPosition tposition = walker.previous(); tposition != null; tposition = walker
        .previous())
    {
      String type = tposition.getType();
      if (type == ITypeConstants.TAG || type == ITypeConstants.EMPTYTAG)
      {
        fInitialIndent = getIndent(tposition.getOffset());
        if (type != ITypeConstants.EMPTYTAG)
          fIndentLevel++;
        break;
      } else if (type == ITypeConstants.ENDTAG)
      {
        fInitialIndent = getIndent(tposition.getOffset());
        break;
      }
    }

    //walk through the partitions and format
    walker = new TypedPositionWalker(fDocumentPositions, fOffset, partition.length);
    StringBuffer buffer = new StringBuffer();

    TypedPosition tposition = walker.next();
    while (tposition != null)
    {
      String type = tposition.getType();
      if (type == ITypeConstants.TAG || type == ITypeConstants.EMPTYTAG)
      {
        formatTag(tposition, buffer, false);
        if (type == ITypeConstants.TAG)
          fIndentLevel++;

      } else if (type == ITypeConstants.ENDTAG)
      {
        if (fIndentLevel > 0)
          fIndentLevel--;
        formatTag(tposition, buffer, true);
      } else if (type == ITypeConstants.DECL)
      {
        XMLNode artifact = (XMLNode) tposition;
        String content = artifact.getContent();
        if (content.indexOf("DOCTYPE") >= 0)
        {
          formatTag(tposition, buffer, true);
        } else
        {
          formatCDATA(tposition, buffer);
        }

      } else if (type == ITypeConstants.COMMENT)
      {
        formatDefault(tposition, buffer);
      } else if (type == ITypeConstants.TEXT)
      {
        formatDefault(tposition, buffer);
      } else if (type == ITypeConstants.PI)
      {
        formatTag(tposition, buffer, true);
      }
      tposition = walker.next();
    }

    //finally, have the line infos update the positions array
    Iterator it = fLineInfos.iterator();
    while (it.hasNext())
    {
      LineInfo info = (LineInfo) it.next();
      info.updatePositions();
    }

    return buffer.toString();

  }

  /**
   * default formatting. Everything is aligned, one indent level above the
   * nearest enclosing opening element, if any
   */
  protected void formatDefault(TypedPosition region, StringBuffer buffer) throws BadLocationException
  {
    LineWalker lineWalker = new LineWalker(fDocument, region, fPositions);
    boolean alreadyKept = false;
    while (lineWalker.hasMoreLines())
    {
      LineInfo info = lineWalker.nextLine();

      if (info.isEmpty())
      {
        info.setWriteOffset(writeEmpty(buffer));
        fLineInfos.add(info);
      } else
      {
        String line = info.trimData();

        if (line.length() > 0)
        {
          int off = writeLine(info.data, fInitialIndent, fIndentLevel, buffer);

          info.setWriteOffset(off);
          fLineInfos.add(info);
        } else if (fPreferences.preserveBlankLines() && !alreadyKept)
        {
          int off = writeLine("", fInitialIndent, fIndentLevel, buffer);
          info.setWriteOffset(off);
          fLineInfos.add(info);
          //                    buffer.append(fLineDelimiter);
          alreadyKept = true;
        } else {
          int off = writeEmpty(buffer);
          info.setWriteOffset(off);
          fLineInfos.add(info);
        }
      }
    }
  }

  /**
   * format a tag. Attributes, if starting on a new line, are given an
   * additional indent TODO update this comment for the new magical multiline
   * handline
   */
  protected void formatTag(
      TypedPosition tposition,
      StringBuffer buffer,
      boolean collapseAlways) throws BadLocationException
  {
    LineWalker lineWalker = new LineWalker(fDocument, tposition, fPositions);

   if (lineWalker.isMultiline() && (fPreferences.wrapLongTags() || collapseAlways))
//      if (false)
    {
      formatMultiLineStartTag(lineWalker, buffer);
      return;
    }

    int count = 0;
    boolean first = true;
    while (lineWalker.hasMoreLines())
    {
      LineInfo info = lineWalker.nextLine();
      String line = info.trimData();

      if (line.length() > 0)
      {

        int indentLevel = count > 0 ? fIndentLevel + 1 : fIndentLevel;
        int writeOffset = writeLine(info.data, fInitialIndent, indentLevel, buffer);

        info.setWriteOffset(writeOffset);
        fLineInfos.add(info);

        count++;
      }
    }
  }
  protected void formatMultiLineStartTag(LineWalker lineWalker, StringBuffer buffer) throws BadLocationException
  {

    LineInfo info = lineWalker.nextLine();
    String line = info.trimDataLeadingAndTrailing();
    int count = 0;

    //get the first non empty line
    while (line.length() <= 0 && lineWalker.hasMoreLines())
    {
      info = lineWalker.nextLine();
      line = info.trimDataLeadingAndTrailing();
    }

    if (line.length() > 0)
    {
      //format the first line
      int indentLevel = fIndentLevel;

      int writeOffset = writeLine(info.data, fInitialIndent, indentLevel, buffer, false);

      info.setWriteOffset(writeOffset);
      fLineInfos.add(info);
      count++;
    }

    while (lineWalker.hasMoreLines())
    {
      info = lineWalker.nextLine();
      line = info.trimDataLeadingAndTrailing();

      if (line.length() > 0)
      {

        int writeOffset = append(info.data, buffer);

        info.setWriteOffset(writeOffset);
        fLineInfos.add(info);

        count++;

      }
    }

    if (count > 0)
      buffer.append(fLineDelimiter);

  }

  /**
   * format a CDATA region, preserving indenting within the CDATA
   */
  private void formatCDATA(TypedPosition region, StringBuffer buffer) throws BadLocationException
  {
    LineWalker lineWalker = new LineWalker(fDocument, region, fPositions);

    LineInfo info = lineWalker.nextLine();
    int firstIndent = getIndent(info.offset);
    info.trimData();
    int writeOffset = writeLine(info.data, fInitialIndent, fIndentLevel, buffer);

    info.setWriteOffset(writeOffset);
    fLineInfos.add(info);

    while (lineWalker.hasMoreLines())
    {
      info = lineWalker.nextLine();
      int indentDelta = getIndent(info.data) - firstIndent;
      String line = info.trimData();
      if (line.length() > 0)
      {
        int indent = indentDelta > 0 ? fInitialIndent + indentDelta : fInitialIndent;
        writeOffset = writeLine(info.data, indent, fIndentLevel, buffer);

        info.setWriteOffset(writeOffset);
        fLineInfos.add(info);
      } else
      {
        buffer.append(fLineDelimiter);
      }
    }
  }

}