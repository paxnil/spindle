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
package com.iw.plugins.spindle.util.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jface.util.Assert;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.JarEntryFileFaker;

// does not stay up to date as time goes on!

public class TapestryNamespaceLookup implements ILookupAcceptor {

  protected ITapestryProject project;

  protected NamespaceFragment[] namespaces;

  protected TapestryLibraryModel library;

  protected IWorkspace workspace;

  private boolean initialized = false;

  protected ILookupAcceptor acceptor;

  public TapestryNamespaceLookup(ITapestryProject project) throws CoreException {

    acceptor = new DefaultAcceptor();
    this.project = project;

    library =
      (TapestryLibraryModel) project.getModelManager().getReadOnlyModel(
        project.getProjectStorage());

    configure(library);
  }

  private void configure(TapestryLibraryModel rootLibrary) throws CoreException {
    Assert.isTrue(library.isLoaded());
    namespaces = getNamespaceFragments();
    workspace = TapestryPlugin.getDefault().getWorkspace();
    initialized = true;
  }

  private NamespaceFragment[] getNamespaceFragments() throws CoreException {

    ILibrarySpecification specification = library.getSpecification();
    SpindleStatus status = new SpindleStatus();

    ArrayList result = new ArrayList();

    result.add(DefaultLibraryNamespaceFragment.getInstance());
    result.add(new NamespaceFragment("", specification));

    for (Iterator iter = specification.getLibraryIds().iterator(); iter.hasNext();) {
      String libId = (String) iter.next();

      String path = specification.getLibrarySpecificationPath(libId);

      IStorage libStorage = findTapestryLibrary(path);

      TapestryProjectModelManager mgr = project.getModelManager();

      TapestryLibraryModel lib = (TapestryLibraryModel) mgr.getReadOnlyModel(libStorage);

      result.add(new NamespaceFragment(libId, lib.getSpecification()));

    }

    return (NamespaceFragment[]) result.toArray(new NamespaceFragment[result.size()]);

  }

  private IStorage findTapestryLibrary(String path) throws CoreException {

    SpindleStatus status = new SpindleStatus();

    TapestryLookup lookup = project.getLookup();

    IStorage[] results = lookup.findLibrary(path);

    if (results == null || results.length == 0) {

      status.setError("could not resolve: " + path);
      throw new CoreException(status);

    }

    if (results.length > 1) {

      status.setError("could not uniquely resolve: " + path);
      throw new CoreException(status);
    }

    return results[0];

  }

  public void findAllComponents(
    String prefix,
    boolean partialMatch,
    INamespaceLookupRequestor requestor) {

    findAll(prefix, partialMatch, ACCEPT_COMPONENTS, requestor);

  }

  public void findAllPages(
    String prefix,
    boolean partialMatch,
    INamespaceLookupRequestor requestor) {

    findAll(prefix, partialMatch, ACCEPT_PAGES, requestor);

  }

  private void findAll(
    String prefix,
    boolean partialMatch,
    int acceptFlags,
    INamespaceLookupRequestor requestor) {

    if (!initialized) {
      throw new Error("not initialized");
    }
    int count = namespaces.length;
    for (int i = 0; i < count; i++) {
      if (requestor.isCancelled())
        return;
      INamespaceFragment fragment = namespaces[i];

      if (seek(prefix, fragment, partialMatch, acceptFlags, requestor)) {
        return;
      }
    }
  }

  private boolean seek(
    String name,
    INamespaceFragment fragment,
    boolean partialMatch,
    int acceptFlags,
    INamespaceLookupRequestor requestor) {

    if (!initialized) {
      throw new Error("not initialized");
    }
    boolean stopLooking = false;
    String matchName = partialMatch ? name.toLowerCase() : name;
    if (fragment == null) {
      return stopLooking;
    }

    if ((ACCEPT_COMPONENTS & acceptFlags) > 0) {

      stopLooking =
        doSeek(
          fragment.getSpecification().getComponentAliases(),
          matchName,
          fragment,
          partialMatch,
          requestor);

    } else if ((ACCEPT_PAGES & acceptFlags) > 0) {

      stopLooking =
        doSeek(fragment.getSpecification().getPageNames(), name, fragment, partialMatch, requestor);
    }

    return stopLooking;

  }

  protected boolean doSeek(
    List potentialNames,
    String name,
    INamespaceFragment fragment,
    boolean partialMatch,
    INamespaceLookupRequestor requestor) {
    	
    boolean stopSearch = false;

    for (Iterator iter = potentialNames.iterator(); iter.hasNext();) {
    	
      String potentialName = (String) iter.next();
      
      if (nameMatches(name, potentialName, partialMatch)) {
          requestor.accept(potentialName, fragment); 
        }

    }
    
    return stopSearch;


  }


  protected boolean nameMatches(String candidate, String existing, boolean partialMatch) {
    if ("*".equals(candidate)) {
      return true;
    }
    String LHS = candidate;
    String RHS = existing.toLowerCase();
    int dot = existing.lastIndexOf(".");
    if (dot > 0) {
      RHS = RHS.substring(0, dot);
    }
    return partialMatch ? RHS.startsWith(LHS) : LHS.equals(RHS);
  }

  public boolean acceptAsTapestry(IJavaProject jproject, IStorage s, int acceptFlags) {
    return true;
  }

  public static class BasicNamespaceRequest implements INamespaceLookupRequestor {

    ArrayList result;

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
      if (result == null) {
        result = new ArrayList();
      }
      result.add(fragment.getName()+ name);
      return true;
    }

    public String [] getResults() {
      if (result == null) {
        return new String[0];
      } else {
        return (String[]) result.toArray(new String[0]);
      }
    }
  }



}