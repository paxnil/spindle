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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.parse.TemplateParser;
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
        DocumentArtifact jwcidArt = (DocumentArtifact) attrmap.get(TemplateParser.JWCID_ATTRIBUTE_NAME);
        if (jwcidArt != null)
        {
            jwcid = jwcidArt.getAttributeValue();
        }

        DocumentArtifact existingAttr = tag.getAttributeAt(documentOffset);
        if (existingAttr != null && existingAttr.getOffset() < documentOffset)
        {
            if (TemplateParser.JWCID_ATTRIBUTE_NAME.equalsIgnoreCase(existingAttr.getName()))
                return NoProposals;
            //are we inside an attribute name?
            computeAttributeNameReplacements(documentOffset, existingAttr, jwcid, attrmap.keySet(), proposals);

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
            computeAttributeProposals(documentOffset, addLeadingSpace, jwcid, attrmap.keySet(), proposals);
        }

        Collections.sort(proposals, CompletionProposal.PROPOSAL_COMPARATOR);

        proposals.add(new TestProposal(tag == null ? "null" : tag.getStateString(documentOffset) + tag.toString()));
        proposals.add(new TestProposal(tag == null ? "null" : tag.getContentTo(documentOffset, false)));
        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);

    }

    private void computeAttributeProposals(
        int documentOffset,
        boolean addLeadingSpace,
        String jwcid,
        Set existingAttributeNames,
        List proposals)
    {

        Map found = new HashMap();

        try
        {
            ContentAssistHelper helper = new ContentAssistHelper(fEditor);
            helper.setJwcid(jwcid);
            ContentAssistHelper.CAHelperParameterInfo[] infos = helper.findParameters(null, existingAttributeNames);
            for (int i = 0; i < infos.length; i++)
            {
                found.put(infos[i].parameterName, infos[i].description);
            }
        } catch (IllegalArgumentException e)
        {
            return;
        }

        if (!found.isEmpty())
        {

            for (Iterator iter = found.keySet().iterator(); iter.hasNext();)
            {
                String replacementWord = (String) iter.next();
                String extra = (String) found.get(replacementWord);
                proposals.add(
                    CompletionProposal.getAttributeProposal(
                        replacementWord,
                        CompletionProposal.DEFAULT_ATTR_VALUE,
                        extra,
                        addLeadingSpace,
                        documentOffset));
            }
        }

    }

    private void computeAttributeNameReplacements(
        int documentOffset,
        DocumentArtifact existingAttribute,
        String jwcid,
        Set existingAttributeNames,
        List proposals)
    {
        String name = existingAttribute.getName();
        String fragment = existingAttribute.getContentTo(documentOffset, false);
        if (fragment.length() > name.length())
        {
            return;
        }

        if (fragment.length() == 0)
        {
            fragment = null;
        }
        Map matches = new HashMap();
        Map replaces = new HashMap();

        try
        {
            // first get the matches
            ContentAssistHelper helper = new ContentAssistHelper(fEditor);
            helper.setJwcid(jwcid);
            ContentAssistHelper.CAHelperParameterInfo[] infos = helper.findParameters(fragment, existingAttributeNames);
            for (int i = 0; i < infos.length; i++)
            {
                matches.put(infos[i].parameterName, infos[i].description);
            }
            //then get the replaces
            HashSet newSet = new HashSet(existingAttributeNames); 
            newSet.addAll(matches.keySet());

            infos = helper.findParameters(null, newSet);
            for (int i = 0; i < infos.length; i++)
            {
                replaces.put(infos[i].parameterName, infos[i].description);
            }
        } catch (IllegalArgumentException e)
        {
            return;
        }

        if (!matches.isEmpty())
        {
            int replacementOffset = existingAttribute.getOffset();
            int replacementLength = name.length();
            for (Iterator iter = matches.keySet().iterator(); iter.hasNext();)
            {
                String replacementWord = (String) iter.next();
                proposals.add(
                    new CompletionProposal(
                        replacementWord,
                        replacementOffset,
                        replacementLength,
                        new Point(replacementWord.length(), 0),
                        null,
                        replacementWord,
                        null,
                        (String) matches.get(replacementWord)));
            }
        }
        if (!replaces.isEmpty())
        {
            int replacementOffset = existingAttribute.getOffset();
            int replacementLength = name.length();
            for (Iterator iter = replaces.keySet().iterator(); iter.hasNext();)
            {
                String replacementWord = (String) iter.next();
                CompletionProposal prop =
                    new CompletionProposal(
                        replacementWord,
                        replacementOffset,
                        replacementLength,
                        new Point(replacementWord.length(), 0),
                        null,
                        replacementWord + " (replace)",
                        null,
                        (String) replaces.get(replacementWord));
                prop.setYOrder(1);
                proposals.add(prop);
            }
        }
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
            return "TAG";
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
