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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.ui.editor.PropertiesAction;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.spec.PluginParameterSpecification;
import com.iw.plugins.spindle.ui.ComponentAliasParameterViewer;
import com.iw.plugins.spindle.ui.EmptySelection;
import com.iw.plugins.spindle.ui.FieldPropertyDescriptor;
import com.iw.plugins.spindle.ui.IToolTipHelpProvider;
import com.iw.plugins.spindle.ui.IToolTipProvider;
import com.iw.plugins.spindle.ui.TreeViewerWithToolTips;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;
import net.sf.tapestry.spec.BindingType;

public class ComponentBindingsEditorSection extends AbstractPropertySheetEditorSection {

  private List bindingHolders = new ArrayList();

  private DeleteBindingAction deleteAction = new DeleteBindingAction();
  private NewBindingAction newBindingAction = new NewBindingAction();
  private NewInheritedBindingAction newInheritedAction = new NewInheritedBindingAction();
  private NewFieldBindingAction newFieldAction = new NewFieldBindingAction();
  private NewStaticBindingAction newStaticAction = new NewStaticBindingAction();
  private NewStringBindingAction newStringAction = new NewStringBindingAction();

  private NewBindingButtonAction newBindingButtonAction = new NewBindingButtonAction();

  private PluginContainedComponent selectedComponent;

  private BindingEditorLabelProvider labelProvider = new BindingEditorLabelProvider();

  private HashMap precomputedAliasInfo = new HashMap();

  /**
   * Constructor for ParameterEditorSection
   */
  public ComponentBindingsEditorSection(SpindleFormPage page) {
    super(page);
    setContentProvider(new BindingEditorContentProvider());
    setLabelProvider(labelProvider);
    setNewAction(newBindingButtonAction);
    setDeleteAction(deleteAction);
    setHeaderText("Bindings");
    setDescription("This section allows one to edit selected component's bindings");
  }

  public void setPrecomputedAliasInfo(HashMap map) {
    precomputedAliasInfo = map;
  }

  public void initialize(Object object) {
    super.initialize(object);
    BaseTapestryModel model = (BaseTapestryModel) object;
    if (!model.isEditable()) {
      newInheritedAction.setEnabled(false);
      newBindingAction.setEnabled(false);
      newFieldAction.setEnabled(false);
      newStaticAction.setEnabled(false);
      newStringAction.setEnabled(false);
    }
    TreeViewerWithToolTips viewer = (TreeViewerWithToolTips) getViewer();
    viewer.setToolTipProvider(labelProvider);
    viewer.setToolTipHelpProvider(labelProvider);
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    // this can only come from the ComponentSelectionSection and it can only be
    // that a new PluginContainedComponent was selected!
    selectedComponent = (PluginContainedComponent) changeObject;
    if (selectedComponent != null) {
      String copyOf = selectedComponent.getCopyOf();
      if (copyOf != null && !"".equals(copyOf.trim())) {
        selectedComponent = null;
      }
    }
    updateNeeded = true;
    update();
    layout();
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("bindings")) {
        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    bindingHolders = Collections.EMPTY_LIST;
    PluginContainedComponent spec = selectedComponent;
    if (spec == null) {
      setInput(bindingHolders);
      fireSelectionNotification(EmptySelection.Instance);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }
    Iterator iter = spec.getBindingNames().iterator();
    bindingHolders = new ArrayList();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      BindingHolder holder = new BindingHolder(name, (PluginBindingSpecification) spec.getBinding(name));
      bindingHolders.add(holder);
    }
    setInput(bindingHolders);
    //selectFirst();
  }

  protected void fillContextMenu(IMenuManager manager) {
    ISelection selection = getSelection();
    final Object object = ((IStructuredSelection) selection).getFirstElement();
    MenuManager submenu = new MenuManager("New");
    submenu.add(newInheritedAction);
    submenu.add(newBindingAction);
    submenu.add(newFieldAction);
    submenu.add(newStaticAction);
    submenu.add(newStringAction);
    manager.add(submenu);
    if (object != null) {
      manager.add(new Separator());
      manager.add(deleteAction);

    }
    manager.add(new Separator());
    PropertiesAction pAction = new PropertiesAction(getFormPage().getEditor());
    pAction.setText("Edit");
    pAction.setToolTipText("Edit the selected");
    pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  protected String findPrevious(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((BindingHolder) items[i]).name.equals(name) && i >= 1) {
          return ((BindingHolder) items[i - 1]).name;
        }
      }
    }
    return null;
  }

  protected boolean alreadyHasBinding(String name) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        BindingHolder holder = (BindingHolder) items[i];
        if (holder.name.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  public void setSelection(String name) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        BindingHolder holder = (BindingHolder) items[i];
        if (holder.name.equals(name)) {
          ArrayList list = new ArrayList();
          list.add(items[i]);
          setSelection(new JavaListSelectionProvider(list));
          break;
        }
      }
    }
  }

  protected class BindingHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    public String name;
    public PluginBindingSpecification spec;

    private IPropertyDescriptor[] bindingDescriptors =
      { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("value", "Property Path")};

    private IPropertyDescriptor[] staticDescriptors =
      { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("value", "Value")};

    private IPropertyDescriptor[] inheritDescriptors =
      { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("value", "Parameter Name")};

    private IPropertyDescriptor[] fieldDescriptors =
      { new FieldPropertyDescriptor("value", "Field Name", getModel())};
      
    private IPropertyDescriptor[] stringDescriptiors =
      { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("value", "Key")};
    
  

  /**
   * Constructor for PropertyHolder
   */
  public BindingHolder(String name, PluginBindingSpecification spec) {
    super();
    this.name = name;
    this.spec = spec;
  }

  public void resetPropertyValue(Object key) {
    if ("name".equals(key)) {
      name = null;
    } else if ("path".equals(key)) {
      spec.setValue("");
    }
  }

  public IPropertySource getPropertySource(Object key) {
    return this;
  }

  public void setPropertyValue(Object key, Object value) {
    if (!isModelEditable()) {
      updateNeeded = true;
      update();
      setSelection(this.name);
      return;
    }
    IModel model = (IModel) getFormPage().getModel();
    PluginContainedComponent componentSpec = selectedComponent;
    if ("name".equals(key)) {
      String oldName = this.name;
      String newName = (String) value;
      if ("".equals(newName.trim())) {
        newName = oldName;
      } else if (alreadyHasBinding(newName)) {
        newName = "Copy of " + newName;
      }
      this.name = newName;
      componentSpec.removeBinding(oldName);
      componentSpec.setBinding(this.name, spec);
      forceDirty();
      update();
      setSelection(this.name);
    } else if ("value".equals(key)) {
      spec.setValue((String) value);
      componentSpec.setBinding(this.name, spec);
      forceDirty();

      update();
      setSelection(this.name);
    }
  }

  public boolean isPropertySet(Object key) {
    if ("name".equals(key)) {
      return name != null;
    } else if ("value".equals(key)) {
      return spec.getValue() != null;
    } else {
      return true;
    }
  }

  public Object getPropertyValue(Object key) {
    if ("name".equals(key)) {
      return name;
    } else if ("value".equals(key)) {
      return spec.getValue();
    }
    return "ignore this";
  }

  public IPropertyDescriptor[] getPropertyDescriptors() {
    BindingType type = spec.getType();
    if (type == BindingType.INHERITED) {
      return inheritDescriptors;
    }
    if (type == BindingType.STATIC) {
      return staticDescriptors;
    }
    if (type == BindingType.DYNAMIC) {
      return bindingDescriptors;
    }
    if (type == BindingType.FIELD) {
      return fieldDescriptors;
    }
    if (type == BindingType.STRING) {
    	return stringDescriptiors;
    }

    return null;
  }

  public Object getEditableValue() {
    return name;
  }

  public Object getAdapter(Class clazz) {
    if (clazz == IPropertySource.class) {
      return (IPropertySource) this;
    }
    return null;
  }

}

public class BindingEditorLabelProvider
  extends LabelProvider
  implements IToolTipProvider, IToolTipHelpProvider, ITableLabelProvider {

  private Image dynamicBindingImage = TapestryImages.getSharedImage("bind-dynamic.gif");
  private Image fieldBindingImage = TapestryImages.getSharedImage("bind-field.gif");
  private Image inheritedBindingImage = TapestryImages.getSharedImage("bind-inhert.gif");
  private Image staticBindingImage = TapestryImages.getSharedImage("bind-static.gif");
    private Image stringBindingImage = TapestryImages.getSharedImage("bind-string.gif");

  private HashMap toolTipInfo = new HashMap();

  //---------- IToolTipProvider ----------------------------//

  public String getToolTipText(Object object) {
    toolTipInfo.clear();
    String parameter = ((BindingHolder) object).name;
    // empty means no alias!
    if (precomputedAliasInfo.isEmpty()) {
      // the type is not an alias
      String type = selectedComponent.getType();
      if (type == null || type.equals("")) {
        return "No Type found for contained component: " + parameter;
      }
      StringBuffer buffer = new StringBuffer();
      TapestryComponentModel component = TapestryPlugin.getTapestryModelManager().findComponent(type, getModel());
      if (component == null) {
        buffer.append("Component: " + type + " not found.");
        return buffer.toString();
      } else if (!component.isLoaded()) {
        try {
          component.load();
        } catch (Exception e) {
          buffer.append("Could not load component: " + type);
          return buffer.toString();
        }
      }
      PluginComponentSpecification componentSpec = component.getComponentSpecification();
      PluginParameterSpecification parameterSpec =
        (PluginParameterSpecification) componentSpec.getParameter(parameter);
      if (parameterSpec == null) {
        buffer.append("parameter '" + parameter + "' not found");
        if (componentSpec.getAllowInformalParameters()) {
          buffer.append(" but informals are allowed");
          return buffer.toString();
        } else {
          buffer.append(" WARNING informals are not allowed!");
          return buffer.toString();
        }
      }
      parameterSpec.getHelpText(parameter, buffer);
      return buffer.toString();

    } else {
      // the type IS an alias
      computeToolTipInfo(parameter, precomputedAliasInfo);
      String alias = selectedComponent.getType();
      Set keys = toolTipInfo.keySet();
      TapestryApplicationModel firstModel = null;
      TapestryApplicationModel defaultModel = TapestryPlugin.selectedApplication;
      if (defaultModel != null && keys.contains(defaultModel)) {
        firstModel = defaultModel;
      }
      Object[] keyArray = toolTipInfo.keySet().toArray();
      if (keyArray.length == 0) {
        return "Couldn't find any component aliased to '" + alias + "' and having parameter '" + parameter + "'";
      } else {
        StringBuffer buffer = new StringBuffer();
        if (firstModel == null) {
          firstModel = ((TapestryApplicationModel) keyArray[0]);
        }
        TapestryComponentModel cmodel = (TapestryComponentModel) toolTipInfo.get(firstModel);
        PluginComponentSpecification firstComponent =
          (PluginComponentSpecification) cmodel.getComponentSpecification();
        buffer.append(
          "Found alias '" + alias + "' in application '" + firstModel.getUnderlyingStorage().getFullPath() + "\n");
        buffer.append(alias + " maps to " + firstModel.getApplicationSpec().getComponentAlias(alias) + "\n");
        if (keyArray.length > 1) {
          buffer.append(
            "press F1 to check " + (keyArray.length - 1) + " other application(s) that have alias '" + alias + "'.\n");
        }
        ((PluginParameterSpecification) firstComponent.getParameter(parameter)).getHelpText(parameter, buffer);
        return buffer.toString();
      }
    }
  }

  private void computeToolTipInfo(String parameter, HashMap precomputed) {
    Iterator iter = precomputed.keySet().iterator();
    while (iter.hasNext()) {
      Object applicationModel = iter.next();
      TapestryComponentModel cmodel = (TapestryComponentModel) precomputed.get(applicationModel);
      PluginComponentSpecification component = (PluginComponentSpecification) cmodel.getComponentSpecification();
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
    String parameter = ((BindingHolder) obj).name;
    if (precomputedAliasInfo.isEmpty()) {
      return null;
    }
    // its not a possible alias now...
    return new ComponentAliasParameterViewer(parameter, precomputedAliasInfo);
  }

  //---------- ITableLabelProvider -------------------------//

  public String getText(Object object) {
    BindingHolder holder = (BindingHolder) object;
    PluginBindingSpecification spec = holder.spec;
    BindingType type = spec.getType();
    if (type == BindingType.DYNAMIC) {
      return holder.name + " property-path = " + spec.getValue();
    }
    if (type == BindingType.FIELD) {
      return holder.name + " field-name = " + spec.getValue();
    }
    if (type == BindingType.STATIC) {
      return holder.name + " value = " + spec.getValue();
    }
    if (type == BindingType.INHERITED) {
      return holder.name + " parameter-name = " + spec.getValue();
    }
    if (type == BindingType.STRING) {
      return holder.name + " key = " + spec.getValue();
    }

    return null;
  }

  public void dispose() {
    // shared image disposal handled by the plugin
  }

  public String getColumnText(Object object, int column) {
    if (column != 1) {
      return "";
    }
    return getText(object);
  }

  public Image getImage(Object object) {
    BindingHolder holder = (BindingHolder) object;
    PluginBindingSpecification spec = holder.spec;
    BindingType type = spec.getType();
    if (type == BindingType.DYNAMIC) {
      return dynamicBindingImage;
    }
    if (type == BindingType.INHERITED) {
      return inheritedBindingImage;
    }
    if (type == BindingType.FIELD) {
      return fieldBindingImage;
    }
    if (type == BindingType.STATIC) {
      return staticBindingImage;
    }
    if (type == BindingType.STRING) {
      return stringBindingImage;
    }
    return null;
  }

  public Image getColumnImage(Object object, int column) {
    if (column != 1) {
      return null;
    }
    return getImage(object);
  }
}

class BindingEditorContentProvider extends DefaultContentProvider implements ITreeContentProvider {
  public Object[] getElements(Object object) {
    return bindingHolders.toArray();
  }
  public Object[] getChildren(Object parent) {
    return new Object[0];
  }
  public Object getParent(Object child) {
    return null;
  }
  public boolean hasChildren(Object parent) {
    return false;
  }
}

class DeleteBindingAction extends Action {

  protected DeleteBindingAction() {
    super();
    setText("Delete");
    setToolTipText("Delete the selected");
  }

  /**
  * @see Action#run()
  */
  public void run() {
    updateSelection = true;
    BindingHolder holder = (BindingHolder) getSelected();
    if (holder != null) {
      PluginContainedComponent spec = selectedComponent;
      String prev = findPrevious(holder.name);
      spec.removeBinding(holder.name);
      forceDirty();
      updateNeeded = true;
      update();
      if (prev != null) {
        setSelection(prev);
      } else {
        selectFirst();
      }
    }
    updateSelection = false;
  }

}

protected class NewBindingButtonAction extends Action {

  protected NewBindingButtonAction() {
    super();
  }

  public void run() {
  	if (selectedComponent == null) {
  		return;
  	}
    ChooseBindingTypeDialog dialog = null;
    Shell shell = newButton.getShell();
    Set existingBindingParms = getExistingBindingParameters();
    if (precomputedAliasInfo.isEmpty()) {
      String selectedType = selectedComponent.getType();
      if (selectedComponent != null) {
        TapestryComponentModel cmodel =
          TapestryPlugin.getTapestryModelManager().findComponent(selectedType, getModel());
        if (cmodel != null) {
          dialog = new ChooseBindingTypeDialog(shell, cmodel, existingBindingParms);
        } else {
          dialog = new ChooseBindingTypeDialog(shell);
        }
      } else {
        return;
      }
    } else {
      dialog = new ChooseBindingTypeDialog(shell, precomputedAliasInfo, existingBindingParms);
    }
    dialog.create();
    if (dialog.open() == dialog.OK) {
      BindingType chosen = dialog.getSelectedBindingType();
      List chosenParamterNames = dialog.getParameterNames();
      if (chosenParamterNames.isEmpty()) {
        createBinding(chosen);
      } else {
        Iterator newNames = chosenParamterNames.iterator();
        while (newNames.hasNext()) {
          createBinding(chosen, (String) newNames.next());
        }
      }
    }
  }

  private Set getExistingBindingParameters() {
    HashSet result = new HashSet();
    Iterator existing = bindingHolders.iterator();
    while (existing.hasNext()) {
      result.add(((BindingHolder) existing.next()).name);
    }
    return result;
  }

  private void createBinding(BindingType type) {
    if (type == BindingType.DYNAMIC) {
      newBindingAction.run();
    } else if (type == BindingType.FIELD) {
      newFieldAction.run();
    } else if (type == BindingType.INHERITED) {
      newInheritedAction.run();
    } else if (type == BindingType.STATIC) {
      newStaticAction.run();
    } else if (type == BindingType.STRING) {
      newStringAction.run();
    }
  }
  private void createBinding(BindingType type, String parameterName) {
    if (type == BindingType.DYNAMIC) {
      newBindingAction.run(parameterName);
    } else if (type == BindingType.FIELD) {
      newFieldAction.run(parameterName);
    } else if (type == BindingType.INHERITED) {
      newInheritedAction.run(parameterName);
    } else if (type == BindingType.STATIC) {
      newStaticAction.run(parameterName);
    } else if (type == BindingType.STRING) {
      newStringAction.run(parameterName);
    }
  }
}

protected abstract class BaseNewBindingAction extends Action {

  protected String defaultBindingName = "binding";
  protected BaseNewBindingAction() {
    super();
  } /**
                 * @see Action#run()
                 */

  public void run() {
    run(defaultBindingName);
  }

  public void run(String parameterName) {
    String useName = parameterName;
    PluginContainedComponent spec = selectedComponent;
    if (spec.getBinding(useName) != null) {
      int counter = 1;
      while (spec.getBinding(useName + counter) != null) {
        counter++;
      }
      useName = useName + counter;
    }
    PluginBindingSpecification newSpec = new PluginBindingSpecification(getType(), "fill in value");
    spec.setBinding(useName, newSpec);
    updateSelection = true;
    updateNeeded = true;
    forceDirty();
    update();
    setSelection(useName);
    updateSelection = false;
  }

  protected abstract BindingType getType();
}

class NewStringBindingAction
  extends BaseNewBindingAction { /**
     * Constructor for NewPropertyAction
     */
  protected NewStringBindingAction() {
    super();
    setText("String Binding");
    defaultBindingName = "string";
    setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-string.gif")));

  }

  public BindingType getType() {
    return BindingType.STRING;
  }

}

class NewFieldBindingAction
  extends BaseNewBindingAction { /**
     * Constructor for NewPropertyAction
     */
  protected NewFieldBindingAction() {
    super();
    setText("Field Binding");
    defaultBindingName = "field";
    setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-field.gif")));
  }

  public BindingType getType() {
    return BindingType.FIELD;
  }

}

class NewStaticBindingAction
  extends BaseNewBindingAction { /**
     * Constructor for NewPropertyAction
     */
  protected NewStaticBindingAction() {
    super();
    setText("Static Binding");
    defaultBindingName = "static";
    setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-static.gif")));

  }

  public BindingType getType() {
    return BindingType.STATIC;
  }

}

class NewBindingAction extends BaseNewBindingAction {

  protected NewBindingAction() {
    super();
    setText("Dynamic Binding");
    defaultBindingName = "dynamic";
    setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-dynamic.gif")));

  }

  public BindingType getType() {
    return BindingType.DYNAMIC;
  }

}

class NewInheritedBindingAction extends BaseNewBindingAction {

  protected NewInheritedBindingAction() {
    super();
    setText("Inherted Binding");
    defaultBindingName = "inherited";
    setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-inhert.gif")));

  }

  public BindingType getType() {
    return BindingType.INHERITED;
  }

}

}
