package com.iw.plugins.spindle.core.builder;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.internal.Workbench;
import org.osgi.framework.Bundle;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.resources.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.ContextRootLocation;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.templates.TemplateFinder;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.util.Markers;

/**
 * The Tapestry Builder, kicks off full and incremental builds.
 * 
 * @author glongman@intelligentworks.com
 * 
 */
public class TapestryBuilder extends IncrementalProjectBuilder
{

  private final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi");
  
  private static ThreadLocal PACKAGE_CACHE;
  private static ThreadLocal TYPE_CACHE;
  private static ThreadLocal STORAGE_CACHE;

  static
  {
    PACKAGE_CACHE = new ThreadLocal();
    TYPE_CACHE = new ThreadLocal();
    STORAGE_CACHE = new ThreadLocal();
  }

  public static Map getPackageCache()
  {
    return (Map) PACKAGE_CACHE.get();
  }

  public static Map getTypeCache()
  {
    return (Map) TYPE_CACHE.get();
  }

  public static Map getStorageCache()
  {
    return (Map) STORAGE_CACHE.get();
  }

  public static final String STRING_KEY = "builder-";
  public static final String APPLICATION_EXTENSION = "application";
  public static final String COMPONENT_EXTENSION = "jwc";
  public static final String PAGE_EXTENSION = "page";
  public static final String TEMPLATE_EXTENSION = "html";
  public static final String SCRIPT_EXTENSION = "script";
  public static final String LIBRARY_EXTENSION = "library";
  public static final String[] KnownExtensions = new String[]{APPLICATION_EXTENSION,
      COMPONENT_EXTENSION, PAGE_EXTENSION, TEMPLATE_EXTENSION,
      //            SCRIPT_EXTENSION,
      LIBRARY_EXTENSION};
  public static final String APP_SPEC_PATH_PARAM = "org.apache.tapestry.application-specification";
  public static final String ENGINE_CLASS_PARAM = "org.apache.tapestry.engine-class";

  public static boolean DEBUG = false;

  //TODO this is really ugly, but I need this fast.
  public static List fDeferredActions = new ArrayList();

  /**
   * Obtain all the template locations for a component specification
   * 
   * @param specification the IComponentSpecification we want to find templates
   *          for
   * @param collector an IProblemCollector to collect any problems encountered.
   * @return an array of IResourceWorkspaceLocation - the template locations.
   */
  public static IResourceWorkspaceLocation[] scanForTemplates(
      IComponentSpecification specification,
      IProblemCollector collector)
  {
    TemplateFinder finder = new TemplateFinder();

    IResourceWorkspaceLocation[] locations = new IResourceWorkspaceLocation[0];
    //        TemplateFinder finder = new TemplateFinder();
    try
    {
      return finder.getTemplates(specification, collector);

    } catch (CoreException e)
    {
      TapestryCore.log(e);
    }
    return new IResourceWorkspaceLocation[]{};
  }

  public static State readState(DataInputStream in) throws IOException
  {
    return State.read(in);
  }

  public static void writeState(Object state, DataOutputStream out) throws IOException
  {
    ((State) state).write(out);
  }

  IProject fCurrentProject;
  IJavaProject fJavaProject;
  TapestryProject fTapestryProject;
  IWorkspaceRoot fWorkspaceRoot;
  ContextRootLocation fContextRoot;
  ClasspathRootLocation fClasspathRoot;
  int fProjectType;
  boolean fValidateWebXML;

  private IBuild fBuild;

  BuildNotifier fNotifier;

  IClasspathEntry[] fClasspath;

  /**
   * Constructor for TapestryBuilder.
   */
  public TapestryBuilder()
  {
    super();
  }

  /**
   * @see org.eclipse.core.internal.events.InternalBuilder#build(int, Map,
   *      IProgressMonitor)
   */
  protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
  {
    
    if (systemBundle.getState() == Bundle.STOPPING)
		throw new OperationCanceledException();
    PACKAGE_CACHE.set(new HashMap());
    TYPE_CACHE.set(new HashMap());
    STORAGE_CACHE.set(new HashMap());

    Workbench workbench = (Workbench) TapestryCore.getDefault().getWorkbench();
    if (workbench.isClosing())
      return getRequiredProjects(true);
    fCurrentProject = getProject();
    if (fCurrentProject == null || !fCurrentProject.isAccessible())
      return new IProject[0];

    long start = System.currentTimeMillis();
    if (DEBUG)
      System.out.println("\nStarting build of " + fCurrentProject.getName() + " @ "
          + new Date(System.currentTimeMillis()));
    this.fNotifier = new BuildNotifier(monitor, fCurrentProject);
    fNotifier.begin();
    boolean ok = false;
    try
    {
      fNotifier.checkCancel();
      initializeBuilder();

      if (isWorthBuilding())
      {
        if (kind == FULL_BUILD)
        {
          buildAll();
        } else
        {
          buildIncremental();
        }
        for (Iterator iter = fDeferredActions.iterator(); iter.hasNext();)
        {
          IBuildAction action = (IBuildAction) iter.next();
          action.run();
        }
        ok = true;
      }
    } catch (CoreException e)
    {
      ErrorDialog.openError(TapestryCore
          .getDefault()
          .getWorkbench()
          .getActiveWorkbenchWindow()
          .getShell(), TapestryCore.getString("build-failed-core-title"), TapestryCore
          .getString("build-failed-core-message"), e.getStatus());
    } catch (BuilderException e)
    {
      Markers.removeProblemsForProject(fCurrentProject);
      Markers.addBuildBrokenProblemMarkerToResource(getProject(), e.getMessage());
    } catch (NullPointerException e)
    {
      TapestryCore.log(e);
      throw e;
    } catch (OperationCanceledException e)
    {
      throw e;
    } catch (RuntimeException e)
    {
      TapestryCore.log(e);
      throw e;
    } finally
    {
      PACKAGE_CACHE.set(null);
      TYPE_CACHE.set(null);
      STORAGE_CACHE.set(null);
      if (!ok)
        // If the build failed, clear the previously built state, forcing a full
        // build next time.
        clearLastState();
      fNotifier.done();
      fDeferredActions.clear();
      cleanup();
      TapestryCore.getDefault().buildOccurred();
    }
    IProject[] requiredProjects = getRequiredProjects(true);
    long stop = System.currentTimeMillis();
    if (DEBUG)
      System.out.println("Finished build of " + fCurrentProject.getName() + " @ "
          + new Date(stop));
    System.out.println("elapsed (ms) = " + (stop - start));
    return requiredProjects;
  }

  /**
   * Method cleanup.
   */
  private void cleanup()
  {
    if (fBuild != null)
      fBuild.cleanUp();

    fClasspath = null;
  }

  private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites)
  {
    if (fJavaProject == null || fWorkspaceRoot == null)
      return new IProject[0];

    ArrayList projects = new ArrayList();
    try
    {
      IClasspathEntry[] entries = ((JavaProject) fJavaProject).getExpandedClasspath(true);
      for (int i = 0, length = entries.length; i < length; i++)
      {
        IClasspathEntry entry = JavaCore.getResolvedClasspathEntry(entries[i]);
        if (entry != null)
        {
          IPath path = entry.getPath();
          IProject p = null;
          if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
          {
            IProject workspaceProject = fWorkspaceRoot.getProject(path.lastSegment());
            if (workspaceProject.hasNature(TapestryCore.NATURE_ID))
              p = workspaceProject;

          }
          if (p != null && !projects.contains(p))
            projects.add(p);

        }
      }
    } catch (CoreException e)
    {
      return new IProject[0];
    }
    IProject[] result = new IProject[projects.size()];
    projects.toArray(result);
    return result;
  }

  State getLastState(IProject project)
  {
    return (State) TapestryArtifactManager
        .getTapestryArtifactManager()
        .getLastBuildState(fCurrentProject, false);
  }

  /**
   * Method clearLastState.
   */
  private void clearLastState()
  {
    TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
        fCurrentProject,
        null);
  }

  /**
   * Method buildAll.
   */
  private void buildAll() throws BuilderException, CoreException
  {
    if (TapestryBuilder.DEBUG)
      System.out.println("FULL Tapestry build");

    fNotifier.subTask(TapestryCore.getString(TapestryBuilder.STRING_KEY
        + "full-build-starting"));

    Markers.removeProblemsForProject(fCurrentProject);

    int type = fTapestryProject.getProjectType();
    switch (type)
    {
      case TapestryProject.APPLICATION_PROJECT_TYPE :
        fBuild = new FullBuild(this);
        break;

      //            case TapestryProject.LIBRARY_PROJECT_TYPE :
      //                fBuild = new LibraryBuild(this);
      default :
        throw new BuilderException("unsupport project type");
    }
    if (fBuild != null)
      fBuild.build();

  }

  private void buildIncremental() throws BuilderException, CoreException
  {

    int type = fTapestryProject.getProjectType();
    IIncrementalBuild incBuild = null;
    IResourceDelta delta = getDelta(fTapestryProject.getProject());
    switch (type)
    {
      case TapestryProject.APPLICATION_PROJECT_TYPE :
        incBuild = new IncrementalProjectBuild(this, delta);
        break;

      default :
        throw new BuilderException("unsupport project type");
    }
    if (incBuild == null)
      throw new Error("no builder!");

    if (incBuild.canIncrementalBuild())
    {
      if (!incBuild.needsIncrementalBuild())
        return;

      fBuild = incBuild;
      if (TapestryBuilder.DEBUG)
        System.out.println("Incremental Tapestry build");

      fNotifier.subTask(TapestryCore.getString(TapestryBuilder.STRING_KEY
          + "incremental-build-starting"));
      incBuild.build();
    } else
    {
      buildAll();
    }
  }

  /**
   * Method isWorthBuilding.
   * 
   * @return boolean
   */
  private boolean isWorthBuilding()
  {

    if (fJavaProject == null)
      throw new BuilderException(TapestryCore.getString(STRING_KEY + "non-java-projects"));

    try
    {
      fClasspath = fJavaProject.getResolvedClasspath(true);
    } catch (JavaModelException e3)
    {
      throw new BuilderException(TapestryCore.getString(STRING_KEY
          + "classpath-not-determined"));
    }

    try
    {
      IResource resource = fJavaProject.getUnderlyingResource();
      IMarker[] jprojectMarkers = new IMarker[0];
      if (resource != null && resource.exists())
        jprojectMarkers = resource.findMarkers(
            IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
            false,
            IResource.DEPTH_ZERO);
      if (jprojectMarkers.length > 0)
        throw new BuilderException(TapestryCore.getString(STRING_KEY
            + "java-builder-failed"));

    } catch (CoreException e)
    {
      // assume there are no Java builder problems
    }

    try
    {
      IPath outputPath = fJavaProject.getOutputLocation();
      IPath projectPath = fJavaProject.getPath();

      if (projectPath.equals(outputPath))
        throw new BuilderException(TapestryCore.getString(STRING_KEY
            + "abort-invalid-output-location", outputPath.toString()));

    } catch (JavaModelException e1)
    {
      throw new BuilderException(TapestryCore.getString(STRING_KEY
          + "abort-no-output-location"));
    }

    // make sure all prereq projects have valid build states...
    IProject[] requiredProjects = getRequiredProjects(false);
    next : for (int i = 0, length = requiredProjects.length; i < length; i++)
    {
      IProject p = requiredProjects[i];
      if (getLastState(p) == null)
      {
        if (DEBUG)
          System.out.println(TapestryCore.getString(
              STRING_KEY + "abort-prereq-not-built",
              p.getName()));
        throw new BuilderException(TapestryCore.getString(STRING_KEY
            + "abort-prereq-not-built", p.getName()));
      }
    }

    if (getType(TapestryCore.getString(STRING_KEY + "applicationServletClassname")) == null)
      throw new BuilderException(TapestryCore.getString(STRING_KEY
          + "tapestry-jar-missing"));

    int projectType = fTapestryProject.getProjectType();

    if (projectType == TapestryProject.APPLICATION_PROJECT_TYPE)
    {
      if (fContextRoot == null || !fContextRoot.exists())
        throw new BuilderException(TapestryCore.getString(STRING_KEY + "missing-context"));

      IResourceWorkspaceLocation webXML = (IResourceWorkspaceLocation) fContextRoot
          .getRelativeLocation("WEB-INF/web.xml");

      if (webXML.getStorage() == null)
        throw new BuilderException(TapestryCore.getString(STRING_KEY
            + "abort-missing-web-xml", webXML.toString()));

    } else
    {
      throw new BuilderException("unsupported project type");
    }

    return true;
  }

  /**
   * Method initializeBuilder.
   */
  private void initializeBuilder()
  {

    try
    {
      fJavaProject = (IJavaProject) fCurrentProject.getNature(JavaCore.NATURE_ID);

    } catch (CoreException e)
    {
      TapestryCore.log(e);
      throw new BuilderException("could not obtain the Java Project!");
    }

    try
    {
      fTapestryProject = (TapestryProject) fCurrentProject
          .getNature(TapestryCore.NATURE_ID);
    } catch (CoreException e)
    {
      TapestryCore.log(e);
      throw new BuilderException("could not obtain the Tapestry Project!");
    }
    fContextRoot = fTapestryProject.getWebContextLocation();

    try
    {
      fClasspathRoot = fTapestryProject.getClasspathRoot();
    } catch (CoreException e1)
    {
      throw new BuilderException("could not obtain the Classpath Root!");
    }
    
    fValidateWebXML = fTapestryProject.isValidatingWebXML();

  }

  protected IType getType(String fullyQualifiedName)
  {
    Map cache = getTypeCache();

    if (cache != null && cache.containsKey(fullyQualifiedName))
      return (IType) cache.get(fullyQualifiedName);

    IType result = null;
    try
    {
      result = fJavaProject.findType(fullyQualifiedName);
    } catch (JavaModelException e)
    {
      // do nothing
    }

    if (cache != null)
      cache.put(fullyQualifiedName, result);

    return result;
  }

  protected boolean conflictsWithJavaOutputDirectory(IResource resource)
  {
    try
    {
      IPath containerPath = fJavaProject.getOutputLocation();
      return containerPath.isPrefixOf(resource.getFullPath());
    } catch (JavaModelException e)
    {
      // do nothing
    }
    return false;
  }

}