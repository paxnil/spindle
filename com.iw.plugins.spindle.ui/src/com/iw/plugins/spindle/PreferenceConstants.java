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

package com.iw.plugins.spindle;

import net.sf.solareclipse.ui.preferences.ITextStylePreferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import com.iw.plugins.spindle.editors.template.ITemplateSyntaxConstants;

/**
 *  Preference constants used by Spindle
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PreferenceConstants
{

    public static String EDITOR_OVERVIEW_RULER = UIPlugin.PLUGIN_ID + ".EDITOR_OVERVIEW_RULER";

    public static final String EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER =
        UIPlugin.PLUGIN_ID + ".EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER";

    public static final String EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER =
        UIPlugin.PLUGIN_ID + ".EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER";

    public static final String EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER =
        UIPlugin.PLUGIN_ID + ".EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER";

    public static final String EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER =
        UIPlugin.PLUGIN_ID + ".EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER";

    public static final String EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER =
        UIPlugin.PLUGIN_ID + ".EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER";

    public static final String EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER =
        UIPlugin.PLUGIN_ID + ".EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER";

    public static final String P_HTML_TO_GENERATE = UIPlugin.PLUGIN_ID + ".P_HTML_TO_GENERATE";

    public static final String EDITOR_CURRENT_LINE = UIPlugin.PLUGIN_ID + ".EDITOR_CURRENT_LINE";

    public static final String EDITOR_CURRENT_LINE_COLOR = UIPlugin.PLUGIN_ID + ".EDITOR_CURRENT_LINE_COLOR";

    public static final String EDITOR_PRINT_MARGIN = UIPlugin.PLUGIN_ID + ".EDITOR_PRINT_MARGIN";

    public static final String EDITOR_PRINT_MARGIN_COLOR = UIPlugin.PLUGIN_ID + ".EDITOR_PRINT_MARGIN_COLOR";

    public static final String EDITOR_PRINT_MARGIN_COLUMN = UIPlugin.PLUGIN_ID + ".EDITOR_PRINT_MARGIN_COLUMN";

    public static final String EDITOR_PROBLEM_INDICATION = UIPlugin.PLUGIN_ID + ".EDITOR_PROBLEM_INDICATION";

    public static final String EDITOR_PROBLEM_INDICATION_COLOR =
        UIPlugin.PLUGIN_ID + ".EDITOR_PROBLEM_INDICATION_COLOR";

    public static final String EDITOR_WARNING_INDICATION = UIPlugin.PLUGIN_ID + ".EDITOR_WARNING_INDICATION";

    public static final String EDITOR_WARNING_INDICATION_COLOR =
        UIPlugin.PLUGIN_ID + ".EDITOR_WARNING_INDICATION_COLOR";

    public static final String EDITOR_TASK_INDICATION = UIPlugin.PLUGIN_ID + ".EDITOR_TASK_INDICATION";

    public static final String EDITOR_TASK_INDICATION_COLOR = UIPlugin.PLUGIN_ID + ".EDITOR_TASK_INDICATION_COLOR";

    public static final String EDITOR_BOOKMARK_INDICATION = UIPlugin.PLUGIN_ID + ".EDITOR_BOOKMARK_INDICATION";

    public static final String EDITOR_BOOKMARK_INDICATION_COLOR =
        UIPlugin.PLUGIN_ID + ".EDITOR_BOOKMARK_INDICATION_COLOR";

    public static final String EDITOR_SEARCH_RESULT_INDICATION =
        UIPlugin.PLUGIN_ID + ".EDITOR_SEARCH_RESULT_INDICATION";

    public static final String EDITOR_SEARCH_RESULT_INDICATION_COLOR =
        UIPlugin.PLUGIN_ID + ".EDITOR_SEARCH_RESULT_INDICATION_COLOR";

    public static final String EDITOR_UNKNOWN_INDICATION = UIPlugin.PLUGIN_ID + ".EDITOR_UNKNOWN_INDICATION";

    public static final String EDITOR_UNKNOWN_INDICATION_COLOR =
        UIPlugin.PLUGIN_ID + ".EDITOR_UNKNOWN_INDICATION_COLOR";

    public static void initializeDefaultValues(IPreferenceStore store)
    {

        store.setDefault(EDITOR_OVERVIEW_RULER, true);

        store.setDefault(PreferenceConstants.EDITOR_CURRENT_LINE, true);
        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_CURRENT_LINE_COLOR, new RGB(225, 235, 224));

        store.setDefault(PreferenceConstants.EDITOR_PRINT_MARGIN, false);
        store.setDefault(PreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 80);
        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, new RGB(176, 180, 185));

        store.setDefault(PreferenceConstants.EDITOR_PROBLEM_INDICATION, true);
        PreferenceConverter.setDefault(
            store,
            PreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR,
            new RGB(255, 0, 128));
        store.setDefault(PreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER, true);

        store.setDefault(PreferenceConstants.EDITOR_WARNING_INDICATION, true);
        PreferenceConverter.setDefault(
            store,
            PreferenceConstants.EDITOR_WARNING_INDICATION_COLOR,
            new RGB(244, 200, 45));
        store.setDefault(PreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER, true);

        store.setDefault(PreferenceConstants.EDITOR_TASK_INDICATION, false);
        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_TASK_INDICATION_COLOR, new RGB(0, 128, 255));
        store.setDefault(PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER, true);

        store.setDefault(PreferenceConstants.EDITOR_BOOKMARK_INDICATION, false);
        PreferenceConverter.setDefault(
            store,
            PreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR,
            new RGB(34, 164, 99));
        store.setDefault(PreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER, true);

        store.setDefault(PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION, true);
        PreferenceConverter.setDefault(
            store,
            PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR,
            new RGB(192, 192, 192));
        store.setDefault(PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER, true);

        store.setDefault(PreferenceConstants.EDITOR_UNKNOWN_INDICATION, false);
        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR, new RGB(0, 0, 0));
        store.setDefault(PreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER, false);

        store.setDefault(PreferenceConstants.P_HTML_TO_GENERATE, UIPlugin.getString("TAPESTRY.genHTMLSource"));

        setDefault(store, ITemplateSyntaxConstants.TAPESTRY_ATT_NAME, "0,64,128", ITextStylePreferences.STYLE_NORMAL);
        setDefault(store, ITemplateSyntaxConstants.TAPESTRY_ATT_VALUE, "0,64,128", ITextStylePreferences.STYLE_BOLD);
        
    }

    private static void setDefault(IPreferenceStore store, String constant, String color, String style)
    {
        store.setDefault(constant + ITextStylePreferences.SUFFIX_FOREGROUND, color);
        store.setDefault(constant + ITextStylePreferences.SUFFIX_STYLE, style);
    }
}
