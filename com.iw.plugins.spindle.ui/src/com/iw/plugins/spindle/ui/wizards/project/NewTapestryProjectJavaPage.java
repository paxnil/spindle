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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.wizards.ClassPathDetector;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;

/**
 * As addition to the JavaCapabilityConfigurationPage, the wizard does an early
 * project creation (so that linked folders can be defined) and, if an existing
 * external location was specified, offers to do a classpath detection
 */
public class NewTapestryProjectJavaPage extends JavaCapabilityConfigurationPage
{

  static private IClasspathEntry TAPESTRY_FRAMEWORK = JavaCore.newContainerEntry(
      new Path(TapestryCore.CORE_CONTAINER),
      false);
  //    IClasspathEntry poo = new ClasspathEntry(
  //            IPackageFragmentRoot.K_BINARY,
  //            ClasspathEntry.CPE_CONTAINER,
  //            new Path(TapestryCore.CORE_CONTAINER),
  //            new Path[] {},
  //            null,
  //            null,
  //            null,
  //            false);

  private WizardNewProjectCreationPage fMainPage;

  private IPath fCurrProjectLocation;
  protected IProject fCurrProject;

  protected boolean fCanRemoveContent;
  
  private NewTapestryProjectWizard fWizard;

  /**
   * Constructor for NewProjectCreationWizardPage.
   */
  public NewTapestryProjectJavaPage(NewTapestryProjectWizard wizard, WizardNewProjectCreationPage mainPage)
  {
    super();
    fWizard = wizard;
    fMainPage = mainPage;
    fCurrProjectLocation = null;
    fCurrProject = null;
    fCanRemoveContent = false;
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
  
  public  void changeToNewProject()
  {
    IProject newProjectHandle = fMainPage.getProjectHandle();
    IPath newProjectLocation = fMainPage.getLocationPath();

    if (fMainPage.useDefaults())
    {
      fCanRemoveContent = !newProjectLocation
          .append(fMainPage.getProjectName())
          .toFile()
          .exists();
    } else
    {
      fCanRemoveContent = !newProjectLocation.toFile().exists();
    }

    final boolean initialize = !(newProjectHandle.equals(fCurrProject) && newProjectLocation
        .equals(fCurrProjectLocation));

    IRunnableWithProgress op = new IRunnableWithProgress()
    {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException
      {
        try
        {
          updateProject(initialize, monitor);
        } catch (CoreException e)
        {
          throw new InvocationTargetException(e);
        }
      }
    };

    try
    {
      IRunnableContext context = (IRunnableContext) getContainer();
      if (context == null)
      {
        if (getWizard() == null)
        {
          UIPlugin
              .log("creating : wizard is null: bug [ 843021 ] Is this what 3 Beta is supposed to do");
        } else
        {
          UIPlugin
              .log("creating : container not set in wizard: bug [ 843021 ] Is this what 3 Beta is supposed to do");
        }
        context = (IRunnableContext) UIPlugin.getDefault().getActivePage();
      }
      context.run(false, true, op);

    } catch (InvocationTargetException e)
    {
      String title = NewWizardMessages
          .getString("NewProjectCreationWizardPage.EarlyCreationOperation.error.title"); //$NON-NLS-1$
      String message = NewWizardMessages
          .getString("NewProjectCreationWizardPage.EarlyCreationOperation.error.desc"); //$NON-NLS-1$
      ExceptionHandler.handle(e, getShell(), title, message);
    } catch (InterruptedException e)
    {
      // cancel pressed
    }
  }

  protected void updateProject(boolean initialize, IProgressMonitor monitor) throws CoreException,
      InterruptedException
  {
    fCurrProject = fMainPage.getProjectHandle();
    fCurrProjectLocation = fMainPage.getLocationPath();
    boolean noProgressMonitor = !initialize && fCanRemoveContent;

    if (monitor == null || noProgressMonitor)
    {
      monitor = new NullProgressMonitor();
    }
    try
    {
      monitor.beginTask(NewWizardMessages
          .getString("NewProjectCreationWizardPage.EarlyCreationOperation.desc"), 2); //$NON-NLS-1$

      createProject(
          fCurrProject,
          fCurrProjectLocation,
          new SubProgressMonitor(monitor, 1));

      if (initialize)
      {
        IClasspathEntry[] entries = null;
        IPath outputLocation = null;

        if (fCurrProjectLocation.toFile().exists()
            && !Platform.getLocation().equals(fCurrProjectLocation))
        {
          // detect classpath
          if (!fCurrProject.getFile(".classpath").exists())
          { //$NON-NLS-1$
            // if .classpath exists noneed to look for files
            ClassPathDetector detector = new ClassPathDetector(fCurrProject);
            entries = detector.getClasspath();
            outputLocation = detector.getOutputLocation();
          }
        }
        if (outputLocation == null)
        {

          fCurrProject.open(null);
          outputLocation = createOutputLocation().getFullPath();

        }

        entries = checkEntries(entries);

        init(JavaCore.create(fCurrProject), outputLocation, entries, false);
      }
      monitor.worked(1);
    } finally
    {
      monitor.done();
    }
  }

  /**
   * @param entries
   * @return
   */
  private IClasspathEntry[] checkEntries(IClasspathEntry[] entries) throws CoreException
  {

    if (entries == null)
    {
      createSrcFolder();
      return new IClasspathEntry[]{createSrcClasspathEntry(), TAPESTRY_FRAMEWORK,
          JavaRuntime.getDefaultJREContainerEntry()};

    }

    boolean hasSrcEntry = false;
    boolean hasTapestryEntry = false;
    boolean hasDefaultJREEntry = false;
    List allEntries = Arrays.asList(entries);
    for (Iterator iter = allEntries.iterator(); iter.hasNext();)
    {
      IClasspathEntry element = (IClasspathEntry) iter.next();
      if (!hasSrcEntry && element.getEntryKind() == IClasspathEntry.CPE_SOURCE)
      {
        hasSrcEntry = true;
      } else if (element.getEntryKind() == IClasspathEntry.CPE_CONTAINER)
      {
        if (!hasTapestryEntry)
          hasTapestryEntry = element.getPath().segment(0).equals(
              TapestryCore.CORE_CONTAINER);
        if (!hasDefaultJREEntry)
          hasDefaultJREEntry = element.getPath().segment(0).equals(
              JavaRuntime.JRE_CONTAINER);
      }
    }

    if (!hasSrcEntry)
    {
      createSrcFolder();
      allEntries.add(createSrcClasspathEntry());

    }

    if (!hasTapestryEntry)
      allEntries.add(TAPESTRY_FRAMEWORK);

    if (!hasDefaultJREEntry)
      allEntries.add(JavaRuntime.getDefaultJREContainerEntry());

    return (IClasspathEntry[]) allEntries.toArray(new IClasspathEntry[allEntries.size()]);
  }

  private IClasspathEntry createSrcClasspathEntry()
  {

    return JavaCore.newSourceEntry(new Path("/" + fCurrProject.getName() + "/src"));
    //        return new ClasspathEntry(
    //            IPackageFragmentRoot.K_SOURCE,
    //            ClasspathEntry.CPE_SOURCE,
    //            new Path("/" + fCurrProject.getName() + "/src"),
    //            new Path[] {},
    //            null,
    //            null,
    //            null,
    //            false);
  }

  private void createSrcFolder()
  {
    IFolder srcFolder = fCurrProject.getFolder("src");
    if (!srcFolder.exists())
    {
      try
      {
        srcFolder.create(true, true, null);
      } catch (CoreException e)
      {
        UIPlugin.log(e);
      }
    }
  }

  private IFolder getContextFolder() throws CoreException
  {
    NewTapestryProjectWizard wiz = (NewTapestryProjectWizard) getWizard();
    String contextFolderName = wiz.getContextFolderName();
    IFolder context = fCurrProject.getFolder(contextFolderName);
    if (!context.exists())
      context.create(true, true, null);
    return context;
  }

  private IFolder createOutputLocation() throws CoreException
  {
    IFolder context = getContextFolder();
    IFolder webInf = context.getFolder("WEB-INF");
    if (!webInf.exists())
      webInf.create(true, true, null);
    IFolder classes = webInf.getFolder("classes");
    if (!classes.exists())
      classes.create(true, true, null);
    return classes;
  }

  /**
   * Called from the wizard on finish.
   */
  public void performFinish(IProgressMonitor monitor) throws CoreException,
      InterruptedException
  {
    try
    {
      monitor.beginTask(NewWizardMessages
          .getString("NewProjectCreationWizardPage.createproject.desc"), 3); //$NON-NLS-1$
      if (fCurrProject == null)
      {
        updateProject(true, new SubProgressMonitor(monitor, 1));
      }
      configureJavaProject(new SubProgressMonitor(monitor, 2));
    } finally
    {
      monitor.done();
      fCurrProject = null;
    }
  }

   public void removeProject()
  {
    if (fCurrProject == null || !fCurrProject.exists())
    {
      return;
    }

    IRunnableWithProgress op = new IRunnableWithProgress()
    {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException
      {
        boolean noProgressMonitor = Platform.getLocation().equals(fCurrProjectLocation);
        if (monitor == null || noProgressMonitor)
        {
          monitor = new NullProgressMonitor();
        }
        monitor.beginTask(NewWizardMessages
            .getString("NewProjectCreationWizardPage.removeproject.desc"), 3); //$NON-NLS-1$

        try
        {
          fCurrProject.delete(fCanRemoveContent, false, monitor);
        } catch (CoreException e)
        {
          throw new InvocationTargetException(e);
        } finally
        {
          monitor.done();
          fCurrProject = null;
          fCanRemoveContent = false;
        }
      }
    };

    try
    {
      IRunnableContext context = (IRunnableContext) getContainer();
      if (context == null)
      {
        if (getWizard() == null)
        {
          UIPlugin
              .log("removing : wizard is null: bug [ 843021 ] Is this what 3 Beta is supposed to do");
        } else
        {
          UIPlugin
              .log("removing : container not set in wizard: bug [ 843021 ] Is this what 3 Beta is supposed to do");
        }
        context = (IRunnableContext) UIPlugin.getDefault().getActivePage();
      }
      context.run(false, true, op);
    } catch (InvocationTargetException e)
    {
      String title = NewWizardMessages
          .getString("NewProjectCreationWizardPage.op_error.title"); //$NON-NLS-1$
      String message = NewWizardMessages
          .getString("NewProjectCreationWizardPage.op_error_remove.message"); //$NON-NLS-1$
      ExceptionHandler.handle(e, getShell(), title, message);
    } catch (InterruptedException e)
    {
      // cancel pressed
    }
  }

  /**
   * Called from the wizard on cancel.
   */
  public void performCancel()
  {
    removeProject();
  }
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.ui.wizards.NewElementWizardPage#updateStatus(org.eclipse.core.runtime.IStatus)
   */
  protected void updateStatus(IStatus status)
  {
    super.updateStatus(status);
    if (status.isOK())
    {
      IClasspathEntry[] classpath = getRawClassPath();
      boolean hasSrcEntry = false;
      boolean hasTapestryFramework = false;
      for (int i = 0; i < classpath.length; i++)
      {
        if (!hasSrcEntry && classpath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE)
        {
          hasSrcEntry = true;

        } else if (!hasTapestryFramework
            && classpath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE)
        {
          IPath path = classpath[i].getPath();
          if (path.segment(0).equals(TapestryCore.CORE_CONTAINER))
          {
            hasTapestryFramework = true;
          }
        }

      }
      IStatus tapStatus = null;
      if (!hasSrcEntry)
      {

        tapStatus = new Status(IStatus.ERROR, TapestryCore.PLUGIN_ID, 0, UIPlugin
            .getString("new-project-wizard-must-have-src-folder"), null);
      }

      if (tapStatus != null)
        super.updateStatus(tapStatus);
    }
  }

}