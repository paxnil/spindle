
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
package com.iw.plugins.spindle.ui.descriptors;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.ui.EditableDialogCellEditor;
import com.iw.plugins.spindle.ui.PublicStaticFieldSelectionDialog;

/**
 * @version 	1.0
 * @author
 */
public class FieldBindingPropertyDescriptor extends PropertyDescriptor {

  private ITapestryModel model;

  /**
   * Constructor for FieldBindingPropertyDescriptor.
   * @param id
   * @param displayName
   */
  public FieldBindingPropertyDescriptor(Object id, String displayName, ITapestryModel model) {
    super(id, displayName);
    this.model = model;
  }

  public CellEditor createPropertyEditor(Composite parent) {

    return new FieldBindingCellEditor(parent);
  }

  class FieldBindingCellEditor extends EditableDialogCellEditor {

    /**
     * Constructor for TypeDialogCellEditor
     */
    protected FieldBindingCellEditor(Composite parent) {
      super(parent);
    }   

    /**
     * @see DialogCellEditor#openDialogBox(Control)
     */
    protected Object openDialogBox(Control cellEditorWindow) {
      Object value = getValue();
      IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
      PublicStaticFieldSelectionDialog dialog = new PublicStaticFieldSelectionDialog(cellEditorWindow.getShell(), jproject, (String)value);
      dialog.create();
      if (dialog.open() == dialog.OK) {
        return dialog.getDialogResult();
      }

      return value;
    }
  }
}