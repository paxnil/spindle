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

import net.sf.solareclipse.xml.ui.XMLPlugin;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.TextEditor;


/**
 * HTML Editor.
 * 
 * @author Igor Malinin
 */
public class HTMLEditor extends TextEditor {
	public HTMLEditor() {
		setPreferenceStore(XMLPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 */
	protected void initializeEditor() {
		super.initializeEditor();

		XMLTextTools xmlTextTools = XMLPlugin.getDefault().getXMLTextTools();

		setSourceViewerConfiguration(new XMLConfiguration(xmlTextTools));

		setDocumentProvider(new HTMLDocumentProvider());
	}

	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return XMLPlugin.getDefault().getXMLTextTools().affectsBehavior(event);
	}
}
