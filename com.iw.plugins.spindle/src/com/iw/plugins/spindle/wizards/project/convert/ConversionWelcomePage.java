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
package com.iw.plugins.spindle.wizards.project.convert;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.update.ui.forms.internal.engine.FormEngine;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.ui.dialogfields.RadioDialogField;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;
import com.iw.plugins.spindle.wizards.TapestryWizardPage;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class ConversionWelcomePage
  extends TapestryWizardPage
  implements IDialogFieldChangedListener {

  IStructuredSelection selection;

  private RadioDialogField applicationOrLibrary;

  private boolean hasExistingApplicationsOrLibraries = false;
  private int state = ConvertToTapestryProjectWizard.CREATE_APPLICATION;

  private String welcomeMessage;

  private int count = 0;

  private IStorage[] foundModelStorages = new IStorage[0];
  ;
  /**
   * Constructor for ConversionWelcomePage.
   * @param name
   */
  public ConversionWelcomePage(String name, IStructuredSelection selection) {
    super(name);

    this.setImageDescriptor(
      ImageDescriptor.createFromURL(TapestryImages.getImageURL("application32.gif")));
    this.setDescription("Conversion Options");

    applicationOrLibrary =
      new RadioDialogField(
        "Create:",
        80,
        new String[] { "A new Application", "A new Library" },
        SWT.VERTICAL);

    applicationOrLibrary.addListener(this);

	this.selection = selection;
    setup();
  }

  private void setup() {

    state = -1;
    int count = 0;

    try {

      IJavaProject jproject = (IJavaProject) selection.getFirstElement();

      TapestryLookup lookup = new TapestryLookup();

      lookup.configure(jproject);

      TapestryLookup.StorageOnlyRequest request = TapestryLookup.createRequest();

      lookup.findAll(
        "*",
        true,
        TapestryLookup.ACCEPT_APPLICATIONS
          | TapestryLookup.ACCEPT_LIBRARIES
          | TapestryLookup.WRITEABLE
          | TapestryLookup.THIS_PROJECT_ONLY,
        request);

      foundModelStorages = request.getResults();

      count = foundModelStorages.length;

    } catch (JavaModelException e) {
      e.printStackTrace();
    }

    if (count == 1) {

      state = ConvertToTapestryProjectWizard.ONE_EXISTS;

    } else if (count > 1) {

      state = ConvertToTapestryProjectWizard.CHOOSE_EXISTING;

    }

    welcomeMessage = getWelcomeMessage();

  }
  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
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

    FormEngine notes = new FormEngine(composite, SWT.NONE);

    formData = new FormData();
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    formData.top = new FormAttachment(composite, 4);
    formData.height = 200;
    formData.width = 350;

    notes.setLayoutData(formData);

    notes.load(getWelcomeMessage(), true, false);

    if (state == -1) {

      Control radioControl = applicationOrLibrary.getControl(composite);

      formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(notes, 4);

      radioControl.setLayoutData(formData);

    }

    setControl(composite);

  }

  /**
   * Method getWelcomeMessage.
   * @return String
   */
  private String getWelcomeMessage() {
    String resource = "";
    if (state == ConvertToTapestryProjectWizard.ONE_EXISTS) {

      resource = resource + "welcomeFoundOne.xml";

    } else if (state == ConvertToTapestryProjectWizard.CHOOSE_EXISTING) {

      resource = resource + "welcomeFoundMany.xml";

    } else {

      resource = resource + "welcomeFoundNone.xml";

    }

    byte[] resultBytes = null;
    try {

      resultBytes = Utils.getInputStreamAsByteArray(getClass().getResourceAsStream(resource), -1);

    } catch (IOException e) {
    }

    if (resultBytes == null) {

      return "<form><p><b>Error Occured</b></p></form>";

    }

    String message = new String(resultBytes);

    if (state == ConvertToTapestryProjectWizard.ONE_EXISTS) {

      message =
        replace("$INSERT_TAPESTRY_MODEL$", message, foundModelStorages[0].getFullPath().toString());

    }
    return message;
  }

  private String replace(String template, String message, String replaceWith) {
    return message;
  }

  private String replace(String string, String message, IStorage iStorage) {
    return null;
  }

  /**
   * Method getRunnable.
   * @return IRunnableWithProgress
   */
  public IRunnableWithProgress getRunnable(Object object) {
    return null;
  }

  /**
   * Method getProjectResource.
   * @return IFile
   */
  public IFile getProjectResource() {
    return null;
  }

  /**
   * Returns the state.
   * @return int
   */
  public int getState() {
    return state;
  }

  /**
   * @see com.iw.plugins.spindle.wizards.TapestryWizardPage#getResource()
   */
  public IResource getResource() {
    return null;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
   */
  public boolean isPageComplete() {

    if (state == -1) {

      return applicationOrLibrary.getSelectedIndex() >= 0;

    }
    return true;
  }

  /**
   * @see com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field) {
  }

  /**
   * @see com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener#dialogFieldChanged(DialogField)
   */
  public void dialogFieldChanged(DialogField field) {

    SpindleStatus status = new SpindleStatus();

    int index = applicationOrLibrary.getSelectedIndex();

    switch (index) {

      case 0 :
        state = ConvertToTapestryProjectWizard.CREATE_APPLICATION;
        break;
      case 1 :
        state = ConvertToTapestryProjectWizard.CREATE_LIBRARY;
        break;
      default :
        status.setError("");

    }

    updateStatus(status);
  }

  /**
   * @see com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
   */
  public void dialogFieldStatusChanged(IStatus status, DialogField field) {
  }

}
