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
import java.util.Iterator;

import org.eclipse.core.internal.plugins.IModel;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.ui.editor.PropertiesAction;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginAssetSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;
import com.primix.tapestry.spec.AssetType;

public class AssetsEditorSection extends AbstractPropertySheetEditorSection {

  private ArrayList assetHolders = new ArrayList();

  private DeleteAssetAction deleteAction = new DeleteAssetAction();
  private NewContextAssetAction newContextAction = new NewContextAssetAction();
  private NewExternalAssetAction newExternalAction = new NewExternalAssetAction();
  private NewPrivateAssetAction newPrivateAction = new NewPrivateAssetAction();

  private NewButtonAction newButtonAction = new NewButtonAction();

  /**
   * Constructor for ParameterEditorSection
   */
  public AssetsEditorSection(SpindleFormPage page) {
    super(page);
    setContentProvider(new AssetContentProvider());
    setLabelProvider(new AssetLabelProvider());
    setNewAction(newButtonAction);
    setDeleteAction(deleteAction);
    setHeaderText("Assets");
    setDescription("This section allows one to edit the component's assets");
  }

  public void initialize(Object object) {
    super.initialize(object);
    BaseTapestryModel model = (BaseTapestryModel) object;
    if (!model.isEditable()) {
      newContextAction.setEnabled(false);
      newExternalAction.setEnabled(false);
      newPrivateAction.setEnabled(false);
    }
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("assets")) {
        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    PluginComponentSpecification spec = ((TapestryComponentModel) model).getComponentSpecification();
    Iterator iter = spec.getAssetNames().iterator();
    assetHolders.removeAll(assetHolders);
    while (iter.hasNext()) {
      String name = (String) iter.next();
      AssetHolder holder = new AssetHolder(name, (PluginAssetSpecification) spec.getAsset(name));
      assetHolders.add(holder);
    }
    setInput(assetHolders);
    //selectFirst();
  }

  protected void fillContextMenu(IMenuManager manager) {
    ISelection selection = getSelection();
    final Object object = ((IStructuredSelection) selection).getFirstElement();
    MenuManager submenu = new MenuManager("New");
    submenu.add(newContextAction);
    submenu.add(newExternalAction);
    submenu.add(newPrivateAction);
    manager.add(submenu);
    if (object != null) {
      manager.add(new Separator());
      manager.add(deleteAction);

    }
    manager.add(new Separator());
    PropertiesAction pAction = new PropertiesAction(getFormPage().getEditor());
    pAction.setText("Edit");
    pAction.setToolTipText("Edit the selected");
    pAction.setEnabled(((IEditable) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  protected String findPrevious(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((AssetHolder) items[i]).name.equals(name) && i >= 1) {
          return ((AssetHolder) items[i - 1]).name;
        }
      }
    }
    return null;
  }

  protected boolean alreadyHasAsset(String name) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        AssetHolder holder = (AssetHolder) items[i];
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
        AssetHolder holder = (AssetHolder) items[i];
        if (holder.name.equals(name)) {
          ArrayList list = new ArrayList();
          list.add(items[i]);
          setSelection(new JavaListSelectionProvider(list));
          break;
        }
      }
    }
  }

  protected class AssetHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    public String name;
    public PluginAssetSpecification spec;

    private IPropertyDescriptor[] privateDescriptors =
      {
        new TextPropertyDescriptor("name", "Name"),
        new TextPropertyDescriptor("path", "Resource Path"),
        };
    private IPropertyDescriptor[] externalDescriptors =
      { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("path", "URL"), };
    private IPropertyDescriptor[] contextDescriptors =
      { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("path", "Path"), };

    /**
     * Constructor for PropertyHolder
     */
    public AssetHolder(String name, PluginAssetSpecification spec) {
      super();
      this.name = name;
      this.spec = spec;
    }

    public void resetPropertyValue(Object key) {
      if ("name".equals(key)) {
        name = null;
      } else if ("path".equals(key)) {
        spec.setPath("");
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
      PluginComponentSpecification componentSpec =
        ((TapestryComponentModel) model).getComponentSpecification();
      if ("name".equals(key)) {
        String oldName = this.name;
        String newName = (String) value;
        if ("".equals(newName.trim())) {
          newName = oldName;
        } else if (alreadyHasAsset(newName)) {
          newName = "Copy of " + newName;
        }
        this.name = newName;
        componentSpec.removeAsset(oldName);
        componentSpec.setAsset(this.name, spec);
        forceDirty();
        update();
        setSelection(this.name);
      } else if ("path".equals(key)) {
        spec.setPath((String) value);
        componentSpec.setAsset(this.name, spec);
        forceDirty();

        update();
        setSelection(this.name);
      }
    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
        return name != null;
      } else if ("path".equals(key)) {
        return spec.getPath() != null;
      } else {
        return true;
      }
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return name;
      } else if ("path".equals(key)) {
        return spec.getPath();
      }
      return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
      AssetType type = spec.getType();
      if (type == AssetType.CONTEXT) {
        return contextDescriptors;
      }
      if (type == AssetType.EXTERNAL) {
        return externalDescriptors;
      }
      if (type == AssetType.PRIVATE) {
        return privateDescriptors;
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

  public class AssetLabelProvider extends LabelProvider implements ITableLabelProvider {

    private Image privateAssetImage = TapestryImages.getSharedImage("missing");
    private Image contextAssetImage = TapestryImages.getSharedImage("missing");
    private Image externalAssetImage = TapestryImages.getSharedImage("missing");

    public String getText(Object object) {
      AssetHolder holder = (AssetHolder) object;
      PluginAssetSpecification spec = holder.spec;
      AssetType type = spec.getType();
      if (type == AssetType.PRIVATE) {
        return holder.name + " resource-path = " + spec.getPath();
      }
      if (type == AssetType.EXTERNAL) {
        return holder.name + " URL = " + spec.getPath();
      }
      if (type == AssetType.CONTEXT) {
        return holder.name + " path = " + spec.getPath();
      }
      return null;
    }

    public void dispose() {
      //shared images are disposed by the Plugin
    }

    public String getColumnText(Object object, int column) {
      if (column != 1) {
        return "";
      }
      return getText(object);
    }

    public Image getImage(Object object) {
      AssetHolder holder = (AssetHolder) object;
      PluginAssetSpecification spec = holder.spec;
      AssetType type = spec.getType();
      if (type == AssetType.PRIVATE) {
        return privateAssetImage;
      }
      if (type == AssetType.EXTERNAL) {
        return externalAssetImage;
      }
      if (type == AssetType.CONTEXT) {
        return contextAssetImage;
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

  class AssetContentProvider extends DefaultContentProvider implements ITreeContentProvider {
    public Object[] getElements(Object object) {
      return assetHolders.toArray();
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

  class DeleteAssetAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected DeleteAssetAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      AssetHolder holder = (AssetHolder) getSelected();
      if (holder != null) {
        PluginComponentSpecification spec =
          ((TapestryComponentModel) getFormPage().getModel()).getComponentSpecification();
        String prev = findPrevious(holder.name);
        spec.removeAsset(holder.name);
        forceDirty();

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

  protected class NewButtonAction extends Action {

    protected NewButtonAction() {
      super();
    }

    public void run() {
      ChooseAssetTypeDialog dialog = new ChooseAssetTypeDialog(newButton.getShell());
      dialog.create();
      if (dialog.open() == dialog.OK) {
        AssetType chosen = dialog.getSelectedAssetType();
        if (chosen == AssetType.CONTEXT) {
          newContextAction.run();
        } else if (chosen == AssetType.EXTERNAL) {
          newExternalAction.run();
        } else if (chosen == AssetType.PRIVATE) {
          newPrivateAction.run();
        }
      }
    }
  }

  protected abstract class BaseNewAssetAction extends Action {

    protected BaseNewAssetAction() {
      super();
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginComponentSpecification spec =
        ((TapestryComponentModel) getFormPage().getModel()).getComponentSpecification();
      String useName = "asset";
      if (spec.getAsset(useName) != null) {
        int counter = 1;
        while (spec.getAsset(useName + counter) != null) {
          counter++;
        }
        useName = useName + counter;
      }
      PluginAssetSpecification newSpec = new PluginAssetSpecification(getType(), "fill in path");
      spec.setAsset(useName, newSpec);
      forceDirty();
      update();
      setSelection(useName);
      updateSelection = false;
    }

    protected abstract AssetType getType();

  }

  class NewPrivateAssetAction extends BaseNewAssetAction {
    /**
     * Constructor for NewPropertyAction
     */
    protected NewPrivateAssetAction() {
      super();
      setText("Private Asset");
      setToolTipText("Create a new private asset");
    }

    public AssetType getType() {
      return AssetType.PRIVATE;
    }

  }

  class NewExternalAssetAction extends BaseNewAssetAction {

    protected NewExternalAssetAction() {
      super();
      setText("External Asset");
      setToolTipText("Create a new external asset");
    }

    public AssetType getType() {
      return AssetType.EXTERNAL;
    }

  }

  class NewContextAssetAction extends BaseNewAssetAction {

    protected NewContextAssetAction() {
      super();
      setText("Context Asset");
      setToolTipText("Create a new context asset");
    }

    public AssetType getType() {
      return AssetType.CONTEXT;
    }

  }

}