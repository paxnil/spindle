/**********************************************************************
Copyright (c) 2003  Vasanth Dharmaraj and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:

$Id$
**********************************************************************/
package net.sf.solareclipse.xml.internal.ui.text;

import org.eclipse.jface.text.rules.IWordDetector;


/**
 * 
 * 
 * @author Vasanth Dharmaraj
 */
public class CSSWordDetector implements IWordDetector {
	/*
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
	 */
	public boolean isWordPart(char character) {
		return !Character.isWhitespace(character) && character != '#' &&
			character != ':' && character != ',' && character != ';';
	}

	/*
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
	 */
	public boolean isWordStart(char character) {
		return Character.isUnicodeIdentifierStart(character);
	}
}
