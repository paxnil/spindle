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
package com.iw.plugins.spindle.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * @author GWL
 * @version $Id: SpindleEditorPreferencePage.java,v 1.1 2004/05/05 19:24:58
 *          glongman Exp $
 * 
 * Copryright 2002, Intelligent Works Inc. All Rights Reserved
 */
public class SpindleEditorPreferencePage extends PreferencePage
    implements
      IWorkbenchPreferencePage,
      IPropertyChangeListener
{
  private static final String EDITOR_DISPLAY_TAB_WIDTH = PreferenceConstants.EDITOR_DISPLAY_TAB_WIDTH;
  private static final String FORMATTER_PRESERVE_BLANK_LINES = PreferenceConstants.FORMATTER_PRESERVE_BLANK_LINES;
  private static final String FORMATTER_USE_TABS_TO_INDENT = PreferenceConstants.FORMATTER_USE_TABS_TO_INDENT;
  private static final String AUTO_ACTIVATE_CONTENT_ASSIST = PreferenceConstants.AUTO_ACTIVATE_CONTENT_ASSIST;

  private static final String OFFER_XHTML = PreferenceConstants.TEMPLATE_EDITOR_HTML_SHOW_XHTML;
  private static final String[][] OFFER_XHTML_OPTIONS = new String[][]{
      new String[]{TemplateEditor.XHTML_STRICT_LABEL, TemplateEditor.XHTML_STRICT_LABEL},
      new String[]{TemplateEditor.XHTML_TRANSITIONAL_LABEL,
          TemplateEditor.XHTML_TRANSITIONAL_LABEL},
      new String[]{TemplateEditor.XHTML_FRAMES_LABEL, TemplateEditor.XHTML_FRAMES_LABEL},
      new String[]{TemplateEditor.XHTML_NONE_LABEL, TemplateEditor.XHTML_NONE_LABEL},};

  private BooleanFieldEditor fAutoActivateContentAssist;
  private IntegerFieldEditor fDisplayTabWidth;
  private BooleanFieldEditor fPreserveBlankLines;
  private BooleanFieldEditor fUseTabsForIndentation;
  private RadioGroupFieldEditor fOfferXHTML;

  /**
   * Constructor for SpindleRefactorPreferencePage.
   * 
   * @param style
   */
  public SpindleEditorPreferencePage()
  {
    super(UIPlugin.getString("preference-editor-title"), Images
        .getImageDescriptor("applicationDialog.gif"));
    setDescription(UIPlugin.getString("preference-editor-settings"));
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
   */
  public void init(IWorkbench workbench)
  {
  }

  protected Control createContents(Composite parent)
  {
    Font font = parent.getFont();
    GridData gd;

    Composite top = new Composite(parent, SWT.LEFT);
    top.setFont(font);

    // Sets the layout data for the top composite's
    // place in its parent's layout.
    top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    createVerticalSpacer(top, 1);

    fAutoActivateContentAssist = new BooleanFieldEditor(
        AUTO_ACTIVATE_CONTENT_ASSIST,
        UIPlugin.getString("preference-editor-auto-insert-assist"),
        BooleanFieldEditor.DEFAULT,
        top);

    fAutoActivateContentAssist.setPreferencePage(this);
    fAutoActivateContentAssist.setPreferenceStore(UIPlugin
        .getDefault()
        .getPreferenceStore());
    fAutoActivateContentAssist.load();
    fAutoActivateContentAssist.setPropertyChangeListener(this);

    createVerticalSpacer(top, 1);

    createVerticalSpacer(top, 1);

    Composite displayComp = new Composite(top, SWT.NONE);
    GridLayout displayCompLayout = new GridLayout();
    displayCompLayout.numColumns = 2;
    displayCompLayout.marginHeight = 0;
    displayCompLayout.marginWidth = 0;
    displayComp.setLayout(displayCompLayout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    displayComp.setLayoutData(gd);
    displayComp.setFont(font);

    fDisplayTabWidth = new IntegerFieldEditor(EDITOR_DISPLAY_TAB_WIDTH, UIPlugin
        .getString("preference-editor-tab-display-width"), displayComp, 4)
    {
      public void showErrorMessage(String message)
      {
        super.showErrorMessage(UIPlugin
            .getString("preference-editor-tab-display-width-error"));
      }
    };
    fDisplayTabWidth.setValidRange(1, 10);
    fDisplayTabWidth.setPreferencePage(this);
    fDisplayTabWidth.setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
    fDisplayTabWidth.load();
    setValid(fDisplayTabWidth.isValid());
    fDisplayTabWidth.setPropertyChangeListener(this);

    createVerticalSpacer(top, 1);

    fPreserveBlankLines = new BooleanFieldEditor(
        FORMATTER_PRESERVE_BLANK_LINES,
        UIPlugin.getString("preference-formatter-preserve-blank-lines"),
        BooleanFieldEditor.DEFAULT,
        top);

    fPreserveBlankLines.setPreferencePage(this);
    fPreserveBlankLines.setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
    fPreserveBlankLines.load();
    fPreserveBlankLines.setPropertyChangeListener(this);

    fUseTabsForIndentation = new BooleanFieldEditor(
        FORMATTER_USE_TABS_TO_INDENT,
        UIPlugin.getString("preference-formatter-use-tabs-for-indent"),
        BooleanFieldEditor.DEFAULT,
        top);

    fUseTabsForIndentation.setPreferencePage(this);
    fUseTabsForIndentation.setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
    fUseTabsForIndentation.load();
    fUseTabsForIndentation.setPropertyChangeListener(this);

    createVerticalSpacer(top, 1);

    fOfferXHTML = new RadioGroupFieldEditor(OFFER_XHTML, UIPlugin
        .getString("preference-offer-xhtml-proposals"), 4, OFFER_XHTML_OPTIONS, top);

    fOfferXHTML.setPreferencePage(this);
    fOfferXHTML.setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
    fOfferXHTML.load();
    fOfferXHTML.setPropertyChangeListener(this);

    return top;
  }

  /**
   * Create some empty space.
   */
  protected void createVerticalSpacer(Composite comp, int colSpan)
  {
    Label label = new Label(comp, SWT.NONE);
    GridData gd = new GridData();
    gd.horizontalSpan = colSpan;
    label.setLayoutData(gd);
  }

  protected void performDefaults()
  {
    fDisplayTabWidth.loadDefault();
    fPreserveBlankLines.loadDefault();
    fUseTabsForIndentation.loadDefault();
    fOfferXHTML.loadDefault();
    fAutoActivateContentAssist.loadDefault();

    super.performDefaults();
  }

  public boolean performOk()
  {
    fAutoActivateContentAssist.store();
    fDisplayTabWidth.store();
    fPreserveBlankLines.store();
    fUseTabsForIndentation.store();
    fOfferXHTML.store();
    return super.performOk();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.preference.IPreferencePage#isValid()
   */
  public boolean isValid()
  {
    return fDisplayTabWidth.isValid();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    if (event.getProperty().equals(FieldEditor.IS_VALID))
    {
      boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
      // If the new value is true then we must check all field editors.
      // If it is false, then the page is invalid in any case.
      if (newValue)
      {
        setValid(fDisplayTabWidth.isValid() && fAutoActivateContentAssist.isValid()
            && fPreserveBlankLines.isValid() && fUseTabsForIndentation.isValid());
      } else
      {
        setValid(newValue);
      }
    }
  }

}