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

package com.iw.plugins.spindle.editors.spec.assist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.assist.CompletionProposal;
import com.iw.plugins.spindle.editors.assist.ProposalFactory;
import com.iw.plugins.spindle.editors.assist.usertemplates.UserTemplateCompletionProcessor;

/**
 * Processor for default content type
 * 
 * @author glongman@gmail.com
 * @version $Id: DefaultCompletionProcessor.java,v 1.9.2.2 2004/06/22 12:23:18
 *                     glongman Exp $
 */
public class DefaultCompletionProcessor extends SpecCompletionProcessor
{

  protected UserTemplateCompletionProcessor fUserTemplates;
  public DefaultCompletionProcessor(Editor editor)
  {
    super(editor);
    fUserTemplates = new UserTemplateCompletionProcessor(editor);
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
    XMLNode node = XMLNode.getArtifactAt(document, documentOffset);

    if (node == null || document.get().trim().length() == 0)
      return fUserTemplates.computeCompletionProposals(viewer, documentOffset);

    List proposals;

    XMLNode nextNode = node;
    XMLNode parentNode = null;
    // The cursor could be at the very end of the document!
    if (node.getOffset() + node.getLength() == documentOffset)
    {
      nextNode = node.getNextArtifact();
      if (nextNode == null)
      {
        return computeLastPositionProposals(node, viewer, documentOffset);
      } else if (node.isTagPart())
      {
        proposals = ProposalFactory.getRawNewTagProposals(
            document,
            documentOffset,
            0,
            fDTD,
            node,
            ! ITypeConstants.TAG.equals(node.getType()));
        return (ICompletionProposal[]) proposals
            .toArray(new ICompletionProposal[proposals.size()]);
      }
    }
    String type = node.getType();
    if (node == nextNode
        && (ITypeConstants.TAG.equals(type) || ITypeConstants.EMPTYTAG.equals(type)))
    {
      int lastOffset = node.offset + node.length;
      boolean insertAttribute = false;
      if (ITypeConstants.TAG.equals(type))
        insertAttribute = documentOffset == lastOffset - 1;
      else if (ITypeConstants.EMPTYTAG.equals(type))
        insertAttribute = documentOffset == lastOffset - 2;
      if (!insertAttribute)
        return NoProposals;

      return computeAttributeProposals(document, documentOffset, node);
    }
    node = nextNode;
    //we know its a text artifact - lets see if the user is trying to insert
    // the root tag!
    parentNode = node.getParent();
    if ("/".equals(parentNode.getType()))
      return computeRootTagProposal(
          viewer.getDocument(),
          documentOffset,
          node,
          parentNode);

    int offset = documentOffset;
    int length = 0;
    String match = null;

    try
    {
      int lineNumber = document.getLineOfOffset(documentOffset);
      int lineStart = document.getLineOffset(lineNumber);
      for (int i = documentOffset - 1; i >= lineStart; i--)
      {
        char c = document.getChar(i);
        if (Character.isJavaIdentifierPart(c))
          length++;
        else
          break;
      }
      if (length > 0)
      {
        offset = documentOffset - length;
        match = document.get(offset, length);
      }
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    }

    proposals = ProposalFactory.findRawNewTagProposals(
        document,
        offset,
        length,
        fDTD,
        node);

    if (!proposals.isEmpty())
    {

      for (Iterator iterator = proposals.iterator(); iterator.hasNext();)
      {
        ICompletionProposal p = (ICompletionProposal) iterator.next();
        if (match != null && !p.getDisplayString().startsWith(match))
          iterator.remove();
      }
    }

    computeAdditionalProposals(viewer, documentOffset, proposals, true);
    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);
  }
  /**
   * @param document
   * @param documentOffset
   * @param node
   * @return
   */
  private ICompletionProposal[] computeAttributeProposals(
      IDocument document,
      int documentOffset,
      XMLNode node)
  {
    String tagName = node.getName();
    if (tagName == null)
      return NoProposals;

    List excludeName = new ArrayList();
    List proposals = new ArrayList();
    HashSet existingAttributeNames = new HashSet();
    existingAttributeNames.addAll(node.getAttributesMap().keySet());

    proposals.addAll(ProposalFactory.getAttributeProposals(
        fDTD,
        document,
        documentOffset,
        0,
        tagName,
        excludeName,
        existingAttributeNames,
        null,
        true));

    if (proposals.isEmpty())
      return NoProposals;
    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);
  }

  private void computeAdditionalProposals(
      ITextViewer viewer,
      int documentOffset,
      List proposals,
      boolean includeUserTemplates)
  {
    ICompletionProposal endTagProposal = computeEndTagProposal(viewer, documentOffset);
    if (endTagProposal != null)
      proposals.add(0, endTagProposal);
    proposals.add(CommentCompletionProcessor.getDefaultInsertCommentProposal(
        documentOffset,
        0));

    if (includeUserTemplates)
    {
      List user = Arrays.asList(fUserTemplates.computeCompletionProposals(
          viewer,
          documentOffset));
      proposals.addAll(0, user);
    }
  }

  protected ICompletionProposal[] computeRootTagProposal(
      IDocument document,
      int completionOffset,
      XMLNode currentNode,
      XMLNode rootNode)
  {
    XMLNode realRootNode = null;
    List children = rootNode.getChildren();
    for (Iterator iter = children.iterator(); iter.hasNext();)
    {
      XMLNode element = (XMLNode) iter.next();
      String type = element.getType();
      if (type == ITypeConstants.TAG || type == ITypeConstants.EMPTYTAG
          || type == ITypeConstants.ENDTAG)
      {
        realRootNode = element;
        break;
      }

    }
    if (realRootNode != null)
      return NoProposals;
    IEditorInput input = fEditor.getEditorInput();
    IStorage storage = (IStorage) input.getAdapter(IStorage.class);
    String name = storage.getName();
    List proposals = null;
    if (name.endsWith(".jwc"))
    {
      proposals = ProposalFactory.getNewElementCompletionProposals(
          document,
          completionOffset,
          0,
          fDTD,
          "component-specification");
    } else if (name.endsWith(".page"))
    {
      proposals = ProposalFactory.getNewElementCompletionProposals(
          document,
          completionOffset,
          0,
          fDTD,
          "page-specification");
    } else if (name.endsWith(".application"))
    {
      proposals = ProposalFactory.getNewElementCompletionProposals(
          document,
          completionOffset,
          0,
          fDTD,
          "application");
    } else if (name.endsWith(".library"))
    {
      proposals = ProposalFactory.getNewElementCompletionProposals(
          document,
          completionOffset,
          0,
          fDTD,
          "library-specification");
    }

    if (proposals == null || proposals.isEmpty())
      return NoProposals;
    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);
  }

  private ICompletionProposal[] computeLastPositionProposals(
      XMLNode artifact,
      ITextViewer viewer,
      int documentOffset)
  {
    String type = artifact.getType();
    String name = artifact.getName();

    XMLNode parent = artifact.getParent();

    if (parent.getType().equals("/"))
      return computeRootTagProposal(
          viewer.getDocument(),
          documentOffset,
          artifact,
          parent);

    if (type == ITypeConstants.TEXT || name == null)
      name = parent.getName();

    if (name == null)
      return NoProposals;

    List proposals = ProposalFactory.getRawNewTagProposals(
        viewer.getDocument(),
        documentOffset,
        0,
        fDTD,
        name,
        null);
    //    if (proposals != null && !proposals.isEmpty())
    //    {
    //      for (Iterator iterator = proposals.iterator(); iterator.hasNext();)
    //      {
    //        CompletionProposal p = (CompletionProposal) iterator.next();
    //        p.setReplacementOffset(documentOffset);
    //        p.setReplacementLength(0);
    //      }
    //    }
    computeAdditionalProposals(viewer, documentOffset, proposals, false);
    if (proposals.isEmpty())
      return NoSuggestions;
    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);
  }

  /**
   * @param fDocumentOffset
   * @return
   */
  public static ICompletionProposal computeEndTagProposal(
      ITextViewer viewer,
      int documentOffset)
  {
    XMLNode artifact = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
    if (artifact.getType() == ITypeConstants.TAG && artifact.getName() != null)
    {
      String tmp = artifact.getName();
      return new CompletionProposal("</" + tmp + ">", documentOffset, 0, new Point(tmp
          .length() + 3, 0), Images.getSharedImage("bullet.gif"), null, null, null);
    }
    XMLNode parentArtifact = artifact.getParent();
    if (parentArtifact == null || parentArtifact.getType().equals("/")
        || parentArtifact.getType() != ITypeConstants.TAG)
      return null;
    String parentName = parentArtifact.getName();
    if (parentName == null)
      return null;

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
          Images.getSharedImage("bullet.gif"),
          null,
          null,
          null);
    return null;
  }
}