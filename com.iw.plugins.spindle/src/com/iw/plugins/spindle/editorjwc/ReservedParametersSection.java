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
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
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
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;

public class ReservedParametersSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener {

  private ParameterHolder parameterHolder = new ParameterHolder();

  /**
   * Constructor for PropertySection 
   */
  public ReservedParametersSection(SpindleFormPage page) {
    super(page);
    setContentProvider(new ParameterContentProvider());
    setLabelProvider(new ParameterLabelProvider());
    setNewAction(new NewReservedParameterAction());
    setDeleteAction(new DeleteReservedParameterAction());
    setHeaderText("Reserved Parameters");
    setDescription("In this section you can manage reserved parameters. Note that values are case insensitive.");
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("parameters")) {
        updateNeeded = true;
        update();
      }
    }
  }

  public void update(BaseTapestryModel model) {
    setInput(parameterHolder);
    //selectFirst();
  }

  protected void setSelection(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((String) items[i]).equals(name)) {
          list.add(items[i]);
        }
      }
      if (list.isEmpty()) {
        return;
      }
      setSelection(new JavaListSelectionProvider(list));
    }
  }

  protected String findPrevious(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((String) items[i]).equals(name) && i >= 1) {
          return (String) items[i - 1];
        }
      }
    }
    return null;
  }

  protected boolean alreadyHasParameter(String name) {
    TapestryComponentModel model = (TapestryComponentModel) getModel();
    PluginComponentSpecification spec = model.getComponentSpecification();
    return spec.isReservedParameterName(name);
  }
  
  /*
   * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {
    Object object = null;
    if (!event.getSelection().isEmpty()) {
      ISelection selection = event.getSelection();
      if (selection instanceof IStructuredSelection) {
        object = ((IStructuredSelection) selection).getFirstElement();
      }
    }
    if (object != null) {
      parameterHolder.setName((String) object);
    }
    fireSelectionNotification(parameterHolder);
    StructuredSelection useSelection = new StructuredSelection(parameterHolder);
    if (hasFocus || updateSelection) {
      getFormPage().setSelection(useSelection);
    }
    updateButtons(object);
  }

  protected void handleEdit() {
    if (!getSelection().isEmpty()) {
      fireSelectionNotification(parameterHolder);
      getFormPage().setSelection(new StructuredSelection(parameterHolder));
      pAction.run();
    }
  }  

  public class ParameterLabelProvider extends LabelProvider implements ITableLabelProvider {

    private Image image = TapestryImages.getSharedImage("property16.gif");

    public String getText(Object object) {

      return (String) object;
    }

    public void dispose() {
      // shared images are disposed by the Plugin
    }

    public String getColumnText(Object object, int column) {
      if (column != 1) {
        return "";
      }
      return getText(object);

    }

    public Image getImage(Object object) {
      return image;
    }

    public Image getColumnImage(Object object, int column) {
      if (column != 1) {
        return null;
      }
      return getImage(object);
    }
  }

  class ParameterContentProvider extends DefaultContentProvider implements ITreeContentProvider {
    public Object[] getElements(Object object) {
      TapestryComponentModel model = (TapestryComponentModel) getModel();
      PluginComponentSpecification spec = model.getComponentSpecification();
      Collection reservedParameters = spec.getReservedParameters();
      if (reservedParameters == null || reservedParameters.isEmpty()) {
        return new Object[0];
      }
      reservedParameters = (Collection) (((HashSet) reservedParameters).clone());
      Collection currentParameters = spec.getParameterNames();
      if (currentParameters != null && !currentParameters.isEmpty()) {
        reservedParameters.removeAll(currentParameters);
      }
      return reservedParameters.toArray();
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

  class DeleteReservedParameterAction extends Action {

    private ITapestryModel model;

    /**
     * Constructor for NewPropertyAction
     */
    protected DeleteReservedParameterAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      TapestryComponentModel model = (TapestryComponentModel) getModel();
      PluginComponentSpecification spec = model.getComponentSpecification();
      String prev = findPrevious(parameterHolder.name);
      spec.setReservedParameter(parameterHolder.name, false);
      forceDirty();
      update();
      if (prev != null) {
        setSelection(prev);
      } else {
        selectFirst();
      }
      updateSelection = false;
    }

  }

  class NewReservedParameterAction extends Action {

    private ITapestryModel model;

    /**
     * Constructor for NewPropertyAction
     */
    protected NewReservedParameterAction() {
      super();
      setText("New");
      setToolTipText("Create a new Service");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      TapestryComponentModel model = (TapestryComponentModel) getModel();
      PluginComponentSpecification spec = model.getComponentSpecification();
      String useParameterName = "reserved";
      if (spec.isReservedParameterName(useParameterName + 1)) {
        int counter = 2;
        while (spec.isReservedParameterName(useParameterName + counter)) {
          counter++;
        }
        useParameterName = useParameterName + counter;
      } else {
        useParameterName = useParameterName + 1;
      }
      spec.setReservedParameter(useParameterName, true);
      forceDirty();
      update();
      setSelection(useParameterName);
      updateSelection = false;
    }

  }

  private static IPropertyDescriptor[] descriptors;

  static {
    descriptors = new IPropertyDescriptor[] { 
    	new TextPropertyDescriptor("name", "Reserved Name")};
  }

  protected class ParameterHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    public String name;

    /**
     * Constructor for PropertyHolder
     */
    public ParameterHolder() {
    }

    public void resetPropertyValue(Object key) {
      if ("Name".equals(key)) {
        name = null;
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
      TapestryComponentModel model = (TapestryComponentModel) getFormPage().getModel();
      PluginComponentSpecification spec = (PluginComponentSpecification) model.getComponentSpecification();
      if ("name".equals(key)) {
        String oldName = this.name;
        String newName = ((String) value).toLowerCase();
        if ("".equals(newName.trim())) {

          newName = oldName;
        } else if (alreadyHasParameter(newName)) {

          newName = "copyof" + newName;
        }
        this.name = newName;
        spec.setReservedParameter(oldName, false);
        spec.setReservedParameter(name, true);
        forceDirty();
        update();
        setSelection(name);
      }
    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
        return name != null;
      }
      return false;
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return name;
      }
      return "ignore this";
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
      return descriptors;
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

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

  }



}