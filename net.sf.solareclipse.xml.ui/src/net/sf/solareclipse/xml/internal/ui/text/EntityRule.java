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
 * Rule detecting XML or DTD entities.
 * 
 * @author Igor Malinin
 */
public class EntityRule implements IRule {
	private static NameDetector detector = new NameDetector();

	private char   start;
	private IToken token;

	public EntityRule( char start, IToken token ) {
		this.start = start;
		this.token = token;
	}

	public IToken evaluate( ICharacterScanner scanner ) {
		int ch = scanner.read();

		if ( ch == start ) {
			ch = scanner.read();

			if ( ch == ICharacterScanner.EOF ) {
				scanner.unread();
				return token;
			}

			if ( ch == ';' ) {
				return token;
			}

			if ( !detector.isWordStart((char) ch) ) {
				scanner.unread();
				return token;
			}

			while ( true ) {
				ch = scanner.read();

				if ( ch == ICharacterScanner.EOF ) {
					scanner.unread();
					return token;
				}

				if ( ch == ';' ) {
					return token;
				}

				if ( !detector.isWordPart((char) ch) ) {
					scanner.unread();
					return token;
				}
			}
		}

		scanner.unread();

		return Token.UNDEFINED;
	}
}
