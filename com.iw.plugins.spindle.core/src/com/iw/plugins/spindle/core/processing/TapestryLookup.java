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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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

import com.iw.plugins.spindle.core.TapestryBuilder;
import com.iw.plugins.spindle.core.TapestryModelException;
import com.iw.plugins.spindle.core.TapestryProject;

// does not stay up to date as time goes on!

public class TapestryLookup {

  public int ACCEPT_NONE = 0x0100000;

  public int ACCEPT_LIBRARIES = 0x0000001;

  /**
  * Accept flag for specifying components.
  */
  public int ACCEPT_COMPONENTS = 0x00000002;
  /**
   * Accept flag for specifying application.
   */
  public int ACCEPT_APPLICATIONS = 0x00000004;
  /**
   *  Accept flag for specifying HTML files
   */
  public int ACCEPT_HTML = 0x00000008;

  public int ACCEPT_PAGES = 0x00000010;

  public int ACCEPT_SCRIPT = 0x00000020;

  public int ACCEPT_ANY = 0x00000100;

  protected IPackageFragmentRoot[] packageFragmentRoots = null;
  protected IFolder applicationRoot = null;
  protected IFolder servletContextRoot = null;
  protected IFolder[] applicationContextRoots = null;

  protected HashMap packageFragments;

  protected IJavaProject jproject;
  protected TapestryProject tproject;

  private boolean initialized = false;

  private List seekExtensions = Arrays.asList(TapestryBuilder.KnownExtensions);

  public TapestryLookup() {

  }

  public void configure(TapestryProject project, String[] knownServletNames)
    throws CoreException {
    this.tproject = project;
    this.jproject = project.getJavaProject();
    configureClasspath();
    configureTapestry(knownServletNames);
    initialized = true;
  }

  /* pull the classpath info we need from the JavaModel */
  protected void configureClasspath() throws TapestryModelException {
    try {
      packageFragmentRoots = jproject.getAllPackageFragmentRoots();
      packageFragments = new HashMap();
      IPackageFragment[] frags = getPackageFragmentsInRoots(packageFragmentRoots, jproject);
      for (int i = 0; i < frags.length; i++) {
        IPackageFragment fragment = frags[i];
        IPackageFragment[] entry =
          (IPackageFragment[]) packageFragments.get(fragment.getElementName());
        if (entry == null) {
          entry = new IPackageFragment[1];
          entry[0] = fragment;
          packageFragments.put(fragment.getElementName(), entry);
        } else {
          IPackageFragment[] copy = new IPackageFragment[entry.length + 1];
          System.arraycopy(entry, 0, copy, 0, entry.length);
          copy[entry.length] = fragment;
          packageFragments.put(fragment.getElementName(), copy);
        }
      }
    } catch (JavaModelException e) {
      throw new TapestryModelException(null);
    }
  }

  protected void configureTapestry(String[] knownServletNames) throws TapestryModelException {
    applicationRoot = tproject.getAppRootFolder();
    if (applicationRoot != null && applicationRoot.exists()) {
      IPath rootPath = applicationRoot.getFullPath();
      if (tproject.isOnOutputPath(rootPath) || tproject.isOnOutputPath(rootPath)) {
        applicationRoot = null;
        throw new TapestryModelException(null);
      }
    }
    servletContextRoot = tproject.getWebContextFolder();
    if (servletContextRoot != null && servletContextRoot.exists()) {
      IPath contextRootPath = servletContextRoot.getFullPath();
      if (tproject.isOnOutputPath(contextRootPath) || tproject.isOnOutputPath(contextRootPath)) {
        servletContextRoot = null;
        throw new TapestryModelException(null);
      }
      ArrayList servletFolders = new ArrayList();
      for (int i = 0; i < knownServletNames.length; i++) {
        if (knownServletNames[i] == null || "".equals(knownServletNames[i].trim())) {
          throw new TapestryModelException(null);
        }
        IFolder servletFolder = servletContextRoot.getFolder(knownServletNames[i]);
        if (servletFolder.exists()) {
          servletFolders.add(servletFolder);
        }

      }
      
        applicationContextRoots =
          (IFolder[]) servletFolders.toArray(new IFolder[servletFolders.size()]);
     
    }

  }

  private IPackageFragment[] getPackageFragmentsInRoots(
    IPackageFragmentRoot[] roots,
    IJavaProject project) {

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

  public void findAll(ILookupRequestor requestor) {
    findAllInClasspath(requestor, ACCEPT_ANY);
    findAllInFolders(requestor);
  }

  public void findAllInFolders(ILookupRequestor requestor) {
    if (applicationRoot != null) {
      seekInFolder(applicationRoot, requestor, ACCEPT_HTML, false);
    }
    if (servletContextRoot != null) {
      seekInFolder(
        servletContextRoot,
        requestor,
        ACCEPT_APPLICATIONS | ACCEPT_COMPONENTS | ACCEPT_PAGES | ACCEPT_HTML,
        false);
    }
    for (int i = 0; i < applicationContextRoots.length; i++) {
      seekInFolder(
        applicationContextRoots[i],
        requestor,
        ACCEPT_APPLICATIONS | ACCEPT_COMPONENTS | ACCEPT_PAGES | ACCEPT_HTML,
        true);
    }
  }

  public void findAllInClasspath(ILookupRequestor requestor, int flags) {
    if (!initialized) {
      throw new Error("not initialized");
    }
    int count = packageFragmentRoots.length;
    for (int i = 0; i < count; i++) {
      if (requestor.isCancelled())
        return;
      IPackageFragmentRoot root = packageFragmentRoots[i];
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
          if (seekInPackage((IPackageFragment) packages[j], requestor, flags)) {
            return;
          }
        }
      }
    }
  }

  public boolean seekInFolder(
    IFolder folder,
    ILookupRequestor requestor,
    int flags,
    boolean childFoldersInError) {
    if (!initialized) {
      throw new Error("not initialized");
    }
    if (folder.exists()) {
      try {
        IResource[] members = folder.members();
        for (int i = 0; i < members.length; i++) {
          if ((members[i] instanceof IFile)) {
            if (acceptAsTapestry((IStorage) members[i], folder, requestor, flags)) {
              requestor.accept((IStorage) members[i], folder);
            }
          } else if (members[i] instanceof IFolder && childFoldersInError) {
            seekInFolder((IFolder) members[i], requestor, ACCEPT_NONE, true);
          }
        }
      } catch (CoreException e) {
        // ignore it
      }
    }
    return false;
  }

  public boolean seekInPackage(IPackageFragment pkg, ILookupRequestor requestor, int flags) {

    if (!initialized) {
      throw new Error("not initialized");
    }
    boolean stopLooking = false;

    IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();

    try {
      int packageFlavor = root.getKind();

      switch (packageFlavor) {
        case IPackageFragmentRoot.K_BINARY :
          stopLooking = seekInBinaryPackage(pkg, requestor, flags);
          break;
        case IPackageFragmentRoot.K_SOURCE :
          stopLooking = seekInSourcePackage(pkg, requestor, flags);
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
    IPackageFragment pkg,
    ILookupRequestor requestor,
    int flags) {
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
      if (acceptAsTapestry(jarFile, pkg, requestor, flags)) {
        requestor.accept((IStorage) jarFile, pkg);
      }
    }
    return false;
  }

  protected boolean seekInSourcePackage(
    IPackageFragment pkg,
    ILookupRequestor requestor,
    int flags) {
    Object[] files = null;
    try {
      files = getSourcePackageResources(pkg);

    } catch (CoreException npe) {
      return false; // the package is not present
    }
    if (files == null) {
      return false;
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
      if (acceptAsTapestry(file, pkg, requestor, flags)) {
        requestor.accept((IStorage) file, pkg);
      }
    }
    return false;
  }

  /**
   * Method getPackageResources.
   * @param pkg
   * @return Object[]
   */
  private Object[] getSourcePackageResources(IPackageFragment pkg) throws CoreException {
    Object[] result = new Object[0];
    if (!pkg.isDefaultPackage()) {
      result = pkg.getNonJavaResources();
    } else {
      IContainer container = (IContainer) pkg.getUnderlyingResource();
      if (container != null && container.exists()) {
        IResource[] members = container.members(false);
        ArrayList resultList = new ArrayList();
        for (int i = 0; i < members.length; i++) {
          if (members[i] instanceof IFile) {
            resultList.add(members[i]);
          }
          result = resultList.toArray();
        }
      }

    }
    return result;
  }

  public boolean acceptAsTapestry(
    IStorage s,
    Object parent,
    ILookupRequestor requestor,
    int acceptFlags) {
    String extension = s.getFullPath().getFileExtension();
    if (!seekExtensions.contains(extension)) {
      return false;
    }
    if ((acceptFlags & ACCEPT_ANY) != 0) {
      return true;
    }
    if ("jwc".equals(extension) && (acceptFlags & ACCEPT_COMPONENTS) == 0) {
      markBadLocation(s, parent, requestor);
      return false;
    }
    if ("application".equals(extension) && (acceptFlags & ACCEPT_APPLICATIONS) == 0) {
      markBadLocation(s, parent, requestor);
      return false;
    }
    if ("html".equals(extension) && (acceptFlags & ACCEPT_HTML) == 0) {
      markBadLocation(s, parent, requestor);
      return false;
    }
    if ("library".equals(extension) && (acceptFlags & ACCEPT_LIBRARIES) == 0) {
      markBadLocation(s, parent, requestor);
      return false;
    }
    if ("page".equals(extension) && (acceptFlags & ACCEPT_PAGES) == 0) {
      markBadLocation(s, parent, requestor);
      return false;
    }
    if ("script".equals(extension) && (acceptFlags & ACCEPT_SCRIPT) == 0) {
      markBadLocation(s, parent, requestor);
      return false;
    }
    return true;
  }

  private void markBadLocation(IStorage s, Object parent, ILookupRequestor requestor) {
    String extension = s.getFullPath().getFileExtension();
    String message = extension + "files not allowed here.";
    requestor.markBadLocation(s, parent, requestor, message);
  }

}