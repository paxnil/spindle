package com.iw.plugins.spindle.wizards.fields;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.dialogfields.DialogFieldStatus;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class JavaClassnameField extends AbstractNameField {

  /**
   * Constructor for JavaClassnameField.
   * @param fieldName
   * @param labelWidth
   */
  public JavaClassnameField(String fieldName, int labelWidth) {
    super(fieldName, labelWidth);
  }

  /**
   * Constructor for JavaClassnameField.
   * @param fieldName
   */
  public JavaClassnameField(String fieldName) {
    super(fieldName);
  }

  protected IStatus nameChanged() {
    DialogFieldStatus status = new DialogFieldStatus();
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
        status.setError(MessageUtil.getFormattedString(fieldName + ".error.InvalidClassName", val.getMessage()));
        return status;
      } else if (val.getSeverity() == IStatus.WARNING) {
        status.setWarning(
          MessageUtil.getFormattedString(fieldName + ".warning.ClassNameDiscouraged", val.getMessage()));
        return status;
      }
    }
    if (packageField != null && packageField.getPackageFragment() != null) {
      try {
        IContainer folder = (IContainer) packageField.getPackageFragment().getUnderlyingResource();
        IFile file = folder.getFile(new Path(appname + ".java"));
        if (file.exists()) {
          status.setError(MessageUtil.getFormattedString(fieldName + ".error.ClassAlreadyExists", appname));
        }
      } catch (JavaModelException e) {
        // do nothing
      }
    }
    char first = appname.charAt(0);
    if (Character.isLowerCase(first)) {
      status.setWarning(
        MessageUtil.getFormattedString(
          fieldName + ".warning.ClassNameDiscouraged",
          "first character is lowercase"));
    }
   
    return status;


  }

}
