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

import org.eclipse.pde.internal.ui.editor.PDEFormSection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.model.BaseTapestryModel;

public abstract class SpindleFormSection extends PDEFormSection {

  /**
   * Constructor for TapestryFormSection
   */
  public SpindleFormSection(SpindleFormPage page) {
    super(page);
    setCollapsable(true);
  }
  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */ 
  public abstract Composite createClient(Composite arg0, FormWidgetFactory arg1);
  
  public boolean canUpdate() {
  	return ((BaseTapestryModel)getFormPage().getModel()).isLoaded();
  }

}

