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

import net.sf.solareclipse.editor.I18NDocumentProvider;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 
 * 
 * @author Igor Malinin
 */
public abstract class AbstractDocumentProvider extends I18NDocumentProvider {
	protected IWhitespaceDetector detector = new WhitespaceDetector();

	/*
	 * @see org.eclipse.ui.editors.text.IStorageDocumentProvider#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		return "UTF-8";
	}

	public String getDeclaredEncoding( InputStream in ) throws IOException {
		if ( !in.markSupported() ) {
			in = new BufferedInputStream( in, 512 );
		}

		in.mark( 512 );
		String encoding = super.getDeclaredEncoding( in );
		if ( encoding != null ) {
			return encoding;
		}

		in.reset();

		// check Prolog-Start <?xml
		if ( !skipXMLDecl(in) ) {
			return null;
		}

		// detect 'encoding'
		skipEncoding( in );

		// read encoding
		int delimiter;

		while ( true ) {
			int ch = in.read();
			if ( ch < 0 ) {
				return null;
			}

			if ( detector.isWhitespace((char) ch) ) {
				continue;
			}

			if ( ch == '"' || ch == '\'' ) {
				delimiter = ch;
				break;
			}

			return null;
		}

		StringBuffer buf = new StringBuffer();

		while ( true ) {
			int ch = in.read();
			if ( ch < 0 ) {
				return null;
			}

			if ( ch == delimiter ) {
				break;
			}

			buf.append( (char) ch );
		}

		return buf.toString();
	}

	private boolean skipXMLDecl( InputStream in ) throws IOException {
		int ch = in.read();
		if ( ch != '<' ) {
			return false;
		}

		ch = in.read();
		if ( ch != '?' ) {
			return false;
		}

		ch = in.read();
		if ( ch != 'x' ) {
			return false;
		}

		ch = in.read();
		if ( ch != 'm' ) {
			return false;
		}

		ch = in.read();
		if ( ch != 'l' ) {
			return false;
		}

		return true;
	}

	private boolean skipEncoding( InputStream in ) throws IOException {
		int ch = in.read();

		boolean whitespace = false;

		while ( true ) {
			if ( ch < 0 ) {
				return false;
			}

			if ( detector.isWhitespace((char) ch) ) {
				ch = in.read();
				whitespace = true;
				continue;
			}

			if ( ch == '?' || ch == '<' ) {
				return false;
			}

			if ( ch != 'e' ) {
				ch = in.read();
				whitespace = false;
				continue;
			}

			if ( !whitespace ) {
				ch = in.read();
				continue;
			}

			if ( (ch = in.read()) == 'n' &&
					(ch = in.read()) == 'c' &&
					(ch = in.read()) == 'o' &&
					(ch = in.read()) == 'd' &&
					(ch = in.read()) == 'i' &&
					(ch = in.read()) == 'n' &&
					(ch = in.read()) == 'g' ) {
				break;
			}

			whitespace = false;
		}

		// '='
		while ( true ) {
			ch = in.read();
			if ( ch < 0 ) {
				return false;
			}

			if ( detector.isWhitespace((char) ch) ) {
				continue;
			}

			if ( ch == '=' ) {
				break;
			}

			return false;
		}

		return true;
	}
}
