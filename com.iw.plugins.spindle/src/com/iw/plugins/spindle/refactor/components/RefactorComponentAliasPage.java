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

package com.iw.plugins.spindle.refactor.components;

import java.util.List;

import net.sf.tapestry.parse.SpecificationParser;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.ui.dialogfields.CheckBoxField;
import com.iw.plugins.spindle.ui.dialogfields.StringField;
import com.iw.plugins.spindle.wizards.TapestryWizardPage;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class RefactorComponentAliasPage extends TapestryWizardPage {

  StringField componentName;
  CheckBoxField buttons;
  ITapestryProject project;
  String oldName;
  List existingComponentAliases;
  boolean showButtons = true;

  /**
   * Constructor for RefactorCompontentNamePage.
   * @param pageName
   */
  public RefactorComponentAliasPage(String pageName, String oldName, boolean showButtons, List existingComponentAliases) {
    super(pageName);
    setTitle("Change Component Alias");
    setDescription("Specify a new alias for the selected Component");
    this.oldName = oldName;
    this.showButtons = showButtons;
    this.existingComponentAliases = existingComponentAliases;
    existingComponentAliases.remove(oldName);
    
    
    this.setImageDescriptor(
      ImageDescriptor.createFromURL(TapestryImages.getImageURL("component32.gif")));
  }

  public String getNewName() {
    return componentName.getTextValue();
  }

  public boolean getIsDoingUpdate() {

    return buttons.getCheckBoxValue();

  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NONE);

    FormLayout layout = new FormLayout();
    layout.marginWidth = 4;
    layout.marginHeight = 4;
    container.setLayout(layout);

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.width = 400;
    container.setLayoutData(formData);

    componentName = new StringField("Alias:");
    Control cnameControl = componentName.getControl(container);

    Control buttonsControl = null;

    if (showButtons) {

      buttons = new CheckBoxField("update components using alias '" + oldName + "' as a component type", 200);

      buttonsControl = buttons.getControl(container);

      buttons.setCheckBoxValue(true);

    }

    formData = new FormData();
    formData.top = new FormAttachment(container, 0);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    cnameControl.setLayoutData(formData);

    Control separator = createSeparator(container, cnameControl);

    if (buttonsControl != null) {
      addControl(buttonsControl, separator);
    }

    Text cnameTextControl = (Text) componentName.getTextControl(null);
    cnameTextControl.setText(oldName);
    cnameTextControl.selectAll();

    cnameTextControl.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        checkPageComplete((Text) e.widget);
      }
    });

    setPageComplete(true);

    setControl(container);
  }

  //  /**
  //   * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
  //   */
  //  public void setVisible(boolean visible) {
  //    componentName.setFocus();
  //    checkPageComplete((Text) componentName.getTextControl(null)); 
  //  }

  /**
   * Method checkPageComplete.
   * @param text
   */
  private void checkPageComplete(Text text) {

    String currentValue = text.getText();

    setMessage(null);

    if (currentValue == null || "".equals(currentValue.trim())) {

      setButtonsEnabled(false);
      setPageComplete(false);
      return;

    }

    IStatus val = null;
    try {
      val =
        TapestryPlugin.getDefault().validate(
        currentValue,
          SpecificationParser.COMPONENT_ALIAS_PATTERN,
          
          "SpecificationParser.invalid-component-alias");
    } catch (CoreException e) {
    }

    if (val != null && !val.isOK()) {

      setMessage(val.getMessage(), ERROR);
      return;

    }

    if (existingComponentAliases.contains(currentValue.trim())) {

      setButtonsEnabled(false);

      setMessage("That alias is already in use", ERROR);

      setPageComplete(false);

      return;
    }

    ((RefactorComponentAliasWizard)getWizard()).newNameChanged(currentValue);
    setPageComplete(true);

  }

  private void setButtonsEnabled(boolean flag) {
    if (buttons != null) {
      buttons.setEnabled(flag);
    }
  }

  /**
   * @see com.iw.plugins.spindle.wizards.TapestryWizardPage#getResource()
   */
  public IResource getResource() {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.wizards.TapestryWizardPage#getRunnable(Object)
   */
  public IRunnableWithProgress getRunnable(Object object) {
    return null;
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
   */
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible) {
    	
    	checkPageComplete(componentName.getTextControl(null));
    	
    }
  }

}
