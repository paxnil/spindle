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

import net.sf.tapestry.spec.Direction;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
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
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginParameterSpecification;
import com.iw.plugins.spindle.ui.CheckboxPropertyDescriptor;
import com.iw.plugins.spindle.ui.ComboBoxCellEditor;
import com.iw.plugins.spindle.ui.ComboBoxPropertyDescriptor;
import com.iw.plugins.spindle.ui.DocumentationPropertyDescriptor;
import com.iw.plugins.spindle.ui.TypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;

public class ParameterEditorSection extends AbstractPropertySheetEditorSection {

  private ArrayList parameterHolders = new ArrayList();
  private String[] directionLabels = { "custom", "in" };
  private Direction[] directions = {Direction.CUSTOM, Direction.IN};

  /**
   * Constructor for ParameterEditorSection
   */
  public ParameterEditorSection(SpindleFormPage page) {
    super(page);
    setContentProvider(new ParameterContentProvider());
    setLabelProvider(new ParameterLabelProvider());
    setNewAction(new NewParameterAction());
    setDeleteAction(new DeleteParameterAction());
    setHeaderText("Parameters");
    setDescription("This section describes the component's parameters");
  }

  public void initialize(Object input) {
    super.initialize(input);
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("parameters")) {
        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    PluginComponentSpecification spec = ((TapestryComponentModel) model).getComponentSpecification();
    Iterator iter = spec.getParameterNames().iterator();
    parameterHolders.removeAll(parameterHolders);
    while (iter.hasNext()) {
      String name = (String) iter.next();
      ParameterHolder holder = new ParameterHolder(name, (PluginParameterSpecification) spec.getParameter(name));
      parameterHolders.add(holder);
    }
    setInput(parameterHolders);
    //selectFirst();
  }

  protected String findPrevious(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((ParameterHolder) items[i]).name.equals(name) && i >= 1) {
          return ((ParameterHolder) items[i - 1]).name;
        }
      }
    }
    return null;
  }

  protected boolean alreadyHasParameter(String name) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        ParameterHolder holder = (ParameterHolder) items[i];
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
        ParameterHolder holder = (ParameterHolder) items[i];
        if (holder.name.equals(name)) {
          ArrayList list = new ArrayList();
          list.add(items[i]);
          setSelection(new JavaListSelectionProvider(list));
          break;
        }
      }
    }
  }

  protected class ParameterHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    public String name;
    public PluginParameterSpecification spec;

    /**
     * Constructor for PropertyHolder
     */
    public ParameterHolder(String name, PluginParameterSpecification spec) {
      super();
      this.name = name;
      this.spec = spec;
    }

    public void resetPropertyValue(Object key) {
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
      PluginComponentSpecification componentSpec = ((TapestryComponentModel) model).getComponentSpecification();
      if ("name".equals(key)) {
        String oldName = this.name;
        String newName = (String) value;
        if ("".equals(newName.trim())) {
          updateNeeded = true;
          update();
          setSelection(this.name);
          return;
        } else if (alreadyHasParameter(newName)) {
          newName = "Copy of " + newName;
        }
        this.name = newName;
        componentSpec.removeParameter(oldName);

      } else if ("propertyName".equals(key)) {
        spec.setPropertyName((String) value);

      } else if ("type".equals(key)) {
        spec.setType((String) value);

      } else if ("required".equals(key)) {
        spec.setRequired(((Boolean) value).booleanValue());

      } else if ("direction".equals(name)) {
        String direction = (String) value;
        if ("in".equals(direction)) {
          spec.setDirection(Direction.IN);
        } else {
          spec.setDirection(Direction.CUSTOM);
        }
      } else if ("description".equals(key)) {
        String newValue = (String) key;
        if (!(newValue.trim().equals(spec.getDescription()))) {
          spec.setDescription((String) value);
        } else {
          return;
        }
      }
      updateUI(componentSpec);
    }

    private void updateUI(PluginComponentSpecification componentSpec) {
      componentSpec.setParameter(this.name, spec);
      forceDirty();
      update();
      setSelection(this.name);
    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
        return name != null;
      } else if ("type".equals(key)) {
        return spec.getType() != null;
      } else {
        return true;
      }
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return name;
      } else if ("propertyName".equals(key)) {
        return spec.getPropertyName();
      } else if ("type".equals(key)) {
        return spec.getType();
      } else if ("required".equals(key)) {
        if (spec.isRequired()) {
          return Boolean.TRUE;
        } else {
          return Boolean.FALSE;
        }
      } else if ("direction".equals(key)) {
        Direction currentDir = spec.getDirection();
        for (int i = 0; i < directions.length; i++) {
          if (currentDir == directions[i]) {
            return new Integer(i);
          }
        }
        return new Integer(0);
      } else if ("description".equals(key)) {
        return spec.getDescription();
      }
      return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
      BaseTapestryModel model = (BaseTapestryModel) getFormPage().getModel();
      IPropertyDescriptor[] descriptors =
        new IPropertyDescriptor[] {
          new TextPropertyDescriptor("name", "Name"),
          new TextPropertyDescriptor("propertyName", "Property Name"),
          new TypeDialogPropertyDescriptor("type", "Type", model),
          new CheckboxPropertyDescriptor("required", "Required"),
          new ComboBoxPropertyDescriptor("direction", "Direction", directionLabels, false),
          new DocumentationPropertyDescriptor("description", "Description", "Document parameter: " + this.name, null)
      };
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

  }

  public class ParameterLabelProvider extends LabelProvider implements ITableLabelProvider {

    private Image image = TapestryImages.getSharedImage("missing");
    public String getText(Object object) {
      ParameterHolder holder = (ParameterHolder) object;
      PluginComponentSpecification componentSpec = ((TapestryComponentModel) getModel()).getComponentSpecification();
      String type = holder.spec.getType();
      type = type == null ? "" : type;
      return holder.name
        + (!"".equals(holder.spec.getType()) ? " type = " + type : "")
        + (holder.spec.isRequired() ? " REQUIRED" : "");
    }

    public void dispose() {
      // shared image disposal handled by Plugin
    }

    public String getColumnText(Object object, int column) {
      if (column != 1) {
        return "";
      }
      return (String) object;
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
      return parameterHolders.toArray();
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

  class DeleteParameterAction extends Action { /**
           * Constructor for NewPropertyAction
           */
    protected DeleteParameterAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    } /**
                       * @see Action#run()
                       */
    public void run() {
      updateSelection = true;
      ParameterHolder holder = (ParameterHolder) getSelected();
      if (holder != null) {
        PluginComponentSpecification spec = ((TapestryComponentModel) getFormPage().getModel()).getComponentSpecification();
        String prev = findPrevious(holder.name);
        spec.removeParameter(holder.name);
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

  class NewParameterAction extends Action { /**
           * Constructor for NewPropertyAction
           */
    protected NewParameterAction() {
      super();
      setText("New");
      setToolTipText("Create a new Parameter");
    } /**
                       * @see Action#run()
                       */
    public void run() {
      updateSelection = true;
      PluginComponentSpecification spec = ((TapestryComponentModel) getFormPage().getModel()).getComponentSpecification();
      String useName = "parameter";
      if (spec.getParameter(useName + 1) != null) {
        int counter = 2;
        while (spec.getParameter(useName + counter) != null) {
          counter++;
        }
        useName = useName + counter;
      } else {
        useName = useName + 1;
      }
      PluginParameterSpecification newSpec = new PluginParameterSpecification();
      newSpec.setRequired(false);
      newSpec.setType("");
      spec.setParameter(useName, newSpec);
      forceDirty();
      update();
      setSelection(useName);
      updateSelection = false;
    }

  }

}