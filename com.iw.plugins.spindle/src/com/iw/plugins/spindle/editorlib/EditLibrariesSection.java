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
package com.iw.plugins.spindle.editorlib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.tapestry.spec.ILibrarySpecification;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.ui.descriptors.TypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.WorkspaceStoragePropertyDescriptor;
import com.iw.plugins.spindle.ui.dialogfields.DialogFieldStatus;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public class EditLibrariesSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener {

  private Button revertButton;
  private Action revertAction;

  /**
   * Constructor for PropertySection 
   */
  public EditLibrariesSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(new LibraryLabelProvider());
    setNewAction(new NewLibraryAction());
    setDeleteAction(new DeleteLibraryAction());
    setHeaderText("Libraries");
    setDescription("In this section you can work with libraries");
  }

  public void dispose() {
    super.dispose();
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("libraries")) {
        updateNeeded = true;
      }
    }
  }

  private boolean checkSelfAdd(String potentialLibrary) {

    ITapestryModel model = getModel();
    IStorage modelStorage = model.getUnderlyingStorage();

    if (model.getClass() == TapestryLibraryModel.class) {

      Path path = new Path(potentialLibrary);

      if (!modelStorage.getName().equals(path.lastSegment())) {

        return false;

      }

      String potentialPackage = path.removeLastSegments(1).toString();
      potentialPackage = (potentialPackage.substring(1)).replace('/', '.');

      try {
      	
        ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(modelStorage);

        TapestryLookup lookup = project.getLookup();

        IPackageFragment fragment = lookup.findPackageFragment(modelStorage);

        if (potentialPackage.equals(fragment.getElementName())) {

          return true;
        }
        
      } catch (JavaModelException e) {
      } catch (CoreException e) {
      }

    }
    return false;

  }

  public void update(BaseTapestryModel model) {
    holderArray.removeAll(holderArray);

    ILibrarySpecification spec =
      (ILibrarySpecification) ((TapestryLibraryModel) model).getSpecification();

    List myLibraries = spec.getLibraryIds();

    Iterator iter = myLibraries.iterator();

    while (iter.hasNext()) {
      String name = (String) iter.next();
      LibraryHolder holder = new LibraryHolder(name, spec.getLibrarySpecificationPath(name));
      holderArray.add(holder);
    }
    setInput(holderArray);
  }

  public class LibraryLabelProvider extends AbstractIdentifiableLabelProvider {

    private Image image = TapestryImages.getSharedImage("library16.gif");

    public Image getImage(Object object) {
      return image;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
     */
    public String getText(Object element) {

      LibraryHolder holder = (LibraryHolder) element;
      return holder.getIdentifier() + " = " + holder.getPropertyValue("path");
    }

  }

  class DeleteLibraryAction extends Action {

    private ITapestryModel model;

    protected DeleteLibraryAction() {
      super();
      setText("Delete");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      LibraryHolder holder = (LibraryHolder) getSelected();
      if (holder != null) {
        String prev = findPrevious(holder.identifier);
        ((IPluginLibrarySpecification) holder.getParent()).removeLibrarySpecificationPath(
          holder.getIdentifier());
        forceDirty();
        update();
        if (prev != null) {
          setSelection(prev);
        } else {
          selectFirst();
        }
      }
      updateSelection = false;
    }

  }

  class NewLibraryAction extends Action {

    private ITapestryModel model;

    protected NewLibraryAction() {
      super();
      setText("New");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      TapestryLibraryModel model = (TapestryLibraryModel) getModel();
      ILibrarySpecification spec = (PluginLibrarySpecification) model.getSpecification();
      String useLibraryName = "library";
      if (spec.getLibrarySpecificationPath(useLibraryName + 1) != null) {
        int counter = 2;
        while (spec.getLibrarySpecificationPath(useLibraryName + counter) != null) {
          counter++;
        }
        useLibraryName = useLibraryName + counter;
      } else {
        useLibraryName = useLibraryName + 1;
      }
      spec.setLibrarySpecificationPath(useLibraryName, "fill in value");
      forceDirty();
      update();
      setSelection(useLibraryName);
      updateSelection = false;
    }

  }

  protected class LibraryHolder implements IPropertySource, IIdentifiable {

    private String identifier;
    private String specificationPath;

    /**
     * Constructor for PropertyHolder
     */
    public LibraryHolder(String identifier, String specificationPath) {
      super();
      this.identifier = identifier;
      this.specificationPath = specificationPath;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getIdentifier()
     */
    public String getIdentifier() {
      return identifier;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getParent()
     */
    public Object getParent() {
      TapestryLibraryModel model = (TapestryLibraryModel) getFormPage().getModel();

      return model.getSpecification();
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setIdentifier(String)
     */
    public void setIdentifier(String id) {
      identifier = id;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
     */
    public void setParent(Object parent) {
    }

    public void resetPropertyValue(Object key) {
      if ("Name".equals(key)) {
        identifier = null;
      }
    }

    public void setPropertyValue(Object key, Object value) {
      IPluginLibrarySpecification spec = (IPluginLibrarySpecification) getParent();
      if ("name".equals(key)) {

        String oldName = this.identifier;
        String newName = ((String) value).trim();

        if ("".equals(newName) || oldName.equals(newName)) {

          return;

        } else if (spec.getLibrarySpecificationPath(newName) != null) {

          newName = "Copy of " + newName;
        }
        this.identifier = newName;
        spec.removeLibrarySpecificationPath(oldName);
        spec.setLibrarySpecificationPath(this.identifier, this.specificationPath);

      } else if ("path".equals(key)) {

        String potentialValue = (String) value;

        DialogFieldStatus status = new DialogFieldStatus();

        if ("/net/sf/tapestry/Framework.library".equals(potentialValue)) {

          status.setError("/net/sf/tapestry/Framework.library is implied");

        }

        if (checkSelfAdd(potentialValue)) {

          status.setError("Can't add " + potentialValue + " to itself");
        }

        if (status.isError()) {

          ErrorDialog.openError(
            newButton.getShell(),
            "Spindle Error",
            "Could not add library: " + potentialValue,
            status);

        } else {

          this.specificationPath = (String) value;
          spec.setLibrarySpecificationPath(this.identifier, this.specificationPath);
        }

      }

    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
        return identifier != null;
      } else if ("path".equals(key)) {
        return specificationPath != null;
      }
      return false;
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
        return identifier;
      } else if ("path".equals(key)) {
        return specificationPath;
      }
      return null;
    }
    public IPropertyDescriptor[] getPropertyDescriptors() {
      return new IPropertyDescriptor[] {
        new TextPropertyDescriptor("name", "Library Name"),
        new WorkspaceStoragePropertyDescriptor(
          "path",
          "Spec Path",
          "Library Choose",
          "Choose a Library",
          TapestryLookup.ACCEPT_LIBRARIES),
        };
    }

    public Object getEditableValue() {
      return identifier;
    }

  }

}
