/**********************************************************************
Copyright (c) 2002  Widespace, OU  and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:
	Igor Malinin - initial contribution
    Geoffrey Longman - adapted for Tapestry

Original CVS ID: XMLPartitionScanner.java,v 1.3 2002/08/02 12:27:31 l950637 Exp 
$Id$
**********************************************************************/
package com.iw.plugins.spindle.editors.template;

import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * An extension of XMLPartition scanner that picks up jwcid attributes for Tapestry.
 * 
 * @see#getNextTagToken()
 * 
 */
public class TemplatePartitionScanner implements IPartitionTokenScanner
{
    public static final String XML_PI = XMLPartitionScanner.XML_PI;
    public static final String XML_COMMENT = XMLPartitionScanner.XML_COMMENT;
    public static final String XML_DECL = XMLPartitionScanner.XML_DECL;
    public static final String XML_TAG = XMLPartitionScanner.XML_TAG;
    public static final String XML_ATTRIBUTE = XMLPartitionScanner.XML_ATTRIBUTE;
    public static final String XML_CDATA = XMLPartitionScanner.XML_CDATA;

    public final static String TAPESTRY_JWCID_ATTRIBUTE = "__jwcid_attribute";

    public static final String DTD_INTERNAL = XMLPartitionScanner.DTD_INTERNAL;
    public static final String DTD_INTERNAL_PI = XMLPartitionScanner.DTD_INTERNAL_PI;
    public static final String DTD_INTERNAL_COMMENT = XMLPartitionScanner.DTD_INTERNAL_COMMENT;
    public static final String DTD_INTERNAL_DECL = XMLPartitionScanner.DTD_INTERNAL_DECL;
    public static final String DTD_CONDITIONAL = XMLPartitionScanner.DTD_CONDITIONAL;

    public static final int STATE_DEFAULT = 0;
    public static final int STATE_TAG = 1;
    public static final int STATE_DECL = 2;
    public static final int STATE_CDATA = 4;

    public static final int STATE_INTERNAL = 8;

    private IDocument fDocument;
    private int fBegin;
    private int fEnd;

    private int fOffset;
    private int fLength;

    private int fPosition;
    private int fState;

    private boolean fParsedtd;

    private Map fTokens = new HashMap();

    boolean fIsJWCIDTag = false;

    public TemplatePartitionScanner(boolean parsedtd)
    {
        fParsedtd = parsedtd;
    }

    /*
     * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
     */
    public IToken nextToken()
    {
        fOffset += fLength;

        switch (fState)
        {
            case STATE_TAG :
                return nextTagToken();

            case STATE_DECL :
                return nextDeclToken();

            case STATE_CDATA :
                return nextCDATAToken();
        }

        switch (read())
        {
            case ICharacterScanner.EOF :
                fState = STATE_DEFAULT;
                return getToken(null);

            case '<' :
                switch (read())
                {
                    case ICharacterScanner.EOF :
                        if (fParsedtd || isInternal())
                        {
                            break;
                        }

                        fState = STATE_DEFAULT;
                        return getToken(XML_TAG);

                    case '?' : // <?  <?PI
                        return nextPIToken();

                    case '!' : // <!  <!DEFINITION or <![CDATA[ or <!--COMMENT
                        switch (read())
                        {
                            case ICharacterScanner.EOF :
                                fState = STATE_DEFAULT;
                                return getToken(XML_TAG);

                            case '-' : // <!-  <!--COMMENT
                                switch (read())
                                {
                                    case ICharacterScanner.EOF :
                                        return nextDeclToken();

                                    case '-' : // <!--
                                        return nextCommentToken();
                                }

                            case '[' : // <![  <![CDATA[ or <![%cond;[
                                if (fParsedtd)
                                {
                                    return nextConditionalToken();
                                }

                                if (!isInternal())
                                {
                                    return nextCDATAToken();
                                }
                        }

                        return nextDeclToken();
                }

                if (fParsedtd || isInternal())
                {
                    break;
                }

                unread();

                return nextTagToken();

            case ']' :
                if (isInternal())
                {
                    unread();

                    fState = STATE_DECL;
                    fLength = 0;
                    return nextToken();
                }
        }

        loop : while (true)
        {
            switch (read())
            {
                case ICharacterScanner.EOF :
                    fState = STATE_DEFAULT;
                    return getToken(null);

                case '<' :
                    if (fParsedtd || isInternal())
                    {
                        switch (read())
                        {
                            case ICharacterScanner.EOF :
                                fState = STATE_DEFAULT;
                                return getToken(null);

                            case '!' :
                            case '?' :
                                unread();
                                break;

                            default :
                                continue loop;
                        }
                    }

                    unread();

                    fState &= STATE_INTERNAL;
                    return getToken(isInternal() ? DTD_INTERNAL : null);

                case ']' :
                    if (isInternal())
                    {
                        unread();

                        fState = STATE_DECL;
                        if (fPosition == fOffset)
                        {
                            // nothing between
                            fLength = 0;
                            return nextToken();
                        }

                        return getToken(DTD_INTERNAL);
                    }
            }
        }
    }

    private IToken nextTagToken()
    {
        StringBuffer lastRead = new StringBuffer();
        int quot = read();

        switch (quot)
        {
            case ICharacterScanner.EOF :
            case '>' :
                fState = STATE_DEFAULT;
                return getToken(XML_TAG);

            case '"' :
            case '\'' :
                while (true)
                {
                    int ch = read();

                    if (ch == quot)
                    {
                        try
                        {
                            fState = STATE_TAG;

                            if (fIsJWCIDTag)
                                return getToken(TAPESTRY_JWCID_ATTRIBUTE);

                            return getToken(XML_ATTRIBUTE);
                        } finally
                        {
                            fIsJWCIDTag = false;
                        }
                    }

                    switch (ch)
                    {
                        case '<' :
                            unread();

                        case ICharacterScanner.EOF :
                            try
                            {
                                fState = STATE_DEFAULT;

                                if (fIsJWCIDTag)
                                    return getToken(TAPESTRY_JWCID_ATTRIBUTE);

                                return getToken(XML_ATTRIBUTE);
                            } finally
                            {
                                fIsJWCIDTag = false;
                            }
                    }
                }
        }

        while (true)
        {
            int readChar = read();
            lastRead.append((char) readChar);
            switch (readChar)
            {
                case '<' :
                    unread();

                case ICharacterScanner.EOF :
                case '>' :
                    fState = STATE_DEFAULT;
                    return getToken(XML_TAG);

                case '"' :
                case '\'' :
                    unread();

                    //                    System.out.println("XXXXXlastRead=" + lastRead.toString());
                    String toBeChecked = lastRead.toString().toLowerCase();
                    char[] chars = toBeChecked.toCharArray();
                    int pos = chars.length - 1;
                    while (pos > 0)
                    {
                        if (chars[pos] == '\''
                            || chars[pos] == '"'
                            || chars[pos] == '='
                            || Character.isWhitespace(chars[pos]))
                            pos--;
                        else
                            break;
                    }
                    fIsJWCIDTag = pos > 0 && toBeChecked.substring(0, pos + 1).endsWith(" jwcid");

                    fState = STATE_TAG;
                    return getToken(XML_TAG);

            }
        }
    }

    private IToken nextDeclToken()
    {
        loop : while (true)
        {
            switch (read())
            {
                case ICharacterScanner.EOF :
                    fState = STATE_DEFAULT;
                    return getToken(isInternal() ? DTD_INTERNAL_DECL : XML_DECL);

                case '<' :
                    if (fParsedtd || isInternal())
                    {
                        switch (read())
                        {
                            case ICharacterScanner.EOF :
                                fState = STATE_DEFAULT;
                                return getToken(isInternal() ? DTD_INTERNAL : null);

                            case '!' :
                            case '?' :
                                unread();
                                break;

                            default :
                                continue loop;
                        }
                    }

                    unread();

                case '>' :
                    fState &= STATE_INTERNAL;
                    return getToken(isInternal() ? DTD_INTERNAL_DECL : XML_DECL);

                case '[' : // <!DOCTYPE xxx [dtd]>
                    if (!isInternal())
                    {
                        fState = STATE_INTERNAL;
                        return getToken(XML_DECL);
                    }
            }
        }
    }

    private IToken nextCommentToken()
    {
        fState &= STATE_INTERNAL;

        loop : while (true)
        {
            switch (read())
            {
                case ICharacterScanner.EOF :
                    break loop;

                case '-' : // -  -->
                    switch (read())
                    {
                        case ICharacterScanner.EOF :
                            break loop;

                        case '-' : // --  -->
                            switch (read())
                            {
                                case ICharacterScanner.EOF :
                                case '>' :
                                    break loop;
                            }

                            unread();
                            break loop;
                    }
            }
        }

        return getToken(isInternal() ? DTD_INTERNAL_COMMENT : XML_COMMENT);
    }

    private IToken nextPIToken()
    {
        fState &= STATE_INTERNAL;

        loop : while (true)
        {
            switch (read())
            {
                case ICharacterScanner.EOF :
                    break loop;

                case '?' : // ?  ?>
                    switch (read())
                    {
                        case ICharacterScanner.EOF :
                        case '>' :
                            break loop;
                    }

                    unread();
            }
        }

        return getToken(isInternal() ? DTD_INTERNAL_PI : XML_PI);
    }

    private IToken nextCDATAToken()
    {
        fState = STATE_DEFAULT;

        loop : while (true)
        {
            switch (read())
            {
                case ICharacterScanner.EOF :
                    break loop;

                case ']' : // ]  ]]>
                    switch (read())
                    {
                        case ICharacterScanner.EOF :
                            break loop;

                        case ']' : // ]]  ]]>
                            switch (read())
                            {
                                case ICharacterScanner.EOF :
                                case '>' : // ]]>
                                    break loop;
                            }

                            unread();
                            unread();
                            continue loop;
                    }
            }
        }

        return getToken(XML_CDATA);
    }

    private IToken nextConditionalToken()
    {
        fState = STATE_DEFAULT;

        int level = 1;

        loop : while (true)
        {
            switch (read())
            {
                case ICharacterScanner.EOF :
                    break loop;

                case '<' : // -  -->
                    switch (read())
                    {
                        case ICharacterScanner.EOF :
                            break loop;

                        case '!' : // --  -->
                            switch (read())
                            {
                                case ICharacterScanner.EOF :
                                    break loop;

                                case '[' :
                                    ++level;
                                    continue loop;
                            }

                            unread();
                            continue loop;
                    }

                    unread();
                    continue loop;

                case ']' : // -  -->
                    switch (read())
                    {
                        case ICharacterScanner.EOF :
                            break loop;

                        case ']' : // --  -->
                            switch (read())
                            {
                                case ICharacterScanner.EOF :
                                case '>' :
                                    if (--level == 0)
                                    {
                                        break loop;
                                    }

                                    continue loop;
                            }

                            unread();
                            unread();
                            continue loop;
                    }
            }
        }

        return getToken(DTD_CONDITIONAL);
    }

    private IToken getToken(String type)
    {
        fLength = fPosition - fOffset;

        if (fLength == 0)
        {
            return Token.EOF;
        }

        if (type == null)
        {
            return Token.UNDEFINED;
        }

        IToken token = (IToken) fTokens.get(type);
        if (token == null)
        {
            token = new Token(type);
            fTokens.put(type, token);
        }

        return token;
    }

    private boolean isInternal()
    {
        return (fState & STATE_INTERNAL) != 0;
    }

    private int read()
    {
        if (fPosition >= fEnd)
        {
            return ICharacterScanner.EOF;
        }

        try
        {
            return fDocument.getChar(fPosition++);
        } catch (BadLocationException e)
        {
            --fPosition;
            return ICharacterScanner.EOF;
        }
    }

    private void unread()
    {
        --fPosition;
    }

    /*
     * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenOffset()
     */
    public int getTokenOffset()
    {
        return fOffset;
    }

    /*
     * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
     */
    public int getTokenLength()
    {
        return fLength;
    }

    /*
     * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(IDocument, int, int)
     */
    public void setRange(IDocument document, int offset, int length)
    {
        this.fDocument = document;
        this.fBegin = offset;
        this.fEnd = offset + length;

        this.fOffset = offset;
        this.fPosition = offset;
        this.fLength = 0;
    }

    /*
     * @see org.eclipse.jface.text.rules.IPartitionTokenScanner
     */
    public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset)
    {
        if (contentType == TAPESTRY_JWCID_ATTRIBUTE)
        {
            fState = STATE_TAG;
            fIsJWCIDTag = true;
        } else if (contentType == XML_ATTRIBUTE)
        {
            fState = STATE_TAG;
        } else if (contentType == XML_TAG)
        {
            fState = isContinuationPartition(document, offset) ? STATE_TAG : STATE_DEFAULT;
        } else if (contentType == XML_DECL)
        {
            fState = isContinuationPartition(document, offset) ? STATE_DECL : STATE_DEFAULT;
        } else if (
            contentType == DTD_INTERNAL
                || contentType == DTD_INTERNAL_PI
                || contentType == DTD_INTERNAL_DECL
                || contentType == DTD_INTERNAL_COMMENT)
        {
            fState = STATE_INTERNAL;
        } else
        {
            fState = STATE_DEFAULT;
        }

        setRange(document, partitionOffset, length);
    }

    private boolean isContinuationPartition(IDocument document, int offset)
    {
        try
        {
            String type = document.getContentType(offset - 1);

            if (type != IDocument.DEFAULT_CONTENT_TYPE)
            {
                return true;
            }
        } catch (BadLocationException e)
        {}

        return false;
    }
}
