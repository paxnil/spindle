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
import org.eclipse.swt.graphics.Point;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.util.CompletionProposal;

/**
 *  Processor for default content type
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class DefaultCompletionProcessor extends SpecCompletionProcessor
{

    public DefaultCompletionProcessor(Editor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        XMLNode artifact = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
        if (artifact.getOffset() + artifact.getLength() == documentOffset)
            artifact = artifact.getNextArtifact();

        XMLNode nextArtifact = artifact.getNextArtifact();
        if (nextArtifact.getType() != XMLDocumentPartitioner.ENDTAG)
            artifact = nextArtifact;

        List proposals = new ArrayList();

        List rawProposals = findRawNewTagProposals(fDTD, artifact, documentOffset);
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
        ICompletionProposal endTagProposal = computeEndTagProposal(viewer, documentOffset);
        if (endTagProposal != null)
            proposals.add(0,endTagProposal);
        proposals.add(SpecTapestryAccess.getDefaultInsertCommentProposal(documentOffset, 0));

        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    /**
     * @param documentOffset
     * @return
     */
    private ICompletionProposal computeEndTagProposal(ITextViewer viewer, int documentOffset)
    {
        XMLNode artifact = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
        XMLNode parentArtifact = artifact.getParent();
        if (parentArtifact == null
            || parentArtifact.getType().equals("/")
            || parentArtifact.getType() != XMLDocumentPartitioner.TAG)
            return null;

        String parentName = parentArtifact.getName();
        if (parentName == null)
            return null;
        ;

        XMLNode corr = parentArtifact.getCorrespondingNode();
        String corrName = null;
        if (corr != null)
            corrName = corr.getName();

        if (corr == null || (corrName != null && !corrName.equals(parentName)))
            return new CompletionProposal(
                "</" + parentName + ">",
                documentOffset,
                0,
                new Point(parentName.length() + 3, 0),
                Images.getSharedImage("bullet.gif"), null, null, null);

        return null;
    }
}
