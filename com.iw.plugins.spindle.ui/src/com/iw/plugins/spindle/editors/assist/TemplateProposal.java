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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors.assist;

import org.eclipse.jdt.internal.ui.text.template.contentassist.PositionBasedCompletionProposal;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * A template completion proposal. Clients may subclass.
 *  
 */
public class TemplateProposal

    implements
      OrderedProposal,
      ICompletionProposal,
      ICompletionProposalExtension,
      ICompletionProposalExtension2,
      ICompletionProposalExtension3
{

  private final Template fTemplate;
  protected final TemplateContext fContext;
  private final Image fImage;
  private final IRegion fRegion;
  private int fRelevance;
  private int fYOrder;

  private IRegion fSelectedRegion; // initialized
  // by
  // apply()
  private String fDisplayString;
  private InclusivePositionUpdater fUpdater;
  private Object fMode = LinkedModeUI.CYCLE_WHEN_NO_PARENT;

  /**
   * Creates a template proposal with a template and its context.
   * 
   * @param template the template
   * @param context the context in which the template was requested.
   * @param region the region this proposal is applied to
   * @param image the icon of the proposal.
   */
  public TemplateProposal(Template template, TemplateContext context, IRegion region,
      Image image)
  {
    this(template, context, region, image, 0);
  }

  /**
   * Creates a template proposal with a template and its context.
   * 
   * @param template the template
   * @param context the context in which the template was requested.
   * @param image the icon of the proposal.
   * @param region the region this proposal is applied to
   * @param relevance the relevance of the proposal
   */
  public TemplateProposal(Template template, TemplateContext context, IRegion region,
      Image image, int relevance)
  {
    Assert.isNotNull(template);
    Assert.isNotNull(context);
    Assert.isNotNull(region);

    fTemplate = template;
    fContext = context;
    fImage = image;
    fRegion = region;

    fDisplayString = null;

    fRelevance = relevance;
  }

  /*
   * @see ICompletionProposal#apply(IDocument)
   */
  public final void apply(IDocument document)
  {
    // not called anymore
  }

  public void setCyclingMode(Object mode)
  {
    if (mode != LinkedModeUI.CYCLE_ALWAYS && mode != LinkedModeUI.CYCLE_NEVER
        && mode != LinkedModeUI.CYCLE_WHEN_NO_PARENT)
      throw new IllegalArgumentException();

    fMode = mode;
  }

  /**
   * Inserts the template offered by this proposal into the viewer's document
   * and sets up a <code>LinkedModeUI</code> on the viewer to edit any of the
   * template's unresolved variables.
   * 
   * @param viewer {@inheritDoc}
   * @param trigger {@inheritDoc}
   * @param stateMask {@inheritDoc}
   * @param offset {@inheritDoc}
   */
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset)
  {

    IDocument document = viewer.getDocument();
    try
    {
      fContext.setReadOnly(false);
      TemplateBuffer templateBuffer;
      try
      {
        templateBuffer = fContext.evaluate(fTemplate);
      } catch (TemplateException e1)
      {
        fSelectedRegion = fRegion;
        return;
      }

      int start = getReplaceOffset();
      int end = Math.max(getReplaceEndOffset(), offset);

      // insert template string
      String templateString = templateBuffer.getString();
      document.replace(start, end - start, templateString);

      // translate positions
      LinkedModeModel model = new LinkedModeModel();
      TemplateVariable[] variables = templateBuffer.getVariables();
      boolean hasPositions = false;
      for (int i = 0; i != variables.length; i++)
      {
        TemplateVariable variable = variables[i];

        if (variable.isUnambiguous())
          continue;

        LinkedPositionGroup group = new LinkedPositionGroup();

        int[] offsets = variable.getOffsets();
        int length = variable.getLength();

        String[] values = variable.getValues();
        ICompletionProposal[] proposals = new ICompletionProposal[values.length];
        for (int j = 0; j < values.length; j++)
        {
          ensurePositionCategoryInstalled(document, model);
          Position pos = new Position(offsets[0] + start, length);
          document.addPosition(getCategory(), pos);
          proposals[j] = new PositionBasedCompletionProposal(values[j], pos, length);
        }

        for (int j = 0; j != offsets.length; j++)
          if (j == 0 && proposals.length > 1)
            group.addPosition(new ProposalPosition(
                document,
                offsets[j] + start,
                length,
                proposals));
          else
            group.addPosition(new LinkedPosition(document, offsets[j] + start, length));

        model.addGroup(group);
        hasPositions = true;
      }

      if (hasPositions)
      {
        model.forceInstall();
        LinkedModeUI ui = new LinkedModeUI(model, viewer);

        ui.setCyclingMode(fMode);
        ui.setExitPosition(
            viewer,
            getCaretOffset(templateBuffer) + start,
            0,
            Integer.MAX_VALUE);
        ui.enter();

        fSelectedRegion = ui.getSelectedRegion();
      } else
      {
        ensurePositionCategoryRemoved(document);
        fSelectedRegion = new Region(getCaretOffset(templateBuffer) + start, 0);
      }

    } catch (BadLocationException e)
    {
      openErrorDialog(viewer.getTextWidget().getShell(), e);
      ensurePositionCategoryRemoved(document);
      fSelectedRegion = fRegion;
    } catch (BadPositionCategoryException e)
    {
      openErrorDialog(viewer.getTextWidget().getShell(), e);
      fSelectedRegion = fRegion;
    }

  }

  private void ensurePositionCategoryInstalled(
      final IDocument document,
      LinkedModeModel model)
  {
    if (!document.containsPositionCategory(getCategory()))
    {
      document.addPositionCategory(getCategory());
      fUpdater = new InclusivePositionUpdater(getCategory());
      document.addPositionUpdater(fUpdater);

      model.addLinkingListener(new ILinkedModeListener()
      {

        /*
         * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel,
         *              int)
         */
        public void left(LinkedModeModel environment, int flags)
        {
          ensurePositionCategoryRemoved(document);
        }

        public void suspend(LinkedModeModel environment)
        {
        }
        public void resume(LinkedModeModel environment, int flags)
        {
        }
      });
    }
  }

  private void ensurePositionCategoryRemoved(IDocument document)
  {
    if (document.containsPositionCategory(getCategory()))
    {
      try
      {
        document.removePositionCategory(getCategory());
      } catch (BadPositionCategoryException e)
      {
        // ignore
      }
      document.removePositionUpdater(fUpdater);
    }
  }

  private String getCategory()
  {
    return "TemplateProposalCategory_" + toString();
  }

  private int getCaretOffset(TemplateBuffer buffer)
  {

    TemplateVariable[] variables = buffer.getVariables();
    for (int i = 0; i != variables.length; i++)
    {
      TemplateVariable variable = variables[i];
      if (variable.getType().equals(GlobalTemplateVariables.Cursor.NAME))
        return variable.getOffsets()[0];
    }

    return buffer.getString().length();
  }

  /**
   * Returns the offset of the range in the document that will be replaced by
   * applying this template.
   * 
   * @return the offset of the range in the document that will be replaced by
   *                 applying this template
   */
  private int getReplaceOffset()
  {
    int start;
    if (fContext instanceof DocumentTemplateContext)
    {
      DocumentTemplateContext docContext = (DocumentTemplateContext) fContext;
      start = docContext.getStart();
    } else
    {
      start = fRegion.getOffset();
    }
    return start;
  }

  /**
   * Returns the end offset of the range in the document that will be replaced
   * by applying this template.
   * 
   * @return the end offset of the range in the document that will be replaced
   *                 by applying this template
   */
  private int getReplaceEndOffset()
  {
    int end;
    if (fContext instanceof DocumentTemplateContext)
    {
      DocumentTemplateContext docContext = (DocumentTemplateContext) fContext;
      end = docContext.getEnd();
    } else
    {
      end = fRegion.getOffset() + fRegion.getLength();
    }
    return end;
  }

  /*
   * @see ICompletionProposal#getSelection(IDocument)
   */
  public Point getSelection(IDocument document)
  {
    return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
  }

  /*
   * @see ICompletionProposal#getAdditionalProposalInfo()
   */
  public String getAdditionalProposalInfo()
  {
    try
    {
      fContext.setReadOnly(true);
      TemplateBuffer templateBuffer;
      try
      {
        templateBuffer = fContext.evaluate(fTemplate);
      } catch (TemplateException e)
      {
        return null;
      }

      return templateBuffer.getString();

    } catch (BadLocationException e)
    {
      return null;
    }
  }

  /*
   * @see ICompletionProposal#getDisplayString()
   */
  public String getDisplayString()
  {
    if (fDisplayString == null)
    {
      fDisplayString = fTemplate.getName() + " - " + fTemplate.getDescription();
    }
    return fDisplayString;
  }

  /*
   * @see ICompletionProposal#getImage()
   */
  public Image getImage()
  {
    return fImage;
  }

  /*
   * @see ICompletionProposal#getContextInformation()
   */
  public IContextInformation getContextInformation()
  {
    return null;
  }

  private void openErrorDialog(Shell shell, Exception e)
  {
    MessageDialog.openError(shell, "Template Evaluation Error", e.getMessage());
  }

  /**
   * Returns the relevance.
   * 
   * @return the relevance
   */
  public int getRelevance()
  {
    return fRelevance;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getInformationControlCreator()
   */
  public IInformationControlCreator getInformationControlCreator()
  {
    //		return new TemplateInformationControlCreator();
    return null;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(org.eclipse.jface.text.ITextViewer,
   *              boolean)
   */
  public void selected(ITextViewer viewer, boolean smartToggle)
  {
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(org.eclipse.jface.text.ITextViewer)
   */
  public void unselected(ITextViewer viewer)
  {
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument,
   *              int, org.eclipse.jface.text.DocumentEvent)
   */
  public boolean validate(IDocument document, int offset, DocumentEvent event)
  {
    try
    {
      int replaceOffset = getReplaceOffset();
      if (offset >= replaceOffset)
      {
        String content = document.get(replaceOffset, offset - replaceOffset);
        return fTemplate.getName().startsWith(content);
      }
    } catch (BadLocationException e)
    {
      // concurrent modification - ignore
    }
    return false;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getReplacementString()
   */
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset)
  {
    return fTemplate.getName();
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getReplacementOffset()
   */
  public int getPrefixCompletionStart(IDocument document, int completionOffset)
  {
    return getReplaceOffset();
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#apply(org.eclipse.jface.text.IDocument,
   *              char, int)
   */
  public void apply(IDocument document, char trigger, int offset)
  {
    // not called any longer
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#isValidFor(org.eclipse.jface.text.IDocument,
   *              int)
   */
  public boolean isValidFor(IDocument document, int offset)
  {
    // not called any longer
    return false;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getTriggerCharacters()
   */
  public char[] getTriggerCharacters()
  {
    // no triggers
    return new char[0];
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getContextInformationPosition()
   */
  public int getContextInformationPosition()
  {
    return fRegion.getOffset();
  }

  public void setYOrder(int order)
  {
    fYOrder = order;
  }

  public int getYOrder()
  {
    return fYOrder;
  }
}