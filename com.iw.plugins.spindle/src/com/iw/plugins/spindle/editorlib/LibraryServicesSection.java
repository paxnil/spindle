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
package com.iw.plugins.spindle.editorlib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.tapestry.spec.ILibrarySpecification;
import org.eclipse.jface.action.Action;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryModelManager;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.ui.descriptors.TypeDialogPropertyDescriptor;

public class LibraryServicesSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener {

  private Button revertButton;
  private Action revertAction;

  /**
   * Constructor for PropertySection 
   */
  public LibraryServicesSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(new ServiceLabelProvider());
    setNewAction(new NewServiceAction());
    setDeleteAction(new DeleteServiceAction());
    setHeaderText("Services");
    setDescription("In this section you can add/override services. Note that service names are case insensitive.");
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
    holderArray.removeAll(holderArray);

    ILibrarySpecification spec =
      (ILibrarySpecification) ((TapestryLibraryModel) model).getSpecification();

    List myServices = spec.getServiceNames();

    ILibrarySpecification framework = TapestryModelManager.getDefaultLibrary().getSpecification();

    ArrayList defaultServices = (ArrayList) ((ArrayList) framework.getServiceNames()).clone();

    defaultServices.removeAll(myServices);

    for (Iterator iter = defaultServices.iterator(); iter.hasNext();) {

      String defaultName = (String) iter.next();
      ServiceHolder holder =
        new ServiceHolder(defaultName, framework.getServiceClassName(defaultName));
      holderArray.add(holder);
    }

    Iterator iter = myServices.iterator();

    while (iter.hasNext()) {
      String name = (String) iter.next();
      ServiceHolder holder = new ServiceHolder(name, spec.getServiceClassName(name));
      holderArray.add(holder);
    }
    setInput(holderArray);
  }

  public class ServiceLabelProvider extends AbstractIdentifiableLabelProvider {

    private Image image = TapestryImages.getSharedImage("property16.gif");

    public Image getImage(Object object) {
      return image;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
     */
    public String getText(Object element) {
    	
      ServiceHolder holder = (ServiceHolder)element;
      return holder.getIdentifier() + " = " + holder.getPropertyValue("class");
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
        PluginApplicationSpecification spec = (PluginApplicationSpecification) holder.getParent();
        String prev = findPrevious(holder.identifier);
        spec.removeService(holder.identifier);
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
      TapestryLibraryModel model = (TapestryLibraryModel) getModel();
      PluginApplicationSpecification spec =
        (PluginApplicationSpecification) model.getSpecification();
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
      spec.setServiceClassName(useSeviceName, "fill in value");
      forceDirty();
      update();
      setSelection(useSeviceName);
      updateSelection = false;
    }

  }

  protected class ServiceHolder implements IPropertySource, IIdentifiable {

    private String identifier;
    private String classname;

    /**
     * Constructor for PropertyHolder
     */
    public ServiceHolder(String identifier, String classname) {
      super();
      this.identifier = identifier;
      this.classname = classname;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getIdentifier()
     */
    public String getIdentifier() {
      return identifier;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getParent()
     */
    public Object getParent() {
      TapestryLibraryModel model = (TapestryLibraryModel) getFormPage().getModel();

      return (PluginApplicationSpecification) model.getSpecification();
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setIdentifier(String)
     */
    public void setIdentifier(String id) {
      identifier = id;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
     */
    public void setParent(Object parent) {
    }

    public void resetPropertyValue(Object key) {
      if ("Name".equals(key)) {
        identifier = null;
      }
    }

    public void setPropertyValue(Object key, Object value) {
      if (!isModelEditable()) {
        updateNeeded = true;
        update();
        setSelection(this.identifier);
        return;
      }

      PluginApplicationSpecification spec = (PluginApplicationSpecification) getParent();
      if ("name".equals(key)) {
        String oldName = this.identifier;
        String newName = ((String) value).toLowerCase();
        if ("".equals(newName.trim())) {

          newName = oldName;
        } else if (spec.getServiceClassName(newName) != null && !spec.isDefaultService(newName)) {

          newName = "copyof" + newName;
        }
        this.identifier = newName;
        spec.removeService(oldName);
        spec.setServiceClassName(this.identifier, this.classname);

      } else if ("class".equals(key)) {
        this.classname = (String) value;
        spec.setServiceClassName(this.identifier, this.classname);

      }

    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
        return identifier != null;
      } else if ("class".equals(key)) {
        return classname != null;
      }
      return false;
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return identifier;
      } else if ("class".equals(key)) {
        return classname;
      }
      return null;
    }
    public IPropertyDescriptor[] getPropertyDescriptors() {
      return new IPropertyDescriptor[] {
        new TextPropertyDescriptor("name", "Name"),
        new TypeDialogPropertyDescriptor("class", "Class", "net.sf.tapestry.IEngineService"),
        };
    }

    public Object getEditableValue() {
      return identifier;
    }

  }

  /*
   * override to handle revert button
   * @see AbstractPropertySheetEditorSection#updateButtons(Object)
   */
  protected void updateButtons(Object selected) {
    boolean isEditable = isModelEditable();
    ServiceHolder holder = (ServiceHolder) selected;
    TapestryLibraryModel model = (TapestryLibraryModel) getModel();
    IPluginLibrarySpecification spec = (IPluginLibrarySpecification) model.getSpecification();
    newButton.setEnabled(isEditable);
    if (deleteButton != null) {
      deleteButton.setEnabled(
        isEditable && selected != null && spec.canDeleteService(holder.identifier));
    }
    editButton.setEnabled(isEditable && holder != null);
    revertButton.setEnabled(
      isEditable && holder != null && spec.canRevertService(holder.identifier));
  }

}
