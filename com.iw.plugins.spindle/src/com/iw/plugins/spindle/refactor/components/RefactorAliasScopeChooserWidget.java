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

package com.iw.plugins.spindle.refactor.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.ui.CheckboxTreeAndList;
import com.iw.plugins.spindle.ui.TapestryStorageLabelProvider;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class RefactorAliasScopeChooserWidget extends Viewer implements ICheckStateListener {

  List affectedComponents;

  private CheckboxTreeAndList widget;

  private Control control;

  private int width = -1;

  private int height = -1;

  private int style = SWT.NULL;

  private RootNode rootObject;

  public RefactorAliasScopeChooserWidget(List affectedComponents, int width, int height, int style) {
    super();
    this.affectedComponents = affectedComponents;
    this.width = width;
    this.height = height;
    this.style = style;
    rootObject = new RootNode(affectedComponents);


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
        height) {

      public void buttonPressed(int buttonType) {
        super.buttonPressed(buttonType);
        fireSelectionChanged(new SelectionChangedEvent(RefactorAliasScopeChooserWidget.this, getSelection()));
      }

    };

    this.control = container;

    widget.aboutToOpen();

    widget.setAllSelections(true);

    widget.addCheckStateListener(this);
    widget.expandAll();

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
    return affectedComponents;
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection() {

    Iterator iterator = widget.getAllCheckedListItems().iterator();

    if (!iterator.hasNext()) {

      return new StructuredSelection();

    }

    ArrayList list = new ArrayList();

    while (iterator.hasNext()) {

      list.add(iterator.next());

    }

    return new StructuredSelection(list.toArray());
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
    this.affectedComponents = (List) affectedComponents;
    widget.setRoot(affectedComponents);
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

    List children = new ArrayList();
    HashMap temp = new HashMap();

    /**
     * Constructor for ProjectNode.
     * @param principal
     * @param parent
     */
    public ProjectNode(Object principal, Object parent) {
      super(principal, parent);
    }

    private void addAffectedComponent(RefactorComponentAliasWizard.AffectedComponentHolder affected) {

      IPackageFragment fragment = affected.fragment;
      PackageNode packNode = (PackageNode) temp.get(fragment);
      if (packNode == null) {

        packNode = new PackageNode(fragment, this);
        temp.put(fragment, packNode);
        children.add(packNode);

      }

      packNode.addAffectedComponent(affected);

    }

    public Object[] getChildren() {
      return children.toArray();
    }

    public Object[] getContents() {
      return getChildren();
    }

    public Object[] getListContents() {
      return empty;
    }

    public boolean hasChildren() {
      return true;
    }

    public String toString() {
      return ((IProject) principal).getName();
    }

  }

  class RootNode extends Node {

    Object[] children = null;

    public RootNode(List affectedComponents) {
      buildChildren(affectedComponents);
    }

    public Object[] getChildren() {
      return children;
    }

    public Object[] getContents() {
      return getChildren();
    }

    public Object[] getListContents() {
      return empty;
    }

    private void buildChildren(List affectedComponents) {

      Map projectMap = new HashMap();
      List children = new ArrayList();

      for (Iterator iter = affectedComponents.iterator(); iter.hasNext();) {
        RefactorComponentAliasWizard.AffectedComponentHolder element =
          (RefactorComponentAliasWizard.AffectedComponentHolder) iter.next();

        IResource resource = (IResource) element.model.getUnderlyingStorage();

        IProject project = resource.getProject();
        ProjectNode pnode = (ProjectNode) projectMap.get(project);

        if (pnode == null) {

          pnode = new ProjectNode(project, this);
          projectMap.put(project, pnode);
          children.add(pnode);

        }
        pnode.addAffectedComponent(element);
      }

      this.children = children.toArray();
    }

    public boolean hasChildren() {
      return true;
    }

  }

  class PackageNode extends Node {

    List contents = new ArrayList();

    public PackageNode(IPackageFragment fragment, Object parent) {
      super(fragment, parent);
    }

    public void addAffectedComponent(RefactorComponentAliasWizard.AffectedComponentHolder affected) {
      contents.add(affected);
    }

    public Object[] getChildren() {

      return empty;
    }

    public Object[] getContents() {
      return empty;
    }

    public boolean hasChildren() {

      return false;
    }

    public Object[] getListContents() {
      return contents.toArray();
    }

    public String toString() {
      return ((IPackageFragment) principal).getElementName();
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
    JavaElementLabelProvider javaLabelProvider = new JavaElementLabelProvider();

    public WidgetLabelProvider() {
      super(true);
    }

    public Image getImage(Object element) {

      if (element instanceof RefactorComponentAliasWizard.AffectedComponentHolder) {

        IStorage storage = ((RefactorComponentAliasWizard.AffectedComponentHolder) element).model.getUnderlyingStorage();
        return super.getImage(storage);

      } else if (element instanceof ProjectNode) {

        return projectImage;

      } else if (element instanceof PackageNode) {

        return javaLabelProvider.getImage(((Node)element).principal);

      } 

      return null;
    }

    public String getText(Object element) {

      if (element instanceof RefactorComponentAliasWizard.AffectedComponentHolder) {

        IStorage storage = ((RefactorComponentAliasWizard.AffectedComponentHolder) element).model.getUnderlyingStorage();
        return super.getText(storage);
      }
      return element.toString();
    }

  }

  public void checkStateChanged(CheckStateChangedEvent event) {

    fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));

  }

}
