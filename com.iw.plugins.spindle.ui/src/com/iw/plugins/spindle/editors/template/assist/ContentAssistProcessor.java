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

package com.iw.plugins.spindle.editors.template.assist;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 *  Content Assist for Templates
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class ContentAssistProcessor implements IContentAssistProcessor
{
    protected static final RuleBasedPartitionScanner SCANNER;
    protected static final ICompletionProposal [] NoProposals = new ICompletionProposal[0];

    static {
        SCANNER = new RuleBasedPartitionScanner();
        SCANNER.setPredicateRules(new IPredicateRule[] { new DocumentArtifactRule()});
    }

    protected TemplateEditor fEditor;
    protected IPreferenceStore fPreferenceStore = UIPlugin.getDefault().getPreferenceStore();
    protected DocumentArtifactPartitioner fAssistParititioner;

    public ContentAssistProcessor(TemplateEditor editor)
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
            System.out.println("boo "+documentOffset);
            IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
            fAssistParititioner.connect(document);

            return doComputeCompletionProposals(viewer, documentOffset);

        } finally
        {
            fAssistParititioner.disconnect();
        }
    }

    protected abstract ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset);

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
    {
        return null;
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

    protected DocumentArtifact getArtifactAt(IDocument doc, int offset)
    {
        try
        {
            Position[] pos = doc.getPositions(DocumentArtifactPartitioner.CONTENT_TYPES_CATEGORY);

            for (int i = 0; i < pos.length; i++)
            {
                if (offset >= pos[i].getOffset() && offset <= pos[i].getOffset() + pos[i].getLength())
                {
                    return (DocumentArtifact) pos[i];
                }
            }
        } catch (BadPositionCategoryException e)
        {
            //do nothing
        }

        return null;
    }

}
