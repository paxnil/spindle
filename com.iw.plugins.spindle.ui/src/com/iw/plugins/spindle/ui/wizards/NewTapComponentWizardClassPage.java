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

package com.iw.plugins.spindle.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.util.CoreUtils;
import com.iw.plugins.spindle.ui.dialogfields.CheckBoxField;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.ui.wizards.factories.ClassFactory;
import com.iw.plugins.spindle.ui.wizards.fields.ContainerDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.InterfaceChooser;
import com.iw.plugins.spindle.ui.wizards.fields.JavaClassnameField;
import com.iw.plugins.spindle.ui.wizards.fields.PackageDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.RawTypeDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.SuperClassDialogField;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class NewTapComponentWizardClassPage extends TapestryWizardPage
{

    class RadioField extends CheckBoxField
    {

        public RadioField(String label)
        {
            super(label);
        }

        public Button getCheckBoxControl(Composite parent, int modifier)
        {
            // TODO Auto-generated method stub
            return super.getCheckBoxControl(parent, SWT.RADIO);
        }
    }

    String PAGE_NAME;

    private static int LABEL_WIDTH = 64;

    private CheckBoxField fChooseClass;
    private RawTypeDialogField fChooseSpecClassDialogField;
    private CheckBoxField fNewClass;

    private ContainerDialogField fChooseContainer;
    private PackageDialogField fPackageDialogField;
    private JavaClassnameField fSpecClassDialogField;
    private CheckBoxField fMakeAbstractField;
    private SuperClassDialogField fSuperClassDialogField;
    private InterfaceChooser fIFaceChooser;

    private DialogField fComponentNameField;
    private DialogField fProjectField;

    private String fFullyQualifiedBaseType;
    private String fDefaultSpecClass;

    private NewTapComponentWizardPage fFirstWizardPage;

    private IType finalSpecClass;
    private IFile fGeneratedJavaFile = null;

    IDialogFieldChangedListener listener = new FieldEventsAdapter();

    /**
     * Constructor for NewTapComponetClassWizardPage.
     * @param name
     */
    public NewTapComponentWizardClassPage(IWorkspaceRoot root, String pageName, NewTapComponentWizardPage predecessor)
    {
        super(UIPlugin.getString(pageName + ".title"));

        PAGE_NAME = pageName;
        String CHOOSECLASS = PAGE_NAME + ".chooseclass";
        String SPEC_CLASS = PAGE_NAME + ".specclass";
        String NEWCLASS = PAGE_NAME + ".newclass";

        String CONTAINER = PAGE_NAME + ".container";
        String PACKAGE = PAGE_NAME + ".package";
        String CLASS = PAGE_NAME + ".class";
        String SUPER = PAGE_NAME + ".super";
        String IFACE = PAGE_NAME + ".iface";
        String MAKE_ABSTRACT = PAGE_NAME + ".makeabstract";

        this.fFullyQualifiedBaseType = UIPlugin.getString(PAGE_NAME + ".baseType");
        this.fDefaultSpecClass = UIPlugin.getString(PAGE_NAME + ".defaultSpecClass");

        this.fFirstWizardPage = predecessor;

        this.setImageDescriptor(ImageDescriptor.createFromURL(Images.getImageURL("componentDialog.gif")));

        this.setDescription(UIPlugin.getString(PAGE_NAME + ".description"));

        fChooseClass = new RadioField(UIPlugin.getString(CHOOSECLASS));
        fChooseClass.addListener(listener);

        fChooseSpecClassDialogField = createChooseSpecClassField(SPEC_CLASS);
        connect(fChooseSpecClassDialogField);
        fChooseSpecClassDialogField.addListener(listener);

        fNewClass = new RadioField(UIPlugin.getString(NEWCLASS));
        fNewClass.addListener(listener);

        fChooseContainer = new ContainerDialogField(CONTAINER, root, LABEL_WIDTH);
        connect(fChooseContainer);
        fChooseContainer.addListener(listener);

        fPackageDialogField = new PackageDialogField(PACKAGE, LABEL_WIDTH);
        fPackageDialogField.setAcceptSourcePackagesOnly(true);
        connect(fPackageDialogField);
        fPackageDialogField.addListener(listener);

        fMakeAbstractField = new CheckBoxField(UIPlugin.getString(MAKE_ABSTRACT + ".label"), LABEL_WIDTH, true);

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

    public void init(IJavaElement jelem)
    {

        IRunnableContext context = (IRunnableContext) getWizard().getContainer();

        fChooseSpecClassDialogField.init(jelem == null ? null : jelem.getJavaProject(), context);
        fChooseSpecClassDialogField.setTextValue(fDefaultSpecClass);

        fChooseContainer.init(jelem, context);
        fPackageDialogField.init(fChooseContainer, context);
        fSpecClassDialogField.init(fPackageDialogField);
        fSuperClassDialogField.init(fPackageDialogField, context);
        IPackageFragment pack = null;
        if (jelem != null)
        {
            pack = (IPackageFragment) CoreUtils.findElementOfKind(jelem, IJavaElement.PACKAGE_FRAGMENT);
        }
        fPackageDialogField.setPackageFragment(pack);

        fIFaceChooser.init(fPackageDialogField, context);

        fChooseClass.setCheckBoxValue(true);
        fChooseClass.setCheckBoxValue(false);

        fSuperClassDialogField.setTextValue(fDefaultSpecClass);

        fComponentNameField = fFirstWizardPage.getComponentNameField();
        fComponentNameField.addListener(listener);

        fProjectField = fFirstWizardPage.getProjectField();
        fProjectField.addListener(listener);

        updateStatus();

    }

    private void checkEnabled()
    {
        boolean chooseFlag = fChooseClass.getCheckBoxValue();
        boolean newFlag = fNewClass.getCheckBoxValue();
        if (chooseFlag == false && newFlag == false)
        {
            fChooseClass.setCheckBoxValue(true);
            return;
        }
        fChooseSpecClassDialogField.setEnabled(chooseFlag);
        fChooseContainer.setEnabled(newFlag);
        fPackageDialogField.setEnabled(newFlag);
        fSpecClassDialogField.setEnabled(newFlag);
        fMakeAbstractField.setEnabled(newFlag);
        fSuperClassDialogField.setEnabled(newFlag);
        fIFaceChooser.setEnabled(newFlag);
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NO_RADIO_GROUP);

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
        Control makeabstractControl = fMakeAbstractField.getControl(composite);
        Control superclassControl = fSuperClassDialogField.getControl(composite);
        Control interfaceControl = fIFaceChooser.getControl(composite);

        addControl(containerControl, newClassRadioControl, 4);
        addControl(packageControl, containerControl, 4);
        addControl(specclassControl, packageControl, 4);
        addControl(makeabstractControl, specclassControl, 4);
        addControl(superclassControl, makeabstractControl, 4);

        formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(superclassControl, 4);
        formData.bottom = new FormAttachment(100, -4);
        interfaceControl.setLayoutData(formData);

        setControl(composite);

        chooseClassRadioControl.setFocus();
    }

    protected RawTypeDialogField createChooseSpecClassField(String label)
    {
        // 	  unfortunately, this feature (hierarchy root stuff) does not seem to work in Wizards
        return new RawTypeDialogField(label, fFullyQualifiedBaseType, LABEL_WIDTH);
        //return new SpecClassDialogField(SPEC_CLASS);
    }

    private void toggle(DialogField field)
    {
        boolean flag;
        if (field == fChooseClass)
        {
            flag = fChooseClass.getCheckBoxValue();
            fNewClass.setCheckBoxValue(!flag);

        } else
        {
            flag = fNewClass.getCheckBoxValue();
            fChooseClass.setCheckBoxValue(!flag);
        }
        checkEnabled();
    }

    /**
     * @see NewTapestryElementWizardPage#updateStatus()
     */
    protected void updateStatus()
    {
        super.updateStatus();
        checkEnabled();
    }

    public boolean performFinish()
    {
        return true;
    }

    /**
    * @see NewElementWizardPage#getRunnable()
    */
    public IRunnableWithProgress getRunnable(Object object)
    {
        return new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                try
                {
                    if (monitor == null)
                    {
                        monitor = new NullProgressMonitor();
                    }
                    createSpecClassResource(new SubProgressMonitor(monitor, 1));
                    monitor.done();
                } catch (CoreException e)
                {
                    throw new InvocationTargetException(e);
                }
            }
        };
    }

    protected void createSpecClassResource(IProgressMonitor monitor) throws InterruptedException, CoreException
    {
        if (fChooseClass.getCheckBoxValue())
        {
            finalSpecClass = fChooseSpecClassDialogField.getType();
        } else
        {
            ClassFactory factory = new ClassFactory();
            finalSpecClass =
                factory.createClass(
                    fChooseContainer.getPackageFragmentRoot(),
                    fPackageDialogField.getPackageFragment(),
                    fSpecClassDialogField.getTextValue(),
                    fSuperClassDialogField.getType(),
                    fIFaceChooser.getInterfaces(),
                    fMakeAbstractField.getCheckBoxValue(),
                    null,
                    monitor);
            fGeneratedJavaFile = factory.getGeneratedFile();
        }
    }

    public IFile getGeneratedJavaFile()
    {
        return fGeneratedJavaFile;
    }

    /**
     * Gets the finalSpecClass.
     * @return Returns a IType
     */
    public IType getFinalSpecClass()
    {
        return finalSpecClass;
    }

    public IResource getResource()
    {

        IResource result = null;

        try
        {
            result = finalSpecClass.getUnderlyingResource();

        } catch (JavaModelException e)
        {}

        return result;
    }

    private class FieldEventsAdapter implements IDialogFieldChangedListener
    {

        public void dialogFieldChanged(DialogField field)
        {
            if (field == fChooseClass || field == fNewClass)
            {
                toggle(field);
            }
            if (field == fComponentNameField)
            {
                fSpecClassDialogField.setTextValue(fFirstWizardPage.getChosenComponentName());
            }

            if (field == fProjectField)
            {
                try
                {
                    TapestryProject newProject = fFirstWizardPage.getChosenProject();
                    if (newProject == null)
                        return;

                    IJavaProject jproject = newProject.getJavaProject();

                    IRunnableContext context = (IRunnableContext) getWizard().getContainer();

                    fChooseSpecClassDialogField.init(jproject, context);
                    fChooseSpecClassDialogField.setTextValue(fDefaultSpecClass);
                    IPackageFragmentRoot old = fChooseContainer.getPackageFragmentRoot();
                    boolean guess = true;
                    if (old != null && jproject.equals(old.getJavaProject()))
                        guess = false;

                    if (guess)
                    {
                        IPackageFragmentRoot[] roots = jproject.getPackageFragmentRoots();
                        IPackageFragmentRoot useRoot = null;
                        for (int i = 0; i < roots.length; i++)
                        {
                            if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE && !roots[i].equals(jproject))
                            {
                                useRoot = roots[i];
                                break;
                            }
                        }
                        fChooseContainer.setPackageFragmentRoot(useRoot, true);
                    }

                } catch (CoreException e)
                {
                    UIPlugin.log(e);
                }
            }
            updateStatus();
        }
        /**
         * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
         */
        public void dialogFieldButtonPressed(DialogField field)
        {}

        /**
         * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
         */
        public void dialogFieldStatusChanged(IStatus status, DialogField field)
        {}

    }

}