package com.iw.plugins.spindle.ui.properties;
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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.PropertyPage;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.ui.util.Revealer;

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

public class ProjectPropertyPage extends PropertyPage
{

    abstract class Validator implements ISelectionValidator
    {
        protected boolean isOnOutputPath(IJavaProject project, IPath candidate)
        {
            try
            {
                IPath output = project.getOutputLocation();
                return pathCheck(output, candidate);
            } catch (JavaModelException e)
            {}
            return false;
        }
        protected boolean isOnSourcePath(IJavaProject project, IPath candidate)
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
        protected boolean pathCheck(IPath existing, IPath candidate)
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
    }

    class ApplicationContextValidator extends Validator
    {
        public String isValidString(String value)
        {
            return isValid(new Path(value));
        }

        public String isValid(Object selection)
        {
            try
            {
                IJavaProject jproject = getJavaProject();
                IProject project = (IProject) jproject.getAdapter(IProject.class);
                Path selected = (Path) selection;

                if (!project.getFolder(selected).exists())
                {
                    return UIPlugin.getString("property-page-wrong-project");
                }
                if (isOnOutputPath(jproject, selected))
                {
                    return UIPlugin.getString("property-page-output-folder");
                }
                if (isOnSourcePath(jproject, selected))
                {
                    return UIPlugin.getString("property-page-no-source-path");
                }
                return null;
            } catch (CoreException e)
            {
                return "error occured!";
            }
        }
    }

    class DialogContextValidator extends Validator
    {
        public String isValidString(String value)
        {
            return isValid(new Path(value));
        }

        public String isValid(Object selection)
        {
            try
            {
                IPath selected = (IPath) selection;
                IWorkspaceRoot root = UIPlugin.getWorkspace().getRoot();

                IProject selectedProject = root.getProject(selected.segment(0));

                IJavaProject jproject = getJavaProject();
                IProject project = (IProject) jproject.getAdapter(IProject.class);

                if (!project.equals(selectedProject))
                {
                    return UIPlugin.getString("property-page-wrong-project");
                }

                selected = (IPath) selected.removeFirstSegments(0);
                if (isOnOutputPath(jproject, selected))
                {
                    return UIPlugin.getString("property-page-output-folder");
                }
                if (isOnSourcePath(jproject, selected))
                {
                    return UIPlugin.getString("property-page-no-source-path");
                }
                return null;
            } catch (CoreException e)
            {
                return "error occured!";
            }
        }
    }

    private static final String PROJECT_TYPE_PROPERTY = TapestryCore.PLUGIN_ID + ".project-type";
    private static final String CONTEXT_ROOT_PROPERTY = TapestryCore.PLUGIN_ID + ".context-root";
    private static final String LIBRARY_SPEC_PROPERTY = TapestryCore.PLUGIN_ID + ".library-spec";

    private static final int TEXT_FIELD_WIDTH = 30;

    private Text fOwnerText;

    private Button fIsTapestryProjectCheck;
    private Combo fProjectTypeCombo;
    private Label fContextRootLabel;
    private Text fWebContextRoot;
    private Button fBrowseContextRoot;
    private Label fLibrarySpecLabel;
    private Text fLibrarySpec;
    private Button fBrowseLibrarySpecification;

    private ApplicationContextValidator fContextValidator = new ApplicationContextValidator();
    private DialogContextValidator fDialogContextValidator = new DialogContextValidator();

    /**
     * Constructor for SamplePropertyPage.
     */
    public ProjectPropertyPage()
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

        fIsTapestryProjectCheck = new Button(composite, SWT.CHECK | SWT.LEFT);
        fIsTapestryProjectCheck.setText(UIPlugin.getString("property-page-is-tapestry-project"));
        fIsTapestryProjectCheck.setEnabled(true);

        fIsTapestryProjectCheck.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                updateApplyButton();
                checkEnabled();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                //do nothing
            }
        });
        try
        {
            fIsTapestryProjectCheck.setSelection(getJavaProject().getProject().hasNature(TapestryCore.NATURE_ID));
        } catch (CoreException ex)
        {
            TapestryCore.log(ex.getMessage());
        }
        fProjectTypeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);

        // commented out until a real library build can occur

        //        fProjectTypeCombo.setItems(
        //            new String[] {
        //                UIPlugin.getString("property-page-project-type-application"),
        //                UIPlugin.getString("property-page-project-type-library")});
        fProjectTypeCombo.setItems(new String[] { UIPlugin.getString("property-page-project-type-application")});

        fProjectTypeCombo.select(getProjectType());
        fProjectTypeCombo.setEnabled(fIsTapestryProjectCheck.getSelection());
        fProjectTypeCombo.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                updateApplyButton();
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
        if (fBrowseLibrarySpecification != null)
        {
            boolean canEnable = fIsTapestryProjectCheck.getSelection();

            fProjectTypeCombo.setEnabled(canEnable);

            boolean appSelected = fProjectTypeCombo.getSelectionIndex() == TapestryProject.APPLICATION_PROJECT_TYPE;
            boolean libSelected = fProjectTypeCombo.getSelectionIndex() == TapestryProject.LIBRARY_PROJECT_TYPE;
            boolean showApp = appSelected && canEnable;
            boolean showLib = libSelected && canEnable;

            fContextRootLabel.setEnabled(showApp);
            fWebContextRoot.setEnabled(showApp);
            fBrowseContextRoot.setEnabled(showApp);
            fLibrarySpecLabel.setEnabled(showLib);
            fLibrarySpec.setEnabled(showLib);
            fBrowseLibrarySpecification.setEnabled(showLib);
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
            fIsTapestryProjectCheck.getSelection()
                && fProjectTypeCombo.getSelectionIndex() == TapestryProject.APPLICATION_PROJECT_TYPE;

        Composite fieldGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        fieldGroup.setLayout(layout);
        fieldGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fContextRootLabel = new Label(fieldGroup, SWT.NONE);
        fContextRootLabel.setText(UIPlugin.getString("property-page-contextRoot"));
        fContextRootLabel.setEnabled(true);

        fWebContextRoot = new Text(fieldGroup, SWT.BORDER);
        fWebContextRoot.setEditable(false);
        fWebContextRoot.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
        fWebContextRoot.setLayoutData(data);
        fWebContextRoot.setText(this.getContextRootLocation());
        fWebContextRoot.setEnabled(isEnabled);
        fWebContextRoot.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                updateApplyButton();
            }
        });

        fBrowseContextRoot = new Button(fieldGroup, SWT.PUSH);
        fBrowseContextRoot.setText(UIPlugin.getString("browse-button-label"));
        fBrowseContextRoot.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent evt)
            {
                String newValue = chooseContextFolder();
                if (newValue != null)
                {
                    fWebContextRoot.setText(newValue);
                }
            }
        });

        fBrowseContextRoot.setEnabled(isEnabled);

    }

    private void addLibrarySection(Composite parent)
    {
        Composite composite = createDefaultComposite(parent);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        boolean isEnabled =
            fIsTapestryProjectCheck.getSelection()
                && fProjectTypeCombo.getSelectionIndex() == TapestryProject.LIBRARY_PROJECT_TYPE;

        Composite fieldGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        fieldGroup.setLayout(layout);
        fieldGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fLibrarySpecLabel = new Label(fieldGroup, SWT.NONE);
        fLibrarySpecLabel.setText(UIPlugin.getString("property-page-project-library-spec"));
        fLibrarySpecLabel.setEnabled(true);

        fLibrarySpec = new Text(fieldGroup, SWT.BORDER);
        fLibrarySpec.setEditable(false);
        fLibrarySpec.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
        fLibrarySpec.setLayoutData(data);
        fLibrarySpec.setText(this.getLibrarySpecLocation());
        fLibrarySpec.setEnabled(isEnabled);
        fLibrarySpec.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                updateApplyButton();
            }
        });

        fBrowseLibrarySpecification = new Button(fieldGroup, SWT.PUSH);
        fBrowseLibrarySpecification.setText(UIPlugin.getString("browse-button-label"));
        fBrowseLibrarySpecification.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent evt)
            {
                String newValue = chooseLibraryFile();
                if (newValue != null)
                {
                    fLibrarySpec.setText(newValue);
                }
            }
        });

        fBrowseLibrarySpecification.setEnabled(isEnabled);
    }

    public boolean isValid()
    {
        setErrorMessage(null);

        IJavaProject jproject = null;
        try
        {
            jproject = getJavaProject();
        } catch (CoreException e)
        {
            UIPlugin.log(e);
        }

        if (jproject == null)
        {
            setErrorMessage(UIPlugin.getString("property-page-no-java-project"));
            disableAll();
            return true;
        }

        IProject project = jproject.getProject();

        try
        {
            // old spindle and new spindle can't co-exist!
            if (project.hasNature("com.iw.plugins.spindle.project.TapestryProject"))
            {
                setErrorMessage(UIPlugin.getString("property-page-conflicts-with-old-spindle"));
                disableAll();
                return true;
            }
        } catch (CoreException e1)
        {
            UIPlugin.log(e1);
        }

        if (!fIsTapestryProjectCheck.getSelection())
        {
            return true;
        }
        int type = fProjectTypeCombo.getSelectionIndex();
        switch (type)
        {
            case TapestryProject.APPLICATION_PROJECT_TYPE :
                String wcroot = fWebContextRoot.getText();
                if ("/context".equals(wcroot.trim()))
                {
                    return true;
                }
                String badApp = fContextValidator.isValidString((String) wcroot);
                if (badApp != null)
                {
                    setErrorMessage(badApp);
                    return false;
                }
                break;

            case TapestryProject.LIBRARY_PROJECT_TYPE :
                String libFile = fLibrarySpec.getText();
                String badLib = validateLibraryPath(libFile);
                if (badLib != null)
                {
                    setErrorMessage(badLib);
                    return false;
                }
            default :
                break;
        }
        return true;
    }

    //called by isValid() disable if the project is already an old (1.1.X) Spindle project
    private void disableAll()
    {
        fIsTapestryProjectCheck.setEnabled(false);
        fProjectTypeCombo.setEnabled(false);
        fContextRootLabel.setEnabled(false);
        fWebContextRoot.setEnabled(false);
        fBrowseContextRoot.setEnabled(false);
        fLibrarySpecLabel.setEnabled(false);
        fLibrarySpec.setEnabled(false);
        fBrowseLibrarySpecification.setEnabled(false);
    }

    private String validateLibraryPath(String value)
    {
        if (value == null || value.trim().length() == 0)
        {
            return UIPlugin.getString("property-page-lib-not-empty");
        }
        if (!value.endsWith(".library"))
        {
            return UIPlugin.getString("property-page-must-be-library");
        }
        return null;
    }

    protected String chooseContextFolder()
    {
        IContainer project = (IContainer) getElement().getAdapter(IContainer.class);
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), project, true, "");
        dialog.setValidator(fDialogContextValidator);
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

    protected String chooseLibraryFile()
    {
        IJavaProject jproject = null;
        try
        {
            jproject = getJavaProject();
        } catch (CoreException e)
        {
            UIPlugin.log(e);
        }
        if (jproject == null)
        {
            return fLibrarySpec.getText();
        }
        LibrarySearchDialog dialog =
            new LibrarySearchDialog(
                getShell(),
                jproject,
                UIPlugin.getString("property-page-library-dialog-window-title"),
                UIPlugin.getString("property-page-library-dialog-description"));
        if (dialog.open() == LibrarySearchDialog.OK)
        {
            return dialog.getResult();
        }
        return null;
    }

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
        fWebContextRoot.setText("/context");
    }

    public boolean performOk()
    {
        // store the values as properties
        try
        {
            IResource resource = (IResource) getElement();
            resource.setPersistentProperty(
                new QualifiedName("", PROJECT_TYPE_PROPERTY),
                new Integer(fProjectTypeCombo.getSelectionIndex()).toString());
            resource.setPersistentProperty(new QualifiedName("", CONTEXT_ROOT_PROPERTY), fWebContextRoot.getText());
            resource.setPersistentProperty(new QualifiedName("", LIBRARY_SPEC_PROPERTY), fLibrarySpec.getText());
        } catch (CoreException e)
        {}
        // now configure/deconfigure the project
        try
        {
            if (fIsTapestryProjectCheck.getSelection())
            {
                TapestryProject.addTapestryNature(getJavaProject());
                TapestryProject prj = getTapestryProject();
                switch (fProjectTypeCombo.getSelectionIndex())
                {
                    case TapestryProject.APPLICATION_PROJECT_TYPE :
                        prj.setProjectType(TapestryProject.APPLICATION_PROJECT_TYPE);
                        String projectName = prj.getProject().getName();
                        String temp = fWebContextRoot.getText();
                        createFolderIfRequired(projectName + temp);
                        prj.setWebContext(temp);
                        break;

                    case TapestryProject.LIBRARY_PROJECT_TYPE :
                        prj.setProjectType(TapestryProject.LIBRARY_PROJECT_TYPE);
                        prj.setLibrarySpecPath(fLibrarySpec.getText());
                        break;
                }
                prj.saveProperties();

            } else
            {
                TapestryProject.removeTapestryNature(getJavaProject());
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
        try
        {
            if (!folder.exists())
            {

                folder.create(false, true, null);

            }
            Revealer.selectAndReveal(new StructuredSelection(folder), UIPlugin.getDefault().getActiveWorkbenchWindow());
        } catch (CoreException e)
        {
            TapestryCore.log(e);
            System.err.println(e.getStatus().getMessage());
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
