package org.eclipse.pde.internal.ui.editor;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.pde.internal.ui.editor.text.IColorManager;
import org.eclipse.pde.internal.ui.editor.text.IPDEColorConstants;
import org.eclipse.pde.internal.ui.editor.text.NonRuleBasedDamagerRepairer;
import org.eclipse.pde.internal.ui.editor.text.PDEPartitionScanner;
import org.eclipse.pde.internal.ui.editor.text.PDEScanner;
import org.eclipse.pde.internal.ui.editor.text.PDETagScanner;

public class XMLConfiguration extends SourceViewerConfiguration {
	private XMLDoubleClickStrategy doubleClickStrategy;
	private PDETagScanner tagScanner;
	private PDEScanner pdeScanner;
	private IColorManager colorManager;

	public XMLConfiguration(IColorManager colorManager) {
		this.colorManager = colorManager;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			PDEPartitionScanner.XML_COMMENT,
			PDEPartitionScanner.XML_TAG };
	}
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new XMLDoubleClickStrategy();
		return doubleClickStrategy;
	}
	protected PDEScanner getPDEScanner() {
		if (pdeScanner == null) {
			pdeScanner = new PDEScanner(colorManager);
			pdeScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(colorManager.getColor(IPDEColorConstants.P_DEFAULT))));
		}
		return pdeScanner;
	}
	protected PDETagScanner getPDETagScanner() {
		if (tagScanner == null) {
			tagScanner = new PDETagScanner(colorManager);
			tagScanner.setDefaultReturnToken(
				new Token(new TextAttribute(colorManager.getColor(IPDEColorConstants.P_TAG))));
		}
		return tagScanner;
	}
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getPDEScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getPDETagScanner());
		reconciler.setDamager(dr, PDEPartitionScanner.XML_TAG);
		reconciler.setRepairer(dr, PDEPartitionScanner.XML_TAG);

		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(colorManager.getColor(IPDEColorConstants.P_XML_COMMENT)));
		reconciler.setDamager(ndr, PDEPartitionScanner.XML_COMMENT);
		reconciler.setRepairer(ndr, PDEPartitionScanner.XML_COMMENT);

		return reconciler;
	}
}