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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.iw.plugins.spindle.editors.util.CompletionProposal;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;

/**
 *  Processor for default content type
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class DefaultCompletionProcessor extends SpecCompletionProcessor
{

    public DefaultCompletionProcessor(AbstractTextEditor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        DocumentArtifact artifact = DocumentArtifact.getArtifactAt(viewer.getDocument(), documentOffset);
        if (artifact.getOffset() + artifact.getLength() == documentOffset)
            artifact = artifact.getNextArtifact();

        artifact = artifact.getNextArtifact();

        List proposals = new ArrayList();
        proposals.add(SpecAssistHelper.getDefaultInsertCommentProposal(documentOffset, 0));

        List rawProposals = getRawNewTagProposals(fDTD, artifact, documentOffset);
        if (rawProposals != null && !rawProposals.isEmpty())
        {
            for (Iterator iterator = rawProposals.iterator(); iterator.hasNext();)
            {
                CompletionProposal p = (CompletionProposal) iterator.next();
                p.setReplacementOffset(documentOffset);
                p.setReplacementLength(0);
                proposals.add(p);
            }
        }
        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
    }
}
