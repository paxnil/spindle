/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.wizards.fields;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;

public class PackageDialogField extends StringButtonField
{

  private String name;
  private ContainerDialogField container;
  private AbstractNameField nameField;
  private IPackageFragment currentPackage;
  private boolean fSourcePackagesOnly = false;

  /**
   * Constructor for PackageDialogField.
   * 
   * @param label
   * @param defaultLabel
   */
  public PackageDialogField(String name, int labelWidth)
  {
    super(UIPlugin.getString(name + ".label"), labelWidth);
    this.name = name;

  }

  public void setAcceptSourcePackagesOnly(boolean flag)
  {
    fSourcePackagesOnly = flag;
  }

  /**
   * Constructor for PackageDialogField.
   * 
   * @param label
   * @param defaultLabel
   * @param labelWidth
   */

  /**
   * Constructor for PackageDialogField
   */
  public PackageDialogField(String name)
  {
    this(name, -1);

  }

  public void init(
      AbstractNameField nameField,
      ContainerDialogField container,
      IRunnableContext context)
  {
    this.nameField = nameField;
    nameField.addListener(this);
    this.init(container, context);
  }

  public void init(ContainerDialogField container, IRunnableContext context)
  {
    super.init(context);
    this.container = container;
    container.addListener(this);
    setButtonLabel(UIPlugin.getString(name + ".button"));
    setTextValue("");
  }

  public ContainerDialogField getContainer()
  {
    return container;
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field)
  {
    if (field == this)
    {
      IPackageFragment pack = choosePackage();
      if (pack != null)
      {
        setPackageFragment(pack);
      }
    }
  }

  public void dialogFieldChanged(DialogField field)
  {
    if (field == this || field == container)
      refreshStatus();

    if (nameField != null && field == nameField)
      refreshStatus();
  }

  public void refreshStatus()
  {
    setStatus(packageChanged());
  }

  public IStatus packageChanged()
  {
    SpindleStatus status = new SpindleStatus();
    checkButtonEnabled();
    String packName = getTextValue();
    if (!"".equals(packName))
    {
      IStatus val = JavaConventions.validatePackageName(packName);
      if (val.getSeverity() == IStatus.ERROR)
      {
        status.setError(UIPlugin.getString(name + ".error.InvalidPackageName", val
            .getMessage()));
        return status;
      } else if (val.getSeverity() == IStatus.WARNING)
      {
        status.setWarning(UIPlugin.getString(
            name + ".warning.DiscouragedPackageName",
            val.getMessage()));
        // continue
      }
    } else
    {

      status.setError(UIPlugin.getString(name + ".error.defaultPackage"));
      return status;
    }

    IPackageFragmentRoot root;
    if (container == null)
    {
      root = null;
    } else
    {
      root = container.getPackageFragmentRoot();
    }
    if (root != null)
    {
      IPackageFragment pack = root.getPackageFragment(packName);
      try
      {
        if (fSourcePackagesOnly && root.getKind() == IPackageFragmentRoot.K_BINARY)
        {
          status.setError(UIPlugin.getString(name + ".error.PackageMustBeSource"));
          return status;
        }

        IPath rootPath = root.getPath();
        IPath outputPath = root.getJavaProject().getOutputLocation();
        if (rootPath.isPrefixOf(outputPath) && !rootPath.equals(outputPath))
        {
          // if the bin folder is inside of our root, dont allow to name a
          // package
          // like the bin folder
          IPath packagePath = pack.getUnderlyingResource().getFullPath();
          if (outputPath.isPrefixOf(packagePath))
          {
            status.setError(UIPlugin.getString(name + ".error.ClashOutputLocation"));
            return status;
          }
        }
      } catch (JavaModelException e)
      {
        UIPlugin.log(e);
        // let pass
      }

      currentPackage = pack;
      if (currentPackage != null && nameField != null)
      {
        try
        {
          IContainer folder = (IContainer) getPackageFragment().getUnderlyingResource();

          boolean isComponent = nameField.getKind() == AbstractNameField.COMPONENT_NAME;
          IFile file = folder.getFile(new Path(nameField.getTextValue()
              + (isComponent ? ".jwc" : ".page")));
          if (file.exists())
          {

            status.setError(UIPlugin.getString(
                name + ".error.ComponentAlreadyExists",
                file.getFullPath().toString()));
            return status;
          }
        } catch (JavaModelException e)
        {
          UIPlugin.log(e);
        }
      }
    } else
    {
      status.setError("");
    }
    return status;
  }

  private void checkButtonEnabled()
  {
    if (container != null)
    {
      enableButton((container.getPackageFragmentRoot() != null) && isEnabled());
    }
  }

  public void setPackageFragment(IPackageFragment fragment)
  {
    IPackageFragment old = currentPackage;
    currentPackage = fragment;
    String str = (fragment == null) ? "" : fragment.getElementName();
    setTextValue(str);
    fireDialogFieldChanged(this);
  }

  public IPackageFragment getPackageFragment()
  {
    return currentPackage;
  }

  private IPackageFragment choosePackage()
  {
    IPackageFragmentRoot froot = container.getPackageFragmentRoot();
    try
    {
      SelectionDialog dialog = JavaUI.createPackageDialog(getShell(), froot, "");
      dialog.setTitle(UIPlugin.getString(name + ".ChoosePackageDialog.title"));
      dialog.setMessage(UIPlugin.getString(name + ".ChoosePackageDialog.description"));
      if (currentPackage != null)
      {
        dialog.setInitialSelections(new Object[]{currentPackage});
      }
      if (dialog.open() == Window.OK)
      {
        return (IPackageFragment) dialog.getResult()[0];
      }
    } catch (JavaModelException e)
    {
      UIPlugin.log(e);
    }
    return null;
  }

  /**
   * @see DialogField#setEnabled(boolean)
   */
  public void setEnabled(boolean flag)
  {
    super.setEnabled(flag);
    checkButtonEnabled();
  }

}