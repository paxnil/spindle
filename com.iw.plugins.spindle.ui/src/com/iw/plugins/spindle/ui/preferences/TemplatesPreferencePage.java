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
 **/
package com.iw.plugins.spindle.ui.preferences;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.assist.usertemplates.UserTemplateAccess;

/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class TemplatesPreferencePage extends TemplatePreferencePage implements IWorkbenchPreferencePage {
	
	public TemplatesPreferencePage() {
		setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
		setTemplateStore(UserTemplateAccess.getDefault().getTemplateStore());
		setContextTypeRegistry(UserTemplateAccess.getDefault().getContextTypeRegistry());
	}

	protected boolean isShowFormatterSetting() {
		return false;
	}
	
	
	public boolean performOk() {
		boolean ok= super.performOk();
		
		UIPlugin.getDefault().savePluginPreferences();
		
		return ok;
	}
}