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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.wizards.project;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ProjectPreferenceStore;
import com.iw.plugins.spindle.core.util.SpindleMultiStatus;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.editors.assist.usertemplates.XMLFileContextType;
import com.iw.plugins.spindle.ui.preferences.WizardTemplatesPreferencePage;
import com.iw.plugins.spindle.ui.util.UIUtils;
import com.iw.plugins.spindle.ui.widgets.PreferenceTemplateSelector;
import com.iw.plugins.spindle.ui.wizards.TapestryWizardPage;
import com.iw.plugins.spindle.ui.wizards.factories.IFactoryTemplateSource;
import com.iw.plugins.spindle.ui.wizards.factories.PageFactory;
import com.iw.plugins.spindle.ui.wizards.factories.TapestryTemplateFactory;
import com.iw.plugins.spindle.ui.wizards.factories.TemplateFactory;

/**
 * TemplateSelectionPage - users can pick and chose what code templates are used
 * by the wizard.
 * 
 * @author glongman@gmail.com
 *  
 */
public class BaseTemplateSelectionPage extends TapestryWizardPage implements IFactoryTemplateSource
{

  class Listener implements ISelectionChangedListener, IPropertyChangeListener
  {

    boolean run = true;

    public void start()
    {
      run = true;
    }

    public void stop()
    {
      run = false;
    }
    
    public void selectionChanged(SelectionChangedEvent event)
    {
      if (run)
        validate();
    }

    public void propertyChange(PropertyChangeEvent event)
    {
      if (run)
        if ((UIPlugin.PLUGIN_ID + ".customtemplates").equals(event.getProperty()))
          validate();
    }
  }

  private Button fUseWorkspaceDefaultTemplates;
  private PreferenceTemplateSelector fLibraryTemplateSelector;
  private PreferenceTemplateSelector fComponentTemplateSelector;
  private PreferenceTemplateSelector fPageTemplateSelector;
  private PreferenceTemplateSelector fTapestryTemplateSelector;

  private ProjectPreferenceStore fProjectPreferences;

  private Group fProjectTemplateGroup;
  private Listener fListener;
  
  String PAGE_NAME;

  public BaseTemplateSelectionPage(String name)
  {
    super(name);
        
    PAGE_NAME = name;
    //    this.setImageDescriptor(ImageDescriptor.createFromURL(Images.getImageURL(UIPlugin
    //        .getString(PAGE_NAME + ".image"))));
    //    this.setDescription(UIPlugin.getString(PAGE_NAME + ".description"));
    this.setDescription("Project file generation templates");

    fProjectPreferences = ProjectPreferenceStore.createEmptyStore(UIPlugin
        .getDefault()
        .getPreferenceStore());

    fLibraryTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.LIBRARY_FILE_CONTEXT_TYPE,
        PreferenceConstants.LIB_TEMPLATE,
        fProjectPreferences);

    fComponentTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE,
        PreferenceConstants.COMPONENT_TEMPLATE,
        fProjectPreferences);

    fPageTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.PAGE_FILE_CONTEXT_TYPE,
        PreferenceConstants.PAGE_TEMPLATE,
        fProjectPreferences);

    fTapestryTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE,
        PreferenceConstants.TAP_TEMPLATE_TEMPLATE,
        fProjectPreferences);

    fListener = new Listener();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    setPageComplete(true);

    Font font = parent.getFont();
    Composite composite = new Composite(parent, SWT.NULL);
    composite.setFont(font);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_BOTH
        | GridData.VERTICAL_ALIGN_BEGINNING));

    fUseWorkspaceDefaultTemplates = new Button(composite, SWT.CHECK);
    fUseWorkspaceDefaultTemplates.setFont(font);
    fUseWorkspaceDefaultTemplates.setText("U&se workspace default templates.");
    fUseWorkspaceDefaultTemplates.addSelectionListener(new SelectionListener()
    {

      public void widgetSelected(SelectionEvent e)
      {
        updateEnabled(!((Button) e.widget).getSelection());
        validate();
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {
        // do nothing
      }
    });

    fUseWorkspaceDefaultTemplates.setLayoutData(new GridData(
        GridData.HORIZONTAL_ALIGN_BEGINNING));

    Label spacer = new Label(parent, SWT.NULL);
    spacer.setText(" ");
    spacer.setFont(font);
    spacer.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

    Button button = new Button(composite, SWT.NULL);
    button.setText("Configure workspace defaults");
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.addSelectionListener(new SelectionListener()
    {
      public void widgetSelected(SelectionEvent e)
      {
        showWorkspaceDefaultPreferencePage();
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {
        //ignore
      }
    });

    spacer = new Label(parent, SWT.NULL);
    spacer.setText(" ");
    spacer.setFont(font);
    spacer.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

    createComboGroup(composite);
    setErrorMessage(null);
    setMessage(null);
    setControl(composite);

    fUseWorkspaceDefaultTemplates.setSelection(true);
    updateEnabled(false);

    fLibraryTemplateSelector.load();
    fComponentTemplateSelector.load();
    fPageTemplateSelector.load();
    fTapestryTemplateSelector.load();

    UIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fListener);

    validate();
  }

  /**
   *  
   */
  protected void showWorkspaceDefaultPreferencePage()
  {
    UIUtils.showPreferencePage(
        getShell(),
        "SpindleWizardTemplatePreferences",
        new WizardTemplatesPreferencePage());
  }

  private void createComboGroup(Composite parent)
  {
    Font font = parent.getFont();
    fProjectTemplateGroup = new Group(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    int columnCount = 3;
    layout.numColumns = columnCount;
    fProjectTemplateGroup.setLayout(layout);
    fProjectTemplateGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fProjectTemplateGroup.setFont(font);
    fProjectTemplateGroup.setText("Setup project default templates:");

    fLibraryTemplateSelector.createControl(fProjectTemplateGroup, columnCount);
    fLibraryTemplateSelector.addSelectionChangedListener(fListener);

    fComponentTemplateSelector.createControl(fProjectTemplateGroup, columnCount);
    fComponentTemplateSelector.addSelectionChangedListener(fListener);

    fPageTemplateSelector.createControl(fProjectTemplateGroup, columnCount);
    fPageTemplateSelector.addSelectionChangedListener(fListener);

    fTapestryTemplateSelector.createControl(fProjectTemplateGroup, columnCount);
    fTapestryTemplateSelector.addSelectionChangedListener(fListener);

  }

  private void updateEnabled(boolean flag)
  {

    fProjectTemplateGroup.setEnabled(flag);
    Control[] children = fProjectTemplateGroup.getChildren();
    for (int i = 0; i < children.length; i++)
    {
      children[i].setEnabled(flag);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  public void dispose()
  {
    super.dispose();
    fLibraryTemplateSelector.dispose();
    fComponentTemplateSelector.dispose();
    fPageTemplateSelector.dispose();
    fTapestryTemplateSelector.dispose();
    UIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fListener);
  }

  public IResource getResource()
  {
    return null;
  }
  public IRunnableWithProgress getRunnable(Object object)
  {

    if (fUseWorkspaceDefaultTemplates.getSelection())
    {
      fListener.stop();
      fLibraryTemplateSelector.loadDefault();
      fComponentTemplateSelector.loadDefault();
      fPageTemplateSelector.loadDefault();
      fTapestryTemplateSelector.loadDefault();
    }

    IProject project = null;
    if (object instanceof IProject)
    {
      project = (IProject) object;
    } else if (object instanceof IJavaProject)
    {
      project = ((IJavaProject) object).getProject();
    }

    if (project != null)
    {
      final IProject useProject = project;
      final ProjectPreferenceStore dirtyStore = fProjectPreferences;
      return new IRunnableWithProgress()
      {
        public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException
        {
          try
          {
            IFile dataFile = useProject.getFile(UIPlugin.SPINDLEUI_PREFS_FILE);

            String fullname;
            if (dataFile.exists())
            {
              fullname = dataFile.getLocation().toOSString();
            } else
            {
              fullname = useProject.getLocation().toOSString() + File.separator
                  + UIPlugin.SPINDLEUI_PREFS_FILE;
            }

            dirtyStore.setFilename(fullname);
            dirtyStore.save();

          } catch (Exception e)
          {
            UIPlugin.log(e);
            throw new InvocationTargetException(e);
          }
        }
      };
    }
    return null;
  }
  public Template getTemplate(String templateContextId)
  {
    if (fUseWorkspaceDefaultTemplates.getSelection())
    {
      String name = UIPlugin.getDefault().getPreferenceStore().getString(
          templateContextId);
      List templates = TemplateFactory.getAllTemplates(templateContextId);
      for (Iterator iter = templates.iterator(); iter.hasNext();)
      {
        Template element = (Template) iter.next();
        if (element.getName().equals(name))
          return element;
      }
      return null;

    } else
    {
      if (PageFactory.CONTEXT_TYPE.equals(templateContextId))
        return fPageTemplateSelector.getSelectedTemplate();

      if (TapestryTemplateFactory.CONTEXT_TYPE.equals(templateContextId))
        return fTapestryTemplateSelector.getSelectedTemplate();

      return null;
    }
  }

  public Template getTemplate(TemplateFactory factory)
  {
    return getTemplate(factory.getTemplateContextId());
  }

  private void validate()
  {

    IStatus resultStatus;

    if (fUseWorkspaceDefaultTemplates.getSelection())
    {
      resultStatus = validateWorkspaceSettings();
    } else
    {
      SpindleMultiStatus status = new SpindleMultiStatus();
      status.addStatus(fComponentTemplateSelector.validate());
      status.addStatus(fPageTemplateSelector.validate());
      status.addStatus(fTapestryTemplateSelector.validate());

      resultStatus = status.getMostSevere(status.getChildren());
    }
    if (resultStatus.getSeverity() != IStatus.OK)
    {
      if (fUseWorkspaceDefaultTemplates.getSelection())
      {
        SpindleStatus newResult = new SpindleStatus();
        newResult
            .setError("There are problems with the workspace defaults. Go fix them or choose project defaults instead.");
        resultStatus = newResult;
      }
      updateStatus(resultStatus);
    }
  }

  protected void updateStatus(IStatus status)
  {
    fCurrStatus = status;

    setPageComplete(fCurrStatus != null && !fCurrStatus.matches(IStatus.ERROR));
    if (fPageVisible)
    {
      applyToStatusLine(this, fCurrStatus);
    }
  }
  /**
   * check that the workspace setting are valid.
   */
  private IStatus validateWorkspaceSettings()
  {
    IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
    SpindleMultiStatus status = new SpindleMultiStatus();

    status.addStatus(XMLFileContextType.validateTemplateName(
        store,
        XMLFileContextType.APPLICATION_FILE_CONTEXT_TYPE,
        store.getString(PreferenceConstants.APP_TEMPLATE)));

    status.addStatus(XMLFileContextType.validateTemplateName(
        store,
        XMLFileContextType.LIBRARY_FILE_CONTEXT_TYPE,
        store.getString(PreferenceConstants.LIB_TEMPLATE)));

    status.addStatus(XMLFileContextType.validateTemplateName(
        store,
        XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE,
        store.getString(PreferenceConstants.COMPONENT_TEMPLATE)));

    status.addStatus(XMLFileContextType.validateTemplateName(
        store,
        XMLFileContextType.PAGE_FILE_CONTEXT_TYPE,
        store.getString(PreferenceConstants.PAGE_TEMPLATE)));

    status.addStatus(XMLFileContextType.validateTemplateName(
        store,
        XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE,
        store.getString(PreferenceConstants.TAP_TEMPLATE_TEMPLATE)));

    return status.getMostSevere(status.getChildren());

  }

}