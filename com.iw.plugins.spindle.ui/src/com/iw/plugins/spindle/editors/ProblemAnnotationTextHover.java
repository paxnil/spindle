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
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Text Hover for Editor annotations
 * 
 * @author glongman@gmail.com
 */
public class ProblemAnnotationTextHover extends DefaultTextHover
{

  public ProblemAnnotationTextHover(Editor editor)
  {
    super(editor);
  }

  /*
   * Formats a message as HTML text.
   */
  private String formatMessage(String message)
  {
    return message;
  }

  /*
   * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
   */
  public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
  {
    if (fEditor == null)
      return null;

    String result = null;

    IDocumentProvider provider = fEditor.getDocumentProvider();
    IAnnotationModel model = provider.getAnnotationModel(fEditor.getEditorInput());

    if (model != null)
    {
      Iterator e = new AnnotationIterator(model, true);
      while (e.hasNext())
      {
        Annotation a = (Annotation) e.next();
        Position p = model.getPosition(a);
        if (p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength()))
        {
          String msg = ((IProblemAnnotation) a).getMessage();
          if (msg != null && msg.trim().length() > 0)
            result = formatMessage(msg);
        }
      }
    }
    if (result == null)
      result = super.getHoverInfo(textViewer, hoverRegion);
    return result;
  }

  //    /* (non-Javadoc)
  //     * @see
  // org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer,
  // int)
  //     */
  //    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
  //    {
  //        return findWord(textViewer.getDocument(), offset);
  //    }

  private IRegion findWord(IDocument document, int offset)
  {

    int start = -1;
    int end = -1;

    try
    {

      int pos = offset;
      char c;

      while (pos >= 0)
      {
        c = document.getChar(pos);
        if (!Character.isJavaIdentifierPart(c))
          break;
        --pos;
      }

      start = pos;

      pos = offset;
      int length = document.getLength();

      while (pos < length)
      {
        c = document.getChar(pos);
        if (!Character.isJavaIdentifierPart(c))
          break;
        ++pos;
      }

      end = pos;

    } catch (BadLocationException x)
    {}

    if (start > -1 && end > -1)
    {
      if (start == offset && end == offset)
        return new Region(offset, 0);
      else if (start == offset)
        return new Region(start, end - start);
      else
        return new Region(start + 1, end - start - 1);
    }

    return null;
  }

  class AnnotationIterator implements Iterator
  {

    private Iterator fIterator;
    private IProblemAnnotation fNext;
    private boolean fSkipIrrelevants;

    public AnnotationIterator(IAnnotationModel model, boolean skipIrrelevants)
    {
      fIterator = model.getAnnotationIterator();
      fSkipIrrelevants = skipIrrelevants;
      skip();
    }

    private void skip()
    {
      while (fIterator.hasNext())
      {
        Object next = fIterator.next();
        if (next instanceof IProblemAnnotation)
        {
          IProblemAnnotation a = (IProblemAnnotation) next;
          if (fSkipIrrelevants)
          {
            if (a.isRelevant())
            {
              fNext = a;
              return;
            }
          } else
          {
            fNext = a;
            return;
          }
        }
      }
      fNext = null;
    }

    /*
     * @see Iterator#hasNext()
     */
    public boolean hasNext()
    {
      return fNext != null;
    }

    /*
     * @see Iterator#next()
     */
    public Object next()
    {
      try
      {
        return fNext;
      } finally
      {
        skip();
      }
    }

    /*
     * @see Iterator#remove()
     */
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }

}