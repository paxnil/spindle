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
package com.iw.plugins.spindle.editorlib;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.editors.PropertyEditableSection;
import com.iw.plugins.spindle.editors.SpindleForm;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryLibraryModel;

public class OverviewLibFormPage extends SpindleFormPage {
	
  OverviewForm form;

  public OverviewLibFormPage(SpindleMultipageEditor editor, String title) {
    super(editor, title);
  }

  /**
   * @see PDEFormPage#createForm()
   */
  protected AbstractSectionForm createForm() {
  	form = new OverviewForm(this);
    return form;
  }

  /**
   * @see PDEFormPage#createContentOutlinePage()
   */
  /**
   * @see PDEFormPage#createContentOutlinePage()
   */
  public IContentOutlinePage createContentOutlinePage() {
    return new LibraryContentOutlinePage(this);
  }

  protected SpindleFormSection getGeneralSection(SpindleFormPage page) {
    return new OverviewLibGeneralSection(page);
  }

  protected class OverviewForm extends SpindleForm {

    private OverviewAlertSection alertSection;
    private SpindleFormSection generalSection;
    private OverviewPageSection pageSection;
    private OverviewComponentRefSection componentSection;
    private EditLibrariesSection librariesSection;
    private PropertyEditableSection propertySection;
    private LibraryServicesSection servicesSection;
    private OverviewExtensionSection extensionLinksSection;

    public OverviewForm(SpindleFormPage page) {
      super(page);
    }

    protected void createFormClient(Composite parent) {

      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      layout.marginWidth = 10;
      layout.horizontalSpacing = 15;
      //layout.makeColumnsEqualWidth=true;
      parent.setLayout(layout);

      GridData gd;
      Control control;

      // To be re-added when a strategy for handling alerts is devised
      //    alertSection = new OverviewAlertSection(page);
      //    control = alertSection.createControl(parent, getFactory());
      //    gd = new GridData(GridData.FILL_HORIZONTAL);
      //    gd.horizontalSpan = 2;
      //    control.setLayoutData(gd);

      Composite leftColumn = getFactory().createComposite(parent);
      gd = new GridData(GridData.FILL_BOTH);
      leftColumn.setLayoutData(gd);
      GridLayout leftLayout = new GridLayout();
      leftLayout.verticalSpacing = 10;
      leftLayout.marginWidth = 0;
      leftColumn.setLayout(leftLayout);

      Composite rightColumn = getFactory().createComposite(parent);
      gd = new GridData(GridData.FILL_BOTH);
      rightColumn.setLayoutData(gd);
      GridLayout rightLayout = new GridLayout();
      rightLayout.verticalSpacing = 10;
      rightLayout.marginWidth = 0;
      rightColumn.setLayout(rightLayout);

      generalSection = getGeneralSection(page);
      control = generalSection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      librariesSection = new EditLibrariesSection((SpindleFormPage) page);
      control = librariesSection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);
      
      servicesSection = new LibraryServicesSection((SpindleFormPage) page);
      control = servicesSection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      propertySection = new PropertyEditableSection((SpindleFormPage) page);
      control = propertySection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      pageSection = new OverviewPageSection((SpindleFormPage) page);
      control = pageSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      componentSection = new OverviewComponentRefSection((SpindleFormPage) page);
      control = componentSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      extensionLinksSection = new OverviewExtensionSection((SpindleFormPage) page);
      control = extensionLinksSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      //    registerSection(alertSection);
      registerSection(generalSection);
      registerSection(pageSection);
      registerSection(componentSection);
      registerSection(propertySection);
      registerSection(servicesSection);
      registerSection(librariesSection);
      registerSection(extensionLinksSection);


    }

    public void initialize(Object modelObject) {
      super.initialize(modelObject);
      if (hasBeenInitialized()) {
        TapestryLibraryModel model = (TapestryLibraryModel) modelObject;
        if (!model.isLoaded()) {
          return;
        }
        String name = model.getUnderlyingStorage().getName();
        if (model.isEditable() == false) {
          name =
            MessageUtil.getFormattedString("TapistryLibraryForm.readonly", new String[] { name });
        }
        setHeadingText(name);
        ((Composite) getControl()).layout(true);
      }
    }

  }

  /**
   * @see com.iw.plugins.spindle.editors.SpindleFormPage#fillContextMenu(IMenuManager)
   */
  public void fillContextMenu(IMenuManager mng) {
  	form.fillContextMenu(mng);
  }

}