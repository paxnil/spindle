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

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.ui.forms.internal.FormEntry;
import org.eclipse.update.ui.forms.internal.FormSection;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;

public class ComponentSummarySection extends SpindleFormSection {

  private FormEntry componentId;
  private FormEntry componentType;
  private Composite container;

  private PluginContainedComponent selectedComponent;

  /**
   * Constructor for ComponentAliasSummarySection
   */
  public ComponentSummarySection(SpindleFormPage page) {
    super(page);
    setHeaderText("Contained Component Summary (READ ONLY)");
    setDescription("This section will show info about the selected component");
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClientContainer(Composite parent, FormWidgetFactory factory) {
    container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 7;
    layout.horizontalSpacing = 6;
    container.setLayout(layout);

    String labelName = "Component Id";
    componentId = new FormEntry(createText(container, labelName, factory));
    Text text = (Text) componentId.getControl();
    text.setEditable(false);
    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));

    labelName = "Type";
    componentType = new FormEntry(createText(container, labelName, factory));
    text = (Text) componentType.getControl();
    text.setEditable(false);
    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));

    factory.paintBordersFor(container);
    return container;
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    selectedComponent = (PluginContainedComponent) changeObject;
    updateSummary();
  }

  private void updateSummary() {
    if (canUpdate()) {
      String newId = "";
      String newType = "";
      if (selectedComponent != null) {
        newId = findContainedComponentId();
        String copyOf = selectedComponent.getCopyOf();
        if (copyOf != null && !"".equals(copyOf.trim())) {
        	newType = "this contained component is a copy-of '"+copyOf+"'";
        } else {
        	newType = selectedComponent.getType();
        }
      }
      componentId.setValue(newId, false);
      
      componentType.setValue(newType, false);
    }
  }

  private String findContainedComponentId() {
    PluginComponentSpecification spec =
      ((TapestryComponentModel) getFormPage().getModel()).getComponentSpecification();
    if (selectedComponent != null) {
      Iterator ids = spec.getComponentIds().iterator();
      while (ids.hasNext()) {
        String id = (String) ids.next();
        if (spec.getComponent(id) == selectedComponent) {
          return id;
        }
      }
    }
    return "";
  }

}