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
package com.iw.plugins.spindle.wizards.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.ui.ChooseDialog;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;

public class ContainerDialogField extends StringButtonField {

  protected String name;
  private IClasspathEntry currentRoot;
  private IClasspathEntry projectRoot;
  private List validRoots;
  private IProject project;

  /**
   * Constructor for ContainerDialogField.
   * @param label
   * @param labelWidth
   */
  public ContainerDialogField(String name, int labelWidth) {
    super(MessageUtil.getString(name + ".label"), labelWidth);
    this.name = name;
    currentRoot = null;
  }

  /**
   * Constructor for ContainerDialogField
   */
  public ContainerDialogField(String name) {
    this(name, -1);

  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field) {

    IClasspathEntry root = chooseSourceContainer(getClasspathEntry());
    if (root != null) {
      setClasspathEntry(root, true);
    }
  }

  //  public void dialogFieldChanged(DialogField field) {
  //    if (field == this) {
  //      setStatus(containerChanged());
  //    }
  //
  //  }

  public String getContainerText() {
    return getTextValue();
  }

  public IClasspathEntry getClasspathEntry() {
    return currentRoot;
  }

  public void setClasspathEntry(IClasspathEntry root, boolean canBeModified) {
    currentRoot = root;
    String str = (root == null) ? "" : root.getPath().toString();
    setTextValue(str);
    setEnabled(canBeModified);
  }

  public void init(IClasspathEntry[] entries, IProject project, IRunnableContext context) {
    super.init(context);

    setButtonLabel(MessageUtil.getString(name + ".button"));
    IPackageFragmentRoot initRoot = null;
    validRoots = new ArrayList();
    projectRoot = null;

    for (int i = 0; i < entries.length; i++) {

      if (entries[i].getEntryKind() == entries[i].CPE_SOURCE) {

        validRoots.add(entries[i]);

        if (entries[i].getPath().equals(project.getFullPath())) {

          projectRoot = entries[i];

        }

      }

    }

    if (projectRoot != null && validRoots.size() == 1) {

      setClasspathEntry(projectRoot, false);

    } else {

      if (projectRoot == null && !validRoots.isEmpty()) {

        setClasspathEntry((IClasspathEntry) validRoots.get(0), true);

      } else {

        for (Iterator iter = validRoots.iterator(); iter.hasNext();) {
          IClasspathEntry element = (IClasspathEntry) iter.next();
          if (element != projectRoot) {

            setClasspathEntry(element, true);
            break;
          }
        }
      }

    }
  }

  private IClasspathEntry chooseSourceContainer(IClasspathEntry current) {

    ChooseDialog dialog = new ChooseDialog(getShell(), "Choose Source Container");
    dialog.setContentProvider(new ClasspathEntryProvider());
    dialog.setLabelProvider(new ClasspathLabelProvider());

    dialog.create();

    dialog.setSelected(current);

    if (dialog.open() == dialog.OK) {

      IStructuredSelection selection = (IStructuredSelection) dialog.getSelection();
      if (!selection.isEmpty()) {

        return (IClasspathEntry) selection.getFirstElement();

      }

    }
    return null;
  }

  //  protected IStatus containerChanged() {
  //    SpindleStatus status = new SpindleStatus();
  //    //
  //    //    currentRoot = null;
  //    //    String str = getContainerText();
  //    //    if (str == null || "".equals(str)) {
  //    //      status.setError(MessageUtil.getString(name + ".error.EnterContainerName"));
  //    //      return status;
  //    //    }
  //    //    IPath path = new Path(str);
  //    //    IResource res = workspaceRoot.findMember(path);
  //    //    if (res != null) {
  //    //      int resType = res.getType();
  //    //      if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
  //    //        IProject proj = res.getProject();
  //    //        if (!proj.isOpen()) {
  //    //          status.setError(
  //    //            MessageUtil.getFormattedString(
  //    //              name + ".error.ProjectClosed",
  //    //              proj.getFullPath().toString()));
  //    //          return status;
  //    //        }
  //    //        IJavaProject jproject = JavaCore.create(proj);
  //    //        currentRoot = jproject.getPackageFragmentRoot(res);
  //    //        if (currentRoot.exists()) {
  //    //          try {
  //    //            if (!proj.hasNature(JavaCore.NATURE_ID)) {
  //    //              if (resType == IResource.PROJECT) {
  //    //                status.setWarning(MessageUtil.getString(name + ".warning.NotAJavaProject"));
  //    //              } else {
  //    //                status.setWarning(MessageUtil.getString(name + ".warning.NotInAJavaProject"));
  //    //              }
  //    //              return status;
  //    //            }
  //    //          } catch (CoreException e) {
  //    //            status.setWarning(MessageUtil.getString(name + ".warning.NotAJavaProject"));
  //    //          }
  //    //          try {
  //    //            if (!Utils.isOnBuildPath(jproject, currentRoot)) {
  //    //              status.setWarning(
  //    //                MessageUtil.getFormattedString(name + ".warning.NotOnClassPath", str));
  //    //            }
  //    //          } catch (JavaModelException e) {
  //    //            status.setWarning(
  //    //              MessageUtil.getFormattedString(name + ".warning.NotOnClassPath", str));
  //    //          }
  //    //          if (currentRoot.isArchive()) {
  //    //            status.setError(MessageUtil.getFormattedString(name + ".error.ContainerIsBinary", str));
  //    //            return status;
  //    //          }
  //    //        }
  //    //        return status;
  //    //      } else {
  //    //        status.setError(MessageUtil.getFormattedString(name + ".error.NotAFolder", str));
  //    //        return status;
  //    //      }
  //    //    } else {
  //    //      status.setError(MessageUtil.getFormattedString(name + ".error.ContainerDoesNotExist", str));
  //    //      return status;
  //    //    }
  //    return status;
  //  }

  class ClasspathEntryProvider implements ITreeContentProvider {
    public Object[] getElements(Object object) {
      if (validRoots.isEmpty()) {

        return new Object[0];

      }

      return validRoots.toArray();
    }
    public Object[] getChildren(Object parent) {
      return new Object[0];
    }
    public Object getParent(Object child) {
      return null;
    }
    public boolean hasChildren(Object parent) {
      return false;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

  }

  class ClasspathLabelProvider extends LabelProvider {

    Image projectImage = TapestryImages.getSharedImage("project16.gif");
    Image folderImage = TapestryImages.getSharedImage("folder16.gif");

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
     */
    public Image getImage(Object element) {

      if (element == projectRoot) {

        return projectImage;

      }

      return folderImage;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
     */
    public String getText(Object element) {
      return ((IClasspathEntry) element).getPath().toString();
    }

  }

}