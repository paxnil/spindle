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
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.BaseBindingsEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.ModelUtils;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.manager.TapestryModelManager;
import com.iw.plugins.spindle.spec.IBindingHolder;
import com.iw.plugins.spindle.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.spec.PluginParameterSpecification;
import com.iw.plugins.spindle.ui.ComponentAliasParameterViewer;
import com.iw.plugins.spindle.ui.IToolTipHelpProvider;
import com.iw.plugins.spindle.ui.IToolTipProvider;

public class ComponentBindingsEditorSection extends BaseBindingsEditorSection {

  private ToolTipProvider toolTipProvider = new ToolTipProvider();

  private HashMap precomputedAliasInfo = new HashMap();

  /**
   * Constructor for ParameterEditorSection
   */
  public ComponentBindingsEditorSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Bindings");
    setDescription("This section allows one to edit selected component's bindings");
    setUseToolTips(true);
    setToolTipProvider(toolTipProvider);
    setToolTipHelpProvider(toolTipProvider);
  }

  public void setPrecomputedAliasInfo(HashMap map) {
    precomputedAliasInfo = map;
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
    editButton.setEnabled(selectedComponent != null);
    deleteButton.setEnabled(selectedComponent != null);
    updateNeeded = true;
    update();
  }

  protected ChooseBindingTypeDialog getDialog() {

    TapestryModelManager mgr = TapestryPlugin.getTapestryModelManager();
    TapestryComponentModel cmodel = null;
    ChooseBindingTypeDialog dialog = null;
    Shell shell = newButton.getShell();

    PluginContainedComponent containedComponent = (PluginContainedComponent) selectedComponent;

    Set existingBindingParms = getExistingBindingParameters();

    if (precomputedAliasInfo.isEmpty()) {

      String selectedType = containedComponent.getType();

      if (selectedComponent != null) {

        cmodel = ModelUtils.findComponent(selectedType, getModel());

        if (cmodel != null) {
          dialog = new ChooseBindingTypeDialog(shell, cmodel, existingBindingParms, isDTD12);
        } else {
          dialog = new ChooseBindingTypeDialog(shell, isDTD12);
        }

      } else {

        dialog = null;

      }
    } else {

      dialog =
        new ChooseBindingTypeDialog(shell, precomputedAliasInfo, existingBindingParms, isDTD12);
    }

    return dialog;

  }

  public class ToolTipProvider
    extends BindingEditorLabelProvider
    implements IToolTipProvider, IToolTipHelpProvider {

    private HashMap toolTipInfo = new HashMap();

    //---------- IToolTipProvider ----------------------------//

    public String getToolTipText(Object object) {
      TapestryComponentModel component = null;
      String result = null;
      toolTipInfo.clear();

      PluginBindingSpecification spec = (PluginBindingSpecification) object;
      PluginContainedComponent containedComponent = (PluginContainedComponent) selectedComponent;
      String parameter = spec.getIdentifier();
      // empty means no alias!
      if (precomputedAliasInfo.isEmpty()) {
        // the type is not an alias
        String type = containedComponent.getType();
        if (type == null || type.equals("")) {
          return "No Type found for contained component: " + parameter;
        }
        StringBuffer buffer = new StringBuffer();
        component = ModelUtils.findComponent(type, getModel());
        if (component == null) {
          buffer.append("Component: " + type + " not found.");
          result = buffer.toString();
        } else if (!component.isLoaded()) {
          try {
            component.load();
          } catch (Exception e) {
            buffer.append("Could not load component: " + type);
            result = buffer.toString();
          }
        }
        PluginComponentSpecification componentSpec = component.getComponentSpecification();
        PluginParameterSpecification parameterSpec =
          (PluginParameterSpecification) componentSpec.getParameter(parameter);
        if (parameterSpec == null) {
          buffer.append("parameter '" + parameter + "' not found");
          if (componentSpec.getAllowInformalParameters()) {
            buffer.append(" but informals are allowed");
            result = buffer.toString();
          } else {
            buffer.append(" WARNING informals are not allowed!");
            result = buffer.toString();
          }
        }
        parameterSpec.getHelpText(parameter, buffer);
        result = buffer.toString();

      } else {
        // the type IS an alias
        computeToolTipInfo(parameter, precomputedAliasInfo);
        String alias = containedComponent.getType();
        Set keys = toolTipInfo.keySet();
        TapestryApplicationModel firstModel = null;
        TapestryApplicationModel defaultModel = TapestryPlugin.selectedApplication;
        if (defaultModel != null && keys.contains(defaultModel)) {
          firstModel = defaultModel;
        }
        Object[] keyArray = toolTipInfo.keySet().toArray();
        if (keyArray.length == 0) {
          result =
            "Couldn't find any component aliased to '"
              + alias
              + "' and having parameter '"
              + parameter
              + "'";
        } else {
          StringBuffer buffer = new StringBuffer();
          if (firstModel == null) {
            firstModel = ((TapestryApplicationModel) keyArray[0]);
          }
          TapestryComponentModel cmodel = (TapestryComponentModel) toolTipInfo.get(firstModel);
          PluginComponentSpecification firstComponent =
            (PluginComponentSpecification) cmodel.getComponentSpecification();
          buffer.append(
            "Found alias '"
              + alias
              + "' in application '"
              + firstModel.getUnderlyingStorage().getFullPath()
              + "\n");
          buffer.append(
            alias + " maps to " + firstModel.getSpecification().getComponentSpecificationPath(alias) + "\n");
          if (keyArray.length > 1) {
            buffer.append(
              "press F1 to check "
                + (keyArray.length - 1)
                + " other application(s) that have alias '"
                + alias
                + "'.\n");
          }
          ((PluginParameterSpecification) firstComponent.getParameter(parameter)).getHelpText(
            parameter,
            buffer);
          result = buffer.toString();
        }
      }
      return result;
    }

    private void computeToolTipInfo(String parameter, HashMap precomputed) {
      Iterator iter = precomputed.keySet().iterator();
      while (iter.hasNext()) {
        Object applicationModel = iter.next();
        TapestryComponentModel cmodel = (TapestryComponentModel) precomputed.get(applicationModel);
        PluginComponentSpecification component =
          (PluginComponentSpecification) cmodel.getComponentSpecification();
        if (component.getParameter(parameter) != null) {
          toolTipInfo.put(applicationModel, cmodel);
        }
      }
    }

    public Image getToolTipImage(Object object) {
      return getImage(object);
    }

    //---------- IToolTipHelpTextProvider --------------------//

    public Object getHelp(Object obj) {
      String parameter = ((PluginBindingSpecification) obj).getIdentifier();
      if (precomputedAliasInfo.isEmpty()) {
        return null;
      }
      // its not a possible alias now...
      return new ComponentAliasParameterViewer(parameter, precomputedAliasInfo);
    }

  }

}
