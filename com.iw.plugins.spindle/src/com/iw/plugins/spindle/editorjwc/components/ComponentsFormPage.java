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
package com.iw.plugins.spindle.editorjwc.components;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;
import org.eclipse.update.ui.forms.internal.SectionChangeManager;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.editors.SpindleForm;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.util.IStimulatable;

public class ComponentsFormPage extends SpindleFormPage {

  private IStimulatable stim;

  /**
   * Constructor for OverviewFormPage
   */
  public ComponentsFormPage(SpindleMultipageEditor editor, String title) {
    super(editor, title);
  }

  /**
   * @see PDEFormPage#createForm()
   */
  protected AbstractSectionForm createForm() {
    AbstractSectionForm form = new ComponentsForm(this);
    stim = (IStimulatable) form;
    return form;
  }

  /**
   * @see PDEFormPage#createContentOutlinePage()
   */
  public IContentOutlinePage createContentOutlinePage() {
    return null;
  }

  public void openTo(Object object) {
    stim.stimulate(object);
  }

  class ComponentsForm extends SpindleForm implements IStimulatable {

    private ComponentSelectionSection selectionSection;
    private ComponentSummarySection summarySection;
    private ComponentBindingsEditorSection editorSection;

    /**
     * Constructor for OverviewForm
     */
    public ComponentsForm(SpindleFormPage page) {
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

      selectionSection = new ComponentSelectionSection((SpindleFormPage) page);
      control = selectionSection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);
      gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
      gd.widthHint = 200;
      gd.verticalSpan = 75;
      control.setLayoutData(gd);

      summarySection = new ComponentSummarySection((SpindleFormPage) page);
      control = summarySection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      editorSection = new ComponentBindingsEditorSection((SpindleFormPage) page);
      control = editorSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);
      control = editorSection.getControl();
      gd = new GridData(GridData.FILL_BOTH);
      gd.widthHint = 200;
      gd.verticalSpan = 75;
      control.setLayoutData(gd);

      registerSection(selectionSection);
      registerSection(summarySection);
      registerSection(editorSection);

      SectionChangeManager manager = new SectionChangeManager();
      manager.linkSections(selectionSection, summarySection);
      manager.linkSections(selectionSection, editorSection);

      editorSection.setPrecomputedAliasInfo(selectionSection.getPrecomputedAliasInfo());
    }

    public void stimulate(Object stimulus) {
      selectionSection.setSelection((String) stimulus);
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
