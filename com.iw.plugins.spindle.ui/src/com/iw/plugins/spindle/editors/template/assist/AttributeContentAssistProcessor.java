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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.editors.template.TemplateEditor;
import com.iw.plugins.spindle.editors.util.CompletionProposal;
import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;

/**
 *  Content assist inside of Tags (but not attributes)
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class AttributeContentAssistProcessor extends ContentAssistProcessor
{

    private static String[] MISSPELLINGS = new String[] { "ongl:" };

    public AttributeContentAssistProcessor(TemplateEditor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.template.assist.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        DocumentArtifact tag = DocumentArtifact.getArtifactAt(viewer.getDocument(), documentOffset);
        DocumentArtifact attribute = tag.getAttributeAt(documentOffset);

        int state = attribute.getStateAt(documentOffset);

        if (state == DocumentArtifact.TAG)
            return NoProposals;

        Point valueLocation = null;
        String attributeValue = null;
        try
        {
            IDocument document = viewer.getDocument();
            ITypedRegion region = document.getPartition(documentOffset);
            valueLocation = new Point(region.getOffset() + 1, region.getLength() - 1);
            char last = viewer.getDocument().getChar(valueLocation.x + valueLocation.y - 1);
            if (last == '\'' || last == '"')
                valueLocation.y -= 1;
            attributeValue = document.get(valueLocation.x, valueLocation.y);
            char[] chars = attributeValue.toCharArray();
            int i = 0;
            for (; i < chars.length; i++)
            {
                if (!Character.isWhitespace(chars[i]))
                    break;
            }
            if (i > 0)
            {
                valueLocation.x += i;
                attributeValue = document.get(valueLocation.x, valueLocation.y);
            }

        } catch (BadLocationException e)
        {
            return NoProposals;
        }

        if (attributeValue.startsWith("ognl:"))
            return computeDynamicProposals(documentOffset, attributeValue, valueLocation);

        if (attributeValue.startsWith("message:"))
            return computeMessageProposals(documentOffset, attributeValue, valueLocation);

        // check for misspellings

        String misspell = null;
        for (int i = 0; i < MISSPELLINGS.length; i++)
        {
            if (attributeValue.startsWith(MISSPELLINGS[i]))
            {
                misspell = MISSPELLINGS[i];
                break;
            }
        }
        if (misspell != null)
        {
            int delta = documentOffset - valueLocation.x;

            return new ICompletionProposal[] {
                 new CompletionProposal(
                    "ognl:",
                    valueLocation.x,
                    5,
                    delta <= 0 ? new Point(0, 0) : new Point(delta, 0),
                    Images.getSharedImage("oops.gif"),
                    null,
                    null,
                    null)};
        }

        return computeStaticProposals(documentOffset, attributeValue, valueLocation);
    }

    /**
     * Compute proposals for converting a Dynamic to a Message or a Static binding
     */
    private ICompletionProposal[] computeDynamicProposals(
        int documentOffset,
        String attributeValue,
        Point valueLocation)
    {
        int delta = documentOffset - valueLocation.x;

        ICompletionProposal[] result = new ICompletionProposal[2];
        result[0] =
            new CompletionProposal(
                "message:",
                valueLocation.x,
                5,
                delta <= 0 ? new Point(8, 0) : new Point(delta < 5 ? 8 : delta - 5 + 8, 0),
                Images.getSharedImage("bind-string.gif"),
                "Change to message binding",
                null,
                "change 'ognl' into 'message'");
        result[1] =
            new CompletionProposal(
                "",
                valueLocation.x,
                5,
                delta <= 0 ? new Point(0, 0) : new Point(delta < 5 ? 0 : delta - 5, 0),
                Images.getSharedImage("bind-static.gif"),
                "Change to static binding",
                null,
                "remove 'ognl:'");

        return result;
    }
    

    /**
     * Compute proposals for converting a Message to a Dynamic or a Static binding;
     */
    private ICompletionProposal[] computeMessageProposals(
        int documentOffset,
        String attributeValue,
        Point valueLocation)
    {
        int delta = documentOffset - valueLocation.x;

        ICompletionProposal[] result = new ICompletionProposal[2];
        result[0] =
            new CompletionProposal(
                "ognl:",
                valueLocation.x,
                8,
                delta <= 0 ? new Point(5, 0) : new Point(delta < 8 ? 5 : delta - 8 + 5, 0),
                Images.getSharedImage("bind-dynamic.gif"),
                "Change to dynamic binding",
                null,
                "change 'message' into 'ognl'");
        result[1] =
            new CompletionProposal(
                "",
                valueLocation.x,
                8,
                delta <= 0 ? new Point(0, 0) : new Point(delta < 8 ? 0 : delta - 8, 0),
                Images.getSharedImage("bind-static.gif"),
                "Change to static binding",
                null,
                "remove 'message:'");

        return result;
    }

    /**
     * Compute proposals for converting a Static to a Dynamic or a Message binding;
     */
    private ICompletionProposal[] computeStaticProposals(
        int documentOffset,
        String attributeValue,
        Point valueLocation)
    {
        int delta = documentOffset - valueLocation.x;

        ICompletionProposal[] result = new ICompletionProposal[2];
        result[0] =
            new CompletionProposal(
                "ognl:",
                valueLocation.x,
                0,
                delta <= 0 ? new Point(5, 0) : new Point(delta + 5, 0),
                Images.getSharedImage("bind-dynamic.gif"),
                "Make dynamic binding",
                null,
                "prepend 'ognl:' to '" + attributeValue);
        result[1] =
            new CompletionProposal(
                "message:",
                valueLocation.x,
                0,
                delta <= 0 ? new Point(8, 0) : new Point(delta + 8, 0),
                Images.getSharedImage("bind-string.gif"),
                "Make message binding",
                null,
                "prepend 'message:' to '" + attributeValue);

        return result;
    }

}
