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

package com.iw.plugins.spindle.editors.assist;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.Editor;
import com.wutka.dtd.DTD;

/**
 * Base class for all content assist processors.
 *  
 */
public abstract class AbstractContentAssistProcessor implements IContentAssistProcessor
{

  protected static final ICompletionProposal[] NoSuggestions = new ICompletionProposal[]{
      new AbstractContentAssistProcessor.MessageProposal("no suggestions available"),
      new AbstractContentAssistProcessor.MessageProposal("")};
  protected static final ICompletionProposal[] NoProposals = NoSuggestions;

  protected static final IContextInformation[] NoInformation = new IContextInformation[0];

  protected Editor fEditor;
  protected IPreferenceStore fPreferenceStore = UIPlugin
      .getDefault()
      .getPreferenceStore();
  protected boolean fDoingContextInformation = false;
  protected DTD fDTD;

  public AbstractContentAssistProcessor(Editor editor)
  {
    this.fEditor = editor;

  }

  protected abstract void init(IDocument document) throws IllegalStateException;
  {
    // do nothing
  }

  public ICompletionProposal[] computeCompletionProposals(
      ITextViewer viewer,
      int documentOffset)
  {
    IDocument document = viewer.getDocument();

    //TODO above is wrong as there may be other proposals offered!

    try
    {
      init(document);
//      ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
//      if (selection.getLength() > 0) {
//        System.out.println(selection.getOffset()+" "+ selection.getLength());
//      }
       

      return doComputeCompletionProposals(viewer, documentOffset);

    } catch (IllegalStateException e)
    {
      return NoProposals;
    } catch (RuntimeException e)
    {
      UIPlugin.log(e);
      throw e;
    }
  }

  protected abstract ICompletionProposal[] doComputeCompletionProposals(
      ITextViewer viewer,
      int documentOffset);

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer,
   *              int)
   */
  public IContextInformation[] computeContextInformation(
      ITextViewer viewer,
      int documentOffset)
  {
    return NoInformation;
  }

  public IContextInformation[] computeInformation(ITextViewer viewer, int documentOffset)
  {
    try
    {
      init(viewer.getDocument());
      return doComputeContextInformation(viewer, documentOffset);
    } catch (IllegalStateException e)
    {
      return NoInformation;
    }
  }

  // default result, override in subclass
  public IContextInformation[] doComputeContextInformation(
      ITextViewer viewer,
      int documentOffset)
  {
    return NoInformation;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
   */
  public char[] getCompletionProposalAutoActivationCharacters()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
   */
  public char[] getContextInformationAutoActivationCharacters()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
   */
  public String getErrorMessage()
  {
    return UIPlugin.getString("noCompletions");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
   */
  public IContextInformationValidator getContextInformationValidator()
  {
    return null;
  }

  public static class MessageProposal implements ICompletionProposal
  {
    String fLabel = "coming soon!";

    public MessageProposal()
    {
    }

    public MessageProposal(String label)
    {
      fLabel = label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
     */
    public void apply(IDocument document)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo()
    {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation()
    {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
     */
    public String getDisplayString()
    {
      return fLabel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
     */
    public Image getImage()
    {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
     */
    public Point getSelection(IDocument document)
    {
      return null;
    }

  }

}