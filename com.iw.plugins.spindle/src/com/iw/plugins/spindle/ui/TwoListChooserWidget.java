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
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class TwoListChooserWidget extends Viewer {

  static private final Object[] empty = new Object[0];

  private int upperListHeightInChars = 10;
  private int lowerListHeightInChars = 4;
  private String filterLabel = "search";
  private String upperListLabel = "choose:";
  private String lowerListLabel = "in";
  private String initialFilter = "*";

  private Text searchText;
  private Table upperList;
  private Table lowerList;

  private Composite control = null;

  private ILabelProvider upperListLabelProvider;
  private ILabelProvider lowerListLabelProvider;

  private IStructuredContentProvider upperListContentProvider;
  private IStructuredContentProvider lowerListContentProvider;

  private List doubleClickListeners = new ArrayList();

  /**
   * Constructor for TwoListChooseWidget.
   */
  public TwoListChooserWidget() {
    super();
  }

  public Composite createControl(Composite parent) {
  	
  	

    Composite container = new Composite(parent, SWT.NONE);

		

    FormLayout layout = new FormLayout();
    layout.marginWidth = 4;
    layout.marginHeight = 4;
    container.setLayout(layout);

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0); 
    formData.right = new FormAttachment(100, 0);
    formData.width = 400;

    container.setLayoutData(formData);

    Label searchLabel = new Label(container, SWT.NONE);
    searchLabel.setText(filterLabel);

    searchText = createText(container);

    Label upperListLabelControl = new Label(container, SWT.NONE);
    upperListLabelControl.setText(upperListLabel);

    upperList = createUpperList(container);

    Label lowerListLabelControl = new Label(container, SWT.NONE);
    lowerListLabelControl.setText(lowerListLabel);

    lowerList = createLowerList(container);

    addControl(searchLabel, container, 4);

    addControl(searchText, searchLabel, 4);

    addControl(upperListLabelControl, searchText, 4);

    addControl(upperList, upperListLabelControl, 4);

    PixelConverter converter = new PixelConverter(upperList);

    formData = (FormData) upperList.getLayoutData();
    formData.height = converter.convertHeightInCharsToPixels(upperListHeightInChars);

    addControl(lowerListLabelControl, upperList, 4);

    addControl(lowerList, lowerListLabelControl, 4);

    converter = new PixelConverter(lowerList);

    formData = (FormData) lowerList.getLayoutData();
    formData.height = converter.convertHeightInCharsToPixels(lowerListHeightInChars);

    //a little trick to make the window come up faster
    if (initialFilter != null) {

      searchText.setText(initialFilter);
      searchText.selectAll();
      refresh();
    }

    control = container;
    return container;
  }

  public Control getControl() {

    return control;

  }

  public void dispose() {
    searchText.dispose();
    upperList.dispose();
    lowerList.dispose();
  }

  public void setFocus() {

    searchText.selectAll();
    searchText.setFocus();

  }

  private Text createText(Composite parent) {
    final Text text = new Text(parent, SWT.BORDER);
    Listener l = new Listener() {
      public void handleEvent(Event evt) {

        refresh();

      }
    };
    text.addListener(SWT.Modify, l);

    return text;
  }

  private Table createUpperList(Composite parent) {

    Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    table.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event evt) {
        handleUpperSelectionChanged();
      }
    });

    table.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event evt) {
        handleDoubleClick();
      }
    });
    table.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        upperListLabelProvider.dispose();
      }
    });
    return table;
  }

  private Table createLowerList(Composite parent) {

    Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    table.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event evt) {
        handleLowerSelectionChanged();
      }
    });
    table.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event evt) {
        handleDoubleClick();
      }
    });
    table.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        lowerListLabelProvider.dispose();
      }
    });

    return table;
  }

  /**
   * Method handleDoubleClick.
   */
  private void handleDoubleClick() {

    for (Iterator iter = doubleClickListeners.iterator(); iter.hasNext();) {

      IDoubleClickListener listener = (IDoubleClickListener) iter.next();

      listener.doubleClick(new DoubleClickEvent(this, getSelection()));
    }
  }

  protected void handleUpperSelectionChanged() {
  	
  	TableItem [] selected = upperList.getSelection();

    if (selected.length > 0) {

    TableItem selectedItem = upperList.getSelection()[0];
    
      Object[] newLowerListData = lowerListContentProvider.getElements(selectedItem.getData());

      updateListWidget(newLowerListData, lowerList, lowerListLabelProvider);

    } else {

      updateListWidget(empty, lowerList, lowerListLabelProvider);

    }

    fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));

  }

  protected void handleLowerSelectionChanged() {

    fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));

  }

  public ISelection getSelection() {

    Object upperSelected = null;

    int upperSelectionIndex = upperList.getSelectionIndex();

    if (upperSelectionIndex >= 0) {

      upperSelected = upperList.getSelection()[0].getData();

    }

    Object lowerSelected = null;

    int lowerSelectionIndex = lowerList.getSelectionIndex();

    if (lowerSelectionIndex >= 0) {

      lowerSelected = lowerList.getSelection()[0].getData();

    }

    StructuredSelection selection =
      new StructuredSelection(new Object[] { upperSelected, lowerSelected });

    return selection;
  }

  private void updateListWidget(Object[] elements, Table table, ILabelProvider provider) {
    int size = elements.length;
    table.setRedraw(false);
    int itemCount = table.getItemCount();
    if (size < itemCount) {
      table.remove(0, itemCount - size - 1);
    }
    TableItem[] items = table.getItems();
    for (int i = 0; i < size; i++) {
      TableItem ti = null;
      if (i < itemCount) {
        ti = items[i];
      } else {
        ti = new TableItem(table, i);
      }
      ti.setText(provider.getText(elements[i]));
      ti.setData(elements[i]);
      Image img = provider.getImage(elements[i]);
      if (img != null) {
        ti.setImage(img);
      }
    }
    if (table.getItemCount() > 0) {
      table.setSelection(0);
    }
    table.setRedraw(true);
    handleSelectionChanged(table);
  }

  protected void handleSelectionChanged(Table table) {
    if (table == upperList) {
      handleUpperSelectionChanged();
    } else {
      fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
    }
  }

  /**
   * @see org.eclipse.jface.viewers.IInputProvider#getInput()
   */
  public Object getInput() {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#refresh()
   */
  public void refresh() {

    Object[] upperListContents = upperListContentProvider.getElements(searchText.getText());
    updateListWidget(upperListContents, upperList, upperListLabelProvider);

  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setInput(Object)
   */
  public void setInput(Object input) {

    refresh();

  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setSelection(ISelection, boolean)
   */
  public void setSelection(ISelection selection, boolean reveal) {
  }

  /**
   * Sets the lowerListHeightInChars.
   * @param lowerListHeightInChars The lowerListHeightInChars to set
   */
  public void setLowerListHeightInChars(int lowerListHeightInChars) {
    this.lowerListHeightInChars = lowerListHeightInChars;
  }

  /**
   * Sets the upperListHeightInChars.
   * @param upperListHeightInChars The upperListHeightInChars to set
   */
  public void setUpperListHeightInChars(int upperListHeightInChars) {
    this.upperListHeightInChars = upperListHeightInChars;
  }

  protected void addControl(Control toBeAdded, Control parent) {
    addControl(toBeAdded, parent, 0);
  }

  protected void addControl(Control toBeAdded, Control parent, int verticalOffset) {
    FormData formData = new FormData();
    formData.top = new FormAttachment(parent, verticalOffset);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    toBeAdded.setLayoutData(formData);
  }

  public void addDoubleClickListener(IDoubleClickListener listener) {
    if (!doubleClickListeners.contains(listener)) {

      doubleClickListeners.add(listener);

    }
  }

  public void removeDoubleClickListener(IDoubleClickListener listener) {

    doubleClickListeners.remove(listener);

  }

  /**
   * Returns the filterLabel.
   * @return String
   */
  public String getFilterLabel() {
    return filterLabel;
  }

  /**
   * Returns the lowerListLabel.
   * @return String
   */
  public String getLowerListLabel() {
    return lowerListLabel;
  }

  /**
   * Returns the upperListLabel.
   * @return String
   */
  public String getUpperListLabel() {
    return upperListLabel;
  }

  /**
   * Sets the filterLabel.
   * @param filterLabel The filterLabel to set
   */
  public void setFilterLabel(String filterLabel) {
    this.filterLabel = filterLabel;
  }

  /**
   * Sets the lowerListLabel.
   * @param lowerListLabel The lowerListLabel to set
   */
  public void setLowerListLabel(String lowerListLabel) {
    this.lowerListLabel = lowerListLabel;
  }

  /**
   * Sets the upperListLabel.
   * @param upperListLabel The upperListLabel to set
   */
  public void setUpperListLabel(String upperListLabel) {
    this.upperListLabel = upperListLabel;
  }

  /**
   * Returns the lowerListLabelProvider.
   * @return ILabelProvider
   */
  public ILabelProvider getLowerListLabelProvider() {
    return lowerListLabelProvider;
  }

  /**
   * Returns the upperListLabelProvider.
   * @return ILabelProvider
   */
  public ILabelProvider getUpperListLabelProvider() {
    return upperListLabelProvider;
  }

  /**
   * Sets the lowerListLabelProvider.
   * @param lowerListLabelProvider The lowerListLabelProvider to set
   */
  public void setLowerListLabelProvider(ILabelProvider lowerListLabelProvider) {
    this.lowerListLabelProvider = lowerListLabelProvider;
  }

  /**
   * Sets the upperListLabelProvider.
   * @param upperListLabelProvider The upperListLabelProvider to set
   */
  public void setUpperListLabelProvider(ILabelProvider upperListLabelProvider) {
    this.upperListLabelProvider = upperListLabelProvider;
  }

  /**
   * Returns the lowerListContentProvider.
   * @return IStructuredContentProvider
   */
  public IStructuredContentProvider getLowerListContentProvider() {
    return lowerListContentProvider;
  }

  /**
   * Returns the upperListContentProvider.
   * @return IStructuredContentProvider
   */
  public IStructuredContentProvider getUpperListContentProvider() {
    return upperListContentProvider;
  }

  /**
   * Sets the lowerListContentProvider.
   * @param lowerListContentProvider The lowerListContentProvider to set
   */
  public void setLowerListContentProvider(IStructuredContentProvider lowerListContentProvider) {
    this.lowerListContentProvider = lowerListContentProvider;
  }

  /**
   * Sets the upperListContentProvider.
   * @param upperListContentProvider The upperListContentProvider to set
   */
  public void setUpperListContentProvider(IStructuredContentProvider upperListContentProvider) {
    this.upperListContentProvider = upperListContentProvider;
  }

  /**
   * Sets the initialFilter.
   * @param initialFilter The initialFilter to set
   */
  public void setInitialFilter(String initialFilter) {
    this.initialFilter = initialFilter;
  }

}
