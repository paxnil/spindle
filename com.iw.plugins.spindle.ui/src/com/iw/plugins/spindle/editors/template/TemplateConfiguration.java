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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors.template;

import net.sf.solareclipse.text.TextDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.AttValueDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.SimpleDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.TagDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultAutoIndentStrategy;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.BaseSourceConfiguration;
import com.iw.plugins.spindle.editors.XMLAutoIndentStrategy;
import com.iw.plugins.spindle.editors.template.assist.AttributeContentAssistProcessor;
import com.iw.plugins.spindle.editors.template.assist.DefaultContentAssistProcessor;
import com.iw.plugins.spindle.editors.template.assist.JWCIDContentAssistProcessor;
import com.iw.plugins.spindle.editors.template.assist.TagContentAssistProcessor;

/**
 * SourceViewerConfiguration for the TemplateEditor
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: TemplateConfiguration.java,v 1.9.2.2 2004/06/22 12:23:34
 *          glongman Exp $
 */
public class TemplateConfiguration extends BaseSourceConfiguration
{
  //TODO debug flag
  public static final boolean DEBUG = false;

  private TemplateTextTools fTextTools;

  private ITextDoubleClickStrategy dcsDefault;
  private ITextDoubleClickStrategy dcsSimple;
  private ITextDoubleClickStrategy dcsTag;
  private ITextDoubleClickStrategy dcsAttValue;

  public TemplateConfiguration(TemplateTextTools tools, TemplateEditor editor,
      IPreferenceStore preferenceStore)
  {
    super(editor, preferenceStore);
    fTextTools = tools;
    dcsDefault = new TextDoubleClickStrategy();
    dcsSimple = new SimpleDoubleClickStrategy();
    dcsTag = new TagDoubleClickStrategy();
    dcsAttValue = new AttValueDoubleClickStrategy();
  }

  /*
   * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer,
   *      String)
   */
  public ITextDoubleClickStrategy getDoubleClickStrategy(
      ISourceViewer sourceViewer,
      String contentType)
  {
    if (TemplatePartitionScanner.XML_COMMENT.equals(contentType))
      return dcsSimple;

    if (TemplatePartitionScanner.XML_PI.equals(contentType))
      return dcsSimple;

    if (TemplatePartitionScanner.XML_TAG.equals(contentType))
      return dcsTag;

    if (TemplatePartitionScanner.XML_ATTRIBUTE.equals(contentType))
      return dcsAttValue;

    if (TemplatePartitionScanner.XML_CDATA.equals(contentType))
      return dcsSimple;

    if (TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE.equals(contentType))
      return dcsAttValue;

    if (contentType.startsWith(TemplatePartitionScanner.DTD_INTERNAL))
      return dcsSimple;

    return dcsDefault;
  }

  /*
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
   */
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
  {
    return new String[]{IDocument.DEFAULT_CONTENT_TYPE, TemplatePartitionScanner.XML_PI,
        TemplatePartitionScanner.XML_COMMENT, TemplatePartitionScanner.XML_DECL,
        TemplatePartitionScanner.XML_TAG, TemplatePartitionScanner.XML_ATTRIBUTE,
        TemplatePartitionScanner.XML_CDATA,
        TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE,
        TemplatePartitionScanner.DTD_INTERNAL, TemplatePartitionScanner.DTD_INTERNAL_PI,
        TemplatePartitionScanner.DTD_INTERNAL_COMMENT,
        TemplatePartitionScanner.DTD_INTERNAL_DECL,};
  }

  /*
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
   */
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
  {
    PresentationReconciler reconciler = new PresentationReconciler();

    DefaultDamagerRepairer dr;

    dr = new DefaultDamagerRepairer(fTextTools.getXMLTextScanner());
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    dr = new DefaultDamagerRepairer(fTextTools.getDTDTextScanner());
    reconciler.setDamager(dr, TemplatePartitionScanner.DTD_INTERNAL);
    reconciler.setRepairer(dr, TemplatePartitionScanner.DTD_INTERNAL);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLPIScanner());

    reconciler.setDamager(dr, TemplatePartitionScanner.XML_PI);
    reconciler.setRepairer(dr, TemplatePartitionScanner.XML_PI);
    reconciler.setDamager(dr, TemplatePartitionScanner.DTD_INTERNAL_PI);
    reconciler.setRepairer(dr, TemplatePartitionScanner.DTD_INTERNAL_PI);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLCommentScanner());

    reconciler.setDamager(dr, TemplatePartitionScanner.XML_COMMENT);
    reconciler.setRepairer(dr, TemplatePartitionScanner.XML_COMMENT);
    reconciler.setDamager(dr, TemplatePartitionScanner.DTD_INTERNAL_COMMENT);
    reconciler.setRepairer(dr, TemplatePartitionScanner.DTD_INTERNAL_COMMENT);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLDeclScanner());

    reconciler.setDamager(dr, TemplatePartitionScanner.XML_DECL);
    reconciler.setRepairer(dr, TemplatePartitionScanner.XML_DECL);
    reconciler.setDamager(dr, TemplatePartitionScanner.DTD_INTERNAL_DECL);
    reconciler.setRepairer(dr, TemplatePartitionScanner.DTD_INTERNAL_DECL);

    dr = new DefaultDamagerRepairer(fTextTools.getTemplateTagScanner());

    reconciler.setDamager(dr, TemplatePartitionScanner.XML_TAG);
    reconciler.setRepairer(dr, TemplatePartitionScanner.XML_TAG);

    dr = new DefaultDamagerRepairer(fTextTools.getJwcidAttributeScanner());

    reconciler.setDamager(dr, TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE);
    reconciler.setRepairer(dr, TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE);

    reconciler.setDamager(dr, TemplatePartitionScanner.XML_ATTRIBUTE);
    reconciler.setRepairer(dr, TemplatePartitionScanner.XML_ATTRIBUTE);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLAttributeScanner());

    reconciler.setDamager(dr, TemplatePartitionScanner.XML_ATTRIBUTE);
    reconciler.setRepairer(dr, TemplatePartitionScanner.XML_ATTRIBUTE);

    dr = new DefaultDamagerRepairer(fTextTools.getXMLCDATAScanner());

    reconciler.setDamager(dr, TemplatePartitionScanner.XML_CDATA);
    reconciler.setRepairer(dr, TemplatePartitionScanner.XML_CDATA);

    reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

    return reconciler;
  }
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer,
   *      java.lang.String)
   */
  public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
  {
    if (DEBUG)
    {
      return new ITextHover()
      {
        public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
        {
          try
          {
            IDocumentProvider provider = getEditor().getDocumentProvider();
            IDocument doc = provider.getDocument(getEditor().getEditorInput());
            String result = doc.getPartition(hoverRegion.getOffset()).getType();

            return result;
          } catch (BadLocationException e)
          {
            return "bad location: " + hoverRegion;
          }
        }

        public IRegion getHoverRegion(ITextViewer textViewer, int offset)
        {
          return new Region(offset, 1);
        }
      };
    }
    return super.getTextHover(sourceViewer, contentType);
  }

  public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
  {
    ContentAssistant assistant = getEditor().getContentAssistant();
    TagContentAssistProcessor contentAssistForTag = new TagContentAssistProcessor(
        (TemplateEditor) getEditor());
    AttributeContentAssistProcessor contentAssistForAttribute = new AttributeContentAssistProcessor(
        (TemplateEditor) getEditor());
    JWCIDContentAssistProcessor contentAssistForJWCID = new JWCIDContentAssistProcessor(
        (TemplateEditor) getEditor());
    DefaultContentAssistProcessor contentAssistForDefault = new DefaultContentAssistProcessor(
        (TemplateEditor) getEditor());

    assistant.setContentAssistProcessor(
        contentAssistForTag,
        TemplatePartitionScanner.XML_TAG);
    assistant.setContentAssistProcessor(
        contentAssistForAttribute,
        TemplatePartitionScanner.XML_ATTRIBUTE);
    assistant.setContentAssistProcessor(
        contentAssistForJWCID,
        TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE);
    assistant.setContentAssistProcessor(
        contentAssistForDefault,
        IDocument.DEFAULT_CONTENT_TYPE);
    assistant.enableAutoActivation(true);
    assistant.setProposalSelectorBackground(UIPlugin
        .getDefault()
        .getSharedTextColors()
        .getColor(new RGB(254, 241, 233)));
    assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
    assistant.install(sourceViewer);

    return assistant;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoIndentStrategy(org.eclipse.jface.text.source.ISourceViewer,
   *      java.lang.String)
   */
  public IAutoIndentStrategy getAutoIndentStrategy(
      ISourceViewer sourceViewer,
      String contentType)
  {
    if (contentType == XMLPartitionScanner.XML_COMMENT
        || contentType == XMLPartitionScanner.XML_CDATA)
      return new DefaultAutoIndentStrategy();
    return new XMLAutoIndentStrategy(UIPlugin.getDefault().getPreferenceStore());
  }

}