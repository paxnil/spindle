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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.formatter;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * MasterFormatter formats a whole document or a section of a document.
 * Depending on preference settings, start tags are collapsed to one line.
 * (leaving them in a state where slave formatters can do thier work!).
 * 
 * @author glongman@gmail.com
 */
public class DoctypeEditFormatWorker extends FormatWorker
{

  private static final int DOCTYPE = 0;
  private static final int ROOT_TAG = 1;
  private static final int PUBLIC = 2;
  private static final int URL1 = 3;
  private static final int URL2 = 4;
  private static final int LAST = 99;

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
    fLineDelimiter = UIUtils.getLineDelimiter(document);

    try
    {
      XMLNode node = new XMLNode(
          partition.offset,
          partition.length,
          partition.getType(),
          document);

      if (!node.isTerminated() || !"!DOCTYPE".equals(node.getName()))
        return null;

      MultiTextEdit result = new MultiTextEdit(partition.getOffset(), partition
          .getLength());

      TagWalker walker = null;
      try
      {
        walker = new TagWalker(document, node);
      } catch (BadLocationException e)
      {
        UIPlugin.log_it(e);
        return null;
      }

      fInitialIndent = getIndent(node.getOffset());

      //format with line spliting
      formatWithSplits(walker, result);

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
      UIPlugin.log_it(e);
    }
    return false;
  }

  /**
   * @param walker
   * @param buffer
   */
  private void formatWithSplits(TagWalker walker, MultiTextEdit edit)
  {
    int state = DOCTYPE;
    int delta;
    int publicLength = 0;
    int rootlength = 0;
    //    String lineIndent = getIndent(fInitialIndent, 1);
    String extraIndent = null;
    //skip the open bracket;
    LineInfo chunk = walker.nextChunk();
    chunk = walker.nextChunk();
    while (chunk != null)
    {
      switch (state)
      {
        case DOCTYPE :

          if (walker.tagname != null)
          {
            //fix the tagname
            chunk.trimData();
            chunk.setWriteOffset(chunk.offset);
            delta = -1 * chunk.delta;
            if (delta > 0)
              edit.addChild(new ReplaceEdit(chunk.offset, delta, ""));
          }
          chunk = walker.nextChunk();
          if (chunk != null)
          {
            chunk.trimData();
            if ("public".equals(chunk.data))
            {
              state = PUBLIC;
            } else
            {
              state = ROOT_TAG;
            }
          }
          break;

        case ROOT_TAG :
          rootlength = chunk.data.length() + 1;
          chunk.setWriteOffset(chunk.offset);
          delta = -1 * chunk.delta;
          if (delta > 1)
            edit.addChild(new ReplaceEdit(chunk.offset + 1, delta - 1, ""));
          state = PUBLIC;
          chunk = walker.nextChunk();
          chunk.trimData();
          break;

        case PUBLIC :

          publicLength = chunk.data.length() + 1;
          chunk.setWriteOffset(chunk.offset);
          delta = -1 * chunk.delta;
          if (delta > 1)
            edit.addChild(new ReplaceEdit(chunk.offset + 1, delta - 1, ""));
          state = URL2;
          chunk = walker.nextChunk();
          break;

        case URL1 :

          chunk.trimData();
          chunk.setWriteOffset(chunk.offset);
          delta = -1 * chunk.delta;
          if (delta > 1)
            edit.addChild(new ReplaceEdit(chunk.offset + 1, delta - 1, ""));
          state = URL2;
          chunk = walker.nextChunk();
          break;

        case URL2 :

          chunk.trimData();
          chunk.setWriteOffset(chunk.offset);
          delta = -1 * chunk.delta;
          if (delta > 0)
          {
            if (extraIndent == null)
            {
              //              StringBuffer buffer = new StringBuffer(lineIndent);
              StringBuffer buffer = new StringBuffer();
              writeColumns(2, buffer);
              extraIndent = buffer.toString();
            }
            edit.addChild(new ReplaceEdit(chunk.offset, delta, fLineDelimiter
                + extraIndent));
          }
          chunk = walker.nextChunk();
          if (!walker.hasNext())
            state = LAST;
          break;

        case LAST :
          chunk.trimData();
          chunk.setWriteOffset(chunk.offset);
          delta = -1 * chunk.delta;
          if (delta > 0)
            edit.addChild(new ReplaceEdit(chunk.offset, delta, ""));
          chunk = null;
          break;

      }
    }

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
}