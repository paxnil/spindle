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

package com.iw.plugins.spindle.editors.spec;

import net.sf.solareclipse.xml.internal.ui.text.AttValueDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.SimpleDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.TagDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.BaseSourceConfiguration;
import com.iw.plugins.spindle.editors.DefaultDoubleClickStrategy;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.XMLContentFormatter;
import com.iw.plugins.spindle.editors.XMLFormattingStrategy;
import com.iw.plugins.spindle.editors.spec.assist.AttributeCompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.CDATACompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.CommentCompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.DeclCompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.DefaultCompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.TagCompletionProcessor;
import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;

/**
 *  SourceViewerConfiguration for the TemplateEditor
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SpecConfiguration extends BaseSourceConfiguration
{
    public static final boolean DEBUG = false;

    private XMLTextTools fTextTools;

    private ITextDoubleClickStrategy fDefaultDoubleClick;
    private ITextDoubleClickStrategy dcsSimple;
    private ITextDoubleClickStrategy dcsTag;
    private ITextDoubleClickStrategy dcsAttValue;

    /**
     * @param colorManager
     * @param editor
     */
    public SpecConfiguration(XMLTextTools tools, Editor editor, IPreferenceStore preferenceStore)
    {
        super(editor, preferenceStore);
        fTextTools = tools;
        fDefaultDoubleClick = new DefaultDoubleClickStrategy();
        dcsSimple = new SimpleDoubleClickStrategy();
        dcsTag = new TagDoubleClickStrategy();
        dcsAttValue = new AttValueDoubleClickStrategy();
    }

    /*
     * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
     */
    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType)
    {
        if (XMLPartitionScanner.XML_COMMENT.equals(contentType))
            return dcsSimple;

        if (XMLPartitionScanner.XML_PI.equals(contentType))
            return dcsSimple;

        if (XMLPartitionScanner.XML_TAG.equals(contentType))
            return dcsTag;

        if (XMLPartitionScanner.XML_ATTRIBUTE.equals(contentType))
            return dcsAttValue;

        if (XMLPartitionScanner.XML_CDATA.equals(contentType))
            return dcsSimple;

        if (contentType.startsWith(XMLPartitionScanner.DTD_INTERNAL))
            return dcsSimple;

        return fDefaultDoubleClick;
    }

    /*
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
     */
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
    {
        return new String[] {
            IDocument.DEFAULT_CONTENT_TYPE,
            XMLPartitionScanner.XML_PI,
            XMLPartitionScanner.XML_COMMENT,
            XMLPartitionScanner.XML_DECL,
            XMLPartitionScanner.XML_TAG,
            XMLPartitionScanner.XML_ATTRIBUTE,
            XMLPartitionScanner.XML_CDATA,
            XMLPartitionScanner.DTD_INTERNAL,
            XMLPartitionScanner.DTD_INTERNAL_PI,
            XMLPartitionScanner.DTD_INTERNAL_COMMENT,
            XMLPartitionScanner.DTD_INTERNAL_DECL,
            };
    }
    
    public IContentFormatter getContentFormatter(ISourceViewer sourceViewer)
    {
        IContentFormatter formatter = new XMLContentFormatter(
            new XMLFormattingStrategy(),
            new String[] {DefaultPartitioner.CONTENT_TYPES_CATEGORY,}, UIPlugin.getDefault().getPreferenceStore());
        
        return formatter;
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
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
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
                        return doc.getPartition(hoverRegion.getOffset()).getType();
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
        ContentAssistProcessor tagProcessor = new TagCompletionProcessor(fEditor);
        ContentAssistProcessor commentProcessor = new CommentCompletionProcessor(fEditor);
        ContentAssistProcessor attributeProcessor = new AttributeCompletionProcessor(fEditor);
        ContentAssistProcessor declProcessor = new DeclCompletionProcessor(fEditor);
        ContentAssistProcessor defaultProcessor = new DefaultCompletionProcessor(fEditor);
        ContentAssistProcessor cdataProcessor = new CDATACompletionProcessor(fEditor);

        assistant.setContentAssistProcessor(tagProcessor, XMLPartitionScanner.XML_TAG);
        assistant.setContentAssistProcessor(commentProcessor, XMLPartitionScanner.XML_COMMENT);
        assistant.setContentAssistProcessor(attributeProcessor, XMLPartitionScanner.XML_ATTRIBUTE);
        assistant.setContentAssistProcessor(declProcessor, XMLPartitionScanner.XML_DECL);
        assistant.setContentAssistProcessor(defaultProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(cdataProcessor, XMLPartitionScanner.XML_CDATA);
        assistant.enableAutoActivation(true);
        assistant.enableAutoInsert(false);
        assistant.setProposalSelectorBackground(
            UIPlugin.getDefault().getSharedTextColors().getColor(new RGB(254, 241, 233)));
        assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
        assistant.install(sourceViewer);

        return assistant;
    }

}
