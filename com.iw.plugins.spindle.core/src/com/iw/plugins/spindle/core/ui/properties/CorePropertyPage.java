package com.iw.plugins.spindle.core.ui.properties;
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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.PropertyPage;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;

/**
 * Property Page that is added to all Java Projects.
 * <p>
 *  Allows users to add/remove the Tapestry Project nature to any Java Project easily.
 * </p>
 * <p>
 * Also allows users to designate two non source folders as application root and servlet 
 * context root.
 * </p>
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */

public class CorePropertyPage extends PropertyPage
{

    private static final String IS_TAPESTRY_PROJECT = "property-page-is-tapestry-project";
    private static final String IGNORE1 = "property-page-ignore1";
    private static final String IGNORE2 = "property-page-ignore2";
    private static final String APP_ROOT = "property-page-appRoot";
    private static final String CONTEXT_ROOT = "property-page-contextRoot";
    private static final String LIBRARY_SPEC = "property-page-project-library-spec";
    private static final String BROWSE_BUTTON_LABEL = "browse-button-label";
    private static final String WRONG_PROJECT = "property-page-wrong-project";
    private static final String OUTPUT_FOLDER_ERROR = "property-page-output-folder";
    private static final String SOURCE_PATH_ERROR = "property-page-source-path";
    private static final String APP_TYPE = "property-page-project-type-application";

    private static final String LIB_TYPE = "property-page-project-type-library";

    private static final String PROJECT_TYPE_PROPERTY = TapestryCore.PLUGIN_ID + ".project-type";
    private static final String CONTEXT_ROOT_PROPERTY = TapestryCore.PLUGIN_ID + ".context-root";
    private static final String LIBRARY_SPEC_PROPERTY = TapestryCore.PLUGIN_ID + ".library-spec";

    private static final int TEXT_FIELD_WIDTH = 30;

    private Text ownerText;

    private Button isTapestryProjectCheck;
    private Combo projectTypeCombo;
    private Label contextRootLabel;
    private Text webContextRoot;
    private Button browseContextRoot;
    private Label librarySpecLabel;
    private Text librarySpec;
    private Button browseLibrarySpecification;

    private ISelectionValidator chooseValidator = new ISelectionValidator()
    {

        public String isValid(Object selection)
        {
            try
            {
                IJavaProject jproject = getJavaProject();
                IProject project = (IProject) jproject.getAdapter(IProject.class);
                Path selected = (Path) selection;
                if (!selected.segment(0).equals(project.getName()))
                {
                    return TapestryCore.getString(WRONG_PROJECT);
                }
                if (isOnOutputPath(jproject, selected))
                {
                    return TapestryCore.getString(OUTPUT_FOLDER_ERROR);
                }
                if (isOnSourcePath(jproject, selected))
                {
                    return TapestryCore.getString(SOURCE_PATH_ERROR);
                }
                return null;
            } catch (CoreException e)
            {
                return "error occured!";
            }
        }

        private boolean isOnOutputPath(IJavaProject project, Path candidate)
        {
            try
            {
                IPath output = project.getOutputLocation();
                return pathCheck(output, candidate);
            } catch (JavaModelException e)
            {}
            return false;
        }

        private boolean isOnSourcePath(IJavaProject project, Path candidate)
        {
            try
            {
                IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
                for (int i = 0; i < roots.length; i++)
                {
                    if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE)
                    {
                        IPath rootpath = roots[i].getUnderlyingResource().getFullPath();
                        if (pathCheck(rootpath, candidate))
                        {
                            return true;
                        }
                    }

                }
            } catch (JavaModelException e)
            {}
            return false;
        }

        private boolean pathCheck(IPath existing, IPath candidate)
        {
            if (existing.equals(candidate))
            {
                return true;
            }
            if (candidate.segmentCount() < existing.segmentCount())
            {
                return false;
            }
            return existing.matchingFirstSegments(candidate) == existing.segmentCount();
        }

    };

    /**
     * Constructor for SamplePropertyPage.
     */
    public CorePropertyPage()
    {
        super();
    }

    private void addFirstSection(Composite parent)
    {
        Composite composite = createDefaultComposite(parent);

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        isTapestryProjectCheck = new Button(composite, SWT.CHECK | SWT.LEFT);
        isTapestryProjectCheck.setText(TapestryCore.getString(IS_TAPESTRY_PROJECT));
        isTapestryProjectCheck.setEnabled(true);

        isTapestryProjectCheck.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                checkEnabled();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                //do nothing
            }
        });
        try
        {
            isTapestryProjectCheck.setSelection(getJavaProject().getProject().hasNature(TapestryCore.NATURE_ID));
        } catch (CoreException ex)
        {
            TapestryCore.log(ex.getMessage());
        }
        projectTypeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        projectTypeCombo.setItems(new String[] { TapestryCore.getString(APP_TYPE), TapestryCore.getString(LIB_TYPE)});
        projectTypeCombo.select(getProjectType());
        projectTypeCombo.setEnabled(isTapestryProjectCheck.getSelection());
        projectTypeCombo.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                checkEnabled();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                // do nothing
            }
        });
    }

    private void checkEnabled()
    {
        if (browseLibrarySpecification != null)
        {
            boolean canEnable = isTapestryProjectCheck.getSelection();

            projectTypeCombo.setEnabled(canEnable);

            boolean appSelected = projectTypeCombo.getSelectionIndex() == TapestryProject.APPLICATION_PROJECT_TYPE;
            boolean libSelected = projectTypeCombo.getSelectionIndex() == TapestryProject.LIBRARY_PROJECT_TYPE;
            boolean showApp = appSelected && canEnable;
            boolean showLib = libSelected && canEnable;

            contextRootLabel.setEnabled(showApp);
            webContextRoot.setEnabled(showApp);
            browseContextRoot.setEnabled(showApp);
            librarySpecLabel.setEnabled(showLib);
            librarySpec.setEnabled(showLib);
            browseLibrarySpecification.setEnabled(showLib);
        }
    }

    private void addSeparator(Composite parent)
    {
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        separator.setLayoutData(gridData);
    }

    private void addApplicationSection(Composite parent)
    {
        Composite composite = createDefaultComposite(parent);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        boolean isEnabled =
            isTapestryProjectCheck.getSelection()
                && projectTypeCombo.getSelectionIndex() == TapestryProject.APPLICATION_PROJECT_TYPE;

        Composite fieldGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        fieldGroup.setLayout(layout);
        fieldGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        contextRootLabel = new Label(fieldGroup, SWT.NONE);
        contextRootLabel.setText(TapestryCore.getString(CONTEXT_ROOT));
        contextRootLabel.setEnabled(true);

        webContextRoot = new Text(fieldGroup, SWT.BORDER);
        webContextRoot.setEditable(false);
        webContextRoot.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
        webContextRoot.setLayoutData(data);
        webContextRoot.setText(this.getContextRootLocation());
        webContextRoot.setEnabled(isEnabled);

        browseContextRoot = new Button(fieldGroup, SWT.PUSH);
        browseContextRoot.setText(TapestryCore.getString(BROWSE_BUTTON_LABEL));
        browseContextRoot.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent evt)
            {
                String newValue = chooseFolder();
                if (newValue != null)
                {
                    webContextRoot.setText(newValue);
                }
            }
        });

        browseContextRoot.setEnabled(isEnabled);

    }

    private void addLibrarySection(Composite parent)
    {
        Composite composite = createDefaultComposite(parent);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        boolean isEnabled =
            isTapestryProjectCheck.getSelection() && projectTypeCombo.getSelectionIndex() == TapestryProject.LIBRARY_PROJECT_TYPE;

        Composite fieldGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        fieldGroup.setLayout(layout);
        fieldGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        librarySpecLabel = new Label(fieldGroup, SWT.NONE);
        librarySpecLabel.setText(TapestryCore.getString(LIBRARY_SPEC));
        librarySpecLabel.setEnabled(true);

        librarySpec = new Text(fieldGroup, SWT.BORDER);
        librarySpec.setEditable(false);
        librarySpec.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
        librarySpec.setLayoutData(data);
        librarySpec.setText(this.getLibrarySpecLocation());
        librarySpec.setEnabled(isEnabled);

        browseLibrarySpecification = new Button(fieldGroup, SWT.PUSH);
        browseLibrarySpecification.setText(TapestryCore.getString(BROWSE_BUTTON_LABEL));
        browseLibrarySpecification.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent evt)
            {
                //                String newValue = chooseFolder();
                //                if (newValue != null)
                //                {
                //                    webContextRoot.setText(newValue);
                //                }
            }
        });

        browseLibrarySpecification.setEnabled(isEnabled);
    }

    protected String chooseFolder()
    {
        IContainer project = (IContainer) getElement().getAdapter(IContainer.class);
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), project, true, "");
        dialog.setValidator(chooseValidator);
        dialog.showClosedProjects(false);
        if (dialog.open() == ContainerSelectionDialog.OK)
        {
            Object[] result = dialog.getResult();
            IPath selected = (IPath) result[0];
            selected = selected.removeFirstSegments(1);
            return selected.makeAbsolute().toString();
        }
        return null;
    }

    //    protected String getAppRootLocation()
    //    {
    //        String result = "/";
    //        try
    //        {
    //            QualifiedName key = new QualifiedName("", APP_ROOT_PROPERTY);
    //            TapestryProject prj = getTapestryProject();
    //            if (prj != null)
    //            {
    //                result = prj.getAppRoot();
    //                if (result == null || "".equals(result.trim()))
    //                {
    //                    result = getPropertyFromWorkspace(key);
    //                }
    //            } else
    //            {
    //                result = getPropertyFromWorkspace(key);
    //            }
    //        } catch (CoreException ex)
    //        {}
    //        return result;
    //    }

    private int getIntPropertyFromWorkspace(QualifiedName key) throws CoreException
    {
        String result = getPropertyFromWorkspace(key);
        try
        {
            return new Integer(result).intValue();
        } catch (NumberFormatException e)
        {
            return TapestryProject.APPLICATION_PROJECT_TYPE;
        }
    }

    private String getPropertyFromWorkspace(QualifiedName key) throws CoreException
    {
        String result = ((IResource) getElement()).getPersistentProperty(key);
        if (result == null)
        {
            throw new CoreException(new Status(IStatus.ERROR, TapestryCore.PLUGIN_ID, 0, "not found", null));
        }
        return result;
    }

    protected int getProjectType()
    {
        int result = TapestryProject.APPLICATION_PROJECT_TYPE;
        try
        {
            QualifiedName key = new QualifiedName("", PROJECT_TYPE_PROPERTY);
            TapestryProject prj = getTapestryProject();
            if (prj != null)
            {
                result = prj.getProjectType();

                if (result == -1)
                {
                    result = getIntPropertyFromWorkspace(key);
                }
            } else
            {
                result = getIntPropertyFromWorkspace(key);
            }
        } catch (CoreException ex)
        {}
        return result;

    }

    protected String getContextRootLocation()
    {
        String result = "/context";
        try
        {
            QualifiedName key = new QualifiedName("", CONTEXT_ROOT_PROPERTY);
            TapestryProject prj = getTapestryProject();
            if (prj != null)
            {
                result = prj.getWebContext();
                if (result == null || "".equals(result.trim()))
                {
                    result = getPropertyFromWorkspace(key);
                }
            } else
            {
                result = getPropertyFromWorkspace(key);
            }

        } catch (CoreException ex)
        {}
        return result;
    }

    protected String getLibrarySpecLocation()
    {
        String result = "";
        try
        {
            QualifiedName key = new QualifiedName("", LIBRARY_SPEC_PROPERTY);
            TapestryProject prj = getTapestryProject();
            if (prj != null)
            {
                result = prj.getLibrarySpecPath();
                if (result == null || "".equals(result.trim()))
                {
                    result = getPropertyFromWorkspace(key);
                }
            } else
            {
                result = getPropertyFromWorkspace(key);
            }

        } catch (CoreException ex)
        {}
        return result;
    }

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);

        addFirstSection(composite);
        addSeparator(composite);
        addApplicationSection(composite);
        addSeparator(composite);
        addLibrarySection(composite);
        return composite;
    }

    private Composite createDefaultComposite(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NULL);
        return composite;
    }

    protected void performDefaults()
    {
        webContextRoot.setText("/context");
    }

    public boolean performOk()
    {
        // store the values as properties
        try
        {
            IResource resource = (IResource) getElement();
            resource.setPersistentProperty(
                new QualifiedName("", PROJECT_TYPE_PROPERTY),
                new Integer(projectTypeCombo.getSelectionIndex()).toString());
            resource.setPersistentProperty(new QualifiedName("", CONTEXT_ROOT_PROPERTY), webContextRoot.getText());
            resource.setPersistentProperty(new QualifiedName("", LIBRARY_SPEC_PROPERTY), librarySpec.getText());
        } catch (CoreException e)
        {}
        // now configure/deconfigure the project
        try
        {
            if (isTapestryProjectCheck.getSelection())
            {
                WorkspaceModifyOperation op = new WorkspaceModifyOperation()
                {
                    protected void execute(IProgressMonitor monitor)
                        throws CoreException, InvocationTargetException, InterruptedException
                    {
                        TapestryProject.addTapestryNature(getJavaProject());
                        TapestryProject prj = getTapestryProject();
                        switch (projectTypeCombo.getSelectionIndex())
                        {
                            case TapestryProject.APPLICATION_PROJECT_TYPE :
                                prj.setProjectType(TapestryProject.APPLICATION_PROJECT_TYPE);
                                String projectName = prj.getProject().getName();
                                String temp = webContextRoot.getText();
                                createFolderIfRequired(projectName + temp);
                                prj.setWebContext(temp);
                                break;

                            case TapestryProject.LIBRARY_PROJECT_TYPE :
                                prj.setProjectType(TapestryProject.LIBRARY_PROJECT_TYPE);
                                prj.setLibrarySpecPath(librarySpec.getText());
                                break;
                        }
                        prj.saveProperties();
                    }
                };
                op.run(new NullProgressMonitor());

            } else
            {
                WorkspaceModifyOperation op = new WorkspaceModifyOperation()
                {
                    protected void execute(IProgressMonitor monitor)
                        throws CoreException, InvocationTargetException, InterruptedException
                    {
                        TapestryProject.removeTapestryNature(getJavaProject());
                    }
                };
                op.run(new NullProgressMonitor());

            }
        } catch (Exception ex)
        {
            TapestryCore.log(ex.getMessage());
        }
        return true;
    }

    private void createFolderIfRequired(String value)
    {
        IPath path = new Path(value);
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IFolder folder = (IFolder) root.getFolder(path);
        if (!folder.exists())
        {
            try
            {
                folder.create(false, true, null);
            } catch (CoreException e)
            {
                TapestryCore.log(e);
                System.err.println(e.getStatus().getMessage());
            }
        }
    }

    /* helper methods */
    protected IJavaProject getJavaProject() throws CoreException
    {
        IProject project = (IProject) (this.getElement().getAdapter(IProject.class));
        return (IJavaProject) (project.getNature(JavaCore.NATURE_ID));
    }
    protected TapestryProject getTapestryProject() throws CoreException
    {
        return TapestryProject.create(getJavaProject());
    }

}
