package com.iw.plugins.spindle.editors;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;

public class PropertyEditableSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener {

  private ArrayList propertyHolders = new ArrayList();

  /**
   * Constructor for PropertySection 
   */
  public PropertyEditableSection(SpindleFormPage page) {
    super(page);
    setContentProvider(new PropertyContentProvider());
    setLabelProvider(new PropertyLabelProvider());
    setNewAction(new NewPropertyAction());
    setDeleteAction(new DeletePropertyAction());
    setHeaderText("Properties");
    setDescription("This section describes properties you can edit");
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("properties")) {
        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    Iterator iter = model.getPropertyNames().iterator();
    propertyHolders.removeAll(propertyHolders);
    while (iter.hasNext()) {
      String name = (String) iter.next();
      PropertyHolder holder = new PropertyHolder(name, model.getProperty(name));
      propertyHolders.add(holder);
    }
    setInput(propertyHolders);
    //selectFirst();
  }

  protected void setSelection(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((PropertyHolder) items[i]).name.equals(name)) {
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
        if (((PropertyHolder) items[i]).name.equals(name) && i >= 1) {
          return ((PropertyHolder) items[i - 1]).name;
        }
      }
    }
    return null;
  }

  protected boolean alreadyHasName(String name) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        PropertyHolder holder = (PropertyHolder) items[i];
        if (holder.name.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  public class PropertyLabelProvider extends LabelProvider implements ITableLabelProvider {

    private Image image = TapestryImages.getSharedImage("property16.gif");

    public String getText(Object object) {
      PropertyHolder holder = (PropertyHolder) object;
      return (holder.name + " = " + holder.value);
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

  class PropertyContentProvider extends DefaultContentProvider implements ITreeContentProvider {
    public Object[] getElements(Object object) {
      return propertyHolders.toArray();
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

  class DeletePropertyAction extends Action {

    private ITapestryModel model;

    /**
     * Constructor for NewPropertyAction
     */
    protected DeletePropertyAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PropertyHolder holder = (PropertyHolder) getSelected();
      if (holder != null) {
        BaseTapestryModel model = (BaseTapestryModel) getFormPage().getModel();
        String prev = findPrevious(holder.name);
        model.setProperty(holder.name, null);
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

  class NewPropertyAction extends Action {

    private ITapestryModel model;

    /**
     * Constructor for NewPropertyAction
     */
    protected NewPropertyAction() {
      super();
      setText("New");
      setToolTipText("Create a new Property");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      BaseTapestryModel model = (BaseTapestryModel) getFormPage().getModel();
      String useProperty = "property";
      if (model.getProperty(useProperty + 1) != null) {
        int counter = 2;
        while (model.getProperty(useProperty + counter) != null) {
          counter++;
        }
        useProperty = useProperty + counter;
      } else {
      	useProperty = useProperty + 1;
      }
      model.setProperty(useProperty, "fill in value");
      forceDirty();
      update();
      setSelection(useProperty);
      updateSelection = false;
    }

  }

  private static IPropertyDescriptor[] descriptors;

  static {
    descriptors =
      new IPropertyDescriptor[] {
        new TextPropertyDescriptor("name", "Name"),
        new TextPropertyDescriptor("value", "Value")};
  }

  protected class PropertyHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    public String name;
    public String value;

    /**
     * Constructor for PropertyHolder
     */
    public PropertyHolder(String name, String value) {
      super();
      this.name = name;
      this.value = value;
    }

    public void resetPropertyValue(Object key) {
      if ("Name".equals(key)) {
        name = null;
      } else if ("Value".equals(key)) {
        value = null;
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

      BaseTapestryModel model = (BaseTapestryModel) getFormPage().getModel();
      if ("name".equals(key)) {
        String oldName = this.name;
        String newName = (String) value;
        if ("".equals(newName.trim())) {
          newName = oldName;
        } else if (alreadyHasName(newName)) {
          newName = "Copy of " + newName;
        }
        this.name = newName;
        model.setProperty(oldName, null);
        model.setProperty(this.name, this.value);
        forceDirty();
        update();
        setSelection(this.name);
      } else if ("value".equals(key)) {
        this.value = (String) value;
        model.setProperty(this.name, this.value);
        forceDirty();
        update();
        setSelection(this.name);
      }

    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
        return name != null;
      } else if ("value".equals(key)) {
        return value != null;
      }
      return false;
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return name;
      } else if ("value".equals(key)) {
        return value;
      }
      return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
      return descriptors;
    }

    public Object getEditableValue() {
      return value;
    }

    public Object getAdapter(Class clazz) {
      if (clazz == IPropertySource.class) {
        return (IPropertySource) this;
      }
      return null;
    }

  }

}