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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.wizards.factories.LibraryFactory;


public class LibraryWizardPage extends NewTapestryProjectPage { 

  private final static String PAGE_NAME = "NewTapLibWizardPage";

  protected final static String CONTAINER = PAGE_NAME + ".container";
  protected final static String PACKAGE = PAGE_NAME + ".package";
  protected final static String LIBNAME = PAGE_NAME + ".libraryname";

  //label widths
  private static int APP_WIDTH = 64;
  private static int SERV_WIDTH = 92;

  private ContainerDialogField fContainerDialogField;
  private PackageDialogField fPackageDialogField;
  private ApplicationNameField fLibraryNameDialog;

  private IFile library = null;

  private DialogField[] applicationFields;

  /**
   * Constructor for NewTapLibWizardPage1
   */
  public LibraryWizardPage(IWorkspaceRoot root) {
    super(MessageUtil.getString("NewTapLibWizardPage.title"));

    this.setImageDescriptor(
      ImageDescriptor.createFromURL(TapestryImages.getImageURL("application32.gif")));
    this.setDescription(MessageUtil.getString("NewTapLibWizardPage.description"));

    IDialogFieldChangedListener listener = new FieldEventsAdapter();

    fContainerDialogField = new ContainerDialogField(CONTAINER, APP_WIDTH);
    connect(fContainerDialogField);
    fContainerDialogField.addListener(listener); 

    fPackageDialogField = new PackageDialogField(PACKAGE, APP_WIDTH);
    connect(fPackageDialogField);
    fPackageDialogField.addListener(listener);

    fLibraryNameDialog = new ApplicationNameField(LIBNAME);
    connect(fLibraryNameDialog);
    fLibraryNameDialog.addListener(listener);

  }

  /**
   * Should be called from the wizard with the input element. 
   */
  public void init(IClasspathEntry[] entries, IProject project) {

    fContainerDialogField.init(entries, project, getWizard().getContainer());
    String temp = fPackageDialogField.getTextValue();
    if (temp == null) {

      fPackageDialogField.setPackageFragment("");

    }
    temp = fLibraryNameDialog.getTextValue();
    if (temp == null || "".equals(temp.trim())) {
      fLibraryNameDialog.setTextValue(project.getName());
    }

    setFocus();

    updateStatus();

  }

  /**
   * @see DialogPage#createControl(Composite)
   */
  public void createControl(Composite container) {
    Composite composite = new Composite(container, SWT.NONE);

    FormLayout layout = new FormLayout();
    layout.marginWidth = 4;
    layout.marginHeight = 4;
    composite.setLayout(layout);

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.width = 400;
    composite.setLayoutData(formData);

    Control appNameControl = fLibraryNameDialog.getControl(composite);
    Control containerControl = fContainerDialogField.getControl(composite);
    Control packageControl = fPackageDialogField.getControl(composite);

    addControl(appNameControl, composite, 10);
    Control separator = createSeparator(composite, appNameControl);
    addControl(containerControl, separator, 4);
    addControl(packageControl, containerControl, 4);

    setControl(composite);
    setFocus();

  }

  /**
   * @see NewElementWizardPage#getRunnable()
   */
  public IRunnableWithProgress getRunnable(Object object) {
  	
  	final IJavaProject project = (IJavaProject)object;
    return new IRunnableWithProgress() {

      public void run(IProgressMonitor monitor)
        throws InvocationTargetException, InterruptedException {

        try {

          if (monitor == null) {

            monitor = new NullProgressMonitor();

          }
          createLibraryResource(project, new SubProgressMonitor(monitor, 1));
          monitor.done();

        } catch (CoreException e) {

          throw new InvocationTargetException(e);

        }
      }
    };
  }

  public boolean performFinish() {
    return true;
  }

  public void createLibraryResource(IJavaProject project, IProgressMonitor monitor)
    throws InterruptedException, CoreException {

    project.open(monitor);

    NewProjectUtils.fixClasspath(project, monitor);

    IClasspathEntry srcEntry = fContainerDialogField.getClasspathEntry();
    IPackageFragmentRoot root = project.getPackageFragmentRoots(srcEntry)[0];

    String packageName = fPackageDialogField.getTextValue();
    IPackageFragment pack = root.createPackageFragment(packageName, true, monitor);

    String libname = fLibraryNameDialog.getTextValue();
    library = LibraryFactory.createLibrary(root, pack, libname, monitor);
    
    
    ITapestryProject tproject =
      TapestryPlugin.getDefault().addTapestryProjectNatureTo(project, monitor);

    tproject.setProjectStorage(library);

    tproject.getModelManager().getReadOnlyModel(library);
    
  }

  public IResource getResource() {
    return library;
  }

  protected void setFocus() {
    fLibraryNameDialog.setFocus();
  }

  private class FieldEventsAdapter implements IDialogFieldChangedListener {

    public void dialogFieldChanged(DialogField field) {

      updateStatus();
    }
    /**
     * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
     */
    public void dialogFieldButtonPressed(DialogField field) {
    }

    /**
     * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
     */
    public void dialogFieldStatusChanged(IStatus status, DialogField field) {

    }

  }

}