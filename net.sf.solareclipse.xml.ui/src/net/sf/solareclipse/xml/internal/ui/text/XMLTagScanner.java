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
import org.eclipse.jface.text.rules.WordRule;

import java.util.Map;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class XMLTagScanner extends BufferedRuleBasedScanner {
	/**
	 * Creates a color token scanner.
	 */
	public XMLTagScanner( Map tokens ) {
		setDefaultReturnToken( (Token)
			tokens.get(IXMLSyntaxConstants.XML_DEFAULT) );

		IToken tag = (Token) tokens.get( IXMLSyntaxConstants.XML_TAG );
		IToken attribute = (Token) tokens.get( IXMLSyntaxConstants.XML_ATT_NAME );

		IRule[] rules = {
			new XMLTagRule( tag ),
			new WordRule( new NameDetector(), attribute ),
		};

		setRules( rules );
	}
}
