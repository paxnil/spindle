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

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;

//Commented out Dependency Page until replacement available

public class JWCPageMultipageEditor extends JWCMultipageEditor { 


  /**
   * Constructor for TapestryMultipageEditor
   */
  public JWCPageMultipageEditor() {
    super();
  }

  protected boolean isValidContentType(IEditorInput input) {
    String name = input.getName().toLowerCase();
    String valid = MessageUtil.getString("JWCPageMultipageEditor.ValidContentType");
    return name.endsWith(valid);
  }
  
  /**
   * @see com.iw.plugins.spindle.editors.SpindleMultipageEditor#getDefaultHeadingImage()
   */
  public Image getDefaultHeadingImage() {
    return TapestryImages.getSharedImage("page_banner.gif");
  }

}