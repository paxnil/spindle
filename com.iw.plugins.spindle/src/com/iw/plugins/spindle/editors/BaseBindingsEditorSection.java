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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.tapestry.spec.BindingType;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.ui.editor.PropertiesAction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editorjwc.components.ChooseBindingTypeDialog;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.spec.IBindingHolder;
import com.iw.plugins.spindle.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.EmptySelection;

public class BaseBindingsEditorSection extends AbstractPropertySheetEditorSection {

  private NewBindingAction newBindingAction = new NewBindingAction();
  private NewInheritedBindingAction newInheritedAction = new NewInheritedBindingAction();
  private NewFieldBindingAction newFieldAction = new NewFieldBindingAction();
  private NewStaticBindingAction newStaticAction = new NewStaticBindingAction();
  private NewStringBindingAction newStringAction = new NewStringBindingAction();

  protected NewBindingButtonAction newBindingButtonAction = new NewBindingButtonAction();
  protected DeleteBindingAction deleteAction = new DeleteBindingAction();

  protected IBindingHolder selectedComponent;

  private BindingEditorLabelProvider labelProvider = new BindingEditorLabelProvider();

  /**
   * Constructor for ParameterEditorSection
   */
  public BaseBindingsEditorSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(labelProvider);
    setNewAction(newBindingButtonAction);
    setDeleteAction(deleteAction);
    setHeaderText("Bindings");
    setDescription("This section allows one to edit bindings");
    setUseToolTips(false);
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
    if (DTDVersion < XMLUtil.DTD_1_2) {

      newStringAction.setEnabled(false);
    }
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    // this can only come from the ComponentSelectionSection and it can only be
    // that a new PluginContainedComponent was selected!
    selectedComponent = (IBindingHolder) changeObject;
    updateNeeded = true;
    update();
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
    holderArray = Collections.EMPTY_LIST;
    IBindingHolder spec = selectedComponent;
    if (spec == null) {
      setInput(holderArray);
      fireSelectionNotification(EmptySelection.Instance);
      clearPageSelection();
      return;
    }
    Iterator iter = spec.getBindingNames().iterator();
    holderArray = new ArrayList();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      holderArray.add(spec.getBinding(name));
    }
    setInput(holderArray);
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

  protected ChooseBindingTypeDialog getDialog() {

    return new ChooseBindingTypeDialog(TapestryPlugin.getDefault().getActiveWorkbenchShell(), DTDVersion >= XMLUtil.DTD_1_2);

  }

  protected Set getExistingBindingParameters() {
    HashSet result = new HashSet();
    Iterator existing = holderArray.iterator();

    while (existing.hasNext()) {

      PluginBindingSpecification spec = (PluginBindingSpecification) existing.next();
      result.add(spec.getIdentifier());
    }

    return result;
  }

  public class BindingEditorLabelProvider extends AbstractIdentifiableLabelProvider {

    private Image dynamicBindingImage = TapestryImages.getSharedImage("bind-dynamic.gif");
    private Image fieldBindingImage = TapestryImages.getSharedImage("bind-field.gif");
    private Image inheritedBindingImage = TapestryImages.getSharedImage("bind-inhert.gif");
    private Image staticBindingImage = TapestryImages.getSharedImage("bind-static.gif");
    private Image stringBindingImage = TapestryImages.getSharedImage("bind-string.gif");

    //---------- ITableLabelProvider -------------------------//

    public String getText(Object object) {

      PluginBindingSpecification spec = (PluginBindingSpecification) object;
      BindingType type = spec.getType();
      String identifier = spec.getIdentifier();

      if (type == BindingType.DYNAMIC) {

        int DTDVersion = XMLUtil.getDTDVersion(getModel().getPublicId());

        if (DTDVersion >= XMLUtil.DTD_1_3) {

          return identifier + " expression = " + spec.getValue();

        }
        return identifier + " property-path = " + spec.getValue();
      }

      if (type == BindingType.FIELD) {
        return identifier + " field-name = " + spec.getValue();
      }

      if (type == BindingType.STATIC) {
        return identifier + " value = " + spec.getValue();
      }

      if (type == BindingType.INHERITED) {
        return identifier + " parameter-name = " + spec.getValue();
      }

      if (type == BindingType.STRING) {
        return identifier + " key = " + spec.getValue();
      }

      return null;
    }

    public Image getImage(Object object) {

      PluginBindingSpecification spec = (PluginBindingSpecification) object;
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
      if (DTDVersion >= XMLUtil.DTD_1_2 && type == BindingType.STRING) {
        return stringBindingImage;
      }
      return null;
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
      PluginBindingSpecification spec = (PluginBindingSpecification) getSelected();

      if (spec != null) {

        String identifier = spec.getIdentifier();
        String prev = findPrevious(identifier);

        ((IBindingHolder) spec.getParent()).removeBinding(identifier);

        spec.setParent(null);

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

    /**
     * Constructor for NewBindingButtonAction.
     */
    public NewBindingButtonAction() {
      super();
      setText("New");
    }

    /**
     * Constructor for NewBindingButtonAction.
     * @param text
     * @param image
     */
    public NewBindingButtonAction(String text, ImageDescriptor image) {
      super(text, image);
    }

    public void run() {

      if (selectedComponent == null) {
        return;
      }

      ChooseBindingTypeDialog dialog = getDialog();

      if (dialog == null) {
        return;
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

    private void createBinding(BindingType type) {
      if (type == BindingType.DYNAMIC) {
        newBindingAction.run();
      } else if (type == BindingType.FIELD) {
        newFieldAction.run();
      } else if (type == BindingType.INHERITED) {
        newInheritedAction.run();
      } else if (type == BindingType.STATIC) {
        newStaticAction.run();
      } else if (DTDVersion >= XMLUtil.DTD_1_2 && type == BindingType.STRING) {
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
      } else if (DTDVersion >= XMLUtil.DTD_1_2 && type == BindingType.STRING) {
        newStringAction.run(parameterName);
      }
    }
  }

  protected abstract class BaseNewBindingAction extends Action {

    protected String defaultBindingName = "binding";
    protected BaseNewBindingAction() {
      super();
    }

    public void run() {
      run(defaultBindingName);
    }

    public void run(String parameterName) {
      String useName = parameterName;
      IBindingHolder spec = (IBindingHolder) selectedComponent;
      if (spec.getBinding(useName) != null) {
        int counter = 1;
        while (spec.getBinding(useName + counter) != null) {
          counter++;
        }
        useName = useName + counter;
      }
      PluginBindingSpecification newSpec =
        new PluginBindingSpecification(getType(), "fill in value");
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

  class NewStringBindingAction extends BaseNewBindingAction {

    protected NewStringBindingAction() {
      super();
      setText("String Binding");
      defaultBindingName = "string";
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-string.gif")));

    }

    public BindingType getType() {
      return BindingType.STRING;
    }

  }

  class NewFieldBindingAction extends BaseNewBindingAction {

    protected NewFieldBindingAction() {
      super();
      setText("Field Binding");
      defaultBindingName = "field";
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-field.gif")));
    }

    public BindingType getType() {
      return BindingType.FIELD;
    }

  }

  class NewStaticBindingAction extends BaseNewBindingAction {

    protected NewStaticBindingAction() {
      super();
      setText("Static Binding");
      defaultBindingName = "static";
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-static.gif")));

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
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-dynamic.gif")));

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
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bind-inhert.gif")));

    }

    public BindingType getType() {
      return BindingType.INHERITED;
    }

  }

}
