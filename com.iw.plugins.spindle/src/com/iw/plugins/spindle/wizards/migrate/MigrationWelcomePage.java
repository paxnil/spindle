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
package com.iw.plugins.spindle.wizards.migrate;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.ui.forms.internal.engine.FormEngine;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.ui.migrate.MigrationContext;
import com.iw.plugins.spindle.util.Utils;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class MigrationWelcomePage extends WizardPage {

  MigrationContext context;

  /**
   * Constructor for ConversionWelcomePage.
   * @param name
   */
  public MigrationWelcomePage(String name, MigrationContext context) {
    super(name);

    this.setImageDescriptor(
      ImageDescriptor.createFromURL(TapestryImages.getImageURL("applicationDialog.gif")));
    this.setDescription("Migrate a Tapestry Project");

    this.context = context;

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
    formData.height = 250;
    formData.width = 350;

    notes.setLayoutData(formData);

    notes.load(getWelcomeMessage(), true, false);

    setControl(composite);

  }

  /**
   * Method getWelcomeMessage.
   * @return String
   */
  private String getWelcomeMessage() {
    String resource = "migrationWelcome.xml";

    byte[] resultBytes = null;
    try {

      resultBytes = Utils.getInputStreamAsByteArray(getClass().getResourceAsStream(resource), -1);

    } catch (IOException e) {
    }

    if (resultBytes == null) {

      return "<form><p><b>Error Occured</b></p></form>";

    }

    return new String(resultBytes);
  }

  /**
   * Method getProjectResource.
   * @return IFile
   */
  public IFile getProjectResource() {
    return null;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
   */
  public boolean isPageComplete() {

    return true;
  }

}
