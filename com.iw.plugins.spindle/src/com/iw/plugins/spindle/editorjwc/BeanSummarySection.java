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
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.primix.tapestry.spec.BeanLifecycle;

public class BeanSummarySection extends SpindleFormSection {

  private FormEntry beanName;
  private FormEntry beanClass;
  private FormEntry beanLifeCycle;
  private Composite container;

  private PluginBeanSpecification selectedBean;

  /**
   * Constructor for ComponentAliasSummarySection
   */
  public BeanSummarySection(SpindleFormPage page) {
    super(page);
    setHeaderText("Bean Summary");
    setDescription("This section will show info about the selected bean");
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClient(Composite parent, FormWidgetFactory factory) {
    container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 7;
    layout.horizontalSpacing = 6;
    container.setLayout(layout);

    String labelName = "Component Id";
    beanName = new FormEntry(createText(container, labelName, factory));
    Text text = (Text) beanName.getControl();
    text.setEditable(false);
    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));

    labelName = "Type";
    beanClass = new FormEntry(createText(container, labelName, factory));
    text = (Text) beanClass.getControl();
    text.setEditable(false);
    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
    
    labelName = "Lifecycle";
    beanLifeCycle = new FormEntry(createText(container, labelName, factory));
    text = (Text) beanLifeCycle.getControl();
    text.setEditable(false);
    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));    

    factory.paintBordersFor(container);
    return container;
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    // this can only come from the ComponentSelectionSection and it can only be
    // that a new PluginContainedComponent was selected!
    selectedBean = (PluginBeanSpecification) changeObject;
    updateSummary();
  }

  private void updateSummary() {
    String newId = "";
    String newType = "";
    String newLifecycle = "";
    if (selectedBean != null) {
      newId = findBeanName();
      newType = selectedBean.getClassName();
      newLifecycle = convertLifecycleToString(selectedBean.getLifecycle());
    }
    beanName.setValue(newId, false);
    beanClass.setValue(newType, false);
    beanLifeCycle.setValue(newLifecycle, false);
  }

  private String findBeanName() {
    PluginComponentSpecification spec =
      ((TapestryComponentModel) getFormPage().getModel()).getComponentSpecification();
    if (selectedBean != null) {
      Iterator ids = spec.getBeanNames().iterator();
      while (ids.hasNext()) {
        String id = (String) ids.next();
        if (spec.getBeanSpecification(id) == selectedBean) {
          return id;
        }
      }
    }
    return "";
  }

  private String convertLifecycleToString(BeanLifecycle value) {
    if (value != null) {
      if (value == BeanLifecycle.NONE) {
        return "NONE";
      }
      if (value == BeanLifecycle.PAGE) {
        return "PAGE";
      }
      if (value == BeanLifecycle.REQUEST) {
        return "REQUEST";
      }
    }
    return "";
  }

}