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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.parse.TemplateParser;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 *  Content assist inside of Tags (but not attributes)
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TagContentAssistProcessor extends ContentAssistProcessor
{

    public TagContentAssistProcessor(TemplateEditor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.template.assist.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        DocumentArtifact tag = getArtifactAt(viewer.getDocument(), documentOffset);
        int baseState = tag.getStateAt(documentOffset);
        if (tag.getType() == DocumentArtifactPartitioner.ENDTAG)
            return new ICompletionProposal[] {};

        boolean addLeadingSpace = false;
        if (baseState == DocumentArtifact.TAG)
        {
            String tagName = tag.getName();
            if (tagName != null)
            {
                System.out.println(tag.getContentTo(documentOffset, false));
                // we are inside , or at the end of the element name.
                // can only insert if we are at the end of the name.
                if (!tag.getContentTo(documentOffset, false).endsWith(tagName))
                    return NoProposals;
            }
            addLeadingSpace = true;

        } else if (baseState == DocumentArtifact.ATT_VALUE)
        {
            return new ICompletionProposal[] {
                 new CompletionProposal(
                    "'" + CompletionProposal.DEFAULT_ATTR_VALUE + "'",
                    documentOffset,
                    0,
                    new Point(1, CompletionProposal.DEFAULT_ATTR_VALUE.length()))};
        } else
        {
            //ensure that we are in a legal position to insert. ie. not inside another attribute name!

            addLeadingSpace = baseState == DocumentArtifact.AFTER_ATT_VALUE;
        }

        List proposals = new ArrayList();
        Map attrmap = tag.getAttributesMap();
        String jwcid = null;
        jwcid = getJwcid(attrmap);

        DocumentArtifact existingAttr = tag.getAttributeAt(documentOffset);
        if (baseState != DocumentArtifact.AFTER_ATT_VALUE
            && existingAttr != null
            && existingAttr.getOffset() < documentOffset)
        {
            // no proposals if the attribute name is jwcid!
            if (TemplateParser.JWCID_ATTRIBUTE_NAME.equalsIgnoreCase(existingAttr.getName()))
            {
                return NoProposals;
            }

            // if there's no jwcid already....
            if (!attrmap.containsKey(TemplateParser.JWCID_ATTRIBUTE_NAME)
                && !attrmap.containsKey(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME))
            {
                if (existingAttr.getStateAt(documentOffset) == DocumentArtifact.TAG)
                {

                    String currentName = existingAttr.getContentTo(documentOffset, false).toLowerCase();

                    if (TemplateParser.JWCID_ATTRIBUTE_NAME.startsWith(currentName))
                    {
                        return new ICompletionProposal[] {
                             CompletionProposal.getAttributeProposal(
                                TemplateParser.JWCID_ATTRIBUTE_NAME.substring(currentName.length()),
                                TemplateParser.JWCID_ATTRIBUTE_NAME,
                                "",
                                null,
                                false,
                                documentOffset)};

                    } else if (TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME.startsWith(currentName))
                    {
                        return new ICompletionProposal[] {
                             CompletionProposal.getAttributeProposal(
                                TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME.substring(currentName.length()),
                                TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
                                "",
                                null,
                                false,
                                documentOffset)};
                    }

                    return NoProposals;
                }
            } else
            {
                // we need to find parameter name replacements
                computeAttributeNameReplacements(
                    documentOffset,
                    existingAttr,
                    jwcid,
                    new HashSet(attrmap.keySet()),
                    proposals);
            }

        } else if (
            !attrmap.containsKey(TemplateParser.JWCID_ATTRIBUTE_NAME)
                && !attrmap.containsKey(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME))
        {
            // we have no Tapestry attributes yet.
            proposals.add(
                CompletionProposal.getAttributeProposal(
                    TemplateParser.JWCID_ATTRIBUTE_NAME,
                    addLeadingSpace,
                    documentOffset));
            proposals.add(
                CompletionProposal.getAttributeProposal(
                    TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
                    addLeadingSpace,
                    documentOffset));

        } else
        {
            computeAttributeProposals(documentOffset, addLeadingSpace, jwcid, new HashSet(attrmap.keySet()), proposals);
        }

        if (proposals.isEmpty())
            return NoSuggestions;

        Collections.sort(proposals, CompletionProposal.PROPOSAL_COMPARATOR);

        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);

    }
    protected void computeAttributeProposals(
        int documentOffset,
        boolean addLeadingSpace,
        String jwcid,
        HashSet existingAttributeNames,
        List proposals)
    {

        Map found = new HashMap();

        try
        {
            ContentAssistHelper helper = new ContentAssistHelper(fEditor);
            IStorage storage = (IStorage) fEditor.getEditorInput().getAdapter(IStorage.class);
            IProject project = TapestryCore.getDefault().getProjectFor(storage);
            helper.setJwcid(
                jwcid,
                (ICoreNamespace) TapestryArtifactManager.getTapestryArtifactManager().getFrameworkNamespace(project));
            ContentAssistHelper.CAHelperResult[] infos = helper.findParameters(null, existingAttributeNames);
            for (int i = 0; i < infos.length; i++)
            {
                CompletionProposal proposal =
                    CompletionProposal.getAttributeProposal(
                        infos[i].name,
                        infos[i].name,
                        CompletionProposal.DEFAULT_ATTR_VALUE,
                        infos[i].description,
                        addLeadingSpace,
                        documentOffset);

                if (infos[i].required)
                    proposal.setImage(Images.getSharedImage("bullet_pink.gif"));
                proposals.add(proposal);

                // now why am I using a map here?
            }
        } catch (IllegalArgumentException e)
        {
            //do nothing
        }
    }

    protected void computeAttributeNameReplacements(
        int documentOffset,
        DocumentArtifact existingAttribute,
        String jwcid,
        HashSet existingAttributeNames,
        List proposals)
    {
        String name = existingAttribute.getName();
        String fragment = existingAttribute.getContentTo(documentOffset, false);
        if (fragment.length() > name.length())
            return;

        int replacementOffset = existingAttribute.getOffset();
        int replacementLength = name.length();

        if (fragment.length() == 0)
            fragment = null;

        try
        {
            // first get the matches
            ContentAssistHelper helper = new ContentAssistHelper(fEditor);
            helper.setJwcid(jwcid);

            ContentAssistHelper.CAHelperResult[] infos = helper.findParameters(fragment, existingAttributeNames);
            for (int i = 0; i < infos.length; i++)
            {
                CompletionProposal proposal =
                    new CompletionProposal(
                        infos[i].name,
                        replacementOffset,
                        replacementLength,
                        new Point(infos[i].name.length(), 0),
                        infos[i].required
                            ? Images.getSharedImage("bullet_pink.gif")
                            : Images.getSharedImage("bullet.gif"),
                        null,
                        null,
                        infos[i].description);

                proposals.add(proposal);
                existingAttributeNames.add(infos[i].name.toLowerCase());
            }

            //then get the replaces

            infos = helper.findParameters(null, existingAttributeNames);
            for (int i = 0; i < infos.length; i++)
            {
                CompletionProposal proposal =
                    new CompletionProposal(
                        infos[i].name,
                        replacementOffset,
                        replacementLength,
                        new Point(infos[i].name.length(), 0),
                        infos[i].required
                            ? Images.getSharedImage("bullet_weird.gif")
                            : Images.getSharedImage("bullet_d.gif"),
                        null,
                        null,
                        infos[i].description);

                proposal.setYOrder(1);
                proposals.add(proposal);
            }

        } catch (IllegalArgumentException e)
        {
            //do nothing
        }
    }
}
