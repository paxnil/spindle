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
package com.iw.plugins.spindle.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;

// does not stay up to date as time goes on!

public class TapestryLookup {

  /**
  * Accept flag for specifying components.
  */
  public static final int ACCEPT_COMPONENTS = 0x00000001;
  /**
   * Accept flag for specifying application.
   */
  public static final int ACCEPT_APPLICATIONS = 0x00000002;
  /**
   *  Accept flag for specifying the search name includes Tapestry path!
   */
  public static final int FULL_TAPESTRY_PATH = 0x00000004;
  /**
   *  Accept flag for specifying HTML files
   */
  public static final int ACCEPT_HTML = 0x00000008;
  /**
   * Accept flag for writeable (non read only) files;
   */
  public static final int WRITEABLE = 0x00000010;

  protected IPackageFragmentRoot[] fPackageFragmentRoots = null;

  protected HashMap fPackageFragments;

  protected IJavaProject project;

  protected IWorkspace workspace;

  private boolean initialized = false;

  public TapestryLookup() {

  }

  public void configure(IJavaProject project) throws JavaModelException {
    this.project = project;
    workspace = project.getJavaModel().getWorkspace();
    fPackageFragmentRoots = project.getPackageFragmentRoots();
    fPackageFragments = new HashMap();
    IPackageFragment[] frags = getPackageFragmentsInRoots(fPackageFragmentRoots, project);
    for (int i = 0; i < frags.length; i++) {
      IPackageFragment fragment = frags[i];
      IPackageFragment[] entry = (IPackageFragment[]) fPackageFragments.get(fragment.getElementName());
      if (entry == null) {
        entry = new IPackageFragment[1];
        entry[0] = fragment;
        fPackageFragments.put(fragment.getElementName(), entry);
      } else {
        IPackageFragment[] copy = new IPackageFragment[entry.length + 1];
        System.arraycopy(entry, 0, copy, 0, entry.length);
        copy[entry.length] = fragment;
        fPackageFragments.put(fragment.getElementName(), copy);
      }
    }
    initialized = true;
  }

  private IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots, IJavaProject project) {

    ArrayList frags = new ArrayList();
    for (int i = 0; i < roots.length; i++) {
      IPackageFragmentRoot root = roots[i];
      try {
        IJavaElement[] children = root.getChildren();

        int length = children.length;
        if (length == 0)
          continue;
        if (children[0].getParent().getParent().equals(project)) {
          for (int j = 0; j < length; j++) {
            frags.add(children[j]);
          }
        } else {
          for (int j = 0; j < length; j++) {
            frags.add(root.getPackageFragment(children[j].getElementName()));
          }
        }
      } catch (JavaModelException e) {
        // do nothing
      }
    }
    IPackageFragment[] fragments = new IPackageFragment[frags.size()];
    frags.toArray(fragments);
    return fragments;
  }

  public IStorage[] findComponent(String tapestryPath) {
    if (!initialized) {
      throw new Error("not initialized");
    }
    StorageOnlyRequest request = new StorageOnlyRequest();
    findAll(tapestryPath, false, ACCEPT_COMPONENTS | FULL_TAPESTRY_PATH, request);
    return request.getResults();
  }

  public IStorage[] findApplication(String tapestryPath) {
    if (!initialized) {
      throw new Error("not initialized");
    }
    StorageOnlyRequest request = new StorageOnlyRequest();
    findAll(tapestryPath, false, ACCEPT_APPLICATIONS | FULL_TAPESTRY_PATH, request);
    return request.getResults();
  }
  
  public IStorage[] findHtmlFor(String tapestryPath) {
    if (!tapestryPath.endsWith(".jwc")) {
      return new IStorage[0];
    }
    String usePath = tapestryPath.substring(0, tapestryPath.lastIndexOf("."));
    usePath += ".html";
    StorageOnlyRequest request = new StorageOnlyRequest();
    findAll(usePath, false, ACCEPT_HTML | FULL_TAPESTRY_PATH, request);
    return request.getResults();
  }

  public IResource findParentResource(IStorage storage) throws JavaModelException {
    if (storage instanceof IResource) {
      return (IResource) ((IResource) storage).getParent();
    }
    IPackageFragment fragment = findPackageFragment(storage);
    if (fragment == null) {
      return null;
    }
    return (IResource) fragment.getParent().getUnderlyingResource();
  }

  public IPackageFragment findPackageFragment(IStorage storage) throws JavaModelException {
    if (storage instanceof JarEntryFileFaker) {
      storage = (IStorage) storage.getAdapter(IStorage.class);
    }
    if (storage instanceof IResource) {
      return project.findPackageFragment(((IResource) storage).getParent().getFullPath());
    }
    for (int i = 0; i < fPackageFragmentRoots.length; i++) {
      boolean isBinary = fPackageFragmentRoots[i].getKind() == IPackageFragmentRoot.K_BINARY;
      Object[] children = null;
      try {
        children = fPackageFragmentRoots[i].getChildren();
        if (children == null) {
          continue;
        }
        for (int j = 0; j < children.length; j++) {
          IPackageFragment fragment = (IPackageFragment) children[j];
          Object[] nonJavaResources = fragment.getNonJavaResources();
          for (int k = 0; k < nonJavaResources.length; k++) {
            if (nonJavaResources[k].equals(storage)) {
              return fragment;
            }
          }
        }
      } catch (JavaModelException ex) {
        //do nothing
      }
    }
    return null;

  }

  public boolean projectContainsJarEntry(JarEntryFile jarFile) {
    if (!initialized) {
      throw new Error("not initialized");
    }
    for (int i = 0; i < fPackageFragmentRoots.length; i++) {
      IPackageFragmentRoot root = fPackageFragmentRoots[i];
      IJavaElement[] packages = null;
      try {
        packages = root.getChildren();
      } catch (JavaModelException npe) {
        continue; // the root is not present, continue;
      }
      if (packages != null) {
        Object[] jarFiles = null;
        for (int j = 0, packageCount = packages.length; j < packageCount; j++) {
          IPackageFragment pkg = (IPackageFragment) packages[j];
          try {
            if (pkg.getKind() == IPackageFragmentRoot.K_SOURCE) {
              continue;
            }
            jarFiles = pkg.getNonJavaResources();
          } catch (JavaModelException npe) {
            continue;
          }
          for (int k = 0; k < jarFiles.length; k++) {
            JarEntryFile other = (JarEntryFile) jarFiles[k];
            if (jarFile.equals(other)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public void findAll(String prefix, boolean partialMatch, int acceptFlags, ITapestryLookupRequestor requestor) {

    if (!initialized) {
      throw new Error("not initialized");
    }
    int count = fPackageFragmentRoots.length;
    for (int i = 0; i < count; i++) {
      if (requestor.isCancelled())
        return;
      IPackageFragmentRoot root = fPackageFragmentRoots[i];
      IJavaElement[] packages = null;
      try {
        packages = root.getChildren();
      } catch (JavaModelException npe) {
        continue; // the root is not present, continue;
      }
      if (packages != null) {
        for (int j = 0, packageCount = packages.length; j < packageCount; j++) {
          if (requestor.isCancelled())
            return;
          if (seek(prefix, (IPackageFragment) packages[j], partialMatch, acceptFlags, requestor)) {
            return;
          }
        }
      }
    }

  }

  public boolean seek(
    String name,
    IPackageFragment pkg,
    boolean partialMatch,
    int acceptFlags,
    ITapestryLookupRequestor requestor) {

    if (!initialized) {
      throw new Error("not initialized");
    }
    boolean stopLooking = false;
    String matchName = partialMatch ? name.toLowerCase() : name;
    if (pkg == null) {
      findAll(matchName, partialMatch, acceptFlags, requestor);
      return stopLooking;
    }
    IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();

    try {
      int packageFlavor = root.getKind();

      switch (packageFlavor) {
        case IPackageFragmentRoot.K_BINARY :
          if ((acceptFlags & WRITEABLE) != 0) {
            break;
          }
          stopLooking = seekInBinaryPackage(matchName, pkg, partialMatch, acceptFlags, requestor);
          break;
        case IPackageFragmentRoot.K_SOURCE :
          stopLooking = seekInSourcePackage(matchName, pkg, partialMatch, acceptFlags, requestor);
          break;
        default :
          return stopLooking;
      }
    } catch (JavaModelException e) {
      return stopLooking;
    }
    return stopLooking;
  }

  protected boolean seekInBinaryPackage(
    String name,
    IPackageFragment pkg,
    boolean partialMatch,
    int acceptFlags,
    ITapestryLookupRequestor requestor) {

    Object[] jarFiles = null;
    try {
      jarFiles = pkg.getNonJavaResources();
    } catch (JavaModelException npe) {
      return false; // the package is not present
    }
    int length = jarFiles.length;
    for (int i = 0; i < length; i++) {
      if (requestor.isCancelled()) {
        return true;
      }
      
      JarEntryFile jarFile = null;
      try {
      	jarFile = (JarEntryFile) jarFiles[i];
      } catch (ClassCastException ccex) {
      	//skip it
      	continue;
      }
      if (acceptAsTapestry(jarFile, acceptFlags)) {
        if ((acceptFlags & FULL_TAPESTRY_PATH) != 0) {
          if (nameMatchesFull(name, pkg, jarFile.getFullPath())) {
            requestor.accept((IStorage) jarFile, pkg);
            return true;
          }
          continue;
        } else if (nameMatches(name, jarFile.getName(), partialMatch)) {
          requestor.accept((IStorage) jarFile, pkg);
        }
      }
    }
    return false;
  }

  protected boolean seekInSourcePackage(
    String name,
    IPackageFragment pkg,
    boolean partialMatch,
    int acceptFlags,
    ITapestryLookupRequestor requestor) {

    Object[] files = null;
    try {
      files = pkg.getNonJavaResources();
    } catch (JavaModelException npe) {
      return false; // the package is not present
    }
    int length = files.length;

    for (int i = 0; i < length; i++) {
      if (requestor.isCancelled()) {
        return true;
      }
      IFile file = null;
      try {
        file = (IFile) files[i];
      } catch (ClassCastException ccex) {
        // skip it
        continue;
      }
      if (acceptAsTapestry(file, acceptFlags)) {
        if ((acceptFlags & FULL_TAPESTRY_PATH) != 0) {
          if (nameMatchesFull(name, pkg, file.getFullPath())) {
            requestor.accept((IStorage) file, pkg);
            return true;
          }
          continue;
        } else if (nameMatches(name, file.getName(), partialMatch)) {
          requestor.accept((IStorage) file, pkg);
        }
      }
    }
    return false;
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

  protected boolean nameMatchesFull(String candidate, IPackageFragment pkg, IPath path) {
    String LHS = candidate;
    String existingName = "/" + pkg.getElementName() + "/";
    existingName = existingName.replace('.', '/');
    String RHS = existingName + path.lastSegment();
    return LHS.equals(RHS);
  }

  protected boolean acceptAsTapestry(IStorage s, int acceptFlags) {
    String extension = s.getFullPath().getFileExtension();
    int w = acceptFlags & WRITEABLE;
    int j = acceptFlags & ACCEPT_COMPONENTS;
    if ((acceptFlags & WRITEABLE) != 0 && s.isReadOnly()) {
      return false;
    }
    if ("jwc".equals(extension)) {
      return (acceptFlags & ACCEPT_COMPONENTS) != 0;
    }
    if ("application".equals(extension)) {
      return (acceptFlags & ACCEPT_APPLICATIONS) != 0;
    }
    if ("html".equals(extension)) {
      return (acceptFlags & ACCEPT_HTML) != 0;
    }
    return false;
  }

  protected class StorageOnlyRequest implements ITapestryLookupRequestor {

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
    public boolean accept(IStorage storage, IPackageFragment frgament) {
      if (result == null) {
        result = new ArrayList();
      }
      result.add(storage);
      return true;
    }

    public IStorage[] getResults() {
      if (result == null) {
        return new IStorage[0];
      } else {
        return (IStorage[]) result.toArray(new IStorage[0]);
      }
    }
  }
  
  
}