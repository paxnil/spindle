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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.util.lookup.INamespaceFragment;
import com.iw.plugins.spindle.util.lookup.INamespaceLookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryNamespaceLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class ChooseFromNamespaceWidget extends TwoListChooserWidget {

  static private final Object[] empty = new Object[0];

  private int acceptFlags = TapestryNamespaceLookup.ACCEPT_COMPONENTS;

  private ScanCollector collector = new ScanCollector();

  private TapestryNamespaceLookup lookup;

  private String resultString;

  private INamespaceFragment resultNamespace;

  public ChooseFromNamespaceWidget(ITapestryProject project, int acceptFlags) {

    super();

    configure(project);

    checkAcceptFlags(acceptFlags);

    setFilterLabel("Search:");
    setInitialFilter("*");

    setUpperListLabel("Chose:");
    NameProvider nameProvider = new NameProvider();
    setUpperListLabelProvider(nameProvider);
    setUpperListContentProvider(nameProvider);

    setLowerListLabel("in namespace:");
    NamespaceProvider namespaceProvider = new NamespaceProvider();
    setLowerListLabelProvider(namespaceProvider);
    setLowerListContentProvider(namespaceProvider);

  }

  /**
   * Method checkAcceptFlags.
   * @param acceptFlags
   */
  private void checkAcceptFlags(int acceptFlags) {

    boolean components = (acceptFlags & TapestryNamespaceLookup.ACCEPT_COMPONENTS) > 0;
    boolean pages = (acceptFlags & TapestryNamespaceLookup.ACCEPT_PAGES) > 0;

    Assert.isTrue(!(components && pages), "error: trying to lookup both pages & components!");
    Assert.isTrue((components || pages), "error: must seach for one of pages or components");

    this.acceptFlags = acceptFlags;

  }

  public ISelection getSelection() {

    IStructuredSelection selection = (IStructuredSelection) super.getSelection();

    if (selection == null || selection.isEmpty()) {

      return selection;

    }

    Object[] selectionData = selection.toArray();

    resultString = (String) selectionData[0];
    Object dataNamespace = selectionData[1];

    if (dataNamespace instanceof INamespaceFragment) {

      resultNamespace = (INamespaceFragment) selectionData[1];

    } else {

      resultNamespace = null;
    }

    if (resultString == null) {

      return new StructuredSelection();

    }

    return new StructuredSelection(getResultPath());
  }

  public void refresh() {
    if (lookup == null) {

      return;

    } else {

      super.refresh();
    }

  }

  public void configure(ITapestryProject project) {
    try {

      lookup = new TapestryNamespaceLookup(project);

    } catch (CoreException jmex) {

      TapestryPlugin.getDefault().logException(jmex);
      lookup = null;
    }

  }

  public void dispose() {
    super.dispose();
    lookup = null;
  }

  public String getResultString() {
    return resultString;
  }

  public INamespaceFragment getResultNamespace() {
    return resultNamespace;
  }

  public String getResultPath() {

    String result = "";

    if (resultNamespace != null) {

      result = resultNamespace.getName();

    }

    if (!"".equals(result)) {
    	
      result += ":" + resultString;
      
    } else {
    	
      result = resultString;
      
    }

    return result;

  }

  class NameProvider extends LabelProvider implements IStructuredContentProvider {

    Image componentImage = TapestryImages.getSharedImage("component16.gif");
    Image pageImage = TapestryImages.getSharedImage("page16.gif");

    public Object[] getElements(Object inputElement) {

      String searchFilter = (String) inputElement;

      if (searchFilter == null || "".equals(searchFilter)) {

        return empty;

      }

      collector.reset();

      if ((acceptFlags & TapestryNamespaceLookup.ACCEPT_COMPONENTS) > 0) {

        lookup.findAllComponents(searchFilter, true, collector);

      } else {

        lookup.findAllPages(searchFilter, true, collector);

      }

      return collector.getNames();

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

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
     */
    public Image getImage(Object element) {
      if ((acceptFlags & TapestryNamespaceLookup.ACCEPT_COMPONENTS) > 0) {

        return componentImage;

      } else {

        return pageImage;

      }
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
     */
    public String getText(Object element) {
      return super.getText(element);
    }

  }

  class NamespaceProvider extends LabelProvider implements IStructuredContentProvider {

    Image namespaceImage = TapestryImages.getSharedImage("property16.gif");

    public Object[] getElements(Object inputElement) {

      String selectedName = (String) inputElement;

      if (selectedName == null) {

        return empty;

      }

      return collector.getNamespacesFor(selectedName);

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

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
     */
    public Image getImage(Object element) {
      return namespaceImage;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
     */
    public String getText(Object element) {

      if (element instanceof String) {

        return (String) element;

      }
      return ((INamespaceFragment) element).getName();
    }

  }

  class ScanCollector implements INamespaceLookupRequestor {

    Map results;
    Map namespaceLookup;

    /**
     * Constructor for ScanCollector
     */
    public ScanCollector() {
      super();
      reset();
    }

    public void reset() {
      results = new HashMap();
      namespaceLookup = new HashMap();
    }

    public Map getResults() {
      return results;
    }

    public Object[] getNames() {
      if (results == null) {
        return empty;
      }
      return new TreeSet(results.keySet()).toArray();
    }

    public Object[] getNamespacesFor(String name) {
      if (results == null) {

        return empty;

      }
      Set namespaces = (Set) results.get(name);

      if (namespaces == null) {

        return empty;
      }
      return namespaces.toArray();
    }

    /**
     * @see ITapestryLookupRequestor#isCancelled()
     */
    public boolean isCancelled() {
      return false;
    }

    /**
     * @see ITapestryLookupRequestor#accept(IStorage, IPackageFragment)
     */
    public boolean accept(String name, INamespaceFragment fragment) {

      Object storeNamespace;
      String namespaceName;

      if (fragment.isDefaultNamespace()) {

        storeNamespace = "(framework namespace)";
        namespaceName = (String) storeNamespace;

      } else if ("".equals(fragment.getName())) {

        storeNamespace = "(default namespace)";
        namespaceName = (String) storeNamespace;

      } else {

        storeNamespace = fragment;
        namespaceName = fragment.getName();
      }

      namespaceLookup.put(name + namespaceName, fragment);

      Set namespaces = (Set) results.get(name);

      if (namespaces == null) {

        namespaces = new HashSet();
        namespaces.add(storeNamespace);
        results.put(name, namespaces);

      } else if (!namespaces.contains(storeNamespace)) {

        namespaces.add(storeNamespace);
      }
      return true;
    }

  }

}
