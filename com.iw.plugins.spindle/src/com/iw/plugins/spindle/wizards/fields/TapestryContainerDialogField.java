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

import java.util.ArrayList;

import net.sf.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.ui.ChooseWorkspaceModelDialog;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;
import com.iw.plugins.spindle.ui.dialogfields.UneditableStringButtonField;
import com.iw.plugins.spindle.ui.dialogfields.UneditableStringField;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.ILookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;
import com.iw.plugins.spindle.wizards.NewTapComponentWizardPage;

public class TapestryContainerDialogField extends UneditableStringButtonField {

  private String name;

  private ComponentNameField nameField;

  private boolean isComponentWizard;

  private ContainerDialogField container;
  private TapestryLibraryModel currentModel;
  private IPackageFragment selectedPackage;

  private ITapestryProject tproject;

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
    boolean isComponentWizard,
    IRunnableContext context) {

    super.init(context);
    this.container = container;
    this.nameField = nameField;
    nameField.addListener(this);
    this.isComponentWizard = isComponentWizard;

    setButtonLabel(MessageUtil.getString(name + ".button"));
    setTextValue("");

    try {
      tproject =
        TapestryPlugin.getDefault().getTapestryProjectFor(
          container.getPackageFragmentRoot().getJavaProject());

      checkPreviouslySelectedApplication(tproject);
    } catch (CoreException e) {
    }

  }

  public void dialogFieldButtonPressed(DialogField field) {

    if (field == this) {

      TapestryLibraryModel model = chooseTapestryContainer();

      setContainerModel(model);

    }
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
    checkButtonEnabled();
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

  private void checkButtonEnabled() {
    if (container != null) {

      enableButton((container.getPackageFragmentRoot() != null) && isEnabled());

    }
  }

  public void setContainerModel(TapestryLibraryModel model) {

    currentModel = model;
    String str = "";
    if (model != null) {
      str =
        currentModel.getUnderlyingStorage().getName()
          + " - "
          + (selectedPackage == null ? "(default package)" : "" + selectedPackage.getElementName());
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

  private TapestryLibraryModel chooseTapestryContainer() {

    IPackageFragmentRoot froot = container.getPackageFragmentRoot();
    ChooseWorkspaceModelDialog dialog =
      ChooseWorkspaceModelDialog.createApplicationAndLibraryModelDialog(
        getShell(),
        froot.getJavaProject(),
        "Choose AutoAdd App or Library",
        "Choose an Application or Library to add to");

    dialog.setIgnoreReadOnly(true);

    if (dialog.open() == dialog.OK) {

      selectedPackage = dialog.getResultPackage();
      return (TapestryLibraryModel) dialog.getResultModel();

    }

    return currentModel;
  }

  /**
   * @see DialogField#setEnabled(boolean)
   */
  public void setEnabled(boolean flag) {
    super.setEnabled(flag);
    checkButtonEnabled();
  }

  private void checkPreviouslySelectedApplication(ITapestryProject tproject) {

    IPreferenceStore pstore = TapestryPlugin.getDefault().getPreferenceStore();
    String previouslySelected = pstore.getString(NewTapComponentWizardPage.P_ADD_TO_APPLICATION);

    if (previouslySelected != null && !"".equals(previouslySelected.trim())) {

      Path path = new Path(previouslySelected);

      if (path.isValidPath(previouslySelected)) {

        try {
          TapestryLookup lookup = tproject.getLookup();
          FinderRequest request = new FinderRequest();

          lookup.findAll(
            previouslySelected,
            false,
            TapestryLookup.ACCEPT_APPLICATIONS
              | TapestryLookup.ACCEPT_LIBRARIES
              | TapestryLookup.FULL_TAPESTRY_PATH,
            request);

          IStorage[] found = request.getStorageResults();
          if (found == null || found.length == 0) {
            return;
          }
          TapestryLibraryModel foundModel = null;

          try {
            TapestryProjectModelManager mgr = tproject.getModelManager();
            foundModel = (TapestryLibraryModel) mgr.getReadOnlyModel(found[0]);

          } catch (CoreException e) {
          }
          if (foundModel == null) {
            selectedPackage = null;
          } else {
            selectedPackage = request.getPackageResults()[0];
            setContainerModel(foundModel);
          }

        } catch (CoreException e) {
          e.printStackTrace();
          TapestryPlugin.getDefault().logException(e);
        }
      }
    }
  }

  protected class FinderRequest implements ILookupRequestor {

    ArrayList storages;
    ArrayList packages;

    public boolean isCancelled() {
      return false;
    }

    public boolean accept(IStorage storage, IPackageFragment fragment) {
      //filter out anything that came from a jar file
      if (storage.getAdapter(IResource.class) == null) {
        return false;
      }
      if (storages == null) {
        storages = new ArrayList();
        packages = new ArrayList();
      }
      storages.add(storage);
      packages.add(fragment);
      return true;
    }

    public IStorage[] getStorageResults() {
      if (storages == null) {
        return new IStorage[0];
      }
      return (IStorage[]) storages.toArray(new IStorage[0]);

    }

    public IPackageFragment[] getPackageResults() {
      if (packages == null) {
        return new IPackageFragment[0];
      }
      return (IPackageFragment[]) packages.toArray(new IPackageFragment[packages.size()]);
    }

    public ArrayList getPackageList() {
      if (packages == null) {
        return new ArrayList();
      }
      return packages;
    }

    public ArrayList getModelResults() {
      try {

        return tproject.getModelManager().getModelListFor(storages);
        
      } catch (CoreException e) {
        return new ArrayList();

      }
    }
  }

}