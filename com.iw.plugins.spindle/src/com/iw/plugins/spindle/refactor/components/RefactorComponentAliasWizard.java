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
package com.iw.plugins.spindle.refactor.components;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class RefactorComponentAliasWizard extends Wizard implements ISelectionChangedListener {

  ITapestryProject project;
  String oldName;
  String newName;
  List existingComponentAliases;
  RefactorComponentAliasPage firstPage;
  RefactorAliasScopePage secondPage;
  Map affectedComponents = new HashMap();
  Map editors = new HashMap();
  private boolean showSecondPage = true;
  private boolean canShow = true;
  private boolean canFinish = false;

  static public RefactorComponentAliasWizard createWizard(
    ITapestryProject project,
    String oldName,
    List existingComponentAliases) {

    RefactorComponentAliasWizard result = new RefactorComponentAliasWizard(project, oldName, existingComponentAliases);

    if (result.canShow) {

      return result;

    }

    return null;

  }

  /**
   * Constructor for RefactorComponentNameWizard.
   */
  private RefactorComponentAliasWizard(ITapestryProject project, String oldName, List existingComponentAliases) {
    super();
    this.project = project;
    this.oldName = oldName;
    this.existingComponentAliases = existingComponentAliases;
    setWindowTitle("Tapestry Refactoring Wizard");
    setup();
    if (affectedComponents.isEmpty()) {
      showSecondPage = false;
    }
    setNeedsProgressMonitor(true);
  }

  /**
   * Method setup.
   */
  private void setup() {

    try {
      TapestryPlugin.getDefault().getActiveWorkbenchWindow().run(false, false, new IRunnableWithProgress() {

        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

          try {
            findAffectedComponents();

            canShow = doAnyRequiredSaves();

          } catch (CoreException e) {
          }
        }
      });
    } catch (InvocationTargetException e) {
    } catch (InterruptedException e) {
    }

  }

  public String getNewName() {

    return newName;

  }

  public void newNameChanged(String value) {

    canFinish = !oldName.equals(value);
    getContainer().updateButtons();

  }

  /**
   * Method doAnyRequiredSaves.
   * @return boolean
   */
  private boolean doAnyRequiredSaves() {
    if (affectedComponents.isEmpty()) {

      return true;
    }

    List dirtyEditors = new ArrayList();

    for (Iterator iter = editors.entrySet().iterator(); iter.hasNext();) {
      Map.Entry element = (Map.Entry) iter.next();
      IEditorPart editor = (IEditorPart) element.getValue();

      if (editor.isDirty()) {
        dirtyEditors.add(element.getValue());
      }
    }

    if (dirtyEditors.isEmpty()) {

      return true;

    }

    RequiredSaveEditorAction action = new RequiredSaveEditorAction(dirtyEditors);

    return action.save();

  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish() {

    newName = firstPage.getNewName();

    if (affectedComponents.isEmpty()) {

      return true;

    }

    IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(getFinishRunnable());
    try {
      getContainer().run(false, true, op);
    } catch (InvocationTargetException e) {
      TapestryPlugin.getDefault().logException(e);
      return false;
    } catch (InterruptedException e) {
      return false;
    }
    return true;
  }

  /**
   * Method getFinishRunnable.
   */
  private IRunnableWithProgress getFinishRunnable() {

    return new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        TapestryProjectModelManager manager;
        try {

          manager = project.getModelManager();

        } catch (CoreException e) {

          ErrorDialog.openError(
            getContainer().getShell(),
            "Spindle Error",
            "refactoring failed. could not obtain a model manager from the project",
            e.getStatus());
          return;
        }

        monitor.beginTask("refactoring...", affectedComponents.size());

        List affected = getFinalAffectedComponents();

        for (Iterator iter = affected.iterator(); iter.hasNext();) {

          AffectedComponentHolder holder = (AffectedComponentHolder) iter.next();
          IStorage storage = holder.model.getUnderlyingStorage();

          IEditorPart editor = Utils.getEditorFor(storage);
          boolean pulledFromWorkspace = false;

          TapestryComponentModel model;

          try {
            if (editor == null) {

              model = (TapestryComponentModel) manager.getEditableModel(storage, this);
              pulledFromWorkspace = true;

            } else {

              model = holder.model;

            }

            if (model.isLoaded()) {

              PluginComponentSpecification componentSpec = (PluginComponentSpecification) model.getComponentSpecification();

              String[] affectedIds = holder.getIds();

              for (int i = 0; i < affectedIds.length; i++) {

                PluginContainedComponent contained = (PluginContainedComponent) componentSpec.getComponent(affectedIds[i]);
                if (contained != null && oldName.equals(contained.getType())) {

                  contained.setType(newName);
                  // we do this so that the editor's dity flag is in sync with the models dirty flag
                  componentSpec.setComponent(affectedIds[i], contained);

                }

              }

            }

            monitor.worked(1);

            if (pulledFromWorkspace) {

              Utils.saveModel(model, monitor);

            } else {

              editor.doSave(monitor);
              if (editor instanceof SpindleMultipageEditor) {

                ((SpindleMultipageEditor) editor).showPage(SpindleMultipageEditor.SOURCE_PAGE);

              }

            }

          } finally {

            if (pulledFromWorkspace) {

              manager.disconnect(storage, this);
            }
          }

        }
      }
    };
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  public void addPages() {
    super.addPages();
        
    firstPage = new RefactorComponentAliasPage("FirstPage", oldName, showSecondPage, existingComponentAliases);
    addPage(firstPage);
    if (showSecondPage) {

      List affected = new ArrayList();

      for (Iterator iter = affectedComponents.keySet().iterator(); iter.hasNext();) {
        Object key = (Object) iter.next();
        affected.add(affectedComponents.get(key));
      }

      secondPage = new RefactorAliasScopePage("SecondPage", affected);
      secondPage.addSelectionChangedListener(this);
      addPage(secondPage);

    }
  }

  private List getFinalAffectedComponents() {

    if (secondPage != null) {

      return ((IStructuredSelection) secondPage.getSelection()).toList();

    } else {

      List affected = new ArrayList();

      for (Iterator iter = affectedComponents.keySet().iterator(); iter.hasNext();) {
        Object key = (Object) iter.next();
        affected.add(affectedComponents.get(key));
      }

      return affected;

    }

  }

  private void findAffectedComponents() throws CoreException {

    affectedComponents.clear();
    editors.clear();

    TapestryLibraryModel libModel = (TapestryLibraryModel) project.getProjectModel();
    IEditorPart editor = Utils.getEditorFor(libModel.getUnderlyingStorage());
    if (editor != null && editor instanceof SpindleMultipageEditor) {

      libModel = (TapestryLibraryModel) ((SpindleMultipageEditor) editor).getModel();

    }

    if (!libModel.isLoaded()) {

      return;

    }
    TapestryProjectModelManager manager = project.getModelManager();
    IPluginLibrarySpecification libSpec = (IPluginLibrarySpecification) libModel.getSpecification();

    if (libSpec == null) {
      return;
    }

    TapestryLookup lookup = project.getLookup();

    for (Iterator iter = libSpec.getComponentAliases().iterator(); iter.hasNext();) {
      String element = (String) iter.next();
      String tapestryPath = libSpec.getComponentSpecificationPath(element);

      IPackageFragment fragment = lookup.findPackageFragment(tapestryPath);

      doModelCheck(manager, lookup, tapestryPath, fragment);

    }

    for (Iterator iter = libSpec.getPageNames().iterator(); iter.hasNext();) {
      String element = (String) iter.next();
      String tapestryPath = libSpec.getPageSpecificationPath(element);

      IPackageFragment fragment = lookup.findPackageFragment(tapestryPath);

      doModelCheck(manager, lookup, tapestryPath, fragment);

    }
  }

  private void doModelCheck(
    TapestryProjectModelManager manager,
    TapestryLookup lookup,
    String tapestryPath,
    IPackageFragment fragment) {
    TapestryComponentModel model = (TapestryComponentModel) getModelFor(manager, lookup, tapestryPath);
    if (model != null && model.isLoaded()) {

      PluginComponentSpecification componentSpec = (PluginComponentSpecification) model.getComponentSpecification();
      if (componentSpec != null) {

        for (Iterator iterator = componentSpec.getComponentIds().iterator(); iterator.hasNext();) {

          String id = (String) iterator.next();
          PluginContainedComponent contained = (PluginContainedComponent) componentSpec.getComponent(id);

          if (oldName.equals(contained.getType())) {

            addAffectedComponent(model, id, fragment);
          }

        }

      }
    }

    if (model != null) {

      IStorage storage = model.getUnderlyingStorage();

      if (!affectedComponents.containsKey(storage)) {

        editors.remove(storage);

      } else {

        IEditorPart editor = (IEditorPart) editors.get(storage);

        if (editor != null && !editor.isDirty()) {

          editors.remove(storage);

        }

      }
    }

  }

  private ITapestryModel getModelFor(TapestryProjectModelManager manager, TapestryLookup lookup, String tapestryPath) {

    IStorage[] found =
      lookup.findByTapestryPath(
        tapestryPath,
        lookup.ACCEPT_PAGES | lookup.ACCEPT_COMPONENTS | lookup.WRITEABLE | lookup.THIS_PROJECT_ONLY);

    if (found == null || found.length == 0) {

      return null;

    }

    ITapestryModel model = manager.getReadOnlyModel(found[0]);
    IEditorPart editor = Utils.getEditorFor(found[0]);

    if (editor != null && editor instanceof SpindleMultipageEditor) {

      model = (ITapestryModel) ((SpindleMultipageEditor) editor).getModel();

      editors.put(found[0], editor);

    }

    return model;

  }

  /**
   * Method addAffectedComponent.
   * @param componentModel
   * @param id
   */
  private void addAffectedComponent(TapestryComponentModel componentModel, String id, IPackageFragment fragment) {

    AffectedComponentHolder existing = (AffectedComponentHolder) affectedComponents.get(componentModel.getUnderlyingStorage());

    if (existing == null) {

      AffectedComponentHolder newHolder = new AffectedComponentHolder(componentModel, fragment);
      newHolder.addId(id);
      affectedComponents.put(componentModel.getUnderlyingStorage(), newHolder);

    } else {

      existing.addId(id);
    }

  }

  class AffectedComponentHolder {

    public TapestryComponentModel model;
    private Set ids = new HashSet();
    public IPackageFragment fragment;

    public AffectedComponentHolder(TapestryComponentModel model, IPackageFragment fragment) {

      this.model = model;
      this.fragment = fragment;

    }

    public void addId(String id) {
      ids.add(id);
    }

    public String[] getIds() {

      return (String[]) ids.toArray(new String[ids.size()]);
    }

    public boolean equals(Object obj) {
      if (obj == null || this.getClass() != obj.getClass()) {

        return false;

      }

      AffectedComponentHolder other = (AffectedComponentHolder) obj;
      return this.model.equals(other.model);
    }

  }

  public void selectionChanged(SelectionChangedEvent event) {

    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    canFinish = !selection.isEmpty() && !oldName.equals(firstPage.getNewName());
    
    getContainer().updateButtons();

  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#canFinish()
   */
  public boolean canFinish() {
   return canFinish;
  }

}
