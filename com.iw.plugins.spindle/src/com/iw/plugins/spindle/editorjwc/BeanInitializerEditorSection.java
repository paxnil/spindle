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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.ui.editor.PropertiesAction;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.bean.PluginFieldBeanInitializer;
import com.iw.plugins.spindle.bean.PluginPropertyBeanInitializer;
import com.iw.plugins.spindle.bean.PluginStaticBeanInitializer;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.ui.CheckboxPropertyDescriptor;
import com.iw.plugins.spindle.ui.ComboBoxPropertyDescriptor;
import com.iw.plugins.spindle.ui.EmptySelection;
import com.iw.plugins.spindle.ui.FieldPropertyDescriptor;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;

import net.sf.tapestry.bean.FieldBeanInitializer;
import net.sf.tapestry.bean.IBeanInitializer;
import net.sf.tapestry.bean.PropertyBeanInitializer;
import net.sf.tapestry.bean.StaticBeanInitializer;

public class BeanInitializerEditorSection extends AbstractPropertySheetEditorSection {

  private List initializerHolders = new ArrayList();

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
    setContentProvider(new InitializerEditorContentProvider());
    setLabelProvider(new InitializerLabelProvider());
    setNewAction(newInitializerAction);
    setDeleteAction(deleteAction);
    setHeaderText("Initializers");
    setDescription("This section allows one to edit selected beans's initializers");
  }

  public void initialize(Object object) {
    super.initialize(object);
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
    initializerHolders = Collections.EMPTY_LIST;
    PluginBeanSpecification spec = selectedBean;
    if (spec == null || spec.getInitializers() == null || spec.getInitializers().isEmpty()) {
      setInput(initializerHolders);
      fireSelectionNotification(EmptySelection.Instance);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }
    Iterator iter = spec.getInitializers().iterator();
    initializerHolders = new ArrayList();
    while (iter.hasNext()) {
      initializerHolders.add(getHolderFor((IBeanInitializer) iter.next()));
    }
    setInput(initializerHolders);
    //selectFirst();
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
    PropertiesAction pAction = new PropertiesAction(getFormPage().getEditor());
    pAction.setText("Edit");
    pAction.setToolTipText("Edit the selected");
    pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  protected BaseInitializerHolder findPrevious(BaseInitializerHolder holder) {
    if (holder != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((BaseInitializerHolder) items[i]).equals(holder) && i >= 1) {
          return ((BaseInitializerHolder) items[i - 1]);
        }
      }
    }
    return null;
  }

  protected boolean alreadyHasInitializer(String propertyName) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        BaseInitializerHolder holder = (BaseInitializerHolder) items[i];
        IBeanInitializer initer = holder.getInitializer();
        if (initer.getPropertyName().equals(propertyName)) {
          return true;
        }
      }
    }
    return false;
  }

  public void setSelection(IBeanInitializer initer) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        BaseInitializerHolder foundholder = (BaseInitializerHolder) items[i];
        if (foundholder.getInitializer() == initer) {
          ArrayList list = new ArrayList();
          list.add(items[i]);
          setSelection(new JavaListSelectionProvider(list));
          break;
        }
      }
    }
  }

  public BaseInitializerHolder getHolderFor(IBeanInitializer initer) {
    if (initer instanceof PluginStaticBeanInitializer) {
      return new StaticInitializerHolder((PluginStaticBeanInitializer) initer);
    }
    if (initer instanceof PluginPropertyBeanInitializer) {
      return new PropertyInitializerHolder((PluginPropertyBeanInitializer) initer);
    }
    if (initer instanceof PluginFieldBeanInitializer) {
    	return new FieldInitializerHolder((PluginFieldBeanInitializer) initer);
    }
    return null;
  }

  protected abstract class BaseInitializerHolder implements IPropertySource {
    public abstract IBeanInitializer getInitializer();

    public Object getAdapter(Class clazz) {
      if (clazz == IPropertySource.class) {
        return (IPropertySource) this;
      }
      return null;
    }

    public IPropertySource getPropertySource(Object key) {
      return this;
    }

    public void resetPropertyValue(Object key) {
    }

  }

  protected class PropertyInitializerHolder
    extends BaseInitializerHolder
    implements IAdaptable, IPropertySource, IPropertySourceProvider {

    private PluginPropertyBeanInitializer initer;

    private IPropertyDescriptor[] descriptors =
      { new TextPropertyDescriptor("property", "Property Name"), new TextPropertyDescriptor("value", "Property Path")};

    public PropertyInitializerHolder(PluginPropertyBeanInitializer initer) {
      super();
      this.initer = initer;
    }

    public String toString() {
      String result = "property = \"" + initer.getPropertyName();
      String value = initer.getOriginalPropertyPath();
      if (value == null) {
        return result + "\" value is null";
      }
      return result + "\" property-path = \"" + value + "\"";
    }

    public IBeanInitializer getInitializer() {
      return initer;
    }

    public void setPropertyValue(Object key, Object value) {
      if (!isModelEditable()) {
        updateNeeded = true;
        update();
        setSelection(initer);
        return;
      }
      if ("property".equals(key)) {
        String oldName = initer.getPropertyName();
        String newName = (String) value;
        if ("".equals(newName.trim())) {
          newName = oldName;
        } else if (alreadyHasInitializer(newName)) {
          newName = "Copy of " + newName;
          PluginPropertyBeanInitializer copy = new PluginPropertyBeanInitializer(newName, initer.getOriginalPropertyPath());
          forceDirty();
          update();
          setSelection(copy);
          return;
        }
        initer.setPropertyName(newName);
      } else if ("value".equals(key)) {
        initer.setPropertyPath((String) value);
      }
      forceDirty();
      update();
      setSelection(initer);
    }

    public boolean isPropertySet(Object key) {
      if ("property".equals(key)) {
        return initer.getPropertyName() != null;
      } else if ("value".equals(key)) {
        return initer.getPropertyPath() != null;
      } else {
        return true;
      }
    }

    public Object getPropertyValue(Object key) {
      if ("property".equals(key)) {
        return initer.getPropertyName();
      } else if ("value".equals(key)) {
        return initer.getOriginalPropertyPath();
      }
      return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
      return descriptors;
    }

    public Object getEditableValue() {
      return initer.getPropertyName();
    }

  }

  static final String[] typeNames = { "Boolean", "String", "Double", "Integer" };
  static final int BOOLEAN = 0;
  static final int STRING = 1;
  static final int DOUBLE = 2;
  static final int INTEGER = 3;
  static final Double trueD = new Double(1.0);
  static final Double falseD = new Double(0.0);
  static final Integer trueI = new Integer(1);
  static final Integer falseI = new Integer(0);

  protected class StaticInitializerHolder
    extends BaseInitializerHolder
    implements IAdaptable, IPropertySource, IPropertySourceProvider {

    private PluginStaticBeanInitializer initer;

    private TextPropertyDescriptor propertyDescriptor = new TextPropertyDescriptor("property", "Property Name");

    private ComboBoxPropertyDescriptor typeDescriptor = new ComboBoxPropertyDescriptor("type", "Type", typeNames, false);

    private IPropertyDescriptor[] booleanTypeDescriptors =
      new IPropertyDescriptor[] { propertyDescriptor, typeDescriptor, new CheckboxPropertyDescriptor("value", "Value")};

    private IPropertyDescriptor[] otherTypeDescriptors =
      new IPropertyDescriptor[] { propertyDescriptor, typeDescriptor, new TextPropertyDescriptor("value", "Value")};

    public StaticInitializerHolder(PluginStaticBeanInitializer initer) {
      super();
      this.initer = initer;
    }

    public String toString() {
      String result = "property = \"" + initer.getPropertyName();
      Object value = initer.getValue();
      if (value == null) {
        return result + "\" value is null";
      }
      return result + "\" type = \"" + typeNames[getInitializerValueType()] + "\" value = \"" + value.toString() + "\"";
    }

    public IBeanInitializer getInitializer() {
      return initer;
    }

    public void setPropertyValue(Object key, Object value) {
      if (!isModelEditable()) {
        updateNeeded = true;
        update();
        setSelection(initer);
        return;
      }
      if ("property".equals(key)) {
        String oldName = initer.getPropertyName();
        String newName = (String) value;
        if ("".equals(newName.trim())) {
          newName = oldName;
        } else if (alreadyHasInitializer(newName)) {
          newName = "Copy of " + newName;
          PluginStaticBeanInitializer copy = new PluginStaticBeanInitializer(newName, initer.getValue());
          forceDirty();
          update();
          setSelection(copy);
          return;
        }
        initer.setPropertyName(newName);
      } else if ("type".equals(key)) {
        Object oldValue = initer.getValue();
        int type = ((Integer) value).intValue();
        //set value to the new type to enable conversion below
        switch (type) {
          case BOOLEAN :
            initer.setValue(Boolean.TRUE);
            break;
          case STRING :
            initer.setValue("");
            break;
          case DOUBLE :
            initer.setValue(trueD);
            break;
          case INTEGER :
            initer.setValue(trueI);
            break;
        }
        // now we can force conversion
        setValue(oldValue.toString());

      } else if ("value".equals(key)) {
        // conversion is forced here too based on existing type
        setValue(value);
      }
      forceDirty();
      update();
      setSelection(initer);
    }

    // set the initializer's value, converting if necessary
    private void setValue(Object value) {
      Object converted = null;
      // find the type of the existing value so we know what
      // we are converting 'value' into
      int type = getInitializerValueType();
      // there are only two type of cell editors we are using
      // string, and boolean
      if (value instanceof String) {
        switch (type) {
          case BOOLEAN :
            converted = convertStringToBoolean((String) value);
            break;
          case STRING :
            converted = value;
            break;
          case DOUBLE :
            converted = convertStringToDouble((String) value);
            break;
          case INTEGER :
            converted = convertStringToInteger((String) value);
            break;
        }
      } else { // its Boolean
        boolean bvalue = ((Boolean) value).booleanValue();
        switch (type) {
          case BOOLEAN :
            converted = value;
            break;
          case STRING :
            converted = ((Boolean) value).toString();
            break;
          case DOUBLE :
            converted = (bvalue ? trueD : falseD);
            break;
          case INTEGER :
            converted = (bvalue ? trueD : falseD);
            break;
        }
      }
      initer.setValue(converted);
    }

    private int getInitializerValueType() {
      String name = initer.getValue().getClass().getName();
      for (int i = 0; i < typeNames.length; i++) {
        if (name.endsWith(typeNames[i])) {
          return i;
        }
      }
      return -1;
    }

    private Double convertStringToDouble(String candidate) {
      Double result = new Double(0.0);
      try {
        result = new Double(candidate);
      } catch (Exception e) {
      }
      return result;
    }

    private Integer convertStringToInteger(String candidate) {
      Integer result = new Integer(0);
      try {
        result = new Integer(candidate);
      } catch (Exception e) {
      }
      return result;
    }

    private Boolean convertStringToBoolean(String candidate) {
      Boolean result = Boolean.FALSE;
      try {
        result = new Boolean(candidate);
      } catch (Exception e) {
      }
      return result;
    }

    public boolean isPropertySet(Object key) {
      if ("property".equals(key)) {
        return initer.getPropertyName() != null;
      } else if ("value".equals(key)) {
        return initer.getValue() != null;
      } else {
        return true;
      }
    }

    public Object getPropertyValue(Object key) {
      if ("property".equals(key)) {
        return initer.getPropertyName();
      } else if ("type".equals(key)) {
        return new Integer(getInitializerValueType());
      } else if ("value".equals(key)) {
        if (getInitializerValueType() == BOOLEAN) {
          return ((Boolean) initer.getValue());
        } else {
          return initer.getValue().toString();
        }
      }
      return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
      if (getInitializerValueType() == BOOLEAN) {
        return booleanTypeDescriptors;
      } else {
        return otherTypeDescriptors;
      }
    }

    public Object getEditableValue() {
      return initer.getPropertyName();
    }

  }

  protected class FieldInitializerHolder
    extends BaseInitializerHolder
    implements IAdaptable, IPropertySource, IPropertySourceProvider {

    private PluginFieldBeanInitializer initer;

    private IPropertyDescriptor[] descriptors =
      {
        new TextPropertyDescriptor("property", "Property Name"),
        new FieldPropertyDescriptor("value", "Field Name", getModel())};

    public FieldInitializerHolder(PluginFieldBeanInitializer initer) {
      this.initer = initer;
    }

    public String toString() {
      String result = "property = \"" + initer.getPropertyName();
      String value = initer.getFieldName();
      if (value == null) {
        return result + "\" value is null";
      }
      return result + "\" field-name = \"" + value + "\"";
    }
    /* (non-Javadoc)
    * @see IPropertySource#getEditableValue()
    */
    public Object getEditableValue() {
      return initer.getPropertyName();
    }

    /* (non-Javadoc)
     * @see IPropertySource#getPropertyDescriptors()
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
      return descriptors;
    }

    /* (non-Javadoc)
     * @see IPropertySource#getPropertyValue(Object)
     */
    public Object getPropertyValue(Object key) {
      if ("property".equals(key)) {
        return initer.getPropertyName();
      } else if ("value".equals(key)) {
        return initer.getFieldName();
      }
      return null;
    }

    /* (non-Javadoc)
     * @see IPropertySource#isPropertySet(Object)
     */
    public boolean isPropertySet(Object key) {
      if ("property".equals(key)) {
        return initer.getPropertyName() != null;
      } else if ("value".equals(key)) {
        return initer.getFieldName() != null;
      } else {
        return true;
      }
    }

    /* (non-Javadoc)
    * @see IPropertySource#setPropertyValue(Object, Object)
    */
    public void setPropertyValue(Object key, Object value) {
      if (!isModelEditable()) {
        updateNeeded = true;
        update();
        setSelection(initer);
        return;
      }
      if ("property".equals(key)) {
        String oldName = initer.getPropertyName();
        String newName = (String) value;
        if ("".equals(newName.trim())) {
          newName = oldName;
        } else if (alreadyHasInitializer(newName)) {
          newName = "Copy of " + newName;
          PluginFieldBeanInitializer copy = new PluginFieldBeanInitializer(newName, initer.getFieldName());
          forceDirty();
          update();
          setSelection(copy);
          return;
        }
        initer.setPropertyName(newName);
      } else if ("value".equals(key)) {
        initer.setFieldName((String) value);
      }
      forceDirty();
      update();
      setSelection(initer);
    }

    /* (non-Javadoc)
    * @see BaseInitializerHolder#getInitializer()
    */
    public IBeanInitializer getInitializer() {
      return initer;
    }

  }

  public class InitializerLabelProvider extends LabelProvider implements ITableLabelProvider {

    private Image staticImage = TapestryImages.getSharedImage("bean-static-init.gif");
    private Image propertyImage = TapestryImages.getSharedImage("bean-property-init.gif");
    private Image fieldImage = TapestryImages.getSharedImage("bean-property-init.gif");

    public String getText(Object object) {
      return object.toString();
    }

    public String getColumnText(Object object, int column) {
      if (column != 1) {
        return "";
      }
      return getText(object);
    }

    public Image getImage(Object object) {
      BaseInitializerHolder holder = (BaseInitializerHolder) object;
      IBeanInitializer initer = holder.getInitializer();
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

    public Image getColumnImage(Object object, int column) {
      if (column != 1) {
        return null;
      }
      return getImage(object);
    }
  }

  class InitializerEditorContentProvider extends DefaultContentProvider implements ITreeContentProvider {

    Comparator comparer = new Comparator() {
      public int compare(Object a, Object b) {
        String aString = ((BaseInitializerHolder) a).getInitializer().getPropertyName();
        String bString = ((BaseInitializerHolder) a).getInitializer().getPropertyName();

        return aString.compareTo(bString);
      }
    };
    public Object[] getElements(Object object) {
      Collections.sort(initializerHolders, comparer);
      return initializerHolders.toArray();
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
      BaseInitializerHolder holder = (BaseInitializerHolder) getSelected();
      if (holder != null) {
        PluginBeanSpecification spec = selectedBean;
        BaseInitializerHolder prev = findPrevious(holder);
        spec.removeInitializer(holder.getInitializer());
        forceDirty();
        update();
        if (prev != null) {
          setSelection(prev.getInitializer());
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
        } if (chosen == FieldBeanInitializer.class) {
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
      PluginBeanSpecification spec = selectedBean;
      String newName = "property";
      if (alreadyHasInitializer(newName)) {
        int counter = 0;
        while (alreadyHasInitializer(newName + counter)) {
          counter++;
        }
        newName = newName + counter;
      }
      IBeanInitializer newInitializer = getNewInitializerFor(newName);
      spec.addInitializer(newInitializer);
      forceDirty();
      update();
      setSelection(newInitializer);
    }

    protected abstract IBeanInitializer getNewInitializerFor(String propertyName);

  }

  class NewStaticInitializerAction extends BaseNewInitializerAction {
    protected NewStaticInitializerAction() {
      super();
      setText("Static Initializer");
      setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-static-init.gif")));

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
      setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-property-init.gif")));
    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      return new PluginPropertyBeanInitializer(propertyName, "fill in value");
    }

  }
  class NewFieldInitializerAction extends BaseNewInitializerAction {

    protected NewFieldInitializerAction() {
      super();
      setText("Field Initializer");
      setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-property-init.gif")));
    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      return new PluginFieldBeanInitializer(propertyName, "select value");
    }

  }

}