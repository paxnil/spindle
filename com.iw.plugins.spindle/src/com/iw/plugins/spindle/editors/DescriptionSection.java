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

import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.model.BaseTapestryModel;

public class DescriptionSection extends SpindleFormSection {

  private Text description;
  private Font font;

  private boolean updateNeeded = false;
  private boolean duringInit = false;

  /**
   * Constructor for ComponentAliasSummarySection
   */
  public DescriptionSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Description");
    setDescription("Here's a space to add some documentation");
  }

  public void dispose() {
    if (description != null) {
      description.dispose();
      description = null;
    }
    if (font != null) {
      font.dispose();
      font = null;
    }
  }

  public void initialize(Object input) {
    duringInit = true;
    BaseTapestryModel model = (BaseTapestryModel) input;
    update(model);
    description.setEditable(model.isEditable() && model.isLoaded());
    model.addModelChangedListener(this);
    duringInit = false;
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClientContainer(Composite parent, FormWidgetFactory factory) {
    Composite container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    container.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.grabExcessVerticalSpace = true;
    container.setLayoutData(gd);

    description = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
    gd.verticalSpan = 10;
    description.setLayoutData(gd);
    description.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent event) {        
        if (!duringInit) {
            ((BaseTapestryModel) getFormPage().getModel()).setDescription(description.getText());
        	forceDirty();
        }
      }
    });
    FontData data = new FontData("courier", SWT.NULL, 6);
    // on windows platform, I need to do the following
    // even though the values are the same as the constructor's
    data.setStyle(SWT.NORMAL);
    data.setHeight(6);
    font = new Font(description.getDisplay(), data);
    description.setFont(font);
    factory.paintBordersFor(container);
    return container;
  }

  public void commitChanges(boolean onSave) {
    BaseTapestryModel model = (BaseTapestryModel) getFormPage().getModel();
    if (isDirty() && model.isEditable()) {
      model.setDescription(description.getText());
    }
    super.commitChanges(onSave);
  }

  public void update() {
    if (updateNeeded) {
      this.update(getFormPage().getModel());
    }
  }

  public void update(Object input) {
    BaseTapestryModel model = (BaseTapestryModel) input;
    if (!model.isLoaded()) {
      return;
    }
    String desc = model.getDescription();
    description.setText(desc == null ? "" : desc);
    updateNeeded = false;
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

}