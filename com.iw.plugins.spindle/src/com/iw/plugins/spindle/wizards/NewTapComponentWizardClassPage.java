package com.iw.plugins.spindle.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
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
import com.iw.plugins.spindle.wizards.factories.ClassFactory;
import com.iw.plugins.spindle.ui.dialogfields.CheckBoxField;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.wizards.fields.ContainerDialogField;
import com.iw.plugins.spindle.wizards.fields.InterfaceChooser;
import com.iw.plugins.spindle.wizards.fields.JavaClassnameField;
import com.iw.plugins.spindle.wizards.fields.PackageDialogField;
import com.iw.plugins.spindle.wizards.fields.RawTypeDialogField;
import com.iw.plugins.spindle.wizards.fields.SuperClassDialogField;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class NewTapComponentWizardClassPage extends TapestryWizardPage {

  String PAGE_NAME;

  private static int LABEL_WIDTH = 64;

  String CHOOSECLASS;
  String SPEC_CLASS;
  String NEWCLASS;

  String CONTAINER;
  String PACKAGE;
  String CLASS;
  String SUPER;
  String IFACE;

  private CheckBoxField fChooseClass;
  private RawTypeDialogField fChooseSpecClassDialogField;
  private CheckBoxField fNewClass;

  private ContainerDialogField fChooseContainer;
  private PackageDialogField fPackageDialogField;
  private JavaClassnameField fSpecClassDialogField;
  private SuperClassDialogField fSuperClassDialogField;
  private InterfaceChooser fIFaceChooser;

  private DialogField componentNameField;
  private DialogField componentContainerField;
  private DialogField componentPackageField;

  private String fullyQualifiedBaseType;
  private String defaultSpecClass;

  private NewTapComponentWizardPage predecessor;

  private IType finalSpecClass;

  IDialogFieldChangedListener listener = new FieldEventsAdapter();

  /**
   * Constructor for NewTapComponetClassWizardPage.
   * @param name
   */
  public NewTapComponentWizardClassPage(
    IWorkspaceRoot root,
    String pageName,
    NewTapComponentWizardPage predecessor) {
    super(MessageUtil.getString(pageName + ".title"));

    PAGE_NAME = pageName;
    String CHOOSECLASS = PAGE_NAME + ".chooseclass";
    String SPEC_CLASS = PAGE_NAME + ".specclass";
    String NEWCLASS = PAGE_NAME + ".newclass";

    String CONTAINER = PAGE_NAME + ".container";
    String PACKAGE = PAGE_NAME + ".package";
    String CLASS = PAGE_NAME + ".class";
    String SUPER = PAGE_NAME + ".super";
    String IFACE = PAGE_NAME + ".iface";

    this.fullyQualifiedBaseType = MessageUtil.getString(PAGE_NAME+".baseType");
    this.defaultSpecClass = MessageUtil.getString(PAGE_NAME+".defaultSpecClass");

    this.predecessor = predecessor;

    this.setImageDescriptor(ImageDescriptor.createFromURL(TapestryImages.getImageURL("component32.gif")));

    this.setDescription(MessageUtil.getString(PAGE_NAME + ".description"));

    fChooseClass = new CheckBoxField(MessageUtil.getString(CHOOSECLASS));
    fChooseClass.addListener(listener);

    fChooseSpecClassDialogField = createChooseSpecClassField(SPEC_CLASS);
    connect(fChooseSpecClassDialogField);
    fChooseSpecClassDialogField.addListener(listener);

    fNewClass = new CheckBoxField(MessageUtil.getString(NEWCLASS));
    fNewClass.addListener(listener);

    fChooseContainer = new ContainerDialogField(CONTAINER, root, LABEL_WIDTH);
    connect(fChooseContainer);
    fChooseContainer.addListener(listener);

    fPackageDialogField = new PackageDialogField(PACKAGE, LABEL_WIDTH);
    connect(fPackageDialogField);
    fPackageDialogField.addListener(listener);

    fSpecClassDialogField = new JavaClassnameField(CLASS, LABEL_WIDTH);
    connect(fSpecClassDialogField);
    fSpecClassDialogField.addListener(listener);

    fSuperClassDialogField = new SuperClassDialogField(SUPER, LABEL_WIDTH);
    connect(fSuperClassDialogField);
    fSuperClassDialogField.addListener(listener);

    fIFaceChooser = new InterfaceChooser(IFACE, LABEL_WIDTH);
    connect(fIFaceChooser);
    fIFaceChooser.addListener(listener);

  }

  public void init(IJavaElement jelem) {

    IRunnableContext context = (IRunnableContext) getWizard().getContainer();

    IJavaProject jproject = jelem.getJavaProject();
    fChooseSpecClassDialogField.init(jproject, context);
    fChooseSpecClassDialogField.setTextValue(defaultSpecClass);

    fChooseContainer.init(jelem, context);
    fPackageDialogField.init(fChooseContainer, context);
    fSpecClassDialogField.init(fPackageDialogField);
    fSuperClassDialogField.init(fPackageDialogField, context);
    IPackageFragment pack = null;
    if (jelem != null) {
      pack = (IPackageFragment) Utils.findElementOfKind(jelem, IJavaElement.PACKAGE_FRAGMENT);
    }
    fPackageDialogField.setPackageFragment(pack);

    fIFaceChooser.init(fPackageDialogField, context);

    fChooseClass.setCheckBoxValue(true);
    fChooseClass.setCheckBoxValue(false);

    fSuperClassDialogField.setTextValue(defaultSpecClass);

    componentNameField = predecessor.getComponentNameField();
    componentNameField.addListener(listener);

    componentContainerField = predecessor.getComponentContainerField();
    componentContainerField.addListener(listener);

    componentPackageField = predecessor.getComponentPackageField();
    componentPackageField.addListener(listener);

    updateStatus();

  }

  private void checkEnabled() {
    boolean chooseFlag = fChooseClass.getCheckBoxValue();
    boolean newFlag = fNewClass.getCheckBoxValue();
    if (chooseFlag == false && newFlag == false) {
      fChooseClass.setCheckBoxValue(true);
      return;
    }
    fChooseSpecClassDialogField.setEnabled(chooseFlag);
    fChooseContainer.setEnabled(newFlag);
    fPackageDialogField.setEnabled(newFlag);
    fSpecClassDialogField.setEnabled(newFlag);
    fSuperClassDialogField.setEnabled(newFlag);
    fIFaceChooser.setEnabled(newFlag);
  }

  /**
   * @see IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    FormLayout layout = new FormLayout();
    layout.marginWidth = 4;
    layout.marginHeight = 4;
    composite.setLayout(layout);

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.width = 400;
    composite.setLayoutData(formData);

    Control chooseClassRadioControl = fChooseClass.getControl(composite);
    Control newClassRadioControl = fNewClass.getControl(composite);
    Control chooseSpecClassControl = fChooseSpecClassDialogField.getControl(composite);

    addControl(chooseClassRadioControl, composite, 10);

    addControl(chooseSpecClassControl, chooseClassRadioControl, 4);
    Control separator = createSeparator(composite, chooseSpecClassControl);

    addControl(newClassRadioControl, separator, 4);

    Control containerControl = fChooseContainer.getControl(composite);
    Control packageControl = fPackageDialogField.getControl(composite);
    Control specclassControl = fSpecClassDialogField.getControl(composite);
    Control superclassControl = fSuperClassDialogField.getControl(composite);
    Control interfaceControl = fIFaceChooser.getControl(composite);

    addControl(containerControl, newClassRadioControl, 4);
    addControl(packageControl, containerControl, 4);
    addControl(specclassControl, packageControl, 4);
    addControl(superclassControl, specclassControl, 4);

    formData = new FormData();
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    formData.top = new FormAttachment(superclassControl, 4);
    formData.bottom = new FormAttachment(100, -4);
    interfaceControl.setLayoutData(formData);

    setControl(composite);

    chooseClassRadioControl.setFocus();
  }

  protected RawTypeDialogField createChooseSpecClassField(String label) {
    // 	  unfortunately, this feature (hierarchy root stuff) does not seem to work in Wizards
    return new RawTypeDialogField(label, fullyQualifiedBaseType, LABEL_WIDTH);
    //return new SpecClassDialogField(SPEC_CLASS);
  }

  private void toggle(DialogField field) {
    boolean flag;
    if (field == fChooseClass) {
      flag = fChooseClass.getCheckBoxValue();
      fNewClass.setCheckBoxValue(!flag);

    } else {
      flag = fNewClass.getCheckBoxValue();
      fChooseClass.setCheckBoxValue(!flag);
    }
    checkEnabled();
  }

  /**
   * @see NewTapestryElementWizardPage#updateStatus()
   */
  protected void updateStatus() {
    super.updateStatus();
    checkEnabled();
  }

  public boolean performFinish() {
    return true;
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
          createSpecClassResource(new SubProgressMonitor(monitor, 1));
          monitor.done();
        } catch (CoreException e) {
          throw new InvocationTargetException(e);
        }
      }
    };
  }

  protected void createSpecClassResource(IProgressMonitor monitor) throws InterruptedException, CoreException {
    if (fChooseClass.getCheckBoxValue()) {
      finalSpecClass = fChooseSpecClassDialogField.getType();
    } else {
      ClassFactory factory = new ClassFactory();
      finalSpecClass =
        factory.createClass(
          fChooseContainer.getPackageFragmentRoot(),
          fPackageDialogField.getPackageFragment(),
          fSpecClassDialogField.getTextValue(),
          fSuperClassDialogField.getType(),
          fIFaceChooser.getInterfaces(),
          null,
          monitor);
    }
  }

  /**
   * Gets the finalSpecClass.
   * @return Returns a IType
   */
  public IType getFinalSpecClass() {
    return finalSpecClass;
  }

  private class FieldEventsAdapter implements IDialogFieldChangedListener {

    public void dialogFieldChanged(DialogField field) {
      if (field == fChooseClass || field == fNewClass) {
        toggle(field);
      }
      if (field == componentNameField) {
        fSpecClassDialogField.setTextValue(predecessor.getChosenComponentName());
      }

      if (field == componentContainerField) {
        fChooseContainer.setTextValue(predecessor.getChosenContainer());
      }
      if (field == componentPackageField) {
        fPackageDialogField.setTextValue(predecessor.getChoosenPackage());
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