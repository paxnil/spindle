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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathsBlock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.internal.WorkbenchPlugin;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryProject;

/**
 *  A wizard page for creating a new Tapestry web project.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class NewTapestryProjectPage extends WizardNewProjectCreationPage
{
    private String fInitialSourceFolderFieldValue = "src";
    private String fInitialContextFolderFieldValue = "context";

    private Text fProjectSourceFolderField;
    private Text fProjectContextFolderField;

    private TapestryProject fTapestryProject;

    private BuildPathsBlock fBuildPathsBlock;

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
        IStatusChangeListener listener = new IStatusChangeListener()
        {
            public void statusChanged(IStatus status)
            {
                    //do nothing
    }
        };

        fBuildPathsBlock = new BuildPathsBlock(listener, 0);
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

//        // src folder label
//        Label srcFolderLabel = new Label(projectGroup, SWT.NONE);
//        srcFolderLabel.setText(UIPlugin.getString("new-project-wizard-page-src-folder"));
//        srcFolderLabel.setFont(parent.getFont());
//
//        // src folder entry field
//        fProjectSourceFolderField = new Text(projectGroup, SWT.BORDER);
//        GridData data = new GridData(GridData.FILL_HORIZONTAL);
//        data.widthHint = 250;
//        fProjectSourceFolderField.setLayoutData(data);
//        fProjectSourceFolderField.setFont(parent.getFont());
//
//        // Set the initial value first before listener
//        // to avoid handling an event during the creation.
//        if (fInitialSourceFolderFieldValue != null)
//            fProjectSourceFolderField.setText(fInitialSourceFolderFieldValue);
//        fProjectSourceFolderField.addListener(SWT.Modify, fieldModifyListener);

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
    }

    protected boolean validatePage()
    {
        boolean superValid = super.validatePage();
        boolean nameSpecified = !"".equals(getProjectName());

//        if (fProjectSourceFolderField != null)
//            fProjectSourceFolderField.setEnabled(nameSpecified);
        if (fProjectContextFolderField != null)
            fProjectContextFolderField.setEnabled(nameSpecified);

        if (!superValid)
            return false;

        IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();

//        String srcFolderContents = fProjectSourceFolderField == null ? "" : fProjectSourceFolderField.getText().trim();
//        if (srcFolderContents.equals(""))
//        {
//            setErrorMessage(null);
//            setMessage(UIPlugin.getString("new-project-wizard-page-empty-src-folder"));
//
//            return false;
//        }
//
//        IStatus status = workspace.validateName(srcFolderContents, IResource.FOLDER);
//        if (!status.isOK())
//        {
//            setErrorMessage(status.getMessage());
//            return false;
//        }

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

    protected void initBuildPaths()
    {
        fBuildPathsBlock.init(getNewJavaProject(), null, null);
        
    }

}
