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

import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;

import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.editors.SpindlePropertySheet;

public class BeansFormPage extends SpindleFormPage {

  BeansForm form;
  /**
   * Constructor for OverviewFormPage
   */
  public BeansFormPage(SpindleMultipageEditor editor, String title) {
    super(editor, title);
  }

  /**
   * @see PDEFormPage#createForm()
   */
  protected AbstractSectionForm createForm() {
  	form = new BeansForm(this);  	
    return form;  
  }

  /**
   * @see PDEFormPage#createContentOutlinePage()
   */
  public IContentOutlinePage createContentOutlinePage() {
    return null;
  }
  
  public IPropertySheetPage createPropertySheetPage() {
    return new SpindlePropertySheet();
  }  
  
  // called by clicking Bean link on the Overview Page
  // the bean name is passed
  public void openTo(Object object) {
  	form.showBean((String)object);  	
  }

}

