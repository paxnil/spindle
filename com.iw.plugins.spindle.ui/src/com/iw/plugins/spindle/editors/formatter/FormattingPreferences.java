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
package com.iw.plugins.spindle.editors.formatter;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;

public class FormattingPreferences
{

  IPreferenceStore fPrefs = UIPlugin.getDefault().getPreferenceStore();

  public String getCanonicalIndent()
  {
    String canonicalIndent;
    if (!useSpacesInsteadOfTabs())
    {
      canonicalIndent = "\t";
    } else
    {
      String tab = "";
      for (int i = 0; i < getTabWidth(); i++)
      {
        tab = tab.concat(" ");
      }
      canonicalIndent = tab;
    }

    return canonicalIndent;
  }
  
  public boolean preserveBlankLines() {
    return fPrefs.getBoolean(PreferenceConstants.FORMATTER_PRESERVE_BLANK_LINES);
  }

  public int getMaximumLineWidth()
  {
    return fPrefs.getInt(PreferenceConstants.FORMATTER_MAX_LINE_LENGTH);
  }

  public boolean wrapLongTags()
  {
    return fPrefs.getBoolean(PreferenceConstants.FORMATTER_WRAP_LONG);
  }

  public boolean alignElementCloseChar()
  {
    return fPrefs.getBoolean(PreferenceConstants.FORMATTER_ALIGN);
  }

  public int getTabWidth()
  {
    return fPrefs.getInt(PreferenceConstants.FORMATTER_TAB_SIZE);
  }

  public boolean useSpacesInsteadOfTabs()
  {
    return !fPrefs.getBoolean(PreferenceConstants.FORMATTER_TAB_CHAR);
  }

  public static boolean affectsFormatting(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    return property.startsWith(PreferenceConstants.FORMATTER_ALIGN)
        || property.startsWith(PreferenceConstants.FORMATTER_MAX_LINE_LENGTH)
        || property.startsWith(PreferenceConstants.FORMATTER_TAB_CHAR)
        || property.startsWith(PreferenceConstants.FORMATTER_TAB_SIZE)
        || property.startsWith(PreferenceConstants.FORMATTER_WRAP_LONG)
        || property.startsWith(PreferenceConstants.FORMATTER_PRESERVE_BLANK_LINES);
  }
  /**
   * Sets the preference store for these formatting preferences.
   * 
   * @param prefs the preference store to use as a reference for the formatting
   *                     preferences
   */
  public void setPreferenceStore(IPreferenceStore prefs)
  {
    fPrefs = prefs;
  }
}