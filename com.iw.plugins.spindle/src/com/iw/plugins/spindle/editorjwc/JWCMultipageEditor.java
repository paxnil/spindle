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
package com.iw.plugins.spindle.editorjwc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.ui.editor.IPDEEditorPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editorjwc.beans.*;
import com.iw.plugins.spindle.editorjwc.components.*;
import com.iw.plugins.spindle.editors.DocumentationFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.editors.XMLEditorPage;
import com.iw.plugins.spindle.model.TapestryComponentModel;

//Commented out Dependency Page until replacement available

public class JWCMultipageEditor extends SpindleMultipageEditor {

  public static final String OVERVIEW = "OVERVIEW";
  public static final String DEPENDS = "DEPENDS";
  public static final String COMPONENTS = "COMPONENTS";
  public static final String BEANS = "BEANS";
  public static final String DOCUMENTATION = "DOCUMENTATION";

  /**
   * Constructor for TapestryMultipageEditor
   */
  public JWCMultipageEditor() {
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
      new OverviewFormPage(this, MessageUtil.getString("JWCMultipageEditor.OverviewTabLabel")));
    //    addPage(
    //      DEPENDS,
    //      new DependencyFormPage(this, MessageUtil.getString("JWCMultipageEditor.DependenciesTabLabel")));
    addPage(
      COMPONENTS,
      new ComponentsFormPage(this, MessageUtil.getString("JWCMultipageEditor.ComponentsTabLabel")));
    addPage(
      BEANS,
      new BeansFormPage(this, MessageUtil.getString("JWCMultipageEditor.BeansTabLabel")));
    addPage(
      DOCUMENTATION,
      new DocumentationFormPage(this, MessageUtil.getString("JWCMultipageEditor.DocTabLabel")));
    super.createPages();
  }

  public boolean isModelCorrect(Object model) {
    if (model != null) {
      TapestryComponentModel cmodel = (TapestryComponentModel) model;
      if (!cmodel.isLoaded()) {
        try {
          cmodel.load();
          return cmodel.getComponentSpecification() != null;
        } catch (CoreException e) {
          return false;
        }
      }
    }
    return true;
  }

  protected boolean isValidContentType(IEditorInput input) {
    String name = input.getName().toLowerCase();
    return name.endsWith(MessageUtil.getString("JWCMultipageEditor.ValidContentType"));
  }

  /**
  * @see com.iw.plugins.spindle.editors.SpindleMultipageEditor#getDefaultHeadingImage()
  */
  public Image getDefaultHeadingImage() {
    return TapestryImages.getSharedImage("component_banner.gif");
  }

}