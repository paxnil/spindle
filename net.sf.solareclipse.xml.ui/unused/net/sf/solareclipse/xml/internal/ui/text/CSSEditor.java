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

import net.sf.solareclipse.xml.ui.XMLPlugin;
import net.sf.solareclipse.xml.ui.text.CSSTextTools;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;


/**
 * 
 * 
 * @author Vasanth Dharmaraj
 */
public class CSSEditor extends TextEditor {
	public CSSEditor() {
		setPreferenceStore(XMLPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 */
	protected void initializeEditor() {
		super.initializeEditor();

		CSSTextTools cssTextTools = XMLPlugin.getDefault().getCSSTextTools();

		setSourceViewerConfiguration(new CSSConfiguration(cssTextTools));

		setDocumentProvider(new CSSDocumentProvider());
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#affectsTextPresentation(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return XMLPlugin.getDefault().getCSSTextTools().affectsBehavior(event);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();

		ContentAssistAction assist = new ContentAssistAction(
			XMLPlugin.getDefault().getResourceBundle(),
			"ContentAssistProposal.", this); //$NON-NLS-1$

		assist.setActionDefinitionId(
			ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);

		setAction("ContentAssistProposal", assist); //$NON-NLS-1$
	}
}
