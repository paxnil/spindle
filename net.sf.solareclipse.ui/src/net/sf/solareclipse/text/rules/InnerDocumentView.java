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
package net.sf.solareclipse.text.rules;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextStore;


/**
 * Inner view to parent document.
 * 
 * @author Igor Malinin
 */
public class InnerDocumentView
	extends AbstractDocument
	implements IDocumentView
{
	/**
	 * Implements ITextStore based on IDocument.
	 */
	class TextStore implements ITextStore {
		/*
		 * @see ITextStore#set
		 */
		public void set(String txt) {
			try {
				parent.replace(range.offset, range.length, txt);
			} catch (BadLocationException x) {}
		}

		/*
		 * @see ITextStore#replace
		 */
		public void replace(int offset, int length, String txt) {
			try {
				parent.replace(range.offset + offset, length, txt);
			} catch (BadLocationException x) {}
		}

		/*
		 * @see ITextStore#getLength
		 */
		public int getLength() {
			return range.length;
		}

		/*
		 * @see ITextStore#get
		 */
		public String get(int offset, int length) {
			try {
				return parent.get(range.offset + offset, length);
			} catch (BadLocationException x) {}

			return null;
		}

		/*
		 * @see ITextStore#get
		 */
		public char get(int offset) {
			try {
				return parent.getChar(range.offset + offset);
			} catch (BadLocationException x) {}

			return (char) 0;
		}
	};

	/** The parent document */
	IDocument parent;

	/** The section inside the parent document */
	ViewNode range;

	/**
	 * Constructs inner view to parent document.
	 * 
	 * @param parent  parent document
	 * @param range
	 */
	public InnerDocumentView(IDocument parent, ViewNode range) {
		this.parent = parent;
		this.range = range;

		setTextStore(new TextStore());
		setLineTracker(new DefaultLineTracker());
		getTracker().set(getStore().get(0, getLength()));
		completeInitialization();
	}

	/*
	 * @see net.sf.solareclipse.text.rules.IDocumentView#getParentDocument()
	 */
	public IDocument getParentDocument() {
		return parent;
	}

	/*
	 * @see org.eclipse.jface.text.AbstractDocument#fireDocumentAboutToBeChanged(DocumentEvent)
	 */
	protected void fireDocumentAboutToBeChanged(DocumentEvent event) {
		super.fireDocumentAboutToBeChanged(event);
	}

	/*
	 * @see org.eclipse.jface.text.AbstractDocument#fireDocumentChanged(DocumentEvent)
	 */
	protected void fireDocumentChanged(DocumentEvent event) {
		try {
			// TODO: move to a better place
			getTracker().replace(
				event.getOffset(), event.getLength(), event.getText());
		} catch (BadLocationException x) {}

		super.fireDocumentChanged(event);
	}

	/*
	 * @see net.sf.solareclipse.text.rules.IDocumentView#getParentOffset(int)
	 */
	public int getParentOffset(int localOffset) {
		return localOffset + range.offset;
	}

	/*
	 * @see net.sf.solareclipse.text.rules.IDocumentView#getLocalOffset(int)
	 */
	public int getLocalOffset(int parentOffset) {
		return parentOffset - range.offset;
	}
}
