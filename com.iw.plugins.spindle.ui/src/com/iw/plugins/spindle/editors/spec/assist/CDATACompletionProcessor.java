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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.assist.AbstractContentAssistProcessor;

/**
 * Processor for default declType type - only works to insert comments within
 * the body of the XML
 * 
 * @author glongman@gmail.com
 */
public class CDATACompletionProcessor extends AbstractContentAssistProcessor
{

  /**
   * @param editor
   */
  public CDATACompletionProcessor(Editor editor)
  {
    super(editor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.util.AbstractContentAssistProcessor#connect(org.eclipse.jface.text.IDocument)
   */
  protected void init(IDocument document) throws IllegalStateException
  {    
    // do nothing.
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.util.AbstractContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer,
   *      int)
   */
  protected ICompletionProposal[] doComputeCompletionProposals(
      ITextViewer viewer,
      int documentOffset)
  {
    IDocument document = viewer.getDocument();
    XMLNode artifact = XMLNode.getArtifactAt(viewer.getDocument(), documentOffset);

    if (artifact.getParent().getType().equals("/"))
      return NoProposals;

    String content = artifact.getContentTo(documentOffset, false);
    if (content.equals("<!-"))
    {
      return new ICompletionProposal[]{CommentCompletionProcessor
          .getDefaultInsertCommentProposal(artifact.getOffset(), 3)};
    }
    return NoProposals;
  }

}