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
package com.iw.plugins.spindle.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.ui.editor.PropertiesAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.ui.IToolTipHelpProvider;
import com.iw.plugins.spindle.ui.IToolTipProvider;
import com.iw.plugins.spindle.ui.TreeViewerWithToolTips;
import com.iw.plugins.spindle.ui.descriptors.INeedsModelInitialization;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;
import com.iw.plugins.spindle.util.StringSorter;

public abstract class AbstractPropertySheetEditorSection
  extends SpindleFormSection
  implements ISelectionChangedListener, IModelChangedListener, IPropertySource, IPropertySourceProvider {

  private static DefaultTooltipProvider defaultTooltipProvider = new DefaultTooltipProvider();

  protected Button newButton;
  protected Button editButton;
  protected Button deleteButton;
  private TreeViewer treeViewer;
  private FormWidgetFactory factory;
  protected boolean updateNeeded;
  protected boolean updateSelection = false;

  private ITreeContentProvider contentProvider = new ContentProvider();
  private ITableLabelProvider labelProvider;
  private Action newAction;
  private Action deleteAction;
  private int boxHeight = -1;
  protected PropertiesAction pAction;

  protected boolean hasFocus;
  private Composite container;

  protected boolean isDTD12;

  protected List holderArray = new ArrayList();

  protected Point oldSize = new Point(0, 0);

  private boolean useToolTips = false;

  private IToolTipProvider toolTipProvider = defaultTooltipProvider;
  private IToolTipHelpProvider toolTipHelpProvider = defaultTooltipProvider;

  /**
   * Constructor for PropertySection
   */
  public AbstractPropertySheetEditorSection(SpindleFormPage page) {
    super(page);
    pAction = new PropertiesAction(page.getEditor());
    pAction.setText("Edit");
    pAction.setToolTipText("Edit the selected");
  }

  public AbstractPropertySheetEditorSection(SpindleFormPage page, int boxHeight) {
    this(page);
    this.boxHeight = boxHeight;
    setContentProvider(new ContentProvider());

  }

  public void setContentProvider(ITreeContentProvider provider) {
    contentProvider = provider;
  }

  public void setLabelProvider(ITableLabelProvider provider) {
    labelProvider = provider;
  }

  public void setNewAction(Action action) {
    newAction = action;
  }

  public void setDeleteAction(Action action) {
    deleteAction = action;
  }

  public Control getControl() {
    return treeViewer.getControl();
  }

  public TreeViewer getViewer() {
    return treeViewer;
  }

  public void update() {
    if (updateNeeded && canUpdate()) {
      update((BaseTapestryModel) getFormPage().getModel());
      if (holderArray != null && !holderArray.isEmpty()) {
        layout();
      }
    }
    updateNeeded = false;
  }

  protected abstract void update(BaseTapestryModel model);

  public void setInput(Object obj) {
    treeViewer.setInput(obj);
  }

  public void initialize(Object input) {
    BaseTapestryModel model = (BaseTapestryModel) input;
    boolean isEditable = model.isEditable();
    if (newButton != null) {
      newButton.setEnabled(isEditable);
    }
    if (deleteButton != null) {
      deleteAction.setEnabled(isEditable);
    }
    if (editButton != null) {
      editButton.setEnabled(isEditable);
    }
    if (deleteButton != null) {
      deleteButton.setEnabled(isEditable);
    }
    updateNeeded = true;
    if (canUpdate()) {
      update();
    }

    String DTDVersion = model.getDTDVersion();
    isDTD12 = DTDVersion != null && DTDVersion.equals("1.2");

    if (useToolTips) {
      TreeViewerWithToolTips viewer = (TreeViewerWithToolTips) treeViewer;
      viewer.setToolTipHelpProvider(toolTipHelpProvider);
      viewer.setToolTipProvider(toolTipProvider);
    }

    model.addModelChangedListener(this);
  }

  public Composite createClientContainer(Composite parent, FormWidgetFactory factory) {
    this.factory = factory;
    container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    container.setLayout(layout);

    Control control = createTree(container, factory);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 200;
    if (boxHeight != -1) {
      gd.heightHint = boxHeight;
    }
    control.setLayoutData(gd);

    Composite buttonContainer = factory.createComposite(container);
    layout = new GridLayout();
    layout.marginHeight = 0;
    buttonContainer.setLayout(layout);
    gd = new GridData(GridData.FILL_VERTICAL);
    buttonContainer.setLayoutData(gd);

    createButtons(buttonContainer, factory);

    factory.paintBordersFor(container);
    return container;
  }

  protected void createButtons(Composite buttonContainer, FormWidgetFactory factory) {
    newButton = factory.createButton(buttonContainer, "New", SWT.PUSH);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    newButton.setLayoutData(gd);
    newButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleNew();
      }
    });
    editButton = factory.createButton(buttonContainer, "Edit", SWT.PUSH);
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    editButton.setLayoutData(gd);
    editButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleEdit();
      }
    });
    if (deleteAction != null) {
      deleteButton = factory.createButton(buttonContainer, "Delete", SWT.PUSH);
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      deleteButton.setLayoutData(gd);
      deleteButton.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          handleDelete();
        }
      });
    }
  }

  private Tree createTree(Composite container, FormWidgetFactory factory) {
    Tree tree = factory.createTree(container, SWT.NULL);

    MenuManager popupMenuManager = new MenuManager();
    IMenuListener listener = new IMenuListener() {
      public void menuAboutToShow(IMenuManager mng) {
        fillContextMenu(mng);
      }
    };
    popupMenuManager.setRemoveAllWhenShown(true);
    popupMenuManager.addMenuListener(listener);
    Menu menu = popupMenuManager.createContextMenu(tree);
    tree.setMenu(menu);

    if (useToolTips) {

      TreeViewerWithToolTips viewer = new TreeViewerWithToolTips(tree);
      viewer.setToolTipProvider(toolTipProvider);
      viewer.setToolTipHelpProvider(toolTipHelpProvider);
      treeViewer = viewer;

    } else {

      treeViewer = new TreeViewer(tree);
    }

    treeViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        handleEdit();
      }
    });
    treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
    treeViewer.setContentProvider(contentProvider);
    treeViewer.setLabelProvider(labelProvider);
    treeViewer.addSelectionChangedListener(this);
    treeViewer.setSorter(new TreeViewSorter());
    treeViewer.getControl().addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent event) {
        hasFocus = true;
      }
      public void focusLost(FocusEvent event) {
        hasFocus = false;
      }
    });
    return tree;
  }

  public void setSelection(IStructuredSelection selection) {
    treeViewer.setSelection(selection);
  }

  protected void selectFirst() {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      ArrayList list = new ArrayList();
      list.add(items[0]);
      setSelection(new JavaListSelectionProvider(list));
    }
  }

  protected ITreeContentProvider getContentProvider() {
    return this.contentProvider;
  }

  protected Object getSelected() {
    return ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
  }

  public ITapestryModel getModel() {
    return (ITapestryModel) getFormPage().getModel();
  }

  public boolean isModelEditable() {
    return ((BaseTapestryModel) getModel()).isEditable();
  }

  public void setFocus() {
    treeViewer.getTree().setFocus();
    getFormPage().setSelection(treeViewer.getSelection());
  }

  protected void updateButtons(Object selected) {
    boolean isEditable = isModelEditable();
    newButton.setEnabled(isEditable);
    if (deleteButton != null) {
      deleteButton.setEnabled(isEditable && selected != null);
    }
  }

  protected void fillContextMenu(IMenuManager manager) {
    ISelection selection = getSelection();
    final Object object = ((IStructuredSelection) selection).getFirstElement();
    boolean isEditable = ((BaseTapestryModel) getFormPage().getModel()).isEditable();
    if (isEditable) {
      manager.add(newAction);
      if (object != null) {
        manager.add(new Separator());
        manager.add(deleteAction);

      }
    }
    manager.add(new Separator());
    pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  protected ISelection getSelection() {
    return treeViewer.getSelection();
  }

  protected void handleNew() {
    newAction.run();
  }

  protected void handleEdit() {
    IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
    if (!selection.isEmpty()) {
      Object object = ((IStructuredSelection) selection).getFirstElement();
      fireSelectionNotification(object);
      getFormPage().setSelection(selection);
      pAction.run();
    }
  }

  protected void handleDelete() {
    if (deleteAction != null) {
      deleteAction.run();
    }
  }

  protected void forceDirty() {
    setDirty(true);
    updateNeeded = true;
    IModel model = (IModel) getFormPage().getModel();
    if (model instanceof IEditable) {
      IEditable editable = (IEditable) model;
      editable.setDirty(true);
      getFormPage().getEditor().fireSaveNeeded();
    }
  }

  public void selectionChanged(SelectionChangedEvent event) {
    Object object = null;
    if (!event.getSelection().isEmpty()) {
      ISelection selection = event.getSelection();
      if (selection instanceof IStructuredSelection) {
        object = ((IStructuredSelection) selection).getFirstElement();
      }
    }
    fireSelectionNotification(object);
    if (hasFocus || updateSelection) {
      getFormPage().setSelection(new StructuredSelection(this));
    }
    updateButtons(object);
  }

  protected String findPrevious(String name) {
    if (name != null) {
      Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
      final ArrayList list = new ArrayList();
      for (int i = 0; i < items.length; i++) {
        if (((IIdentifiable) items[i]).getIdentifier().equals(name) && i >= 1) {
          return ((IIdentifiable) items[i - 1]).getIdentifier();
        }
      }
    }
    return null;
  }

  protected boolean alreadyHasIdentifer(String name) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        IIdentifiable holder = (IIdentifiable) items[i];
        if (holder.getIdentifier().equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  public void setSelection(IIdentifiable identifiable) {
    setSelection(identifiable.getIdentifier());
  }

  public void setSelection(String name) {
    Object[] items = ((ITreeContentProvider) getContentProvider()).getElements(null);
    if (items != null && items.length >= 1) {
      for (int i = 0; i < items.length; i++) {
        IIdentifiable holder = (IIdentifiable) items[i];
        if (holder.getIdentifier().equals(name)) {
          ArrayList list = new ArrayList();
          list.add(items[i]);
          setSelection(new JavaListSelectionProvider(list));
          break;
        }
      }
    }
  }

  public boolean isSelected(Object object) {
    IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
    if (selection == null || selection.isEmpty()) {
      return false;
    }
    return object == selection.getFirstElement();
  }

  protected IPropertySource getSelectedPropertySource() {
    return (IPropertySource) getSelected();
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
   */
  public Object getEditableValue() {
    IPropertySource selected = getSelectedPropertySource();
    if (selected != null) {
      return selected.getEditableValue();
    }
    return null;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
   */
  public IPropertyDescriptor[] getPropertyDescriptors() {
    IPropertySource selected = getSelectedPropertySource();

    if (selected != null) {

      IPropertyDescriptor[] descriptors = selected.getPropertyDescriptors();

      for (int i = 0; i < descriptors.length; i++) {

        if (descriptors[i] instanceof INeedsModelInitialization) {

          ((INeedsModelInitialization) descriptors[i]).initialize(getModel());
        }
      }

      return descriptors;

    }
    return new IPropertyDescriptor[0];
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
   */
  public Object getPropertyValue(Object id) {
    IPropertySource selected = getSelectedPropertySource();
    if (selected != null) {
      return selected.getPropertyValue(id);
    }
    return null;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
   */
  public boolean isPropertySet(Object id) {
    IPropertySource selected = getSelectedPropertySource();
    if (selected != null) {
      return selected.isPropertySet(id);
    }
    return false;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(Object)
   */
  public void resetPropertyValue(Object id) {
    IPropertySource selected = getSelectedPropertySource();
    if (selected != null) {
      selected.resetPropertyValue(id);
    }
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(Object, Object)
   */
  public void setPropertyValue(Object id, Object value) {
    if (!isModelEditable()) {
      updateNeeded = true;
      update();
      setSelection(((IIdentifiable) getSelected()).getIdentifier());
      return;
    }
    IPropertySource selected = getSelectedPropertySource();
    if (selected != null) {
      selected.setPropertyValue(id, value);
    }

    updateUI();

    // we may have changed the identifier, must ensure that
    // which was selected is still selected!
    setSelection((IIdentifiable) selected);

  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySourceProvider#getPropertySource(Object)
   */
  public IPropertySource getPropertySource(Object object) {
    return this;
  }

  protected void updateUI() {
    forceDirty();
    updateNeeded = true;
    update();
  }

  protected class TreeViewSorter extends StringSorter {

    protected TreeViewSorter() {
      super();
    }

    public int compare(Viewer viewer, Object e1, Object e2) {
      ITableLabelProvider provider = (ITableLabelProvider) ((TreeViewer) viewer).getLabelProvider();
      return super.compare(viewer, provider.getColumnText(e1, 1), provider.getColumnText(e2, 1));
    }
  }

  class ContentProvider implements ITreeContentProvider {
    public Object[] getElements(Object object) {
      return holderArray.toArray();
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

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

  }
  /**
   * Returns the useToolTips.
   * @return boolean
   */
  public boolean isUseToolTips() {
    return useToolTips;
  }

  /**
   * Sets the useToolTips.
   * @param useToolTips The useToolTips to set
   */
  public void setUseToolTips(boolean useToolTips) {

    if (treeViewer == null) {
      this.useToolTips = useToolTips;
    }
  }

  /**
   * Returns the tooltipHelpProvider.
   * @return IToolTipHelpProvider
   */
  public IToolTipHelpProvider getToolTipHelpProvider() {
    if (useToolTips) {

      if (treeViewer != null) {

        return ((TreeViewerWithToolTips) treeViewer).getToolTipHelpProvider();

      } else {

        return toolTipHelpProvider;
      }
    }
    return null;
  }

  /**
   * Returns the tooltipProvider.
   * @return IToolTipProvider
   */
  public IToolTipProvider getToolTipProvider() {
    if (useToolTips) {
      return toolTipProvider;
    }
    return null;
  }

  /**
   * Sets the tooltipHelpProvider.
   * @param tooltipHelpProvider The tooltipHelpProvider to set
   */
  public void setToolTipHelpProvider(IToolTipHelpProvider tooltipHelpProvider) {
    if (useToolTips) {

      if (treeViewer != null) {

        ((TreeViewerWithToolTips) treeViewer).setToolTipHelpProvider(tooltipHelpProvider);

      } else if (tooltipHelpProvider != null) {

        this.toolTipHelpProvider = tooltipHelpProvider;
      }
    }
  }

  /**
   * Sets the tooltipProvider.
   * @param tooltipProvider The tooltipProvider to set
   */
  public void setToolTipProvider(IToolTipProvider tooltipProvider) {
    if (useToolTips) {

      if (treeViewer != null) {

        ((TreeViewerWithToolTips) treeViewer).setToolTipProvider(tooltipProvider);

      } else if (tooltipProvider != null) {

        this.toolTipProvider = tooltipProvider;
      }
    }
  }

  public static class DefaultTooltipProvider implements IToolTipHelpProvider, IToolTipProvider {

    /**
     * @see com.iw.plugins.spindle.ui.IToolTipHelpProvider#getHelp(Object)
     */
    public Object getHelp(Object object) {
      return "Tooltip provider not supplied!";
    }

    /**
     * @see com.iw.plugins.spindle.ui.IToolTipProvider#getToolTipText(Object)
     */
    public String getToolTipText(Object object) {
      return "Tooltip provider not supplied!";
    }

    /**
     * @see com.iw.plugins.spindle.ui.IToolTipProvider#getToolTipImage(Object)
     */
    public Image getToolTipImage(Object object) {
      return null;
    }

  }

}