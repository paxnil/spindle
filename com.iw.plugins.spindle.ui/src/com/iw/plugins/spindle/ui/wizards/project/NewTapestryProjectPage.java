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

package com.iw.plugins.spindle.ui.wizards.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.util.IndentingWriter;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.assist.usertemplates.XMLFileContextType;
import com.iw.plugins.spindle.ui.dialogfields.CheckBoxField;
import com.iw.plugins.spindle.ui.properties.ProjectPropertyPage;
import com.iw.plugins.spindle.ui.widgets.PreferenceTemplateSelector;
import com.iw.plugins.spindle.ui.wizards.factories.ApplicationFactory;
import com.iw.plugins.spindle.ui.wizards.factories.ITemplateSource;
import com.iw.plugins.spindle.ui.wizards.factories.PageFactory;
import com.iw.plugins.spindle.ui.wizards.factories.TapestryTemplateFactory;

/**
 * A wizard page for creating a new Tapestry web project.
 * 
 * @author glongman@intelligentworks.com
 */
public class NewTapestryProjectPage extends WizardNewProjectCreationPage
{

  class TemplateListener implements ISelectionChangedListener
  {
    public void selectionChanged(SelectionChangedEvent event)
    {
      setPageComplete(validatePage());
    }
  }
  private String fInitialContextFolderFieldValue = "context";

  private Text fProjectContextFolderField;

  private Combo fServletSpecVersionCombo;

  private CheckBoxField fInsertTapestryFilter;

  private List fReveal;

  private PreferenceTemplateSelector fApplicationTemplateSelector;

  private Group fTapestryGroup;
  private Group fTemplateGroup;

  private Listener fieldModifyListener = new Listener()
  {
    public void handleEvent(Event e)
    {
      setPageComplete(validatePage());
    }
  };

  private NewTapestryProjectWizard fWizard;
  private TemplateListener fTemplateListener;

  /* the file factories used by this wizard */
  private ApplicationFactory fAppFactory;
  private PageFactory fPageFactory;
  private TapestryTemplateFactory fTemplateFactory;
  /* the source of the actual templates to use */
  private ITemplateSource fTemplateSource;
  /**
   * @param pageName
   */
  public NewTapestryProjectPage(String pageName, NewTapestryProjectWizard wizard,
      ITemplateSource source)
  {
    super(pageName);
    fWizard = wizard;

    fInsertTapestryFilter = new CheckBoxField(UIPlugin
        .getString("new-project-wizard-page-insert-filter-servlet-2.3-and-up-only"));

    fAppFactory = new ApplicationFactory();
    fPageFactory = new PageFactory();
    fTemplateFactory = new TapestryTemplateFactory();
    fTemplateSource = source;
    fTemplateListener = new TemplateListener();

    fApplicationTemplateSelector = new PreferenceTemplateSelector(
        XMLFileContextType.APPLICATION_FILE_CONTEXT_TYPE,
        PreferenceConstants.APP_TEMPLATE,
        UIPlugin.getDefault().getPreferenceStore());
    
    fApplicationTemplateSelector.setReadOnly(true);
  }

  public void setVisible(boolean visible)
  {
    super.setVisible(visible);
    if (visible)
    {
      fWizard.entering(this);
    } else
    {
      fWizard.leaving(this);
    }
  }
  /**
   * (non-Javadoc) Method declared on IDialogPage.
   */
  public void createControl(Composite parent)
  {
    Composite wrapper = new Composite(parent, SWT.NULL);
    wrapper.setFont(parent.getFont());

    wrapper.setLayout(new GridLayout());
    wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    super.createControl(wrapper);

    Composite superComp = (Composite) getControl();
    superComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Composite composite = new Composite(wrapper, SWT.NULL);
    composite.setFont(parent.getFont());

    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));

    createTapestryGroup(composite);

    createTemplateGroup(composite);

    fApplicationTemplateSelector.load();

    setPageComplete(validatePage());
    // Show description on opening
    setErrorMessage(null);
    setMessage(null);
    setControl(wrapper);
  }

  /**
   * @param wrapper
   */
  private void createTemplateGroup(Composite parent)
  {
    fTemplateGroup = new Group(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    fTemplateGroup.setLayout(layout);
    fTemplateGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fTemplateGroup.setFont(parent.getFont());
    fTemplateGroup.setText(UIPlugin
        .getString("new-project-wizard-page-template-group-label"));

    fApplicationTemplateSelector.createControl(fTemplateGroup, 3);
    fApplicationTemplateSelector.addSelectionChangedListener(fTemplateListener);
  }

  /**
   * Creates the project name specification controls.
   * 
   * @param parent the parent composite
   */
  private final void createTapestryGroup(Composite parent)
  {
    fTapestryGroup = new Group(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    fTapestryGroup.setLayout(layout);
    fTapestryGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fTapestryGroup.setFont(parent.getFont());
    fTapestryGroup.setText(UIPlugin
        .getString("new-project-wizard-page-context-group-label"));

    // context folder label
    Label projectLabel = new Label(fTapestryGroup, SWT.NONE);
    projectLabel.setText(UIPlugin.getString("new-project-wizard-page-context-folder"));
    projectLabel.setFont(parent.getFont());

    // context folder entry field
    fProjectContextFolderField = new Text(fTapestryGroup, SWT.BORDER);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.widthHint = 250;
    fProjectContextFolderField.setLayoutData(data);
    fProjectContextFolderField.setFont(parent.getFont());

    // Set the initial value first before listener
    // to avoid handling an event during the creation.
    if (fInitialContextFolderFieldValue != null)
      fProjectContextFolderField.setText(fInitialContextFolderFieldValue);
    fProjectContextFolderField.addListener(SWT.Modify, fieldModifyListener);

    // context folder label
    Label specLabel = new Label(fTapestryGroup, SWT.NONE);
    specLabel.setText(UIPlugin.getString("new-project-wizard-page-servlet-spec"));
    specLabel.setFont(parent.getFont());

    // servlet spec version combo
    fServletSpecVersionCombo = new Combo(fTapestryGroup, SWT.READ_ONLY);
    fServletSpecVersionCombo.add(TapestryCore.SERVLET_2_4_SCHEMA);
    fServletSpecVersionCombo.add(TapestryCore.SERVLET_2_3_PUBLIC_ID);
    fServletSpecVersionCombo.add(TapestryCore.SERVLET_2_2_PUBLIC_ID);
    fServletSpecVersionCombo.setFont(parent.getFont());
    fServletSpecVersionCombo.select(1);
    fServletSpecVersionCombo.addSelectionListener(new SelectionListener()
    {
      public void widgetSelected(SelectionEvent e)
      {
        int dtdId = XMLUtil.getDTDVersion(getServletSpecPublicId());
        if (fInsertTapestryFilter != null)
          fInsertTapestryFilter.setEnabled(dtdId >= XMLUtil.DTD_SERVLET_2_3);
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {
        //do nothing
      }
    });

    Control control = fInsertTapestryFilter.getControl(fTapestryGroup);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    control.setLayoutData(data);
    control.setFont(parent.getFont());
  }

  protected boolean validatePage()
  {
    boolean superValid = super.validatePage();
    boolean nameSpecified = !"".equals(getProjectName());

    if (fTapestryGroup == null)
      return superValid;

    setGroupEnabled(fTapestryGroup, nameSpecified);
    setGroupEnabled(fTemplateGroup, nameSpecified);

    int dtdId = XMLUtil.getDTDVersion(getServletSpecPublicId());
    if (fInsertTapestryFilter != null)
      fInsertTapestryFilter.setEnabled(fServletSpecVersionCombo.isEnabled()
          && nameSpecified && dtdId >= XMLUtil.DTD_SERVLET_2_3);

    if (!superValid)
      return false;

    if (nameSpecified)
    {

      IWorkspace workspace = ResourcesPlugin.getWorkspace();

      String contextFolderContents = fProjectContextFolderField == null
          ? "" : fProjectContextFolderField.getText().trim();

      if (contextFolderContents.equals(""))
      {
        setErrorMessage(null);
        setMessage(UIPlugin.getString("new-project-wizard-page-empty-context-folder"));
        return false;
      }

      IStatus status = workspace.validateName(contextFolderContents, IResource.FOLDER);
      if (!status.isOK())
      {
        setErrorMessage(status.getMessage());
        return false;
      }

      status = fApplicationTemplateSelector.validate();
      if (!status.isOK())
      {
        setErrorMessage(status.getMessage());
        return false;
      }

    }

    setErrorMessage(null);
    setMessage(null);
    return true;
  }

  protected void setGroupEnabled(Group group, boolean flag)
  {
    Control[] children = group.getChildren();
    for (int i = 0; i < children.length; i++)
    {
      children[i].setEnabled(flag);
    }
  }

  public String getContextFolderName()
  {
    if (fProjectContextFolderField == null)
      return null;

    return fProjectContextFolderField.getText();
  }

  public String getServletSpecPublicId()
  {
    if (fServletSpecVersionCombo == null)
      return null;

    return fServletSpecVersionCombo.getItem(fServletSpecVersionCombo.getSelectionIndex());
  }

  public boolean writeTapestryRedirectFilter()
  {
    return fInsertTapestryFilter.isEnabled() && fInsertTapestryFilter.getCheckBoxValue();
  }

  // Once the java project has been created, we can setup the Tapestry stuff.
  // assumes the java project esists and is open.
  protected IRunnableWithProgress getRunnable(final IJavaProject jproject)
  {
    if (jproject != null)
    {
      return new IRunnableWithProgress()
      {
        public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException
        {
          try
          {
            configureTapestryProject(jproject, monitor);
            addTapestryNature(jproject, monitor);
          } catch (CoreException e)
          {
            throw new InvocationTargetException(e);
          }
        }

      };
    }
    return null;
  }

  protected void addTapestryNature(IJavaProject jproject, IProgressMonitor monitor) throws CoreException
  {
    IProject project = jproject.getProject();
    // store the values as properties

    project.setPersistentProperty(new QualifiedName(
        "",
        ProjectPropertyPage.PROJECT_TYPE_PROPERTY), String
        .valueOf(TapestryProject.APPLICATION_PROJECT_TYPE));
    project.setPersistentProperty(new QualifiedName(
        "",
        ProjectPropertyPage.CONTEXT_ROOT_PROPERTY), "/" + getContextFolderName());

    // now configure/deconfigure the project

    TapestryProject.addTapestryNature(jproject);
    TapestryProject prj = TapestryProject.create(jproject);
    // No longer required, library projects are deprecated
    //        prj.setProjectType(TapestryProject.APPLICATION_PROJECT_TYPE);
    prj.setWebContext("/" + getContextFolderName());

    int version = XMLUtil.getDTDVersion(getServletSpecPublicId());
    if (version == XMLUtil.UNKNOWN_DTD)
      prj.setValidateWebXML(false);

    prj.saveProperties();

  }

  /**
   * Do the setup of the Tapestry project with:
   * <p>
   * <ul>
   * <li>context folder and WEB-INF folder created</li>
   * <li>web.xml created</li>
   * <li>application file created</li>
   * <li>Home.page created</li>
   * <li>Home.html created</li>
   * 
   * @param monitor
   */
  protected void configureTapestryProject(IJavaProject jproject, IProgressMonitor monitor) throws CoreException,
      InterruptedException
  {
    fReveal = new ArrayList();
    monitor.beginTask(UIPlugin.getString("new-project-wizard-page-initializing"), 6);

    IProject underlyingProject = jproject.getProject();
    String projectName = underlyingProject.getName();

    IFolder contextFolder = underlyingProject.getFolder(getContextFolderName());
    fReveal.add(contextFolder);

    if (!contextFolder.exists())
      contextFolder.create(true, true, monitor);

    monitor.worked(1);

    IFolder webInfFolder = contextFolder.getFolder("WEB-INF");
    fReveal.add(webInfFolder);

    if (!webInfFolder.exists())
      webInfFolder.create(true, true, monitor);

    monitor.worked(1);

    configureWebXML(projectName, webInfFolder, writeTapestryRedirectFilter(), monitor);

    monitor.worked(1);

    // the project application spec
    fReveal.add(fAppFactory.createApplication(webInfFolder, fApplicationTemplateSelector
        .getSelectedTemplate(), projectName, TapestryCore
        .getString("TapestryEngine.defaultEngine"), "Home.page", monitor));

    monitor.worked(1);

    //the home page spec
    fReveal.add(fPageFactory.createPage(webInfFolder, fTemplateSource
        .getTemplate(fPageFactory), "Home", TapestryCore
        .getString("TapestryPageSpec.defaultSpec"), monitor));

    monitor.worked(1);

    // the home page template
    fReveal.add(fTemplateFactory.createTapestryTemplate(webInfFolder, fTemplateSource
        .getTemplate(fTemplateFactory), "Home", "html", monitor));

    monitor.done();
  }

  /**
   * @param contextFolder
   * @param monitor
   */
  private void configureWebXML(
      String projectName,
      IFolder webInfFolder,
      boolean writeRedirectFilter,
      IProgressMonitor monitor) throws CoreException
  {
    IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
    boolean useTabs = store.getBoolean(PreferenceConstants.FORMATTER_TAB_CHAR);
    int tabSize = store.getInt(PreferenceConstants.FORMATTER_TAB_SIZE);
    StringWriter swriter = new StringWriter();
    IndentingWriter iwriter = new IndentingWriter(swriter, useTabs, tabSize, 0, null);

    XMLUtil.writeWebDOTXML(
        projectName,
        getServletSpecPublicId(),
        writeRedirectFilter,
        iwriter);
    iwriter.flush();
    IFile webDotXML = webInfFolder.getFile("web.xml");
    fReveal.add(webDotXML);
    InputStream contents = new ByteArrayInputStream(swriter.toString().getBytes());
    webDotXML.create(contents, true, new SubProgressMonitor(monitor, 1));
  }

  /**
   *  
   */
  public IResource[] getReveal()
  {
    if (fReveal == null)
      return new IResource[0];

    return (IResource[]) fReveal.toArray(new IResource[fReveal.size()]);
  }

}