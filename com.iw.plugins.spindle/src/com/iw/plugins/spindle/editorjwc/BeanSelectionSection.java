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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.ui.editor.PropertiesAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.ui.ComboBoxPropertyDescriptor;
import com.iw.plugins.spindle.ui.EmptySelection;
import com.iw.plugins.spindle.ui.TypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;
import com.iw.plugins.spindle.util.StringSorter;
import com.primix.tapestry.spec.BeanLifecycle;

public class BeanSelectionSection
  extends SpindleFormSection
  implements IModelChangedListener, ISelectionChangedListener {

  private boolean updateNeeded = false;
  private boolean updateSelection = false;
  private boolean hasFocus = false;
  private Button newButton;
  private Button editButton;
  private Button deleteButton;

  private PropertiesAction pAction;
  private Action newBeanAction = new NewBeanAction();
  private Action deleteBeanAction = new DeleteBeanAction();

  private BeanContentProvider contentProvider = new BeanContentProvider();
  private List beanHolders = new ArrayList();

  private TreeViewer viewer;

  /**
   * Constructor for ComponentSelectionSection
   */
  public BeanSelectionSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Beans");
    pAction = new PropertiesAction(page.getEditor());
    pAction.setText("Edit");
    pAction.setToolTipText("Edit the selected");
  }

  public void initialize(Object input) {
    TapestryComponentModel model = (TapestryComponentModel) input;
    updateNeeded = true;
    update();
    model.addModelChangedListener(this);

  }

  public void setSelectedBean(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) viewer.getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((BeanHolder) items[i]).name.equals(name)) {
          list.add(items[i]);
        }
      }
      if (list.isEmpty()) {
        return;
      }
      viewer.setSelection(new JavaListSelectionProvider(list));
    }
  }

  protected boolean alreadyHasBean(String name) {
    Object[] items = ((ITreeContentProvider) contentProvider).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        BeanHolder holder = (BeanHolder) items[i];
        if (holder.name.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean selectFirst() {
    Object[] items = ((ITreeContentProvider) viewer.getContentProvider()).getElements(null);
    if (items == null || items.length < 1) {
      return false;
    }
    final ArrayList list = new ArrayList();
    list.add(items[0]);
    viewer.setSelection(new JavaListSelectionProvider(list));
    return true;
  }

  protected BeanHolder findPrevious(BeanHolder holder) {
    if (holder != null) {
      Object[] items = ((ITreeContentProvider) contentProvider).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        BeanHolder candidate = (BeanHolder) items[i];
        if (candidate == holder && i >= 1) {
          return ((BeanHolder) items[i - 1]);
        }
      }
    }
    return null;
  }

  public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    if (selection.isEmpty()) {
      fireSelectionNotification(null);
    }
    BeanHolder holder = (BeanHolder) selection.getFirstElement();
    if (holder != null) {
      fireSelectionNotification(holder.beanSpec);
      if (hasFocus || updateSelection) {
        getFormPage().setSelection(event.getSelection());
      }
    }
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClient(Composite parent, FormWidgetFactory factory) {
    Composite container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
    gd.grabExcessVerticalSpace = true;
    container.setLayoutData(gd);

    container.setLayout(layout);
    viewer = new TreeViewer(container, SWT.SINGLE | SWT.BORDER);
    viewer.setSorter(new Sorter());
    viewer.setLabelProvider(new BeanLabelProvider());
    viewer.setContentProvider(new BeanContentProvider());
    viewer.addSelectionChangedListener(this);
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        handleEdit();
      }
    });

    factory.paintBordersFor(container);
    Tree treeControl = (Tree) viewer.getControl();
    gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
    gd.verticalSpan = 100;
    treeControl.setLayoutData(gd);
    treeControl.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent event) {
        hasFocus = true;
      }
      public void focusLost(FocusEvent event) {
        hasFocus = false;
      }
    });
    MenuManager popupMenuManager = new MenuManager();
    IMenuListener listener = new IMenuListener() {
      public void menuAboutToShow(IMenuManager mng) {
        fillContextMenu(mng);
      }
    };
    popupMenuManager.setRemoveAllWhenShown(true);
    popupMenuManager.addMenuListener(listener);
    Menu menu = popupMenuManager.createContextMenu(treeControl);
    treeControl.setMenu(menu);

    Composite buttonContainer = factory.createComposite(container);
    gd = new GridData(GridData.FILL_VERTICAL);
    buttonContainer.setLayoutData(gd);
    layout = new GridLayout();
    layout.marginHeight = 0;
    buttonContainer.setLayout(layout);

    newButton = factory.createButton(buttonContainer, "New", SWT.PUSH);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.verticalAlignment = GridData.BEGINNING;
    newButton.setLayoutData(gd);
    newButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleNew();
        newButton.getShell().setDefaultButton(null);
      }
    });

    editButton = factory.createButton(buttonContainer, "Edit", SWT.PUSH);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.verticalAlignment = GridData.BEGINNING;
    editButton.setLayoutData(gd);
    editButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleEdit();
        editButton.getShell().setDefaultButton(null);
      }
    });

    deleteButton = factory.createButton(buttonContainer, "Delete", SWT.PUSH);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.verticalAlignment = GridData.BEGINNING;
    deleteButton.setLayoutData(gd);
    deleteButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleDelete();
        deleteButton.getShell().setDefaultButton(null);
      }
    });

    return container;
  }

  protected void fillContextMenu(IMenuManager manager) {
    ISelection selection = viewer.getSelection();
    final Object object = ((IStructuredSelection) selection).getFirstElement();
    manager.add(newBeanAction);
    if (object != null) {
      manager.add(new Separator());
      manager.add(deleteBeanAction);

    }
    manager.add(new Separator());
    pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  private void handleNew() {
    newBeanAction.run();
  }

  private void handleEdit() {
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    if (!selection.isEmpty()) {
      BeanHolder holder = (BeanHolder) ((IStructuredSelection) selection).getFirstElement();
      fireSelectionNotification(holder.beanSpec);
      getFormPage().setSelection(selection);
      pAction.run();
    }
  }

  private void handleDelete() {
    deleteBeanAction.run();
  }

  public ITapestryModel getModel() {
    return (ITapestryModel) getFormPage().getModel();
  }

  private void forceDirty() {
    setDirty(true);
    IModel model = (IModel) getModel();
    if (model instanceof IEditable) {
      IEditable editable = (IEditable) model;
      editable.setDirty(true);
      getFormPage().getEditor().fireSaveNeeded();
    }
  }

  private PluginComponentSpecification getSpec() {
    TapestryComponentModel model = (TapestryComponentModel) getFormPage().getModel();
    return model.getComponentSpecification();
  }

  public void dispose() {
    viewer.getControl().dispose();
    newButton.dispose();
    super.dispose();
  }

  public void update() {
    if (updateNeeded && canUpdate()) {
      this.update(getSpec());
    }
  }

  public void update(PluginComponentSpecification spec) {
    beanHolders = Collections.EMPTY_LIST;
    Set ids = new TreeSet(spec.getBeanNames());
    if (ids.isEmpty()) {
      viewer.setInput(beanHolders);
      fireSelectionNotification(null);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }
    Iterator iter = ids.iterator();
    beanHolders = new ArrayList();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      BeanHolder holder = new BeanHolder(name, (PluginBeanSpecification) spec.getBeanSpecification(name));
      beanHolders.add(holder);
    }
    viewer.setInput(beanHolders);
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

  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    viewer.addSelectionChangedListener(listener);
  }

  protected class BeanLabelProvider implements ILabelProvider {

    Image beanImage;
    ILabelProviderListener listener;

    public BeanLabelProvider() {
      beanImage = TapestryImages.getSharedImage("bean.gif");
    }
    public Image getImage(Object element) {
      return beanImage;
    }
    public String getText(Object element) {
      return ((BeanHolder) element).name;
    }
    public void addListener(ILabelProviderListener arg0) {
      listener = arg0;
    }
    public void dispose() {
      // Image disposal managed by the plugin
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }
    public void removeListener(ILabelProviderListener arg0) {
      listener = null;
    }
  }

  protected class BeanContentProvider implements ITreeContentProvider {
    /**
     * @see ITreeContentProvider#getChildren(Object)
     */
    public Object[] getChildren(Object element) {
      return null;
    }
    public Object getParent(Object element) {
      return null;
    }
    public boolean hasChildren(Object arg0) {
      return false;
    }
    public Object[] getElements(Object arg0) {

      return beanHolders.toArray();
    }
    public void dispose() {
    }
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
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
      NewBeanDialog dialog =
        new NewBeanDialog(newButton.getShell(), getModel(), getSpec().getBeanNames());
      dialog.create();
      if (dialog.open() == dialog.OK) {
        String name = dialog.getResultName();
        PluginBeanSpecification newSpec = dialog.getResultBeanSpec();
        getSpec().addBeanSpecification(name, newSpec);
        forceDirty();
        update();
        setSelectedBean(name);
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
      BeanHolder holder = (BeanHolder) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
      if (holder != null) {
        PluginComponentSpecification spec = getSpec();
        BeanHolder prev = findPrevious(holder);
        spec.removeBeanSpecification(holder.name);
        forceDirty();
        update();
        if (prev != null) {
          setSelectedBean(prev.name);
        } else {
          selectFirst();
        }
      }
      updateSelection = false;
    }

  }

  protected class BeanHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    private String name;
    private PluginBeanSpecification beanSpec;

    private BeanLifecycle[] lifecycles =
      { BeanLifecycle.NONE, BeanLifecycle.PAGE, BeanLifecycle.REQUEST };

    private String[] lifecycleLabels = { "None", "Page", "Request" };

    private IPropertyDescriptor[] descriptors =
      {
        new TextPropertyDescriptor("name", "Name"),
        new TypeDialogPropertyDescriptor("class", "Class", getModel()),
        new ComboBoxPropertyDescriptor("lifecycle", "Lifecycle", lifecycleLabels, false)};

    public BeanHolder(String name, PluginBeanSpecification beanSpec) {
      super();
      this.name = name;
      this.beanSpec = beanSpec;
    }

    public void resetPropertyValue(Object key) {
    }

    public IPropertySource getPropertySource(Object key) {
      return this;
    }

    public void setPropertyValue(Object key, Object value) {
      if (!((BaseTapestryModel) getModel()).isEditable()) {
        updateNeeded = true;
        update();
        setSelectedBean(name);
        return;
      }
      if (name.equals(key)) {
        String oldName = name;
        String newName = (String) value;
        if ("".equals(newName.trim())) {
          newName = oldName;
        } else if (alreadyHasBean(newName)) {
          newName = newName + "Copy";
          PluginBeanSpecification copy =
            new PluginBeanSpecification(beanSpec.getClassName(), beanSpec.getLifecycle());
          getSpec().addBeanSpecification(newName, copy);
          forceDirty();
          update();
          setSelectedBean(newName);
          return;
        }
        name = newName;
        getSpec().removeComponent(oldName);
      } else if ("class".equals(key)) {
        beanSpec.setClassName((String) value);
      } else if ("lifecycle".equals(key)) {
        int chosenIndex = ((Integer) value).intValue();
        beanSpec.setLifecycle(lifecycles[chosenIndex]);
      }
      getSpec().setBeanSpecification(name, beanSpec);
      forceDirty();
      update();
      setSelectedBean(name);
    }

    public boolean isPropertySet(Object key) {
      if ("id".equals(key)) {
        return true;
      } else if ("class".equals(key)) {
        return beanSpec.getClassName() != null;
      } else {
        return true;
      }
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return name;
      } else if ("class".equals(key)) {
        return beanSpec.getClassName();
      } else if ("lifecycle".equals(key)) {
        BeanLifecycle current = beanSpec.getLifecycle();
        for (int i = 0; i < lifecycles.length; i++) {
          if (lifecycles[i] == current) {
            return new Integer(i);
          }
        }
      }
      return null;

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

  }

  protected class Sorter extends StringSorter {

    protected Sorter() {
      super();
    }

    public int compare(Viewer viewer, Object e1, Object e2) {
      BeanLabelProvider provider = (BeanLabelProvider) ((TreeViewer) viewer).getLabelProvider();
      return super.compare(viewer, provider.getText(e1), provider.getText(e2));
    }
  }

}