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
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.editors.DTDProposalGenerator;
import com.iw.plugins.spindle.editors.UITapestryAccess;
import com.iw.plugins.spindle.editors.assist.CompletionProposal;
import com.iw.plugins.spindle.editors.assist.OrderedProposal;
import com.iw.plugins.spindle.editors.assist.ProposalFactory;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Content assist inside of Tags (but not attributes)
 * 
 * TODO make this generic for any processor that presents tag attribute
 * proposals.
 * 
 * @author glongman@intelligentworks.com
 *  
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
   *              int)
   */
  protected ICompletionProposal[] doComputeCompletionProposals(
      ITextViewer viewer,
      int documentOffset)
  {
    XMLNode tag = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
    String tagName = tag.getName();
    int baseState = tag.getStateAt(documentOffset);
    if (tag.getType() != ITypeConstants.TAG && tag.getType() != ITypeConstants.EMPTYTAG)
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
        List allElementProposals = DTDProposalGenerator.getRawNewTagProposalsSimple(
            fDTD,
            tag);
        if (allElementProposals.isEmpty())
          return NoSuggestions;

        String content = tag.getContent();
        int length = tag.getLength();
        proposals = new ArrayList();
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

        if (length > 1 && documentOffset > tag.getOffset() + 1)
        {
          String match = tag.getContentTo(documentOffset, true).trim().toLowerCase();
          for (Iterator iter = allElementProposals.iterator(); iter.hasNext();)
          {
            CompletionProposal proposal = (CompletionProposal) iter.next();
            if (proposal.getDisplayString().startsWith(match))
            {
              proposal.setReplacementOffset(tag.getOffset());
              proposal.setReplacementLength(replacementLength);
              proposals.add(proposal);
            }
          }
          if (proposals.isEmpty())
          {
            return NoSuggestions;
          }

        } else
        {
          for (Iterator iter = allElementProposals.iterator(); iter.hasNext();)
          {
            CompletionProposal proposal = (CompletionProposal) iter.next();
            proposal.setReplacementOffset(tag.getOffset());
            proposal.setReplacementLength(replacementLength);
            proposals.add(proposal);
          }
        }
        Collections.sort(proposals, ProposalFactory.PROPOSAL_COMPARATOR);
        return (ICompletionProposal[]) proposals
            .toArray(new ICompletionProposal[proposals.size()]);
      }
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

    Map attrmap = tag.getAttributesMap();
    String jwcid = null;
    jwcid = getJwcid(attrmap);
    HashSet existingAttributeNames = new HashSet(attrmap.keySet());

    XMLNode existingAttr = tag.getAttributeAt(documentOffset);
    if (baseState != XMLNode.AFTER_ATT_VALUE && existingAttr != null
        && existingAttr.getOffset() < documentOffset)
    {
      proposals = computeAllAttributeReplacements(
          documentOffset,
          attrmap,
          tagName,
          jwcid,
          existingAttr);

    } else if (!attrmap.containsKey(TemplateParser.JWCID_ATTRIBUTE_NAME)
        && !attrmap.containsKey(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME))
    {
      proposals = computeNewAttributeProposalsNoParameters(
          viewer.getDocument(),
          documentOffset,
          tagName,
          existingAttributeNames,
          addLeadingSpace);
    } else
    {
      proposals = computeNewAttributeProposalsWithParameters(
          documentOffset,
          addLeadingSpace,
          tagName,
          jwcid,
          existingAttributeNames);
    }

    if (proposals.isEmpty())
      return NoSuggestions;

    Collections.sort(proposals, ProposalFactory.PROPOSAL_COMPARATOR);

    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);

  }

  private List computeNewAttributeProposalsNoParameters(
      IDocument document,
      int documentOffset,
      String tagName,
      HashSet existingAttributeNames,
      boolean addLeadingSpace)
  {
    List proposals = new ArrayList();
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

      context = new AttributeTemplateContext(document, documentOffset, 0, addLeadingSpace);
      context.setAttributeName(name);

      proposals.add(ProposalFactory.createTemplateProposal(
          context.getTemplate(),
          context,
          new Region(documentOffset, 0),
          Images.getSharedImage("bullet_web.gif"),
          null,
          0));
    }

    return proposals;
  }

  private List computeAllAttributeReplacements(
      int documentOffset,
      Map attrmap,
      String tagName,
      String jwcid,
      XMLNode existingAttr)
  {
    List proposals = new ArrayList();
    List webAttributeNames = Collections.EMPTY_LIST;
    HashSet existingAttributeNames = new HashSet(attrmap.keySet());
    String attrName = existingAttr.getName();
    String attrValue = existingAttr.getAttributeValue();
    int replacementOffset = existingAttr.getOffset();
    int replacementLength = (attrName == null ? 0 : attrName.length());

    if (fDTD != null && tagName != null)
      webAttributeNames = DTDProposalGenerator.getAttributes(fDTD, tagName);

    // no proposals if the attribute name is jwcid!
    if (!TemplateParser.JWCID_ATTRIBUTE_NAME.equalsIgnoreCase(existingAttr.getName()))
    {

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
            if (attrValue == null)
            {
              proposals.add(ProposalFactory.getAttributeProposal(
                  TemplateParser.JWCID_ATTRIBUTE_NAME.substring(currentName.length()),
                  TemplateParser.JWCID_ATTRIBUTE_NAME,
                  "",
                  null,
                  false,
                  documentOffset));

            } else
            {
              proposals.add(new CompletionProposal(
                  TemplateParser.JWCID_ATTRIBUTE_NAME,
                  replacementOffset,
                  replacementLength,
                  new Point(TemplateParser.JWCID_ATTRIBUTE_NAME.length(), 0),
                  Images.getSharedImage("bullet.gif"),
                  TemplateParser.JWCID_ATTRIBUTE_NAME,
                  null,
                  null));
            }

          } else if (TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME
              .startsWith(currentName))
          {
            if (attrValue == null)
            {
              proposals.add(ProposalFactory.getAttributeProposal(
                  TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME.substring(currentName
                      .length()),
                  TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
                  "",
                  null,
                  false,
                  documentOffset));

            } else
            {
              proposals.add(new CompletionProposal(
                  TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
                  replacementOffset,
                  replacementLength,
                  new Point(TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME.length(), 0),
                  Images.getSharedImage("bullet.gif"),
                  TemplateParser.LOCALIZATION_KEY_ATTRIBUTE_NAME,
                  null,
                  null));
            }
          }
        }
      } else
      {
        // we need to find parameter name replacements
        existingAttributeNames.addAll(computeParameterNameReplacements(
            documentOffset,
            existingAttr,
            jwcid,
            existingAttributeNames,
            proposals));
      }
    }

    if (!webAttributeNames.isEmpty())
    {
      CompletionProposal proposal;
      //get index of whitespace
      String fragment = existingAttr.getContentTo(documentOffset, false).toLowerCase();
      if (fragment.length() <= attrName.length())
      {

        if (fragment.length() == 0)
          fragment = null;

        for (Iterator iter = webAttributeNames.iterator(); iter.hasNext();)
        {
          String name = (String) iter.next();
          if (existingAttributeNames.contains(name))
            continue;
          if (fragment != null && !name.startsWith(fragment))
            continue;
          if (attrValue == null)
          {
            proposal = new CompletionProposal(
                name + "=\"\"",
                replacementOffset,
                replacementLength,
                new Point(name.length(), 0),
                Images.getSharedImage("bullet_web.gif"),
                null,
                null,
                null);
          } else
          {
            proposal = new CompletionProposal(
                name,
                replacementOffset,
                replacementLength,
                new Point(name.length(), 0),
                Images.getSharedImage("bullet_web.gif"),
                null,
                null,
                null);
          }

          proposal.setYOrder(1000);
          proposals.add(proposal);
        }
      }

    }

    return proposals;
  }

  protected List computeNewAttributeProposalsWithParameters(
      int documentOffset,
      boolean addLeadingSpace,
      String tagName,
      String jwcid,
      HashSet existingAttributeNames)
  {
    List proposals = new ArrayList();
    List webAttributeNames = Collections.EMPTY_LIST;

    if (fDTD != null && tagName != null)
      webAttributeNames = DTDProposalGenerator.getAttributes(fDTD, tagName);

    try
    {
      TemplateTapestryAccess helper = new TemplateTapestryAccess((TemplateEditor) fEditor);
      helper.setJwcid(jwcid);
      UITapestryAccess.Result[] infos = helper.findParameters(
          null,
          existingAttributeNames);
      for (int i = 0; i < infos.length; i++)
      {
        existingAttributeNames.add(infos[i].name);
        CompletionProposal proposal = ProposalFactory.getAttributeProposal(
            infos[i].name,
            infos[i].name,
            ProposalFactory.DEFAULT_ATTR_VALUE,
            infos[i].description,
            addLeadingSpace,
            documentOffset);

        if (infos[i].required)
        {
          proposal.setImage(Images.getSharedImage("bullet_pink.gif"));
          proposal.setYOrder(-2);
        } else
        {
          proposal.setYOrder(-1);
        }
        proposals.add(proposal);
      }

    } catch (IllegalArgumentException e)
    {
      //do nothing
    }

    for (Iterator iter = webAttributeNames.iterator(); iter.hasNext();)
    {
      String name = (String) iter.next();
      if (existingAttributeNames.contains(name))
        continue;
      CompletionProposal proposal = ProposalFactory.getAttributeProposal(
          name,
          name,
          ProposalFactory.DEFAULT_ATTR_VALUE,
          null,
          addLeadingSpace,
          documentOffset);

      proposal.setImage(Images.getSharedImage("bullet_web.gif"));
      proposals.add(proposal);
    }
    return proposals;
  }

  protected List computeParameterNameReplacements(
      int documentOffset,
      XMLNode existingAttribute,
      String jwcid,
      HashSet existingAttributeNames,
      List proposals)
  {
    List parameterNames = new ArrayList();
    String name = existingAttribute.getName();
    String value = existingAttribute.getAttributeValue();
    //get index of whitespace
    String fragment = existingAttribute.getContentTo(documentOffset, false);
    if (fragment.length() > name.length())
      return Collections.EMPTY_LIST;

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
        parameterNames.add(infos[i].name);
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

      if (fDTD == null)
      // its confusing to include the non matching parameter if we are including
      // XHTML ones too.
      {
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
      }

    } catch (IllegalArgumentException e)
    {
      //do nothing
    }
    return parameterNames;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.util.AbstractContentAssistProcessor#doComputeContextInformation(org.eclipse.jface.text.ITextViewer,
   *              int)
   */
  public IContextInformation[] doComputeContextInformation(
      ITextViewer viewer,
      int documentOffset)
  {
    XMLNode tag = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
    int baseState = tag.getStateAt(documentOffset);
    if (tag.getType() == ITypeConstants.ENDTAG)
      return NoInformation;

    Map attrMap = tag.getAttributesMap();

    if (!attrMap.containsKey(TemplateParser.JWCID_ATTRIBUTE_NAME))
      return NoInformation;

    XMLNode attr = tag.getAttributeAt(documentOffset);
    if (attr == null
        || attr.getName().equalsIgnoreCase(TemplateParser.JWCID_ATTRIBUTE_NAME))
    {
      return NoInformation;
    }

    try
    {
      TemplateTapestryAccess helper = new TemplateTapestryAccess((TemplateEditor) fEditor);
      XMLNode jwcidAttr = (XMLNode) attrMap.get(TemplateParser.JWCID_ATTRIBUTE_NAME);
      helper.setJwcid(jwcidAttr.getAttributeValue());

      UITapestryAccess.Result result = helper.getParameterContextInformation(attr
          .getName());

      if (result != null)
        return new IContextInformation[]{new ContextInformation(
            result.displayName,
            result.description)};

    } catch (IllegalArgumentException e)
    {
      //do nothing
    }

    return NoInformation;
  }
}