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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * Rule detecting CSS numbers.
 * 
 * @author Igor Malinin
 */
public class CSSNumberRule implements IRule {
	/** The table of predefined postfixes and token for this rule */
	private Map postfixes = new HashMap();

	/**
	 * The default token to be returned on success and if nothing else
	 * has been specified.
	 */
	private IToken defaultToken;

	public CSSNumberRule(IToken token) {
		this.defaultToken = token;
	}

	public void addPostfix(String postfix, IToken token) {
		postfixes.put(postfix, token);
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int ch = scanner.read();

		boolean sign = (ch == '-' || ch == '+');

		if (sign) {
			ch = scanner.read();
		}

		if (ch == '.') {
			ch = scanner.read();

			if (isDigit(ch)) {
				do {
					ch = scanner.read();
				} while (isDigit(ch));

				scanner.unread();
				return evaluatePostfix(scanner);
			}

			if (sign) {
				scanner.unread();
			}

			scanner.unread();
			scanner.unread();
			return Token.UNDEFINED;
		}

		if (isDigit(ch)) {
			do {
				ch = scanner.read();
				if (ch == '.') {
					do {
						ch = scanner.read();
					} while (isDigit(ch));

					scanner.unread();
					return evaluatePostfix(scanner);
				}
			} while (isDigit(ch));

			scanner.unread();
			return evaluatePostfix(scanner);
		}

		if (sign) {
			scanner.unread();
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	private IToken evaluatePostfix(ICharacterScanner scanner) {
		StringBuffer buf = new StringBuffer();

		int c = scanner.read();
		while (c != ICharacterScanner.EOF && isPostfix((char) c)) {
			buf.append((char) c);
			c = scanner.read();
		};
		scanner.unread();

		IToken token = (IToken) postfixes.get(buf.toString());
		if (token != null) {
			return token;
		}

		return defaultToken;
	}

	private boolean isAlpha(int ch) {
		return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
	}

	private boolean isDigit(int ch) {
		return ('0' <= ch && ch <= '9');
	}

	private boolean isPostfix(int ch) {
		return isAlpha(ch) || isDigit(ch) ||
			ch == '-' || ch == '%' || ch >= 161;
	}
}
