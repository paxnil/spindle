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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.pde.internal.ui.editor.text.ColorManager;
import org.eclipse.pde.internal.ui.editor.text.IColorManager;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class XMLEditorPage extends TapestrySourcePage {

  private IColorManager colorManager = new ColorManager();

  public XMLEditorPage(SpindleMultipageEditor editor) {
    super(editor);
    setSourceViewerConfiguration(new SpindleXMLConfiguration(colorManager));
  }

  public IContentOutlinePage createContentOutlinePage() {
    return null;
    /*
    return new ManifestSourceOutlinePage(
    	getEditorInput(),
    	getDocumentProvider(),
    	this);*/
  }
  public void dispose() {
    colorManager.dispose();
    super.dispose();
  }
  protected void editorContextMenuAboutToShow(MenuManager menu) {
    getEditor().editorContextMenuAboutToShow(menu);
    menu.add(new Separator());
    super.editorContextMenuAboutToShow(menu);
  }
}