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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.widgets.Composite;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;

public class NewTapComponentWizard extends NewTapestryElementWizard {

  protected NewTapComponentWizardPage fPage1;
  protected NewTapComponentWizardClassPage fPage2;

  public NewTapComponentWizard() {
    super();
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
    fPage1 = new NewTapComponentWizardPage(workspace.getRoot(), "FirstComponentWizardPage");
    addPage(fPage1);
    fPage2 = new NewTapComponentWizardClassPage(workspace.getRoot(), "SecondComponentWizardPage", fPage1);
    addPage(fPage2);
  }

  /**
   * @see Wizard#performFinish()
   */
  public boolean performFinish() {
    if (finishPage(fPage1.getAutoAddRunnable())) {
      if (finishPage(fPage2.getRunnable(null))) {
        if (finishPage(fPage1.getRunnable(fPage2.getFinalSpecClass()))) {
          IFile file = (IFile)fPage1.getResource();
          try {
            selectAndReveal(file);
            openResource(file);
          } catch (Exception e) { // let pass, only reveal and open will fail
          }
        }
      }
    }

    fPage1.performFinish();
    fPage2.performFinish();
    return true;
  } 
  
  /**
   * @see IWizard#createPageControls(Composite)
   */
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
    setWindowTitle(MessageUtil.getString("NewTapComponentWizard.windowtitle"));
    IJavaElement initElem = getInitElement();
    fPage1.init(initElem, prepopulateName);
    fPage2.init(initElem);
  }

}