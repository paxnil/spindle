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

package com.iw.plugins.spindle.editors.util;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 *  Rule for producing DocumentArtifactss
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class DocumentArtifactRule implements IPredicateRule
{

    public static final IToken TAG = new Token(DocumentArtifactPartitioner.TAG);
    public static final IToken ENDTAG = new Token(DocumentArtifactPartitioner.ENDTAG);
    public static final IToken TEXT = new Token(DocumentArtifactPartitioner.TEXT);
    public static final IToken PI = new Token(DocumentArtifactPartitioner.PI);
    public static final IToken DECLARATION = new Token(DocumentArtifactPartitioner.DECL);
    public static final IToken COMMENT = new Token(DocumentArtifactPartitioner.COMMENT);
    public static final IToken EMPTYTAG = new Token(DocumentArtifactPartitioner.EMPTYTAG);

    /* (non-Javadoc)
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

    private int scanTo(ICharacterScanner scanner, String end, char escapeChar, boolean isTagScan)
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
                    if (Character.isWhitespace((char)c)) {
                        if (!nameStarted) {
                            scanner.unread();
                            return c;
                        }
                            
                    } else {
                        nameStarted = true;
                    }
                    break;
            }
        } while (c != -1);
        return c;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner, boolean)
     */
    public IToken evaluate(ICharacterScanner scanner, boolean resume)
    {
        return evaluate(scanner);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
     */
    public IToken getSuccessToken()
    {
        return DECLARATION;
    }

}
