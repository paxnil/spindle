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

import net.sf.tapestry.parse.SpecificationParser;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.action.Action;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.ui.forms.internal.FormEntry;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import org.eclipse.update.ui.forms.internal.IFormTextListener;

import com.iw.plugins.spindle.editors.FormCheckbox;
import com.iw.plugins.spindle.editors.IFormCheckboxListener;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;

public class OverviewGeneralSection extends SpindleFormSection implements IModelChangedListener {

  
  private Text dtdText;
  private Text pathText;
  private FormEntry componentClassText;
  private Label bodyLabel;
  private Label informalsLabel;
  private FormCheckbox allowBody;
  private FormCheckbox allowInformalParameters;
  private boolean updateNeeded;
  private ChangeClassAction changeClassAction = new ChangeClassAction();

  /**
   * Constructor for TapistryAppGeneralSestion
   */
  public OverviewGeneralSection(SpindleFormPage page) {
    super(page);
    setHeaderText("General Information");
    setDescription("This section describes general information about this component");
  }

  public void initialize(Object input) {
    TapestryComponentModel model = (TapestryComponentModel) input;
    update(input);
    dtdText.setEditable(false);
    pathText.setEditable(false);
    if (model.isEditable() == false) {
      componentClassText.getControl().setEditable(false);
    }
    model.addModelChangedListener(this);
  }

  public void dispose() {
    dtdText.dispose();
    pathText.dispose();
    bodyLabel.dispose();
    informalsLabel.dispose();
    allowBody.getControl().dispose();
    allowInformalParameters.getControl().dispose();
    ((BaseTapestryModel) getFormPage().getModel()).removeModelChangedListener(this);
    super.dispose();
  }

  public void update() {
    if (updateNeeded && canUpdate()) {
      this.update(getFormPage().getModel());
    }
  }

  public void update(Object input) {
    TapestryComponentModel model = (TapestryComponentModel) input;
    if (!model.isLoaded()) {
      return;
    }

    PluginComponentSpecification spec = model.getComponentSpecification();

    IStorage storage = (IStorage) model.getUnderlyingStorage();
    String path = storage.getFullPath().toString();
    String name = storage.getName();
    if (!model.isEditable()) {
      name += "  (READ ONLY)";
    }
    
    String dtdVersion = spec.getDTDVersion();
    if (dtdVersion == null) {
    	dtdVersion = "Unknown DTD or pre 1.1 DTD";
    } else if ("1.1".equals(dtdVersion)) {
    	dtdVersion = SpecificationParser.TAPESTRY_DTD_1_1_PUBLIC_ID;
    } else if ("1.2".equals(dtdVersion)) {
    	dtdVersion = SpecificationParser.TAPESTRY_DTD_1_2_PUBLIC_ID;
    }
    
    dtdText.setText(dtdVersion);
    
    getFormPage().getForm().setHeadingText(name);
    ((SpindleMultipageEditor) getFormPage().getEditor()).updateTitle();
    pathText.setText(path);
    componentClassText.setValue(spec.getComponentClassName(), true);
    allowBody.setValue(spec.getAllowBody(), true);
    allowInformalParameters.setValue(spec.getAllowInformalParameters(), true);

    boolean editable = model.isEditable();
    componentClassText.getControl().setEditable(editable);
    allowBody.getControl().setEnabled(editable);
    allowInformalParameters.getControl().setEnabled(editable);

    updateNeeded = false;
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClient(Composite parent, FormWidgetFactory factory) {
    Composite container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 7;
    layout.horizontalSpacing = 6;
    container.setLayout(layout);

    final TapestryComponentModel model = (TapestryComponentModel) getFormPage().getModel();

    String labelName = "DTD";
    dtdText = createText(container, labelName, factory);
    dtdText.setText("-//Howard Ship//Tapestry Specification 1.2//EN");
    dtdText.setEnabled(false);

    labelName = "Component Path";
    pathText = createText(container, labelName, factory);
    pathText.setEnabled(false);

    labelName = "Specification Class";
    Text text = createText(container, labelName, factory);
    componentClassText = new FormEntry(text);
    componentClassText.addFormTextListener(new IFormTextListener() {
      //called on commit	
      public void textValueChanged(FormEntry text) {
        model.getComponentSpecification().setComponentClassName(text.getValue());
      }

      public void textDirty(FormEntry text) {
        forceDirty();
      }
    });

    labelName = "Allow Body";
    bodyLabel = factory.createLabel(container, labelName);
    allowBody = new FormCheckbox(container, null);
    allowBody.addFormCheckboxListener(new IFormCheckboxListener() {
      public void booleanValueChanged(FormCheckbox box) {
        model.getComponentSpecification().setAllowBody(box.getValue());
      }

      public void valueDirty(FormCheckbox box) {
        forceDirty();
      }
    });
    ((Button) allowBody.getControl()).setBackground(factory.getBackgroundColor());

    labelName = "Allow Informal Paramters";
    informalsLabel = factory.createLabel(container, labelName);
    allowInformalParameters = new FormCheckbox(container, null);
    allowInformalParameters.addFormCheckboxListener(new IFormCheckboxListener() {
      //called on commit
      public void booleanValueChanged(FormCheckbox box) {
        model.getComponentSpecification().setAllowInformalParameters(box.getValue());
      }

      public void valueDirty(FormCheckbox box) {
        forceDirty();
      }
    });
    ((Button) allowInformalParameters.getControl()).setBackground(factory.getBackgroundColor());

    factory.paintBordersFor(container);
    return container;
  }

  private boolean checkEngineClass(String value) {
    return true;
  }

  public boolean isDirty() {
    return componentClassText.isDirty() || allowBody.isDirty() || allowInformalParameters.isDirty();
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
    componentClassText.commit();
    allowBody.commit();
    allowInformalParameters.commit();
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
  }
  
  /**
   * @version 	1.0
   * @author
   */
  public static class ChangeClassAction extends Action {

    /*
    * @see IAction#run()
    */
    /**
     * Constructor for ChangeClassAction.
     */
    protected ChangeClassAction() {
      super();
      setText("change specification class");
    }    

    public void run() {
      
    }

  }


}