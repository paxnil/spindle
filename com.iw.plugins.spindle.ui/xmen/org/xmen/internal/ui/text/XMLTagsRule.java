/*******************************************************************************
 * Copyright (c) 2000, 2003 Jens Lukowski and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *    Jens Lukowski - initial API and implementation
 *    Geoff Longman -  modified for Spindle
 *******************************************************************************/
package org.xmen.internal.ui.text;

import org.eclipse.jface.text.rules.*;

/**
 * @author jll
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class XMLTagsRule implements IPredicateRule
{

  public static final IToken TAG = new Token("TAG");

  public static final IToken ENDTAG = new Token("ENDTAG");

  public static final IToken TEXT = new Token("TEXT");

  public static final IToken PI = new Token("PI");

  public static final IToken DECLARATION = new Token(ITypeConstants.DECL);

  public static final IToken START_DECLARATION = new Token(ITypeConstants.START_DECL);

  public static final IToken END_DECLARATION = new Token(ITypeConstants.END_DECL);

  public static final IToken COMMENT = new Token("COMMENT");

  public static final IToken EMPTYTAG = new Token("EMPTYTAG");

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

    if (c == ']')
    {
      c = scanTo(scanner, ">", true);

      return END_DECLARATION;
    } else if (c != '<')
    {
      while (c != -1 && c != '<' && c != ']')
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
      case '!':
        result = DECLARATION;
        c = scanner.read();
        if (c == '-')
        {
          c = scanner.read();
          if (c == '-')
          {
            c = scanner.read();
            result = COMMENT;
            c = scanTo(scanner, "-->", false);
          } else
          {
            c = findFirstOf(scanner, '>', '[', true);

            if (c == '>')
            {
              return DECLARATION;
            } else
            {
              return START_DECLARATION;
            }
          }
        } else
        {
          scanner.unread();
          if (isNext(scanner, "[CDATA["))
          {
            result = TEXT;
            c = scanTo(scanner, "]]>", false);
          } else
          {
            c = findFirstOf(scanner, '>', '[', true);

            if (c == '>')
            {
              return DECLARATION;
            } else
            {
              return START_DECLARATION;
            }
          }
        }
        break;
      case '?':
        result = PI;
        c = scanTo(scanner, "?>", false);
        break;
      case '>':
        break;
      case '/':
        result = ENDTAG;
        c = scanTo(scanner, ">", true);
        break;
      default:
        c = scanTo(scanner, ">", true);
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

  /**
   * @param scanner
   * @param string
   * @return
   */
  private boolean isNext(ICharacterScanner scanner, String s)
  {
    int pos = 0;

    while (pos < s.length())
    {
      int c = scanner.read();
      if (c != s.charAt(pos))
      {
        for (int i = 0; i < pos; i++)
        {
          scanner.unread();
        }
        return false;
      }
      pos++;
    }

    return true;
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

  private int scanTo(ICharacterScanner scanner, String end, boolean quoteEscapes)
  {
    int c;
    int i = 0;
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;

    do
    {

      c = scanner.read();
      
      if (c=='<') {
        if (!quoteEscapes) {
          scanner.unread();
          scanner.unread();
          return scanner.read();
        } else if (!(inSingleQuote || inDoubleQuote)) {
          scanner.unread();
          scanner.unread();
          return scanner.read();
        }         
      }

      if (c == '"' && !inSingleQuote && quoteEscapes)
      {
        inDoubleQuote = !inDoubleQuote;
        i = 0;
      } else if (c == '\'' && !inDoubleQuote && quoteEscapes)
      {
        inSingleQuote = !inSingleQuote;
        i = 0;
      } else if (!inSingleQuote && !inDoubleQuote)
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
    } while (c != -1);

    return c;

  }

  private int findFirstOf(
      ICharacterScanner scanner,
      char one,
      char other,
      boolean quoteEscapes)
  {
    int c;
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;

    do
    {
      c = scanner.read();
      if (c == '"' && !inSingleQuote)
      {
        inDoubleQuote = !inDoubleQuote;
      } else if (c == '\'' && !inDoubleQuote)
      {
        inSingleQuote = !inSingleQuote;
      } else if (!inSingleQuote && !inDoubleQuote)
      {
        if (c == one)
        {
          return c;
        } else if (c == other)
        {
          return c;
        }
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
    return Token.EOF;//TEXT;//DECLARATION;
  }

}