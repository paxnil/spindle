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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.ui.EmptySelection;

public class BeanSelectionSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener, ISelectionChangedListener {

  private Action newBeanAction = new NewBeanAction();
  private Action deleteBeanAction = new DeleteBeanAction();

  /**
   * Constructor for ComponentSelectionSection
   */
  public BeanSelectionSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Beans");
    setLabelProvider(new BeanLabelProvider());
    setNewAction(newBeanAction);
    setDeleteAction(deleteBeanAction);
  }

  public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    if (selection.isEmpty()) {
      fireSelectionNotification(null);
    }
    PluginBeanSpecification bean = (PluginBeanSpecification) getSelected();
    if (bean != null) {
      fireSelectionNotification(bean);
      if (hasFocus || updateSelection) {
        getFormPage().setSelection(event.getSelection());
      }
    }

  }

  protected void fillContextMenu(IMenuManager manager) {
    PluginBeanSpecification bean = (PluginBeanSpecification) getSelected();
    manager.add(newBeanAction);
    if (bean != null) {
      manager.add(new Separator());
      manager.add(deleteBeanAction);

    }
    manager.add(new Separator());
    pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  public void update(BaseTapestryModel model) {
    holderArray.removeAll(holderArray);
    PluginComponentSpecification spec =
      ((TapestryComponentModel) model).getComponentSpecification();
    Set ids = new TreeSet(spec.getBeanNames());
    if (ids.isEmpty()) {
      setInput(holderArray);
      fireSelectionNotification(null);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }
    Iterator iter = ids.iterator();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      holderArray.add((PluginBeanSpecification) spec.getBeanSpecification(name));
    }
    setInput(holderArray);
    selectFirst();
  }

  public void modelChanged(IModelChangedEvent event) {
    int eventType = event.getChangeType();
    if (eventType == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
      return;
    } else if (eventType == IModelChangedEvent.CHANGE) {
      String propertyName = event.getChangedProperty();
      updateNeeded = propertyName.equals("beans");
    }
  }

  protected class BeanLabelProvider extends AbstractIdentifiableLabelProvider {

    Image beanImage;

    public BeanLabelProvider() {
      beanImage = TapestryImages.getSharedImage("bean.gif");
    }
    public Image getImage(Object element) {
      return beanImage;
    }

  }

  class NewBeanAction extends Action {
    /**
     * Constructor for NewPropertyAction
     */
    protected NewBeanAction() {
      super();
      setText("New");
      setToolTipText("Create a new Bean");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;

      TapestryComponentModel model = (TapestryComponentModel) getFormPage().getModel();
      PluginComponentSpecification cspec = model.getComponentSpecification();

      NewBeanDialog dialog =
        new NewBeanDialog(newButton.getShell(), getModel(), cspec.getBeanNames());
      dialog.create();

      if (dialog.open() == dialog.OK) {

        String name = dialog.getResultName();
        PluginBeanSpecification newSpec = dialog.getResultBeanSpec();
        cspec.addBeanSpecification(name, newSpec);
        forceDirty();
        update();
        setSelection(name);
      }
      updateSelection = false;
    }

  }

  class DeleteBeanAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected DeleteBeanAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginBeanSpecification bean = (PluginBeanSpecification) getSelected();

      if (bean != null) {

        PluginComponentSpecification spec = (PluginComponentSpecification) bean.getParent();
        String prev = findPrevious(bean.getIdentifier());
        spec.removeBeanSpecification(bean.getIdentifier());
        bean.setParent(null);
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
