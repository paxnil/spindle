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

import org.eclipse.jface.text.rules.IWhitespaceDetector;


/**
 * XML white-space detector.
 * 
 * @author Igor Malinin
 */
public class WhitespaceDetector implements IWhitespaceDetector {
	/**
	 * @see IWhitespaceDetector#isWhitespace(char)
	 */
	public boolean isWhitespace( char ch ) {
		switch ( ch ) {
			case 0x09:
			case 0x0A:
			case 0x0D:
			case 0x20:
				return true;

			default :
				return false;
		}
	}
}
