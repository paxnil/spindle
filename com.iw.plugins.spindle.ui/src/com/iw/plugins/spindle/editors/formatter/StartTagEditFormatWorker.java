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
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;

/**
 * MasterFormatter formats a whole document or a section of a document.
 * Depending on preference settings, start tags are collapsed to one line.
 * (leaving them in a state where slave formatters can do thier work!).
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class StartTagEditFormatWorker extends FormatWorker
{

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

    if (!isOneLinePartition(document, partition))
      return null;

    fDocument = document;
    fPreferences = prefs;
    fLineDelimiter = getLineDelimiter(document);

    try
    {
      XMLNode node = new XMLNode(
          partition.offset,
          partition.length,
          partition.getType(),
          document);

      if (!node.isTerminated())
        return null;

      MultiTextEdit result = new MultiTextEdit(partition.getOffset(), partition
          .getLength());

      TagWalker walker = null;
      try
      {
        walker = new TagWalker(document, node);
      } catch (BadLocationException e)
      {
        UIPlugin.log(e);
        return null;
      }

      fInitialIndent = getIndent(node.getOffset());
      int totalLength = fInitialIndent + walker.totalLength;

      List attrs = node.getAttributes();
//criteria for splitting a line...
// A - Prefs call for splitting
// B - the tag is too long
// C - the tag has 2 or more attrs
      
      boolean shouldSplit = fPreferences.wrapLongTags() && totalLength > fPreferences.getMaximumLineWidth() && attrs.size() >= 2;
      if (shouldSplit)
      {
        //format - no line splitting
        formatWithSplits(walker, result);
      } else
      {
        //format with line spliting       
        formatNoLineSplit(walker, result);
      }

      return result;
    } finally
    {
      fDocument = null;
      fPositions = null;
    }
  }
  /**
   * @param document
   * @param partition
   * @return
   */
  private boolean isOneLinePartition(IDocument document, TypedPosition partition)
  {
    int startOffset = partition.getOffset();
    int endOffset = startOffset + partition.length - 1;

    try
    {
      return document.getLineOfOffset(startOffset) == document.getLineOfOffset(endOffset);
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    }
    return false;
  }

  /**
   * @param walker
   * @param buffer
   */
  private void formatWithSplits(TagWalker walker, MultiTextEdit edit)
  {
    int delta;
    //skip the open braket..
    LineInfo chunk = walker.nextChunk();

    String lineIndent = getIndent(fInitialIndent, 1);

    if (walker.tagname != null)
    {
      //fix the tagname
      chunk = walker.nextChunk();
      chunk.trimData();
      chunk.setWriteOffset(chunk.offset);
      delta = -1 * chunk.delta;
      if (delta > 0)
        edit.addChild(new ReplaceEdit(chunk.offset, delta, ""));
    }

    chunk = walker.nextChunk();
    while (walker.hasNext())
    {
      chunk.trimData();
      chunk.setWriteOffset(chunk.offset);
      delta = -1 * chunk.delta;
      if (delta > 0)
        edit.addChild(new ReplaceEdit(chunk.offset, delta, fLineDelimiter + lineIndent));

      chunk = walker.nextChunk();
    }

    //now the closing markup
    chunk.trimData();
    chunk.setWriteOffset(chunk.offset);
    delta = -1 * chunk.delta;
    if (delta > 0)
      edit.addChild(new ReplaceEdit(chunk.offset, delta, ""));

  }
  /**
   * @param initialIndent
   * @param i
   * @return
   */
  private String getIndent(int initialColumns, int indentLevel)
  {
    StringBuffer buffer = new StringBuffer();
    writeColumns(initialColumns, buffer);
    writeIndent(indentLevel, buffer);
    return buffer.toString();
  }

  private void formatNoLineSplit(TagWalker walker, MultiTextEdit edit)
  {

    int delta = 0;

    //skip the open braket..
    LineInfo chunk = walker.nextChunk();

    if (walker.tagname != null)
    {
      //fix the tagname
      chunk = walker.nextChunk();
      chunk.trimData();
      chunk.setWriteOffset(chunk.offset);
      delta = -1 * chunk.delta;
      if (delta > 0)
        edit.addChild(new ReplaceEdit(chunk.offset, delta, ""));
    }

    chunk = walker.nextChunk();
    while (walker.hasNext())
    {
      chunk.trimData();
      chunk.setWriteOffset(chunk.offset);
      delta = -1 * chunk.delta;
      if (delta > 1)
        edit.addChild(new ReplaceEdit(chunk.offset + 1, delta - 1, ""));

      chunk = walker.nextChunk();
    }

    //now the closing markup
    chunk.trimData();
    chunk.setWriteOffset(chunk.offset);
    delta = -1 * chunk.delta;
    if (delta > 0)
      edit.addChild(new ReplaceEdit(chunk.offset, delta, ""));
  }

}