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



package com.iw.plugins.spindle.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class CheckboxTreeAndList
  implements ICheckStateListener, ISelectionChangedListener, ITreeViewerListener {

  public static final int DESELECT = 0;
  public static final int SELECT = 1;
  public static final int INVERT = 2;

  private Object root;
  private Object currentTreeSelection;
  private List expandedTreeNodes = new ArrayList();
  private Map checkedStateStore = new HashMap(9);
  private List whiteCheckedTreeItems = new ArrayList();
  private List listeners = new ArrayList();

  private ITreeContentProvider treeContentProvider;
  private IStructuredContentProvider listContentProvider;
  private ILabelProvider treeLabelProvider;
  private ILabelProvider listLabelProvider;

  // widgets
  private CheckboxTreeViewer treeViewer;
  private CheckboxTableViewer listViewer;
  /**
   *	Create an instance of this class.  Use this constructor if you wish to specify
   *	the width and/or height of the combined widget (to only hardcode one of the
   *	sizing dimensions, specify the other dimension's value as -1)
   *
   *	@param parent org.eclipse.swt.widgets.Composite
   *	@param style int
   *  	@param rootObject java.lang.Object
   *	@param childPropertyName java.lang.String
   *	@param parentPropertyName java.lang.String
   *	@param listPropertyName java.lang.String
   *	@param width int
   *	@param height int
   */
  public CheckboxTreeAndList(
    Composite parent,
    Object rootObject,
    ITreeContentProvider treeContentProvider,
    ILabelProvider treeLabelProvider,
    IStructuredContentProvider listContentProvider,
    ILabelProvider listLabelProvider,
    int style,
    int width,
    int height) {

    root = rootObject;
    this.treeContentProvider = treeContentProvider;
    this.listContentProvider = listContentProvider;
    this.treeLabelProvider = treeLabelProvider;
    this.listLabelProvider = listLabelProvider;
    createContents(parent, width, height, style);
  }
  /**
   * This method must be called just before this window becomes visible.
   */
  public void aboutToOpen() {
    determineWhiteCheckedDescendents(root);
    checkNewTreeElements(treeContentProvider.getElements(root));
    currentTreeSelection = null;

    //select the first element in the list
    Object[] elements = treeContentProvider.getElements(root);
    Object primary = elements.length > 0 ? elements[0] : null;
    if (primary != null) {
      treeViewer.setSelection(new StructuredSelection(primary));
    }
    treeViewer.getControl().setFocus();
  }
  /**
   *	Add the passed listener to self's collection of clients
   *	that listen for changes to element checked states
   *
   *	@param listener ICheckStateListener
   */
  public void addCheckStateListener(ICheckStateListener listener) {
    listeners.add(listener);
  }
  /**
   *	Add the receiver and all of it's ancestors to the checkedStateStore if they
   * are not already there.
   */
  private void addToHierarchyToCheckedStore(Object treeElement) {

    // if this tree element is already gray then its ancestors all are as well
    if (!checkedStateStore.containsKey(treeElement))
      checkedStateStore.put(treeElement, new ArrayList());

    Object parent = treeContentProvider.getParent(treeElement);
    if (parent != null)
      addToHierarchyToCheckedStore(parent);
  }
  /**
   *	Return a boolean indicating whether all children of the passed tree element
   *	are currently white-checked
   *
   *	@return boolean
   *	@param treeElement java.lang.Object
   */
  protected boolean areAllChildrenWhiteChecked(Object treeElement) {
    Object[] children = treeContentProvider.getChildren(treeElement);
    for (int i = 0; i < children.length; ++i) {
      if (!whiteCheckedTreeItems.contains(children[i]))
        return false;
    }

    return true;
  }
  /**
   *	Return a boolean indicating whether all list elements associated with
   *	the passed tree element are currently checked
   *
   *	@return boolean
   *	@param treeElement java.lang.Object
   */
  protected boolean areAllElementsChecked(Object treeElement) {
    List checkedElements = (List) checkedStateStore.get(treeElement);
    if (checkedElements == null) // ie.- tree item not even gray-checked
      return false;

    return getListItemsSize(treeElement) == checkedElements.size();
  }
  /**
   *	Iterate through the passed elements which are being realized for the first
   *	time and check each one in the tree viewer as appropriate
   */
  protected void checkNewTreeElements(Object[] elements) {
    for (int i = 0; i < elements.length; ++i) {
      Object currentElement = elements[i];
      boolean checked = checkedStateStore.containsKey(currentElement);
      treeViewer.setChecked(currentElement, checked);
      treeViewer.setGrayed(
        currentElement,
        checked && !whiteCheckedTreeItems.contains(currentElement));
    }
  }
  /**
  *	An item was checked in one of self's two views.  Determine which
  *	view this occurred in and delegate appropriately
  *
  *	@param event CheckStateChangedEvent
  */
  public void checkStateChanged(final CheckStateChangedEvent event) {

    //Potentially long operation - show a busy cursor
    BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
      public void run() {
        if (event.getCheckable().equals(treeViewer))
          treeItemChecked(event.getElement(), event.getChecked());
        else
          listItemChecked(event.getElement(), event.getChecked(), true);

        notifyCheckStateChangeListeners(event);
      }
    });
  }
  /**
   *	Lay out and initialize self's visual components.
   *
   *	@param parent org.eclipse.swt.widgets.Composite
   *	@param width int
   *	@param height int
   */
  protected void createContents(Composite parent, int width, int height, int style) {
    // group pane
    Composite composite = new Composite(parent, style);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.makeColumnsEqualWidth = true;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));

    createTreeViewer(composite, width / 2, height);
    createListViewer(composite, width / 2, height);

    initialize();
  }
  /**
   *	Create this group's list viewer.
   */
  protected void createListViewer(Composite parent, int width, int height) {
    listViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
    GridData data = new GridData(GridData.FILL_BOTH);
    data.widthHint = width;
    data.heightHint = height;
    listViewer.getTable().setLayoutData(data);
    listViewer.setContentProvider(listContentProvider);
    listViewer.setLabelProvider(listLabelProvider);
    listViewer.addCheckStateListener(this);

    new Composite(parent, SWT.NULL);
    createButtonBar(parent);

  }
  /**
   *	Create this group's tree viewer.
   */
  protected void createTreeViewer(Composite parent, int width, int height) {
    Tree tree = new Tree(parent, SWT.CHECK | SWT.BORDER | SWT.SINGLE);
    GridData data = new GridData(GridData.FILL_BOTH);
    data.widthHint = width;
    data.heightHint = height;
    tree.setLayoutData(data);

    treeViewer = new CheckboxTreeViewer(tree);
    treeViewer.setContentProvider(treeContentProvider);
    treeViewer.setLabelProvider(treeLabelProvider);
    treeViewer.addTreeListener(this);
    treeViewer.addCheckStateListener(this);
    treeViewer.addSelectionChangedListener(this);
  }
  /**
   * Returns a boolean indicating whether the passed tree element should be
   * at LEAST gray-checked.  Note that this method does not consider whether
   * it should be white-checked, so a specified tree item which should be
   * white-checked will result in a <code>true</code> answer from this method.
   * To determine whether a tree item should be white-checked use method
   * #determineShouldBeWhiteChecked(Object).
   *
   * @param element java.lang.Object
   * @return boolean
   * @see #determineShouldBeWhiteChecked(java.lang.Object)
   */
  protected boolean determineShouldBeAtLeastGrayChecked(Object treeElement) {
    // if any list items associated with treeElement are checked then it
    // retains its gray-checked status regardless of its children
    List checked = (List) checkedStateStore.get(treeElement);
    if (checked != null && (!checked.isEmpty()))
      return true;

    // if any children of treeElement are still gray-checked then treeElement
    // must remain gray-checked as well
    Object[] children = treeContentProvider.getChildren(treeElement);
    for (int i = 0; i < children.length; ++i) {
      if (checkedStateStore.containsKey(children[i]))
        return true;
    }

    return false;
  }
  /**
   * Returns a boolean indicating whether the passed tree item should be
   * white-checked.
   *
   * @return boolean
   * @param treeElement java.lang.Object
   */
  protected boolean determineShouldBeWhiteChecked(Object treeElement) {
    return areAllChildrenWhiteChecked(treeElement) && areAllElementsChecked(treeElement);
  }
  /**
   *	Recursively add appropriate tree elements to the collection of
   *	known white-checked tree elements.
   *
   *	@param treeElement java.lang.Object
   */
  protected void determineWhiteCheckedDescendents(Object treeElement) {
    // always go through all children first since their white-checked
    // statuses will be needed to determine the white-checked status for
    // this tree element
    Object[] children = treeContentProvider.getElements(treeElement);
    for (int i = 0; i < children.length; ++i)
      determineWhiteCheckedDescendents(children[i]);

    // now determine the white-checked status for this tree element
    if (determineShouldBeWhiteChecked(treeElement))
      setWhiteChecked(treeElement, true);
  }
  /**
   * Cause the tree viewer to expand all its items
   */
  public void expandAll() {
    treeViewer.expandAll();
  }
  /**
   *	Answer a flat collection of all of the checked elements in the
   *	list portion of self
   *
   *	@return java.util.Vector
   */
  public List getAllCheckedListItems() {
    List result = new ArrayList();
    Iterator listCollectionsEnum = checkedStateStore.values().iterator();

    while (listCollectionsEnum.hasNext()) {
      Iterator currentCollection = ((List) listCollectionsEnum.next()).iterator();
      while (currentCollection.hasNext())
        result.add(currentCollection.next());
    }

    return result;
  }
  /**
   *	Answer a collection of all of the checked elements in the tree portion
   *	of self
   *
   *	@return java.util.Vector
   */
  public Set getAllCheckedTreeItems() {
    return checkedStateStore.keySet();
  }
  /**
   *	Answer the number of elements that have been checked by the
   *	user.
   *
   *	@return int
   */
  public int getCheckedElementCount() {
    return checkedStateStore.size();
  }
  /**
   *	Return a count of the number of list items associated with a
   *	given tree item.
   *
   *	@return int
   *	@param treeElement java.lang.Object
   */
  protected int getListItemsSize(Object treeElement) {
    Object[] elements = listContentProvider.getElements(treeElement);
    return elements.length;
  }
  /**
   * Get the table the list viewer uses.
   * @return org.eclipse.swt.widgets.Table
   */
  public Table getListTable() {
    return this.listViewer.getTable();
  }
  /**
   *	Logically gray-check all ancestors of treeItem by ensuring that they
   *	appear in the checked table
   */
  protected void grayCheckHierarchy(Object treeElement) {

    // if this tree element is already gray then its ancestors all are as well
    if (checkedStateStore.containsKey(treeElement))
      return; // no need to proceed upwards from here

    checkedStateStore.put(treeElement, new ArrayList());
    if (determineShouldBeWhiteChecked(treeElement)) {
      setWhiteChecked(treeElement, true);
    }
    Object parent = treeContentProvider.getParent(treeElement);
    if (parent != null)
      grayCheckHierarchy(parent);
  }
  /**
   *	Set the initial checked state of the passed list element to true.
   */
  public void initialCheckListItem(Object element) {
    Object parent = treeContentProvider.getParent(element);
    currentTreeSelection = parent;
    //As this is not done from the UI then set the box for updating from the selection to false 
    listItemChecked(element, true, false);
    updateHierarchy(parent);
  }
  /**
   *	Set the initial checked state of the passed element to true,
   *	as well as to all of its children and associated list elements
   */
  public void initialCheckTreeItem(Object element) {
    treeItemChecked(element, true);
  }
  /**
   *	Initialize this group's viewers after they have been laid out.
   */
  protected void initialize() {
    treeViewer.setInput(root);
  }
  /**
   *	Callback that's invoked when the checked status of an item in the list
   *	is changed by the user. Do not try and update the hierarchy if we are building the
   *  initial list.
   */
  protected void listItemChecked(
    Object listElement,
    boolean state,
    boolean updatingFromSelection) {
    List checkedListItems = (List) checkedStateStore.get(currentTreeSelection);

    if (state) {
      if (checkedListItems == null) {
        // since the associated tree item has gone from 0 -> 1 checked
        // list items, tree checking may need to be updated
        grayCheckHierarchy(currentTreeSelection);
        checkedListItems = (List) checkedStateStore.get(currentTreeSelection);
      }
      checkedListItems.add(listElement);
    } else {
      checkedListItems.remove(listElement);
      if (checkedListItems.isEmpty()) {
        // since the associated tree item has gone from 1 -> 0 checked
        // list items, tree checking may need to be updated
        ungrayCheckHierarchy(currentTreeSelection);
      }
    }

    if (updatingFromSelection)
      updateHierarchy(currentTreeSelection);
  }
  /**
   *	Notify all checked state listeners that the passed element has had
   *	its checked state changed to the passed state
   */
  protected void notifyCheckStateChangeListeners(CheckStateChangedEvent event) {
    Iterator listenersEnum = listeners.iterator();
    while (listenersEnum.hasNext())
       ((ICheckStateListener) listenersEnum.next()).checkStateChanged(event);
  }
  /**
   *Set the contents of the list viewer based upon the specified selected
   *tree element.  This also includes checking the appropriate list items.
   *
   *@param treeElement java.lang.Object
   */
  protected void populateListViewer(final Object treeElement) {
    listViewer.setInput(treeElement);
    List listItemsToCheck = (List) checkedStateStore.get(treeElement);

    if (listItemsToCheck != null) {
      Iterator listItemsEnum = listItemsToCheck.iterator();
      while (listItemsEnum.hasNext())
        listViewer.setChecked(listItemsEnum.next(), true);
    }
  }
  /**
   *	Remove the passed listener from self's collection of clients
   *	that listen for changes to element checked states
   *
   *	@param listener ICheckStateListener
   */
  public void removeCheckStateListener(ICheckStateListener listener) {
    listeners.remove(listener);
  }
  /**
   *	Handle the selection of an item in the tree viewer
   *
   *	@param selection ISelection
   */
  public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    Object selectedElement = selection.getFirstElement();
    if (selectedElement == null) {
      currentTreeSelection = null;
      listViewer.setInput(currentTreeSelection);
      return;
    }

    // ie.- if not an item deselection
    if (selectedElement != currentTreeSelection)
      populateListViewer(selectedElement);

    currentTreeSelection = selectedElement;
  }
  /**
   * Select or deselect all of the elements in the tree depending on the value of the selection
   * boolean. Be sure to update the displayed files as well.
   */
  public void setAllSelections(final boolean selection) {

    //Potentially long operation - show a busy cursor
    BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
      public void run() {
        setTreeChecked(root, selection);
        listViewer.setAllChecked(selection);
      }
    });
  }
  /**
   *	Set the list viewer's providers to those passed
   *
   *	@param contentProvider ITreeContentProvider
   *	@param labelProvider ILabelProvider
   */
  public void setListProviders(
    IStructuredContentProvider contentProvider,
    ILabelProvider labelProvider) {
    listViewer.setContentProvider(contentProvider);
    listViewer.setLabelProvider(labelProvider);
  }
  /**
   *	Set the sorter that is to be applied to self's list viewer
   */
  public void setListSorter(ViewerSorter sorter) {
    listViewer.setSorter(sorter);
  }
  /**
   * Set the root of the widget to be new Root. Regenerate all of the tables and lists from this
   * value.
   * @param newRoot 
   */
  public void setRoot(Object newRoot) {
    this.root = newRoot;
    initialize();
  }
  /**
   *	Set the checked state of the passed tree element appropriately, and
   *	do so recursively to all of its child tree elements as well
   */
  protected void setTreeChecked(Object treeElement, boolean state) {

    if (treeElement.equals(currentTreeSelection)) {
      listViewer.setAllChecked(state);
    }

    if (state) {
      Object[] listItems = listContentProvider.getElements(treeElement);
      List listItemsChecked = new ArrayList();
      for (int i = 0; i < listItems.length; ++i)
        listItemsChecked.add(listItems[i]);

      checkedStateStore.put(treeElement, listItemsChecked);
    } else
      checkedStateStore.remove(treeElement);

    setWhiteChecked(treeElement, state);
    treeViewer.setChecked(treeElement, state);
    treeViewer.setGrayed(treeElement, false);

    // now logically check/uncheck all children as well
    Object[] children = treeContentProvider.getChildren(treeElement);
    for (int i = 0; i < children.length; ++i) {
      setTreeChecked(children[i], state);
    }
  }
  /**
   *	Set the tree viewer's providers to those passed
   *
   *	@param contentProvider ITreeContentProvider
   *	@param labelProvider ILabelProvider
   */
  public void setTreeProviders(
    ITreeContentProvider contentProvider,
    ILabelProvider labelProvider) {
    treeViewer.setContentProvider(contentProvider);
    treeViewer.setLabelProvider(labelProvider);
  }
  /**
   *	Set the sorter that is to be applied to self's tree viewer
   */
  public void setTreeSorter(ViewerSorter sorter) {
    treeViewer.setSorter(sorter);
  }
  /**
   *	Adjust the collection of references to white-checked tree elements appropriately.
   *
   *	@param treeElement java.lang.Object
   *	@param isWhiteChecked boolean
   */
  protected void setWhiteChecked(Object treeElement, boolean isWhiteChecked) {
    if (isWhiteChecked) {
      if (!whiteCheckedTreeItems.contains(treeElement))
        whiteCheckedTreeItems.add(treeElement);
    } else
      whiteCheckedTreeItems.remove(treeElement);
  }
  /**
   *	Handle the collapsing of an element in a tree viewer
   */
  public void treeCollapsed(TreeExpansionEvent event) {
    // We don't need to do anything with this
  }
  /**
   *	Handle the expansionsion of an element in a tree viewer
   */
  public void treeExpanded(TreeExpansionEvent event) {

    Object item = event.getElement();

    // First see if the children need to be given their checked state at all.  If they've
    // already been realized then this won't be necessary
    if (!expandedTreeNodes.contains(item)) {
      expandedTreeNodes.add(item);
      checkNewTreeElements(treeContentProvider.getChildren(item));
    }
  }
  /**
   *  Callback that's invoked when the checked status of an item in the tree
   *  is changed by the user.
   */
  protected void treeItemChecked(Object treeElement, boolean state) {

    // recursively adjust all child tree elements appropriately
    setTreeChecked(treeElement, state);

    Object parent = treeContentProvider.getParent(treeElement);
    if (parent == null)
      return;

    // now update upwards in the tree hierarchy 
    if (state)
      grayCheckHierarchy(parent);
    else
      ungrayCheckHierarchy(parent);

    updateHierarchy(treeElement);
  }
  /**
   *	Logically un-gray-check all ancestors of treeItem iff appropriate.
   */
  protected void ungrayCheckHierarchy(Object treeElement) {
    if (!determineShouldBeAtLeastGrayChecked(treeElement))
      checkedStateStore.remove(treeElement);

    Object parent = treeContentProvider.getParent(treeElement);
    if (parent != null)
      ungrayCheckHierarchy(parent);
  }
  /**
   *	Set the checked state of self and all ancestors appropriately
   */
  protected void updateHierarchy(Object treeElement) {

    boolean whiteChecked = determineShouldBeWhiteChecked(treeElement);
    boolean shouldBeAtLeastGray = determineShouldBeAtLeastGrayChecked(treeElement);

    treeViewer.setChecked(treeElement, shouldBeAtLeastGray);
    setWhiteChecked(treeElement, whiteChecked);
    if (!whiteChecked)
      treeViewer.setGrayed(treeElement, shouldBeAtLeastGray);

    // proceed up the tree element hierarchy
    Object parent = treeContentProvider.getParent(treeElement);
    if (parent != null) {
      updateHierarchy(parent);
    }
  }
  /**
   * Update the selections of the tree elements in items to reflect the new
   * selections provided.
   * @param Map with keys of Object (the tree element) and values of List (the selected
   * list elements).
   */
  public void updateSelections(final Map items) {

    //Potentially long operation - show a busy cursor
    BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
      public void run() {
        Iterator keyIterator = items.keySet().iterator();

        //Update the store before the hierarchy to prevent updating parents before all of the children are done
        while (keyIterator.hasNext()) {
          Object key = keyIterator.next();
          //Replace the items in the checked state store with those from the supplied items
          List selections = (List) items.get(key);
          if (selections.size() == 0)
            //If it is empty remove it from the list
            checkedStateStore.remove(key);
          else {
            checkedStateStore.put(key, selections);
            // proceed up the tree element hierarchy
            Object parent = treeContentProvider.getParent(key);
            if (parent != null) {
              addToHierarchyToCheckedStore(parent);
            }
          }
        }

        //Now update hierarchies
        keyIterator = items.keySet().iterator();

        while (keyIterator.hasNext()) {
          Object key = keyIterator.next();
          updateHierarchy(key);
          if (currentTreeSelection != null && currentTreeSelection.equals(key)) {
            listViewer.setAllChecked(false);
            listViewer.setCheckedElements(((List) items.get(key)).toArray());
          }
        }
      }
    });

  }

  protected Control createButtonBar(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    // create a layout with spacing and margins appropriate for the font size.
    GridLayout layout = new GridLayout();
    layout.numColumns = 0; // this is incremented by createButton
    layout.makeColumnsEqualWidth = true;
    layout.marginWidth = 3;
    layout.marginHeight = 3;
    layout.horizontalSpacing = 3;
    layout.verticalSpacing = 3;

    composite.setLayout(layout);

    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
    composite.setLayoutData(data);

    composite.setFont(parent.getFont());

    // Add the buttons to the button bar.
    createButtonsForButtonBar(composite);

    return composite;
  }

  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, SELECT, "Select All", true);
    createButton(parent, DESELECT, "Deselect All", false);
    createButton(parent, INVERT, "Invert", false);
  }

  protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
    // increment the number of columns in the button bar
     ((GridLayout) parent.getLayout()).numColumns++;

    Button button = new Button(parent, SWT.PUSH);
    button.setFont(parent.getFont());

    button.setText(label);

    PixelConverter converter = new PixelConverter(button);
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
    data.heightHint = converter.convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
    int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
    data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    button.setLayoutData(data);

    button.setData(new Integer(id));
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        buttonPressed(((Integer) event.widget.getData()).intValue());
      }
    });
    if (defaultButton) {
      Shell shell = parent.getShell();
      if (shell != null) {
        shell.setDefaultButton(button);
      }
    }

    return button;
  }

  /**
   * Method buttonPressed.
   * @param i
   */
  public void buttonPressed(int buttonType) {

    IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
    Object key = selection.getFirstElement();

    Map newSelection = new HashMap();

    switch (buttonType) {

      case DESELECT :

        newSelection.put(key, new ArrayList());

        break;

      case SELECT :

        newSelection.put(key, new ArrayList(Arrays.asList(listContentProvider.getElements(key))));

        break;

      case INVERT :

        List checked = getAllCheckedListItems();

        List toBeChecked = new ArrayList(Arrays.asList(listContentProvider.getElements(key)));

        toBeChecked.removeAll(checked);

        newSelection.put(key, toBeChecked);

        break;

    }

    updateSelections(newSelection);

  }

}
