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

import net.sf.solareclipse.editor.I18NDocumentProvider;
import net.sf.solareclipse.xml.ui.XMLPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;


/**
 * HTML document provider.
 * 
 * @author Igor Malinin
 */
public class HTMLDocumentProvider extends I18NDocumentProvider {
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument( Object element ) throws CoreException {
		IDocument document = super.createDocument( element );
		if ( document != null ) {
			IDocumentPartitioner partitioner = XMLPlugin
				.getDefault().getXMLTextTools().createXMLPartitioner();

			if ( partitioner != null ) {
				partitioner.connect( document );
				document.setDocumentPartitioner( partitioner );
			}
		}

		return document;
	}
}
