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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.ui.PagePathDialogCellEditor;

public class PagePathDialogPropertyDescriptor extends PropertyDescriptor implements INeedsModelInitialization {

  ITapestryModel model;
  String title;
  String message;
  

  public PagePathDialogPropertyDescriptor(Object id, String displayName, String dialogTitle, String dialogMessage) {
    super(id, displayName);
    this.title = dialogTitle;
    this.message = dialogMessage;
  }
  
  public void initialize(ITapestryModel model) {
  	this.model = model;
  }

  public CellEditor createPropertyEditor(Composite parent) {
    return new PagePathDialogCellEditor(parent, model, title, message);
  }
  /**
   * @see com.iw.plugins.spindle.ui.descriptors.INeedsModelInitialization#initialize(ITapestryModel)
   */
  

}