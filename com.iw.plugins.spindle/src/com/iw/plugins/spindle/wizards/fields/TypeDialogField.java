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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;

public class TypeDialogField extends StringButtonField {

  protected String name;
  protected PackageDialogField packageChooser;
  protected IType chosenType;
  private String hierarchyRoot;

  public TypeDialogField(String name, String hierarchyRoot, int labelWidth) {
    super(MessageUtil.getString(name + ".label"), labelWidth);
    this.name = name;
    this.hierarchyRoot = hierarchyRoot;
  }

  public TypeDialogField(String name, String hierarchyRoot) {
    this(name, hierarchyRoot, -1);
  }

  /**
   * Constructor for TypeDialogField
   */
  public TypeDialogField(String name) {
    this(name, null, -1);

  }
  
  public TypeDialogField(String name, int labelWidth) {
    this(name, null, labelWidth);
  }

  public void init(PackageDialogField packageChooser, IRunnableContext context) {
    super.init(context);
    this.packageChooser = packageChooser;
    packageChooser.addListener(this);
    setButtonLabel(MessageUtil.getString(name + ".button"));
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field) {

    if (field != this) {
      return;
    }
    IType type = chooseType();
    if (type != null) {
      String old = getTextValue();
      setTextValue(type.getFullyQualifiedName());

    }
  }

  public void dialogFieldChanged(DialogField field) {
    if (field == this) {
      setStatus(typeChanged());
    }
  }

  protected IStatus typeChanged() {
    IPackageFragmentRoot root = packageChooser.getContainer().getPackageFragmentRoot();
    chosenType = null;
    SpindleStatus status = new SpindleStatus();
    String typeName = getTextValue();
    if ("".equals(typeName)) {
      status.setError(MessageUtil.getString(name + ".error.EnterTypeName"));
      return status;
    }
    if (typeName.indexOf('.') != -1) {
      status.setError(MessageUtil.getString(name + ".error.QualifiedName"));
      return status;
    }
    IStatus val = JavaConventions.validateJavaTypeName(typeName);
    if (!val.isOK()) {
      if (val.getSeverity() == IStatus.ERROR) {
        status.setError(MessageUtil.getFormattedString(name + ".error.InvalidTypeName", val.getMessage()));
        return status;
      } else if (val.getSeverity() == IStatus.WARNING) {
        status.setWarning(MessageUtil.getFormattedString(name + ".warning.TypeNameDiscouraged", val.getMessage()));
      }
    }
    IPackageFragment pack = packageChooser.getPackageFragment();
    if (pack != null) {
      ICompilationUnit cu = pack.getCompilationUnit(typeName + ".java");
      if (cu.exists()) {
        status.setError(MessageUtil.getString(name + ".error.TypeNameExists"));
        return status;
      }
    }
    return status;
  }

  private IJavaSearchScope createSearchScope(IPackageFragmentRoot root) {
    IJavaProject jproject = root.getJavaProject();
    IJavaSearchScope result = null;
    IType hrootElement = null;
    try {
      if (hierarchyRoot != null) {
        hrootElement = Utils.findType(jproject, hierarchyRoot);
      }
      if (hrootElement != null) {
        result = SearchEngine.createHierarchyScope(hrootElement);
      }
    } catch (JavaModelException jmex) {
      //ignore
      jmex.printStackTrace();
    }
    if (result == null) {
      IJavaElement[] elements = new IJavaElement[] { jproject };
      result = SearchEngine.createJavaSearchScope(elements);
    }
    return result;
  }

  private IType chooseType() {
    IPackageFragmentRoot root = packageChooser.getContainer().getPackageFragmentRoot();
    if (root == null) {
      return null;
    }

    IJavaSearchScope scope = createSearchScope(root);
    try {

      SelectionDialog dialog =
        JavaUI.createTypeDialog(getShell(), getRunnableContext(), scope, IJavaElementSearchConstants.CONSIDER_CLASSES, false);

      dialog.setTitle(MessageUtil.getString(name + ".TypeDialog.title"));
      String message = MessageUtil.getString(name + ".TypeDialog.message");
      dialog.setMessage(hierarchyRoot == null ? message : message + " (extends/implements " + hierarchyRoot + ")");

      if (dialog.open() == dialog.OK) {
        return (IType) dialog.getResult()[0]; //FirstResult();
      }
    } catch (JavaModelException jmex) {
      TapestryPlugin.getDefault().logException(jmex);
    }
    return null;
  }

  protected IType resolveTypeName(IJavaProject jproject, String typeName) throws JavaModelException {
    IType type = null;
    IPackageFragment currPack = null;
    if (packageChooser != null) {
      packageChooser.getPackageFragment();
    } else {
      // create/get one!
    }
    if (type == null && currPack != null) {
      String packName = currPack.getElementName();
      // search in own package
      if (!currPack.isDefaultPackage()) {
        type = Utils.findType(jproject, packName, typeName);
      }
      // search in java.lang
      if (type == null && !"java.lang".equals(packName)) {
        type = Utils.findType(jproject, "java.lang", typeName);
      }
    }
    // search fully qualified
    if (type == null) {
      type = Utils.findType(jproject, typeName);
    }
    //}
    return type;
  }

  public IType getType() {
    return chosenType;
  }

  

  

}