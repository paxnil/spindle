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

package com.iw.plugins.spindle.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.util.lookup.ILookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class ChooseWorkspaceModelWidget extends TwoListChooserWidget {

  static private final Object[] empty = new Object[0];

  private int acceptFlags;

  private ScanCollector collector = new ScanCollector();

  private TapestryLookup lookup;

  private String resultString;

  private IPackageFragment resultPackage;

  private Set libraryFilter;

  public ChooseWorkspaceModelWidget(IJavaProject project, int acceptFlags) {

    this(project, acceptFlags, false);

  }

  public ChooseWorkspaceModelWidget(
    IJavaProject project,
    int acceptFlags,
    boolean filterLibraries) {

    super();

    configure(project, filterLibraries);
    this.acceptFlags = acceptFlags;

    setFilterLabel("Search:");
    setInitialFilter("*");

    setUpperListLabel("Chose:");
    setUpperListLabelProvider(new TapestryStorageLabelProvider());
    setUpperListContentProvider(new StorageContentProvider());

    setLowerListLabel("in package:");
    setLowerListLabelProvider(
      new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_SMALL_ICONS));
    setLowerListContentProvider(new PackageContentProvider());

  }

  public ISelection getSelection() {

    IStructuredSelection selection = (IStructuredSelection) super.getSelection();

    if (selection == null || selection.isEmpty()) {

      return selection;

    }

    Object[] selectionData = selection.toArray();

    IStorage selectedStorage = (IStorage) selectionData[0];
    IPackageFragment selectedPackage = (IPackageFragment) selectionData[1];

    resultString = null;
    resultPackage = null;

    if (selectedStorage != null) {

      resultString = selectedStorage.getName();
    }

    if (selectedPackage != null) {

      resultPackage = selectedPackage;

    }

    if (resultString == null) {

      return new StructuredSelection();

    }

    return new StructuredSelection(resultString);
  }

  public void refresh() {
    if (lookup == null) {

      return;

    } else {

      super.refresh();
    }

  }

  public void configure(IJavaProject project, boolean filterLibraries) {

    try {

      ITapestryProject tproject = TapestryPlugin.getDefault().getTapestryProjectFor(project);

      if (tproject != null) {

        lookup = tproject.getLookup();

        if (filterLibraries) {

          configureFilter(tproject);

        }

      }

    } catch (CoreException jmex) {
      
      lookup = new TapestryLookup();

      try {

        lookup.configure(project);

      } catch (JavaModelException e) {
        lookup = null;
      }
    }

  }

  private void configureFilter(ITapestryProject tproject) throws CoreException {
    libraryFilter = new HashSet();

    TapestryLibraryModel projectModel = (TapestryLibraryModel) tproject.getProjectModel();

    TapestryProjectModelManager modelManager = tproject.getModelManager();

    IPluginLibrarySpecification libSpec = projectModel.getSpecification();

    for (Iterator ids = libSpec.getLibraryIds().iterator(); ids.hasNext();) {

      String libraryPath = libSpec.getLibrarySpecificationPath((String) ids.next());

      IStorage[] found = lookup.findLibrary(libraryPath);

      for (int i = 0; i < found.length; i++) {

        TapestryLibraryModel foundModel =
          (TapestryLibraryModel) modelManager.getReadOnlyModel(found[i]);

        if (foundModel != null && foundModel.isLoaded()) {

          IPluginLibrarySpecification foundSpec = foundModel.getSpecification();

          for (Iterator aliases = foundSpec.getComponentAliases().iterator(); aliases.hasNext();) {

            libraryFilter.add(foundSpec.getComponentSpecificationPath((String) aliases.next()));

          }

          for (Iterator names = foundSpec.getPageNames().iterator(); names.hasNext();) {

            libraryFilter.add(foundSpec.getPageSpecificationPath((String) names.next()));

          }

        }
      }

    }
  }

  public void dispose() {
    super.dispose();
    lookup = null;
  }

  public String getResultString() {
    return resultString;
  }

  public IPackageFragment getResultPackage() {
    return resultPackage;
  }

  public ITapestryModel getResultModel() {

    return collector.getModel(resultString, resultPackage);
  }

  public IStorage getResultStorage() {

    return collector.getStorage(resultString, resultPackage);

  }

  public String getResultPath() {

    String name = getResultStorage().getName();
    String path = "/";
    if ("".equals(resultPackage.getElementName())) {

      return path + name;

    }

    path += resultPackage.getElementName().replace('.', '/');
    path += "/" + name;

    return path;

  }

  class StorageContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object inputElement) {

      String searchFilter = (String) inputElement;

      if (searchFilter == null || "".equals(searchFilter)) {

        return empty;

      }

      collector.reset();

      lookup.findAll(searchFilter, true, acceptFlags, collector);

      return collector.getStorages().toArray();

    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

  }

  class PackageContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object inputElement) {

      IStorage selectedStorage = (IStorage) inputElement;

      if (selectedStorage == null) {

        return empty;

      }

      return collector.getPackagesFor(selectedStorage.getName());

    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

  }

  class ScanCollector implements ILookupRequestor {

    Map results;
    List storages;
    Map storageLookup;

    /**
     * Constructor for ScanCollector
     */
    public ScanCollector() {
      super();
      reset();
    }

    public void reset() {
      results = new HashMap();
      storageLookup = new HashMap();
      storages = new ArrayList();
    }

    public Map getResults() {
      return results;
    }

    public List getStorages() {

      return storages;

    }

    public IStorage getStorage(String name, IPackageFragment pack) {

      String packname = "(default package)";
      if (pack != null) {

        packname = pack.getElementName();
      }
      return (IStorage) storageLookup.get(name + packname);

    }

    public ITapestryModel getModel(String name, IPackageFragment pack) {

      try {

        IStorage storage = getStorage(name, pack);

        return (ITapestryModel) TapestryPlugin.getTapestryModelManager(storage).getReadOnlyModel(
          storage);

      } catch (CoreException e) {

        return null;
      }
    }

    public Object[] getApplicationNames() {
      if (results == null) {
        return empty;
      }
      return new TreeSet(results.keySet()).toArray();
    }

    public Object[] getPackagesFor(String name) {
      if (results == null) {

        return empty;

      }
      Set packages = (Set) results.get(name);

      if (packages == null) {

        return empty;
      }
      return packages.toArray();
    }


    public boolean isCancelled() {
      return false;
    }

    public boolean accept(IStorage storage, IPackageFragment fragment) {

      String name = storage.getName();
      Object storePackageFragment;
      String packageElementName;

      if (libraryFilter != null && isInLibrary(storage, fragment)) {

        return false;

      }

      storages.add(storage);

      if (fragment == null) {

        storePackageFragment = "(default package)";
        packageElementName = (String) storePackageFragment;

      } else {

        storePackageFragment = fragment;
        packageElementName = fragment.getElementName();
      }

      storageLookup.put(name + packageElementName, storage);

      Set packages = (Set) results.get(name);

      if (packages == null) {

        packages = new HashSet();
        packages.add(storePackageFragment);
        results.put(name, packages);

      } else if (!packages.contains(storePackageFragment)) {

        packages.add(storePackageFragment);
      }
      return true;
    }

    private boolean isInLibrary(IStorage storage, IPackageFragment fragment) {

      String tapestryPath;
      String fragmentName = fragment.getElementName();
      String name = storage.getName();

      if ("".equals(fragmentName)) {

        tapestryPath = "/" + name;

      } else {

        tapestryPath = "/" + fragmentName.replace('.', '/') + "/" + name;

      }

      return libraryFilter.contains(tapestryPath);

    }

  }

}
