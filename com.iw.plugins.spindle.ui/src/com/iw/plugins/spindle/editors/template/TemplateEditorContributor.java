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
package com.iw.plugins.spindle.editors.template;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import com.iw.plugins.spindle.UIPlugin;

public class TemplateEditorContributor extends TextEditorActionContributor
{

  protected RetargetTextEditorAction fContentAssistProposal;

  /**
   * Creates a multi-page contributor.
   */
  public TemplateEditorContributor()
  {
    super();

    fContentAssistProposal = new RetargetTextEditorAction(UIPlugin
        .getDefault()
        .getResourceBundle(), "ContentAssistProposal.");
    fContentAssistProposal
        .setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);

  }

  private void doSetActiveEditor(IEditorPart part)
  {
    super.setActiveEditor(part);

    ITextEditor editor = null;
    if (part instanceof ITextEditor)
      editor = (ITextEditor) part;

    fContentAssistProposal.setAction(getAction(editor, "ContentAssistProposal"));
  }

  /*
   * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
   */
  public void setActiveEditor(IEditorPart part)
  {
    super.setActiveEditor(part);
    doSetActiveEditor(part);
  }

  /*
   * @see IEditorActionBarContributor#dispose()
   */
  public void dispose()
  {
    doSetActiveEditor(null);
    super.dispose();
  }
}