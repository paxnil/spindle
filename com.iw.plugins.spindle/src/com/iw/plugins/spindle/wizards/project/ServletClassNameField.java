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

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.wizards.fields.AbstractNameField;

public class ServletClassNameField extends AbstractNameField {

  /**
   * Constructor for SuperClassDialogField.
   * @param name
   * @param hierarchyRoot
   * @param labelWidth
   */
  public ServletClassNameField(String name, int labelWidth) {
    super(name, labelWidth);

  }

  /**
   * Constructor for SuperClassDialogField.
   * @param name
   * @param hierarchyRoot
   */
  public ServletClassNameField(String name) {
    super(name);
  }

  protected IStatus nameChanged() {
    SpindleStatus status = new SpindleStatus();

    String typeName = getTextValue().trim();

    if ("".equals(typeName)) {

      status.setError("");

    } else {

      if (typeName.indexOf('.') != -1) {
        status.setError(MessageUtil.getString(fieldName + ".error.QualifiedName"));
        return status;
      }

      IStatus val = JavaConventions.validateJavaTypeName(typeName);
      if (!val.isOK()) {
        if (val.getSeverity() == IStatus.ERROR) {
          status.setError(
            MessageUtil.getFormattedString(fieldName + ".error.InvalidAppName", val.getMessage()));
          return status;
        } else if (val.getSeverity() == IStatus.WARNING) {
          status.setWarning(
            MessageUtil.getFormattedString(
              fieldName + ".warning.AppNameDiscouraged",
              val.getMessage()));
          return status;
        }
      }
    }
    return status;
  }
  /**
   * @see TypeDialogField#getType()
   */
  public String getType() {

    return getTextValue();

  }

}