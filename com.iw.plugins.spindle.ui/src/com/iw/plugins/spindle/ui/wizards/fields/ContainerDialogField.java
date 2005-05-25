/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.wizards.fields;

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
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.eclipse.CoreUtils;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;

public class ContainerDialogField extends StringButtonField
{

  protected String fName;
  protected IWorkspaceRoot fWorkspaceRoot;
  protected JavaModel fJavaModel;
  protected IPackageFragmentRoot fCurrentRoot;
  protected boolean fThisProjectOnly;
  protected boolean fAcceptSourceOnly;

  public ContainerDialogField(String name, IWorkspaceRoot root, int labelWidth)
  {
    this(name, root, labelWidth, true);
  }

  public ContainerDialogField(String name, IWorkspaceRoot root, int labelWidth,
      boolean thisProjectOnly)
  {
    super(UIPlugin.getString(name + ".label"), labelWidth);
    this.fName = name;
    fWorkspaceRoot = root;
    fCurrentRoot = null;
    this.fThisProjectOnly = thisProjectOnly;
  }

  /**
   * Constructor for ContainerDialogField
   */
  public ContainerDialogField(String name, IWorkspaceRoot root)
  {
    this(name, root, -1);

  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field)
  {

    IPackageFragmentRoot root = getPackageFragmentRoot();
    IJavaElement initElement = (root != null)
        ? (IJavaElement) root : (IJavaElement) JavaCore.create(fWorkspaceRoot);
    root = chooseSourceContainer(initElement);
    if (root != null)
    {
      setPackageFragmentRoot(root, isEnabled());
    }
  }

  public void dialogFieldChanged(DialogField field)
  {
    if (field == this)
      refreshStatus();
  }

  public void refreshStatus()
  {
    setStatus(containerChanged());
  }

  public String getContainerText()
  {
    return getTextValue();
  }

  public IPackageFragmentRoot getPackageFragmentRoot()
  {
    return fCurrentRoot;
  }

  public void setPackageFragmentRoot(IPackageFragmentRoot root, boolean canBeModified)
  {
    IPackageFragmentRoot old = fCurrentRoot;
    fCurrentRoot = root;
    String str = (root == null) ? "" : root.getPath().toString();
    setTextValue(str);
    setEnabled(canBeModified);
  }

  public void init(IJavaElement elem, IRunnableContext context)
  {
    super.init(context);

    setButtonLabel(UIPlugin.getString(fName + ".button"));

    IPackageFragmentRoot initRoot = null;
    if (elem != null)
    {
      initRoot = CoreUtils.getPackageFragmentRoot(elem);
      if (initRoot == null)
      {
        IJavaProject jproject = elem.getJavaProject();
        try
        {
          initRoot = null;
          IPackageFragmentRoot[] roots = jproject.getPackageFragmentRoots();
          for (int i = 0; i < roots.length; i++)
          {
            if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE)
            {
              initRoot = roots[i];
              break;
            }
          }
        } catch (JavaModelException e)
        {
          UIPlugin.log_it(e);
        }
        if (initRoot == null)
        {
          initRoot = jproject.getPackageFragmentRoot("");
        }
      }
      setPackageFragmentRoot(initRoot, true);
    }
  }

  private IPackageFragmentRoot chooseSourceContainer(IJavaElement initElement)
  {
    final IJavaProject thisProject = initElement.getJavaProject();
    Class[] acceptedClasses = new Class[]{IPackageFragmentRoot.class, IJavaProject.class};
    ISelectionStatusValidator validator = new TypeSelectionValidator(acceptedClasses,
        false)
    {
      public boolean isSelectedValid(Object element)
      {
        try
        {
          boolean accepted = false;
          if (fThisProjectOnly)
          {
            accepted = element instanceof IJavaProject
                && thisProject.equals((IJavaProject) element);
          } else
          {
            accepted = element instanceof IJavaProject;
          }
          if (accepted)
          {

            IJavaProject jproject = (IJavaProject) element;
            IPath path = jproject.getProject().getFullPath();
            return (jproject.findPackageFragmentRoot(path) != null);
          } else if (element instanceof IPackageFragmentRoot)
          {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          }
          return true;
        } catch (JavaModelException e)
        {
          UIPlugin.log_it(e); // just log, no ui in validation
        }
        return false;
      }
    };

    acceptedClasses = new Class[]{IJavaModel.class, IPackageFragmentRoot.class,
        IJavaProject.class};
    ViewerFilter filter = new TypeFilter(acceptedClasses)
    {
      public boolean select(Viewer viewer, Object parent, Object element)
      {

        if (fThisProjectOnly && thisProject == null)
          return false;

        if (element instanceof IJavaProject)
        {

          return thisProject.equals((IJavaProject) element);

        }
        if (element instanceof IPackageFragmentRoot)
        {
          try
          {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          } catch (JavaModelException e)
          {
            UIPlugin.log_it(e);
            return false;
          }
        }
        return super.select(viewer, parent, element);
      }
    };
    StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
    ILabelProvider labelProvider = new JavaElementLabelProvider(
        JavaElementLabelProvider.SHOW_DEFAULT);
    ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
        getShell(),
        labelProvider,
        provider);
    dialog.setValidator(validator);
    dialog.setSorter(new JavaElementSorter());
    dialog.setTitle(UIPlugin.getString(fName + ".ChooseSourceContainerDialog.title"));
    dialog.setMessage(UIPlugin.getString(fName
        + ".ChooseSourceContainerDialog.description"));
    dialog.addFilter(filter);
    dialog.setInput(JavaCore.create(fWorkspaceRoot));
    dialog.setInitialSelection(initElement);

    if (dialog.open() == dialog.OK)
    {
      Object element = dialog.getFirstResult();
      if (element instanceof IJavaProject)
      {
        IJavaProject jproject = (IJavaProject) element;
        return jproject.getPackageFragmentRoot(jproject.getProject());
      } else if (element instanceof IPackageFragmentRoot)
      {
        return (IPackageFragmentRoot) element;
      }
      return null;
    }
    return null;

  }

  protected IStatus containerChanged()
  {
    SpindleStatus status = new SpindleStatus();

    fCurrentRoot = null;
    String str = getContainerText();
    if (str == null || "".equals(str))
    {
      status.setError(UIPlugin.getString(fName + ".error.EnterContainerName"));
      return status;
    }
    IPath path = new Path(str);
    IResource res = fWorkspaceRoot.findMember(path);
    if (res != null)
    {
      int resType = res.getType();
      if (resType == IResource.PROJECT || resType == IResource.FOLDER)
      {
        IProject proj = res.getProject();
        if (!proj.isOpen())
        {
          status.setError(UIPlugin.getString(fName + ".error.ProjectClosed", proj
              .getFullPath()
              .toString()));
          return status;
        }
        IJavaProject jproject = JavaCore.create(proj);
        fCurrentRoot = jproject.getPackageFragmentRoot(res);
        if (fCurrentRoot.exists())
        {
          try
          {
            if (!proj.hasNature(JavaCore.NATURE_ID))
            {
              if (resType == IResource.PROJECT)
              {
                status.setWarning(UIPlugin.getString(fName + ".warning.NotAJavaProject"));
              } else
              {
                status.setWarning(UIPlugin
                    .getString(fName + ".warning.NotInAJavaProject"));
              }
              return status;
            }
          } catch (CoreException e)
          {
            status.setWarning(UIPlugin.getString(fName + ".warning.NotAJavaProject"));
          }
          try
          {
            if (!CoreUtils.isOnBuildPath(jproject, fCurrentRoot))
            {
              status.setWarning(UIPlugin
                  .getString(fName + ".warning.NotOnClassPath", str));
            }
          } catch (JavaModelException e)
          {
            status.setWarning(UIPlugin.getString(fName + ".warning.NotOnClassPath", str));
          }
          if (fCurrentRoot.isArchive())
          {
            status.setError(UIPlugin.getString(fName + ".error.ContainerIsBinary", str));
            return status;
          }
        } else
        {
          status.setError(UIPlugin.getString(fName + ".error.NotOnClassPath", str));
        }
        return status;
      } else
      {
        status.setError(UIPlugin.getString(fName + ".error.NotAFolder", str));
        return status;
      }
    } else
    {
      status.setError(UIPlugin.getString(fName + ".error.ContainerDoesNotExist", str));
      return status;
    }
  }

  /**
   * @param b
   */
  public void setAcceptSourceContainers(boolean flag)
  {
    fAcceptSourceOnly = flag;
  }

}