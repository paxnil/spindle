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

package com.iw.plugins.spindle.editorapp.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
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
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginPageSpecification;
import com.iw.plugins.spindle.ui.EmptySelection;
import com.iw.plugins.spindle.ui.descriptors.ComponentTypeDialogPropertyDescriptor;

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
    PluginPageSpecification pageSpec = (PluginPageSpecification) getSelected();
    boolean isEditable = isModelEditable();
    if (isEditable) {
      manager.add(newAliasAction);
      if (pageSpec != null) {
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

        getFormPage().setSelection(event.getSelection());
      }
      newButton.setEnabled(isEditable);
      deleteButton.setEnabled(isEditable);
      editButton.setEnabled(isEditable);

    }
  }

  public void update(BaseTapestryModel model) {
    holderArray.removeAll(holderArray);

    PluginApplicationSpecification appSpec =
      (PluginApplicationSpecification) ((TapestryApplicationModel) getModel()).getApplicationSpec();

    Set ids = new TreeSet(appSpec.getComponentMapAliases());

    if (ids.isEmpty()) {

      setInput(holderArray);
      fireSelectionNotification(null);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }

    Iterator iter = ids.iterator();
    holderArray = new ArrayList();

    while (iter.hasNext()) {

      String name = (String) iter.next();
      AliasHolder holder = new AliasHolder(name);
      holder.setParent(appSpec);
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

  protected class AliasHolder implements IIdentifiable, IPropertySource {

    private String identifier;
    private PluginApplicationSpecification parent;

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
        String specPath = parent.getComponentAlias(oldName);
        String newName = (String) value;

        if ("".equals(newName.trim())) {

          newName = oldName;

        } else if (parent.getComponentAlias(newName) != null) {

          newName = "Copy of " + newName;
          parent.setComponentAlias(newName, specPath);
          return;
        }
        this.identifier = newName;
        parent.removeComponentAlias(oldName);
        parent.setComponentAlias(this.identifier, specPath);

      } else if ("spec".equals(key)) {
        parent.setComponentAlias(this.identifier, (String) value);
      }

    }

    public boolean isPropertySet(Object key) {

      if ("name".equals(key)) {

        return identifier != null;

      } else if ("spec".equals(key)) {

        return parent.getComponentAlias((String) key) != null;

      }

      return true;
    }

    public Object getPropertyValue(Object key) {

      if ("name".equals(key)) {

        return identifier;

      } else if ("spec".equals(key)) {

        return parent.getComponentAlias(this.identifier);

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
      this.parent = (PluginApplicationSpecification) parent;
    }

  }

  protected class NewAliasAction extends Action {

    protected NewAliasAction() {
      super();
      setText("New");
      setDescription("create a new component alias");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;

      PluginApplicationSpecification appSpec =
        (PluginApplicationSpecification) ((TapestryApplicationModel) getModel())
          .getApplicationSpec();

      ComponentRefDialog dialog =
        new ComponentRefDialog(newButton.getShell(), getModel(), appSpec.getComponentMapAliases());

      dialog.create();
      if (dialog.open() == dialog.OK) {
        String name = dialog.getResultName();
        String componentString = dialog.getResultComponent();
        if (appSpec.getComponentAlias(name) != null) {
          int counter = 1;
          while (appSpec.getComponentAlias(name + counter) != null) {
            counter++;
          }
          name = name + counter;
        }
        appSpec.setComponentAlias(name, componentString);
        forceDirty();
        update();
        setSelection(name);
      }
      updateSelection = false;
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

        PluginApplicationSpecification appSpec =
          (PluginApplicationSpecification) holder.getParent();

        String prev = findPrevious(holder.getIdentifier());
        appSpec.removeComponentAlias(holder.getIdentifier());
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