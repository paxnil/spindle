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
package com.iw.plugins.spindle.editorlib.extensions;

import net.sf.tapestry.util.IPropertyHolder;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.editors.PropertyEditableSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;

public class ExtensionPropertiesSection
  extends PropertyEditableSection
  implements IModelChangedListener {


  /**
   * Constructor for PropertySection 
   */
  public ExtensionPropertiesSection(SpindleFormPage page) {
    super(page);
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    externalPropertyHolder = (IPropertyHolder) changeObject;

    newButton.setEnabled(externalPropertyHolder != null);
    editButton.setEnabled(externalPropertyHolder != null);
    deleteButton.setEnabled(externalPropertyHolder != null);
    updateNeeded = true;
    update();
  }

//  /**
//   * @see com.iw.plugins.spindle.editors.PropertyEditableSection#setDefaultPropertyHolder(IPropertyHolder)
//   */
//  protected void setDefaultExternalPropertyHolder(IPropertyHolder holder) {
//    
//    // we override to do nothing as this is called for init.
//    // we don't want that here, our default gets set by sectionChanged!
//  }

  /**
   * @see com.iw.plugins.spindle.editors.PropertyEditableSection#setDefaultExternalPropertyHolder(IPropertyHolder)
   */
  protected void setDefaultExternalPropertyHolder(IPropertyHolder holder) {
   
  }

}