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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
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

public class RawTypeDialogField extends StringButtonField {

  protected String name;
  protected IType chosenType;
  private String hierarchyRoot;
  private IJavaProject jproject;
  private IType requiredType;
  
  public RawTypeDialogField(String name, String hierarchyRoot, int labelWidth) {
    super(MessageUtil.getString(name + ".label"), labelWidth);
    this.name = name;
    this.hierarchyRoot = hierarchyRoot;
  }

  public RawTypeDialogField(String name, String hierarchyRoot) {
    this(name, hierarchyRoot, -1);
  }

  /**
   * Constructor for TypeDialogField
   */
  public RawTypeDialogField(String name) {
    this(name, null, -1);

  }

  public RawTypeDialogField(String name, int labelWidth) {
    this(name, null, labelWidth);
  }

  public void init(IJavaProject jproject, IRunnableContext context) {
    super.init(context);
    this.jproject = jproject;
    try {
      requiredType = resolveTypeName( hierarchyRoot);
    } catch (JavaModelException e) {
      TapestryPlugin.getDefault().logException(e);
    }
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
    //IPackageFragmentRoot root = packageChooser.getContainer().getPackageFragmentRoot();
    chosenType = null;
    SpindleStatus status = new SpindleStatus();
    String typeName = getTextValue();
    if ("".equals(typeName)) {
      status.setError(MessageUtil.getString(name + ".error.EnterTypeName"));
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
    try {
      chosenType = resolveTypeName(typeName);
      if (chosenType == null) {
        status.setError(MessageUtil.getString(name + ".error.TypeNameNotExist"));
        return status;
      }
      if (requiredType != null) {
        if (requiredType.isInterface()) {
          if (!implementsInterface(chosenType, hierarchyRoot)) {
            status.setError(MessageUtil.getFormattedString(name + ".error.MustImplementInterface", hierarchyRoot));
          } else if (!extendsType(chosenType, requiredType)) {
            status.setError(MessageUtil.getFormattedString(name + ".error.MustExtendClass", hierarchyRoot));
          }
        }

      }
    } catch (JavaModelException e) {
      TapestryPlugin.getDefault().logException(e);
    }

    return status;
  }

  private IJavaSearchScope createSearchScope() {
    IJavaSearchScope result = null;
    IType hrootElement = null;
    try {
      if (hierarchyRoot != null) {
        hrootElement = resolveTypeName( hierarchyRoot);
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

    IJavaSearchScope scope = createSearchScope();
    
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

  protected IType resolveTypeName(String typeName) throws JavaModelException {
    return jproject.findType( typeName);
  }

  public IType getType() {
    return chosenType;
  }

  protected boolean extendsType(IType candidate, IType baseType) throws JavaModelException {
    boolean match = false;
    ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
    if (hierarchy.exists()) {
      IType[] superClasses = hierarchy.getAllSupertypes(candidate);
      for (int i = 0; i < superClasses.length; i++) {
        if (superClasses[i].equals(baseType)) {
          match = true;
        }
      }
    }
    return match;
  }

  protected boolean implementsInterface(IType candidate, String interfaceName) throws JavaModelException {
    if (interfaceMatch(candidate, interfaceName)) {
      return true;
    }
    ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
    if (hierarchy.exists()) {
      IType[] superClasses = hierarchy.getAllSupertypes(candidate);
      for (int i = 0; i < superClasses.length; i++) {
        if (interfaceMatch(superClasses[i], interfaceName)){
          return true;
        }
      }
    }
    return false;
  }
  
  private boolean interfaceMatch(IType candidate, String interfaceName) throws JavaModelException {
    boolean match = false;
    String[] superInterfaces = candidate.getSuperInterfaceNames();
    if (superInterfaces != null && superInterfaces.length > 0) {
      for (int i = 0; i < superInterfaces.length; i++) {
        if (candidate.isBinary() && interfaceName.endsWith(superInterfaces[i])) {
          match = true;
        } else {
          match = interfaceName.equals(superInterfaces[i]);
        }
      }
    } else {
      match = false;
    }
    return match;
  }

}