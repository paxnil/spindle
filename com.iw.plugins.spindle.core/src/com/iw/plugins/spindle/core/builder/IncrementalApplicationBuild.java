package com.iw.plugins.spindle.core.builder;

import java.util.Arrays;
import java.util.List;

import org.apache.hivemind.Resource;
import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.extensions.IncrementalBuildVetoController;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.ContextRootLocation;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.CoreUtils;

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

/**
 * Builds a Tapestry Application project incrementally
 * 
 * Well, sort of. An incremental build will not reprocess the framework
 * namespace or any libraries found in jar files.
 * 
 * Other than that its the same as a full build.
 * 
 * TODO Not Used anymore - to be removed
 * 
 * @see com.iw.plugins.spindle.core.builder.IncrementalProjectBuild
 *
 * @author glongman@gmail.com
 */
public class IncrementalApplicationBuild extends FullBuild implements IIncrementalBuild
{
  public static int REMOVED_REPLACED = IResourceDelta.REMOVED | IResourceDelta.REPLACED;
  public static int MOVED_OR_SYNCHED_OR_CHANGED_TYPE = IResourceDelta.MOVED_FROM
      | IResourceDelta.MOVED_TO | IResourceDelta.SYNC | IResourceDelta.TYPE;

  protected IResourceDelta fProjectDelta = null;

  /**
   * Constructor for IncrementalBuilder.
   * 
   * @param builder
   */
  public IncrementalApplicationBuild(TapestryBuilder builder, IResourceDelta projectDelta)
  {
    super(builder);
    fProjectDelta = projectDelta;
  }

  /**
   * Basic incremental build check. called by sub implementations in
   * IncrementalBuild classes before thier own checks.
   * <p>
   * An incremental build is possible if:
   * <ul>
   * <li>a file recognized as a Tapestry template was changed, added, or
   * deleted</li>
   * <li>a java type referenced by a Tapestry file was changed, added, or
   * deleted</li>
   * <li>a Tapestry xml file was changed, added, or deleted</li>
   * </ul>
   * Note that before this method is called it has already been determined that
   * an incremental build is indicated (i.e. web.xml has not changed, last build
   * did not fail, etc).
   */
  public boolean needsIncrementalBuild()
  {
    if (fProjectDelta == null)
      return false;
    fLastState = fTapestryBuilder.getLastState(fTapestryBuilder.fCurrentProject);
    final List knownTapestryExtensions = Arrays.asList(TapestryBuilder.KnownExtensions);

    // check for java files that changed, or have been added
    try
    {
      fProjectDelta.accept(new IResourceDeltaVisitor()
      {
        public boolean visit(IResourceDelta delta) throws CoreException
        {
          IResource resource = delta.getResource();

          if (resource instanceof IContainer)
            return true;

          IPath path = resource.getFullPath();
          String extension = path.getFileExtension();

          if (fLastState.fSeenTemplateExtensions.contains(extension))
            throw new NeedToBuildException();

          if (fLastState.fJavaDependencies.contains(resource)
              || knownTapestryExtensions.contains(extension))
          {
            throw new NeedToBuildException();
          } else
          {

            if (!"java".equals(extension))
              return true;

            String name = path.removeFileExtension().lastSegment();
            IContainer container = resource.getParent();
            IJavaElement element = (IJavaElement) JavaCore.create((IFolder) container);
            if (element == null)
              return true;
            if (element instanceof IPackageFragmentRoot
                && fLastState.fMissingJavaTypes.contains(name))
            {
              throw new NeedToBuildException();
            } else if (element instanceof IPackageFragment
                && fLastState.fMissingJavaTypes.contains(((IPackageFragment) element)
                    .getElementName()
                    + "." + name))
            {
              throw new NeedToBuildException();
            }

          }
          return true;
        }
      });
    } catch (CoreException e)
    {
      TapestryCore.log(e);
    } catch (NeedToBuildException e)
    {
      return true;
    }
    return false;

  }

  /**
   * An exception used to break out of a resource delta scan if an incremental
   * build is indicated.
   * 
   * @see #needsIncrementalBuild(IResourceDelta)
   */
  private static class NeedToBuildException extends RuntimeException
  {
    public NeedToBuildException()
    {
      super();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.builder.IIncrementalBuild#canIncrementalBuild()
   */
  public boolean canIncrementalBuild()
  {
    if (fProjectDelta == null)
      return false;

    fLastState = fTapestryBuilder.getLastState(fTapestryBuilder.fCurrentProject);
    //ensure the last build didn't fail and that state version sync up.
    if (fLastState == null || fLastState.fBuildNumber < 0
        || fLastState.fVersion != State.VERSION)
      return false;

    // The Tapestry framework library must exist in the state
    IResourceWorkspaceLocation frameworkLocation = (IResourceWorkspaceLocation) fTapestryBuilder.fClasspathRoot
        .getRelativeResource("/org/apache/tapestry/Framework.library");
    if (!fLastState.fBinaryNamespaces.containsKey(frameworkLocation))
      return false;

    //ensure the project classpath has not changed
    if (hasClasspathChanged())
      return false;

    //the context root exist and be the same as the one used for the last build.
    ContextRootLocation contextRoot = fTapestryBuilder.fContextRoot;
    if (contextRoot != null)
    {
      if (!contextRoot.equals(fLastState.fContextRoot))
      {
        if (TapestryBuilder.DEBUG)
          System.out.println("inc build abort - context root not same in last state");
        return false;
      }

      if (!contextRoot.exists())
      {
        if (TapestryBuilder.DEBUG)
          System.out.println("inc build abort - context root does not exist"
              + contextRoot);
        return false;
      }

      //web.xml must exist
      IResourceWorkspaceLocation webXML = (IResourceWorkspaceLocation) fTapestryBuilder.fContextRoot
          .getRelativeResource("WEB-INF/web.xml");

      IResource resource = (IResource) webXML.getStorage();
      if (resource == null)
      {
        if (TapestryBuilder.DEBUG)
          System.out.println("inc build abort - web.xml does not exist" + webXML);
        return false;
      }

      //and it must not have changed
      IResourceDelta webXMLDelta = fProjectDelta.findMember(resource
          .getProjectRelativePath());

      if (webXMLDelta != null)
      {
        if (TapestryBuilder.DEBUG)
          System.out.println("inc build abort - web.xml changed since last build");
        return false;
      }
      
      //ensure the .application file did not change
      if (needFullBuildDueToAppSpecChange())
        return false;

    } else
    {
      //must have a context root
      if (TapestryBuilder.DEBUG)
        System.out.println("inc build abort - no context root found in TapestryBuilder!");
      return false;
    }
    
    //contrbuted veto-ers must give thier ok to inc build
    IncrementalBuildVetoController vetoController = new IncrementalBuildVetoController();
    
    if (vetoController.vetoIncrementalBuild(fProjectDelta))
      return false;

    return true;
  }

  protected boolean hasClasspathChanged()
  {
    IClasspathEntry[] currentEntries = fTapestryBuilder.fClasspath;

    if (currentEntries.length != fLastState.fLastKnownClasspath.length)
      return true;

    List old = Arrays.asList(fLastState.fLastKnownClasspath);
    List current = Arrays.asList(currentEntries);

    return !current.containsAll(old);
  }

  private boolean needFullBuildDueToAppSpecChange()
  {
    IResourceWorkspaceLocation appSpecLocation = fLastState.fApplicationServlet.applicationSpecLocation;
    if (appSpecLocation != null)
    {
      IResource specResource = CoreUtils.toResource(appSpecLocation);
      if (specResource == null)
        return false;
      IResourceDelta specDelta = fProjectDelta.findMember(specResource
          .getProjectRelativePath());
      if (specDelta != null)
      {
        // can't incremental build if the application specification
        // has been deleted, replaced, moved, or synchonized with a source
        // repository.
        int kind = specDelta.getKind();
        if ((kind & IResourceDelta.NO_CHANGE) == 0)
        {
          if ((kind & REMOVED_REPLACED) > 0)
            return true;
          int flags = specDelta.getFlags();
          if ((flags & MOVED_OR_SYNCHED_OR_CHANGED_TYPE) > 0)
            return true;
        }
      }
    } else
    {
      // here we check to see if there is an automagic app spec.
      ICoreNamespace last = fLastState.fPrimaryNamespace;
      if (last == null)
        return true;

      IResource existingSpecFile = null;
      Resource previousSpecLocation = last.getSpecificationLocation();
      IResourceWorkspaceLocation WEB_INF = (IResourceWorkspaceLocation) fTapestryBuilder.fContextRoot
          .getRelativeResource("WEB-INF");

      if (!previousSpecLocation.equals(WEB_INF))
      {
        existingSpecFile = CoreUtils.toResource(previousSpecLocation);
      }

      if (existingSpecFile != null)
      {
        IResourceDelta specDelta = fProjectDelta.findMember(existingSpecFile
            .getProjectRelativePath());
        if (specDelta != null)
        {
          // can't incremental build if the application specification
          // has been deleted, replaced, moved, or synchonized with a source
          // repository.
          int kind = specDelta.getKind();
          if ((kind & IResourceDelta.NO_CHANGE) == 0)
          {
            if ((kind & REMOVED_REPLACED) > 0)
            {
              if (TapestryBuilder.DEBUG)
                System.out.println("inc build abort - " + existingSpecFile
                    + "was removed or replaced");
              return true;
            }
            int flags = specDelta.getFlags();
            if ((flags & MOVED_OR_SYNCHED_OR_CHANGED_TYPE) > 0)
            {
              if (TapestryBuilder.DEBUG)
                System.out.println("inc build abort - " + existingSpecFile
                    + "was moved or synced");
              return true;
            }
          }
        }
      } else
      {
        // now we had a synthetic, check to see if a real one has been added.
        try
        {
          fProjectDelta.accept(new IResourceDeltaVisitor()
          {
            public boolean visit(IResourceDelta delta) throws CoreException
            {
              IResource resource = delta.getResource();
              if (resource instanceof IFolder || resource instanceof IProject)
                return true;
              IFile file = (IFile) resource;
              if ("application".equals(file.getFullPath().getFileExtension()))
              {
                if (TapestryBuilder.DEBUG)
                  System.out.println("inc build abort - new app spec found");
                throw new BuilderException();
              }
              return true;
            }

          });
        } catch (BuilderException e)
        {
          // an application file exists now where one did not before
          // force a full build.
          return true;
        } catch (CoreException e)
        {
          TapestryCore.log(e);
          return true;
        }
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.builder.FullBuild#saveState()
   */
  public void saveState()
  {
    State newState = new State();
    newState.copyFrom(fLastState);
    newState.fJavaDependencies = fFoundTypes;
    newState.fMissingJavaTypes = fMissingTypes;
    newState.fTemplateMap = fTemplateMap;
    newState.fFileSpecificationMap = fFileSpecificationMap;
    newState.fPrimaryNamespace = fApplicationNamespace;
    newState.fSeenTemplateExtensions = fSeenTemplateExtensions;
    newState.fCleanTemplates = fCleanTemplates;

    TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
        fTapestryBuilder.fCurrentProject,
        newState);
  }



}