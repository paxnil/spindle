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
package com.iw.plugins.spindle.wizards.fields;

import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.IStatus;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringField;

public abstract class AbstractNameField extends StringField {

  protected String fieldName;
  protected PropertyChangeSupport propertySupport;
  protected PackageDialogField packageField;

  /**
   * Constructor for AbstractNameField.
   * @param label
   * @param labelWidth
   */
  public AbstractNameField(String fieldName, int labelWidth) {
    super(MessageUtil.getString(fieldName + ".label"), labelWidth);
    this.fieldName = fieldName;
  }

  /**
   * Constructor for ApplicationNameField
   */
  public AbstractNameField(String fieldName) {
    this(fieldName, -1);
    

  }

  public void init(PackageDialogField packageField) {
    this.packageField = packageField;
    packageField.addListener(this);

  }

  /**
   * @see IUpdateStatus#dialogFieldChanged(DialogField)
   */
  public void dialogFieldChanged(DialogField field) {
    if (field == this || field == packageField) {
      setStatus(nameChanged());
    }

  }

  protected abstract IStatus nameChanged();

}