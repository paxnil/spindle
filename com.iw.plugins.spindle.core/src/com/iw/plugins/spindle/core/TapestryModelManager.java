package com.iw.plugins.spindle.core;

import java.util.Arrays;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The <code>TapestryModelManager</code> manages instances of <code>ITapestryModel</code>.
 * <p>
 * The single instance of <code>TapestryModelManager</code> is available from
 * the static method <code>TapestryModelManager.getTapestryModelManager()</code>.
 */
public class TapestryModelManager {

  public static boolean VERBOSE = true;

  private TapestryModelCache cache = new TapestryModelCache();

  /**
  * The singleton manager
  */
  private final static TapestryModelManager Manager = new TapestryModelManager();

  /**
   * Returns the singleton JavaModelManager
   */
  public final static TapestryModelManager getTapestryModelManager() {
    return Manager;
  }

  /**
   * Returns whether the given full path (for a package) conflicts with the output location
   * of the given project.
   */
  public static boolean conflictsWithOutputLocation(IPath folderPath, IJavaProject project) {
    try {
      IPath outputLocation = project.getOutputLocation();
      if (outputLocation == null) {
        // in doubt, there is a conflict
        return true;
      }
      if (outputLocation.isPrefixOf(folderPath)) {
        return true;
      }
      return false;
    } catch (JavaModelException e) {
      // in doubt, there is a conflict
      return true;
    }
  }

  /**
   * Returns the Tapestry artifact corresponding to the given storage, or
   * <code>null</code> if unable to associate the given resource
   * with a Tapestry artifact.
   * <p>
   * The resource must be one of:<ul>
   *	<li>a project - the element returned is the corresponding <code>ITapestryProject</code></li>
   *	<li>a <code>.application</code> file - the element returned is the corresponding <code>IBuildUnit</code></li>
   *	<li>a <code>.library</code> file - the element returned is the corresponding <code>IBuildUnit</code></li>
   *	<li>a <code>.page</code> file - the element returned is the corresponding <code>IBuildUnit</code></li>
   *	<li>a <code>.jwc</code> file - the element returned is the corresponding <code>IBuildUnit</code></li>
   *	<li>a <code>.html</code> file - the element returned is the corresponding <code>IBuildUnit</code></li>
   *	<li>a <code>.script</code> file - the element returned is the corresponding <code>IBuildUnit</code></li>
   *  <li>a folder - the element returned is the corresponding  IServletRoot.
   *  <li>the workspace root resource - the element returned is the <code>ITapestryModel</code></li>
   *	</ul>
   * <p>
   * Creating a Tapestry artifact has the side effect of creating and opening all of the
   * artifact's parents if they are not yet open.
   */
  public static ITapestryArtifact create(IResource resource, int type, ITapestryArtifact parent) {
    if (resource != null) {

      switch (type) {
        case IResource.PROJECT :
          return TapestryCore.create((IProject) resource);
        case IResource.FILE :
          return createBuildUnit(resource, type, parent);
        case IResource.FOLDER :
          return create((IFolder) resource, parent.getTapestryProject());
        case IResource.ROOT :
          return TapestryCore.create((IWorkspaceRoot) resource);
        default :
          return null;
      }

    }
    return null;
  }

  /**
  * Returns the Tapestry artifact corresponding to the given storage, its project being the given
  * project.
  * Returns <code>null</code> if unable to associate the given file
  * with a Tapestry artifact.
  *
  * <p>The file must be one of:<ul>
   *	<li>a <code>.application</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
   *	<li>a <code>.library</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
   *	<li>a <code>.page</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
   *	<li>a <code>.jwc</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
   *	<li>a <code>.html</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
   *	<li>a <code>.script</code> file - the element returned is the corresponding <code>IClassFile</code></li>
  *	</ul>
  * <p>
  * Creating a Tapestry artifact has the side effect of creating and opening all of the
  * element's parents if they are not yet open.
  */
  public static ITapestryArtifact createBuildUnit(
    IResource resource,
    int type,
    ITapestryArtifact parent) {
    if (resource == null || parent == null) {
      return null;
    }
    if (isValidBuildUnitName(resource.getName())) {
      return createBuildUnitFrom(resource, type, parent);
    }
    return null;
  }

  private static ITapestryArtifact createBuildUnitFrom(
    IResource resource,
    int type,
    ITapestryArtifact parent) {
    return null;
  }

  /**
   * Returns the tapestry root corresponding to the given folder,
   * its parent or ancestor being the given project. 
   * or <code>null</code> if unable to associate the given folder with a Tapestry artifact.
   * <p>
   * Creating a Tapestry artifact has the side effect of creating and opening all of the
   * element's parents if they are not yet open.
   */
  public static ITapestryArtifact create(IFolder folder, ITapestryProject project) {
    if (folder == null) {
      return null;
    }
    if (project == null) {
      project = TapestryCore.create(folder.getProject());
    }
    if (determineIfOnClasspath(folder)) {
      return null;
    }
    return project.createRoot(folder);
  }

  private static boolean isValidBuildUnitName(String name) {
    int index = name.lastIndexOf(".");
    if (index < 0) {
      return false;
    }
    String extension = name.substring(index);
    return Arrays.asList(TapestryBuilder.KnownExtensions).contains(extension);
  }

  private static boolean determineIfOnClasspath(IFolder folder) {
    IJavaElement element = JavaCore.create(folder);
    if (element != null && element.exists()) {
      return true;
    }
    return false;
  }

  private final TapestryModel tapestryModel = new TapestryModel();

  private TapestryModelManager() {
  }

  public final TapestryModel getTapestryModel() {
    return tapestryModel;
  }

  public Object getInfo(TapestryArtifact artifact) {
    return cache.getInfo(artifact);
  }

  public void removeInfo(TapestryArtifact artifact) {
    cache.removeInfo(artifact);
  }

  public Object peekAtInfo(TapestryArtifact artifact) {
    return getInfo(artifact);
  }

  public State getLastBuiltState(IProject project, IProgressMonitor pm) {
    return null;
  }

  public void setLastBuiltState(IProject currentProject, IProgressMonitor pm) {
  }

}
