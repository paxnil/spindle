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
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.dialogfields.DialogField;
import com.iw.plugins.spindle.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.dialogfields.StringButtonField;
import com.iw.plugins.spindle.util.Utils;

public class PublicStaticFieldSelectionDialog extends AbstractDialog {

  private boolean editing;
  private StringButtonField typeField;
  private Table fields;

  private DialogAdapter adapter = new DialogAdapter();
  private ILabelProvider fieldRenderer = new JavaElementLabelProvider();

  private String dialogResult;
  private String existingBinding;
  private IJavaProject jproject;

  /**
   * Constructor for typeFieldDialog
   */
  public PublicStaticFieldSelectionDialog(Shell shell, IJavaProject project) {
    super(shell);
    this.jproject = project;
    String windowTitle = "Choose Field Binding Value";
    String description = "Choose a Type and one of it public static fields";
    updateWindowTitle(windowTitle);
    updateMessage(description);
  }

  public PublicStaticFieldSelectionDialog(Shell shell, IJavaProject project, String existingBinding) {
    this(shell, project);
    this.existingBinding = existingBinding;
  }

  /**
   * @see AbstractDialog#createAreaContents(Composite)
   */
  protected Composite createAreaContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    FormLayout layout = new FormLayout();
    layout.marginHeight= 4;
    layout.marginWidth = 4;
    container.setLayout(layout);
    
    //our container is embedded in a GridLayout 
    GridData gd = new GridData();
    gd.widthHint = 500;
    gd.heightHint = 300;
  
    container.setLayoutData(gd);

    typeField = new StringButtonField("Type:", 64);
    typeField.addListener(adapter);
    
    Control typeFieldControl = typeField.getControl(container)    ;
    Text text = typeField.getTextControl(container);
    text.setEditable(false);
    text.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
    
    FormData formData = new FormData();
    formData.top = new FormAttachment(0,0);
    formData.left = new FormAttachment(0,0);
    formData.right = new FormAttachment(100,0);
    typeFieldControl.setLayoutData(formData);

    Control fieldListControl = createFieldList(container);
    
    formData = new FormData();
    formData.top = new FormAttachment(typeFieldControl, 4);
    formData.left = new FormAttachment(0,0);
    formData.right = new FormAttachment(100,0);
    formData.bottom = new FormAttachment(100, 0);
    fieldListControl.setLayoutData(formData);

    if (existingBinding != null) {
      populateFromExistingBinding();
    }

    return container;
  }

  private Control createFieldList(Composite parent) {
    
    Composite container = new  Composite(parent, SWT.NULL);
    FormLayout layout = new FormLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    container.setLayout(layout);
    
    Label label = new Label(container, SWT.NONE);
    label.setText("Fields:");
    
    FormData formData = new FormData();
    formData.width = 64;
    formData.top = new FormAttachment(0,0);
    formData.left = new FormAttachment(0,0);
    label.setLayoutData(formData);

    Table list = new Table(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

    list.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event evt) {
        handleFieldSelectionChanged();
      }
    });

    list.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event evt) {
        handleDefaultSelected();
      }
    });

    list.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        fieldRenderer.dispose();
      }
    });
    
    formData = new FormData(300, 200);
    formData.top = new FormAttachment(0,0);
    formData.left = new FormAttachment(label, 4);
    formData.right = new FormAttachment(100,0);
    formData.bottom = new FormAttachment(100,0);
    list.setLayoutData(formData);

    fields = list;

    return container;
  }

  private void populateFromExistingBinding() {

    int index = existingBinding.lastIndexOf(".");
    if (index > 0) {

      String qualifiedClass = existingBinding.substring(0, index);
      String fieldname = existingBinding.substring(index + 1);
      IType type = null;

      try {
        type = Utils.findType(jproject, qualifiedClass);
      } catch (JavaModelException e) {
        TapestryPlugin.getDefault().logException(e);
      }

      if (type != null) {

        typeField.setTextValue(qualifiedClass);
        updateFieldList(type);
        TableItem[] items = fields.getItems();
        TableItem found = null;

        int newSelection = -1;
        for (int i = 0; i < items.length; i++) {
          if (items[i].getText().equals(fieldname)) {
            newSelection = i;
            found = items[i];
            break;
          }
        }

        if (newSelection >= 0) {
          fields.select(newSelection);
          fields.showItem(found);
        }
      }
    }
  }

  private void updateFieldList(IType type) {
    setErrorMessage("");
    List list = new ArrayList();
    try {

      IField[] foundFields = type.getFields();
      for (int i = 0; i < foundFields.length; i++) {
        int flags = foundFields[i].getFlags();
        if (Flags.isStatic(flags) && Flags.isPublic(flags)) {
          list.add(foundFields[i]);
        }
      }
    } catch (JavaModelException jmex) {
      setErrorMessage("An error occured looking for fields in " + type.getElementName());
    }
    Object[] elements = list.toArray();
    int size = elements.length;
    if (size == 0) {
      setErrorMessage(type.getElementName() + "has no public static fields. choose another");
    }
    fields.setRedraw(false);
    int itemCount = fields.getItemCount();
    if (size < itemCount) {
      fields.remove(0, itemCount - size - 1);
    }
    TableItem[] items = fields.getItems();
    for (int i = 0; i < size; i++) {
      TableItem ti = null;
      if (i < itemCount) {
        ti = items[i];
      } else {
        ti = new TableItem(fields, i);
      }
      Image img = null;
      if (elements[i] instanceof IJavaElement) {
        ti.setText(fieldRenderer.getText(elements[i]));
        img = fieldRenderer.getImage(elements[i]);
      } else {
        ti.setText(elements[i].toString());
      }
      if (img != null) {
        ti.setImage(img);
      }
      ti.setData(elements[i]);
    }
    if (fields.getItemCount() > 0) {
      fields.setSelection(0);
    }
    fields.setRedraw(true);
    update();
  }

  private IField getSelectedField() {
    int index = fields.getSelectionIndex();
    if (index >= 0) {
      return (IField) fields.getItem(index).getData();
    }
    return null;
  }

  private void handleFieldSelectionChanged() {
    update();
  }

  private void handleDefaultSelected() {
    if (okToClose()) {
      close();
    }
  }

  /**
   * @see AbstractDialog#performCancel()
   */
  protected boolean performCancel() {
    setReturnCode(CANCEL);
    return true;

  }

  protected void cancelPressed() {
    performCancel();
    close();
  }

  public boolean close() {
    return hardClose();
  }

  protected boolean okToClose() {
    String typeName = typeField.getTextValue();
    IField selectedField = getSelectedField();
    if (typeName == null || "".equals(typeName) || selectedField == null) {
      setReturnCode(CANCEL);
      dialogResult = null;
      return false;
    }
    dialogResult = typeName + "." + getSelectedField().getElementName();
    setReturnCode(OK);
    return true;
  }

  public String getDialogResult() {
    return dialogResult;
  }

  protected class DialogAdapter implements IDialogFieldChangedListener {

    public void dialogFieldChanged(DialogField field) {
      update();
    }
    /**
     * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
     */
    public void dialogFieldButtonPressed(DialogField field) {
      try {
        SelectionDialog dialog =
          JavaUI.createTypeDialog(
            getShell(),
            new ProgressMonitorDialog(getShell()),
            SearchEngine.createWorkspaceScope(),
            IJavaElementSearchConstants.CONSIDER_CLASSES,
            false);
        dialog.setMessage("");
        int result = dialog.open();
        if (result == SelectionDialog.OK) {
          IType chosenType = (IType) dialog.getResult()[0];
          typeField.setTextValue(chosenType.getFullyQualifiedName());
          updateFieldList(chosenType);
        }
      } catch (JavaModelException jmex) {
        TapestryPlugin.getDefault().logException(jmex);
      }
    }

    /**
     * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
     */
    public void dialogFieldStatusChanged(IStatus status, DialogField field) {
    }

  }
//
//  /*
//   * @see Window#getInitialSize()
//   */
//  protected Point getInitialSize() {
//    return new Point(525, 450);
//  }

}