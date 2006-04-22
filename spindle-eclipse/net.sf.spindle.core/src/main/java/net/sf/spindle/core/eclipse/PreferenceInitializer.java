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
package net.sf.spindle.core.eclipse;

import net.sf.spindle.core.CoreStatus;
import net.sf.spindle.core.IPreferenceConstants;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Preference Initializer for the Spindle Core
 * 
 * @author glongman@gmail.com
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer implements
        IPreferenceConstants
{

    public PreferenceInitializer()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = TapestryCorePlugin.getDefault().getPreferenceStore();
        store.setDefault(CACHE_GRAMMAR_PREFERENCE, true);
        store.setDefault(BUILDER_MARKER_MISSES, CoreStatus.IGNORE.getLabel());
        store.setDefault(BUILDER_HANDLE_ASSETS, CoreStatus.WARN.getLabel());
        store.setDefault(NAMESPACE_CLASH_SEVERITY, CoreStatus.ERROR.getLabel());
        // store.addPropertyChangeListener((IPropertyChangeListener)
        // TapestryCorePlugin.getDefault());
    }

}