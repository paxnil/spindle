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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.internal.WorkbenchPlugin;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.ui.properties.ProjectPropertyPage;

/**
 *  A wizard page for creating a new Tapestry web project.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class NewTapestryProjectPage extends WizardNewProjectCreationPage
{
    private String fInitialContextFolderFieldValue = "context";

    private Text fProjectContextFolderField;
    private Combo fServletSpecVersionCombo;

    private List fReveal;

    private Listener fieldModifyListener = new Listener()
    {
        public void handleEvent(Event e)
        {
            setPageComplete(validatePage());
        }
    };

    /**
     * @param pageName
     */
    public NewTapestryProjectPage(String pageName)
    {
        super(pageName);
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent)
    {
        Composite wrapper = new Composite(parent, SWT.NULL);
        wrapper.setFont(parent.getFont());

        wrapper.setLayout(new GridLayout());
        wrapper.setLayoutData(new GridData(GridData.FILL_BOTH));

        super.createControl(wrapper);

        Composite composite = new Composite(wrapper, SWT.NULL);
        composite.setFont(parent.getFont());

        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));

        createTapestryGroup(composite);
        setPageComplete(validatePage());
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(wrapper);
    }

    /**
     * Creates the project name specification controls.
     *
     * @param parent the parent composite
     */
    private final void createTapestryGroup(Composite parent)
    {
        Group projectGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectGroup.setFont(parent.getFont());
        projectGroup.setText(UIPlugin.getString("new-project-wizard-page-context-group-label"));

        // context folder label
        Label projectLabel = new Label(projectGroup, SWT.NONE);
        projectLabel.setText(UIPlugin.getString("new-project-wizard-page-context-folder"));
        projectLabel.setFont(parent.getFont());

        // context folder entry field
        fProjectContextFolderField = new Text(projectGroup, SWT.BORDER);
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
        Label specLabel = new Label(projectGroup, SWT.NONE);
        specLabel.setText(UIPlugin.getString("new-project-wizard-page-servlet-spec"));
        specLabel.setFont(parent.getFont());

        // servlet spec version combo
        fServletSpecVersionCombo = new Combo(projectGroup, SWT.READ_ONLY);
        fServletSpecVersionCombo.add(TapestryCore.SERVLET_2_3_PUBLIC_ID);
        fServletSpecVersionCombo.add(TapestryCore.SERVLET_2_2_PUBLIC_ID);
        fServletSpecVersionCombo.setFont(parent.getFont());
        fServletSpecVersionCombo.select(0);
    }

    protected boolean validatePage()
    {
        boolean superValid = super.validatePage();
        boolean nameSpecified = !"".equals(getProjectName());

        if (fProjectContextFolderField != null)
            fProjectContextFolderField.setEnabled(nameSpecified);

        if (fServletSpecVersionCombo != null)
            fServletSpecVersionCombo.setEnabled(nameSpecified);

        if (!superValid)
            return false;

        IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();

        String contextFolderContents =
            fProjectContextFolderField == null ? "" : fProjectContextFolderField.getText().trim();

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

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    public IJavaProject getNewJavaProject()
    {
        return JavaCore.create(getProjectHandle());
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

    // Once the java project has been created, we can setup the Tapestry stuff.
    // assumes the java project esists and is open.
    protected IRunnableWithProgress getRunnable(final IJavaProject jproject)
    {
        if (jproject != null)
        {
            return new IRunnableWithProgress()
            {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
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

        project.setPersistentProperty(
            new QualifiedName("", ProjectPropertyPage.PROJECT_TYPE_PROPERTY),
            String.valueOf(TapestryProject.APPLICATION_PROJECT_TYPE));
        project.setPersistentProperty(
            new QualifiedName("", ProjectPropertyPage.CONTEXT_ROOT_PROPERTY),
            getContextFolderName());
        project.setPersistentProperty(new QualifiedName("", ProjectPropertyPage.LIBRARY_SPEC_PROPERTY), "");

        // now configure/deconfigure the project

        TapestryProject.addTapestryNature(jproject);
        TapestryProject prj = TapestryProject.create(jproject);
        prj.setProjectType(TapestryProject.APPLICATION_PROJECT_TYPE);
        prj.setWebContext(getContextFolderName());
        prj.saveProperties();

    }

    /**
     *  Do the setup of the Tapestry project with:
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
    protected void configureTapestryProject(IJavaProject jproject, IProgressMonitor monitor) throws CoreException
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
        configureWebXML(projectName, webInfFolder, monitor);
        monitor.worked(1);
        configureApplication(projectName, webInfFolder, monitor);
        monitor.worked(1);
        configureHomePage(webInfFolder, monitor);
        monitor.worked(1);
        configureHomeTemplate(contextFolder, monitor);
        monitor.done();
    }

    /**
     * @param contextFolder
     * @param monitor
     */
    private void configureWebXML(String projectName, IFolder webInfFolder, IProgressMonitor monitor)
        throws CoreException
    {
        StringWriter swriter = new StringWriter();
        PrintWriter writer = new PrintWriter(swriter);
        XMLUtil.writeWebDOTXML(projectName, getServletSpecPublicId(), writer);
        writer.flush();
        IFile webDotXML = webInfFolder.getFile("web.xml");
        fReveal.add(webDotXML);
        InputStream contents = new ByteArrayInputStream(swriter.toString().getBytes());
        webDotXML.create(contents, true, new SubProgressMonitor(monitor, 1));
    }

    /**
     * @param webInfFolder
     * @param monitor
     */
    private void configureApplication(String projectName, IFolder webInfFolder, IProgressMonitor monitor)
        throws CoreException
    {
        PluginApplicationSpecification spec = new PluginApplicationSpecification();
        spec.setName(projectName);
        spec.setEngineClassName(TapestryCore.getString("TapestryEngine.defaultEngine"));
        spec.setDescription("add a description");
        spec.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
        spec.setPageSpecificationPath("Home", "Home.page");
        StringWriter swriter = new StringWriter();
        PrintWriter writer = new PrintWriter(swriter);
        XMLUtil.writeApplicationSpecification(writer, spec, 0);
        writer.flush();
        IFile appFile = webInfFolder.getFile(projectName + ".application");
        fReveal.add(appFile);
        InputStream contents = new ByteArrayInputStream(swriter.toString().getBytes());
        appFile.create(contents, true, new SubProgressMonitor(monitor, 1));
    }

    private void configureHomePage(IFolder webInfFolder, IProgressMonitor monitor) throws CoreException
    {

        PluginComponentSpecification homeSpec = new PluginComponentSpecification();
        homeSpec.setPageSpecification(true);
        homeSpec.setComponentClassName(TapestryCore.getString("TapestryPageSpec.defaultSpec"));
        homeSpec.setDescription("add a description");
        homeSpec.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
        StringWriter swriter = new StringWriter();
        PrintWriter writer = new PrintWriter(swriter);
        XMLUtil.writeSpecification(writer, homeSpec, 0);
        writer.flush();
        IFile pageFile = webInfFolder.getFile("Home.page");
        fReveal.add(pageFile);
        InputStream contents = new ByteArrayInputStream(swriter.toString().getBytes());
        pageFile.create(contents, true, new SubProgressMonitor(monitor, 1));
    }

    /**
     * @param webInfFolder
     * @param monitor
     */
    private void configureHomeTemplate(IFolder contextFolder, IProgressMonitor monitor) throws CoreException
    {
        IPreferenceStore pstore = UIPlugin.getDefault().getPreferenceStore();
        String source = pstore.getString(PreferenceConstants.P_HTML_TO_GENERATE);
        IFile pageFile = contextFolder.getFile("Home.html");
        fReveal.add(pageFile);
        InputStream contents = new ByteArrayInputStream(source.getBytes());
        pageFile.create(contents, true, new SubProgressMonitor(monitor, 1));
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
