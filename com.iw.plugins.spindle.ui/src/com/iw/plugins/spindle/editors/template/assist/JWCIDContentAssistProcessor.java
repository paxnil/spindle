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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 *  Content assist inside of Tags (but not attributes)
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JWCIDContentAssistProcessor extends ContentAssistProcessor
{

    public JWCIDContentAssistProcessor(TemplateEditor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.template.assist.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        DocumentArtifact tag = getArtifactAt(viewer.getDocument(), documentOffset);

        return new ICompletionProposal[] {
             new TestProposal(tag == null ? "null" : tag.getStateString(documentOffset) + tag.toString())};
    }

    class TestProposal implements ICompletionProposal
    {
        String fLabel = "coming soon!";

        public TestProposal()
        {}

        public TestProposal(String label)
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
            return "JWCID";
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
