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

import net.sf.solareclipse.xml.internal.ui.text.DeclScanner;
import net.sf.solareclipse.xml.internal.ui.text.DocumentPartitioner;
import net.sf.solareclipse.xml.internal.ui.text.SingleTokenScanner;
import net.sf.solareclipse.xml.internal.ui.text.TextScanner;
import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;

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
public class DTDTextTools extends AbstractTextTools {
	private static final String[] TOKENS = {
		IXMLSyntaxConstants.XML_DEFAULT,
		IXMLSyntaxConstants.XML_ATT_NAME,
		IXMLSyntaxConstants.XML_ATT_VALUE,
		IXMLSyntaxConstants.XML_ENTITY,
		IXMLSyntaxConstants.XML_PI,
		IXMLSyntaxConstants.XML_COMMENT,
		IXMLSyntaxConstants.XML_DECL,
		IXMLSyntaxConstants.DTD_CONDITIONAL,
	};

	private static final String[] TYPES = {
		XMLPartitionScanner.XML_PI,
		XMLPartitionScanner.XML_COMMENT,
		XMLPartitionScanner.XML_DECL,
		XMLPartitionScanner.DTD_CONDITIONAL,
	};

	/** The DTD partitions scanner */
	private XMLPartitionScanner dtdPartitionScanner;

	/** The DTD text scanner */
	private TextScanner dtdTextScanner;

	/** The DTD conditional sections scanner */
	private SingleTokenScanner dtdConditionalScanner;

	/** The XML processing instructions scanner */
	private SingleTokenScanner xmlPIScanner;

	/** The XML comments scanner */	
	private SingleTokenScanner xmlCommentScanner;

	/** The XML declarations scanner */
	private DeclScanner xmlDeclScanner;

	/**
	 * Creates a new DTD text tools collection.
	 */
	public DTDTextTools( IPreferenceStore store ) {
		super( store, TOKENS );

		dtdPartitionScanner = new XMLPartitionScanner( true );

		Map tokens = getTokens();

		dtdTextScanner = new TextScanner(
			tokens, '%', IXMLSyntaxConstants.XML_DEFAULT );

		dtdConditionalScanner = new SingleTokenScanner(
				tokens, IXMLSyntaxConstants.DTD_CONDITIONAL ); //cond

		xmlPIScanner = new SingleTokenScanner(
				tokens, IXMLSyntaxConstants.XML_PI );

		xmlCommentScanner = new SingleTokenScanner(
				tokens, IXMLSyntaxConstants.XML_COMMENT );

		xmlDeclScanner = new DeclScanner( tokens );
	}

	/**
	 * 
	 */
	public IDocumentPartitioner createDTDPartitioner() {
		return new DocumentPartitioner( dtdPartitionScanner, TYPES );
	}

	/**
	 * 
	 */
	public IPartitionTokenScanner getDTDPartitionScanner() {
		return dtdPartitionScanner;
	}

	/**
	 * Returns a scanner which is configured to scan DTD text.
	 * 
	 * @return  an DTD text scanner
	 */
	public RuleBasedScanner getDTDTextScanner() {
		return dtdTextScanner;
	}

	/**
	 * Returns a scanner which is configured to scan DTD
	 * conditional sections.
	 * 
	 * @return  an DTD conditional section scanner
	 */
	public RuleBasedScanner getDTDConditionalScanner() {
		return dtdConditionalScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML
	 * processing instructions.
	 * 
	 * @return  an XML processing instruction scanner
	 */
	public RuleBasedScanner getXMLPIScanner() {
		return xmlPIScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML comments.
	 * 
	 * @return  an XML comment scanner
	 */
	public RuleBasedScanner getXMLCommentScanner() {
		return xmlCommentScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML declarations.
	 * 
	 * @return  an XML declaration scanner
	 */
	public RuleBasedScanner getXMLDeclScanner() {
		return xmlDeclScanner;
	}
}
