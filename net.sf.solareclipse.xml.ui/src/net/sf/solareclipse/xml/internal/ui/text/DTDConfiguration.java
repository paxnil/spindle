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
import net.sf.solareclipse.xml.ui.text.DTDTextTools;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


/**
 * DTD editor configuration.
 * 
 * @author Igor Malinin
 */
public class DTDConfiguration extends SourceViewerConfiguration {
	private DTDTextTools dtdTextTools;

	private ITextDoubleClickStrategy dcsDefault;
	private ITextDoubleClickStrategy dcsSimple;

	public DTDConfiguration( DTDTextTools tools ) {
		dtdTextTools = tools;

		dcsDefault = new TextDoubleClickStrategy();
		dcsSimple = new SimpleDoubleClickStrategy();
	}

	/*
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer, String contentType
	) {
		if ( XMLPartitionScanner.XML_PI.equals(contentType) ) {
			return dcsSimple;
		}

		if ( XMLPartitionScanner.XML_COMMENT.equals(contentType) ) {
			return dcsSimple;
		}

		if ( XMLPartitionScanner.XML_DECL.equals(contentType) ) {
			return dcsSimple;
		}

		if ( XMLPartitionScanner.DTD_CONDITIONAL.equals(contentType) ) {
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
			XMLPartitionScanner.DTD_CONDITIONAL,
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

		dr = new DefaultDamagerRepairer( dtdTextTools.getDTDTextScanner() );
		reconciler.setDamager( dr, IDocument.DEFAULT_CONTENT_TYPE );
		reconciler.setRepairer( dr, IDocument.DEFAULT_CONTENT_TYPE );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_PI );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_PI );

		dr = new DefaultDamagerRepairer( dtdTextTools.getXMLPIScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_PI );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_PI );

		dr = new DefaultDamagerRepairer( dtdTextTools.getXMLCommentScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_COMMENT );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_COMMENT );

		dr = new DefaultDamagerRepairer( dtdTextTools.getXMLDeclScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.XML_DECL );
		reconciler.setRepairer( dr, XMLPartitionScanner.XML_DECL );

		dr = new DefaultDamagerRepairer( dtdTextTools.getDTDConditionalScanner() );

		reconciler.setDamager( dr, XMLPartitionScanner.DTD_CONDITIONAL );
		reconciler.setRepairer( dr, XMLPartitionScanner.DTD_CONDITIONAL );

		return reconciler;
	}
}
