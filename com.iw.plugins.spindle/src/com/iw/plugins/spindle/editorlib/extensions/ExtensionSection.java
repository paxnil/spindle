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

package com.iw.plugins.spindle.editorlib.extensions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.tapestry.spec.ILibrarySpecification;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
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
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.spec.PluginExtensionSpecification;
import com.iw.plugins.spindle.ui.descriptors.ComponentTypeDialogPropertyDescriptor;

public class ExtensionSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener, ISelectionChangedListener {

  private Action newExtensionAction = new NewExtensionAction();
  private Action deleteExtensionAction = new DeleteExtensionAction();

  /**
   * Constructor for ComponentAliasSection
   */
  public ExtensionSection(SpindleFormPage page) {
    super(page);
    setHeaderText(MessageUtil.getString("ExtensionsSection.headerText"));
    setLabelProvider(new ExtensionLabelProvider());
    setNewAction(newExtensionAction);
    setDeleteAction(deleteExtensionAction);

  }

  protected void fillContextMenu(IMenuManager manager) {
    Object selected =  getSelected();
    boolean isEditable = isModelEditable();
    if (isEditable) {
      manager.add(newExtensionAction);
      if (selected != null) {
        manager.add(new Separator());
        manager.add(deleteExtensionAction);
        manager.add(new Separator());
        pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
        manager.add(pAction);
      }
    }
  }

  public void selectionChanged(SelectionChangedEvent event) {
    PluginExtensionSpecification extension = (PluginExtensionSpecification) getSelected();
    if (extension == null) {

      fireSelectionNotification(null);
      deleteButton.setEnabled(false);
      editButton.setEnabled(false);

    } else {

      boolean isEditable = isModelEditable();
      fireSelectionNotification(extension);

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

    Set ids = librarySpec.getAllExtensionNames();

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
      PluginExtensionSpecification extension = (PluginExtensionSpecification)librarySpec.getExtensionSpecification(name);
      extension.setParent(librarySpec);
      extension.setIdentifier(name);
      holderArray.add(extension);
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
      updateNeeded = event.getChangedProperty().equals("extensions");
    }
  }

  protected class ExtensionLabelProvider extends AbstractIdentifiableLabelProvider {

    Image extensionImage;

    public ExtensionLabelProvider() {
      extensionImage = TapestryImages.getSharedImage("extension16.gif");
    }

    public Image getImage(Object element) {
      return extensionImage;
    }

  }

  protected class NewExtensionAction extends Action {

    protected NewExtensionAction() {
      super();
      setText("New");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      TapestryLibraryModel model = (TapestryLibraryModel) getFormPage().getModel();
      IPluginLibrarySpecification spec = model.getSpecification();
      String newExtensionName = "extension";
      if (spec.getExtensionSpecification(newExtensionName) != null) {
        int counter = 0;
        while (spec.getExtensionSpecification(newExtensionName + counter) != null) {
          counter++;
        }
        newExtensionName = newExtensionName + counter;
      }
      PluginExtensionSpecification newExtension = new PluginExtensionSpecification();
      newExtension.setClassName("choose class");
      spec.setExtensionSpecification(newExtensionName, newExtension);
      forceDirty();
      update();
      setSelection(newExtensionName);
      updateSelection = false;
    }
  }

  protected class DeleteExtensionAction extends Action {

    protected DeleteExtensionAction() {
      super();
      setText("Delete");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginExtensionSpecification holder = (PluginExtensionSpecification) getSelected();
      if (holder != null) {

        IPluginLibrarySpecification appSpec =
          (IPluginLibrarySpecification) holder.getParent();

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