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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.editorapp.OverviewAlertSection;
import com.iw.plugins.spindle.editorjwc.components.*;
import com.iw.plugins.spindle.editors.ParameterEditorSection;
import com.iw.plugins.spindle.editors.PropertyEditableSection;
import com.iw.plugins.spindle.editors.SpindleForm;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryComponentModel;

public class OverviewFormPage extends SpindleFormPage {

  /**
   * Constructor for OverviewFormPage
   */
  public OverviewFormPage(SpindleMultipageEditor editor, String title) {
    super(editor, title);
  }

  /**
   * @see PDEFormPage#createForm()
   */
  protected AbstractSectionForm createForm() {
    return new OverviewForm(this);
  }

  /**
   * @see PDEFormPage#createContentOutlinePage()
   */
  public IContentOutlinePage createContentOutlinePage() {
    return new JWCContentOutlinePage(this);
  }

  class OverviewForm extends SpindleForm {

    private OverviewAlertSection alertSection;
    private OverviewGeneralSection generalSection;
    private PropertyEditableSection propertySection;
    private OverviewComponentsSection componentsSection;
    private ParameterEditorSection parameterSection;
    private OverviewBeanSection beanSection;
    private AssetsEditorSection assetSection;
    private ReservedParametersSection reservedSection;

    /**
     * Constructor for OverviewForm
     */
    public OverviewForm(SpindleFormPage page) {
      super(page);
    }

    protected void createFormClient(Composite parent) {
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      layout.marginWidth = 10;
      layout.horizontalSpacing = 15;
      parent.setLayout(layout);

      GridData gd;
      Control control;

      // To be shown when a strategy for handling markers is devised
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
      gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
      rightColumn.setLayoutData(gd);
      GridLayout rightLayout = new GridLayout();
      rightLayout.verticalSpacing = 10;
      rightLayout.marginWidth = 0;
      rightColumn.setLayout(rightLayout);

      generalSection = new OverviewGeneralSection((SpindleFormPage) page);
      control = generalSection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      parameterSection = new ParameterEditorSection((SpindleFormPage) page);
      control = parameterSection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      propertySection = new PropertyEditableSection((SpindleFormPage) page);
      control = propertySection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      componentsSection = new OverviewComponentsSection((SpindleFormPage) page);
      control = componentsSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      reservedSection = new ReservedParametersSection((SpindleFormPage) page);
      control = reservedSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      beanSection = new OverviewBeanSection((SpindleFormPage) page);
      control = beanSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      assetSection = new AssetsEditorSection((SpindleFormPage) page);
      control = assetSection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      //    registerSection(alertSection);
      registerSection(generalSection);
      registerSection(parameterSection);
      registerSection(propertySection);
      registerSection(reservedSection);
      registerSection(beanSection);
      registerSection(componentsSection);
      registerSection(assetSection);
    }

    public void initialize(Object modelObject) {
      super.initialize(modelObject);
      if (hasBeenInitialized()) {
        TapestryComponentModel model = (TapestryComponentModel) modelObject;
        String name = model.getUnderlyingStorage().getName();
        if (model.isEditable() == false) {
          name = MessageUtil.getFormattedString("TapistryComponentOverviewForm.readonly", name);
        }
        setHeadingText(name);
        ((Composite) getControl()).layout(true);
      }
    }
  }
}