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

import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.BaseBindingsEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IBindingHolder;
import com.iw.plugins.spindle.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.spec.PluginParameterSpecification;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.IToolTipProvider;

public class ComponentBindingsEditorSection extends BaseBindingsEditorSection {

  private ToolTipProvider toolTipProvider = new ToolTipProvider();

  /**
   * Constructor for ParameterEditorSection
   */
  public ComponentBindingsEditorSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Bindings");
    setDescription("This section allows one to edit selected component's bindings");
    setUseToolTips(true);
    setToolTipProvider(toolTipProvider);
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    // this can only come from the ComponentSelectionSection and it can only be
    // that a new PluginContainedComponent was selected!
    selectedComponent = (IBindingHolder) changeObject;
    if (selectedComponent != null) {
      if (selectedComponent instanceof PluginContainedComponent) {
        PluginContainedComponent component = (PluginContainedComponent) selectedComponent;
        String copyOf = component.getCopyOf();
        if (copyOf != null && !"".equals(copyOf.trim())) {
          selectedComponent = null;
        }
      }
    }

    newButton.setEnabled(selectedComponent != null);
    inspectButton.setEnabled(selectedComponent != null);
    deleteButton.setEnabled(selectedComponent != null);
    updateNeeded = true;
    update();
  }

  protected ChooseBindingTypeDialog getDialog() {

    ChooseBindingTypeDialog dialog = null;

    TapestryComponentModel cmodel = null;
    Shell oldshell = TapestryPlugin.getDefault().getActiveWorkbenchShell().getShell();
    Shell shell = new Shell(oldshell.getDisplay(), oldshell.getStyle() | SWT.Resize | SWT.RESIZE);

    PluginContainedComponent containedComponent = (PluginContainedComponent) selectedComponent;

    Set existingBindingParms = getExistingBindingParameters();

    try {

      ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(getModel());

      cmodel = ComponentSelectionSection.resolveContainedComponent(project, containedComponent);

    } catch (CoreException e) {

    }

    if (cmodel != null) {

      dialog = new ChooseBindingTypeDialog(shell, cmodel, existingBindingParms, DTDVersion >= XMLUtil.DTD_1_2);

    } else {

      dialog = new ChooseBindingTypeDialog(shell, DTDVersion >= XMLUtil.DTD_1_2);
    }

    return dialog;

  }

  public class ToolTipProvider extends BindingEditorLabelProvider implements IToolTipProvider {

    private HashMap toolTipInfo = new HashMap();

    //---------- IToolTipProvider ----------------------------//

    public String getToolTipText(Object object) {

      String result = null;

      PluginBindingSpecification bindingSpec = (PluginBindingSpecification) object;

      PluginContainedComponent containedComponent = (PluginContainedComponent) selectedComponent;
      TapestryComponentModel cmodel = null;

      try {

        ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(getModel());
        cmodel = ComponentSelectionSection.resolveContainedComponent(project, containedComponent);

      } catch (CoreException e) {

      }

      if (cmodel == null) {

        result = "could not resolve '" + containedComponent.getType();

      } else {

        PluginComponentSpecification componentSpec = cmodel.getComponentSpecification();

        String parameter = bindingSpec.getIdentifier();

        PluginParameterSpecification parameterSpec = (PluginParameterSpecification) componentSpec.getParameter(parameter);

        if (parameterSpec == null) {

          result = "no parameter '" + parameter + "' found in " + cmodel.getUnderlyingStorage().getFullPath().toString();
        } else {

          StringBuffer buffer = new StringBuffer();
          parameterSpec.getHelpText(parameter, buffer);
          result = buffer.toString();

        }

      }

      return result;

    }

  }

}
