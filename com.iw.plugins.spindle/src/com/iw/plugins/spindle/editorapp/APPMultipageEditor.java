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

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editorlib.LibraryMultipageEditor;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;

public class APPMultipageEditor extends LibraryMultipageEditor {

  /**
   * Constructor for TapestryMultipageEditor
   */
  public APPMultipageEditor() {
    super();
  }

  protected SpindleFormPage getOverviewPage(SpindleMultipageEditor editor) {
    return new OverviewApplicationFormPage(
      this,
      MessageUtil.getString("AppMultipageEditor.OverviewTabLabel"));
  }

  protected boolean isValidContentType(IEditorInput input) {
    String name = input.getName().toLowerCase();
    return name.endsWith(MessageUtil.getString("AppMultipageEditor.ValidContentType"));

  }
  
    /**
   * @see com.iw.plugins.spindle.editors.SpindleMultipageEditor#getDefaultHeadingImage()
   */
  public Image getDefaultHeadingImage() {
    return TapestryImages.getSharedImage("application_banner.gif");
  }

}