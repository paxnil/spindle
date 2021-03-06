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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.ui.SectionWidget;
import com.iw.plugins.spindle.ui.migrate.MigrationContext;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class MigrationActionPage
  extends WizardPage
  implements ISelectionProvider, ISelectionChangedListener {

  MigrationContext context;
  MigrationActionChooserWidget chooser;

  boolean showUndefinedPage;
  /**
   * Constructor for ConversionWelcomePage.
   * @param name
   */
  public MigrationActionPage(String name, MigrationContext context) {
    super(name);

    this.setImageDescriptor(
      ImageDescriptor.createFromURL(TapestryImages.getImageURL("applicationDialog.gif")));
    this.setDescription("Include/Exclude migration actions");

    this.context = context;

  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));

    Section section = new Section("Deselect those Actions you want the Migrator to skip");

    Control sectionControl = section.createControl(composite);

    sectionControl.setLayoutData(new GridData(GridData.FILL_BOTH));

    chooser.addSelectionChangedListener(this);

    setControl(composite);

    setPageComplete(true);

  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {

    boolean complete = false;

    if (!event.getSelection().isEmpty()) {

      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
      Object[] elements = selection.toArray();
      for (int i = 0; i < elements.length; i++) {
        int actionId = ((Integer) elements[i]).intValue();
        if (actionId == MigrationContext.MIGRATE_DTD) {

          complete = true;
          break;
        }

      }

    }
    
    fireSelectionChanged();

    setPageComplete(complete);

  }

  public ISelection getSelection() {

    return chooser.getSelection();

  }

  class Section extends SectionWidget {

    /**
    * Constructor for Section.
    */
    public Section(String headerText) {
      super();
      setDescriptionPainted(false);
      setAddSeparator(false);
      setHeaderText(headerText);

    }

    /**
     * @see com.iw.plugins.spindle.wizards.migrate.SectionWidget#createClient(Composite)
     */
    public Composite createClient(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);

      GridLayout layout = new GridLayout();
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      composite.setLayout(layout);
      composite.setLayoutData(new GridData(GridData.FILL_BOTH));

      chooser = new MigrationActionChooserWidget(context, 380, -1, SWT.NONE);

      Control chooserControl = chooser.createControl(composite);

      chooserControl.setLayoutData(new GridData(GridData.FILL_BOTH));

      return composite;
    }

  }

  /**
   * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
   */
  public boolean canFlipToNextPage() {
    return isPageComplete() && showUndefinedPage;
  }

  List selectionChangeListeners = new ArrayList();

  private void fireSelectionChanged() {

    for (Iterator iter = selectionChangeListeners.iterator(); iter.hasNext();) {
    	
      ISelectionChangedListener element = (ISelectionChangedListener) iter.next();
      element.selectionChanged(new SelectionChangedEvent(this, getSelection()));
      
    }

  }

  public void addSelectionChangedListener(ISelectionChangedListener listener) {

    if (!selectionChangeListeners.contains(listener)) {
      selectionChangeListeners.add(listener);
    }

  }

  public void removeSelectionChangedListener(ISelectionChangedListener listener) {

    selectionChangeListeners.remove(listener);

  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(ISelection)
   */
  public void setSelection(ISelection selection) {
  }

 

  /**
   * Returns the showUndefinedPage.
   * @return boolean
   */
  public boolean getShowUndefinedPage() {
    return showUndefinedPage;
  }

  /**
   * Sets the showUndefinedPage.
   * @param showUndefinedPage The showUndefinedPage to set
   */
  public void setShowUndefinedPage(boolean showUndefinedPage) {
    this.showUndefinedPage = showUndefinedPage;
  }

}
