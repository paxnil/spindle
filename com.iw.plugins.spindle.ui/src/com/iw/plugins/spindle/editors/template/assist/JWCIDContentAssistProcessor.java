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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.parse.TemplateParser;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.editors.UITapestryAccess;
import com.iw.plugins.spindle.editors.template.TemplateEditor;
import com.iw.plugins.spindle.editors.template.TemplatePartitionScanner;
import com.iw.plugins.spindle.editors.util.CompletionProposal;
import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;

/**
 *  Content assist inside of jwcid attributes
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JWCIDContentAssistProcessor extends ContentAssistProcessor
{

    private int fAtSign;

    private String fAttributeValue;

    private Point fValueLocation;

    private TemplateTapestryAccess fAssistHelper;

    public JWCIDContentAssistProcessor(TemplateEditor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.template.assist.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        XMLNode tag = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
        if (tag.getStateAt(documentOffset) == XMLNode.ATT_VALUE)
            return NoProposals;
        Map attributeMap = tag.getAttributesMap();
        XMLNode jwcidAttr = (XMLNode) attributeMap.get(TemplateParser.JWCID_ATTRIBUTE_NAME);

        fAssistHelper = null;
        try
        {
            fAssistHelper = new TemplateTapestryAccess((TemplateEditor) fEditor);
//            IStorage storage = (IStorage) fEditor.getEditorInput().getAdapter(IStorage.class);
//            IProject project = TapestryCore.getDefault().getProjectFor(storage);
//            fAssistHelper.setFrameworkNamespace(
//                (ICoreNamespace) TapestryArtifactManager.getTapestryArtifactManager().getFrameworkNamespace(project));
            fAssistHelper.setJwcid(jwcidAttr.getAttributeValue());
        } catch (IllegalArgumentException e)
        {
            return NoProposals;
        }

        fAttributeValue = null;
        fValueLocation = null;
        IDocument document = viewer.getDocument();

        try
        {
            ITypedRegion region = document.getPartition(documentOffset);
            fValueLocation = new Point(region.getOffset() + 1, region.getLength() - 1);
            char last = viewer.getDocument().getChar(fValueLocation.x + fValueLocation.y - 1);
            if (last == '\'' || last == '"')
                fValueLocation.y -= 1;
            fAttributeValue = document.get(fValueLocation.x, fValueLocation.y);
            if (fAttributeValue.length() > 0)
            {
                char[] chars = fAttributeValue.toCharArray();
                int i = 0;
                for (; i < chars.length; i++)
                {
                    if (!Character.isWhitespace(chars[i]))
                        break;
                }
                if (i > 0)
                {
                    fValueLocation.x += i;
                    fValueLocation.y -= i;
                    fAttributeValue = document.get(fValueLocation.x, fValueLocation.y);
                }
            }

        } catch (BadLocationException e)
        {
            return NoProposals;
        }

        List proposals;
        fAtSign = fAttributeValue.indexOf('@');
        if (fAtSign >= 0)
        {
            if (documentOffset <= fValueLocation.x + fAtSign)
            {

                proposals = computeSimpleIdProposals(document, documentOffset);
            } else
            {
                proposals = computeImplicitTypeProposals(document, documentOffset);
            }
        } else
        {
            proposals = computeSimpleIdProposals(document, documentOffset);
        }

        if (proposals.isEmpty())
            return NoSuggestions;

        Collections.sort(proposals, CompletionProposal.PROPOSAL_COMPARATOR);

        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);

    }

    private List computeImplicitTypeProposals(IDocument document, int documentOffset)
    {
        List proposals = new ArrayList();
        String matchString = null;

        int startOffset = fValueLocation.x + fAtSign + 1; //don't include the @ symbol itself
        int implicitLength = fValueLocation.y - fAtSign - 1;
        if (implicitLength > 0 && documentOffset > startOffset)
        {
            try
            {
                matchString = document.get(startOffset, documentOffset - startOffset).toLowerCase();
                if (matchString.length() == 0)
                {
                    matchString = null;
                }
            } catch (BadLocationException e)
            {
                //do nothing
            }
        }

        UITapestryAccess.Result[] foundTopLevel = fAssistHelper.getComponents();
        for (int i = 0; i < foundTopLevel.length; i++)
        {
            boolean matches = matchString == null ? true : foundTopLevel[i].name.toLowerCase().startsWith(matchString);
            if (matches)
            {
                int delta = foundTopLevel[i].name.length() - implicitLength;

                proposals.add(
                    new CompletionProposal(
                        foundTopLevel[i].name,
                        startOffset,
                        implicitLength,
                        new Point(implicitLength + delta, 0),
                        Images.getSharedImage("bullet_pink.gif"),
                        foundTopLevel[i].displayName,
                        null,
                        foundTopLevel[i].description));
            }
        }
        UITapestryAccess.Result[] foundChild = fAssistHelper.getAllChildNamespaceComponents();
        for (int i = 0; i < foundChild.length; i++)
        {
            boolean matches = matchString == null ? true : foundChild[i].name.toLowerCase().startsWith(matchString);
            if (matches)
            {
                int delta = foundChild[i].name.length() - implicitLength;

                proposals.add(
                    new CompletionProposal(
                        foundChild[i].name,
                        startOffset,
                        implicitLength,
                        new Point(implicitLength + delta, 0),
                        Images.getSharedImage("bullet_pink.gif"),
                        foundChild[i].displayName,
                        null,
                        foundChild[i].description));
            }
        }
        return proposals;
    }

    /**
     * @param document
     * @param documentOffset
     * @return
     */
    private List computeSimpleIdProposals(IDocument document, int documentOffset)
    {
        List proposals = new ArrayList();

        String matchString = null;

        if (documentOffset > fValueLocation.x)
        {
            try
            {
                matchString = document.get(fValueLocation.x, documentOffset - fValueLocation.x);
            } catch (BadLocationException e)
            {
                //do nothing
            }
        }

        List existing = new ArrayList();
        try
        {
            ITypedRegion[] partitions = document.computePartitioning(0, document.getLength() - 1);
            for (int i = 0; i < partitions.length; i++)
            {

                if (!partitions[i].getType().equals(TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE))
                    continue;

                XMLNode tag = XMLNode.getArtifactAt(document, partitions[i].getOffset());
                XMLNode attr = tag.getAttributeAt(partitions[i].getOffset());
                String temp = attr.getAttributeValue().trim();
                int tempAt = temp.indexOf('@');
                if (tempAt > 0)
                {
                    existing.add(temp.substring(0, tempAt));
                } else
                {
                    existing.add(temp);
                }

            }
        } catch (BadLocationException ex)
        {
            //swallow it
        }

        int oldLength = fAtSign >= 0 ? fAtSign : fValueLocation.y;
        String oldValue = fAttributeValue.substring(0, oldLength);

        if (oldValue.length() > 0)
            existing.add(oldValue);

        UITapestryAccess.Result[] found = fAssistHelper.getSimpleIds();
        for (int i = 0; i < found.length; i++)
        {
            if (existing.contains(found[i].name))
                continue;

            int delta = found[i].name.length() - oldValue.length();

            boolean matched = true;
            if (matchString != null)
                matched = found[i].name.startsWith(matchString);
            if (matched)
            {
                // total length change
                proposals.add(
                    new CompletionProposal(
                        found[i].name,
                        fValueLocation.x,
                        oldValue.length(),
                        fAtSign >= 0 ? new Point(fAtSign + delta, 0) : new Point(fValueLocation.y + delta, 0),
                        Images.getSharedImage("bullet.gif"),
                        null,
                        null,
                        null));
            } else
            {
                CompletionProposal proposal =
                    new CompletionProposal(
                        found[i].name,
                        fValueLocation.x,
                        oldValue.length(),
                        fAtSign >= 0 ? new Point(fAtSign + delta, 0) : new Point(fValueLocation.y + delta, 0),
                        Images.getSharedImage("bullet_d.gif"),
                        null,
                        null,
                        null);

                proposal.setYOrder(1);
                proposals.add(proposal);
            }
        }

        return proposals;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#doComputeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] doComputeContextInformation(ITextViewer viewer, int documentOffset)
    {
        XMLNode tag = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
        Map attributeMap = tag.getAttributesMap();
        XMLNode jwcidAttr = (XMLNode) attributeMap.get(TemplateParser.JWCID_ATTRIBUTE_NAME);
        String attributeValue = jwcidAttr.getAttributeValue();
        fAssistHelper = null;
        try
        {
            fAssistHelper = new TemplateTapestryAccess((TemplateEditor) fEditor);
//            IStorage storage = (IStorage) fEditor.getEditorInput().getAdapter(IStorage.class);
//            IProject project = TapestryCore.getDefault().getProjectFor(storage);
//            fAssistHelper.setFrameworkNamespace(
//                (ICoreNamespace) TapestryArtifactManager.getTapestryArtifactManager().getFrameworkNamespace(project));
            fAssistHelper.setJwcid(attributeValue);
        } catch (IllegalArgumentException e)
        {
            return NoInformation;
        }

        UITapestryAccess.Result result = fAssistHelper.getComponentContextInformation();
        if (result == null)
            return NoInformation;

        return new IContextInformation[] { new ContextInformation(result.displayName, result.description)};
    }

}
