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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.iw.plugins.spindle.TapestryImages;
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
public class DeletedComponentOrPageRefactor
  implements IResourceChangeListener, IResourceDeltaVisitor {

  private TapestryProject project;
  private List potentialModelDeletes;
  private TapestryLibraryModel baseModel = null;
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

    if (topLevelDelta.getKind() == IResourceDelta.CHANGED) {

      IResourceDelta projectDelta = topLevelDelta.findMember(thisProject.getFullPath());

      if (projectDelta != null) {

        IJavaProject jproject = null;
        IStorage projectStorage = null;
        potentialModelDeletes = new ArrayList();

        try {

          baseModel = (TapestryLibraryModel) project.getProjectModel();
          projectStorage = baseModel.getUnderlyingStorage();

          //      refactorer =
          //        new LibraryRefactorer(project, (TapestryLibraryModel) project.getProjectModel(), true);
          jproject = TapestryPlugin.getDefault().getJavaProjectFor(thisProject);

        } catch (CoreException e) {

        }

        if (projectStorage == null || jproject == null || baseModel == null) {

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

        HashMap confirmed = getUserConfirmed(findConfirmed());

        if (!confirmed.isEmpty()) {

          try {

            refactorer = new LibraryRefactorer(project, baseModel, true);

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

          return "<component-alias type=\""
            + (String) entry.getValue()
            + "\" specification-path=\""
            + tapestryPath
            + "\"/>";

        } else {

          return "<page name=\""
            + (String) entry.getValue()
            + "\" specification-path=\""
            + tapestryPath
            + "\"/>";
        }
      }
    };

    ListSelectionDialog dialog =
      new ListSelectionDialog(
        TapestryPlugin.getDefault().getActiveWorkbenchShell(),
        confirmed,
        content,
        labels,
        "Selected will be removed from " + baseModel.getUnderlyingStorage().getName());

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

    for (Iterator iter = potentialModelDeletes.iterator(); iter.hasNext();) {

      IFile potential = (IFile) iter.next();

      try {

        String tapestryPath = getTapestryPath(potential);

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

  private String getTapestryPath(IResource potential) throws CoreException {

    IPackageFragment fragment = project.getLookup().findPackageFragment((IStorage) potential);

    String tapestryPath =
      "/" + fragment.getElementName().replace('.', '/') + "/" + potential.getName();
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

        if (delta.getKind() == IResourceDelta.REMOVED
          && (delta.getFlags() & IResourceDelta.MOVED_TO) == 0) {

          potentialModelDeletes.add(deltaFile);

        }

      }

    } catch (ClassCastException e) {
    }

    return true;
  }

}
