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

package com.iw.plugins.spindle.editors.util;

import java.util.Map;

import org.apache.tapestry.parse.TemplateParser;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.iw.plugins.spindle.UIPlugin;

/**
 *  Content Assist for Templates
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class ContentAssistProcessor implements IContentAssistProcessor
{
    protected static final RuleBasedPartitionScanner SCANNER;
    protected static final ICompletionProposal[] NoProposals = new ICompletionProposal[0];
    protected static final ICompletionProposal[] NoSuggestions =
        new ICompletionProposal[] {
            new ContentAssistProcessor.MessageProposal("no suggestions available"),
            new ContentAssistProcessor.MessageProposal("")};

    protected static final IContextInformation[] NoInformation = new IContextInformation[0];

    static {
        SCANNER = new RuleBasedPartitionScanner();
        SCANNER.setPredicateRules(new IPredicateRule[] { new DocumentArtifactRule()});
    }

    protected AbstractTextEditor fEditor;
    protected IPreferenceStore fPreferenceStore = UIPlugin.getDefault().getPreferenceStore();
    protected DocumentArtifactPartitioner fAssistParititioner;
    protected boolean fDoingContextInformation = false;

    public ContentAssistProcessor(AbstractTextEditor editor)
    {
        this.fEditor = editor;
        fAssistParititioner = new DocumentArtifactPartitioner(SCANNER, DocumentArtifactPartitioner.TYPES);
    }

    protected void startCompute()
    {}

    protected void endCompute()
    {}

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        try
        {
            IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
            fAssistParititioner.connect(document);
            Point p = viewer.getSelectedRange();
            if (p.y > 0)
                return NoProposals;

            return doComputeCompletionProposals(viewer, documentOffset);

        } finally
        {
            fAssistParititioner.disconnect();
        }
    }

    protected abstract ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset);

    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
    {
        if (!fDoingContextInformation)
            return null;

        return computeInformation(viewer, documentOffset);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeInformation(ITextViewer viewer, int documentOffset)
    {
        try
        {
            IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
            fAssistParititioner.connect(document);
            //            Point p = viewer.getSelectedRange();
            //            if (p.y > 0)
            //                return NoInformation;

            return doComputeContextInformation(viewer, documentOffset);

        } finally
        {
            fAssistParititioner.disconnect();
        }
    }

    public IContextInformation[] doComputeContextInformation(ITextViewer viewer, int documentOffset)
    {
        return NoInformation;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage()
    { //TODO I10N
        return "no completions available";
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }

    protected String getJwcid(Map attributeMap)
    {
        DocumentArtifact jwcidArt = (DocumentArtifact) attributeMap.get(TemplateParser.JWCID_ATTRIBUTE_NAME);
        if (jwcidArt != null)
            return jwcidArt.getAttributeValue();

        return null;
    }

    public static class MessageProposal implements ICompletionProposal
    {
        String fLabel = "coming soon!";

        public MessageProposal()
        {}

        public MessageProposal(String label)
        {
            fLabel = label;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
         */
        public void apply(IDocument document)
        {}

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
         */
        public String getAdditionalProposalInfo()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
         */
        public IContextInformation getContextInformation()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
         */
        public String getDisplayString()
        {
            return fLabel;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
         */
        public Image getImage()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
         */
        public Point getSelection(IDocument document)
        {
            return null;
        }

    }

}