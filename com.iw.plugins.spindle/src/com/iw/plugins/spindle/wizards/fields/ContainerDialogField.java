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
package com.iw.plugins.spindle.wizards.fields;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;

public class ContainerDialogField extends StringButtonField {

  protected String name;
  private IWorkspaceRoot workspaceRoot;
  private IPackageFragmentRoot currentRoot;

  
  /**
   * Constructor for ContainerDialogField.
   * @param label
   * @param labelWidth
   */
  public ContainerDialogField(String name, IWorkspaceRoot root,int labelWidth) {
    super(MessageUtil.getString(name +".label"), labelWidth);
    this.name = name;
    workspaceRoot = root;    
    currentRoot = null;
  }

  /**
   * Constructor for ContainerDialogField
   */
  public ContainerDialogField(String name, IWorkspaceRoot root) {
    this(name, root, -1);
    
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field) {

    IPackageFragmentRoot root = getPackageFragmentRoot();
    IJavaProject jproject = (root != null) ? root.getJavaProject() : null;
    root = chooseSourceContainer(jproject);
    if (root != null) {
      setPackageFragmentRoot(root, isEnabled());
    }
  }

  public void dialogFieldChanged(DialogField field) {
    if (field == this) {
      setStatus(containerChanged());
    }

    
  }

  public String getContainerText() {
    return getTextValue();
  }

 

  public IPackageFragmentRoot getPackageFragmentRoot() {
    return currentRoot;
  }

  public void setPackageFragmentRoot(IPackageFragmentRoot root, boolean canBeModified) {
    IPackageFragmentRoot old = currentRoot;
    currentRoot = root;
    String str = (root == null) ? "" : root.getPath().toString();
    setTextValue(str);
    setEnabled(canBeModified);    
  }

  public void init(IJavaElement elem, IRunnableContext context) {
    super.init(context);
    
    setButtonLabel(MessageUtil.getString(name + ".button"));

    IPackageFragmentRoot initRoot = null;
    if (elem != null) {

      initRoot = Utils.getPackageFragmentRoot(elem);
      if (initRoot == null || initRoot.isArchive()) {
        IJavaProject jproject = elem.getJavaProject();
        try {
          initRoot = null;
          IPackageFragmentRoot[] roots = jproject.getPackageFragmentRoots();
          for (int i = 0; i < roots.length; i++) {
            if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
              initRoot = roots[i];
              break;
            }
          }
        } catch (JavaModelException e) {
          TapestryPlugin.getDefault().logException(e);
        }
        if (initRoot == null) {
          initRoot = jproject.getPackageFragmentRoot("");
        }
      }
      setPackageFragmentRoot(initRoot, true);
    }
  }

  private IPackageFragmentRoot chooseSourceContainer(IJavaElement initElement) {
  	

    Class[] acceptedClasses = new Class[] { IPackageFragmentRoot.class, IJavaProject.class };
    ISelectionStatusValidator validator = new TypedElementSelectionValidator(acceptedClasses, false) {
      public boolean isSelectedValid(Object element) {
        try {
          if (element instanceof IJavaProject) {
            IJavaProject jproject = (IJavaProject) element;
            IPath path = jproject.getProject().getFullPath();
            return (jproject.findPackageFragmentRoot(path) != null);
          } else if (element instanceof IPackageFragmentRoot) {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          }
          return true;
        } catch (JavaModelException e) {
          TapestryPlugin.getDefault().logException(e); // just log, no ui in validation
        }
        return false;
      }
    };

    acceptedClasses = new Class[] { IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class };
    ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {
      public boolean select(Viewer viewer, Object parent, Object element) {
        if (element instanceof IPackageFragmentRoot) {
          try {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          } catch (JavaModelException e) {
            TapestryPlugin.getDefault().logException(e);
            return false;
          }
        }
        return super.select(viewer, parent, element);
      }
    };
    StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
    ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
    ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, provider);
    dialog.setValidator(validator);
    dialog.setSorter(new JavaElementSorter());
    dialog.setTitle(MessageUtil.getString(name + ".ChooseSourceContainerDialog.title"));
    dialog.setMessage(MessageUtil.getString(name + ".ChooseSourceContainerDialog.description"));
    dialog.addFilter(filter);
    dialog.setInput(JavaCore.create(workspaceRoot));
    dialog.setInitialSelection(initElement);

    if (dialog.open() == dialog.OK) {
      Object element = dialog.getFirstResult();
      if (element instanceof IJavaProject) {
        IJavaProject jproject = (IJavaProject) element;
        return jproject.getPackageFragmentRoot(jproject.getProject());
      } else if (element instanceof IPackageFragmentRoot) {
        return (IPackageFragmentRoot) element;
      }
      return null;
    }
    return null;

  }

  protected IStatus containerChanged() {
    SpindleStatus status = new SpindleStatus();

    currentRoot = null;
    String str = getContainerText();
    if (str == null || "".equals(str)) {
      status.setError(MessageUtil.getString(name + ".error.EnterContainerName"));
      return status;
    }
    IPath path = new Path(str);
    IResource res = workspaceRoot.findMember(path);
    if (res != null) {
      int resType = res.getType();
      if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
        IProject proj = res.getProject();
        if (!proj.isOpen()) {
          status.setError(MessageUtil.getFormattedString(name + ".error.ProjectClosed", proj.getFullPath().toString()));
          return status;
        }
        IJavaProject jproject = JavaCore.create(proj);
        currentRoot = jproject.getPackageFragmentRoot(res);
        if (currentRoot.exists()) {
          try {
            if (!proj.hasNature(JavaCore.NATURE_ID)) {
              if (resType == IResource.PROJECT) {
                status.setWarning(MessageUtil.getString(name + ".warning.NotAJavaProject"));
              } else {
                status.setWarning(MessageUtil.getString(name + ".warning.NotInAJavaProject"));
              }
              return status;
            }
          } catch (CoreException e) {
            status.setWarning(MessageUtil.getString(name + ".warning.NotAJavaProject"));
          }
          try {
            if (!Utils.isOnBuildPath(jproject, currentRoot)) {
              status.setWarning(MessageUtil.getFormattedString(name + ".warning.NotOnClassPath", str));
            }
          } catch (JavaModelException e) {
            status.setWarning(MessageUtil.getFormattedString(name + ".warning.NotOnClassPath", str));
          }
          if (currentRoot.isArchive()) {
            status.setError(MessageUtil.getFormattedString(name + ".error.ContainerIsBinary", str));
            return status;
          }
        }
        return status;
      } else {
        status.setError(MessageUtil.getFormattedString(name + ".error.NotAFolder", str));
        return status;
      }
    } else {
      status.setError(MessageUtil.getFormattedString(name + ".error.ContainerDoesNotExist", str));
      return status;
    }
  }

}