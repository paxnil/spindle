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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.spec;

import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;

import com.iw.plugins.spindle.editors.BaseSourceConfiguration;
import com.iw.plugins.spindle.editors.Editor;

/**
 * BasicSpecConfiguration basic configuration for XML (syntax coloring only)
 * Subclasses extend to add neat things like formatting etc.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BasicSpecConfiguration extends BaseSourceConfiguration
{

  protected XMLTextTools fTextTools;

  public BasicSpecConfiguration(XMLTextTools tools, Editor editor,
      IPreferenceStore preferenceStore)
  {
    super(editor, preferenceStore);
    fTextTools = tools;
  }

  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
  {
    return new String[]{IDocument.DEFAULT_CONTENT_TYPE, XMLPartitionScanner.XML_PI,
        XMLPartitionScanner.XML_COMMENT, XMLPartitionScanner.XML_DECL,
        XMLPartitionScanner.XML_TAG, XMLPartitionScanner.XML_ATTRIBUTE,
        XMLPartitionScanner.XML_CDATA, XMLPartitionScanner.DTD_INTERNAL,
        XMLPartitionScanner.DTD_INTERNAL_PI, XMLPartitionScanner.DTD_INTERNAL_COMMENT,
        XMLPartitionScanner.DTD_INTERNAL_DECL,};
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

    reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

    return reconciler;
  }

}