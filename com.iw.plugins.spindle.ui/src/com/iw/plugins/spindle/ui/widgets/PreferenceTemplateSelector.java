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
package com.iw.plugins.spindle.ui.widgets;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.SpindleStatus;

/**
 * PreferenceTemplateSelectora selector that get's its selection from a
 * preference Also updates the preference on request.
 * 
 * @author glongman@gmail.com
 *  
 */
public class PreferenceTemplateSelector extends SimpleTemplateSelector
    implements
      IPropertyChangeListener
{
  private String fPreferenceKey;
  private IPreferenceStore fPreferenceStore;

  /**
   * @param templateContextId
   */
  public PreferenceTemplateSelector(String templateContextId, String preferenceKey,
      IPreferenceStore store)
  {
    super(templateContextId);
    fPreferenceKey = preferenceKey;
    fPreferenceStore = store;
    UIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }

  public void propertyChange(PropertyChangeEvent event)
  {
    if (fCombo != null && !fCombo.isDisposed() && (UIPlugin.PLUGIN_ID + ".customtemplates").equals(event.getProperty()))
      reload();
  }
  public void dispose()
  {
    super.dispose();
    fPreferenceStore.removePropertyChangeListener(this);
  }

  public void loadDefault()
  {
    populate();
    select(fPreferenceStore.getDefaultString(fPreferenceKey));
    fireSelectionChanged();
  }

  public void load()
  {
    select(fPreferenceStore.getString(fPreferenceKey));
    fireSelectionChanged();
  }

  private void select(String templateName)
  {
    String[] items = fCombo.getItems();
    int i = 0;
    for (; i < items.length; i++)
    {
      Template template = (Template) fCombo.getData(items[i]);

      if (template == null)
        continue;

      if (templateName.equals(template.getName()))
        break;
    }

    if (i < items.length)
    {
      fCombo.select(i);
    } else
    {
      fCombo.add(templateName, 0);
      fCombo.setData(templateName, null);
      fCombo.select(0);
    }
  }

  public void reload()
  {
    if (fCombo != null)
    {
      int index = fCombo.getSelectionIndex();
      if (index >= 0)
      {
        String preserveName;
        Template selected = getSelectedTemplate();

        if (selected == null)
          preserveName = fCombo.getItem(index);
        else
          preserveName = selected.getName();

        populate();
        select(preserveName);

      } else
      {
        populate();
        loadDefault();
      }
    }
    validate();
    fireSelectionChanged();
  }

  protected IStatus doValidate()
  {
    SpindleStatus status = new SpindleStatus();

    if (getSelectedTemplate() == null)
      status.setError(UIPlugin.getString("templates.missing.pref", fCombo.getItem(fCombo
          .getSelectionIndex()), UIPlugin.getString(fPreferenceKey + ".label")));

    return status;
  }

  private void store()
  {
    String value;
    Template selectedTemplate = getSelectedTemplate();
    if (selectedTemplate != null) {
      value = selectedTemplate.getName();
    } else {
      value = fCombo.getItem(fCombo.getSelectionIndex());
    }
    fPreferenceStore.setValue(fPreferenceKey, value);
  }

  public Control createControl(Composite parent, int columnCount)
  {

    Control result = super.createControl(parent, columnCount);
    fCombo.addSelectionListener(new SelectionListener()
    {
      public void widgetSelected(SelectionEvent e)
      {
        store();
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {
        // ignore
      }
    });
    return result;
  }
}