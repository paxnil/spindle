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
import com.iw.plugins.spindle.editors.DTDProposalGenerator;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.util.CommentCompletionProcessor;
import com.iw.plugins.spindle.editors.util.CompletionProposal;

/**
 * Processor for default content type
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: DefaultCompletionProcessor.java,v 1.9.2.2 2004/06/22 12:23:18
 *          glongman Exp $
 */
public class DefaultCompletionProcessor extends SpecCompletionProcessor
{

  public DefaultCompletionProcessor(Editor editor)
  {
    super(editor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer,
   *      int)
   */
  protected ICompletionProposal[] doComputeCompletionProposals(
      ITextViewer viewer,
      int documentOffset)
  {
    IDocument document = viewer.getDocument();
    XMLNode node = XMLNode.getArtifactAt(document, documentOffset);
    XMLNode nextNode = node;
    XMLNode parentNode = null;
    // The cursor could be at the very end of the document!
    if (node.getOffset() + node.getLength() == documentOffset)
    {
      nextNode = node.getNextArtifact();
      // at the end of the document
      if (nextNode == null)
        return computeLastPositionProposals(node, viewer, documentOffset);
    }
    node = nextNode;
    //we know its a text artifact - lets see if the user is trying to insert
    // the root tag!
    parentNode = node.getParent();
    if ("/".equals(parentNode.getType()))
      return computeRootTagProposal(node, parentNode, documentOffset);

    List proposals = new ArrayList();
    List rawProposals = DTDProposalGenerator.findRawNewTagProposals(
        fDTD,
        node,
        documentOffset);
    if (rawProposals != null && !rawProposals.isEmpty())
    {
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

      for (Iterator iterator = rawProposals.iterator(); iterator.hasNext();)
      {
        CompletionProposal p = (CompletionProposal) iterator.next();
        if (match != null && !p.getDisplayString().startsWith(match))
          continue;
        p.setReplacementOffset(offset);
        p.setReplacementLength(length);
        proposals.add(p);

      }
    }

    computeAdditionalProposals(viewer, documentOffset, proposals);
    return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals
        .size()]);
  }

  private void computeAdditionalProposals(
      ITextViewer viewer,
      int documentOffset,
      List proposals)
  {
    ICompletionProposal endTagProposal = computeEndTagProposal(viewer, documentOffset);
    if (endTagProposal != null)
      proposals.add(0, endTagProposal);
    proposals.add(CommentCompletionProcessor.getDefaultInsertCommentProposal(
        documentOffset,
        0));
  }

  protected ICompletionProposal[] computeRootTagProposal(
      XMLNode currentNode,
      XMLNode rootNode,
      int documentOffset)
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
      proposals = DTDProposalGenerator.getNewElementCompletionProposals(
          fDTD,
          "component-specification");
    } else if (name.endsWith(".page"))
    {
      proposals = DTDProposalGenerator.getNewElementCompletionProposals(
          fDTD,
          "page-specification");
    } else if (name.endsWith(".application"))
    {
      proposals = DTDProposalGenerator.getNewElementCompletionProposals(
          fDTD,
          "application");
    } else if (name.endsWith(".library"))
    {
      proposals = DTDProposalGenerator.getNewElementCompletionProposals(
          fDTD,
          "library-specification");
    }
    CompletionProposal proposal = (CompletionProposal) proposals.get(0);
    proposal.setReplacementOffset(documentOffset);
    proposal.setReplacementLength(0);
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
      return computeRootTagProposal(artifact, parent, documentOffset);

    if (type == ITypeConstants.TEXT || name == null)
      name = parent.getName();

    if (name == null)
      return NoProposals;

    List proposals = DTDProposalGenerator.getRawNewTagProposals(fDTD, name, null);
    if (proposals != null && !proposals.isEmpty())
    {
      for (Iterator iterator = proposals.iterator(); iterator.hasNext();)
      {
        CompletionProposal p = (CompletionProposal) iterator.next();
        p.setReplacementOffset(documentOffset);
        p.setReplacementLength(0);
      }
    }
    computeAdditionalProposals(viewer, documentOffset, proposals);
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