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
package com.iw.plugins.spindle.wizards.project;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringField;
import com.iw.plugins.spindle.util.SpindleStatus;

public class PackageDialogField extends StringField {

  private String name;
  private ContainerDialogField container;
  private String currentPackage;

  /**
   * Constructor for PackageDialogField.
   * @param label
   * @param defaultLabel
   */
  public PackageDialogField(String name, int labelWidth) {
    super(MessageUtil.getString(name + ".label"), labelWidth);
    this.name = name;

  }

  /**
   * Constructor for PackageDialogField.
   * @param label
   * @param defaultLabel
   * @param labelWidth
   */

  /**
   * Constructor for PackageDialogField
   */
  public PackageDialogField(String name) {
    this(name, -1);

  }

  public void dialogFieldChanged(DialogField field) {

    setStatus(packageChanged());

  }

  public IStatus packageChanged() {
    SpindleStatus status = new SpindleStatus();

    String packName = getTextValue().trim();

    if ("".equals(packName)) {

      status.setError("");

    } else {
      IStatus val = JavaConventions.validatePackageName(packName);
      if (val.getSeverity() == IStatus.ERROR) {
        status.setError(
          MessageUtil.getFormattedString(name + ".error.InvalidPackageName", val.getMessage()));
        return status;
      } else if (val.getSeverity() == IStatus.WARNING) {
        status.setWarning(
          MessageUtil.getFormattedString(
            name + ".warning.DiscouragedPackageName",
            val.getMessage()));
        // continue
      }
    }

    return status;
  }

  public void setPackageFragment(String fragment) {
    currentPackage = fragment;
    String str = (fragment == null) ? "" : fragment;
    setTextValue(str);
    fireDialogChanged(this);
  }

  public String getPackageFragment() {
    return currentPackage;
  }

  /**
   * @see com.iw.plugins.spindle.ui.dialogfields.DialogField#getControl(Composite)
   */
  public Control getControl(Composite parent) {
  	Control result = super.getControl(parent);
  	setPackageFragment("");
    return result;
  }

}