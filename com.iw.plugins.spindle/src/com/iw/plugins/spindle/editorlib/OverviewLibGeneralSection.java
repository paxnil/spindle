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

import net.sf.tapestry.spec.ILibrarySpecification;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.ui.forms.internal.FormEntry;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import org.eclipse.update.ui.forms.internal.IFormTextListener;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;

public class OverviewLibGeneralSection extends SpindleFormSection implements IModelChangedListener {

  private Text dtdText;
  private FormEntry nameText;
  private boolean updateNeeded;

  public OverviewLibGeneralSection(SpindleFormPage page) {
    super(page);
    setHeaderText("General Information");
    setDescription("This section describes general information about this library");
  }

  public void initialize(Object input) {
    TapestryLibraryModel model = (TapestryLibraryModel) input;
    update(input);
    dtdText.setEditable(false);
    if (model.isEditable() == false) {
      nameText.getControl().setEditable(false);
    }
    model.addModelChangedListener(this);
  }

  public void dispose() {
    dtdText.dispose();
    super.dispose();
  }

  public void update() {
    if (updateNeeded) {
      this.update(getFormPage().getModel());
    }
  }

  public void update(Object input) {
    TapestryLibraryModel model = (TapestryLibraryModel) input;
    ILibrarySpecification spec = (ILibrarySpecification) model.getSpecification();
    String name = model.getUnderlyingStorage().getName();
    String dtdVersion = spec.getPublicId();
    if (dtdVersion == null) {
      dtdVersion = "Undetermined DTD";
    } 
    
    getFormPage().getForm().setHeadingText(name);
    ((SpindleMultipageEditor) getFormPage().getEditor()).updateTitle();
    nameText.setValue(model.getUnderlyingStorage().getName(), true);
    dtdText.setText(dtdVersion);
    updateNeeded = false;
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClientContainer(Composite parent, FormWidgetFactory factory) {
    Composite container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 7;
    layout.horizontalSpacing = 6;
    container.setLayout(layout);

    final TapestryLibraryModel model = (TapestryLibraryModel) getFormPage().getModel();


	ILibrarySpecification spec = model.getSpecification();
    String labelName = "DTD";
    dtdText = createText(container, labelName, factory);
    dtdText.setText(spec == null ? "xml has errors" : model.getSpecification().getPublicId());
    dtdText.setEnabled(false);



    labelName = "Library Name";
    nameText = new FormEntry(createText(container, labelName, factory));
    nameText.addFormTextListener(new IFormTextListener() {
      public void textValueChanged(FormEntry text) {
      	PluginApplicationSpecification appSpec = (PluginApplicationSpecification)model.getSpecification();
        String name = appSpec.getName();
        appSpec.setName(text.getValue());
        if (model.isEditable() == false) {
          name = MessageUtil.getFormattedString("{0} is READ ONLY", name);
        }
        getFormPage().getForm().setHeadingText(name);
        ((SpindleMultipageEditor) getFormPage().getEditor()).updateTitle(name);
      }
      public void textDirty(FormEntry text) {
        forceDirty();
      }
    });
    ((Text)nameText.getControl()).setEditable(false);

    factory.paintBordersFor(container);
    return container;
  }



  public boolean isDirty() {
    return nameText.isDirty();
  }

  private void forceDirty() {
    setDirty(true);
    IModel model = (IModel) getFormPage().getModel();
    if (model instanceof IEditable) {
      IEditable editable = (IEditable) model;
      editable.setDirty(true);
      getFormPage().getEditor().fireSaveNeeded();
    }
  }

  public void commitChanges(boolean onSave) {
    nameText.commit();
  }

  public void modelChanged(IModelChangedEvent event) {
    int eventType = event.getChangeType();
    if (eventType == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
      return;
    }
    if (eventType == IModelChangedEvent.CHANGE) {
      updateNeeded = true;
    }
  }

}