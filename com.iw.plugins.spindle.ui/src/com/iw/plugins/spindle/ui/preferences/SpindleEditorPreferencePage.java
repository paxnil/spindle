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
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * @author GWL
 * 
 * Copryright 2002, Intelligent Works Inc. All Rights Reserved
 */
public class SpindleEditorPreferencePage extends PreferencePage
    implements
      IWorkbenchPreferencePage,
      IPropertyChangeListener
{
  private static final String AUTO_ACTIVATE_CONTENT_ASSIST = PreferenceConstants.AUTO_ACTIVATE_CONTENT_ASSIST;

  private static final String OFFER_XHTML = PreferenceConstants.TEMPLATE_EDITOR_HTML_SHOW_XHTML;
  private static final String[][] OFFER_XHTML_OPTIONS = new String[][]{
      new String[]{TemplateEditor.XHTML_STRICT_LABEL, TemplateEditor.XHTML_STRICT_LABEL},
      new String[]{TemplateEditor.XHTML_TRANSITIONAL_LABEL,
          TemplateEditor.XHTML_TRANSITIONAL_LABEL},
      new String[]{TemplateEditor.XHTML_FRAMES_LABEL, TemplateEditor.XHTML_FRAMES_LABEL},
      new String[]{TemplateEditor.XHTML_NONE_LABEL, TemplateEditor.XHTML_NONE_LABEL},};

  private BooleanFieldEditor fAutoActivateContentAssist;
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

  }

  /**
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
   */
  public void init(IWorkbench workbench)
  {
  }

  protected Control createContents(Composite parent)
  {
    initializeDialogUnits(parent);
    Font font = parent.getFont();
    GridData gd;

    Composite top = new Composite(parent, SWT.LEFT);
    top.setFont(font);
    int numColumns = 2;
    Composite result = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    top.setLayout(layout);

    // Sets the layout data for the top composite's
    // place in its parent's layout.
    top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Group group = createGroup(2, top, "Content Assist");

    fAutoActivateContentAssist = new BooleanFieldEditor(
        AUTO_ACTIVATE_CONTENT_ASSIST,
        UIPlugin.getString("preference-editor-auto-insert-assist"),
        BooleanFieldEditor.DEFAULT,
        group);

    fAutoActivateContentAssist.setPreferencePage(this);
    fAutoActivateContentAssist.setPreferenceStore(UIPlugin
        .getDefault()
        .getPreferenceStore());
    fAutoActivateContentAssist.load();
    fAutoActivateContentAssist.setPropertyChangeListener(this);

    fOfferXHTML = new RadioGroupFieldEditor(OFFER_XHTML, UIPlugin
        .getString("preference-offer-xhtml-proposals"), 4, OFFER_XHTML_OPTIONS, group);

    fOfferXHTML.setPreferencePage(this);
    fOfferXHTML.setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
    fOfferXHTML.load();
    fOfferXHTML.setPropertyChangeListener(this);

    int heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);

    Label spacer = new Label(top, SWT.NULL);
    spacer.setText(" ");
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.horizontalSpan = 2;
    data.heightHint = heightHint;
    spacer.setLayoutData(data);

    Label message = new Label(top, SWT.NULL);
    message.setText("Spindle Editors respond to changes on page: ");
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.horizontalSpan = 2;
    message.setLayoutData(data);
    
    Label message2 = new Label(top, SWT.NULL);
    message2.setText("  Workbench -> Editors -> Text Editor ");
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.horizontalSpan = 2;
    message2.setLayoutData(data);

    return top;
  }

  protected Group createGroup(int numColumns, Composite parent, String text)
  {
    final Group group = new Group(parent, SWT.NONE);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = numColumns;
    //    gd.widthHint = 0;
    group.setLayoutData(gd);
    group.setFont(parent.getFont());

    final GridLayout layout = new GridLayout(numColumns, false);
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);

    group.setLayout(layout);
    group.setText(text);
    return group;
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
    fAutoActivateContentAssist.loadDefault();
    fOfferXHTML.loadDefault();
    super.performDefaults();
  }

  public boolean performOk()
  {
    fAutoActivateContentAssist.store();
    fOfferXHTML.store();
    return super.performOk();
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
        setValid(fAutoActivateContentAssist.isValid() && fOfferXHTML.isValid());
      } else
      {
        setValid(newValue);
      }
    }
  }

}