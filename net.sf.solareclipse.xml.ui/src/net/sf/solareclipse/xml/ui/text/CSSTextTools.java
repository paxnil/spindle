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
package net.sf.solareclipse.xml.ui.text;

import net.sf.solareclipse.xml.internal.ui.text.CSSPartitionScanner;
import net.sf.solareclipse.xml.internal.ui.text.CSSTextScanner;
import net.sf.solareclipse.xml.internal.ui.text.DocumentPartitioner;
import net.sf.solareclipse.xml.internal.ui.text.SingleTokenScanner;

import net.sf.solareclipse.text.AbstractTextTools;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;

import java.util.Map;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class CSSTextTools extends AbstractTextTools {
	private static final String[] TOKENS = {
		ICSSSyntaxConstants.CSS_DEFAULT,
		ICSSSyntaxConstants.CSS_COMMENT,
		ICSSSyntaxConstants.CSS_ID_RULE,
		ICSSSyntaxConstants.CSS_AT_RULE,
		ICSSSyntaxConstants.CSS_PROPERTY,
		ICSSSyntaxConstants.CSS_VALUE,
// TODO: colors
//		ICSSSyntaxConstants.CSS_CLASS,
		ICSSSyntaxConstants.CSS_NUMBER,
		ICSSSyntaxConstants.CSS_STRING,
	};

	private static final String[] TYPES = {
		CSSPartitionScanner.CSS_COMMENT,
	};

	/** The CSS partitions scanner */
	private CSSPartitionScanner cssPartitionScanner;

	/** The CSS text scanner */
	private CSSTextScanner cssTextScanner;

	/** The CSS comments scanner */	
	private SingleTokenScanner cssCommentScanner;

	/**
	 * Creates a new CSS text tools collection.
	 */
	public CSSTextTools(IPreferenceStore store) {
		super(store, TOKENS);

		cssPartitionScanner = new CSSPartitionScanner();

		Map tokens = getTokens();

		cssTextScanner = new CSSTextScanner(tokens);

		cssCommentScanner =
			new SingleTokenScanner(tokens, ICSSSyntaxConstants.CSS_COMMENT);
	}

	/**
	 * 
	 */
	public IDocumentPartitioner createCSSPartitioner() {
		return new DocumentPartitioner(cssPartitionScanner, TYPES);
	}

	/**
	 * 
	 */
	public IPartitionTokenScanner getCSSPartitionScanner() {
		return cssPartitionScanner;
	}

	/**
	 * Returns a scanner which is configured to scan CSS text.
	 * 
	 * @return  an CSS text scanner
	 */
	public RuleBasedScanner getCSSTextScanner() {
		return cssTextScanner;
	}

	/**
	 * Returns a scanner which is configured to scan CSS comments.
	 * 
	 * @return  an CSS comment scanner
	 */
	public RuleBasedScanner getCSSCommentScanner() {
		return cssCommentScanner;
	}
}
