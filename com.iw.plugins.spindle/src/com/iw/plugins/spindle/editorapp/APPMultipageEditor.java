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
package com.iw.plugins.spindle.editorapp;

import org.eclipse.pde.internal.ui.editor.IPDEEditorPage;
import org.eclipse.ui.IEditorInput;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.editorapp.components.*;
import com.iw.plugins.spindle.editorapp.pages.*;
import com.iw.plugins.spindle.editors.DocumentationFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.editors.XMLEditorPage;

//Commented out Dependency Page until replacement available
public class APPMultipageEditor extends SpindleMultipageEditor {

  public static final String OVERVIEW = "OVERVIEW";
  public static final String DEPENDS = "DEPENDS";
  public static final String COMPONENTS = "COMPONENTS";
  public static final String PAGES = "PAGES";
  public static final String DOCUMENTATION = "DOCUMENTATION";
  public static final String SOURCE_PAGE = "SOURCEPAGE";

  /**
   * Constructor for TapestryMultipageEditor
   */
  public APPMultipageEditor() {
    super();
  }

  /**
   * @see PDEMultiPageEditor#getSourcePageId()
   */
  protected String getSourcePageId() {
    return SOURCE_PAGE;
  }

  /**
   * @see PDEMultiPageEditor#getHomePage()
   */
  public IPDEEditorPage getHomePage() {
    return getPage(OVERVIEW);
  }

  /**
   * @see PDEMultiPageEditor#createPages()
   */
  protected void createPages() {
    firstPageId = OVERVIEW;
    formWorkbook.setFirstPageSelected(true);
    addPage(
      OVERVIEW,
      new OverviewApplicationFormPage(
        this,
        MessageUtil.getString("AppMultipageEditor.OverviewTabLabel")));
//    addPage(
//      DEPENDS,
//      new DependencyFormPage(this, MessageUtil.getString("AppMultipageEditor.DependenciesTabLabel")));
    addPage(
      COMPONENTS,
      new ComponentsFormPage(this, MessageUtil.getString("AppMultipageEditor.ComponentsTabLabel")));
    addPage(PAGES, new PagesFormPage(this, MessageUtil.getString("AppMultipageEditor.PagesTabLabel")));
    addPage(
      DOCUMENTATION,
      new DocumentationFormPage(this, MessageUtil.getString("AppMultipageEditor.DocTabLabel")));
    addPage(SpindleMultipageEditor.SOURCE_PAGE, new XMLEditorPage(this));

  }

  protected boolean isValidContentType(IEditorInput input) {
    String name = input.getName().toLowerCase();
    return name.endsWith(MessageUtil.getString("AppMultipageEditor.ValidContentType"));

  }       

}