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

package com.iw.plugins.spindle.editors.spec.assist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.iw.plugins.spindle.editors.util.DocumentArtifact;

/**
 *  Processor for default declType type - only works to insert comments within the
 *  body of the XML
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class CDATACompletionProposal extends SpecCompletionProcessor
{

    /**
     * @param editor
     */
    public CDATACompletionProposal(AbstractTextEditor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        IDocument document = viewer.getDocument();
        DocumentArtifact artifact = DocumentArtifact.getArtifactAt(viewer.getDocument(), documentOffset);

        if (artifact.getParent().getType().equals("/"))
            return NoProposals;

        String content = artifact.getContentTo(documentOffset, false);
        if (content.equals("<!-"))
        {
            return new ICompletionProposal[] {
                 SpecCompletionProcessor.getDefaultInsertCommentProposal(artifact.getOffset(), 3)};
        }
        return NoProposals;
    }

}