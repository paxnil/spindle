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

import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

import java.util.Map;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class SingleTokenScanner extends RuleBasedScanner {
	/**
	 * Creates a single token scanner.
	 */
	public SingleTokenScanner( Map tokens, String property ) {
		setDefaultReturnToken( (Token) tokens.get(property) );
	}
}
