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

package com.iw.plugins.spindle.editors.assist;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * Base class for Completion Proposals (that are not template based!)
 * 
 * @author glongman@gmail.com
 * 
 */
public class CompletionProposal implements OrderedProposal
{
  /** The string to be displayed in the completion proposal popup */
  protected String fDisplayString;
  /** The replacement string */
  protected String fReplacementString;
  /** The replacement offset */
  protected int fReplacementOffset;
  /** The replacement length */
  protected int fReplacementLength;
  /** The cursor position after this proposal has been applied */
  protected Point fSelectionPoint;
  /** The image to be displayed in the completion proposal popup */
  protected Image fImage;
  /** The context information of this proposal */
  protected IContextInformation fContextInformation;
  /** The additional info of this proposal */
  protected String fAdditionalProposalInfo;

  protected int fYOrder = 0;

  /**
   * Creates a new completion proposal based on the provided information. The
   * replacement string is considered being the display string too. All
   * remaining fields are set to <code>null</code>.
   * 
   * @param replacementString the actual string to be inserted into the document
   * @param replacementOffset the offset of the text to be replaced
   * @param replacementLength the length of the text to be replaced
   * @param cursorPosition the position of the cursor following the insert
   *          relative to replacementOffset
   */
  public CompletionProposal(String replacementString, int replacementOffset,
      int replacementLength, Point selectionPoint)
  {
    this(
        replacementString,
        replacementOffset,
        replacementLength,
        selectionPoint,
        null,
        null,
        null,
        null);
  }

  /**
   * Creates a new completion proposal. All fields are initialized based on the
   * provided information.
   * 
   * @param replacementString the actual string to be inserted into the document
   * @param replacementOffset the offset of the text to be replaced
   * @param replacementLength the length of the text to be replaced
   * @param selectionPoint the selection point following the insert relative to
   *          replacementOffset
   * @param image the image to display for this proposal
   * @param displayString the string to be displayed for the proposal
   * @param contentInformation the context information associated with this
   *          proposal
   * @param additionalProposalInfo the additional information associated with
   *          this proposal
   */
  public CompletionProposal(String replacementString, int replacementOffset,
      int replacementLength, Point selectionPoint, Image image, String displayString,
      IContextInformation contextInformation, String additionalProposalInfo)
  {
    Assert.isNotNull(replacementString);
    Assert.isNotNull(selectionPoint); 
    Assert.isTrue(selectionPoint.x >= 0 && selectionPoint.y >= 0); 

    fReplacementString = replacementString; 
    fReplacementOffset = replacementOffset;
    fReplacementLength = replacementLength;
    fSelectionPoint = selectionPoint;
    fImage = image;
    fDisplayString = displayString;
    fContextInformation = contextInformation;
    fAdditionalProposalInfo = additionalProposalInfo;
  }

  public void setYOrder(int order)
  {
    fYOrder = order;
  }
  
  public int getYOrder() { 
    return fYOrder;
  }

  /*
   * @see ICompletionProposal#apply(IDocument)
   */
  public void apply(IDocument document)   
  {
    try
    {
      document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
    } catch (BadLocationException x)
    {
      // ignore
    }
  }

  /*
   * @see ICompletionProposal#getSelection(IDocument)
   */
  public Point getSelection(IDocument document)
  {
    try
    {
      Point usePoint = new Point(
          fSelectionPoint.x + fReplacementOffset,
          fSelectionPoint.y);
      return usePoint;
    } catch (RuntimeException e)
    {
      UIPlugin.log(e);
      throw e;
    } finally
    {
      fReplacementOffset = -1;
      fReplacementLength = -1;
    }
  }

  /*
   * @see ICompletionProposal#getContextInformation()
   */
  public IContextInformation getContextInformation()
  {
    return fContextInformation;
  }

  /*
   * @see ICompletionProposal#getImage()
   */
  public Image getImage()
  {
    return fImage;
  }

  public void setImage(Image image)
  {
    fImage = image;
  }

  /*
   * @see ICompletionProposal#getDisplayString()
   */
  public String getDisplayString()
  {
    if (fDisplayString != null)
      return fDisplayString;
    return fReplacementString;
  }

  /*
   * @see ICompletionProposal#getAdditionalProposalInfo()
   */
  public String getAdditionalProposalInfo()
  {
    return fAdditionalProposalInfo;
  }

  public void setReplacementOffset(int offset)
  {
    fReplacementOffset = offset;
  }

  public void setReplacementLength(int length)
  {
    fReplacementLength = length;
  }

  static public class NullProposal extends CompletionProposal
  {
    public NullProposal(String message, String xtraInfo, int documentOffset)
    {
      super("", documentOffset, 0, new Point(0, 0), null, message, null, xtraInfo);
    }

    public NullProposal(String message, int documentOffset)
    {
      this(message, null, documentOffset);
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
     */
    public void apply(IDocument document)
    {
    }

  }

}