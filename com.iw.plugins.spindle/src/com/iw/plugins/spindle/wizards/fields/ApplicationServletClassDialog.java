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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableContext;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.dialogfields.DialogFieldStatus;

public class ApplicationServletClassDialog extends SuperClassDialogField {

  /**
   * Constructor for ApplicationServletClassDialog.
   * @param name
   * @param hierarchyRoot
   * @param labelWidth
   */
  public ApplicationServletClassDialog(String name, String hierarchyRoot, int labelWidth) {
    super(name, hierarchyRoot, labelWidth);
  }

  /**
   * Constructor for ApplicationServletClassDialog.
   * @param name
   * @param hierarchyRoot
   */
  public ApplicationServletClassDialog(String name, String hierarchyRoot) {
    super(name, hierarchyRoot);
  }

  /**
   * Constructor for ApplicationServletClassDialog.
   * @param name
   * @param labelWidth
   */
  public ApplicationServletClassDialog(String name, int labelWidth) {
    super(name, labelWidth);
  }

  public ApplicationServletClassDialog(String name) {
    super(name);
  }

  public void init(PackageDialogField packageChooser, IRunnableContext context) {
    super.init(packageChooser, context);
    setTextValue(MessageUtil.getString("TapestryEngine.defaultServlet"));
  }

  protected IStatus typeChanged() {
    IStatus status = super.typeChanged();
    if (status.isOK()) {
      status = hasTapestryServletClassAsSuperType(getTextValue(), packageChooser.getContainer().getPackageFragmentRoot());
    }
    return status;

  }

  private IStatus hasTapestryServletClassAsSuperType(String superclassName, IPackageFragmentRoot root) {
    DialogFieldStatus status = new DialogFieldStatus();
    enableButton(root == null);
    if (root == null) {
      return status;
    }
    String superclassBaseTypeName = MessageUtil.getString("TapestryEngine.defaultServlet");
    if (superclassBaseTypeName == null) {
      throw new Error("tapestry servlet type: " + superclassBaseTypeName + " does not exist in properties!");
    }
    if (!superclassName.equals(superclassBaseTypeName)) {
      try {
        IType chosenSuperclassType = resolveTypeName(root.getJavaProject(), superclassName);
        if (chosenSuperclassType == null) {
          status.setWarning(MessageUtil.getFormattedString(name + ".warning.TypeNotExists", superclassName));
          return status;
        }
        boolean isBinary = chosenSuperclassType.isBinary();
        IType superclassBaseType = resolveTypeName(root.getJavaProject(), superclassBaseTypeName);
        if (superclassBaseType == null || !superclassBaseType.exists()) {
          status.setError(MessageUtil.getFormattedString(name + ".warning.TypeNotExists", superclassBaseTypeName));
          return status;
        }
        boolean match = false;
        ITypeHierarchy hierarchy = chosenSuperclassType.newSupertypeHierarchy(null);
        if (hierarchy.exists()) {
          IType[] superClasses = hierarchy.getAllSupertypes(chosenSuperclassType);
          for (int i = 0; i < superClasses.length; i++) {
            if (superClasses[i].equals(superclassBaseType)) {
              match = true;
            }
          }
        }
        if (!match) {
          status.setError(MessageUtil.getFormattedString(name + ".warning.SuperclassClassNotExtend", superclassBaseTypeName));
          return status;
        }

      } catch (JavaModelException e) {
        status.setError(name + ".error.couldn't.do.it");
      }
    }
    return status;
  }

}