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
package com.iw.plugins.spindle.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.dialogfields.CheckBoxField;
import com.iw.plugins.spindle.dialogfields.DialogField;
import com.iw.plugins.spindle.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.factories.ComponentFactory;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.wizards.fields.ComponentNameField;
import com.iw.plugins.spindle.wizards.fields.ContainerDialogField;
import com.iw.plugins.spindle.wizards.fields.PackageDialogField;

public class NewTapComponentWizardPage extends NewTapestryElementWizardPage {

  String PAGE_NAME;

  private static int LABEL_WIDTH = 64;

  String CONTAINER;
  String PACKAGE;
  String SPEC_CLASS;
  String COMPONENTNAME;
  String GENERATE_HTML;

  private ContainerDialogField fContainerDialogField;
  private PackageDialogField fPackageDialogField;
  //  private SpecClassDialogField fSpecClassDialogField;
  private ComponentNameField fComponentNameDialog;

  private CheckBoxField fGenerateHTML;

  private DialogField fNextLabel;

  private IFile component = null;

  /**
   * Constructor for NewTapAppWizardPage1
   */
  public NewTapComponentWizardPage(IWorkspaceRoot root, String pageName) {
    super(MessageUtil.getString(pageName + ".title"));
    PAGE_NAME = pageName;
    CONTAINER = PAGE_NAME + ".container";
    PACKAGE = PAGE_NAME + ".package";
    SPEC_CLASS = PAGE_NAME + ".specclass";
    COMPONENTNAME = PAGE_NAME + ".componentname";

    GENERATE_HTML =
      PAGE_NAME + ".generateHTML";
      
    this.setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("component32.gif")));

    this.setDescription(MessageUtil.getString(PAGE_NAME + ".description"));

    IDialogFieldChangedListener listener = new FieldEventsAdapter();

    fContainerDialogField = new ContainerDialogField(CONTAINER, root, LABEL_WIDTH);
    connect(fContainerDialogField);
    fContainerDialogField.addListener(listener);

    fPackageDialogField = new PackageDialogField(PACKAGE, LABEL_WIDTH);
    connect(fPackageDialogField);
    fPackageDialogField.addListener(listener);

    //    fSpecClassDialogField = createSpecClassField();
    //    connect(fSpecClassDialogField);
    //    fSpecClassDialogField.addListener(listener);

    fComponentNameDialog = new ComponentNameField(COMPONENTNAME);
    connect(fComponentNameDialog);
    fComponentNameDialog.addListener(listener);

    fGenerateHTML = new CheckBoxField(MessageUtil.getString(GENERATE_HTML + ".label"));

    fNextLabel = new DialogField("Choose a class for the specification on the next page...");

  }

  /**
   * Should be called from the wizard with the input element. 
   */
  public void init(IJavaElement jelem) {

    WizardDialog container = (WizardDialog) getWizard().getContainer();
    IRunnableContext context = (IRunnableContext) container;

    fContainerDialogField.init(jelem, context);
    fPackageDialogField.init(fContainerDialogField, context);
    //    fSpecClassDialogField.init(fPackageDialogField);
    IPackageFragment pack = null;
    if (jelem != null) {
      pack = (IPackageFragment) Utils.findElementOfKind(jelem, IJavaElement.PACKAGE_FRAGMENT);
    }
    fPackageDialogField.setPackageFragment(pack);
    fComponentNameDialog.setTextValue("");
    fComponentNameDialog.init(fPackageDialogField);
    fGenerateHTML.setCheckBoxValue(true);
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

    Control nameFieldControl = fComponentNameDialog.getControl(composite);
    Control containerFieldControl = fContainerDialogField.getControl(composite);
    Control packageFieldControl = fPackageDialogField.getControl(composite);
    Control genHTML = fGenerateHTML.getControl(composite);
    Control labelControl = fNextLabel.getControl(composite);

    addControl(nameFieldControl, composite, 10);
    Control separator = createSeparator(composite, nameFieldControl);

    addControl(containerFieldControl, separator, 4);
    addControl(packageFieldControl, containerFieldControl, 4);

    separator = createSeparator(composite, packageFieldControl);

    addControl(genHTML, separator, 10);

    addControl(labelControl, genHTML, 60);

    setControl(composite);
    setFocus();
  }

  /**
   * @see NewElementWizardPage#getRunnable()
   */
  public IRunnableWithProgress getRunnable(IType specClass) {
    final IType useClass = specClass;
    return new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          if (monitor == null) {
            monitor = new NullProgressMonitor();
          }
          createComponentResource(new SubProgressMonitor(monitor, 1), useClass);
          if (fGenerateHTML.getCheckBoxValue()) {
            createHTMLResource(new SubProgressMonitor(monitor, 1));
          }
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

  public void createComponentResource(IProgressMonitor monitor, IType specClass) throws InterruptedException, CoreException {

    IPackageFragmentRoot root = fContainerDialogField.getPackageFragmentRoot();
    IPackageFragment pack = fPackageDialogField.getPackageFragment();
    String compname = fComponentNameDialog.getTextValue();
    component = ComponentFactory.createComponent(root, pack, compname, specClass, monitor);
  }

  public void createHTMLResource(IProgressMonitor monitor) throws InterruptedException, CoreException {
    IPackageFragmentRoot root = fContainerDialogField.getPackageFragmentRoot();
    IPackageFragment pack = fPackageDialogField.getPackageFragment();
    String componentName = fComponentNameDialog.getTextValue();

    IContainer container = (IContainer) pack.getUnderlyingResource();
    IFile file1 = container.getFile(new Path(componentName + ".html"));
    IFile file2 = container.getFile(new Path(componentName + ".htm"));

    if (file1.exists() || file2.exists()) {
      return;
    }

    monitor.beginTask("", 10);
    if (pack == null) {
      pack = root.getPackageFragment("");
    }
    if (!pack.exists()) {
      String packName = pack.getElementName();
      pack = root.createPackageFragment(packName, true, null);
      pack.save(new SubProgressMonitor(monitor, 1), true);
    }
    monitor.worked(1);

    InputStream contents = new ByteArrayInputStream(MessageUtil.getString(PAGE_NAME + ".genHTMLSource").getBytes());
    file1.create(contents, false, new SubProgressMonitor(monitor, 1));
    monitor.worked(1);
    monitor.done();

  }

  public IFile getComponent() {
    return component;
  }

  protected void setFocus() {
    fComponentNameDialog.setFocus();
  }

  private void checkEnabled(IStatus status) {
    boolean flag = status.isOK();
    fContainerDialogField.setEnabled(flag);
    fPackageDialogField.setEnabled(flag);
    //    fSpecClassDialogField.setEnabled(flag);
  }

  public void updateStatus() {
    super.updateStatus();
    checkEnabled(fComponentNameDialog.getStatus());
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

  /**
   * @see IWizardPage#canFlipToNextPage()
   */
  public boolean canFlipToNextPage() {
    return getCurrentStatus().isOK();

  }

  public String getChosenComponentName() {
    return fComponentNameDialog.getTextValue();
  }

  public DialogField getComponentNameField() {
    return fComponentNameDialog;
  }

  /**
   * Method getComponentContainerField.
   * @return DialogField
   */
  public DialogField getComponentContainerField() {
    return fContainerDialogField;
  }

  /**
   * Method getChosenContainer.
   * @return String
   */
  public String getChosenContainer() {
    return fContainerDialogField.getTextValue();
  }

  /**
   * Method getChoosenPackage.
   * @return String
   */
  public String getChoosenPackage() {
    return fPackageDialogField.getTextValue();
  }

  /**
   * Method getComponentPackageField.
   * @return DialogField
   */
  public DialogField getComponentPackageField() {
    return fPackageDialogField;
  }
}