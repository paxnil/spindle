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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.update.ui.forms.internal.SectionChangeManager;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.editors.SpindleForm;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.util.IStimulatable;

public class ComponentsForm extends SpindleForm implements IStimulatable {

  ComponentAliasSection aliasSection;
  ComponentAliasSummarySection summarySection;

  /**
   * Constructor for TapestryAppComponentsForm
   */
  public ComponentsForm(ComponentsFormPage page) {
    super(page);
  }

  public void stimulate(Object stimulus) {
    aliasSection.setSelection((String) stimulus);
  }

  public void initialize(Object modelObject) {
    super.initialize(modelObject);
    if (hasBeenInitialized()) {
      TapestryApplicationModel model = (TapestryApplicationModel) modelObject;
      String name = model.getApplicationSpec().getName();
      if (model.isEditable() == false) {
        name = MessageUtil.getFormattedString("TapistryComponentsForm.readonly", name);
      }
      setHeadingText("Component Aliasing");
      ((Composite)getControl()).layout(true);
    }
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

    Composite leftColumn = getFactory().createComposite(parent);
    gd = new GridData(GridData.FILL_VERTICAL);
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

    aliasSection = new ComponentAliasSection((SpindleFormPage)page);
    control = aliasSection.createControl(leftColumn, getFactory());
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    control.setLayoutData(gd);

    summarySection = new ComponentAliasSummarySection((SpindleFormPage)page);
    control = summarySection.createControl(rightColumn, getFactory());
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    control.setLayoutData(gd);

    registerSection(aliasSection);
    registerSection(summarySection);

    SectionChangeManager manager = new SectionChangeManager();
    manager.linkSections(aliasSection, summarySection);
  }

}