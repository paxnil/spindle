/**********************************************************************
Copyright (c) 2002  Widespace, OU  and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:

$Id$
**********************************************************************/
package net.sf.solareclipse.xml.internal.ui.text;

import net.sf.solareclipse.xml.ui.XMLPlugin;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractTextEditor;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class XMLMergeViewer extends TextMergeViewer {
	/**
	 * The preference store.
	 */
	private IPreferenceStore preferenceStore;

	/**
	 * The listener for changes to the preference store. 
	 */
	private IPropertyChangeListener propertyChangeListener;

	/**
	 * The XML text tools.
	 */
	private XMLTextTools textTools;

	/*
	 * @see TextMergeViewer#TextMergeViewer(Composite, int, CompareConfiguration)
	 */
	public XMLMergeViewer(
		Composite parent, int style, CompareConfiguration configuration
	) {
		super(parent, style, configuration);
	}

	// TextMergeViewer Implementation ------------------------------------------

	/*
	 * @see TextMergeViewer#configureTextViewer()
	 */
	protected void configureTextViewer(TextViewer textViewer) {
		XMLPlugin plugin = XMLPlugin.getDefault();

		preferenceStore = plugin.getPreferenceStore();
		if (preferenceStore != null) {
			propertyChangeListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					handlePreferenceStoreChanged(event);
				}
			};
			preferenceStore.addPropertyChangeListener(propertyChangeListener);
		}

		textTools = plugin.getXMLTextTools();

		if (textViewer instanceof SourceViewer) {
			SourceViewer sourceViewer = (SourceViewer) textViewer;
			sourceViewer.configure(new XMLConfiguration(textTools));
		}

		updateBackgroundColor();
	}

	/*
	 * @see TextMergeViewer#getDocumentPartitioner()
	 */
	protected IDocumentPartitioner getDocumentPartitioner() {
		return textTools.createXMLPartitioner();
	}

	/*
	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#getTitle()
	 */
	public String getTitle() {
		return XMLPlugin.getResourceString("XMLMergeViewer.title"); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.viewers.ContentViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
	 */
	protected void handleDispose(DisposeEvent event) {
		if (propertyChangeListener != null) {
			if (preferenceStore != null) {
				preferenceStore
					.removePropertyChangeListener(propertyChangeListener);
			}

			propertyChangeListener = null;
		}

		super.handleDispose(event);
	}

	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String p = event.getProperty();

		if (p.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND) ||
			p.equals(AbstractTextEditor
				.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
		) {
			updateBackgroundColor();
		} else if (textTools.affectsBehavior(event)) {
			invalidateTextPresentation();
		}
	}

	private void updateBackgroundColor() {
		boolean defaultBackgroundColor = preferenceStore.getBoolean(
			AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);

		if (defaultBackgroundColor) {
			setBackgroundColor(null);
		} else {
			RGB backgroundColor = PreferenceConverter
				.getColor(preferenceStore,
					AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
			setBackgroundColor(backgroundColor);
		}
	}
}
