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
public class NewTapPageWizardClassPage extends NewTapComponentWizardClassPage {

  public NewTapPageWizardClassPage(
    IWorkspaceRoot root,
    String pageName,
    NewTapComponentWizardPage predecessor) {
    	
    super(root, pageName, predecessor);

  }

}