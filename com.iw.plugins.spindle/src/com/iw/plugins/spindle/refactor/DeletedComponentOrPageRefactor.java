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
package com.iw.plugins.spindle.refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.project.TapestryProject;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.ui.TapestryStorageLabelProvider;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class DeletedComponentOrPageRefactor implements IResourceChangeListener, IResourceDeltaVisitor {

  private TapestryProject project;
  private List potentialModelDeletes;
  private LibraryRefactorer refactorer = null;

  public DeletedComponentOrPageRefactor(TapestryProject project) {
    super();
    this.project = project;
  }

  /**
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(IResourceChangeEvent)
   */
  public void resourceChanged(IResourceChangeEvent event) {

    if (event.getType() != IResourceChangeEvent.PRE_AUTO_BUILD) {

      return;

    }

    IProject thisProject = project.getProject();
    IResourceDelta topLevelDelta = event.getDelta();

    if (topLevelDelta != null && topLevelDelta.getKind() == IResourceDelta.CHANGED) {

      IResourceDelta projectDelta = topLevelDelta.findMember(thisProject.getFullPath());

      if (projectDelta != null) { 

        IJavaProject jproject = null;
        IStorage projectStorage = null;
        potentialModelDeletes = new ArrayList();
        refactorer = null;

        try {

          jproject = TapestryPlugin.getDefault().getJavaProjectFor(thisProject);

        } catch (CoreException e) {

        }

        if (jproject == null) {

          return;

        }

        for (Iterator iter = getSourceRootPaths(jproject).iterator(); iter.hasNext();) {
          IPath element = (IPath) iter.next();
          IResourceDelta packageRootDelta = topLevelDelta.findMember(element);

          if (packageRootDelta != null) {

            try {

              packageRootDelta.accept(this);

            } catch (CoreException e) {
            }

          }

        }
        
        if (potentialModelDeletes.isEmpty()) {
        	
        	return;
        	
        }

        TapestryLibraryModel baseModel = null;

        try {

          baseModel = (TapestryLibraryModel) project.getProjectModel();

          if (baseModel == null) {

            return;

          }

          getRefactorer(baseModel);

        } catch (CoreException e) {

        }

        if (refactorer == null) {

          return;

        }

        HashMap confirmed = getUserConfirmed(findConfirmed());

        if (!confirmed.isEmpty()) {

          try {

            getRefactorer(baseModel);

            IPluginLibrarySpecification refactorSpec = refactorer.getSpecification();

            for (Iterator iter = confirmed.keySet().iterator(); iter.hasNext();) {

              IFile element = (IFile) iter.next();
              String alias = (String) confirmed.get(element);

              String extension = element.getFileExtension();

              if ("jwc".equals(extension)) {

                refactorSpec.removeComponentSpecificationPath(alias);

              } else {

                refactorSpec.removePageSpecificationPath(alias);

              }

            }

            refactorer.commit(new NullProgressMonitor());

          } catch (CoreException e) {

            ErrorDialog.openError(
              TapestryPlugin.getDefault().getActiveWorkbenchShell(),
              "Spindle Refactor Error",
              "can't continue",
              e.getStatus());
          }

        }

      }

    }

  }

  private HashMap getUserConfirmed(HashMap confirmed) {

    if (confirmed.isEmpty()) {

      return confirmed;

    }

    final HashMap useConfirmed = confirmed;

    HashMap userConfirmed = new HashMap();

    IStructuredContentProvider content = new IStructuredContentProvider() {
      public Object[] getElements(Object inputElement) {
        return useConfirmed.entrySet().toArray();
      }
      public void dispose() {
      }
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    };

    ILabelProvider labels = new TapestryStorageLabelProvider() {

      public Image getImage(Object element) {
        Map.Entry entry = (Map.Entry) element;
        return super.getImage((IStorage) entry.getKey());
      }

      public String getText(Object element) {
        Map.Entry entry = (Map.Entry) element;
        IResource resource = (IResource) entry.getKey();
        String extension = resource.getFileExtension();
        String tapestryPath = resource.getFullPath().toString();
        try {

          tapestryPath = getTapestryPath(resource);

        } catch (CoreException e) {
        }

        if ("jwc".equals(extension)) {

          return "<component-alias type=\"" + (String) entry.getValue() + "\" specification-path=\"" + tapestryPath + "\"/>";

        } else {

          return "<page name=\"" + (String) entry.getValue() + "\" specification-path=\"" + tapestryPath + "\"/>";
        }
      }
    };

    ListSelectionDialog dialog =
      new ListSelectionDialog(
        TapestryPlugin.getDefault().getActiveWorkbenchShell(),
        confirmed,
        content,
        labels,
        "Selected will be removed from " + refactorer.getEditableModel().getUnderlyingStorage().getName());

    if (dialog.open() == dialog.OK) {

      Object[] results = dialog.getResult();
      for (int i = 0; i < results.length; i++) {

        Map.Entry resultEntry = (Map.Entry) results[i];
        userConfirmed.put(resultEntry.getKey(), resultEntry.getValue());

      }

    }

    return userConfirmed;

  }

  private HashMap findConfirmed() {
    HashMap result = new HashMap();

    TapestryLibraryModel baseModel = refactorer.getEditableModel();

    for (Iterator iter = potentialModelDeletes.iterator(); iter.hasNext();) {

      IFile potential = (IFile) iter.next();

      try {

        String tapestryPath = getTapestryPath(potential);

        if (tapestryPath == null) {

          continue;
        }

        String extension = potential.getFileExtension();

        String alias = null;

        if ("jwc".equals(extension)) {

          alias = baseModel.findComponentAlias(tapestryPath);

        } else {

          alias = baseModel.findPageName(tapestryPath);
        }

        if (alias != null) {

          result.put(potential, alias);

        }

      } catch (CoreException e) {
      }

    }

    return result;
  }

  private String findAlias(String tapestryPath, String extension) throws CoreException {
    String alias = null;

    TapestryLibraryModel libModel = refactorer.getEditableModel();

    if ("jwc".equals(extension)) {

      alias = libModel.findComponentAlias(tapestryPath);

    } else {

      alias = libModel.findPageName(tapestryPath);
    }

    return alias;
  }

  private void getRefactorer(TapestryLibraryModel baseModel) throws CoreException {
    if (refactorer == null) {

      refactorer = new LibraryRefactorer(project, baseModel, true);

    }
  }

  private String getTapestryPath(IResource potential) throws CoreException {

    String tapestryPath = null;

    IPackageFragment fragment = project.getLookup().findPackageFragment((IStorage) potential);

    if (fragment != null) {

      if ("".equals(fragment.getElementName())) {

        return "/" + potential.getName();

      }

      tapestryPath = "/" + fragment.getElementName().replace('.', '/') + "/" + potential.getName();
    } else {

      // do a terrible kludge as the package fragment has been deleted.
      IPath path = potential.getFullPath();
      int pathSegmentCount = path.segmentCount();
      String name = potential.getName();
      IPluginLibrarySpecification spec = refactorer.getEditableModel().getSpecification();

      if (name.endsWith(".jwc")) {

        for (Iterator iter = spec.getComponentAliases().iterator(); iter.hasNext();) {

          String alias = (String) iter.next();
          String specPath = spec.getComponentSpecificationPath(alias);

          if (!path.isValidPath(specPath)) {
            continue;
          }
          if (specPath == null || !specPath.endsWith("/" + name)) {
            continue;
          }
          IPath pathObject = new Path(specPath);
          IPath temp = new Path(path.toString());
          int cut = pathSegmentCount - pathObject.segmentCount();
          temp = temp.uptoSegment(cut);
          temp = temp.append(pathObject);

          if (temp.equals(path)) {
            tapestryPath = specPath;
            break;

          }
        }

      } else if (name.endsWith(".page")) {

        for (Iterator iter = spec.getPageNames().iterator(); iter.hasNext();) {

          String pageName = (String) iter.next();
          String specPath = spec.getPageSpecificationPath(pageName);

          if (!path.isValidPath(specPath)) {
            continue;
          }
          if (specPath == null || !specPath.endsWith("/" + name)) {
            continue;
          }
          IPath pathObject = new Path(specPath);
          IPath temp = new Path(path.toString());
          int cut = pathSegmentCount - pathObject.segmentCount();
          temp = temp.uptoSegment(cut);
          temp.append(pathObject);

          if (temp.equals(pathObject)) {
            tapestryPath = specPath;
            break;

          }

        }
      }

    }

    return tapestryPath;

  }

  private List getSourceRootPaths(IJavaProject jproject) {

    ArrayList result = new ArrayList();

    try {

      IPackageFragmentRoot[] roots = jproject.getPackageFragmentRoots();

      for (int i = 0; i < roots.length; i++) {

        if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {

          result.add(roots[i].getPath());

        }

      }
    } catch (JavaModelException e) {
    }

    return result;

  }

  /**
   * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(IResourceDelta)
   */
  public boolean visit(IResourceDelta delta) throws CoreException {

    try {

      IFile deltaFile = (IFile) delta.getResource();
      String extension = deltaFile.getFullPath().getFileExtension();

      if (extension != null && ("jwc".equals(extension) || "page".equals(extension))) {

        if (delta.getKind() == IResourceDelta.REMOVED && (delta.getFlags() & IResourceDelta.MOVED_TO) == 0) {

          potentialModelDeletes.add(deltaFile);

        }

      }

    } catch (ClassCastException e) {
    }

    return true;
  }

}
