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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
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
 * Allows users to add/remove the Tapestry Project nature to any Java Project
 * easily.
 * </p>
 * <p>
 * Also allows users to designate two non source folders as application root and
 * servlet context root.
 * </p>
 * 
 * @author glongman@gmail.com
 *  
 */

public class ProjectPropertyPage extends PropertyPage
{

  private static boolean DEBUG = false;

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

  //    class ApplicationContextValidator extends Validator
  //    {
  //        public String isValidString(String value)
  //        {
  //            return isValid(new Path(value));
  //        }
  //
  //        public String isValid(Object selection)
  //        {
  //            try
  //            {
  //                IJavaProject jproject = getJavaProject();
  //                IProject project = (IProject) jproject.getAdapter(IProject.class);
  //                Path selected = (Path) selection;
  //
  //                if (!project.getFolder(selected).exists())
  //                {
  //                    return UIPlugin.getString("property-page-wrong-project");
  //                }
  //                if (isOnOutputPath(jproject, selected))
  //                {
  //                    return UIPlugin.getString("property-page-output-folder");
  //                }
  //                if (isOnSourcePath(jproject, selected))
  //                {
  //                    return UIPlugin.getString("property-page-no-source-path");
  //                }
  //                return null;
  //            } catch (CoreException e)
  //            {
  //                return "error occured!";
  //            }
  //        }
  //    }

  class DialogContextValidator extends Validator
  {

    public String isValid(Object selection)
    {
      try
      {
        IPath selected = (IPath) selection;

        if (DEBUG)
          UIPlugin.log("validation path: " + selected);

        IWorkspaceRoot root = UIPlugin.getWorkspace().getRoot();

        IProject selectedProject = root.getProject(selected.segment(0));

        IJavaProject jproject = getJavaProject();
        IProject project = (IProject) jproject.getAdapter(IProject.class);

        if (!project.equals(selectedProject))
        {
          if (DEBUG)
            UIPlugin.log("validation failed: wrong project");
          return UIPlugin.getString("property-page-wrong-project");
        }

        selected = (IPath) selected.removeFirstSegments(0);
        if (isOnOutputPath(jproject, selected))
        {
          if (DEBUG)
            UIPlugin.log("validation failed: path is in the compiler output folder");
          return UIPlugin.getString("property-page-output-folder");
        }
        if (isOnSourcePath(jproject, selected))
        {
          if (DEBUG)
            UIPlugin.log("validation failed: path is in the java source path");
          return UIPlugin.getString("property-page-no-source-path");
        }

        if (DEBUG)
          UIPlugin.log("validation passed");
        return null;
      } catch (CoreException e)
      {
        return "error occured!";
      }
    }
  }

  public static final String PROJECT_TYPE_PROPERTY = TapestryCore.PLUGIN_ID
      + ".project-type";
  public static final String CONTEXT_ROOT_PROPERTY = TapestryCore.PLUGIN_ID
      + ".context-root";
  public static final String VALIDATE_WEBXML_PROPERTY = TapestryCore.PLUGIN_ID
      + ".validate-web-xml";

  private static final int TEXT_FIELD_WIDTH = 30;

  private Text fOwnerText;

  private Button fIsTapestryProjectCheck;
  private Label fContextRootLabel;
  private Text fWebContextRoot;
  private Button fBrowseContextRoot;
  private Button fValidateWebXML;

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
    fIsTapestryProjectCheck.setText(UIPlugin
        .getString("property-page-is-tapestry-project"));
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
      fIsTapestryProjectCheck.setSelection(getJavaProject().getProject().hasNature(
          TapestryCore.NATURE_ID));
    } catch (CoreException ex)
    {
      TapestryCore.log(ex.getMessage());
    }
  }

  private void checkEnabled()
  {
    boolean enable = fIsTapestryProjectCheck.getSelection();
    fContextRootLabel.setEnabled(enable);
    fWebContextRoot.setEnabled(enable);
    fBrowseContextRoot.setEnabled(enable);
    fValidateWebXML.setEnabled(enable);
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
    Font font = parent.getFont();
    Composite composite = createDefaultComposite(parent);
    composite.setFont(font);

    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    boolean isEnabled = fIsTapestryProjectCheck.getSelection();

    Composite fieldGroup = new Composite(composite, SWT.NONE);
    fieldGroup.setFont(font);
    layout = new GridLayout();
    layout.numColumns = 3;
    fieldGroup.setLayout(layout);
    fieldGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    fContextRootLabel = new Label(fieldGroup, SWT.NONE);
    fContextRootLabel.setFont(font);
    fContextRootLabel.setText(UIPlugin.getString("property-page-contextRoot"));
    fContextRootLabel.setEnabled(true);

    fWebContextRoot = new Text(fieldGroup, SWT.BORDER);
    fWebContextRoot.setFont(font);
    fWebContextRoot.setEditable(false);
    fWebContextRoot.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
    fWebContextRoot.setLayoutData(data);
    fWebContextRoot.setText(getContextRootLocation());
    fWebContextRoot.setEnabled(isEnabled);
    fWebContextRoot.addModifyListener(new ModifyListener()
    {
      public void modifyText(ModifyEvent e)
      {
        updateApplyButton();
      }
    });

    fBrowseContextRoot = new Button(fieldGroup, SWT.PUSH);
    fBrowseContextRoot.setFont(font);
    fBrowseContextRoot.setText(UIPlugin.getString("browse-button-label"));
    fBrowseContextRoot.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent evt)
      {
        String newValue = chooseContextFolder();
        if (newValue != null)
        {
          fWebContextRoot.setText(newValue);
          isValid();
        }
      }
    });

    fBrowseContextRoot.setEnabled(isEnabled);

    fValidateWebXML = new Button(fieldGroup, SWT.CHECK);
    fValidateWebXML.setText(UIPlugin.getString("property-page-validate-web-xml"));
    fValidateWebXML.setFont(parent.getFont());
    fValidateWebXML.setSelection(isValidatingWebXML());

    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    gd.horizontalIndent = 1;
    gd.horizontalSpan = 3;
    fValidateWebXML.setLayoutData(gd);

    checkEnabled();

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
        return false;
      }
    } catch (CoreException e1)
    {
      UIPlugin.log(e1);
    }

    if (!fIsTapestryProjectCheck.getSelection())
    {
      return true;
    }

    String wcroot = fWebContextRoot.getText();
    if (wcroot == null || wcroot.trim().length() == 0)
      return false;

    wcroot = wcroot.trim();

    if (!wcroot.startsWith("/"))
      wcroot = "/" + wcroot;

    IPath projPath = project.getFullPath();
    String fullPath = projPath.toString() + wcroot;
    if (!projPath.isValidPath(fullPath))
    {
      setErrorMessage("not a valid path: " + fullPath); // TODO

      return false;
    }
    if (DEBUG)
      UIPlugin.log("isValid() ->about to validate the context root: " + wcroot);
    String badApp;
    try
    {
      badApp = fDialogContextValidator.isValid(new Path(fullPath));
    } catch (RuntimeException e2)
    {
      if (DEBUG)
        UIPlugin.log("isValid() -> an exception ocurred");
      throw e2;
    }
    if (badApp != null)
    {
      setErrorMessage(badApp);
      return false;
    }

    return true;
  }

  //called by isValid() disable if the project is already an old (1.1.X)
  // Spindle project
  private void disableAll()
  {
    fIsTapestryProjectCheck.setEnabled(false);
    fContextRootLabel.setEnabled(false);
    fWebContextRoot.setEnabled(false);
    fBrowseContextRoot.setEnabled(false);
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
    ContainerSelectionDialog dialog = new ContainerSelectionDialog(
        getShell(),
        project,
        true,
        "");
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
      throw new CoreException(new Status(
          IStatus.ERROR,
          TapestryCore.PLUGIN_ID,
          0,
          "not found",
          null));
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

  private boolean isValidatingWebXML()
  {

    try
    {
      QualifiedName key = new QualifiedName("", VALIDATE_WEBXML_PROPERTY);
      TapestryProject prj = getTapestryProject();
      if (prj != null)
      {
        return prj.isValidatingWebXML();
      } else
      {
        String property = getPropertyFromWorkspace(key);
        if (property != null)
          return "true".equals(property);
      }
    } catch (CoreException ex)
    {
      // do nothing.
    }
    return true;
  }
  protected String getContextRootLocation()
  {
    if (DEBUG)
      UIPlugin.log("getting the context root");

    String result = "/context";
    try
    {
      QualifiedName key = new QualifiedName("", CONTEXT_ROOT_PROPERTY);
      TapestryProject prj = getTapestryProject();
      if (prj != null)
      {
        if (DEBUG)
          UIPlugin
              .log("tapestry project is not null - trying to get the context from it..");
        result = prj.getWebContext();
        if (result == null || "".equals(result.trim()))
        {
          if (DEBUG)
            UIPlugin.log("The tapestry project returned: '" + result
                + "' going to the workspace properties....");

          result = getPropertyFromWorkspace(key);

          if (DEBUG)
            UIPlugin.log("got: " + result + " from the workspace properites.");

        } else
        {
          if (DEBUG)
            UIPlugin.log("got: " + result + " from the tapestry project");
        }
      } else
      {
        if (DEBUG)
          UIPlugin.log("No Tapeestry project, going to the workspace properties...");

        result = getPropertyFromWorkspace(key);

        if (DEBUG)
          UIPlugin.log("got: " + result + " from the workspace properites.");
      }

    } catch (CoreException ex)
    {
      if (DEBUG)
        UIPlugin.log("A CoreException occurred accessing the context root");
    }

    if (DEBUG)
      UIPlugin.log("returning context root = " + result);
    return result;
  }

  /**
   * @see PreferencePage#createContents(Composite)
   */
  protected Control createContents(Composite parent)
  {
    if (DEBUG)
      UIPlugin.log("Tapestry Properties Page creation started.");
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
    //        addLibrarySection(composite);

    if (DEBUG)
      UIPlugin.log("Tapestry Properties Page creation done.");
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
    fValidateWebXML.setSelection(true);
  }

  public boolean performOk()
  {
    Shell shell = UIPlugin.getDefault().getActiveWorkbenchShell();
    if (shell == null)
    {
      try
      {
        doOk(new NullProgressMonitor());
      } catch (CoreException e)
      {
        UIPlugin.log(e);
      }
    } else
    {
      try
      {
        new ProgressMonitorDialog(shell).run(false, false, new IRunnableWithProgress()
        {
          public void run(IProgressMonitor monitor) throws InvocationTargetException,
              InterruptedException
          {
            try
            {
              doOk(monitor);
            } catch (CoreException e)
            {
              UIPlugin.log(e);
            }
          }
        });
      } catch (InvocationTargetException e)
      {
        UIPlugin.log(e);
      } catch (InterruptedException e)
      {
        UIPlugin.log(e);
      }
    }

    return true;
  }

  private void doOk(IProgressMonitor monitor) throws CoreException
  {
    // store the values as properties
    IResource resource = (IResource) getElement();

    resource.setPersistentProperty(
        new QualifiedName("", PROJECT_TYPE_PROPERTY),
        new Integer(TapestryProject.APPLICATION_PROJECT_TYPE).toString());

    resource.setPersistentProperty(
        new QualifiedName("", CONTEXT_ROOT_PROPERTY),
        fWebContextRoot.getText());

    resource.setPersistentProperty(
        new QualifiedName("", VALIDATE_WEBXML_PROPERTY),
        Boolean.toString(fValidateWebXML.getSelection()));

    IWorkspaceRunnable runnable = new IWorkspaceRunnable()
    {
      public void run(IProgressMonitor monitor) throws CoreException
      {
        if (fIsTapestryProjectCheck.getSelection())
        {
          if (getTapestryProject() == null)
            TapestryProject.addTapestryNature(getJavaProject());
          TapestryProject prj = getTapestryProject();

          String projectName = prj.getProject().getName();
          String temp = fWebContextRoot.getText();
          createFolderIfRequired(projectName + temp);
          prj.setWebContext(temp);
          prj.setValidateWebXML(fValidateWebXML.getSelection());
          prj.saveProperties();
          IJavaProject jproject = getJavaProject();
          try
          {
            if (jproject.findType(TapestryCore
                .getString("TapestryComponentSpec.specInterface")) == null)
            {
              MessageDialog dialog = new MessageDialog(
                  getShell(),
                  "Tapestry jars missing",
                  null,
                  "Add the Tapestry jars to the classpath?",
                  MessageDialog.INFORMATION,
                  new String[]{IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL},
                  0);
              // OK is the default
              int result = dialog.open();
              if (result == 0)
              {
                List entries = Arrays.asList(jproject.getRawClasspath());
                ArrayList useEntries = new ArrayList(entries);
                useEntries.add(JavaCore.newContainerEntry(new Path(
                    TapestryCore.CORE_CONTAINER)));
                jproject.setRawClasspath((IClasspathEntry[]) useEntries
                    .toArray(new IClasspathEntry[entries.size()]), monitor);
              }
            }
          } catch (JavaModelException e)
          {
            UIPlugin.log(e);
          }

        } else
        {
          TapestryProject.removeTapestryNature(getJavaProject());
        }

        if (fIsTapestryProjectCheck.getSelection())
        {
          IProject project = getJavaProject().getProject();
          project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
        }
      }

    };

    UIPlugin.getWorkspace().run(runnable, monitor);
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
      Revealer.selectAndReveal(new StructuredSelection(folder), UIPlugin
          .getDefault()
          .getActiveWorkbenchWindow());
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

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  public void dispose()
  {
    if (DEBUG)
      UIPlugin.log("Tapestry Property Page closed (disposed)\n\n\n");
    super.dispose();
  }

}