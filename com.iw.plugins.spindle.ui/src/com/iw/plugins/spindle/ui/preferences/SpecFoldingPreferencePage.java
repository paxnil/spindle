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
package com.iw.plugins.spindle.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;

/**
 * @author GWL
 */
public class SpecFoldingPreferencePage extends PreferencePage
    implements
      IWorkbenchPreferencePage,
      IPropertyChangeListener
{
  private static final String FOLDING_ENABLED = PreferenceConstants.EDITOR_FOLDING_ENABLED;

  private BooleanFieldEditor fFoldOnEditorOpen;
  

  /**
   * Constructor for SpindleRefactorPreferencePage.
   * 
   * @param style
   */
  public SpecFoldingPreferencePage()
  {
    super(UIPlugin.getString("preference-folding-title"), Images
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
    int numColumns = 1;    
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    top.setLayout(layout);

    // Sets the layout data for the top composite's
    // place in its parent's layout.
    top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    

    fFoldOnEditorOpen = new BooleanFieldEditor(
        FOLDING_ENABLED,
        UIPlugin.getString("preference-editor-folding-enabled"),
        BooleanFieldEditor.DEFAULT,
        top);

    fFoldOnEditorOpen.setPreferencePage(this);
    fFoldOnEditorOpen.setPreferenceStore(UIPlugin
        .getDefault()
        .getPreferenceStore());
    fFoldOnEditorOpen.load();
    fFoldOnEditorOpen.setPropertyChangeListener(this);

    return top;
  }


  protected void performDefaults()
  {
    fFoldOnEditorOpen.loadDefault();   
    super.performDefaults();
  }

  public boolean performOk()
  {
    fFoldOnEditorOpen.store(); 
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
        setValid(fFoldOnEditorOpen.isValid());
      } else
      {
        setValid(newValue);
      }
    }
  }

}