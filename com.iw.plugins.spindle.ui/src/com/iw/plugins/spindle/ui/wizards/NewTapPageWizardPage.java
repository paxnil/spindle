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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.templates.Template;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.editors.assist.usertemplates.XMLFileContextType;
import com.iw.plugins.spindle.ui.widgets.PreferenceTemplateSelector;
import com.iw.plugins.spindle.ui.wizards.factories.PageFactory;

public class NewTapPageWizardPage extends NewTapComponentWizardPage
{

  public NewTapPageWizardPage(IWorkspaceRoot root, String pageName)
  {
    super(root, pageName);
  }

  protected PreferenceTemplateSelector createComponentTemplateSelector()
  {
    return createTemplateSelector(
        XMLFileContextType.PAGE_FILE_CONTEXT_TYPE,
        PreferenceConstants.PAGE_TEMPLATE);
  }

  protected void createSpecificationResource(
      IProgressMonitor monitor,
      final IType specClass) throws CoreException, InterruptedException
  {
    PageFactory factory = new PageFactory();
    Template template = fComponentTemplateSelector.getSelectedTemplate();

    fComponentFile = factory.createPage((IFile) getResource(), template, specClass
        .getFullyQualifiedName(), monitor);
  }

}