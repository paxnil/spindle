/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.wizards;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

import com.iw.plugins.spindle.UIPlugin;

public class NewTapPageWizard extends NewTapComponentWizard
{

  public NewTapPageWizard()
  {
    super();

  }

  /**
   * @see Wizard#createPages
   */
  public void addPages()
  {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    if (workspace == null)
    {
      throw new IllegalArgumentException();
    }
    fPage1 = new NewTapPageWizardPage(workspace.getRoot(), "FirstPageWizardPage");
    addPage(fPage1);
    fPage2 = new TypeChooseWizardPage("SecondPageWizardPage", fPage1);
    addPage(fPage2);
  }

  public void createPageControls(Composite pageContainer)
  {
    super.createPageControls(pageContainer);
    setWindowTitle(UIPlugin.getString("NewTapPageWizard.windowtitle"));
  }

}