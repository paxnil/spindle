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
package com.iw.plugins.spindle.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editorjwc.components.ChooseBindingTypeDialog;
import com.iw.plugins.spindle.refactor.RenamedComponentOrPageRefactor;

/**
 * @author GWL
 * @version $Id$
 *
 * Copryright 2002, Intelligent Works Inc.
 * All Rights Reserved
 */
public class SpindleTooltipPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  /**
   * Constructor for SpindleRefactorPreferencePage.
   * @param style
   */
  public SpindleTooltipPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    setPreferenceStore(TapestryPlugin.getDefault().getPreferenceStore());
    setDescription("Customize Spindle Tooltips");
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }

  /**
   * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
   */
  protected void createFieldEditors() {

    BooleanFieldEditor hideNewBindingDialogTootips =
      new BooleanFieldEditor(
        ChooseBindingTypeDialog.DISABLE_TOOLTIPS_PREFERENCE,
        "Disable Spindle tooltips on the New Binding Dialog",
        BooleanFieldEditor.DEFAULT,
        getFieldEditorParent());

    addField(hideNewBindingDialogTootips);

  }

}
