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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.jdom.DOMFactory;
import org.eclipse.jdt.core.jdom.IDOMCompilationUnit;
import org.eclipse.jdt.core.jdom.IDOMMethod;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jface.operation.IRunnableContext;
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
import com.iw.plugins.spindle.dialogfields.CheckBoxField;
import com.iw.plugins.spindle.dialogfields.DialogField;
import com.iw.plugins.spindle.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.factories.ApplicationClassFactory;
import com.iw.plugins.spindle.factories.ApplicationFactory;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.wizards.fields.ApplicationNameField;
import com.iw.plugins.spindle.wizards.fields.ApplicationServletClassDialog;
import com.iw.plugins.spindle.wizards.fields.ContainerDialogField;
import com.iw.plugins.spindle.wizards.fields.EngineClassDialog;
import com.iw.plugins.spindle.wizards.fields.PackageDialogField;
import com.iw.plugins.spindle.wizards.fields.ServletClassnameField;

public class NewTapAppWizardPage extends NewTapestryElementWizardPage {

  private final static String PAGE_NAME = "NewTapAppWizardPage";

  protected final static String CONTAINER = PAGE_NAME + ".container";
  protected final static String PACKAGE = PAGE_NAME + ".package";
  protected final static String ENGINE = PAGE_NAME + ".engine";
  protected final static String APPNAME = PAGE_NAME + ".appname";
  protected final static String SERVLETPACKAGE = PAGE_NAME + ".servletpackage";
  protected final static String SERVLETCLASS = PAGE_NAME + ".servletclass";
  protected final static String SERVLETSUPERCLASS = PAGE_NAME + ".servlet";
  
  //label widths
  private static int APP_WIDTH = 64;
  private static int SERV_WIDTH = 92;

  private ContainerDialogField fContainerDialogField;
  private PackageDialogField fPackageDialogField;
  private EngineClassDialog fEngineDialogField;
  private ApplicationNameField fApplicationNameDialog;

  private CheckBoxField fGenerateServletClass;
  private PackageDialogField fServletPackageDialog;
  private ServletClassnameField fServletClassDialog;
  private ApplicationServletClassDialog fServletSuperclassDialog;

  private IType generatedServletType = null;
  private IFile application = null;
  
  private DialogField [] applicationFields;
  private DialogField [] servletFields;

  /**
   * Constructor for NewTapAppWizardPage1
   */
  public NewTapAppWizardPage(IWorkspaceRoot root) {
    super(MessageUtil.getString("NewTapAppWizardPage.title"));
    

    this.setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("application32.gif")));
    this.setDescription(MessageUtil.getString("NewTapAppWizardPage.description"));

    IDialogFieldChangedListener listener = new FieldEventsAdapter();

    fContainerDialogField = new ContainerDialogField(CONTAINER, root, APP_WIDTH);
    connect(fContainerDialogField);
    fContainerDialogField.addListener(listener);

    fPackageDialogField = new PackageDialogField(PACKAGE, APP_WIDTH);
    connect(fPackageDialogField);
    fPackageDialogField.addListener(listener);

    fEngineDialogField = new EngineClassDialog(ENGINE, APP_WIDTH);
    connect(fEngineDialogField);
    fEngineDialogField.addListener(listener);

    fApplicationNameDialog = new ApplicationNameField(APPNAME);
    connect(fApplicationNameDialog);
    fApplicationNameDialog.addListener(listener);

    fGenerateServletClass = new CheckBoxField("dummy");
    fGenerateServletClass.addListener(listener);

    fServletPackageDialog = new PackageDialogField(SERVLETPACKAGE, SERV_WIDTH);
    connect(fServletPackageDialog);
    fServletPackageDialog.addListener(listener);

    fServletClassDialog = new ServletClassnameField(SERVLETCLASS, SERV_WIDTH);
    connect(fServletClassDialog);
    fServletClassDialog.addListener(listener);

    fServletSuperclassDialog = new ApplicationServletClassDialog(SERVLETSUPERCLASS, SERV_WIDTH);
    connect(fServletSuperclassDialog);
    fServletSuperclassDialog.addListener(listener);

    fGenerateServletClass.attachDialogFields(
      new DialogField[] { fServletPackageDialog, fServletClassDialog, fServletSuperclassDialog });
      
    

  }

  /**
   * Should be called from the wizard with the input element. 
   */
  public void init(IJavaElement jelem) {
    
    IRunnableContext context = (IRunnableContext)getWizard().getContainer();
   
    fContainerDialogField.init(jelem, getWizard().getContainer());
    fPackageDialogField.init(fContainerDialogField, context);
    fEngineDialogField.init(fPackageDialogField, context);
    IPackageFragment pack = null;
    if (jelem != null) {
      pack = (IPackageFragment) Utils.findElementOfKind(jelem, IJavaElement.PACKAGE_FRAGMENT);
    }
    fPackageDialogField.setPackageFragment(pack);
    fApplicationNameDialog.setTextValue("");
    fApplicationNameDialog.init(fPackageDialogField);
    fGenerateServletClass.setLabelText(MessageUtil.getString(PAGE_NAME + ".generateservlet"));
    fGenerateServletClass.setCheckBoxValue(false);
    fServletPackageDialog.init(fContainerDialogField, getWizard().getContainer());
    fServletPackageDialog.setPackageFragment(pack);
    fServletClassDialog.init(fServletPackageDialog);
    fServletClassDialog.setTextValue(fApplicationNameDialog.getTextValue() + "Servlet");
    fServletSuperclassDialog.init(fServletPackageDialog, context);
    
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
    formData.top = new FormAttachment(0,0);
    formData.left = new FormAttachment(0,0);
    formData.width = 400;
    composite.setLayoutData(formData);

    Control appNameControl = fApplicationNameDialog.getControl(composite);
    Control containerControl = fContainerDialogField.getControl(composite);
    Control packageControl = fPackageDialogField.getControl(composite);
    Control engineControl = fEngineDialogField.getControl(composite);
    
    Control servletControl = fGenerateServletClass.getControl(composite);
    Control servletPackageControl = fServletPackageDialog.getControl(composite);
    Control servletClassControl = fServletClassDialog.getControl(composite);
    Control servletSuperclassControl = fServletSuperclassDialog.getControl(composite);    

	addControl(appNameControl, composite, 10);
    Control separator = createSeparator(composite, appNameControl);
    addControl(containerControl, separator, 4);
    addControl(packageControl, containerControl, 4);
    addControl(engineControl, packageControl, 4);
    separator = createSeparator(composite, engineControl);
    addControl(servletControl, separator, 4);
    addControl(servletPackageControl, servletControl, 4);
    addControl(servletClassControl, servletPackageControl, 4);
    addControl(servletSuperclassControl, servletClassControl, 4);       

    setControl(composite);
    setFocus();
   
  }



  /**
   * @see NewElementWizardPage#getRunnable()
   */
  public IRunnableWithProgress getRunnable() {
    return new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          if (monitor == null) {
            monitor = new NullProgressMonitor();
          }
          createApplicationResource(new SubProgressMonitor(monitor, 1));
          createServletType(new SubProgressMonitor(monitor, 1));
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

  public void createApplicationResource(IProgressMonitor monitor) throws InterruptedException, CoreException {

    IPackageFragmentRoot root = fContainerDialogField.getPackageFragmentRoot();
    IPackageFragment pack = fPackageDialogField.getPackageFragment();
    String appname = fApplicationNameDialog.getTextValue();
    IType engineClass = fEngineDialogField.getType();
    application = ApplicationFactory.createApplication(root, pack, appname, engineClass, monitor);
  }

  public void createServletType(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, CoreException {

    if (fGenerateServletClass.getCheckBoxValue()) {
      IPackageFragmentRoot root = fContainerDialogField.getPackageFragmentRoot();
      IPackageFragment pack = fServletPackageDialog.getPackageFragment();
      String servletClass = fServletClassDialog.getTextValue();
      IType servletSuperclass = fServletSuperclassDialog.getType();
      generatedServletType = ApplicationClassFactory.createClass(root, pack, servletClass, servletSuperclass, null, monitor);
      populateMethodSource(generatedServletType, monitor);
    }
  }

  protected void populateMethodSource(IType servletType, IProgressMonitor monitor) throws JavaModelException {
    String methodName = MessageUtil.getString("Tapestry.servlet.getAppMethodName");
    ICompilationUnit parentCU = servletType.getCompilationUnit();
    String gensource = parentCU.getSource();

    DOMFactory domFactory = new DOMFactory();
    IDOMCompilationUnit unit = domFactory.createCompilationUnit(gensource, servletType.getElementName());
    IDOMNode current = unit.getFirstChild();
    while (current.getNodeType() != IDOMNode.TYPE) {
      current = current.getNextNode();
    }
    IDOMNode theType = current;
    IDOMMethod method = findMethod(theType, methodName);
    if (method == null) {
      method = domFactory.createMethod("protected String getApplicationSpecificationPath() {}");
      theType.addChild(method);
    }
    method.setBody(getAppMethodBody());

    String newContents = Utils.codeFormat(unit.getContents(), 0, StubUtility.getLineDelimiterUsed(parentCU));
    parentCU.getBuffer().setContents(newContents);
    parentCU.save(monitor, true);
  }

  private IDOMMethod findMethod(IDOMNode type, String desiredMethod) {
    IDOMNode current = type.getFirstChild();
    if (current != null) {
      while (true) {
        if (current.getNodeType() == IDOMNode.METHOD && desiredMethod.equals(current.getName())) {
          return (IDOMMethod) current;
        }
        current = current.getNextNode();
      }
    }
    return null;
  }

  protected String getAppMethodBody() {
    String useName = fPackageDialogField.getPackageFragment().getElementName() + "." + fApplicationNameDialog.getTextValue();

    return " { return \"/" + useName.replace('.', '/') + ".application\";}";
  }

  public IType getGeneratedServletType() {
    return generatedServletType;
  }

  public IFile getApplication() {
    return application;
  }

  protected void setFocus() {
    fApplicationNameDialog.setFocus(); 
  }

  private void checkEnabled(IStatus status) {
    boolean flag = status.isOK();
    fContainerDialogField.setEnabled(flag);
    fPackageDialogField.setEnabled(flag);
    fEngineDialogField.setEnabled(flag);
    fGenerateServletClass.setEnabled(flag);
    boolean gen = fGenerateServletClass.getCheckBoxValue();
    if (gen) {
      fServletPackageDialog.setEnabled(flag && gen);
      fServletClassDialog.setEnabled(flag && gen);
      fServletSuperclassDialog.setEnabled(flag && gen);
    }
  }

  
  public void updateStatus() {
    super.updateStatus();
    checkEnabled(fApplicationNameDialog.getStatus());
  }
  
 

  private class FieldEventsAdapter implements IDialogFieldChangedListener {

   

    public void dialogFieldChanged(DialogField field) {
      if (field == fApplicationNameDialog) {
        String newValue = fApplicationNameDialog.getTextValue();
        if (newValue != null && !"".equals(newValue)) {
          fServletClassDialog.setTextValue(newValue + "Servlet");
        }
      }   
      if (field == fPackageDialogField) {    
          String newValue = fPackageDialogField.getTextValue();    
          fServletPackageDialog.setTextValue(newValue == null ? "" : newValue);
      }
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