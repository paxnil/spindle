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
import org.eclipse.update.ui.forms.internal.SectionChangeManager;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.editors.SpindleForm;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.TapestryComponentModel;

public class BeansForm extends SpindleForm {

  private BeanSelectionSection selectionSection;
  private BeanSummarySection summarySection;
  private BeanDescriptionSection descriptionSection;
  private BeanInitializerEditorSection editorSection;

  /**
   * Constructor for OverviewForm
   */
  public BeansForm(SpindleFormPage page) {
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

    selectionSection = new BeanSelectionSection((SpindleFormPage)page);
    control = selectionSection.createControl(leftColumn, getFactory());
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    control.setLayoutData(gd);

    summarySection = new BeanSummarySection((SpindleFormPage)page);
    control = summarySection.createControl(rightColumn, getFactory());
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    control.setLayoutData(gd);

    descriptionSection = new BeanDescriptionSection((SpindleFormPage)page);
    control = descriptionSection.createControl(rightColumn, getFactory());
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    control.setLayoutData(gd);

    editorSection = new BeanInitializerEditorSection((SpindleFormPage)page);
    control = editorSection.createControl(rightColumn, getFactory());
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    control.setLayoutData(gd);
    control = editorSection.getControl();
    gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 200;
    gd.verticalSpan = 40;
    control.setLayoutData(gd);

    registerSection(selectionSection);
    registerSection(descriptionSection);
    registerSection(summarySection);
    registerSection(editorSection);

    SectionChangeManager manager = new SectionChangeManager();
    manager.linkSections(selectionSection, summarySection);
    manager.linkSections(selectionSection, descriptionSection);
    manager.linkSections(selectionSection, editorSection);
  }

  public void showBean(String name) {
    selectionSection.setSelectedBean(name);
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
      ((Composite)getControl()).layout(true);
    }
  }
}