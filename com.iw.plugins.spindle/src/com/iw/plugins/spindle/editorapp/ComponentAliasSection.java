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

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.ui.ComponentTypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.ui.EmptySelection;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;
import com.iw.plugins.spindle.util.StringSorter;

public class ComponentAliasSection
  extends SpindleFormSection
  implements IModelChangedListener, ISelectionChangedListener {

  private boolean updateNeeded;
  private boolean updateSelection = true;
  private boolean hasFocus;
  private Button newButton;
  private Button editButton;
  private Button deleteButton;

  private List aliasHolders;

  private PropertiesAction pAction;
  private Action newAliasAction = new NewAliasAction();
  private Action deleteAliasAction = new DeleteAliasAction();

  private TreeViewer viewer;
  /**
   * Constructor for ComponentAliasSection
   */
  public ComponentAliasSection(SpindleFormPage page) {
    super(page);
    setHeaderText(MessageUtil.getString("ComponentAliasSection.headerText"));
    pAction = new PropertiesAction(page.getEditor());
    pAction.setText("Edit");
    pAction.setToolTipText("Edit the selected");

  }

  public void initialize(Object input) {
    TapestryApplicationModel model = (TapestryApplicationModel) input;
    updateNeeded = true;
    update();
    model.addModelChangedListener(this);
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClient(Composite parent, FormWidgetFactory factory) {
    Composite container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;

    container.setLayout(layout);
    viewer = new TreeViewer(container, SWT.SINGLE | SWT.BORDER);
    viewer.setSorter(new Sorter());
    viewer.setLabelProvider(new AliasLabelProvider());
    viewer.setContentProvider(new AliasContentProvider());
    viewer.addSelectionChangedListener(this);
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        handleEdit();
      }
    });
    viewer.getControl().addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent event) {
        hasFocus = true;
      }
      public void focusLost(FocusEvent event) {
        hasFocus = false;
      }
    });

    factory.paintBordersFor(container);
    Tree treeControl = (Tree) viewer.getControl();
    GridData gd = new GridData(GridData.FILL_VERTICAL);
    gd.verticalSpan = 50;
    treeControl.setLayoutData(gd);
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
    final AliasHolder holder = (AliasHolder) ((IStructuredSelection) selection).getFirstElement();
    boolean isEditable = isModelEditable();
    if (isEditable) {
      manager.add(newAliasAction);
      if (holder != null) {
        manager.add(new Separator());
        manager.add(deleteAliasAction);
      }
    }
    manager.add(new Separator());
    pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  private void handleNew() {
    newAliasAction.run();
  }

  private void handleEdit() {
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    if (!selection.isEmpty()) {
      AliasHolder holder = (AliasHolder) ((IStructuredSelection) selection).getFirstElement();
      fireSelectionNotification(holder.name);
      getFormPage().setSelection(selection);
      pAction.run();
    }
  }

  private void handleDelete() {
    deleteAliasAction.run();
  }

  protected boolean alreadyHasAlias(String name) {
    Object[] items = ((ITreeContentProvider) viewer.getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        AliasHolder holder = (AliasHolder) items[i];
        if (holder.name.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  public void setSelection(String name) {
    Object[] items = ((ITreeContentProvider) viewer.getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        AliasHolder foundholder = (AliasHolder) items[i];
        if (foundholder.name == name) {
          ArrayList list = new ArrayList();
          list.add(items[i]);
          viewer.setSelection(new JavaListSelectionProvider(list));
          break;
        }
      }
    }
  }

  public void selectFirst() {
    Object[] items = ((ITreeContentProvider) viewer.getContentProvider()).getElements(null);
    if (items == null || items.length < 1) {
      return;
    }
    final ArrayList list = new ArrayList();
    list.add(items[0]);
    viewer.setSelection(new JavaListSelectionProvider(list));
  }

  public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    if (selection.isEmpty()) {
      fireSelectionNotification(null);
    }
    boolean isEditable = isModelEditable();
    AliasHolder holder = (AliasHolder) selection.getFirstElement();
    if (holder != null) {
      fireSelectionNotification(holder.name);
      if (hasFocus || updateSelection) {
        getFormPage().setSelection(event.getSelection());
      }
      newButton.setEnabled(isEditable);
      deleteButton.setEnabled(isEditable);
    } else {
      deleteButton.setEnabled(false);
    }
  }

  protected String findPrevious(AliasHolder holder) {
    if (holder != null) {
      Object[] items = ((ITreeContentProvider) viewer.getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        AliasHolder candidate = (AliasHolder) items[i];
        if (candidate == holder && i >= 1) {
          return ((AliasHolder) items[i - 1]).name;
        }
      }
    }
    return null;
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

  private PluginApplicationSpecification getSpec() {
    TapestryApplicationModel model = (TapestryApplicationModel) getFormPage().getModel();
    return model.getApplicationSpec();
  }

  public void dispose() {
    viewer.getControl().dispose();
    newButton.dispose();
    super.dispose();
  }

  public void update() {
    if (updateNeeded) {
      this.update((BaseTapestryModel) getModel());
    }
  }

  public void update(BaseTapestryModel model) {
    aliasHolders = Collections.EMPTY_LIST;
    Set ids = new TreeSet(getSpec().getComponentMapAliases());
    if (ids.isEmpty()) {
      viewer.setInput(aliasHolders);
      fireSelectionNotification(null);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }
    Iterator iter = ids.iterator();
    aliasHolders = new ArrayList();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      AliasHolder holder = new AliasHolder(name);
      aliasHolders.add(holder);
    }
    viewer.setInput(aliasHolders);
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
      updateNeeded = event.getChangedProperty().equals("componentMap");
    }
  }

  protected class AliasLabelProvider implements ILabelProvider {

    Image componentImage;
    ILabelProviderListener listener;

    public AliasLabelProvider() {
      componentImage = TapestryImages.getSharedImage("component16.gif");
    }

    public Image getImage(Object element) {
      return componentImage;
    }

    public String getText(Object element) {
      return ((AliasHolder) element).name;

    }

    public void addListener(ILabelProviderListener arg0) {
      listener = arg0;
    }

    public void dispose() {
      //shared image disposal handled by Plugin
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener arg0) {
      listener = null;
    }

  }

  protected class AliasContentProvider implements ITreeContentProvider {

    /**
     * @see ITreeContentProvider#getChildren(Object)
     */
    public Object[] getChildren(Object element) {
      return null;
    }

    /**
     * @see ITreeContentProvider#getParent(Object)
     */
    public Object getParent(Object element) {
      return null;
    }

    /**
     * @see ITreeContentProvider#hasChildren(Object)
     */
    public boolean hasChildren(Object arg0) {
      return false;
    }

    /**
     * @see IStructuredContentProvider#getElements(Object)
     */
    public Object[] getElements(Object arg0) {
      return aliasHolders.toArray();
    }

    /**
     * @see IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see IContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
    }

  }

  protected class AliasHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    public String name;

    private IPropertyDescriptor[] descriptors =
      {
        new TextPropertyDescriptor("name", "Name"),
        new ComponentTypeDialogPropertyDescriptor("spec", "Spec", null, null, getModel()),
        };

    /**
     * Constructor for PropertyHolder
     */
    public AliasHolder(String name) {
      super();
      this.name = name;
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
      PluginApplicationSpecification spec = getSpec();
      if ("name".equals(key)) {
        String oldName = this.name;
        String specPath = spec.getComponentAlias(oldName);
        String newName = (String) value;
        if ("".equals(newName.trim())) {
          newName = oldName;
        } else if (alreadyHasAlias(newName)) {
          updateSelection = true;
          newName = "Copy of " + newName;
          spec.setComponentAlias(newName, specPath);
          forceDirty();
          update();
          setSelection(newName);
          updateSelection = false;
          return;
        }
        this.name = newName;
        spec.removeComponentAlias(oldName);
        spec.setComponentAlias(this.name, specPath);
        forceDirty();
        update();
        setSelection(this.name);
      } else if ("spec".equals(key)) {
        spec.setComponentAlias(this.name, (String) value);
        forceDirty();
        update();
        setSelection(this.name);
      }
    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
        return name != null;
      } else if ("spec".equals(key)) {
        return getSpec().getComponentAlias((String) key) != null;
      } else {
        return true;
      }
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return name;
      } else if ("spec".equals(key)) {
        return getSpec().getComponentAlias((String) this.name);
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

  protected class NewAliasAction extends Action {

    protected NewAliasAction() {
      super();
      setText("New");
      setDescription("create a new component alias");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      ComponentRefDialog dialog =
        new ComponentRefDialog(newButton.getShell(), getModel(), getSpec().getComponentMapAliases());

      dialog.create();
      if (dialog.open() == dialog.OK) {
        String name = dialog.getResultName();
        String component = dialog.getResultComponent();
        PluginApplicationSpecification spec = getSpec();
        if (spec.getComponentAlias(name) != null) {
        	int counter = 1;
        	while (spec.getComponentAlias(name+counter) != null) {
        		counter++;
        	}
        	name = name + counter;
        }
        spec.setComponentAlias(name, component);
        forceDirty();
        update();
        setSelection(name);
      }
      updateSelection = false;
    }
  }

  protected class DeleteAliasAction extends Action {

    protected DeleteAliasAction() {
      super();
      setText("Delete");
      setDescription("delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      AliasHolder holder = (AliasHolder) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
      if (holder != null) {
        String prev = findPrevious(holder);
        getSpec().removeComponentAlias(holder.name);
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

  protected class Sorter extends StringSorter {

    protected Sorter() {
      super();
    }

    public int compare(Viewer viewer, Object e1, Object e2) {
      AliasLabelProvider provider = (AliasLabelProvider) ((TreeViewer) viewer).getLabelProvider();
      return super.compare(viewer, provider.getText(e1), provider.getText(e2));
    }
  }

}