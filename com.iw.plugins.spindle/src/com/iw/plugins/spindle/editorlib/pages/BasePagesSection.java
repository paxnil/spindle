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
package com.iw.plugins.spindle.editorlib.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.ui.ChooseWorkspaceModelDialog;
import com.iw.plugins.spindle.ui.OpenTapestryPath;
import com.iw.plugins.spindle.ui.descriptors.ComponentTypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public abstract class BasePagesSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener, ISelectionChangedListener {

  private Action newPageAction = new AddPageAction();
  private Action deletePageAction = new DeletePageAction();
  private OpenTapestryPath openComponentAction =
    new OpenTapestryPath(TapestryLookup.ACCEPT_PAGES | TapestryLookup.FULL_TAPESTRY_PATH);

  private TreeViewer viewer;
  /**
   * Constructor for ComponentAliasSection
   */
  public BasePagesSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Pages");
    setLabelProvider(new PageLabelProvider());
    setNewAction(newPageAction);
    setDeleteAction(deletePageAction);

  }

  protected void fillContextMenu(IMenuManager manager) {

    Object selected = getSelected();
    boolean isEditable = isModelEditable();
    if (isEditable) {

      manager.add(newPageAction);

      if (selected != null) {

        manager.add(new Separator());
        manager.add(deletePageAction);
        manager.add(new Separator());
        try {
          ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(getModel());
          TapestryLookup lookup = project.getLookup();

          IIdentifiable pageHolder = (IIdentifiable) selected;
          String pageName = pageHolder.getIdentifier();
          IPluginLibrarySpecification spec = (IPluginLibrarySpecification) pageHolder.getParent();
          String tapestryPath = spec.getPageSpecificationPath(pageName);

          if (tapestryPath == null) {

            // we must resolve it from Framework.library
            TapestryProjectModelManager mgr;
            try {
              mgr = TapestryPlugin.getDefault().getTapestryModelManager(getModel().getUnderlyingStorage());

              ILibrarySpecification framework = mgr.getDefaultLibrary().getSpecification();

              tapestryPath = framework.getPageSpecificationPath(pageName);
            } catch (CoreException e) {
            }
          }
          if (tapestryPath != null) {
            openComponentAction.configure(lookup, tapestryPath);
            if (openComponentAction.isEnabled()) {
              MenuManager jumpMenu = new MenuManager("Jump To...");
              jumpMenu.add(openComponentAction);
              manager.add(jumpMenu);
              manager.add(new Separator());
            }

          }
        } catch (CoreException e) {
        }
        manager.add(pAction);
      }
    }

  }

  public void selectionChanged(SelectionChangedEvent event) {
    IIdentifiable pageSpec = (IIdentifiable) getSelected();
    boolean isEditable = isModelEditable();

    if (pageSpec == null) {

      fireSelectionNotification(null);
      inspectButton.setEnabled(false);
      deleteButton.setEnabled(false);

    } else {

      fireSelectionNotification(pageSpec.getIdentifier());
      inspectButton.setEnabled(isEditable);
      deleteButton.setEnabled(isEditable);

      if ((hasFocus || updateSelection) && isEditable) {
        setPageSelection();
      }

    }
    newButton.setEnabled(isEditable);

  }

  public void update(BaseTapestryModel model) {
    holderArray.removeAll(holderArray);

    IPluginLibrarySpecification libSpec = getSpec();

    List ids = libSpec.getPageNames();

    try {
      TapestryProjectModelManager mgr;
      mgr = TapestryPlugin.getDefault().getTapestryModelManager(getModel().getUnderlyingStorage());

      IPluginLibrarySpecification defaultSpec = mgr.getDefaultLibrary().getSpecification();

      ArrayList defaultIds = (ArrayList) ((ArrayList) defaultSpec.getPageNames()).clone();

      defaultIds.removeAll(ids);

      for (Iterator iter = defaultIds.iterator(); iter.hasNext();) {

        String defaultName = (String) iter.next();
        String path = defaultSpec.getPageSpecificationPath(defaultName);

        holderArray.add(new DefaultPageHolder(defaultName, path, libSpec));

      }
    } catch (CoreException e) {
    }

    Iterator iter = ids.iterator();

    while (iter.hasNext()) {

      String name = (String) iter.next();
      String path = libSpec.getPageSpecificationPath(name);

      holderArray.add(new PageHolder(name, path, libSpec));
    }

    setInput(holderArray);

    boolean isEditable = isModelEditable();
    if (newButton != null) {
      newButton.setEnabled(isEditable);
    }
    if (deleteButton != null) {
      deleteButton.setEnabled(isEditable);
    }
    if (inspectButton != null) {
      inspectButton.setEnabled(isEditable);
    }
    //selectFirst();
    updateNeeded = false;
  }

  public void modelChanged(IModelChangedEvent event) {
    int eventType = event.getChangeType();
    if (eventType == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
      return;
    }
    if (eventType == IModelChangedEvent.CHANGE) {
      updateNeeded = event.getChangedProperty().equals("pageMap");
    }
  }

  protected abstract IPluginLibrarySpecification getSpec();

  protected boolean checkPagePathOk(String potentialValue) {

    SpindleStatus status = new SpindleStatus();

    if (potentialValue.startsWith("/net/sf/tapestry")) {

      status.setError(potentialValue + " is already defined by default");

    }

    if (status.isError()) {

      ErrorDialog.openError(newButton.getShell(), "Spindle Error", "Could not add Page: " + potentialValue, status);

      return false;

    }

    return true;
  }

  protected class PageLabelProvider extends AbstractIdentifiableLabelProvider {

    Image pageImage;
    Image defaultPageImage;

    public PageLabelProvider() {
      pageImage = TapestryImages.getSharedImage("page16.gif");
      defaultPageImage = TapestryImages.getSharedImage("defaultPage16.gif");
    }

    public Image getImage(Object element) {

      if (element instanceof DefaultPageHolder) {

        return defaultPageImage;
      }
      return pageImage;
    }

  }

  protected class AddPageAction extends Action {

    protected AddPageAction() {
      super();
      setText("Add");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      try {
        ILibrarySpecification specification = getSpec();

        ChooseWorkspaceModelDialog dialog =
          new ChooseWorkspaceModelDialog(
            newButton.getShell(),
            TapestryPlugin.getDefault().getJavaProjectFor(getModel().getUnderlyingStorage()),
            "Choose Page",
            "Choose the Page to be added",
            TapestryLookup.ACCEPT_PAGES,
            true);

        dialog.create();
        if (dialog.open() == dialog.OK) {

          String pageName = dialog.getResultString();
          IPackageFragment pagePackage = dialog.getResultPackage();
          String path = dialog.getResultPath();

          if (checkPagePathOk(path)) {
            updateSelection = true;

            int index = pageName.indexOf(".");

            pageName = pageName.substring(0, index);

            if (specification.getPageSpecificationPath(pageName) != null) {

              int count = 1;
              while (specification.getPageSpecificationPath(pageName + count) != null) {
                count++;
              }

              pageName += count;
            }

            specification.setPageSpecificationPath(pageName, path);
            forceDirty();
            update();
            setSelection(pageName);
            updateSelection = false;
          }
        }
      } catch (CoreException e) {
      }
    }
  }

  protected class DeletePageAction extends Action {

    protected DeletePageAction() {
      super();
      setText("Delete");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PageHolder pageSpec = (PageHolder) getSelected();
      if (pageSpec != null) {

        IPluginLibrarySpecification appSpec = (IPluginLibrarySpecification) pageSpec.getParent();

        String prev = findPrevious(pageSpec.getIdentifier());
        appSpec.removePageSpecificationPath(pageSpec.getIdentifier());
        pageSpec.setParent(null);
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

  /** 
   *  this holder is there to hold default page definitions. 'Exception' is a good example of a default.
   *  Now, one can 'edit' these in Spindle, but the word "edit' is a misnomer. What is happening
   *  behind the scenes id that the act of 'editing' is really an act of 'replacing' the default
   *  with a new PluginPageSpecification
   */

  class DefaultPageHolder implements IIdentifiable, IPropertySource {

    String identifier;
    String path;
    IPluginLibrarySpecification parent;

    private IPropertyDescriptor[] descriptors = { new ComponentTypeDialogPropertyDescriptor("spec", "Spec", null, null)};

    public DefaultPageHolder(String name, String path, IPluginLibrarySpecification parent) {
      this.identifier = name;
      this.path = path;
      this.parent = parent;
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
      return parent;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setIdentifier(String)
     */
    public void setIdentifier(String id) {
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
     */
    public void setParent(Object parent) {
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
     */
    public Object getEditableValue() {
      return identifier;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
      return descriptors;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
     */
    public Object getPropertyValue(Object key) {
      if ("spec".equals(key)) {

        return path;
      }
      return null;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
     */
    public boolean isPropertySet(Object id) {
      return true;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(Object)
     */
    public void resetPropertyValue(Object id) {
    }

    /**
     * name and spec are immutable as this is a default PageSpecification
     * What happens is we create a new PluginPageSpecification  and set the values in it
     */
    public void setPropertyValue(Object key, Object value) {
      if ("spec".equals(key)) {

        updateSelection = true;
        parent.setPageSpecificationPath(identifier, (String) value);
        updateSelection = false;

      }
    }

  }

  class PageHolder implements IIdentifiable, IPropertySource {

    private String identifier;
    private String path;
    private IPluginLibrarySpecification parent;

    /**
     * Constructor for PluginPageSpecification
     */

    public PageHolder(String name, String path, IPluginLibrarySpecification parent) {
      this.identifier = name;
      this.path = path;
      this.parent = parent;
    }

    /**
     * Returns the parent.
     * @return PluginApplicationSpecification
     */
    public Object getParent() {
      return parent;
    }

    /**
     * Sets the parent.
     * @param parent The parent to set
     */
    public void setParent(Object parent) {
      this.parent = (IPluginLibrarySpecification) parent;
    }

    /**
     * Returns the identifier.
     * @return String
     */
    public String getIdentifier() {
      return identifier;
    }

    /**
     * Sets the identifier.
     * @param identifier The identifier to set
     */
    public void setIdentifier(String identifier) {
      this.identifier = identifier;
    }

    private IPropertyDescriptor[] descriptors =
      { new TextPropertyDescriptor("name", "Name"), new ComponentTypeDialogPropertyDescriptor("spec", "Spec", null, null)};

    public void resetPropertyValue(Object key) {
    }

    public void setPropertyValue(Object key, Object value) {
      if ("name".equals(key)) {

        String oldName = this.identifier;
        String specPath = parent.getPageSpecificationPath(oldName);
        String newName = (String) value;

        if ("".equals(newName.trim())) {

          newName = oldName;

        } else {

          String existingPath = parent.getPageSpecificationPath(newName);

          if (existingPath != null) {

            newName = "Copy of " + newName;
            parent.setPageSpecificationPath(newName, specPath);
            return;
          }
        }
        this.identifier = newName;
        parent.removePageSpecificationPath(oldName);
        parent.setPageSpecificationPath(this.identifier, specPath);

      } else if ("spec".equals(key)) {

        parent.setPageSpecificationPath(identifier, (String) value);
      }
    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {

        return identifier != null;

      } else if ("spec".equals(key)) {

        return parent.getPageSpecificationPath(identifier) != null;

      }
      return true;

    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {

        return identifier;

      } else if ("spec".equals(key)) {

        return parent.getPageSpecificationPath(identifier);
      }
      return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {

      return descriptors;
    }

    public Object getEditableValue() {
      return identifier;
    }

  }

}