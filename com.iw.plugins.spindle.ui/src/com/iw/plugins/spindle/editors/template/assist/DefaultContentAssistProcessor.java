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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors.template.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.parse.TemplateParser;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.editors.assist.ProposalFactory;
import com.iw.plugins.spindle.editors.assist.usertemplates.UserTemplateCompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.DefaultCompletionProcessor;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Content assist inside of Tags (but not attributes)
 * 
 * @author glongman@gmail.com
 */
public class DefaultContentAssistProcessor extends TemplateContentAssistProcessor
{

  protected UserTemplateCompletionProcessor fUserTemplates;

  public DefaultContentAssistProcessor(TemplateEditor editor)
  {
    super(editor);
    fUserTemplates = new UserTemplateCompletionProcessor(editor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.template.assist.AbstractContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer,
   *              int)
   */
  protected ICompletionProposal[] doComputeCompletionProposals(
      ITextViewer viewer,
      int documentOffset)
  {

    IDocument document = viewer.getDocument();
    XMLNode tag = XMLNode.getArtifactAt(document, documentOffset);
    if (tag == null)
      return fUserTemplates.computeCompletionProposals(viewer, documentOffset);

    int baseState = tag.getStateAt(documentOffset);

    String type = tag.getType();

    if (type == ITypeConstants.TEXT)
      return computeTextProposals(viewer, documentOffset);

    if (type != ITypeConstants.TAG && type != ITypeConstants.EMPTYTAG)
      return NoProposals;

    if (tag.isTerminated() && documentOffset == tag.getOffset() + tag.getLength())
      return NoProposals;

    String tagName = tag.getName();
    boolean addLeadingSpace = false;
    if (baseState == XMLNode.TAG)
    {

      if (tagName != null)
      {
        System.out.println(tag.getContentTo(documentOffset, false));
        // we are inside , or at the end of the element name.
        // can only insert if we are at the end of the name.
        if (!tag.getContentTo(documentOffset, false).endsWith(tagName))
          return NoProposals;
      }
      addLeadingSpace = true;

      //    }
      //    else if (baseState == XMLNode.ATT_VALUE )
      //    {
      //      return new ICompletionProposal[]{new CompletionProposal("'"
      //          + ProposalFactory.DEFAULT_ATTR_VALUE + "'", documentOffset, 0, new
      // Point(
      //          1,
      //          ProposalFactory.DEFAULT_ATTR_VALUE.length()))};
    } else
    {
      //ensure that we are in a legal position to insert. ie. not inside
      // another attribute name!

      addLeadingSpace = baseState == XMLNode.AFTER_ATT_VALUE;
    }

    List proposals = new ArrayList();
    Map attrmap = tag.getAttributesMap();
    String jwcid = null;
    jwcid = getJwcid(attrmap);
    HashSet existingAttributeNames = new HashSet(attrmap.keySet());

    XMLNode existingAttr = tag.getAttributeAt(documentOffset);
    if (baseState != XMLNode.AFTER_ATT_VALUE && existingAttr != null
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
        if (existingAttr.getStateAt(documentOffset) == XMLNode.TAG)
        {

          String currentName = existingAttr
              .getContentTo(documentOffset, false)
              .toLowerCase();

          if (TemplateParser.JWCID_ATTRIBUTE_NAME.startsWith(currentName))
          {
            return new ICompletionProposal[]{ProposalFactory
                .createElementAttributeProposal(
                    document,
                    TemplateParser.JWCID_ATTRIBUTE_NAME,
                    documentOffset,
                    0,
                    addLeadingSpace,
                    null,
                    null,
                    -1)};

          } else if (TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME
              .startsWith(currentName))
          {

            return new ICompletionProposal[]{ProposalFactory
                .createElementAttributeProposal(
                    document,
                    TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
                    documentOffset,
                    0,
                    addLeadingSpace,
                    null,
                    null,
                    -1)};
          }

          return NoProposals;
        }
      } else
      {
        // we need to find parameter name replacements
        computeAttributeNameReplacements(
            document,
            documentOffset,
            existingAttr,
            jwcid,
            tagName,
            new HashSet(attrmap.keySet()),
            proposals);
      }

    } else if (!attrmap.containsKey(TemplateParser.JWCID_ATTRIBUTE_NAME)
        && !attrmap.containsKey(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME))
    {

      List usedNames = new ArrayList();

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

      computeAttributeProposals(
          document,
          documentOffset,
          addLeadingSpace,
          jwcid,
          tagName,
          new HashSet(attrmap.keySet()),
          proposals);

    } else
    {
      computeAttributeProposals(
          document,
          documentOffset,
          addLeadingSpace,
          jwcid,
          tagName,
          new HashSet(attrmap.keySet()),
          proposals);
    }

    if (proposals.isEmpty())
      return NoSuggestions;

    Collections.sort(proposals, ProposalFactory.PROPOSAL_COMPARATOR);

    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);

  }

  protected void computeAttributeProposals(
      IDocument document,
      int documentOffset,
      boolean addLeadingSpace,

      String jwcid,
      String tagName,
      HashSet existingAttributeNames,
      List proposals)
  {

    List excludeName = new ArrayList();

    proposals.addAll(getParameterProposals(
        (TemplateEditor) fEditor,
        document,
        documentOffset,
        0,
        null,
        jwcid,
        existingAttributeNames,
        excludeName,
        addLeadingSpace));

    proposals.addAll(ProposalFactory.getAttributeProposals(
        fDTD,
        document,
        documentOffset,
        0,
        tagName,
        excludeName,
        existingAttributeNames,
        null,
        addLeadingSpace));
  }

  protected void computeAttributeNameReplacements(
      IDocument document,
      int documentOffset,
      XMLNode existingAttribute,
      String jwcid,
      String tagName,
      HashSet existingAttributeNames,
      List proposals)
  {
    List usedNames = new ArrayList();
    String name = existingAttribute.getName();
    String value = existingAttribute.getAttributeValue();
    //get index of whitespace
    String fragment = existingAttribute.getContentTo(documentOffset, false);
    if (fragment.length() > name.length())
      return;

    int replacementOffset = existingAttribute.getOffset();
    int replacementLength = name.length();

    if (fragment.length() == 0)
      fragment = null;

    proposals.addAll(getParameterProposals(
        (TemplateEditor) fEditor,
        document,
        documentOffset,
        0,
        fragment,
        jwcid,
        existingAttributeNames,
        usedNames,
        false));

    proposals.addAll(ProposalFactory.getAttributeProposals(
        fDTD,
        document,
        documentOffset,
        0,
        tagName,
        usedNames,
        existingAttributeNames,
        fragment,
        false));
  }

  private ICompletionProposal[] computeTextProposals(
      ITextViewer viewer,
      int documentOffset)
  {
    ICompletionProposal endTagProposal = DefaultCompletionProcessor
        .computeEndTagProposal(viewer, documentOffset);

    if (endTagProposal != null)
      return new ICompletionProposal[]{endTagProposal};

    return fUserTemplates.computeCompletionProposals(viewer, documentOffset);
  }
}