/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.ui.wizards.source;

import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;

/**
 * TODO Add Type comment
 * 
 * @author glongman@gmail.com
 */
public class SimpleSourceViewerConfiguration extends SourceViewerConfiguration
{

  XMLTextTools fTextTools = UIPlugin.getDefault().getXMLTextTools();
  IPreferenceStore fPreferenceStore = UIPlugin.getDefault().getPreferenceStore();

  public SimpleSourceViewerConfiguration()
  {
    super();
  }

  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
  {
    PresentationReconciler reconciler = new PresentationReconciler();

    DefaultDamagerRepairer dr;

    dr = new DefaultDamagerRepairer(fTextTools.getXMLTextScanner());
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    dr = new DefaultDamagerRepairer(fTextTools.getDTDTextScanner());
    reconciler.setDamager(dr, XMLPartitionScanner.DTD_INTERNAL);
    reconciler.setRepairer(dr, XMLPartitionScanner.DTD_INTERNAL);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLPIScanner());

    reconciler.setDamager(dr, XMLPartitionScanner.XML_PI);
    reconciler.setRepairer(dr, XMLPartitionScanner.XML_PI);
    reconciler.setDamager(dr, XMLPartitionScanner.DTD_INTERNAL_PI);
    reconciler.setRepairer(dr, XMLPartitionScanner.DTD_INTERNAL_PI);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLCommentScanner());

    reconciler.setDamager(dr, XMLPartitionScanner.XML_COMMENT);
    reconciler.setRepairer(dr, XMLPartitionScanner.XML_COMMENT);
    reconciler.setDamager(dr, XMLPartitionScanner.DTD_INTERNAL_COMMENT);
    reconciler.setRepairer(dr, XMLPartitionScanner.DTD_INTERNAL_COMMENT);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLDeclScanner());

    reconciler.setDamager(dr, XMLPartitionScanner.XML_DECL);
    reconciler.setRepairer(dr, XMLPartitionScanner.XML_DECL);
    reconciler.setDamager(dr, XMLPartitionScanner.DTD_INTERNAL_DECL);
    reconciler.setRepairer(dr, XMLPartitionScanner.DTD_INTERNAL_DECL);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLTagScanner());

    reconciler.setDamager(dr, XMLPartitionScanner.XML_TAG);
    reconciler.setRepairer(dr, XMLPartitionScanner.XML_TAG);

    reconciler.setDamager(dr, XMLPartitionScanner.XML_ATTRIBUTE);
    reconciler.setRepairer(dr, XMLPartitionScanner.XML_ATTRIBUTE);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLAttributeScanner());

    reconciler.setDamager(dr, XMLPartitionScanner.XML_ATTRIBUTE);
    reconciler.setRepairer(dr, XMLPartitionScanner.XML_ATTRIBUTE);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLCDATAScanner());

    reconciler.setDamager(dr, XMLPartitionScanner.XML_CDATA);
    reconciler.setRepairer(dr, XMLPartitionScanner.XML_CDATA);

    return reconciler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
   */
  public int getTabWidth(ISourceViewer sourceViewer)
  {
    return fPreferenceStore.getInt(PreferenceConstants.FORMATTER_TAB_SIZE);
  }

}