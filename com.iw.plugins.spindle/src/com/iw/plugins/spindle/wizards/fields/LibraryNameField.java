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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.util.SpindleStatus;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class LibraryNameField extends ApplicationNameField {

  /**
   * Constructor for LIbraryNameField.
   * @param fieldName
   * @param labelWidth
   */
  public LibraryNameField(String fieldName, int labelWidth) {
    super(fieldName, labelWidth);
  }

  /**
   * Constructor for LIbraryNameField.
   * @param fieldName
   */
  public LibraryNameField(String fieldName) {
    super(fieldName);
  }

  protected IStatus nameChanged() {
    SpindleStatus status = new SpindleStatus();
    String appname = getTextValue();
    if ("".equals(appname)) {
      status.setError("");
      return status;
    }
    if (appname.indexOf('.') != -1) {
      status.setError(MessageUtil.getString(fieldName + ".error.QualifiedName"));
      return status;
    }

    IStatus val = JavaConventions.validateJavaTypeName(appname);
    if (!val.isOK()) {
      if (val.getSeverity() == IStatus.ERROR) {
        status.setError(
          MessageUtil.getFormattedString(fieldName + ".error.InvalidName", val.getMessage()));
        return status;
      } else if (val.getSeverity() == IStatus.WARNING) {
        status.setWarning(
          MessageUtil.getFormattedString(
            fieldName + ".warning.NameDiscouraged",
            val.getMessage()));
        return status;
      }
    }
    if (packageField != null && packageField.getPackageFragment() != null) {
      try {
        IContainer folder = (IContainer) packageField.getPackageFragment().getUnderlyingResource();
        IFile file = folder.getFile(new Path(appname + ".library"));
        if (file.exists()) {
          status.setError(
            MessageUtil.getFormattedString(fieldName + ".error.AlreadyExists", appname));
        }
      } catch (JavaModelException e) {
        // do nothing
      }
    }
    char first = appname.charAt(0);
    if (Character.isLowerCase(first)) {
      status.setWarning(
        MessageUtil.getFormattedString(
          fieldName + ".warning.NameDiscouraged",
          "first character is lowercase"));
    }

    return status;

  }

}
