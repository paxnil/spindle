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

package com.iw.plugins.spindle.editorlib.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.ui.ChooseWorkspaceModelDialog;
import com.iw.plugins.spindle.ui.descriptors.ComponentTypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.SpindleStatus;

public class ComponentAliasSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener, ISelectionChangedListener {

  private Action newAliasAction = new NewAliasAction();
  private Action deleteAliasAction = new DeleteAliasAction();

  /**
   * Constructor for ComponentAliasSection
   */
  public ComponentAliasSection(SpindleFormPage page) {
    super(page);
    setHeaderText(MessageUtil.getString("ComponentAliasSection.headerText"));
    setLabelProvider(new AliasLabelProvider());
    setNewAction(newAliasAction);
    setDeleteAction(deleteAliasAction);

  }

  protected void fillContextMenu(IMenuManager manager) {
    Object selected = getSelected();
    boolean isEditable = isModelEditable();
    if (isEditable) {
      manager.add(newAliasAction);
      if (selected != null) {
        manager.add(new Separator());
        manager.add(deleteAliasAction);
        manager.add(new Separator());
        pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
        manager.add(pAction);
      }
    }
  }

  public void selectionChanged(SelectionChangedEvent event) {
    AliasHolder holder = (AliasHolder) getSelected();
    if (holder == null) {

      fireSelectionNotification(null);
      deleteButton.setEnabled(false);
      editButton.setEnabled(false);

    } else {

      boolean isEditable = isModelEditable();
      fireSelectionNotification(holder.getIdentifier());

      if ((hasFocus || updateSelection) && isEditable) {

        setPageSelection();
      }
      newButton.setEnabled(isEditable);
      deleteButton.setEnabled(isEditable);
      editButton.setEnabled(isEditable);

    }
  }

  public void update(BaseTapestryModel model) {
    holderArray.removeAll(holderArray);

    IPluginLibrarySpecification librarySpec =
      (IPluginLibrarySpecification) ((TapestryLibraryModel) getModel()).getSpecification();

    List ids = librarySpec.getComponentAliases();

    if (ids.isEmpty()) {

      setInput(holderArray);
      fireSelectionNotification(null);
      clearPageSelection();
      return;
    }

    Iterator iter = ids.iterator();
    holderArray = new ArrayList();

    while (iter.hasNext()) {

      String name = (String) iter.next();
      AliasHolder holder = new AliasHolder(name);
      holder.setParent(librarySpec);
      holderArray.add(holder);
    }
    setInput(holderArray);
    selectFirst();
    updateNeeded = false;
  }

  public boolean isModelEditable() {
    return ((BaseTapestryModel) getModel()).isEditable();
  }

  public void modelChanged(IModelChangedEvent event) {
    int eventType = event.getChangeType();
    if (eventType == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
      return;
    }
    if (eventType == IModelChangedEvent.CHANGE) {
      updateNeeded = event.getChangedProperty().equals("componentMap");
    }
  }

  protected class AliasLabelProvider extends AbstractIdentifiableLabelProvider {

    Image componentImage;

    public AliasLabelProvider() {
      componentImage = TapestryImages.getSharedImage("component16.gif");
    }

    public Image getImage(Object element) {
      return componentImage;
    }

  }

  protected boolean checkComponentPathOk(String potentialValue) {

    SpindleStatus status = new SpindleStatus();

    if (potentialValue.startsWith("/net/sf/tapestry")) {

      status.setError(potentialValue + " is defined by default");

    }

    if (status.isError()) {

      ErrorDialog.openError(
        newButton.getShell(),
        "Spindle Error",
        "Could not add Component: " + potentialValue,
        status);

      return false;

    }

    return true;
  }

 

  protected class AliasHolder implements IIdentifiable, IPropertySource {

    private String identifier;
    private IPluginLibrarySpecification parent;

    /**
     * Constructor for PropertyHolder
     */
    public AliasHolder(String identifier) {
      super();
      this.identifier = identifier;
    }

    public void resetPropertyValue(Object key) {
    }

    public void setPropertyValue(Object key, Object value) {
      if ("name".equals(key)) {

        String oldName = this.identifier;
        String specPath = parent.getComponentSpecificationPath(oldName);
        String newName = (String) value;

        if ("".equals(newName.trim())) {

          newName = oldName;

        } else if (parent.getComponentSpecificationPath(newName) != null) {

          newName = "Copy of " + newName;
          parent.setComponentSpecificationPath(newName, specPath);
          return;
        }
        this.identifier = newName;
        parent.removeComponentSpecificationPath(oldName);
        parent.setComponentSpecificationPath(this.identifier, specPath);

      } else if ("spec".equals(key)) {
        parent.setComponentSpecificationPath(this.identifier, (String) value);
      }

    }

    public boolean isPropertySet(Object key) {

      if ("name".equals(key)) {

        return identifier != null;

      } else if ("spec".equals(key)) {

        return parent.getComponentSpecificationPath((String) key) != null;

      }

      return true;
    }

    public Object getPropertyValue(Object key) {

      if ("name".equals(key)) {

        return identifier;

      } else if ("spec".equals(key)) {

        return parent.getComponentSpecificationPath(this.identifier);

      }
      return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {

      return new IPropertyDescriptor[] {
        new TextPropertyDescriptor("name", "Name"),
        new ComponentTypeDialogPropertyDescriptor("spec", "Spec", null, null)};
    }

    public Object getEditableValue() {
      return identifier;
    }

    /**
     * Returns the identifier.
     * @return String
     */
    public String getIdentifier() {
      return identifier;
    }

    /**
     * Sets the identifier.
     * @param identifier The identifier to set
     */
    public void setIdentifier(String identifier) {
      this.identifier = identifier;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getParent()
     */
    public Object getParent() {
      return parent;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
     */
    public void setParent(Object parent) {
      this.parent = (IPluginLibrarySpecification) parent;
    }

  }

  protected class NewAliasAction extends Action {

    protected NewAliasAction() {
      super();
      setText("New");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      try {

        IPluginLibrarySpecification specification =
          (IPluginLibrarySpecification) ((TapestryLibraryModel) getModel()).getSpecification();

        ChooseWorkspaceModelDialog dialog =
          ChooseWorkspaceModelDialog.createComponentModelDialog(
            newButton.getShell(),
            TapestryPlugin.getDefault().getJavaProjectFor(getModel().getUnderlyingStorage()),
            "Choose Component",
            "Choose a Component to be aliased");

        dialog.create();

        if (dialog.open() == dialog.OK) {

          String componentName = dialog.getResultString();
          IPackageFragment componentPackage = dialog.getResultPackage();
          String path = dialog.getResultPath();

          if (checkComponentPathOk(path)) {

            updateSelection = true;

            int index = componentName.indexOf(".");

            componentName = componentName.substring(0, index);

            if (specification.getComponentSpecificationPath(componentName) != null) {

              int count = 1;
              while (specification.getComponentSpecificationPath(componentName + count) != null) {
                count++;
              }

              componentName += count;
            }
            specification.setComponentSpecificationPath(componentName, path);
            forceDirty();
            update();
            setSelection(componentName);
            updateSelection = false;

          }
        }
      } catch (CoreException e) {
      }
    }
  }

  protected class DeleteAliasAction extends Action {

    protected DeleteAliasAction() {
      super();
      setText("Delete");
      setDescription("delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      AliasHolder holder = (AliasHolder) getSelected();
      if (holder != null) {

        IPluginLibrarySpecification appSpec = (IPluginLibrarySpecification) holder.getParent();

        String prev = findPrevious(holder.getIdentifier());
        appSpec.removeComponentSpecificationPath(holder.getIdentifier());
        holder.setParent(null);
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

}