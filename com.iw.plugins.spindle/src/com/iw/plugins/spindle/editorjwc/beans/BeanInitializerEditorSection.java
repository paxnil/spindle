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
package com.iw.plugins.spindle.editorjwc.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import net.sf.tapestry.bean.FieldBeanInitializer;
import net.sf.tapestry.bean.IBeanInitializer;
import net.sf.tapestry.bean.PropertyBeanInitializer;
import net.sf.tapestry.bean.StaticBeanInitializer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.spec.bean.PluginFieldBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginPropertyBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginStaticBeanInitializer;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.ui.EmptySelection;

public class BeanInitializerEditorSection extends AbstractPropertySheetEditorSection {

  private DeleteInitializerAction deleteAction = new DeleteInitializerAction();
  private NewInitializerButtonAction newInitializerAction = new NewInitializerButtonAction();
  private NewPropertyInitializerAction newPropertyAction = new NewPropertyInitializerAction();
  private NewStaticInitializerAction newStaticAction = new NewStaticInitializerAction();
  private NewFieldInitializerAction newFieldAction = new NewFieldInitializerAction();

  private PluginBeanSpecification selectedBean;

  /**
   * Constructor for ParameterEditorSection
   */
  public BeanInitializerEditorSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(new InitializerLabelProvider());
    setNewAction(newInitializerAction);
    setDeleteAction(deleteAction);
    setHeaderText("Initializers");
    setDescription("This section allows one to edit selected beans's initializers");
  }

  public void initialize(Object object) {
    super.initialize(object);

    getViewer().setSorter(new Sorter());

    BaseTapestryModel model = (BaseTapestryModel) object;

    if (!model.isEditable()) {

      newPropertyAction.setEnabled(false);
      newInitializerAction.setEnabled(false);
      newPropertyAction.setEnabled(false);
      newStaticAction.setEnabled(false);
      newFieldAction.setEnabled(false);
    }
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    // this can only come from the BeanSelectionSection and it can only be
    // that a new PluginBeanSpecification was selected!
    selectedBean = (PluginBeanSpecification) changeObject;

    newButton.setEnabled(selectedBean != null);
    deleteButton.setEnabled(selectedBean != null);
    editButton.setEnabled(selectedBean != null);

    updateNeeded = true;
    update();
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {

      updateNeeded = true;

    } else if (event.getChangeType() == IModelChangedEvent.CHANGE) {

      String changed = event.getChangedProperty();

      if (changed.equals("beanInitializers")
        || changed.equals("propertyName")
        || changed.equals("propertyPath")
        || changed.equals("staticValue")
        || changed.equals("fieldValue")) {

        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    holderArray = Collections.EMPTY_LIST;
    PluginBeanSpecification spec = selectedBean;

    if (spec == null || spec.getInitializers() == null || spec.getInitializers().isEmpty()) {

      setInput(holderArray);
      fireSelectionNotification(EmptySelection.Instance);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }

    Iterator iter = spec.getInitializers().iterator();
    holderArray = new ArrayList();

    while (iter.hasNext()) {

      holderArray.add((IBeanInitializer) iter.next());
    }
    setInput(holderArray);
  }

  protected void fillContextMenu(IMenuManager manager) {
    ISelection selection = getSelection();
    final Object object = ((IStructuredSelection) selection).getFirstElement();
    MenuManager submenu = new MenuManager("New");
    submenu.add(newPropertyAction);
    submenu.add(newStaticAction);
    submenu.add(newFieldAction);
    manager.add(submenu);

    if (object != null) {

      manager.add(new Separator());
      manager.add(deleteAction);

    }
    manager.add(new Separator());
    manager.add(pAction);
  }

  public class InitializerLabelProvider extends AbstractIdentifiableLabelProvider {

    private Image staticImage = TapestryImages.getSharedImage("bean-static-init.gif");
    private Image propertyImage = TapestryImages.getSharedImage("bean-property-init.gif");
    private Image fieldImage = TapestryImages.getSharedImage("bean-property-init.gif");

    public String getText(Object object) {
      return object.toString();
    }

    public Image getImage(Object object) {

      IBeanInitializer initer = (IBeanInitializer) object;
      if (initer instanceof PluginStaticBeanInitializer) {
        return staticImage;
      }
      if (initer instanceof PluginPropertyBeanInitializer) {
        return propertyImage;
      }
      if (initer instanceof PluginFieldBeanInitializer) {
        return fieldImage;
      }
      return null;
    }

  }

  public class Sorter extends ViewerSorter {

    /**
    * @see org.eclipse.jface.viewers.ViewerSorter#compare(Viewer, Object, Object)
    */
    public int compare(Viewer viewer, Object a, Object b) {
      String aString = ((IIdentifiable) a).getIdentifier();
      String bString = ((IIdentifiable) b).getIdentifier();

      return aString.compareTo(bString);
    }

  }

  class DeleteInitializerAction extends Action {

    protected DeleteInitializerAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      IIdentifiable initer = (IIdentifiable) getSelected();
      PluginBeanSpecification bean = (PluginBeanSpecification) initer.getParent();

      if (initer != null) {

        String prev = findPrevious(initer.getIdentifier());
        bean.removeInitializer((IBeanInitializer) initer);
        initer.setParent(null);
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

  protected class NewInitializerButtonAction extends Action {

    protected NewInitializerButtonAction() {
      super();
    }

    public void run() {
      updateSelection = true;
      ChooseBeanInitializerDialog dialog = new ChooseBeanInitializerDialog(newButton.getShell());
      dialog.create();

      if (dialog.open() == dialog.OK) {

        Class chosen = dialog.getSelectedIntializerClass();

        if (chosen == PropertyBeanInitializer.class) {

          newPropertyAction.run();
        } else if (chosen == StaticBeanInitializer.class) {

          newStaticAction.run();
        }
        if (chosen == FieldBeanInitializer.class) {

          newFieldAction.run();
        }
      }
      updateSelection = false;
    }
  }

  protected abstract class BaseNewInitializerAction extends Action {

    protected BaseNewInitializerAction() {
      super();
    }

    /**
    * @see Action#run()
    */
    public void run() {
      if (selectedBean == null) {
        return;
      }
      PluginBeanSpecification bean = selectedBean;

      String newName = "property";
      if (bean.alreadyHasInitializer(newName)) {
        int counter = 0;
        while (bean.alreadyHasInitializer(newName + counter)) {
          counter++;
        }
        newName = newName + counter;
      }
      IBeanInitializer newInitializer = getNewInitializerFor(newName);
      bean.addInitializer(newInitializer);
      forceDirty();
      update();
      setSelection(((IIdentifiable) newInitializer).getIdentifier());
    }

    protected abstract IBeanInitializer getNewInitializerFor(String propertyName);

  }

  class NewStaticInitializerAction extends BaseNewInitializerAction {
    protected NewStaticInitializerAction() {
      super();
      setText("Static Initializer");
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-static-init.gif")));

    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      Object value = "fill in value";
      return new PluginStaticBeanInitializer(propertyName, value);
    }
  }

  class NewPropertyInitializerAction extends BaseNewInitializerAction {

    protected NewPropertyInitializerAction() {
      super();
      setText("Property Initializer");
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-property-init.gif")));
    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      return new PluginPropertyBeanInitializer(propertyName, "fill in value");
    }

  }
  class NewFieldInitializerAction extends BaseNewInitializerAction {

    protected NewFieldInitializerAction() {
      super();
      setText("Field Initializer");
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-property-init.gif")));
    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      return new PluginFieldBeanInitializer(propertyName, "select value");
    }

  }

}