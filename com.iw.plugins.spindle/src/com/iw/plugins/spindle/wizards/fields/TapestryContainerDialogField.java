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

import net.sf.tapestry.spec.ILibrarySpecification;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.widgets.Text;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringField;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public class TapestryContainerDialogField extends StringField {

  private String name;

  private ComponentNameField nameField;

  private boolean isComponentWizard;

  private ContainerDialogField container;
  private TapestryLibraryModel currentModel;
  private IPackageFragment selectedPackage;

  private ITapestryProject tproject;
  private TapestryLookup lookup;

  public TapestryContainerDialogField(String name, int labelWidth) {
    super(MessageUtil.getString(name + ".label"), labelWidth);
    this.name = name;
  }

  public TapestryContainerDialogField(String name) {
    this(name, -1);
  }

  public void init(
    ContainerDialogField container,
    ComponentNameField nameField,
    boolean isComponentWizard) {

    this.container = container;
    this.nameField = nameField;
    nameField.addListener(this);
    this.isComponentWizard = isComponentWizard;

    TapestryLibraryModel projectModel = null;

    try {
      tproject =
        TapestryPlugin.getDefault().getTapestryProjectFor(
          container.getPackageFragmentRoot().getJavaProject());

      lookup = tproject.getLookup();

      projectModel = (TapestryLibraryModel) tproject.getProjectModel();

    } catch (CoreException e) {

    }

    setContainerModel(projectModel);

    ((Text) getTextControl(null)).setEditable(false);

  }

  public void dialogFieldChanged(DialogField field) {
    if (field == this || field == nameField) {

      updateStatus();

    }

  }

  public void updateStatus() {

    setStatus(containerChanged());

  }

  public IStatus containerChanged() {

    SpindleStatus newStatus = new SpindleStatus();
    String libraryName = getTextValue();

    if (currentModel == null) {
      //    if (libraryName == null || "".equals(libraryName)) {
      newStatus.setError("");
      return newStatus;
    }

    if (existsInNonSpindleEditor(currentModel)) {
      newStatus.setError(
        currentModel.getUnderlyingStorage().getName() + " is open in an non-Spindle editor");
      return newStatus;
    }
    if (currentModel.getSpecification() == null) {
      try {
        currentModel.load();
      } catch (CoreException e) {
      }
      if (currentModel.getSpecification() == null) {
        newStatus.setError("failed to resolve " + libraryName);
        return newStatus;
      }
    }
    ILibrarySpecification spec = currentModel.getSpecification();
    String newName = nameField.getTextValue();
    if (isComponentWizard && spec.getComponentSpecificationPath(newName) != null) {
      newStatus.setError(
        newName
          + " already exists as a component in "
          + currentModel.getUnderlyingStorage().getName());
      return newStatus;
    } else if (spec.getPageSpecificationPath(newName) != null) {
      newStatus.setError(
        newName + " already exists as page in " + currentModel.getUnderlyingStorage().getName());
      return newStatus;
    }

    return newStatus;
  }

  private boolean existsInNonSpindleEditor(TapestryLibraryModel model) {
    Object editor = Utils.getEditorFor(model.getUnderlyingStorage());
    return (editor != null && !(editor instanceof SpindleMultipageEditor));

  }

  public void setContainerModel(TapestryLibraryModel model) {

    currentModel = model;
    String str = "auto add not possible";

    selectedPackage = null;
    try {

      if (model != null) {

        selectedPackage = lookup.findPackageFragment(model.getUnderlyingStorage());
        if (model != null) {
          str = currentModel.getUnderlyingStorage().getName() + " - ";
          if (selectedPackage != null) {
            str
              += ("".equals(selectedPackage.getElementName().trim())
                ? "(default package)"
                : "" + selectedPackage.getElementName());
          }
        }

      }

    } catch (JavaModelException e) {
    }
    setTextValue(str);
    fireDialogChanged(this);

  }

  public TapestryLibraryModel getContainerModel() {

    return currentModel;

  }

  public IPackageFragment getSelectedPackage() {
    return selectedPackage;
  }

}