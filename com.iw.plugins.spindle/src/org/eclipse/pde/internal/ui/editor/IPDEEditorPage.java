package org.eclipse.pde.internal.ui.editor;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.update.ui.forms.internal.IFormPage;

public interface IPDEEditorPage extends IEditorPart, IFormPage {

boolean contextMenuAboutToShow(IMenuManager manager);
IAction getAction(String id);
	public IContentOutlinePage getContentOutlinePage();
	public IPropertySheetPage getPropertySheetPage();
void openTo(Object object);
boolean performGlobalAction(String id);
void update();
boolean canPaste(Clipboard clipboard);
}
