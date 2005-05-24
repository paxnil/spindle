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
package com.iw.plugins.spindle.ui.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ProjectPreferenceStore;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.util.SpindleMultiStatus;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;
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

  private Button fImport;
  private Button fExport;
  private Button fExportProject;

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
      if (fImport != null)
      {
        fImport.setEnabled(false);
        fExport.setEnabled(false);
        fExportProject.setEnabled(false);
      }
      setValid(false);
      setErrorMessage(mostSevere.getMessage());
    } else
    {
      if (fImport != null)
      {
        fImport.setEnabled(true);
        fExport.setEnabled(true);
        fExportProject.setEnabled(true);
      }
      setErrorMessage(null);
      setValid(true);
    }
  }

  protected void contributeButtons(Composite parent)
  {
    Font font = parent.getFont();
    GridLayout layout = (GridLayout) parent.getLayout();
    layout.numColumns += 3;

    int heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
    int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);

    fImport = new Button(parent, SWT.PUSH);
    fImport.setText("Import");
    Dialog.applyDialogFont(fImport);
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.heightHint = heightHint;
    data.widthHint = Math.max(widthHint, fImport.computeSize(
        SWT.DEFAULT,
        SWT.DEFAULT,
        true).x);
    fImport.setLayoutData(data);
    fImport.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        performImport();
      }
    });

    fExport = new Button(parent, SWT.PUSH);
    fExport.setText("Export");
    Dialog.applyDialogFont(fImport);
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.heightHint = heightHint;
    data.widthHint = Math.max(widthHint, fExport.computeSize(
        SWT.DEFAULT,
        SWT.DEFAULT,
        true).x);
    fExport.setLayoutData(data);
    fExport.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        performExport();
      }
    });

    fExportProject = new Button(parent, SWT.PUSH);
    fExportProject.setText("Export to Project");
    Dialog.applyDialogFont(fImport);
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.heightHint = heightHint;
    data.widthHint = Math.max(widthHint, fExportProject.computeSize(
        SWT.DEFAULT,
        SWT.DEFAULT,
        true).x);
    fExportProject.setLayoutData(data);
    fExportProject.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        performProjectExport();
      }
    });

  }

  /**
   *  
   */
  protected void performProjectExport()
  {
    IJavaProject[] jprojects;
    try
    {
      jprojects = JavaCore
          .create(ResourcesPlugin.getWorkspace().getRoot())
          .getJavaProjects();
    } catch (JavaModelException e)
    {
      UIPlugin.log_it(e);
      jprojects = new IJavaProject[0];
    }

    ArrayList list = new ArrayList();
    try
    {
      for (int i = 0; i < jprojects.length; i++)
      {
        IProject workbenchProject = jprojects[i].getProject();

        if (workbenchProject.equals(getElement().getAdapter(IProject.class)))
          continue;

        if (workbenchProject.isAccessible()
            && workbenchProject.hasNature(TapestryCore.NATURE_ID))
          list.add(workbenchProject);
      }
    } catch (CoreException e1)
    {
      ErrorDialog.openError(getShell(), "Operation Aborted", e1.getMessage(), e1
          .getStatus());
      return;
    }

    if (list.isEmpty())
    {
      MessageDialog.openInformation(
          getShell(),
          "Operation Aborted",
          "No valid export targets found.");
      return;
    }

    LabelProvider labelProvider = new LabelProvider()
    {
      private Image image = Images.getSharedImage("tproject.gif");

      public Image getImage(Object element)
      {
        return image;
      }

      public String getText(Object element)
      {
        return ((IProject) element).getName();
      }
    };

    ElementListSelectionDialog dialog = new ElementListSelectionDialog(
        getShell(),
        labelProvider);
    dialog.setTitle("Export to project");
    dialog.setMessage("Choose the project you want to export to.");
    dialog.setElements(list.toArray());
    dialog.setInitialSelections(new Object[]{});

    IProject selected = null;
    if (dialog.open() == ElementListSelectionDialog.OK)
    {
      selected = (IProject) dialog.getFirstResult();
    }

    if (selected == null)
      return;

    IPreferenceStore myStore = fOverlayStore;

    ProjectPreferenceStore exportStore = ProjectPreferenceStore.getStore(
        selected,
        UIPlugin.SPINDLEUI_PREFS_FILE,
        UIPlugin.getDefault().getPreferenceStore());

    exportProjectPreference(
        myStore,
        exportStore,
        PreferenceConstants.LIB_TEMPLATE,
        fLibraryTemplateSelector);

    exportProjectPreference(
        myStore,
        exportStore,
        PreferenceConstants.COMPONENT_TEMPLATE,
        fComponentTemplateSelector);

    exportProjectPreference(
        myStore,
        exportStore,
        PreferenceConstants.PAGE_TEMPLATE,
        fPageTemplateSelector);

    exportProjectPreference(
        myStore,
        exportStore,
        PreferenceConstants.TAP_TEMPLATE_TEMPLATE,
        fTapestryTemplateSelector);

    try
    {
      exportStore.save();
    } catch (IOException e2)
    {
      SpindleStatus status = new SpindleStatus();
      status.setError(e2.getMessage());
      ErrorDialog.openError(
          getShell(),
          "Operation Aborted",
          e2.getClass().toString(),
          status);
    }

  }

  protected void exportProjectPreference(
      IPreferenceStore src,
      IPreferenceStore target,
      String key,
      PreferenceTemplateSelector selector)
  {
    boolean srcIsDefault = src.isDefault(key);
    boolean targetIsDefault = target.isDefault(key);

    if (srcIsDefault && targetIsDefault)
      return;

    if (srcIsDefault && !targetIsDefault)
    {
      target.setToDefault(key);
      return;
    }

    String value = selector.getSelectedTemplate().getName();

    target.setValue(key, value);
  }

  protected void performImport()
  {
    FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
    fileDialog.setFilterExtensions(new String[]{"*.project.prefs", "*.*"});
    fileDialog.setText("Choose file to import");
    String file = fileDialog.open();
    if (file != null)
    {
      file = file.trim();
      if (file.length() > 0)
      {
        Properties p = null;
        try
        {
          FileInputStream in = new FileInputStream(file);
          p = new Properties();
          p.load(in);
        } catch (FileNotFoundException e)
        {
          MessageDialog.openError(getShell(), "File Not Found", file + " does not exist");
          return;
        } catch (IOException e)
        {
          UIPlugin.log_it(e);
          MessageDialog.openError(getShell(), "File Error", "unable to read: " + file);
          return;
        }
        doImport(p);
      }
    }
  }

  private void doImport(Properties importProperties)
  {
    Map updateMap = new HashMap();
    SpindleMultiStatus errors = new SpindleMultiStatus();

    checkImport(fLibraryTemplateSelector, importProperties, updateMap, errors);
    checkImport(fComponentTemplateSelector, importProperties, updateMap, errors);
    checkImport(fPageTemplateSelector, importProperties, updateMap, errors);
    checkImport(fTapestryTemplateSelector, importProperties, updateMap, errors);

    boolean hasErrors = errors.getChildren().length > 0;
    if (hasErrors)
    {
      if (!updateMap.isEmpty())
      {
        errors.setError("Invalid entries found");
        int decision = ErrorDialog.openError(
            getShell(),
            null,
            "Pressing 'Ok' will continue the import, while ignoring invalid entries.",
            errors);

        if (decision != ErrorDialog.OK)
          return;
      } else
      {
        ErrorDialog.openError(
            getShell(),
            "Import Abort",
            "No valid entries found.",
            errors);
        return;
      }
    }

    if (updateMap.isEmpty())
    {
      MessageDialog.openInformation(
          getShell(),
          "Import Abort",
          "No entries found to import");
      return;
    }

    for (Iterator iter = updateMap.keySet().iterator(); iter.hasNext();)
    {
      String contextType = (String) iter.next();
      if (contextType.equals(XMLFileContextType.LIBRARY_FILE_CONTEXT_TYPE))
      {
        fLibraryTemplateSelector.select((String) updateMap.get(contextType));
      } else if (contextType.equals(XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE))
      {
        fComponentTemplateSelector.select((String) updateMap.get(contextType));
      } else if (contextType.equals(XMLFileContextType.PAGE_FILE_CONTEXT_TYPE))
      {
        fPageTemplateSelector.select((String) updateMap.get(contextType));
      } else if (contextType.equals(XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE))
      {
        fTapestryTemplateSelector.select((String) updateMap.get(contextType));
      }
    }
  }

  private void checkImport(
      PreferenceTemplateSelector selector,
      Properties importProperties,
      Map updateMap,
      SpindleMultiStatus errors)
  {
    IStatus status;

    String contextType = selector.getPreferenceKey();
    String templateName = importProperties.getProperty(contextType, null);
    if (templateName != null)
    {
      status = selector.validate(templateName);
      if (status.isOK())
      {
        updateMap.put(contextType, templateName);
      } else
      {
        errors.addStatus(status);
      }
    }
  }

  protected void performExport()
  {
    FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
    fileDialog.setFilterExtensions(new String[]{"*.project.prefs", "*.*"});
    fileDialog.setText("Choose file to export");
    String filename = fileDialog.open();

    if (filename != null)
    {

      File file = new File(filename);
      if (file.exists())
      {
        if (!MessageDialog.openQuestion(
            getShell(),
            "File exists",
            "Do you want to overwrite '" + file.getName() + "' ?"))
          return;
      }

      Properties p = new Properties();

      p.setProperty(
          XMLFileContextType.LIBRARY_FILE_CONTEXT_TYPE,
          fLibraryTemplateSelector.getSelectedTemplate().getName());

      p.setProperty(
          XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE,
          fComponentTemplateSelector.getSelectedTemplate().getName());

      p.setProperty(XMLFileContextType.PAGE_FILE_CONTEXT_TYPE, fPageTemplateSelector
          .getSelectedTemplate()
          .getName());

      p.setProperty(
          XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE,
          fTapestryTemplateSelector.getSelectedTemplate().getName());

      try
      {
        FileOutputStream out = new FileOutputStream(filename);

        StringBuffer buffer = new StringBuffer();
        p.store(out, "Spindle wizard template defaults - project\n");

      } catch (IOException e)
      {
        SpindleStatus status = new SpindleStatus();
        status.setError(e.getMessage());
        ErrorDialog.openError(
            getShell(),
            "Export Failed",
            e.getClass().toString(),
            status);
      }
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
      UIPlugin.log_it(e);
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