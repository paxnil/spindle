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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle;

import net.sf.solareclipse.ui.preferences.ITextStylePreferences;
import net.sf.solareclipse.xml.internal.ui.preferences.XMLSyntaxPreferencePage;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;

import com.iw.plugins.spindle.editors.spec.outline.MultiPageContentOutline;
import com.iw.plugins.spindle.editors.spec.outline.TapestryOutlinePage;
import com.iw.plugins.spindle.editors.template.ITemplateSyntaxConstants;
import com.iw.plugins.spindle.editors.template.TemplateEditor;
import com.iw.plugins.spindle.ui.wizards.NewTapComponentWizardPage;

/**
 * Preference Initializer for the Spindle UI
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: PreferenceInitializer.java,v 1.1 2004/06/05 04:21:44 glongman
 *          Exp $
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
    implements
      PreferenceConstants
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
    IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
    EditorsUI.useAnnotationsPreferencePage(store);
	EditorsUI.useQuickDiffPreferencePage(store);
    initializeDefaultValues(store);
    XMLSyntaxPreferencePage.initializeDefaultPreferences(store);
    NewTapComponentWizardPage.initializeDefaultPreferences(store);
    MultiPageContentOutline.initializeDefaultPreferences(store);
    TapestryOutlinePage.initializeDefaultPreferences(store);
  }

  private void initializeDefaultValues(IPreferenceStore store)
  {

    store.setDefault(PreferenceConstants.P_HTML_TO_GENERATE, UIPlugin
        .getString("TAPESTRY.genHTMLSource"));

    setDefaultTextStyleInfo(
        store,
        ITemplateSyntaxConstants.TAPESTRY_ATT_NAME,
        "0,64,128",
        ITextStylePreferences.STYLE_NORMAL);
    setDefaultTextStyleInfo(
        store,
        ITemplateSyntaxConstants.TAPESTRY_ATT_VALUE,
        "0,64,128",
        ITextStylePreferences.STYLE_BOLD);

    
    store.setDefault(EDITOR_USE_SMART_INDENT, true);
    store.setDefault(TEMPLATE_EDITOR_HTML_SHOW_XHTML, TemplateEditor.XHTML_NONE_LABEL);
    store.setDefault(AUTO_ACTIVATE_CONTENT_ASSIST, false);
    
    store.setDefault(FORMATTER_MAX_LINE_LENGTH, 80); 
    store.setDefault(FORMATTER_WRAP_LONG, true);  
    store.setDefault(FORMATTER_ALIGN, false);  
    store.setDefault(FORMATTER_PRESERVE_BLANK_LINES, false);
    store.setDefault(FORMATTER_TAB_SIZE, 4); 
    store.setDefault(FORMATTER_TAB_CHAR, false);

  }

  private void setDefaultTextStyleInfo(
      IPreferenceStore store,
      String constant,
      String color,
      String style)
  {
    store.setDefault(constant + ITextStylePreferences.SUFFIX_FOREGROUND, color);
    store.setDefault(constant + ITextStylePreferences.SUFFIX_STYLE, style);
  }

}