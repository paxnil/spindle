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

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * Rule detecting XML tag brackets and name.
 * 
 * @author Igor Malinin
 */
public class XMLTagRule implements IRule {
	private IToken token;

	public XMLTagRule( IToken token ) {
		this.token = token;
	}

	public IToken evaluate( ICharacterScanner scanner ) {
		int ch = scanner.read();

		if ( ch == '>' ) {
			return token;
		}

		if ( ch == '/' ) {
			ch = scanner.read();
			if ( ch == '>' ) {
				return token;
			}

			scanner.unread();
			scanner.unread();
			return Token.UNDEFINED;
		}

		if ( ch == '<' ) {
			ch = scanner.read();

			if ( ch == '/' ) {
				ch = scanner.read();
			}

			loop: while ( true ) {
				switch ( ch ) {
					case ICharacterScanner.EOF:
					case 0x09:
					case 0x0A:
					case 0x0D:
					case 0x20:
						break loop;
				}

				ch = scanner.read();
			}

			scanner.unread();
			return token;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}
}
