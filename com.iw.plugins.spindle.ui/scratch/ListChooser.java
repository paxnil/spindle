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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.wizards.fields;

import java.util.Iterator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class ListChooser extends DialogField {

  private List list;
  private Button addButton;
  private Button removeButton;
  private ListViewer listViewer;

  /**
   * Constructor for InterfaceChooser.
   * @param labelText
   */
  public ListChooser(String labelText) {
    super(labelText);
  }

  /**
   * Constructor for InterfaceChooser.
   * @param labelText
   * @param labelWidth
   */
  public ListChooser(String labelText, int labelWidth) {
    super(labelText, labelWidth);
  }

  /**
   * @see DialogField#getControl(Composite)
   */
  public Control getControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    FormLayout layout = new FormLayout();
    container.setLayout(layout);

    FormData formData;

    Composite leftColumn = new Composite(container, SWT.NULL);
    leftColumn.setLayout(new FormLayout());

    Composite middleColumn = new Composite(container, SWT.NULL);
    middleColumn.setLayout(new FormLayout());

    Composite rightColumn = new Composite(container, SWT.NULL);
    rightColumn.setLayout(new FormLayout());

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.bottom = new FormAttachment(100, 0);
    formData.left = new FormAttachment(0, 0);
    formData.width = getLabelWidth();
    leftColumn.setLayoutData(formData);

    formData = new FormData();
    formData.width = 75;
    formData.top = new FormAttachment(0, 0);
    formData.bottom = new FormAttachment(100, 0);
    formData.right = new FormAttachment(100, 0);
    rightColumn.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(leftColumn, 4);
    formData.right = new FormAttachment(rightColumn, -4);
    formData.bottom = new FormAttachment(100, 0);
    middleColumn.setLayoutData(formData);

    Label labelControl = getLabelControl(leftColumn);
    Button addButtonControl = getAddButtonControl(rightColumn);
    Button removeButtonControl = getRemoveButtonControl(rightColumn);
    List listControl = getTreeControl(middleColumn);

    listViewer = new ListViewer(listControl);

    formData = new FormData();
    formData.width = getLabelWidth();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    labelControl.setLayoutData(formData);

    formData = new FormData();
    formData.width = 75;
    formData.height = 25;

    formData.top = new FormAttachment(0, 5);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    addButtonControl.setLayoutData(formData);

    formData = new FormData();
    formData.width = 75;
    formData.height = 25;

    formData.left = new FormAttachment(0, 0);
    formData.top = new FormAttachment(addButtonControl, 4);
    formData.right = new FormAttachment(100, 0);
    removeButtonControl.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    formData.left = new FormAttachment(0, 0);
    formData.bottom = new FormAttachment(100, 0);
    listControl.setLayoutData(formData);

    return container;
  }

  /**
   * Method getAddButtonControl.
   * @param org.eclipse.swt.widgets.Composite
   * @return Button
   */
  private Button getAddButtonControl(Composite parent) {
    if (addButton == null) {
      addButton = new Button(parent, SWT.NULL);
      addButton.setText("Add");
      addButton.addSelectionListener(new SelectionListener() {

        public void widgetSelected(SelectionEvent event) {

          fireAddButtonPressed();

        }

        public void widgetDefaultSelected(SelectionEvent event) {

          fireAddButtonPressed();

        }

      });
    }
    return addButton;
  }

  /**
   * Method getTreeControl.
   * @param org.eclipse.swt.widgets.Composite
   * @return Tree
   */
  private List getTreeControl(Composite parent) {
    if (list == null) {
      list = new List(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      list.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent event) {

          listSelectionChanged();

        }

        public void widgetDefaultSelected(SelectionEvent event) {

          listSelectionChanged();

        }
      });
    }
    return list;
  }

  /**
   * Method getRemoveButtonControl.
   * @param org.eclipse.swt.widgets.Composite
   * @return Button
   */
  private Button getRemoveButtonControl(Composite parent) {
    if (removeButton == null) {
      removeButton = new Button(parent, SWT.NULL);
      removeButton.setEnabled(false);
      removeButton.setText("Remove");
      removeButton.addSelectionListener(new SelectionListener() {

        public void widgetSelected(SelectionEvent event) {

          removeButtonPressed();

        }

        public void widgetDefaultSelected(SelectionEvent event) {

          removeButtonPressed();

        }

      });
    }
    return removeButton;

  }

  public void setLabelProvider(ILabelProvider provider) {
    if (listViewer != null) {
      listViewer.setLabelProvider(provider);
    }
  }

  public void setContentProvider(IStructuredContentProvider provider) {
    if (listViewer != null) {
      listViewer.setContentProvider(provider);
    }
  }

  public void addSelectionListener(ISelectionChangedListener listener) {
    if (listViewer != null) {
      listViewer.addSelectionChangedListener(listener);
    }
  }

  public void removeSelectionListener(ISelectionChangedListener listener) {
    if (listViewer != null) {
      listViewer.removeSelectionChangedListener(listener);
    }
  }

  public void removeButtonPressed() {
    int selectedIndex = list.getSelectionIndex();
    if (selectedIndex >= 0) {
      list.remove(selectedIndex);
      list.setSelection(-1);
    }
  }

  public void listSelectionChanged() {
    removeButton.setEnabled(list.getSelectionIndex() >= 0);
  }

  public int getSelectedIndex() {
    return list.getSelectionIndex();
  }

  public void fireAddButtonPressed() {
    for (Iterator iterator = getListeners().iterator(); iterator.hasNext();) {
      IDialogFieldChangedListener element = (IDialogFieldChangedListener) iterator.next();
      element.dialogFieldButtonPressed(this);
    }
  }

  public void setInput(Object input) {
    listViewer.setInput(input);
    list.setSelection(-1);
    removeButton.setEnabled(false);
    listViewer.getList().setSelection(-1);
  }

  public Object getInput() {
    return listViewer.getInput();
  }

  public void reveal(Object element) {
    listViewer.reveal(element);
  }

  /**
   * @see DialogField#setEnabled(boolean)
   */
  public void setEnabled(boolean flag) {
    list.setEnabled(flag);
    addButton.setEnabled(flag);
    removeButton.setEnabled(flag && list.getSelectionIndex() >= 0);
    super.setEnabled(flag);
  }

}