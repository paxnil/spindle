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
package com.iw.plugins.spindle.editorapp;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.ui.TypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;

public class ApplicationServicesSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener {

  private ArrayList serviceHolders = new ArrayList();

  private Button revertButton;
  private Action revertAction;

  /**
   * Constructor for PropertySection 
   */
  public ApplicationServicesSection(SpindleFormPage page) {
    super(page);
    setContentProvider(new ServiceContentProvider());
    setLabelProvider(new ServiceLabelProvider());
    setNewAction(new NewServiceAction());
    setDeleteAction(new DeleteServiceAction());
    setHeaderText("Application Services");
    setDescription("In this section you can add/override application services. Note that service names are case insensitive.");
  }

  public void dispose() {
    revertButton.dispose();
    super.dispose();
  }

  protected void createButtons(Composite buttonContainer, FormWidgetFactory factory) {
    super.createButtons(buttonContainer, factory);
    revertButton = factory.createButton(buttonContainer, "Revert", SWT.PUSH);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    revertButton.setLayoutData(gd);
    revertButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        // revert here is another label for delete!
        handleDelete();
      }
    });
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("services")) {
        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    PluginApplicationSpecification spec =
      (PluginApplicationSpecification) ((TapestryApplicationModel) model).getApplicationSpec();

    Iterator iter = spec.getServiceNames().iterator();
    serviceHolders.removeAll(serviceHolders);
    while (iter.hasNext()) {
      String name = (String) iter.next();
      ServiceHolder holder = new ServiceHolder(name, spec.getServiceClassName(name));
      serviceHolders.add(holder);
    }
    setInput(serviceHolders);
    //selectFirst();
  }

  protected void setSelection(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((ServiceHolder) items[i]).name.equals(name)) {
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
        if (((ServiceHolder) items[i]).name.equals(name) && i >= 1) {
          return ((ServiceHolder) items[i - 1]).name;
        }
      }
    }
    return null;
  }

  protected boolean alreadyHasName(String name) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        ServiceHolder holder = (ServiceHolder) items[i];
        if (holder.name.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  public class ServiceLabelProvider extends LabelProvider implements ITableLabelProvider {

    private Image image = TapestryImages.getSharedImage("property16.gif");

    public String getText(Object object) {
      ServiceHolder holder = (ServiceHolder) object;
      return (holder.name + " = " + holder.classname);
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

  class ServiceContentProvider extends DefaultContentProvider implements ITreeContentProvider {
    public Object[] getElements(Object object) {
      return serviceHolders.toArray();
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

  class DeleteServiceAction extends Action {

    private ITapestryModel model;

    /**
     * Constructor for NewPropertyAction
     */
    protected DeleteServiceAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      ServiceHolder holder = (ServiceHolder) getSelected();
      if (holder != null) {
        TapestryApplicationModel model = (TapestryApplicationModel) getModel();
        PluginApplicationSpecification spec = (PluginApplicationSpecification) model.getApplicationSpec();
        String prev = findPrevious(holder.name);
        spec.removeService(holder.name);
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

  class NewServiceAction extends Action {

    private ITapestryModel model;

    /**
     * Constructor for NewPropertyAction
     */
    protected NewServiceAction() {
      super();
      setText("New");
      setToolTipText("Create a new Service");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      TapestryApplicationModel model = (TapestryApplicationModel) getModel();
      PluginApplicationSpecification spec = (PluginApplicationSpecification) model.getApplicationSpec();
      String useSeviceName = "service";
      if (spec.getServiceClassName(useSeviceName + 1) != null) {
        int counter = 2;
        while (spec.getServiceClassName(useSeviceName + counter) != null) {
          counter++;
        }
        useSeviceName = useSeviceName + counter;
      } else {
        useSeviceName = useSeviceName + 1;
      }
      spec.setService(useSeviceName, "fill in value");
      forceDirty();
      update();
      setSelection(useSeviceName);
      updateSelection = false;
    }

  }

  protected class ServiceHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    public String name;
    public String classname;

    /**
     * Constructor for PropertyHolder
     */
    public ServiceHolder(String name, String classname) {
      super();
      this.name = name;
      this.classname = classname;
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

      TapestryApplicationModel model = (TapestryApplicationModel) getFormPage().getModel();
      PluginApplicationSpecification spec = (PluginApplicationSpecification) model.getApplicationSpec();
      if ("name".equals(key)) {
        String oldName = this.name;
        String newName = ((String) value).toLowerCase();
        if ("".equals(newName.trim())) {

          newName = oldName;
        } else if (alreadyHasName(newName) && !spec.isDefaultService(newName)) {

          newName = "copyof" + newName;
        }
        this.name = newName;
        spec.removeService(oldName);
        spec.setService(this.name, this.classname);
        forceDirty();
        update();
        setSelection(this.name);

      } else if ("class".equals(key)) {
        this.classname = (String) value;
        spec.setService(this.name, this.classname);
        forceDirty();
        update();
        setSelection(this.name);
      }

    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
        return name != null;
      } else if ("class".equals(key)) {
        return classname != null;
      }
      return false;
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return name;
      } else if ("class".equals(key)) {
        return classname;
      }
      return null;
    }
    public IPropertyDescriptor[] getPropertyDescriptors() {
      return new IPropertyDescriptor[] {
        new TextPropertyDescriptor("name", "Name"),
        new TypeDialogPropertyDescriptor("class", "Class", getModel(), "net.sf.tapestry.IEngineService"),
        };
    }

    public Object getEditableValue() {
      return classname;
    }

    public Object getAdapter(Class clazz) {
      if (clazz == IPropertySource.class) {
        return (IPropertySource) this;
      }
      return null;
    }

  }

  /*
   * override to handle revert button
   * @see AbstractPropertySheetEditorSection#updateButtons(Object)
   */
  protected void updateButtons(Object selected) {
    boolean isEditable = isModelEditable();
    ServiceHolder holder = (ServiceHolder) selected;
    TapestryApplicationModel model = (TapestryApplicationModel) getModel();
    PluginApplicationSpecification spec = model.getApplicationSpec();
    newButton.setEnabled(isEditable);
    if (deleteButton != null) {
      deleteButton.setEnabled(isEditable && selected != null && spec.canDeleteService(holder.name));
    }
    editButton.setEnabled(isEditable && holder != null);
    revertButton.setEnabled(isEditable && holder != null && spec.canRevertService(holder.name));
  }

}
