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


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.swt.widgets.Composite;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;


public class NewTapLibraryWizard extends NewTapestryElementWizard {

  
  private NewTapLibraryWizardPage fPage1;


  public NewTapLibraryWizard() {
    super();
    //setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);
    //setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
    setWindowTitle(MessageUtil.getString("NewTapLibWizard.windowtitle"));


  }


  /**
   * @see Wizard#createPages
   */
  public void addPages() {
    super.addPages();
    IWorkspace workspace = TapestryPlugin.getDefault().getWorkspace();
    if (workspace == null) {
      throw new IllegalArgumentException();
    }
    fPage1 = new NewTapLibraryWizardPage(workspace.getRoot());
    addPage(fPage1);
   
    //    fPage2 = new NewTapAppWizardPage2(KludgeName, workspace.getRoot());
    //    addPage(fPage2);
    //    fPage2.init(getSelection());
  }


  /**
   * @see Wizard#performFinish()
   */
  public boolean performFinish() {
    if (finishPage(fPage1.getRunnable())) {
      IFile file = fPage1.getLibrary(); 
      try {
        selectAndReveal(file);
        openResource(file);
      } catch (Exception e) {


        // let pass, only reveal and open will fail
      }
    }
    fPage1.performFinish();


    return true;


  }


  /**
   * @see IWizard#createPageControls(Composite)
   */
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
    fPage1.init(getInitElement());
  }

  

}