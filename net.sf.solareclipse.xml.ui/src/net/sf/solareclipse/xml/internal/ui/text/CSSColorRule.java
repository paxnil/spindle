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
 * Rule detecting CSS colors.
 * 
 * @author Igor Malinin
 */
public class CSSColorRule implements IRule {
	private IToken token;

	public CSSColorRule(IToken token) {
		this.token = token;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int ch = scanner.read();

		if (ch != '#') {
			scanner.unread();
			return Token.UNDEFINED;
		}

		ch = scanner.read();

		for (int i = 0; i < 3; i++) {
			if (ch < 0 || !isHex(ch)) {
				for (++i; i >= 0; i--) {
					scanner.unread();
				}
				return Token.UNDEFINED;
			}

			ch = scanner.read();
		}

		if (!isIdentifierChar(ch)) {
			scanner.unread();
			return token;
		}

		for (int i = 0; i < 3; i++) {
			if (ch < 0 || !isHex(ch)) {
				for (i += 4; i >= 0; i--) {
					scanner.unread();
				}
				return Token.UNDEFINED;
			}

			ch = scanner.read();
		}

		if (!isIdentifierChar(ch)) {
			scanner.unread();
			return token;
		}

		for (int i = 7; i >= 0; i--) {
			scanner.unread();
		}

		return Token.UNDEFINED;
	}

	private boolean isAlpha(int ch) {
		return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
	}

	private boolean isDigit(int ch) {
		return ('0' <= ch && ch <= '9');
	}

	private boolean isHex(int ch) {
		return ('A' <= ch && ch <= 'F')
			|| ('a' <= ch && ch <= 'f')
			|| ('0' <= ch && ch <= '9');
	}

	private boolean isIdentifierChar(int ch) {
		return isAlpha(ch) || isDigit(ch) || ch == '-' || ch >= 161;
	}
}
