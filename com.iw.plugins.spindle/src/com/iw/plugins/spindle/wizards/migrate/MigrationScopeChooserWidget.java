package com.iw.plugins.spindle.wizards.migrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.CheckboxTreeAndList;
import com.iw.plugins.spindle.ui.TapestryStorageLabelProvider;
import com.iw.plugins.spindle.ui.migrate.MigrationContext;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MigrationScopeChooserWidget extends Viewer implements ICheckStateListener {

  private MigrationContext context;
  private CheckboxTreeAndList widget;

  private Control control;

  private int width = -1;

  private int height = -1;

  private int style = SWT.NULL;

  private RootNode rootObject;

  public MigrationScopeChooserWidget(MigrationContext context, int width, int height, int style) {
    super();
    this.context = context;
    this.width = width;
    this.height = height;
    this.style = style;
    rootObject = new RootNode(context);
    rootObject.getChildren();
    rootObject.getContents();

  }

  public Control createControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NULL);

    GridLayout layout = new GridLayout();
    layout.marginHeight = 5;
    layout.marginWidth = 0;
    container.setLayout(layout);
    container.setLayoutData(new GridData(GridData.FILL_BOTH));

    WidgetLabelProvider labelProvider = new WidgetLabelProvider();
    TreeContentProvider treeProvider = new TreeContentProvider();
    ListContentProvider listProvider = new ListContentProvider();

    //    Tree tree = new Tree(container, SWT.NULL);
    //    tree.setLayoutData(new GridData(GridData.FILL_BOTH));
    //
    //    TreeViewer viewer = new TreeViewer(tree);
    //    viewer.setContentProvider(treeProvider);
    //    viewer.setLabelProvider(labelProvider);
    //    viewer.setInput(rootObject);

    widget =
      new CheckboxTreeAndList(
        container,
        rootObject,
        treeProvider,
        labelProvider,
        listProvider,
        labelProvider,
        style,
        width,
        height);

    this.control = container;

    widget.aboutToOpen();

    widget.setAllSelections(true);

    widget.addCheckStateListener(this);

    return control;

  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#getControl()
   */
  public Control getControl() {
    return control;

  }

  /**
   * @see org.eclipse.jface.viewers.IInputProvider#getInput()
   */
  public Object getInput() {
    return context;
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection() {

    Iterator iterator = widget.getAllCheckedListItems();

    if (!iterator.hasNext()) {

      return new StructuredSelection();

    }

    ArrayList list = new ArrayList();

    while (iterator.hasNext()) {

      list.add(iterator.next());

    }

    return new StructuredSelection(list.toArray());
  }

  // a JWC that's not defined in the App/Library and has DTD < 1.3
  public List getUndefinedFiles() {

    ArrayList result = new ArrayList();
    List selected = ((IStructuredSelection) getSelection()).toList();
    if (!selected.isEmpty()) {

      Object[] rootChildren = rootObject.getChildren();
      ProjectNode pnode = (ProjectNode)rootChildren[0];
      Object [] projectStorages = pnode.getListContents();
      
      
      if (projectStorages.length != 0) {

        for (int i = 0; i < projectStorages.length; i++) {

          result.add(projectStorages[i]);
        }

      }
    }

    return result;

  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#refresh()
   */
  public void refresh() {
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setInput(Object)
   */
  public void setInput(Object input) {
    this.context = (MigrationContext) context;
    widget.setRoot(context);
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setSelection(ISelection, boolean)
   */
  public void setSelection(ISelection selection, boolean reveal) {
  }

  private Object[] empty = new Object[0];

  abstract class Node {

    public Object principal;
    private Object parent;

    public Node() {
    }

    public Node(Object principal, Object parent) {

      this.principal = principal;
      this.parent = parent;

    }

    public Object getParent() {

      return parent;

    }

    public abstract Object[] getChildren();

    public abstract Object[] getContents();

    public abstract boolean hasChildren();

    public abstract Object[] getListContents();

    public String toString() {
      return "Node";
    }

  }

  class ProjectNode extends Node {

    Object[] children = null;
    Object[] listContents = null;

    /**
     * Constructor for ProjectNode.
     * @param principal
     * @param parent
     */
    public ProjectNode(Object principal, Object parent) {
      super(principal, parent);
    }

    private void findChildren() {

      TapestryProjectModelManager mgr = context.getModelManager();

      List allModels = mgr.getAllModels(null);
      List definedComponents =
        mgr.getAllComponentModelsDefinedIn(context.getContextModel(), context.getLookup());
      List definedPages =
        mgr.getAllPageModelsDefinedIn(context.getContextModel(), context.getLookup());

      allModels.remove(context.getContextModel());
      allModels.removeAll(definedComponents);
      allModels.removeAll(definedPages);

      if (allModels.isEmpty()) {

        listContents = empty;

      } else {

        listContents = new Object[allModels.size()];

        for (int i = 0; i < listContents.length; i++) {
          ITapestryModel element = (ITapestryModel) allModels.get(i);
          listContents[i] = element.getUnderlyingStorage();
        }

      }

      children = new Object[1];
      children[0] =
        new LibraryNode(context.getContextModel(), this, definedComponents, definedPages);

    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getChildren()
     */
    public Object[] getChildren() {
      if (children == null) {

        findChildren();

      }

      return children;
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getContents()
     */
    public Object[] getContents() {
      return getChildren();
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getListContents()
     */
    public Object[] getListContents() {
      return listContents;
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#hasChildren()
     */
    public boolean hasChildren() {
      return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return context.getModelManager().getProject().getName();
    }

  }

  class RootNode extends Node {

    Object[] children = null;

    public RootNode(MigrationContext context) {

      children = new Object[] { new ProjectNode(null, this)};

    }

    public Object[] getChildren() {

      return children;
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getContents()
     */
    public Object[] getContents() {

      return getChildren();

    }

    public Object[] getListContents() {

      return empty;

    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#hasChildren()
     */
    public boolean hasChildren() {
      return true;
    }

  }

  class LibraryNode extends Node {

    Object[] children = null;

    /**
     * Constructor for LibraryNode.
     */
    public LibraryNode(
      TapestryLibraryModel model,
      Object parent,
      List definedComponents,
      List definedPages) {
      super(model, parent);

      List childrenList = new ArrayList();

      if (!definedComponents.isEmpty()) {

        childrenList.add(new LibraryComponents(definedComponents, this));
      }

      if (!definedPages.isEmpty()) {

        childrenList.add(new LibraryPages(definedPages, this));
      }

      children = (Object[]) childrenList.toArray(new Object[childrenList.size()]);
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getChildren()
     */
    public Object[] getChildren() {

      return children;
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getContents()
     */
    public Object[] getContents() {
      return empty;
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#hasChildren()
     */
    public boolean hasChildren() {

      return children.length > 0;
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getListContents()
     */
    public Object[] getListContents() {
      return empty;
    }

    public String toString() {
      return ((TapestryLibraryModel) principal).getUnderlyingStorage().getName();
    }

  }

  abstract class LeafNode extends Node {

    /**
     * Constructor for LeafNode.
     * @param principal
     * @param parent
     */
    public LeafNode(Object principal, Object parent) {
      super(principal, parent);
    }

    public Object[] getChildren() {
      return empty;
    }

    public boolean hasChildren() {
      return false;
    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getContents()
     */
    public Object[] getContents() {
      return empty;
    }

  }

  class LibraryComponents extends LeafNode {

    Object[] listContents = empty;

    public LibraryComponents(List definedComponents, Node parent) {

      super(null, parent);

      if (!definedComponents.isEmpty()) {

        listContents = new Object[definedComponents.size()];

        for (int i = 0; i < listContents.length; i++) {
          ITapestryModel element = (ITapestryModel) definedComponents.get(i);
          listContents[i] = element.getUnderlyingStorage();

        }
      }

    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.MigrationScopeChooserWidget.Node#getListContents()
     */
    public Object[] getListContents() {
      return listContents;
    }

    public String toString() {
      return "Defined Components";
    }

  }

  class LibraryPages extends LeafNode {

    Object[] listContents = empty;

    public LibraryPages(List definedPages, Node parent) {

      super(null, parent);

      if (!definedPages.isEmpty()) {
        listContents = new Object[definedPages.size()];

        for (int i = 0; i < listContents.length; i++) {
          ITapestryModel element = (ITapestryModel) definedPages.get(i);
          listContents[i] = element.getUnderlyingStorage();

        }
      }

    }

    public Object[] getListContents() {

      return listContents;

    }

    public String toString() {
      return "Defined Pages";
    }

  }

  class TreeContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object parentElement) {

      return ((Node) parentElement).getChildren();

    }

    public Object getParent(Object element) {

      return ((Node) element).getParent();
    }

    public boolean hasChildren(Object element) {
      return ((Node) element).hasChildren();
    }

    public Object[] getElements(Object inputElement) {
      return ((Node) inputElement).getContents();
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

  }

  class ListContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object inputElement) {
      return ((Node) inputElement).getListContents();
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

  }

  class WidgetLabelProvider extends TapestryStorageLabelProvider {

    Image projectImage = TapestryImages.getSharedImage("project16.gif");
    Image contentsImage = TapestryImages.getSharedImage("property16.gif");

    /**
     * Constructor for WidgetLabelProvider.
     */
    public WidgetLabelProvider() {
      super(true);
    }

    /**
    * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
    */
    public Image getImage(Object element) {

      if (element instanceof IStorage) {

        return super.getImage(element);

      } else if (element instanceof ProjectNode) {

        return projectImage;

      } else if (element instanceof LibraryNode) {

        return super.getImage(
          ((TapestryLibraryModel) ((Node) element).principal).getUnderlyingStorage());

      } else if (element instanceof LibraryComponents || element instanceof LibraryPages) {

        return contentsImage;

      }

      return null;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
     */
    public String getText(Object element) {

      if (element instanceof IStorage) {
        return super.getText(element);
      }
      return element.toString();
    }

  }

  /**
   * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(CheckStateChangedEvent)
   */
  public void checkStateChanged(CheckStateChangedEvent event) {

    fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));

  }

}
