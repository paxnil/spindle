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

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;


/**
 * 
 * 
 * @author Vasanth Dharmaraj
 */
public class CSSPartitionScanner extends RuleBasedPartitionScanner {
	public final static String CSS_DEFAULT = "__css_default";
	public final static String CSS_COMMENT = "__css_comment";

	public CSSPartitionScanner() {
		IToken cssComment = new Token(CSS_COMMENT);

		IPredicateRule[] rules = new IPredicateRule[] {
			new MultiLineRule("/*", "*/", cssComment),
//			TODO <!-- not a real comments -->
//			new MultiLineRule("<!--", "-->", cssComment),
		};

		setPredicateRules(rules);
	}
}
