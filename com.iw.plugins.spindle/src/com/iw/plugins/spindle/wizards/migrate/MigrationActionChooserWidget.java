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



package com.iw.plugins.spindle.wizards.migrate;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
import com.iw.plugins.spindle.ui.migrate.MigrationContext;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MigrationActionChooserWidget extends Viewer implements ICheckStateListener {

  private MigrationContext context;
  private CheckboxTreeAndList widget;

  private Control control;

  private int width = -1;

  private int height = -1;

  private int style = SWT.NULL;

  private RootNode rootObject;

  public MigrationActionChooserWidget(MigrationContext context, int width, int height, int style) {
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
        fireSelectionChanged(new SelectionChangedEvent(MigrationActionChooserWidget.this, getSelection()));
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
    return context;
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
    this.context = (MigrationContext) context;
    widget.setRoot(context);
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setSelection(ISelection, boolean)
   */
  public void setSelection(ISelection selection, boolean reveal) {

  }

  public void checkStateChanged(CheckStateChangedEvent event) {

    fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));

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

  class ActionsNode extends Node {

    Object[] listContents;
    Object[] children;

    public ActionsNode(Object principal, Object parent) {
      super(principal, parent);
      listContents = new Object[] { new Integer(context.MIGRATE_DTD)};
      children = new Object[] { new OptionalActions(context, this)};
    }
    public Object[] getListContents() {
      return listContents;
    }

    public Object[] getChildren() {
      return children;
    }

    public Object[] getContents() {
      return children;
    }

    public boolean hasChildren() {
      return true;
    }

    public String toString() {
      return "Actions";
    }

  }

  class OptionalActions extends LeafNode {

    Object[] listContents;

    public OptionalActions(Object principal, Object parent) {
      super(principal, parent);
      listContents =
        new Object[] {
          new Integer(context.MIGRATE_COMPONENT_ALIASES),
          new Integer(context.MIGRATE_UPGRADE_PAGES)};
    }
    public Object[] getListContents() {
      return listContents;
    }

    public String toString() {
      return "Optional Actions";
    }

  }

  class RootNode extends Node {

    Object[] children = null;

    public RootNode(MigrationContext context) {

      children = new Object[] { new ActionsNode(null, this)};

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

  class WidgetLabelProvider extends LabelProvider {

    Image nodeImage = TapestryImages.getSharedImage("forward.gif");

    Image migrate = TapestryImages.getSharedImage("migrate16_dark.gif");

    /**
    * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
    */
    public Image getImage(Object element) {

      if (element instanceof Node) {

        return nodeImage;

      }

      return migrate;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
     */
    public String getText(Object element) {

      if (element instanceof Integer) {

        String result = element.toString();

        int value = ((Integer) element).intValue();

        switch (value) {

          case MigrationContext.MIGRATE_DTD :
            result = "(Required) DTD Version -> 1.3";
            break;

          case MigrationContext.MIGRATE_COMPONENT_ALIASES :
            result = "Fix Contained Components using paths as type";
            break;

          case MigrationContext.MIGRATE_UPGRADE_PAGES :
            result = "Change .jwc files to .page files as appropriate";
            break;
        }

        return result;

      }
      return element.toString();
    }

  }

}
