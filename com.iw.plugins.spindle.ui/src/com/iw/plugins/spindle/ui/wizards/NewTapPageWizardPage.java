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
 * The Initial Developer of the Original Code is Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2002 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@intelligentworks.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.wizards;

import org.apache.tapestry.INamespace;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IType;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.ui.wizards.factories.PageFactory;

public class NewTapPageWizardPage extends NewTapComponentWizardPage
{

  public NewTapPageWizardPage(IWorkspaceRoot root, String pageName)
  {
    super(root, pageName);
  }

  protected void createComponentResource(
      IProgressMonitor monitor,
      final IType specClass,
      IFolder libLocation) throws CoreException, InterruptedException
  {
    INamespace useNamespace = fNamespaceDialogField.getSelectedNamespace();
    IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) useNamespace
        .getSpecificationLocation();

    if (libLocation != null)
    {
      fComponentFile = PageFactory.createPage(libLocation, fComponentNameDialogField
          .getTextValue(), specClass, new SubProgressMonitor(monitor, 1));
    } else
    {
      fComponentFile = PageFactory.createPage(location, fComponentNameDialogField
          .getTextValue(), specClass, new SubProgressMonitor(monitor, 1));
    }
  }

}