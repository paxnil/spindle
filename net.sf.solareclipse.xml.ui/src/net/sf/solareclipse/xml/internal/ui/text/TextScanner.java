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

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import java.util.Map;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class TextScanner extends BufferedRuleBasedScanner {
	/**
	 * Creates a color scanner for XML text or attribute value.
	 */
	public TextScanner( Map tokens, char startEntity, String defaultProperty ) {
		setDefaultReturnToken( (Token) tokens.get(defaultProperty) );

		IToken entity = (Token) tokens.get( IXMLSyntaxConstants.XML_ENTITY );

		IRule[] rules = {
			new EntityRule( startEntity, entity )
		};

		setRules( rules );
	}
}
