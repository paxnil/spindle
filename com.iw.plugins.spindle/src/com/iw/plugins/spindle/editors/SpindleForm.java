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
package com.iw.plugins.spindle.editors;

import org.eclipse.jface.action.IMenuManager;

import com.iw.plugins.spindle.model.BaseTapestryModel;

public class SpindleForm extends EditorForm {

  boolean hasBeenInitialized = false;
  /**
   * Constructor for TapestryForm
   */
  public SpindleForm(SpindleFormPage page) {
    super(page);
  }

  /**
   * Once the form is committed, set the model to not dirty.
   * unless the model is out of sync. 
   * i.e. a change was made directly to the model from outside the
   * current editor.
   * 
   * @see Form#commitChanges(boolean)
   */
  public void commitChanges(boolean onSave) {
    super.commitChanges(onSave);
  }

  /**
   * will call super.initialize() only if the model was
   * parsed without error.
   * @see Form#initialize(Object)
   */
  public void initialize(Object model) {
    BaseTapestryModel tmodel = (BaseTapestryModel) model;
    if (tmodel.isLoaded()) {
      super.initialize(model);
      hasBeenInitialized = true;
    }
  }

  public boolean hasBeenInitialized() {
    return hasBeenInitialized;
  }

  /**
   * If an update call comes, check to see if the form has been initialized.
   * It might not have been if there were parse errors when the editor opened.
   * If this instance was never initialized, try to do so now.
   * If this instance was initialized, call super.update()
   * @see Form#update()
   */
  public void update() {
    if (hasBeenInitialized) {
      super.update();
      return;
    }
    BaseTapestryModel model = (BaseTapestryModel) getPage().getModel();
    if (model.isLoaded()) {
      initialize(model);
      hasBeenInitialized = true;
    }
  }

  /**
   * Intended to be overridden by subclasses.
   * 
   */
  public void fillContextMenu(IMenuManager mng) {
    // do nothing
  }

}
