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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.ui.ChooseWorkspaceModelWidget;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class ChooseExistingPage extends WizardPage implements ISelectionChangedListener {

  private ChooseWorkspaceModelWidget chooseWidget;

  /**
   * Constructor for ChooseExistingPage.
   * @param name
   */
  public ChooseExistingPage(String name, IStructuredSelection selection) {
    super(name);

    this.setImageDescriptor(
      ImageDescriptor.createFromURL(TapestryImages.getImageURL("applicationDialog.gif")));
    this.setDescription("Choose an Application or Library for this project");

    IJavaProject jproject = (IJavaProject) selection.getFirstElement();

    chooseWidget =
      new ChooseWorkspaceModelWidget(
        jproject,
        TapestryLookup.ACCEPT_APPLICATIONS
          | TapestryLookup.ACCEPT_LIBRARIES
          | TapestryLookup.WRITEABLE
          | TapestryLookup.THIS_PROJECT_ONLY);

    chooseWidget.addSelectionChangedListener(this);
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);

    FillLayout layout = new FillLayout();
    composite.setLayout(layout);

    chooseWidget.createControl(composite);

    setControl(composite);
    
    setPageComplete(!chooseWidget.getSelection().isEmpty());

  }

  /**
   * @see com.iw.plugins.spindle.wizards.TapestryWizardPage#getResource()
   */
  public IResource getResource() {
    return (IResource) chooseWidget.getResultStorage();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {

    setPageComplete(!event.getSelection().isEmpty());

  }

}
