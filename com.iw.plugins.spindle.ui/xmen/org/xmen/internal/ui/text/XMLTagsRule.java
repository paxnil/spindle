/*******************************************************************************
 * Copyright (c) 2000, 2003 Jens Lukowski and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *    Jens Lukowski - initial API and implementation
 *    Geoff Longman - heavily modified for Spindle
 *******************************************************************************/
package org.xmen.internal.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Rule for producing XMLNodes
 * 
 * @author Jens Lukowski (dark_angel@users.sourceforge.net )
 * @version $Id$
 */
public class XMLTagsRule implements IPredicateRule
{

  public static final IToken TAG = new Token(XMLDocumentPartitioner.TAG);
  public static final IToken ENDTAG = new Token(XMLDocumentPartitioner.ENDTAG);
  public static final IToken TEXT = new Token(XMLDocumentPartitioner.TEXT);
  public static final IToken PI = new Token(XMLDocumentPartitioner.PI);
  public static final IToken DECLARATION = new Token(XMLDocumentPartitioner.DECL);
  public static final IToken COMMENT = new Token(XMLDocumentPartitioner.COMMENT);
  public static final IToken EMPTYTAG = new Token(XMLDocumentPartitioner.EMPTYTAG);

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
   */
  public IToken evaluate(ICharacterScanner scanner)
  {
    IToken result = Token.EOF;
    int c = scanner.read();

    if (c == -1)
    {
      return Token.EOF;
    }

    if (c != '<')
    {
      while (c != -1 && c != '<')
      {
        c = scanner.read();
      }

      scanner.unread();

      return TEXT;
    } else
    {
      result = TAG;
      c = scanner.read();

      switch (c)
      {
        case '<' :
          scanner.unread();
          break;
        case '!' :
          result = DECLARATION;
          c = scanner.read();
          if (c == '-')
          {
            c = scanner.read();
            if (c == '-')
            {
              c = scanner.read();
              result = COMMENT;
              c = scanTo(scanner, "-->", '\0', false);
            } else
            {
              c = scanTo(scanner, ">", '"', true);
            }
          } else
          {
            c = scanTo(scanner, ">", '"', true);
          }
          break;
        case '?' :
          result = PI;
          c = scanTo(scanner, "?>", '\0', true);
          break;
        case '>' :
          break;
        case '/' :
          result = ENDTAG;
          c = scanTo(scanner, ">", '"', true);
          break;
        default :
          c = scanTo(scanner, ">", '"', true);
          if (c != -1)
          {
            scanner.unread();
            scanner.unread();
            if (scanner.read() == '/')
            {
              result = EMPTYTAG;
            }
            scanner.read();
          }
          break;
      }
      //            if (c == -1) {
      //                return Token.EOF;
      //            }
    }

    return result;
  }

  private int scanTo(
      ICharacterScanner scanner,
      String end,
      char escapeChar,
      boolean isTagScan)
  {
    int c;
    int i = 0;
    boolean escaped = false;
    c = scanner.read();
    do
    {
      if (!escaped && (isTagScan && c == '<'))
      {
        scanner.unread();
        scanner.unread();
        return scanner.read();
      }
      if (escapeChar != '\0' && escapeChar == c)
      {
        escaped = !escaped;
        i = 0;
      } else if (!escaped)
      {
        if (c == end.charAt(i))
        {
          i++;
        } else if (i > 0)
        {
          i = 0;
        }
      }
      if (i >= end.length())
      {
        return c;
      }
      c = scanner.read();
    } while (c != -1);

    return c;
  }

  private int scanEndTag(ICharacterScanner scanner)
  {
    int c;
    int i = 0;
    boolean nameStarted = false;
    c = scanner.read();
    do
    {
      switch (c)
      {
        case '>' :
          break;
        default :
          if (Character.isWhitespace((char) c))
          {
            if (!nameStarted)
            {
              scanner.unread();
              return c;
            }

          } else
          {
            nameStarted = true;
          }
          break;
      }
    } while (c != -1);
    return c;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner,
   *      boolean)
   */
  public IToken evaluate(ICharacterScanner scanner, boolean resume)
  {
    return evaluate(scanner);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
   */
  public IToken getSuccessToken()
  {
    return DECLARATION;
  }

}