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
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.editors.DTDProposalGenerator;
import com.iw.plugins.spindle.editors.UITapestryAccess;
import com.iw.plugins.spindle.editors.assist.CompletionProposal;
import com.iw.plugins.spindle.editors.assist.OrderedProposal;
import com.iw.plugins.spindle.editors.assist.ProposalFactory;
import com.iw.plugins.spindle.editors.spec.assist.DefaultCompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.usertemplates.UserTemplateCompletionProcessor;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Content assist inside of Tags (but not attributes)
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: DefaultContentAssistProcessor.java,v 1.2.2.2 2004/06/22
 *                     12:23:59 glongman Exp $
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

    } else if (baseState == XMLNode.ATT_VALUE)
    {
      return new ICompletionProposal[]{new CompletionProposal("'"
          + ProposalFactory.DEFAULT_ATTR_VALUE + "'", documentOffset, 0, new Point(
          1,
          ProposalFactory.DEFAULT_ATTR_VALUE.length()))};
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
            return new ICompletionProposal[]{ProposalFactory.getAttributeProposal(
                TemplateParser.JWCID_ATTRIBUTE_NAME.substring(currentName.length()),
                TemplateParser.JWCID_ATTRIBUTE_NAME,
                "",
                null,
                false,
                documentOffset)};

          } else if (TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME
              .startsWith(currentName))
          {
            return new ICompletionProposal[]{ProposalFactory.getAttributeProposal(
                TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME.substring(currentName
                    .length()),
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

    } else if (!attrmap.containsKey(TemplateParser.JWCID_ATTRIBUTE_NAME)
        && !attrmap.containsKey(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME))
    {

      List webAttributeNames = Collections.EMPTY_LIST;
      OrderedProposal proposal;

      if (fDTD != null && tagName != null)
        webAttributeNames = DTDProposalGenerator.getAttributes(fDTD, tagName);

      AttributeTemplateContext context = new AttributeTemplateContext(
          document,
          documentOffset,
          0,
          addLeadingSpace);
      context.setAttributeName(TemplateParser.JWCID_ATTRIBUTE_NAME);

      proposals.add(ProposalFactory.createTemplateProposal(
          context.getTemplate(),
          context,
          new Region(documentOffset, 0),
          null,
          -1));

      context = new AttributeTemplateContext(document, documentOffset, 0, addLeadingSpace);
      context.setAttributeName(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME);

      proposals.add(ProposalFactory.createTemplateProposal(
          context.getTemplate(),
          context,
          new Region(documentOffset, 0),
          null,
          -1));

      for (Iterator iter = webAttributeNames.iterator(); iter.hasNext();)
      {
        String name = (String) iter.next();

        if (existingAttributeNames.contains(name))
          continue;

        context = new AttributeTemplateContext(
            document,
            documentOffset,
            0,
            addLeadingSpace);
        context.setAttributeName(name);

        proposals.add(ProposalFactory.createTemplateProposal(
            context.getTemplate(),
            context,
            new Region(documentOffset, 0),
            Images.getSharedImage("bullet_web.gif"),
            null,
            0));
      }

      // we have no Tapestry attributes yet.
      //      proposals.add(ProposalFactory.getAttributeProposal(
      //          TemplateParser.JWCID_ATTRIBUTE_NAME,
      //          addLeadingSpace,
      //          documentOffset));
      //      proposals.add(ProposalFactory.getAttributeProposal(
      //          TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
      //          addLeadingSpace,
      //          documentOffset));

    } else
    {
      computeAttributeProposals(documentOffset, addLeadingSpace, jwcid, new HashSet(
          attrmap.keySet()), proposals);
    }

    if (proposals.isEmpty())
      return NoSuggestions;

    Collections.sort(proposals, ProposalFactory.PROPOSAL_COMPARATOR);

    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);

  }

  protected void computeAttributeProposals(
      int documentOffset,
      boolean addLeadingSpace,
      String jwcid,
      HashSet existingAttributeNames,
      List proposals)
  {
    try
    {
      TemplateTapestryAccess helper = new TemplateTapestryAccess((TemplateEditor) fEditor);
      //            IStorage storage = (IStorage)
      // fEditor.getEditorInput().getAdapter(IStorage.class);
      //            IProject project = TapestryCore.getDefault().getProjectFor(storage);
      //            helper.setFrameworkNamespace(
      //                (ICoreNamespace)
      // TapestryArtifactManager.getTapestryArtifactManager().getFrameworkNamespace(project));
      helper.setJwcid(jwcid);
      UITapestryAccess.Result[] infos = helper.findParameters(
          null,
          existingAttributeNames);
      for (int i = 0; i < infos.length; i++)
      {
        CompletionProposal proposal = ProposalFactory.getAttributeProposal(
            infos[i].name,
            infos[i].name,
            ProposalFactory.DEFAULT_ATTR_VALUE,
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
      XMLNode existingAttribute,
      String jwcid,
      HashSet existingAttributeNames,
      List proposals)
  {
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

    try
    {
      // first get the matches
      TemplateTapestryAccess helper = new TemplateTapestryAccess((TemplateEditor) fEditor);
      helper.setJwcid(jwcid);

      UITapestryAccess.Result[] infos = helper.findParameters(
          fragment,
          existingAttributeNames);
      for (int i = 0; i < infos.length; i++)
      {
        CompletionProposal proposal;
        if (value == null)
        {
          proposal = new CompletionProposal(
              infos[i].name + "=\"\"",
              replacementOffset,
              replacementLength,
              new Point(infos[i].name.length(), 0),
              infos[i].required ? Images.getSharedImage("bullet_pink.gif") : Images
                  .getSharedImage("bullet.gif"),
              null,
              null,
              infos[i].description);
        } else
        {
          proposal = new CompletionProposal(
              infos[i].name,
              replacementOffset,
              replacementLength,
              new Point(infos[i].name.length(), 0),
              infos[i].required ? Images.getSharedImage("bullet_pink.gif") : Images
                  .getSharedImage("bullet.gif"),
              null,
              null,
              infos[i].description);
        }

        proposals.add(proposal);
        existingAttributeNames.add(infos[i].name.toLowerCase());
      }

      //then get the replaces

      infos = helper.findParameters(null, existingAttributeNames);
      for (int i = 0; i < infos.length; i++)
      {

        CompletionProposal proposal;
        if (value == null)
        {
          proposal = new CompletionProposal(
              infos[i].name + "=\"\"",
              replacementOffset,
              replacementLength,
              new Point(infos[i].name.length(), 0),
              infos[i].required ? Images.getSharedImage("bullet_weird.gif") : Images
                  .getSharedImage("bullet_d.gif"),
              null,
              null,
              infos[i].description);
        } else
        {

          proposal = new CompletionProposal(
              infos[i].name,
              replacementOffset,
              replacementLength,
              new Point(infos[i].name.length(), 0),
              infos[i].required ? Images.getSharedImage("bullet_weird.gif") : Images
                  .getSharedImage("bullet_d.gif"),
              null,
              null,
              infos[i].description);
        }

        proposal.setYOrder(1);
        proposals.add(proposal);
      }

    } catch (IllegalArgumentException e)
    {
      //do nothing
    }
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