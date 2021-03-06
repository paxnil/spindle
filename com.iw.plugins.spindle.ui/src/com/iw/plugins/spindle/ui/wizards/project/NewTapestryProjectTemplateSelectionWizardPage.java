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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.wizards.project;

import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author glongman@gmail.com
 *  
 */
public class NewTapestryProjectTemplateSelectionWizardPage extends
		BaseTemplateSelectionPage {

	public NewTapestryProjectTemplateSelectionWizardPage(String name) {
		super(name);

	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		NewTapestryProjectWizard wiz = (NewTapestryProjectWizard) getWizard();
		if (visible) {
			wiz.entering(this);
		} else {
			wiz.leaving(this);
		}
	}

    public void performHelp()
    {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp("com.iw.plugins.spindle.docs.projectwizard");
    }
    
    
}