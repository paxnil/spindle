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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonDefaultField;
import com.iw.plugins.spindle.util.SpindleStatus;

public class PackageDialogField extends StringButtonDefaultField {

  private String name;
  private ContainerDialogField container;
  private IPackageFragment currentPackage;

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

  public void init(ContainerDialogField container, IRunnableContext context) {
    super.init(context);
    this.container = container;
    container.addListener(this);
    setButtonLabel(MessageUtil.getString(name + ".button"));
    setTextValue("");
  }

  public ContainerDialogField getContainer() {
    return container;
  }

  /**
  * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
  */
  public void dialogFieldButtonPressed(DialogField field) {
    if (field == this) {
      IPackageFragment pack = choosePackage();
      if (pack != null) {
        setPackageFragment(pack);
      }
    }
  }

  public void dialogFieldChanged(DialogField field) {
    if (field == this || field == container) {
      setStatus(packageChanged());
    }

  }

  public IStatus packageChanged() {
    SpindleStatus status = new SpindleStatus();
    checkButtonEnabled();
    String packName = getTextValue();
    if (!"".equals(packName)) {
      IStatus val = JavaConventions.validatePackageName(packName);
      if (val.getSeverity() == IStatus.ERROR) {
        status.setError(MessageUtil.getFormattedString(name + ".error.InvalidPackageName", val.getMessage()));
        return status;
      } else if (val.getSeverity() == IStatus.WARNING) {
        status.setWarning(MessageUtil.getFormattedString(name + ".warning.DiscouragedPackageName", val.getMessage()));
        // continue
      }
    }

    IPackageFragmentRoot root;
    if (container == null) {
      root = null;
    } else {
      root = container.getPackageFragmentRoot();
    }
    if (root != null) {
      IPackageFragment pack = root.getPackageFragment(packName);
      try {
        IPath rootPath = root.getPath();
        IPath outputPath = root.getJavaProject().getOutputLocation();
        if (rootPath.isPrefixOf(outputPath) && !rootPath.equals(outputPath)) {
          // if the bin folder is inside of our root, dont allow to name a package
          // like the bin folder
          IPath packagePath = pack.getUnderlyingResource().getFullPath();
          if (outputPath.isPrefixOf(packagePath)) {
            status.setError(MessageUtil.getString(name + ".error.ClashOutputLocation"));
            return status;
          }
        }
      } catch (JavaModelException e) {
        TapestryPlugin.getDefault().logException(e);
        // let pass			
      }

      currentPackage = pack;
    } else {
      status.setError("");
    }
    return status;
  }

  private void checkButtonEnabled() {
    if (container != null) {
      enableButton((container.getPackageFragmentRoot() != null) && isEnabled());
    }
  }

  public void setPackageFragment(IPackageFragment fragment) {
    IPackageFragment old = currentPackage;
    currentPackage = fragment;
    String str = (fragment == null) ? "" : fragment.getElementName();
    setTextValue(str);
    updatePackageStatusLabel();
    fireDialogChanged(this);
  }

  public IPackageFragment getPackageFragment() {
    return currentPackage;
  }

  private IPackageFragment choosePackage() {
    IPackageFragmentRoot froot = container.getPackageFragmentRoot();
    try {
      SelectionDialog dialog = JavaUI.createPackageDialog(getShell(), froot, "");
      dialog.setTitle(MessageUtil.getString(name + ".ChoosePackageDialog.title"));
      dialog.setMessage(MessageUtil.getString(name + ".ChoosePackageDialog.description"));
      if (currentPackage != null) {
        dialog.setInitialSelections(new Object[] { currentPackage });
      }
      if (dialog.open() == dialog.OK) {
        return (IPackageFragment) dialog.getResult()[0];
      }
    } catch (JavaModelException e) {
      TapestryPlugin.getDefault().logException(e);
    }
    return null;
  }

  private void updatePackageStatusLabel() {
    String packName = getTextValue();

    if (packName == null || "".equals(packName)) {
      setDefaultLabelText(MessageUtil.getString(name + ".default"));
    } else {
      setDefaultLabelText(" ");
    }
  }

  /**
   * @see DialogField#setEnabled(boolean)
   */
  public void setEnabled(boolean flag) {
    super.setEnabled(flag);
    checkButtonEnabled();
  }

}