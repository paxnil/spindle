/**********************************************************************
Copyright (c) 2002  Widespace, OU  and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:
	Igor Malinin - initial contribution

$Id$
**********************************************************************/
package net.sf.solareclipse.xml.internal.ui.text;

import net.sf.solareclipse.xml.ui.text.IXMLSyntaxConstants;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import java.util.Map;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class XMLCDATAScanner implements ITokenScanner {
	private Map tokens;

	private IDocument document;

	private int begin;
	private int end;

	private int offset;
	private int length;

	private int position;

	public XMLCDATAScanner( Map tokens ) {
		this.tokens = tokens;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(IDocument, int, int)
	 */
	public void setRange( IDocument document, int offset, int length ) {
		this.document = document;

		this.begin = offset;
		this.end = offset + length;

		this.offset = offset;
		this.position = offset;
		this.length = 0;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		offset += length;

		if ( position == begin ) {
			position += 3; // <![

			try {
				if ( document.get(position, 6).equals("CDATA[") ) {
					position += 6;
				}
			} catch ( BadLocationException e ) {}

			return getToken( IXMLSyntaxConstants.XML_CDATA );
		}

		if ( position == end ) {
			return getToken( null );
		}

		try {
			int p = end - 3;
			if ( document.get(p, 3).equals("]]>") ) {
				if ( position == p ) {
					position = end;
					return getToken( IXMLSyntaxConstants.XML_CDATA );
				}

				position = p;
			} else {
				position = end;
			}
		} catch ( BadLocationException e ) {}

		return getToken( IXMLSyntaxConstants.XML_DEFAULT );
	}

	private IToken getToken( String type ) {
		length = position - offset;

		if ( length == 0 ) {
			return Token.EOF;
		}

		if ( type == null ) {
			return Token.UNDEFINED;
		}

		IToken token = (IToken) tokens.get( type );
		if ( token == null ) {
			return Token.UNDEFINED;
		}

		return token;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		return offset;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return length;
	}
}
