
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.ui.DialogCellEditor;
import com.iw.plugins.spindle.ui.PublicStaticFieldSelectionDialog;
import com.iw.plugins.spindle.ui.ToolTipHandler;

/**
 * @version 	1.0
 * @author
 */
public class ComponentAliasRefactorablePropertyDescriptor extends PropertyDescriptor {

  ITapestryProject project;
  List existingComponentAliases;

  /**
   * Constructor for FieldBindingPropertyDescriptor.
   * @param id
   * @param displayName
   */
  public ComponentAliasRefactorablePropertyDescriptor(
    Object id,
    String displayName,
    ITapestryProject project,
    List existingComponentAliases) {
    super(id, displayName);
    this.project = project;
    this.existingComponentAliases = existingComponentAliases;

  }

  public CellEditor createPropertyEditor(Composite parent) {

    return new ComponentNameCellEditor(parent);
  }

  class ComponentNameCellEditor extends DialogCellEditor {

    ToolTipHandler handler;

    /**
     * Constructor for TypeDialogCellEditor
     */
    protected ComponentNameCellEditor(Composite parent) {
      super(parent, true);

    }

    /**
     * @see DialogCellEditor#openDialogBox(Control)
     */
    protected Object openDialogBox(Control cellEditorWindow) {
      String value = (String) getValue();
      if (value == null || "".equals(value.trim()) || "fill in value".equals(value.trim())) {

        return value;

      }

      RefactorComponentAliasWizard wizard = RefactorComponentAliasWizard.createWizard(project, value, existingComponentAliases);

      if (wizard != null) {

        WizardDialog dialog = new WizardDialog(cellEditorWindow.getShell(), wizard);

        if (dialog.open() != dialog.CANCEL) {

          value = wizard.getNewName();

        }

      }

      return value;
    }

    protected Control createControl(Composite parent) {
      Control result = super.createControl(parent);
      handler = new ToolTipHandler(parent.getShell());
      handler.activateHoverHelp(defaultText);
      defaultText.setData("TIP_TEXT", "to refactor, use the button -->");
      return result;
    }

  }

}