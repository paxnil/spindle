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
package com.iw.plugins.spindle.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.IFoldingCommandIds;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.spec.SpecEditor;

/**
 * FoldingToggleRulerAction TODO add something here
 * 
 * @author glongman@gmail.com
 * 
 */
public class FoldingToggleRulerAction extends AbstractRulerActionDelegate {

	private IAction fUIAction;
	private TextOperationAction fAction;
	private ITextEditor fTextEditor;

	/*
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#createAction(org.eclipse.ui.texteditor.ITextEditor, org.eclipse.jface.text.source.IVerticalRulerInfo)
	 */
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		fTextEditor= editor;
		fAction= new TextOperationAction(UIPlugin.getResourceBundle(), "Projection.Toggle.", editor, ProjectionViewer.TOGGLE, true); //$NON-NLS-1$
		fAction.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);

		return fAction;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		fUIAction= callerAction;
		super.setActiveEditor(callerAction, targetEditor);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	public void menuAboutToShow(IMenuManager manager) {
		update();
		super.menuAboutToShow(manager);
	}
	
	private void update() {
		if (fTextEditor instanceof SpecEditor) {
			ISourceViewer viewer= ((SpecEditor) fTextEditor).getViewer();
			if (viewer instanceof ProjectionViewer) {
				boolean enabled= ((ProjectionViewer) viewer).getProjectionAnnotationModel() != null;
				fUIAction.setChecked(enabled);
			}
		}
	}
}