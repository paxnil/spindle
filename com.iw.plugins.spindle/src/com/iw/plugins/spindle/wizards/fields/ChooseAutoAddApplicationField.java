package com.iw.plugins.spindle.wizards.fields;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.dialogfields.DialogField;
import com.iw.plugins.spindle.dialogfields.DialogFieldStatus;
import com.iw.plugins.spindle.dialogfields.UneditableComboBoxDialogField;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.TapestryLookup;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.wizards.NewTapComponentWizardPage;

/**
 * @author administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ChooseAutoAddApplicationField extends UneditableComboBoxDialogField {
  ArrayList appModels;
  ArrayList appPackages;

  /**
   * Constructor for ChooseAutoAddApplicationField.
   * @param labelText
   * @param labelWidth
   * @param values
   */
  public ChooseAutoAddApplicationField(String labelText, int labelWidth, String[] values) {
    super(labelText, labelWidth, values);
  }

  /**
   * Constructor for ChooseAutoAddApplicationField.
   * @param labelText
   * @param values
   */
  public ChooseAutoAddApplicationField(String labelText, String[] values) {
    super(labelText, values);
  }

  public void init(IJavaElement element) {
    try {
      IJavaProject project = (IJavaProject) Utils.findElementOfKind(element, IJavaElement.JAVA_PROJECT);
      TapestryLookup lookup = new TapestryLookup();
      lookup.configure(project);
      FinderRequest request = new FinderRequest();
      lookup.findAll("*", true, TapestryLookup.ACCEPT_APPLICATIONS, request);
      IStorage[] appResources = request.getStorageResults();
      appPackages = request.getPackageList();
      appModels = request.getModelResults();
      checkPreviouslySelectedApplication(project);
      getUneditableComboBoxControl(null).setItems(getRenderedApps());
      select(0);
    } catch (JavaModelException e) {
    }
  }

  /**
   * Method checkPreviouslySelectedApplication.
   * pull the previously selected app from the PreferenceStore
   * Convert the stored value into an ITapestryModel and an IPackageFragment
   * If that's successful, ensure the they is the first in the lists.
   */
  private void checkPreviouslySelectedApplication(IJavaProject project) {

    IPreferenceStore pstore = TapestryPlugin.getDefault().getPreferenceStore();
    String previouslySelected = pstore.getString(NewTapComponentWizardPage.P_ADD_TO_APPLICATION);

    if (previouslySelected != null && !"".equals(previouslySelected.trim())) {
      ITapestryModel previouslySelectedModel = null;
      IPackageFragment previouslySelectedPackage = null;

      Path path = new Path(previouslySelected);

      if (path.isValidPath(previouslySelected)) {

        try {

          TapestryLookup lookup = new TapestryLookup();
          lookup.configure(project);
          FinderRequest request = new FinderRequest();

          lookup.findAll(
            previouslySelected,
            false,
            TapestryLookup.ACCEPT_APPLICATIONS | TapestryLookup.FULL_TAPESTRY_PATH,
            request);

          IStorage[] found = request.getStorageResults();
          if (found == null || found.length == 0) {
            return;
          }
          previouslySelectedModel = TapestryPlugin.getTapestryModelManager().getModel(found[0]);
          if (previouslySelectedModel == null) {
            return;
          }
          previouslySelectedPackage = request.getPackageResults()[0];

        } catch (JavaModelException e) {
          e.printStackTrace();
        }

        if (appModels != null && appModels.size() > 0) {
          // check if the previously selected is already in the list,
          // if so it must be moved to the top
          // if not, it must be added to the top
          int location = -1;
          for (int i = 0; i < appModels.size(); i++) {
            if (previouslySelectedModel == appModels.get(i)) {
              location = i;

              break;
            }
          }
          if (location == 0) {
            // its already in the right place!
            return;
          }
          if (location > 0) {
            // gotta move it!
            appModels.remove(previouslySelectedModel);
            appPackages.remove(previouslySelectedPackage);
          }
          // its not in the list, need to add it to the front
          appModels.add(0, previouslySelectedModel);
          appPackages.add(0, previouslySelectedPackage);

        } else {
          // easy case, the array of models is empty, just
          // add the previously selected to the list.
          appModels = new ArrayList();
          appModels.add(previouslySelectedModel);
          appPackages = new ArrayList();
          appPackages.add(previouslySelectedPackage);
        }        
      }
    }
  }
  /**
   * Method getRenderedApps.
   * @return String[]
   */
  private String[] getRenderedApps() {
    String[] result = new String[appModels.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = ((ITapestryModel) appModels.get(i)).getUnderlyingStorage().getName();
      result[i] += " - ";
      result[i] += ((IPackageFragment) appPackages.get(i)).getElementName();
    }
    return result;

  }

  /**
   * @see com.iw.plugins.spindle.dialogfields.DialogField#fireDialogChanged(DialogField)
   */
  protected void fireDialogChanged(DialogField field) {
    if (field == this) {
      setStatus(selectionChanged());
    }
  }

  /**
   * Method selectionChanged.
   */
  private IStatus selectionChanged() {
    DialogFieldStatus newStatus = new DialogFieldStatus();
    if (isEnabled()) {
      int index = getSelectedIndex();
      if (index > 0) {
        TapestryApplicationModel selected = getSelectedModel();
        if (selected.getApplicationSpec() == null) {
          try {
            selected.load();
          } catch (CoreException e) {
          }
          if (selected.getApplicationSpec() == null) {
            newStatus.setError("failed to resolve " + getSelectedValue());
            return newStatus;
          }
        }
      }
    }
    return newStatus;
  }

  public TapestryApplicationModel getSelectedModel() {
    TapestryApplicationModel selectedModel = null;
    int index = getSelectedIndex();
    if (index >= 0) {
      selectedModel = (TapestryApplicationModel) appModels.get(index);
    }
    return selectedModel;
  }

  /**
  * Method getSelectedPackage.
  * @return IPackageFragment
  */
  public IPackageFragment getSelectedPackage() {
    IPackageFragment selectedPackage = null;
    int index = getSelectedIndex();
    if (index >= 0) {
      selectedPackage = (IPackageFragment) appPackages.get(index);
    }
    return selectedPackage;
  }

  protected class FinderRequest implements ITapestryLookupRequestor {

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
      if (storages == null) {
        return new ArrayList();
      }
      return TapestryPlugin.getTapestryModelManager().getModelListFor(storages);
    }
  }

}
