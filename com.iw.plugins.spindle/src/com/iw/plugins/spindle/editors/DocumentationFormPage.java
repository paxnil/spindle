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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;

public class DocumentationFormPage extends SpindleFormPage {

  public DocumentationFormPage(SpindleMultipageEditor editor, String title) {
    super(editor, title);
  }

  /**
   * @see PDEFormPage#createForm()
   */
  protected AbstractSectionForm createForm() {
    return new DocumentationForm(this);
  }

  /**
   * @see PDEFormPage#createContentOutlinePage()
   */
  public IContentOutlinePage createContentOutlinePage() {
    return null;
  }

  protected class DocumentationForm extends SpindleForm {

    DescriptionSection descriptionSection;

    /**
     * Constructor for DocumentationForm
     */

    public DocumentationForm(SpindleFormPage page) {      
      super(page);
      setHeadingText("");
    }

    protected void createFormClient(Composite parent) {
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      parent.setLayout(layout);

      GridData gd;
      Control control;

      Composite container = getFactory().createComposite(parent);
      gd = new GridData(GridData.FILL_BOTH);
      container.setLayoutData(gd);
      layout = new GridLayout();
      layout.verticalSpacing = 10;
      layout.marginWidth = 0;
      container.setLayout(layout);

      descriptionSection = new DescriptionSection((SpindleFormPage)page);
      control = descriptionSection.createControl(container, getFactory());
      gd =
        new GridData(
          GridData.FILL_BOTH
            | GridData.VERTICAL_ALIGN_BEGINNING
            | GridData.GRAB_HORIZONTAL
            | GridData.GRAB_VERTICAL);
      control.setLayoutData(gd);

      registerSection(descriptionSection);
    }

    public void initialize(Object modelObject) {
      super.initialize(modelObject);
      if (hasBeenInitialized()) {
        setTitle("Documentation");
        super.initialize(modelObject);
        ((Composite)getControl()).layout(true);
      }
    }
  }

}