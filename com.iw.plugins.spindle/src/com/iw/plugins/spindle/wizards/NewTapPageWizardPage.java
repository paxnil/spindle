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

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

import com.iw.plugins.spindle.wizards.factories.PageFactory;



public class NewTapPageWizardPage extends NewTapComponentWizardPage {

  public NewTapPageWizardPage(IWorkspaceRoot root, String pageName) {
    super(root, pageName);
  }

  public void createComponentResource(IProgressMonitor monitor, IType specClass)
    throws InterruptedException, CoreException {

    IPackageFragmentRoot root = fContainerDialogField.getPackageFragmentRoot();
    IPackageFragment pack = fPackageDialogField.getPackageFragment();
    String compname = fComponentNameDialog.getTextValue();
    component = PageFactory.createPage(root, pack, compname, specClass, monitor);
  }

}