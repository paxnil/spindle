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

package com.iw.plugins.spindle.editors.util;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.editors.Editor;

/**
 * Processor for default declType type - only works to insert comments within
 * the body of the XML
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: DeclCompletionProcessor.java,v 1.1.2.2 2004/06/22 12:24:20
 *          glongman Exp $
 */
public class DeclCompletionProcessor extends ContentAssistProcessor
{

  /**
   * @param editor
   */
  public DeclCompletionProcessor(Editor editor)
  {
    super(editor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#connect(org.eclipse.jface.text.IDocument)
   */
  protected void init(IDocument document) throws IllegalStateException
  {
    // do nothing

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
    XMLNode artifact = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);
    XMLNode parent = artifact.getParent();
    if (parent.getType() == "/")
    {
      return NoProposals;
    }
    String content = artifact.getContent();
    int index = content.indexOf('<', 2);
    if (index > 0)
    {
      index--;
      for (; index > 0; index--)
      {
        if (!Character.isWhitespace(content.charAt(index)))
          break;
      }
    }
    return new ICompletionProposal[]{CommentCompletionProcessor
        .getDefaultInsertCommentProposal(artifact.getOffset(), Math.max(2, index + 1))};
  }

}