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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.ui.ChooseComponentDialog;
import com.iw.plugins.spindle.ui.ComponentAliasViewer;
import com.iw.plugins.spindle.ui.ComponentTypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.ui.CopyToClipboardAction;
import com.iw.plugins.spindle.ui.EmptySelection;
import com.iw.plugins.spindle.ui.IToolTipHelpProvider;
import com.iw.plugins.spindle.ui.IToolTipProvider;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.ui.TreeViewerWithToolTips;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;
import com.iw.plugins.spindle.util.StringSorter;
import com.iw.plugins.spindle.util.TapestryLookup;
import com.iw.plugins.spindle.util.Utils;

public class ComponentSelectionSection
  extends SpindleFormSection
  implements IModelChangedListener, ISelectionChangedListener {

  private boolean updateNeeded = false;
  private boolean hasFocus;
  private Button newButton;
  private Button editButton;
  private Button deleteButton;
  private Button copyButton;
  private boolean updateSelection = false;

  private PropertiesAction pAction;
  private Action newComponentAction = new NewComponentAction();
  private Action deleteComponentAction = new DeleteComponentAction();
  private Action copyAction = new CopyComponentAction();
  private Action copyToAction = new CopyToAction();

  private ContainedComponentContentProvider contentProvider = new ContainedComponentContentProvider();
  private ContainedComponentLabelProvider labelProvider = new ContainedComponentLabelProvider();
  private java.util.List containedComponentHolders = new ArrayList();

  private TreeViewerWithToolTips viewer;

  private HashMap precomputedAliasInfo = new HashMap();

  /**
   * Constructor for ComponentSelectionSection
   */
  public ComponentSelectionSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Contained Components");
    pAction = new PropertiesAction(page.getEditor());
    pAction.setText("Edit");
    pAction.setToolTipText("Edit the selected");
  }

  public HashMap getPrecomputedAliasInfo() {
    return precomputedAliasInfo;
  }

  public void initialize(Object input) {
    TapestryComponentModel model = (TapestryComponentModel) input;
    updateNeeded = true;
    update();
    model.addModelChangedListener(this);
    viewer.setToolTipProvider(labelProvider);
    viewer.setToolTipHelpProvider(labelProvider);
  }

  public void selectId(String id) {
    if (id != null) {
      Object[] items = ((ITreeContentProvider) viewer.getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((CHolder) items[i]).id == id) {
          list.add(items[i]);
        }
      }
      if (list.isEmpty()) {
        return;
      }
      viewer.setSelection(new JavaListSelectionProvider(list));
    }
  }

  public void setSelectedComponent(PluginContainedComponent component) {
    if (component != null) {
      Object[] items = ((ITreeContentProvider) viewer.getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((CHolder) items[i]).component == component) {
          list.add(items[i]);
        }
      }
      if (list.isEmpty()) {
        return;
      }
      viewer.setSelection(new JavaListSelectionProvider(list));
    }
  }

  protected PluginContainedComponent findPrevious(CHolder holder) {
    if (holder != null) {
      Object[] items = ((ITreeContentProvider) contentProvider).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        CHolder candidate = (CHolder) items[i];
        if (candidate == holder && i >= 1) {
          return ((CHolder) items[i - 1]).component;
        }
      }
    }
    return null;
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

  public void setSelection(String componentId) {
    Object[] items = ((ITreeContentProvider) contentProvider).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        CHolder foundholder = (CHolder) items[i];
        if (foundholder.id == componentId) {
          ArrayList list = new ArrayList();
          list.add(items[i]);
          viewer.setSelection(new JavaListSelectionProvider(list));
          break;
        }
      }
    }
  }

  public boolean isSelected(Object object) {
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    if (selection == null || selection.isEmpty()) {
      return false;
    }
    return object == selection.getFirstElement();
  }

  public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    if (selection.isEmpty()) {
      precomputedAliasInfo.clear();
      fireSelectionNotification(null);
    }
    boolean isEditable = ((BaseTapestryModel) getModel()).isEditable();
    CHolder holder = (CHolder) selection.getFirstElement();
    if (holder != null) {
      String copyOf = holder.component.getCopyOf();
      fireSelectionNotification(holder.component);
      if (hasFocus || updateSelection) {
        getFormPage().setSelection(event.getSelection());
      }
      newButton.setEnabled(isEditable);
      deleteButton.setEnabled(isEditable);
      editButton.setEnabled(isEditable);
      copyButton.setEnabled(isEditable && holder.component.getCopyOf() == null);
      // lets pre-compute the alias lookup for the Tooltips (if required)
      String selectedType = holder.component.getType();
      if (selectedType == null || "".equals(selectedType.trim()) || selectedType.endsWith(".jwc")) {
        precomputedAliasInfo.clear();
        return;
      }
      precomputeAliasInfoForTooltip(selectedType);
    } else {
      precomputedAliasInfo.clear();
      deleteButton.setEnabled(false);
      copyButton.setEnabled(false);
    }
  }

  private void precomputeAliasInfoForTooltip(String alias) {
    precomputedAliasInfo.clear();
    Iterator applications = Utils.getApplicationsWithAlias(alias).iterator();
    while (applications.hasNext()) {
      TapestryApplicationModel appModel = (TapestryApplicationModel) applications.next();
      PluginApplicationSpecification appSpec = (PluginApplicationSpecification) (appModel).getApplicationSpec();
      TapestryComponentModel cmodel =
        (TapestryComponentModel) TapestryPlugin.getTapestryModelManager().findComponent(
          appSpec.getComponentAlias(alias),
          getModel());
      if (cmodel == null) {
        continue;
      }
      precomputedAliasInfo.put(appModel, cmodel);
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
    gd.grabExcessHorizontalSpace = true;
    container.setLayoutData(gd);

    container.setLayout(layout);
    viewer = new TreeViewerWithToolTips(container, SWT.SINGLE | SWT.BORDER);
    viewer.setSorter(new Sorter());
    viewer.setLabelProvider(new ContainedComponentLabelProvider());
    viewer.setContentProvider(contentProvider);

    viewer.getControl().setLayoutData(gd);
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
    gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
    gd.verticalSpan = 100;
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

    copyButton = factory.createButton(buttonContainer, "Copy", SWT.PUSH);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.verticalAlignment = GridData.BEGINNING;
    copyButton.setLayoutData(gd);
    copyButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleCopy();
        copyButton.getShell().setDefaultButton(null);
      }
    });

    return container;
  }

  protected void fillContextMenu(IMenuManager manager) {
    ISelection selection = viewer.getSelection();
    final CHolder holder = (CHolder) ((IStructuredSelection) selection).getFirstElement();
    boolean isEditable = ((BaseTapestryModel) getModel()).isEditable();
    if (isEditable) {
      manager.add(newComponentAction);
      if (holder != null) {
        manager.add(new Separator());
        manager.add(deleteComponentAction);
        if (holder.component.getCopyOf() == null) {
          manager.add(new Separator());
          manager.add(copyAction);
          manager.add(copyToAction);
          MenuManager submenu = new MenuManager("Copy To Clipboard");
          Display d = viewer.getControl().getDisplay();
          submenu.add(new CopyToClipboardAction(d, "jwcid=\"$value$\"", "value", holder.id));
          submenu.add(new CopyToClipboardAction(d, "<span jwcid=\"$value$\"></span>", "value", holder.id));
          manager.add(submenu);
        }
      }
    }
    manager.add(new Separator());
    pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  private void handleNew() {
    newComponentAction.run();
  }

  private void handleEdit() {
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    if (!selection.isEmpty()) {
      CHolder holder = (CHolder) ((IStructuredSelection) selection).getFirstElement();
      fireSelectionNotification(holder.component);
      getFormPage().setSelection(selection);
      pAction.run();
    }
  }

  private void handleDelete() {
    deleteComponentAction.run();
  }

  private void handleCopy() {
    copyAction.run();
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
      update(getSpec());
    }
    updateNeeded = false;
  }

  public void update(PluginComponentSpecification spec) {
    containedComponentHolders = Collections.EMPTY_LIST;
    Set ids = new TreeSet(spec.getComponentIds());
    if (ids.isEmpty()) {
      viewer.setInput(containedComponentHolders);
      fireSelectionNotification(null);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }
    Iterator iter = ids.iterator();
    containedComponentHolders = new ArrayList();
    while (iter.hasNext()) {
      String id = (String) iter.next();
      CHolder holder = new CHolder(id, (PluginContainedComponent) spec.getComponent(id));
      containedComponentHolders.add(holder);
    }
    viewer.setInput(containedComponentHolders);
    selectFirst();
  }

  public void modelChanged(IModelChangedEvent event) {
    int eventType = event.getChangeType();
    if (eventType == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
      return;
    }
    if (eventType == IModelChangedEvent.CHANGE) {
      updateNeeded = event.getChangedProperty().equals("components");
    }
  }

  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    viewer.addSelectionChangedListener(listener);
  }

  protected boolean alreadyHasId(String id) {
    Object[] items = ((ITreeContentProvider) contentProvider).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        CHolder holder = (CHolder) items[i];
        if (holder.id.equals(id)) {
          return true;
        }
      }
    }
    return false;
  }

  protected class ContainedComponentLabelProvider
    implements IToolTipProvider, IToolTipHelpProvider, ILabelProvider {

    //---------- IToolTipProvider ----------------------------//

    public String getToolTipText(Object object) {

      CHolder holder = (CHolder) object;
      String aliasOrType = holder.component.getType();
      String copyOf = holder.component.getCopyOf();
      if (copyOf != null && !"".equals(copyOf.trim())) {
        return holder.id + " is copy-of " + copyOf;
      }
      StringBuffer buffer =
        new StringBuffer(holder.id + " type = " + (aliasOrType == null ? "" : aliasOrType) + "\n");
      if (!isSelected(holder)) {
        buffer.append("Select this contained component for more tooltip info");
        return buffer.toString();
      }
      // empty means no alias!
      if (precomputedAliasInfo.isEmpty()) {
        // the type is not an alias
        String type = aliasOrType;
        if (type == null || type.equals("")) {
          buffer.append("No Type found for contained component: " + holder.id);
          return buffer.toString();
        }
        TapestryComponentModel component = TapestryPlugin.getTapestryModelManager().findComponent(type, getModel());
        if (component == null) {
          buffer.append(holder.id + "'s type: " + type + " not found.");
          return buffer.toString();
        } else if (!component.isLoaded()) {
          try {
            component.load();
          } catch (Exception e) {
            buffer.append("Could not load component: " + type + ". There could be a parse error.");
            return buffer.toString();
          }
        }
        PluginComponentSpecification componentSpec = component.getComponentSpecification();
        componentSpec.getHelpText(holder.id, buffer);
        return buffer.toString();

      } else {
        // the type IS an alias        
        String alias = aliasOrType;
        Object[] keys = precomputedAliasInfo.keySet().toArray();
        if (keys.length == 0) {
          buffer.append("Alias '" + alias + "' not found in an application.");
          return buffer.toString();
        } else {
          TapestryApplicationModel firstModel = ((TapestryApplicationModel) keys[0]);
          TapestryComponentModel cmodel = (TapestryComponentModel) precomputedAliasInfo.get(keys[0]);
          if (!cmodel.isLoaded()) {
            try {
              cmodel.load();
            } catch (CoreException e) {
            }
          }
          PluginComponentSpecification firstComponent =
            (PluginComponentSpecification) cmodel.getComponentSpecification();
          buffer.append(
            "Found alias '" + alias + "' in application '" + firstModel.getUnderlyingStorage().getFullPath() + "\n");
          buffer.append(alias + " maps to " + firstModel.getApplicationSpec().getComponentAlias(alias) + "\n");
          if (keys.length > 1) {
            buffer.append(
              "press F1 to check " + (keys.length - 1) + " other application(s) that have alias '" + alias + "'.\n");
          }
          if (firstComponent != null) {
            firstComponent.getHelpText(alias, buffer);
          }
          return buffer.toString();
        }
      }
    }

    public Image getToolTipImage(Object object) {
      return getImage(object);
    }

    //---------- IToolTipHelpProvider --------------------//

    public Object getHelp(Object obj) {
      if (!isSelected(obj)) {
        return null;
      }
      if (precomputedAliasInfo.isEmpty()) {
        return null;
      }
      CHolder holder = (CHolder) obj;
      String id = holder.id;
      String alias = holder.component.getType();
      return new ComponentAliasViewer(id, alias, precomputedAliasInfo);

    }

    //---------- ITableLabelProvider -------------------------//

    Image componentImage;
    Image componentAliasImage;
    ILabelProviderListener listener;

    public ContainedComponentLabelProvider() {
      componentImage = TapestryImages.getSharedImage("component16.gif");
      componentAliasImage = TapestryImages.getSharedImage("componentAlias16.gif");
    }
    public Image getImage(Object element) {
      CHolder holder = ((CHolder) element);
      String type = holder.component.getType();
      if (type != null && !type.endsWith(".jwc")) {
        return componentAliasImage;
      }
      return componentImage;
    }
    public String getText(Object element) {
      return ((CHolder) element).id;
    }
    public void addListener(ILabelProviderListener arg0) {
      listener = arg0;
    }
    public void dispose() {
      // shared image disposal handled by plugin
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }
    public void removeListener(ILabelProviderListener arg0) {
      listener = null;
    }
  }

  class NewComponentAction extends Action {
    /**
     * Constructor for NewPropertyAction
     */
    protected NewComponentAction() {
      super();
      setText("New");
      setToolTipText("Create a new Contained Component");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginComponentSpecification spec = getSpec();
      String newC = "component";
      if (spec.getComponent(newC) != null) {
        int counter = 0;
        while (spec.getComponent(newC + counter) != null) {
          counter++;
        }
        newC = newC + counter;
      }
      PluginContainedComponent newComponent = new PluginContainedComponent();
      newComponent.setType("fill in type");
      spec.setComponent(newC, newComponent);
      forceDirty();
      update();
      setSelection(newC);
      updateSelection = false;
    }

  }

  class CopyToAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected CopyToAction() {
      super();
      setText("Copy To");
      setToolTipText("copy this contained component \n and all its bindings to another component");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      CHolder holder = (CHolder) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
      IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(getModel().getUnderlyingStorage());
      ChooseComponentDialog dialog =
        new ChooseComponentDialog(
          newButton.getShell(),
          jproject,
          "Copy To",
          "Choose a target for the copy",
          true,
          TapestryLookup.ACCEPT_COMPONENTS | TapestryLookup.WRITEABLE);
      dialog.create();
      if (dialog.open() == dialog.OK) {
        try {
          String chosen = dialog.getResultComponent();
          TapestryComponentModel target = TapestryPlugin.getTapestryModelManager().findComponent(chosen, getModel());
          if (target == null) {
            throw new Exception("could not find '" + chosen + "'");
          }
          if (!target.isLoaded()) {
            target.load();
          }
          SpindleMultipageEditor targetEditor = (SpindleMultipageEditor) Utils.getEditorFor(target);
          if (targetEditor != null && targetEditor.isDirty()) {
            RequiredSaveEditorAction saver = new RequiredSaveEditorAction(targetEditor);
            if (!saver.save()) {
              return;
            }
            target = (TapestryComponentModel) targetEditor.getModel();
          }
          if (!target.isLoaded()) {
            parseError();
            return;
          }
          Utils.copyContainedComponentTo(holder.id, holder.component, target);
          if (targetEditor == null) {
            TapestryPlugin.openTapestryEditor(target);
          }
        } catch (Exception e) {
          e.printStackTrace();
          TapestryPlugin.getDefault().logException(e);
        }
      }
    }

    private void parseError() {
      Status status =
        new Status(
          IStatus.ERROR,
          TapestryPlugin.getDefault().getPluginId(),
          IStatus.OK,
          "Abort, target component is has parse errors",
          null);
      ErrorDialog.openError(TapestryPlugin.getDefault().getActiveWorkbenchShell(), null, null, status);
    }

  }

  class CopyComponentAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected CopyComponentAction() {
      super();
      setText("Copy Of");
      setToolTipText("make a copy of the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      CHolder holder = (CHolder) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
      if (holder != null && holder.component.getCopyOf() == null) {
        updateSelection = true;
        PluginComponentSpecification spec = getSpec();
        String copyOfName = "copy-of-" + holder.id;
        if (spec.getComponent(copyOfName) != null) {
          int counter = 0;
          while (spec.getComponent(copyOfName + counter) != null) {
            counter++;
          }
          copyOfName = copyOfName + counter;
        }
        PluginContainedComponent copy = new PluginContainedComponent();
        copy.setCopyOf(holder.id);
        spec.addComponent(copyOfName, copy);
        forceDirty();
        update();
        setSelectedComponent(copy);
        updateSelection = false;
      }
    }
  }

  class DeleteComponentAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected DeleteComponentAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      CHolder holder = (CHolder) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
      if (holder != null) {
        PluginComponentSpecification spec = getSpec();
        PluginContainedComponent prev = findPrevious(holder);
        spec.removeComponent(holder.id);
        forceDirty();
        update();
        if (prev != null) {
          setSelectedComponent(prev);
        } else {
          selectFirst();
        }
      }
      updateSelection = false;
    }

  }

  protected class ContainedComponentContentProvider implements ITreeContentProvider {
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
      return containedComponentHolders.toArray();
    }
    public void dispose() {
    }
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
    }
  }

  protected class CHolder implements IAdaptable, IPropertySource, IPropertySourceProvider {

    private String id;
    private PluginContainedComponent component;

    public CHolder(String id, PluginContainedComponent component) {
      super();
      this.id = id;
      this.component = component;
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
        setSelectedComponent(component);
        return;
      }
      if ("id".equals(key)) {
        String oldId = id;
        String newId = (String) value;
        if ("".equals(newId.trim())) {
          newId = oldId;
        } else if (alreadyHasId(newId)) {
          newId = newId + "Copy";
          PluginContainedComponent copy = copy();
          getSpec().addComponent(newId, copy);
          forceDirty();
          update();
          setSelectedComponent(copy);
          return;
        }
        id = newId;
        getSpec().removeComponent(oldId);
      } else if ("type".equals(key)) {
        component.setType((String) value);
      } else if ("copy-of".equals(key)) {
        if (!"".equals(((String) value).trim())) {
          component.setCopyOf((String) value);
        }
      }
      getSpec().setComponent(id, component);
      forceDirty();
      update();
      setSelectedComponent(component);
    }

    private PluginContainedComponent copy() {
      PluginContainedComponent result = new PluginContainedComponent();
      if (component.getCopyOf() != null) {
        result.setCopyOf(component.getCopyOf());
      } else {
        result.setType(component.getType());
      }
      return result;
    }

    public boolean isPropertySet(Object key) {
      if ("id".equals(key)) {
        return true;
      } else if ("type".equals(key)) {
        return component.getType() != null;
      } else if ("copy-of".equals(key)) {
        return component.getCopyOf() != null;
      } else {
        return true;
      }
    }

    public Object getPropertyValue(Object key) {
      if ("id".equals(key)) {
        return id;
      } else if ("type".equals(key)) {
        return component.getType();
      } else if ("copy-of".equals(key)) {
        return component.getCopyOf();
      }
      return null;
    }

    private IPropertyDescriptor[] normal =
      new IPropertyDescriptor[] {
        new TextPropertyDescriptor("id", "ID"),
        new ComponentTypeDialogPropertyDescriptor(
          "type",
          "Type",
          "Choose a Tapestry Component",
          "For now, to use aliases you can't use this dialog, exit then type the alias you wish",
          getModel()),
        };

    private IPropertyDescriptor[] copyof =
      new IPropertyDescriptor[] {
        new TextPropertyDescriptor("id", "ID"),
        new TextPropertyDescriptor("copy-of", "Copy Of"),
        };

    public IPropertyDescriptor[] getPropertyDescriptors() {
      if (component.getCopyOf() == null) {
        return normal;
      } else {
        return copyof;
      }
    }

    public Object getEditableValue() {
      return id;
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
      ContainedComponentLabelProvider provider =
        (ContainedComponentLabelProvider) ((TreeViewer) viewer).getLabelProvider();
      return super.compare(viewer, provider.getText(e1), provider.getText(e2));
    }
  }

}