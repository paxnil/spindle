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

import net.sf.solareclipse.ui.ColorManager;
import net.sf.solareclipse.xml.ui.text.CSSTextTools;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


/**
 * 
 * 
 * @author Vasanth Dharmaraj
 */
public class CSSConfiguration extends SourceViewerConfiguration {
	private CSSTextTools cssTextTools;

	private CSSDoubleClickStrategy doubleClickStrategy;
//	private CSSTextScanner scanner;

	public CSSConfiguration(CSSTextTools tools) {
		cssTextTools = tools;

		doubleClickStrategy = new CSSDoubleClickStrategy();
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			CSSPartitionScanner.CSS_COMMENT,
		};
	}

	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer, String contentType
	) {
		return doubleClickStrategy;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr;

		dr = new DefaultDamagerRepairer(cssTextTools.getCSSTextScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(cssTextTools.getCSSCommentScanner());
		reconciler.setDamager(dr, CSSPartitionScanner.CSS_COMMENT);
		reconciler.setRepairer(dr, CSSPartitionScanner.CSS_COMMENT);

		return reconciler;
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant= new ContentAssistant();

		assistant.setContentAssistProcessor(
			new CSSCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);

		assistant.setContextInformationPopupOrientation(
			IContentAssistant.CONTEXT_INFO_ABOVE);

		// TODO: remove a hack (move to properties)!!!
		ColorManager cm = cssTextTools.getColorManager();
		Color color = cm.getColor("ContentAssistantBackground");
		if (color == null) {
			cm.bindColor("ContentAssistantBackground", new RGB(150, 150, 0));
			color = cm.getColor("ContentAssistantBackground");
		}

		assistant.setContextInformationPopupBackground(color);

		return assistant;
	}
}
