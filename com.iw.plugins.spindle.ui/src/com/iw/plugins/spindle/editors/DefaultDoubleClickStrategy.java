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

package com.iw.plugins.spindle.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Default strategy for selecting text on double click
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: DefaultDoubleClickStrategy.java,v 1.1 2003/10/29 12:33:58
 *          glongman Exp $
 */
public class DefaultDoubleClickStrategy implements ITextDoubleClickStrategy
{
  /*
   * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(ITextViewer)
   */
  public void doubleClicked(ITextViewer viewer)
  {
    int offset = viewer.getSelectedRange().x;
    if (offset < 0)
    {
      return;
    }

    selectWord(viewer, viewer.getDocument(), offset);
  }

  protected void selectWord(ITextViewer textViewer, IDocument document, int offset)
  {
    try
    {
      int start = offset;
      while (start >= 0)
      {
        char c = document.getChar(start);

        if (!Character.isUnicodeIdentifierPart(c))
        {
          break;
        }

        --start;
      }

      int length = document.getLength();

      int end = offset;
      while (end < length)
      {
        char c = document.getChar(end);

        if (!Character.isUnicodeIdentifierPart(c))
        {
          break;
        }

        ++end;
      }

      if (start == end)
      {
        textViewer.setSelectedRange(start, 0);
      } else
      {
        textViewer.setSelectedRange(start + 1, end - start - 1);
      }
    } catch (BadLocationException x)
    {}
  }
}