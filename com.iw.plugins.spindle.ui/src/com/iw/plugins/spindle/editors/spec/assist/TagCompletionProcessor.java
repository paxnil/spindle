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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.assist.CompletionProposal;
import com.iw.plugins.spindle.editors.assist.DTDProposalGenerator;
import com.iw.plugins.spindle.editors.assist.ProposalFactory;

/**
 * Processor for default declType type - only works to insert comments within
 * the body of the XML
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: TagCompletionProcessor.java,v 1.11.2.2 2004/06/22 12:23:19
 *                     glongman Exp $
 */
public class TagCompletionProcessor extends SpecCompletionProcessor
{

  /**
   * @param editor
   */
  public TagCompletionProcessor(Editor editor)
  {
    super(editor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.util.AbstractContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer,
   *              int)
   */
  protected ICompletionProposal[] doComputeCompletionProposals(
      ITextViewer viewer,
      int documentOffset)
  {
    IDocument document = viewer.getDocument();
    XMLNode tag = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);

    if (tag == null)
      return NoProposals;

    String tagName = tag.getName();

    if (tag.getType() == ITypeConstants.ENDTAG && !tag.isTerminated())
      return getEndTagProposal(document, tag, tagName);

    int baseState = tag.getStateAt(documentOffset);
    if (baseState == XMLNode.IN_TERMINATOR)
      return NoProposals;

    if (tag.getOffset() + tag.getLength() == documentOffset)
      tag = tag.getNextArtifact();

    boolean atStart = tag.getType() == ITypeConstants.ENDTAG
        ? tag.getOffset() + 2 == documentOffset : tag.getOffset() + 1 == documentOffset;

    if ((tag.getType() == ITypeConstants.ENDTAG && !atStart))
      return NoSuggestions;

    boolean addLeadingSpace = false;
    List proposals;

    if (baseState == XMLNode.TAG)
    {
      if (atStart || (tag.getAttributes().isEmpty() && !tag.isTerminated()))
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

        proposals = DTDProposalGenerator.findRawNewTagProposals(
            document,
            tag.getOffset(),
            replacementLength,
            fDTD,
            tag);
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
        return (ICompletionProposal[]) proposals
            .toArray(new ICompletionProposal[proposals.size()]);
      } else if (!atStart && tagName != null
          && documentOffset < tag.getOffset() + tagName.length())
      {
        return NoProposals;
        //                if (tagName.equals("binding") || tagName.equals("staticBinding") ||
        // tagName.equals("message-binding") ||
        // tagName.equals("string-binding")) {
        //                    
        //                }
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

    proposals = new ArrayList();

    Map attrmap = tag.getAttributesMap();

    // all that's left is to compute attribute proposals...
    // first ensure that the tag is allowed here!

    XMLNode parent = tag.getParent();
    if (parent != null
        && (parent.getType().equals("/") || DTDProposalGenerator.getAllowedChildren(
            fDTD,
            parent.getName(),
            null,
            false).contains(tagName)))
    {

      XMLNode existingAttr = tag.getAttributeAt(documentOffset);
      HashSet attributes = new HashSet();
      attributes.addAll(attrmap.keySet());
      if (existingAttr != null)
      {

        if (baseState != XMLNode.AFTER_ATT_VALUE && existingAttr != null
            && existingAttr.getOffset() < documentOffset)
        {

          computeAttributeNameReplacements(
              document,
              documentOffset,
              existingAttr,
              tagName,
              attributes,
              proposals);
        } else
        {
          computeAttributeProposals(
              document,
              documentOffset,
              addLeadingSpace,
              tagName,
              attributes,
              proposals);
        }
      } else
      {
        computeAttributeProposals(
            document,
            documentOffset,
            addLeadingSpace,
            tagName,
            attributes,
            proposals);
      }
    }

    if (proposals.isEmpty())
      return NoSuggestions;

    Collections.sort(proposals, ProposalFactory.PROPOSAL_COMPARATOR);

    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);

  }
  private ICompletionProposal[] getEndTagProposal(
      IDocument document,
      XMLNode tag,
      String tagName)
  {
    XMLNode parentTag = tag.getCorrespondingNode();
    String parentName = parentTag != null ? parentTag.getName() : null;
    if (parentName == null || parentName.equals(tagName))
      return NoSuggestions;

    // we offer to replace everything with the correct end tag
    int replacementOffset = tag.getOffset();
    int replacementLength = tag.getLength();

    try
    {
      if (!tag.isTerminated() && tag.getLength() > 2)
      {
        replacementLength = 0;
        for (; replacementLength < tag.getLength(); replacementLength++)
        {
          char c = document.getChar(replacementOffset + replacementLength);
          if (Character.isWhitespace(c))
            break;
        }
      }
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
      return NoSuggestions;
    }

    return new ICompletionProposal[]{new CompletionProposal(
        "</" + parentName + ">",
        replacementOffset,
        replacementLength,
        new Point(parentName.length() + 3, 0),
        Images.getSharedImage("bullet.gif"),
        null,
        null,
        null)};

  }

  /**
   * @param fDocumentOffset
   * @param existingAttr
   * @param tagName
   * @param set
   * @param proposals
   */
  private void computeAttributeNameReplacements(
      IDocument document,
      int documentOffset,
      XMLNode existingAttribute,
      String tagName,
      HashSet existingAttributeNames,
      List proposals)
  {
    String name = existingAttribute.getName();
    String value = existingAttribute.getAttributeValue();

    if (value != null)
      return;

    existingAttributeNames.remove(name);

    String content = existingAttribute.getContent();
    int start = 0;
    for (; Character.isWhitespace(content.charAt(start)); start++);
    String prefix = content.substring(start, start + name.length()).toLowerCase();

    boolean ignorePrefix = prefix == null || prefix.trim().length() == 0;

    int replacementOffset = existingAttribute.getOffset();
    int replacementLength = existingAttribute.getLength();

    proposals.addAll(SpecCompletionProcessor.getAttributeProposals(
        fDTD,
        document,
        replacementOffset,
        replacementLength,
        tagName,
        new ArrayList(),
        existingAttributeNames,
        prefix,
        false));
    //    int matchLength = matchString.length();
    //    if (matchLength == 0 || matchLength > name.length())
    //      matchString = null;
    //
    //    try
    //    {
    //      List attrs = DTDProposalGenerator.getAttributes(fDTD, tagName);
    //
    //      if (!attrs.isEmpty())
    //      {
    //        List requiredAttributes = DTDProposalGenerator.getRequiredAttributes(
    //            fDTD,
    //            tagName);
    //        for (Iterator iter = attrs.iterator(); iter.hasNext();)
    //        {
    //          String attrName = (String) iter.next();
    //          if (existingAttributeNames.contains(attrName)
    //              || (matchString != null && !attrName.startsWith(matchString)))
    //            continue;
    //
    //          CompletionProposal proposal;
    //          if (value == null)
    //          {
    //            proposal = new CompletionProposal(
    //                attrName + "=\"\"",
    //                replacementOffset,
    //                replacementLength,
    //                new Point(attrName.length(), 0),
    //                requiredAttributes.contains(attrName) ? Images
    //                    .getSharedImage("bullet_pink.gif") : Images
    //                    .getSharedImage("bullet.gif"),
    //                null,
    //                null,
    //                null);
    //          } else
    //          {
    //            proposal = new CompletionProposal(
    //                attrName,
    //                replacementOffset,
    //                replacementLength,
    //                new Point(attrName.length(), 0),
    //                requiredAttributes.contains(attrName) ? Images
    //                    .getSharedImage("bullet_pink.gif") : Images
    //                    .getSharedImage("bullet.gif"),
    //                null,
    //                null,
    //                null);
    //          }
    //
    //          proposals.add(proposal);
    //        }
    //      }
    //
    //    } catch (IllegalArgumentException e)
    //    {
    //      //do nothing
    //    }

  }

  protected void computeAttributeProposals(
      IDocument document,
      int documentOffset,
      boolean addLeadingSpace,
      String tagName,
      HashSet existingAttributeNames,
      List proposals)
  {

    proposals.addAll(SpecCompletionProcessor.getAttributeProposals(
        fDTD,
        document,
        documentOffset,
        0,
        tagName,
        new ArrayList(),
        existingAttributeNames,
        null,
        addLeadingSpace));

    //    List attrs = DTDProposalGenerator.getAttributes(fDTD, tagName);
    //
    //    if (!attrs.isEmpty())
    //    {
    //      List requiredAttributes =
    // DTDProposalGenerator.getRequiredAttributes(fDTD, tagName);
    //      for (Iterator iter = attrs.iterator(); iter.hasNext();)
    //      {
    //        String attrname = (String) iter.next();
    //        if (!existingAttributeNames.contains(attrname))
    //        {
    //          CompletionProposal proposal = ProposalFactory.getAttributeProposal(
    //              attrname,
    //              addLeadingSpace,
    //              documentOffset);
    //
    //          if (requiredAttributes.contains(attrname))
    //            proposal.setImage(Images.getSharedImage("bullet_pink.gif"));
    //          proposals.add(proposal);
    //        }
    //
    //      }
    //    }

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
    String name = null;
    if (tag.getType() == ITypeConstants.ENDTAG)
    {
      XMLNode start = tag.getCorrespondingNode();
      if (start != null)
        name = start.getName();
    } else
    {
      name = tag.getName();
    }

    if (name == null)
      return NoInformation;

    if (documentOffset - tag.getOffset() <= name.length() + 1)
    {
      String comment = DTDProposalGenerator.getElementComment(fDTD, name);
      if (comment != null)
        return new IContextInformation[]{new ContextInformation(
            name,
            comment.length() == 0 ? "No Information" : comment)};
    }

    return NoInformation;
  }

}