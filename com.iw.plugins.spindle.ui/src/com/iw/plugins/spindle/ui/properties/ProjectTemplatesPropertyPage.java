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
package com.iw.plugins.spindle.ui.properties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ProjectPreferenceStore;
import com.iw.plugins.spindle.core.util.SpindleMultiStatus;
import com.iw.plugins.spindle.editors.assist.usertemplates.XMLFileContextType;
import com.iw.plugins.spindle.ui.preferences.OverlayPreferenceStore;
import com.iw.plugins.spindle.ui.widgets.PreferenceTemplateSelector;

/**
 * @author glongman@gmail.com
 */
public class ProjectTemplatesPropertyPage extends PropertyPage
{

  class Listener implements ISelectionChangedListener
  {
    public void selectionChanged(SelectionChangedEvent event)
    {
      validate();
    }
  }

  private PreferenceTemplateSelector fLibraryTemplateSelector;
  private PreferenceTemplateSelector fComponentTemplateSelector;
  private PreferenceTemplateSelector fPageTemplateSelector;
  private PreferenceTemplateSelector fTapestryTemplateSelector;

  private OverlayPreferenceStore fOverlayStore;
  private Listener fListener;

  public ProjectTemplatesPropertyPage()
  {
    super();

    fListener = new Listener();

  }

  public void setElement(IAdaptable element)
  {
    super.setElement(element);
    fOverlayStore = createOverlayStore();

    fLibraryTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.LIBRARY_FILE_CONTEXT_TYPE,
        PreferenceConstants.LIB_TEMPLATE,
        fOverlayStore);

    fComponentTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE,
        PreferenceConstants.COMPONENT_TEMPLATE,
        fOverlayStore);

    fPageTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.PAGE_FILE_CONTEXT_TYPE,
        PreferenceConstants.PAGE_TEMPLATE,
        fOverlayStore);

    fTapestryTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE,
        PreferenceConstants.TAP_TEMPLATE_TEMPLATE,
        fOverlayStore);
  }
  /**
   * @return
   */
  private OverlayPreferenceStore createOverlayStore()
  {

    IProject project = (IProject) (this.getElement().getAdapter(IProject.class));

    setPreferenceStore(ProjectPreferenceStore.getStore(
        project,
        UIPlugin.SPINDLEUI_PREFS_FILE,
        UIPlugin.getDefault().getPreferenceStore()));

    List overlayKeys = new ArrayList();
    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.STRING,
        (UIPlugin.PLUGIN_ID + "customtemplates")));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.STRING,
        PreferenceConstants.LIB_TEMPLATE));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.STRING,
        PreferenceConstants.COMPONENT_TEMPLATE));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.STRING,
        PreferenceConstants.PAGE_TEMPLATE));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.STRING,
        PreferenceConstants.TAP_TEMPLATE_TEMPLATE));

    OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys
        .size()];
    overlayKeys.toArray(keys);

    return new OverlayPreferenceStore(getPreferenceStore(), keys);
  }

  private void validate()
  {
    SpindleMultiStatus status = new SpindleMultiStatus();
    status.addStatus(fLibraryTemplateSelector.validate());
    status.addStatus(fComponentTemplateSelector.validate());
    status.addStatus(fPageTemplateSelector.validate());
    status.addStatus(fTapestryTemplateSelector.validate());

    IStatus mostSevere = status.getMostSevere(status.getChildren());
    if (!mostSevere.isOK())
    {
      setValid(false);
      setErrorMessage(mostSevere.getMessage());
    } else
    {
      setErrorMessage(null);
      setValid(true);
    }
  }

  protected Control createContents(Composite parent)
  {
    initializeDialogUnits(parent);

    fOverlayStore.load();
    fOverlayStore.start();

    Font font = parent.getFont();
    Composite composite = new Composite(parent, SWT.NULL);
    composite.setFont(font);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_BOTH
        | GridData.VERTICAL_ALIGN_BEGINNING));

    Label intro = new Label(composite, SWT.WRAP);
    intro.setText(UIPlugin.getString("project-wizard-intro"));
    intro.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

    createVerticalSpacer(composite, 1);

    createComboGroup(composite);

    return composite;
  }

  private void createComboGroup(Composite parent)
  {
    Font font = parent.getFont();
    Group group = new Group(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    int columnCount = 3;
    layout.numColumns = columnCount;
    group.setLayout(layout);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setFont(font);
    group.setText(UIPlugin.getString("project-wizard-group-title"));

    fLibraryTemplateSelector.createControl(group, columnCount);
    fLibraryTemplateSelector.addSelectionChangedListener(fListener);

    fComponentTemplateSelector.createControl(group, columnCount);
    fComponentTemplateSelector.addSelectionChangedListener(fListener);

    fPageTemplateSelector.createControl(group, columnCount);
    fPageTemplateSelector.addSelectionChangedListener(fListener);

    fTapestryTemplateSelector.createControl(group, columnCount);
    fTapestryTemplateSelector.addSelectionChangedListener(fListener);

    fLibraryTemplateSelector.load();
    fComponentTemplateSelector.load();
    fPageTemplateSelector.load();
    fTapestryTemplateSelector.load();
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
    fOverlayStore.loadDefaults();
    fLibraryTemplateSelector.loadDefault();
    fComponentTemplateSelector.loadDefault();
    fPageTemplateSelector.loadDefault();
    fTapestryTemplateSelector.loadDefault();
    super.performDefaults();
  }

  public boolean performOk()
  {
    fOverlayStore.propagate();
    ProjectPreferenceStore store = (ProjectPreferenceStore) getPreferenceStore();
    try
    {
      store.save();
    } catch (IOException e)
    {
      UIPlugin.log(e);
      return false;
    }
    return true;
  }

  public void dispose()
  {
    if (fOverlayStore != null)
    {
      fOverlayStore.stop();
      fOverlayStore = null;
    }

    fLibraryTemplateSelector.dispose();
    fComponentTemplateSelector.dispose();
    fPageTemplateSelector.dispose();
    fTapestryTemplateSelector.dispose();
    super.dispose();
  }

}