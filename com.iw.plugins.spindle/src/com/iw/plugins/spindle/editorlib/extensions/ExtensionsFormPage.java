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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;
import org.eclipse.update.ui.forms.internal.SectionChangeManager;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editorlib.EditLibrariesSection;
import com.iw.plugins.spindle.editorlib.LibraryContentOutlinePage;
import com.iw.plugins.spindle.editorlib.LibraryServicesSection;
import com.iw.plugins.spindle.editorlib.OverviewAlertSection;
import com.iw.plugins.spindle.editorlib.OverviewComponentRefSection;
import com.iw.plugins.spindle.editorlib.OverviewLibGeneralSection;
import com.iw.plugins.spindle.editorlib.OverviewPageSection;
import com.iw.plugins.spindle.editors.PropertyEditableSection;
import com.iw.plugins.spindle.editors.SpindleForm;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.util.IStimulatable;

public class ExtensionsFormPage extends SpindleFormPage {
	
  IStimulatable form;

  public ExtensionsFormPage(SpindleMultipageEditor editor, String title) {
    super(editor, title);
  }

  /**
   * @see PDEFormPage#createForm()
   */
  protected AbstractSectionForm createForm() {
  	
  	ExtensionsForm eForm = new ExtensionsForm(this);
  	form = (IStimulatable)eForm;
    return eForm;
  }

 
  public IContentOutlinePage createContentOutlinePage() {
    return new LibraryContentOutlinePage(this);
  }
  
  public void openTo(Object object) {
    form.stimulate(object);
  }

  protected class ExtensionsForm extends SpindleForm implements IStimulatable {

    private ExtensionSection extensionSection;
    private ExtensionPropertiesSection propertiesSection;
    private ExtensionConfigurationSection configSection;

    public ExtensionsForm(SpindleFormPage page) {
      super(page);
    }
    
    public void stimulate(Object stimulus) {
      extensionSection.setSelection((String) stimulus);
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

      extensionSection = new ExtensionSection(page);
      control = extensionSection.createControl(leftColumn, getFactory());
      gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
      gd.widthHint = 200;
      gd.verticalSpan = 75;
      control.setLayoutData(gd);

      propertiesSection = new ExtensionPropertiesSection((SpindleFormPage) page);
      control = propertiesSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);
      
      configSection = new ExtensionConfigurationSection((SpindleFormPage) page);
      control = configSection.createControl(rightColumn, getFactory());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      control.setLayoutData(gd);

      registerSection(extensionSection);
      registerSection(propertiesSection);
      registerSection(configSection);

      SectionChangeManager manager = new SectionChangeManager();
      manager.linkSections(extensionSection, propertiesSection);
      manager.linkSections(extensionSection, configSection);

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

}