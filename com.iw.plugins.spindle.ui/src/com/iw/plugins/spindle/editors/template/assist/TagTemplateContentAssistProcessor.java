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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.parse.TemplateParser;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.editors.UITapestryAccess;
import com.iw.plugins.spindle.editors.assist.CompletionProposal;
import com.iw.plugins.spindle.editors.assist.ProposalFactory;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Content assist inside of Tags (but not attributes) TODO make this generic for any processor that
 * presents tag attribute proposals.
 * 
 * @author glongman@intelligentworks.com
 */
public class TagTemplateContentAssistProcessor extends TemplateContentAssistProcessor
{

    public TagTemplateContentAssistProcessor(TemplateEditor editor)
    {
        super(editor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.editors.template.assist.AbstractContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer,
     *      int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer,
            int documentOffset)
    {
        XMLNode tag = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);

        int baseState = tag.getStateAt(documentOffset);
        if (tag.getType() != ITypeConstants.TAG && tag.getType() != ITypeConstants.EMPTYTAG)
            return NoProposals;

        String tagName = tag.getName();
        if (tag.isTerminated()
                && (tagName == null || tag.getOffset() + tagName.length() + 1 >= documentOffset))
            return NoProposals;

        if (baseState == XMLNode.IN_TERMINATOR)
            return NoProposals;

        List proposals;

        boolean addLeadingSpace = false;
        if (baseState == XMLNode.TAG)
        {
            boolean atStart = tag.getOffset() == documentOffset;
            boolean canInsertNewTag = tag.getAttributes().isEmpty() && !tag.isTerminated();

            if (fDTD != null && (atStart || canInsertNewTag))
            {

                String content = tag.getContent();
                int length = tag.getLength();

                int i = 0;
                if (!atStart)
                {
                    for (; i < length; i++)
                    {
                        char character = content.charAt(i);
                        if (character == '\r' || character == '\n')
                            break;
                    }
                }

                int replacementLength = i;

                proposals = ProposalFactory.getRawNewTagProposals(viewer.getDocument(), tag
                        .getOffset(), replacementLength, fDTD, tag);

                if (proposals.isEmpty())
                    return NoSuggestions;

                if (length > 1 && documentOffset > tag.getOffset() + 1)
                {
                    String match = tag.getContentTo(documentOffset, true).trim().toLowerCase();
                    for (Iterator iter = proposals.iterator(); iter.hasNext();)
                    {
                        ICompletionProposal proposal = (ICompletionProposal) iter.next();
                        if (!proposal.getDisplayString().startsWith(match))
                            iter.remove();
                    }
                    if (proposals.isEmpty())
                        return NoSuggestions;

                }
                Collections.sort(proposals, ProposalFactory.PROPOSAL_COMPARATOR);
                return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
                        .size()]);
            }
        }
        else if (baseState == XMLNode.ATT_VALUE)
        {
            return new ICompletionProposal[]
            { new CompletionProposal("'" + ProposalFactory.DEFAULT_ATTR_VALUE + "'",
                    documentOffset, 0, new Point(1, ProposalFactory.DEFAULT_ATTR_VALUE.length())) };
        }
        else
        {
            //ensure that we are in a legal position to insert. ie. not inside
            // another attribute name!

            addLeadingSpace = baseState == XMLNode.AFTER_ATT_VALUE;
        }

        proposals = new ArrayList();

        Map attrmap = tag.getAttributesMap();
        String jwcid = null;
        jwcid = getJwcid(attrmap);
        HashSet existingAttributeNames = new HashSet(attrmap.keySet());

        XMLNode existingAttr = tag.getAttributeAt(documentOffset);
        String attributeName = null;
        String attributeValue = null;
        if (existingAttr != null)
        {
            attributeName = existingAttr.getName();
            attributeValue = existingAttr.getValue();

            if (attributeValue != null)
            {
                if (documentOffset > existingAttr.getOffset())
                {
                    return NoProposals;
                }
                else
                {
                    proposals = computeNewAttributeProposalsWithParameters(
                            viewer.getDocument(),
                            documentOffset,
                            addLeadingSpace,
                            tagName,
                            jwcid,
                            existingAttributeNames);
                }
            }
            else if (existingAttr.getOffset() + attributeName.length() >= documentOffset)
            {
                proposals = computeAllAttributeReplacements(
                        viewer.getDocument(),
                        documentOffset,
                        attrmap,
                        tagName,
                        jwcid,
                        existingAttr);
            }

        }
        else if (!attrmap.containsKey(TemplateParser.JWCID_ATTRIBUTE_NAME)
                && !attrmap.containsKey(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME))
        {
            proposals = computeNewAttributeProposalsNoParameters(
                    viewer.getDocument(),
                    documentOffset,
                    tagName,
                    existingAttributeNames,
                    addLeadingSpace);
        }
        else
        {
            proposals = computeNewAttributeProposalsWithParameters(
                    viewer.getDocument(),
                    documentOffset,
                    addLeadingSpace,
                    tagName,
                    jwcid,
                    existingAttributeNames);
        }

        if (proposals.isEmpty())
            return NoSuggestions;

        Collections.sort(proposals, ProposalFactory.PROPOSAL_COMPARATOR);

        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);

    }

    private List computeNewAttributeProposalsNoParameters(IDocument document, int documentOffset,
            String tagName, HashSet existingAttributeNames, boolean addLeadingSpace)
    {
        List proposals = new ArrayList();

        proposals.add(ProposalFactory.createElementAttributeProposal(
                document,
                TemplateParser.JWCID_ATTRIBUTE_NAME,
                documentOffset,
                0,
                addLeadingSpace,
                null,
                null,
                -1));

        proposals.add(ProposalFactory.createElementAttributeProposal(
                document,
                TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
                documentOffset,
                0,
                addLeadingSpace,
                null,
                null,
                -1));

        proposals.addAll(ProposalFactory.getAttributeProposals(
                fDTD,
                document,
                documentOffset,
                0,
                tagName,
                new ArrayList(),
                existingAttributeNames,
                null,
                addLeadingSpace));

        return proposals;

    }

    protected List computeAllAttributeReplacements(IDocument document, int documentOffset,
            Map attrmap, String tagName, String jwcid, XMLNode existingAttribute)
    {
        List proposals = new ArrayList();
        List usedNames = new ArrayList();

        String attrName = existingAttribute.getName();
        String attrValue = existingAttribute.getAttributeValue();

        if (attrValue != null)
        {
            return Collections.EMPTY_LIST;
        }

        HashSet existingAttributeNames = new HashSet(attrmap.keySet());
        existingAttributeNames.remove(existingAttribute.getName());

        String content = existingAttribute.getContent();
        int start = 0;
        for (; Character.isWhitespace(content.charAt(start)); start++)
            ;
        String prefix = content.substring(start, start + attrName.length()).toLowerCase();

        boolean ignorePrefix = prefix == null || prefix.trim().length() == 0;

        int replacementOffset = existingAttribute.getOffset();
        int replacementLength = existingAttribute.getLength();

        if (TemplateParser.JWCID_ATTRIBUTE_NAME.startsWith(prefix))
        {
            proposals.add(ProposalFactory.createElementAttributeProposal(
                    document,
                    TemplateParser.JWCID_ATTRIBUTE_NAME,
                    replacementOffset,
                    replacementLength,
                    false,
                    null,
                    null,
                    -1));
            usedNames.add(TemplateParser.JWCID_ATTRIBUTE_NAME);
        }

        if (TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME.startsWith(prefix))
        {
            proposals.add(ProposalFactory.createElementAttributeProposal(
                    document,
                    TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
                    replacementOffset,
                    replacementLength,
                    false,
                    null,
                    null,
                    -1));
            usedNames.add(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME);
        }

        if (jwcid != null && jwcid.trim().length() > 0)
        {
            proposals.addAll(getParameterProposals(
                    (TemplateEditor) fEditor,
                    document,
                    replacementOffset,
                    replacementLength,
                    prefix,
                    jwcid,
                    existingAttributeNames,
                    usedNames,
                    false));
        }

        proposals.addAll(ProposalFactory.getAttributeProposals(
                fDTD,
                document,
                replacementOffset,
                replacementLength,
                tagName,
                usedNames,
                existingAttributeNames,
                prefix,
                false));

        return proposals;
    }

    protected List computeNewAttributeProposalsWithParameters(IDocument document,
            int documentOffset, boolean addLeadingSpace, String tagName, String jwcid,
            HashSet existingAttributeNames)
    {
        List proposals = new ArrayList();
        List webAttributeNames = Collections.EMPTY_LIST;

        List usedNames = new ArrayList();

        proposals.addAll(getParameterProposals(
                (TemplateEditor) fEditor,
                document,
                documentOffset,
                0,
                null,
                jwcid,
                existingAttributeNames,
                usedNames,
                addLeadingSpace));

        proposals.addAll(ProposalFactory.getAttributeProposals(
                fDTD,
                document,
                documentOffset,
                0,
                tagName,
                usedNames,
                existingAttributeNames,
                null,
                addLeadingSpace));
        return proposals;
    }

    public IContextInformation[] doComputeContextInformation(ITextViewer viewer, int documentOffset)
    {
        XMLNode tag = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
        int baseState = tag.getStateAt(documentOffset);
        if (tag.getType() == ITypeConstants.ENDTAG)
            return NoInformation;

        Map attrMap = tag.getAttributesMap();

        if (!attrMap.containsKey(TemplateParser.JWCID_ATTRIBUTE_NAME))
            return NoInformation;

        XMLNode attr = tag.getAttributeAt(documentOffset);
        if (attr == null || attr.getName().equalsIgnoreCase(TemplateParser.JWCID_ATTRIBUTE_NAME))
        {
            return NoInformation;
        }

        try
        {
            TemplateTapestryAccess helper = new TemplateTapestryAccess((TemplateEditor) fEditor);
            XMLNode jwcidAttr = (XMLNode) attrMap.get(TemplateParser.JWCID_ATTRIBUTE_NAME);
            helper.setJwcid(jwcidAttr.getAttributeValue());

            UITapestryAccess.Result result = helper.getParameterContextInformation(attr.getName());

            if (result != null)
                return new IContextInformation[]
                { new ContextInformation(result.displayName, result.description) };

        }
        catch (IllegalArgumentException e)
        {
            //do nothing
        }

        return NoInformation;
    }
}