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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.ui.util;

import java.util.StringTokenizer;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 *  Access to features exposed by the JDT UI plugin
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JavaUIUtils
{
    public static int getImportNumberThreshold()
    {
        IPreferenceStore prefs = JavaPlugin.getDefault().getPreferenceStore();
        int threshold = prefs.getInt(PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD);
        if (threshold < 0)
        {
            threshold = Integer.MAX_VALUE;
        }
        return threshold;
    }

    public static String[] getImportOrderPreference()
    {
        IPreferenceStore prefs = JavaPlugin.getDefault().getPreferenceStore();
        String str = prefs.getString(PreferenceConstants.ORGIMPORTS_IMPORTORDER);
        if (str != null)
        {
            return unpackOrderList(str);
        }
        return new String[0];
    }

    private static String[] unpackOrderList(String str)
    {
        StringTokenizer tok = new StringTokenizer(str, ";");
        int nTokens = tok.countTokens();
        String[] res = new String[nTokens];
        for (int i = 0; i < nTokens; i++)
        {
            res[i] = tok.nextToken();
        }
        return res;
    }

}
