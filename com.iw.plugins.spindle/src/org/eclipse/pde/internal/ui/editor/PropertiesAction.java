package org.eclipse.pde.internal.ui.editor;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.iw.plugins.spindle.TapestryPlugin;

public class PropertiesAction extends Action {
	public static final String LABEL = "Actions.properties.label";
	private PDEMultiPageEditor editor;

public PropertiesAction(PDEMultiPageEditor editor) {
	this.editor = editor;
	setText("Inspect");//PDEPlugin.getResourceString(LABEL));
}
public void run() {
	try {
		String viewId = IPageLayout.ID_PROP_SHEET;
		IWorkbenchPage perspective = TapestryPlugin.getDefault().getActivePage();
		IViewPart view = perspective.showView(viewId);
		editor.updateSynchronizedViews(editor.getCurrentPage());
		perspective.activate(editor);
		perspective.bringToTop(view);
	} catch (PartInitException e) {
		TapestryPlugin.getDefault().logException(e);
	}
}
}
