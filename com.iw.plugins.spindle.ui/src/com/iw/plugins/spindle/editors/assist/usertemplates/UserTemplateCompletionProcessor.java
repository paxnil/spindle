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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.assist.usertemplates;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.spec.SpecEditor;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * UserTemplateCompletionProcessor TODO add something here
 * 
 * @author glongman@gmail.com
 *  
 */
public class UserTemplateCompletionProcessor extends TemplateCompletionProcessor
{

  private final static int PROPOSAL_MODE_NEWFILE = 1;
  private final static int PROPOSAL_MODE_USER = 2;

  private int fProposalMode;
  /**
   * The Editor we are working for
   */
  private Editor fEditor;

  /**
   * The prefix for the current content assist
   */
  protected String currentPrefix = null;
  /**
   * Cursor position, counted from the beginning of the document.
   * <P>
   * The first position has index '0'.
   */
  protected int cursorPosition = -1;
  /**
   * The text viewer.
   */
  private ITextViewer viewer;

  /**
   *  
   */
  public UserTemplateCompletionProcessor(Editor editor)
  {
    super();
    fEditor = editor;
  }

  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
  {
    fProposalMode = -1;
    this.viewer = viewer;
    return super.computeCompletionProposals(viewer, offset);
  }
  /**
   * Determines the current prefix that should be used for completion.
   */
  private String getCurrentPrefix()
  {

    ITextSelection selection = (ITextSelection) viewer
        .getSelectionProvider()
        .getSelection();
    IDocument doc = viewer.getDocument();
    return getPrefixFromDocument(doc.get(), selection.getOffset() + selection.getLength())
        .toLowerCase();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#extractPrefix(org.eclipse.jface.text.ITextViewer,
   *              int)
   */
  protected String extractPrefix(ITextViewer textViewer, int offset)
  {
    return getPrefixFromDocument(textViewer.getDocument().get(), offset);
  }

  /**
   * Returns the prefix in the specified document text with respect to the
   * specified offset.
   * 
   * @param aDocumentText the whole content of the edited file as String
   * @param anOffset the cursor position
   */
  protected String getPrefixFromDocument(String aDocumentText, int anOffset)
  {
    if (currentPrefix != null)
    {
      return currentPrefix;
    }
    int startOfWordToken = anOffset;

    char token = 'a';
    if (startOfWordToken > 0)
    {
      token = aDocumentText.charAt(startOfWordToken - 1);
    }

    while (startOfWordToken > 0
        && (Character.isJavaIdentifierPart(token) || '.' == token || '-' == token || ';' == token)
        && !('$' == token))
    {
      startOfWordToken--;
      if (startOfWordToken == 0)
      {
        break; //word goes right to the beginning of the doc
      }
      token = aDocumentText.charAt(startOfWordToken - 1);
    }

    if (startOfWordToken != anOffset)
    {
      currentPrefix = aDocumentText.substring(startOfWordToken, anOffset).toLowerCase();
    } else
    {
      currentPrefix = ""; //$NON-NLS-1$
    }
    return currentPrefix;
  }

  /**
   * Cut out angular brackets for relevance sorting, since the template name
   * does not contain the brackets.
   */
  protected int getRelevance(Template template, String prefix)
  {
    if (prefix.startsWith("<")) //$NON-NLS-1$
      prefix = prefix.substring(1);
    if (template.getName().startsWith(prefix))
      return 90;
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getTemplates(java.lang.String)
   */
  protected Template[] getTemplates(String contextTypeId)
  {
    return UserTemplateAccess.getDefault().getTemplateStore().getTemplates(contextTypeId);
  }

  /**
   * Returns the current proposal mode.
   */
  protected void determineProposalMode(
      IDocument document,
      int aCursorPosition,
      String aPrefix)
  {
    if (document.getLength() == 0
        || (document.getLength() == 1 && document.get().equals("<") || document
            .get()
            .trim()
            .length() == 0))
    {
      fProposalMode = PROPOSAL_MODE_NEWFILE;
    } else
    {
      fProposalMode = PROPOSAL_MODE_USER;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getContextType(org.eclipse.jface.text.ITextViewer,
   *              org.eclipse.jface.text.IRegion)
   */
  protected TemplateContextType getContextType(ITextViewer textViewer, IRegion region)
  {
    determineProposalMode(textViewer.getDocument(), cursorPosition, getCurrentPrefix());

    switch (fProposalMode)
    {
      case PROPOSAL_MODE_NEWFILE :
        String contextTypeId = getNewFileContextTypeId();
        if (contextTypeId == null)
          return null;
        return UserTemplateAccess.getDefault().getContextTypeRegistry().getContextType(
            contextTypeId);
      case PROPOSAL_MODE_USER :
        return UserTemplateAccess.getDefault().getContextTypeRegistry().getContextType(
            UserContextType.USER_CONTEXT_TYPE);
      default :
        return null;
    }
  }

  protected String getNewFileContextTypeId()
  {

    IEditorInput input = fEditor.getEditorInput();
    IFile file = ((IFileEditorInput) input).getFile();

    if (fEditor instanceof SpecEditor)
    {
      String extension = file.getFileExtension();
      if (extension == null || fEditor == null)
        return null;

      if ("application".equals(extension))
      {
        return XMLFileContextType.APPLICATION_FILE_CONTEXT_TYPE;
      } else if ("library".equals(extension))
      {
        return XMLFileContextType.LIBRARY_FILE_CONTEXT_TYPE;
      } else if ("page".equals(extension))
      {
        return XMLFileContextType.PAGE_FILE_CONTEXT_TYPE;
      } else if ("jwc".equals(extension))
      {
        return XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE;
      }
    } else if (fEditor instanceof TemplateEditor)
    {
      return XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE;
    }

    return null;

  } /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getImage(org.eclipse.jface.text.templates.Template)
     */
  protected Image getImage(Template template)
  {
    return Images.getSharedImage("template_obj.gif");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#createContext(org.eclipse.jface.text.ITextViewer,
   *              org.eclipse.jface.text.IRegion)
   */
  protected TemplateContext createContext(ITextViewer contextViewer, IRegion region)
  {
    TemplateContextType contextType = getContextType(contextViewer, region);
    if (contextType != null)
    {
      IDocument document = contextViewer.getDocument();
      return new UserTemplateContext(contextType, document, region.getOffset(), region
          .getLength());
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#createProposal(org.eclipse.jface.text.templates.Template,
   *              org.eclipse.jface.text.templates.TemplateContext,
   *              org.eclipse.jface.text.Region, int)
   */
  protected ICompletionProposal createProposal(
      Template template,
      TemplateContext context,
      Region region,
      int relevance)
  {
    return new UserTemplateProposal(
        template,
        context,
        region,
        getImage(template),
        relevance);
  }

}