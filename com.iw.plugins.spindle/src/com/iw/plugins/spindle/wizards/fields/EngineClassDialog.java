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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableContext;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.dialogfields.DialogFieldStatus;
import com.iw.plugins.spindle.util.Utils;

public class EngineClassDialog extends TypeDialogField {

  /**
   * Constructor for EngineClassDialog.
   * @param name
   * @param hierarchyRoot
   */
  public EngineClassDialog(String name, String hierarchyRoot, int labelWidth) {
    super(name, hierarchyRoot, labelWidth);
  }

  /**
   * Constructor for EngineClassDialog.
   * @param name
   * @param hierarchyRoot
   * @param nameOnly
   */
  public EngineClassDialog(String name, String hierarchyRoot) {
    super(name, hierarchyRoot);
  }

  /**
   * Constructor for EngineClassDialog.
   * @param name
   * @param labelWidth
   */
  public EngineClassDialog(String name, int labelWidth) {
    super(name, labelWidth);
  }

  /**
   * Constructor for EngineClassDialog.
   * @param name
   * @param nameOnly
   */
  public EngineClassDialog(String name) {
    super(name);
  }

  public void init(
    PackageDialogField packageChooser,
    IRunnableContext context) {
    super.init(packageChooser, context);
    setTextValue(MessageUtil.getString("TapestryEngine.defaultEngine"));
  }

  protected IStatus typeChanged() {

    DialogFieldStatus status = new DialogFieldStatus();

    String engineClassname = getTextValue();
    IPackageFragmentRoot root =
      packageChooser.getContainer().getPackageFragmentRoot();

    enableButton(root != null);
    chosenType = null;
    if (root == null
      || engineClassname == null
      || "".equals(engineClassname)) {
      status.setError(
        MessageUtil.getFormattedString(
          name + ".warning.TypeNotExists",
          "'empty string'"));
      return status;
    }
    try {
      IType engineType =
        resolveTypeName(root.getJavaProject(), engineClassname);
      if (engineType == null) {
        status.setError(
          MessageUtil.getFormattedString(
            name + ".warning.TypeNotExists",
            engineClassname));
        return status;
      }
      boolean isBinary = engineType.isBinary();
      String engineBaseTypeName =
        MessageUtil.getString("TapestryEngine.classname");
      if (engineBaseTypeName == null) {
        throw new Error(
          "tapestry engine base type: "
            + engineBaseTypeName
            + " does not exist in properties!");
      }
      IType engineBaseType =
        resolveTypeName(root.getJavaProject(), engineBaseTypeName);
      if (engineBaseType == null || !engineBaseType.exists()) {
        status.setError(
          MessageUtil.getFormattedString(
            name + ".warning.EngineBaseTypeNotExists",
            engineBaseTypeName));
        return status;
      }
      boolean match = false;
      if (engineBaseType.isInterface()) {
        String[] superInterfaces = engineType.getSuperInterfaceNames();
        if (superInterfaces != null && superInterfaces.length > 0) {
          for (int i = 0; i < superInterfaces.length; i++) {
            if (isBinary && engineBaseTypeName.endsWith(superInterfaces[i])) {
              match = true;
            } else {
              match = engineBaseTypeName.equals(superInterfaces[i]);
            }
          }
        } else {
          match = false;
        }
        if (!match) {
          status.setWarning(
            MessageUtil.getFormattedString(
              name + ".warning.EngineDoesNotImplementInterface",
              engineBaseTypeName));
          return status;
        }
      } else {
        ITypeHierarchy hierarchy = engineType.newSupertypeHierarchy(null);
        if (hierarchy.exists()) {
          IType[] superClasses = hierarchy.getAllSupertypes(engineType);
          for (int i = 0; i < superClasses.length; i++) {
            if (superClasses[i].equals(engineBaseType)) {
              match = true;
            }
          }
        }
        if (!match) {
          if (!canFindJavaxServletJar(root.getJavaProject())) {
            status.setError(
              MessageUtil.getString(
                name + ".error.javax.servlet.jar.NOTFOUND"));
          } else {
            status.setError(
              MessageUtil.getFormattedString(
                name + ".warning.EngineClassNotExtend",
                engineBaseTypeName));
          }
          return status;
        }
        chosenType = engineType;
      }

    } catch (JavaModelException e) {
      status.setError(name + ".error.couldn't.do.it");
    }

    return status;

  }

  /**
   * Method canFindJavaxServletJar.
   * @return boolean
   */
  private boolean canFindJavaxServletJar(IJavaProject project) {
    try {
      return resolveTypeName(project, "javax.servlet.Servlet") != null;
    } catch (JavaModelException e) {
    }
    return false;
  }

}