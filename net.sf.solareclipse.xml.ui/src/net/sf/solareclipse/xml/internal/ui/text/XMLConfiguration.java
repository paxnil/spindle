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

import net.sf.solareclipse.text.TextDoubleClickStrategy;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


/**
 * XML editor configuration.
 * 
 * @author Igor Malinin
 */
public class XMLConfiguration extends SourceViewerConfiguration {
	private XMLTextTools xmlTextTools;

	private ITextDoubleClickStrategy dcsDefault;
	private ITextDoubleClickStrategy dcsSimple;
	private ITextDoubleClickStrategy dcsTag;
	private ITextDoubleClickStrategy dcsAttValue;

	public XMLConfiguration( XMLTextTools tools ) {
		xmlTextTools = tools;

		dcsDefault = new TextDoubleClickStrategy();
		dcsSimple = new SimpleDoubleClickStrategy();
		dcsTag = new TagDoubleClickStrategy();
		dcsAttValue = new AttValueDoubleClickStrategy();
	}

	/*
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer, String contentType
	) {
		if ( XMLPartitionScanner.XML_COMMENT.equals(contentType) ) {
			return dcsSimple;
		}

		if ( XMLPartitionScanner.XML_PI.equals(contentType) ) {
			return dcsSimple;
		}

		if ( XMLPartitionScanner.XML_TAG.equals(contentType) ) {
			return dcsTag;
		}

		if ( XMLPartitionScanner.XML_ATTRIBUTE.equals(contentType) ) {
			return dcsAttValue;
		}

		if ( XMLPartitionScanner.XML_CDATA.equals(contentType) ) {
			return dcsSimple;
		}

		if ( contentType.startsWith(XMLPartitionScanner.DTD_INTERNAL) ) {
			return dcsSimple;
		}

		return dcsDefault;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes( ISourceViewer sourceViewer ) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			XMLPartitionScanner.XML_PI,
			XMLPartitionScanner.XML_COMMENT,
			XMLPartitionScanner.XML_DECL,
			XMLPartitionScanner.XML_TAG,
			XMLPartitionScanner.XML_ATTRIBUTE,
			XMLPartitionScanner.XML_CDATA,
			XMLPartitionScanner.DTD_INTERNAL,
			XMLPartitionScanner.DTD_INTERNAL_PI,
			XMLPartitionScanner.DTD_INTERNAL_COMMENT,
			XMLPartitionScanner.DTD_INTERNAL_DECL,
		};
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(
		ISourceViewer sourceViewer
	) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr;

		dr = new DefaultDamagerRepairer( xmlTextTools.getXMLTextScanner() );
		reconciler.setDamager( dr, IDocument.DEFAULT_CONTENT_TYPE );
		reconciler.setRepairer( dr, IDocument.DEFAULT_CONTENT_TYPE );

		dr = new DefaultDamagerRepairer( xmlTextTools.getDTDTextScanner() );
		reconciler.setDamager( dr, XMLPartitionScanner.DTD_INTERNAL );
		reconciler.setRepairer( dr, XMLPartitionScanner.DTD_INTERNAL );

		dr = new DefaultDamagerRepairer( xmlTextTools.getXMLPIScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_PI );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_PI );
		reconciler.setDamager( dr, XMLPartitionScanner.DTD_INTERNAL_PI );
		reconciler.setRepairer( dr, XMLPartitionScanner.DTD_INTERNAL_PI );

		dr = new DefaultDamagerRepairer( xmlTextTools.getXMLCommentScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_COMMENT );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_COMMENT );
		reconciler.setDamager( dr, XMLPartitionScanner.DTD_INTERNAL_COMMENT );
		reconciler.setRepairer( dr, XMLPartitionScanner.DTD_INTERNAL_COMMENT );

		dr = new DefaultDamagerRepairer( xmlTextTools.getXMLDeclScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_DECL );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_DECL );
		reconciler.setDamager( dr, XMLPartitionScanner.DTD_INTERNAL_DECL );
		reconciler.setRepairer( dr, XMLPartitionScanner.DTD_INTERNAL_DECL );

		dr = new DefaultDamagerRepairer( xmlTextTools.getXMLTagScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_TAG );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_TAG );

		dr = new DefaultDamagerRepairer( xmlTextTools.getXMLAttributeScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_ATTRIBUTE );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_ATTRIBUTE );

		dr = new DefaultDamagerRepairer( xmlTextTools.getXMLCDATAScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_CDATA );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_CDATA );

		return reconciler;
	}
}
