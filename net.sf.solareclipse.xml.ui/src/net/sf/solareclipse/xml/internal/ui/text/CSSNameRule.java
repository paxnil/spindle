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
 * Rule detecting CSS numbers.
 * 
 * @author Igor Malinin
 */
public class CSSNameRule implements IRule {
	private int start;
	private IToken token;

	public CSSNameRule(IToken token) {
		this.start = -1;
		this.token = token;
	}

	public CSSNameRule(char start, IToken token) {
		this.start = start;
		this.token = token;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		if (start >= 0) {
			int ch = scanner.read();
			if (ch != start) {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}

		skipIdentifier(scanner);

		return token;
	}

	private void skipIdentifier(ICharacterScanner scanner) {
		int ch = scanner.read();

		if (isDigit(ch) || ch == '-') {
			scanner.unread();
			return;
		}

		while (true) {
			if (ch == '\\') {
				skipEscape(scanner);
			}

			if (!isIdentifierChar(ch)) {
				scanner.unread();
				return;
			}

			ch = scanner.read();
		}
	}

	private void skipEscape(ICharacterScanner scanner) {
		int ch = scanner.read();
		if (isHex(ch)) {
			for (int n = 0; n <= 6; n++) {
				ch = scanner.read();
				if (!isHex(ch)) {
					if (ch != ' ') {
						scanner.unread();
					}
					return;
				}
			}

			scanner.unread();
			return;
		}

		if (ch == '\r' || ch == '\n') {
			scanner.unread();
		}

		return;
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
