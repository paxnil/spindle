package com.iw.plugins.spindle.core.builder;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.artifacts.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.Utils;

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

/**
 * Builds a Tapestry Application project incrementally
 * 
 * Well, sort of. An incremental build will not reprocess
 * the framework namespace or any libraries found in jar files.
 * 
 * Other than that its the same as a full build.
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class IncrementalApplicationBuild extends FullBuild implements IIncrementalBuild
{
    public static int REMOVED_REPLACED = IResourceDelta.REMOVED | IResourceDelta.REPLACED;
    public static int MOVED_OR_SYNCHED_OR_CHANGED_TYPE =
        IResourceDelta.MOVED_FROM | IResourceDelta.MOVED_TO | IResourceDelta.SYNC | IResourceDelta.TYPE;

    /**
     * Constructor for IncrementalBuilder.
     * @param builder
     */
    public IncrementalApplicationBuild(TapestryBuilder builder, IResourceDelta projectDelta)
    {
        super(builder, projectDelta);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.IIncrementalBuild#canIncrementalBuild(org.eclipse.core.resources.IResourceDelta)
     */
    public boolean canIncrementalBuild()
    {
        if (!super.canIncrementalBuild())
            return false;

        IResourceWorkspaceLocation contextRoot = fTapestryBuilder.fContextRoot;
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
                    System.out.println("inc build abort - context root does not exist" + contextRoot);
                return false;
            }

            IResourceWorkspaceLocation webXML =
                (IResourceWorkspaceLocation) fTapestryBuilder.fContextRoot.getRelativeLocation("WEB-INF/web.xml");

            if (!webXML.exists())
            {
                if (TapestryBuilder.DEBUG)
                    System.out.println("inc build abort - web.xml does not exist" + webXML);
                return false;
            }
            IResource resource = (IResource) webXML.getStorage();
            IResourceDelta webXMLDelta = fProjectDelta.findMember(resource.getProjectRelativePath());

            if (webXMLDelta != null)
            {
                if (TapestryBuilder.DEBUG)
                    System.out.println("inc build abort - web.xml changed since last build");
                return false;
            }

            if (needFullBuildDueToAppSpecChange())
                return false;

        } else
        {
            if (TapestryBuilder.DEBUG)
                System.out.println("inc build abort - no context root found in TapestryBuilder!");
            return false;
        }

        return true;
    }

    private boolean needFullBuildDueToAppSpecChange()
    {
        IResourceWorkspaceLocation appSpecLocation = fLastState.fApplicationServlet.applicationSpecLocation;
        if (appSpecLocation != null)
        {
            IResource specResource = Utils.toResource(appSpecLocation);
            if (specResource == null)
                return false;
            IResourceDelta specDelta = fProjectDelta.findMember(specResource.getProjectRelativePath());
            if (specDelta != null)
            {
                // can't incremental build if the application specification
                // has been deleted, replaced, moved, or synchonized with a source repository.
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
            IResourceLocation previousSpecLocation = last.getSpecificationLocation();
            IResourceWorkspaceLocation WEB_INF =
                (IResourceWorkspaceLocation) fTapestryBuilder.fContextRoot.getRelativeLocation("WEB-INF");

            if (!previousSpecLocation.equals(WEB_INF))
            {
                existingSpecFile = Utils.toResource(previousSpecLocation);
            }

            if (existingSpecFile != null)
            {
                IResourceDelta specDelta = fProjectDelta.findMember(existingSpecFile.getProjectRelativePath());
                if (specDelta != null)
                {
                    // can't incremental build if the application specification
                    // has been deleted, replaced, moved, or synchonized with a source repository.
                    int kind = specDelta.getKind();
                    if ((kind & IResourceDelta.NO_CHANGE) == 0)
                    {
                        if ((kind & REMOVED_REPLACED) > 0)
                        {
                            if (TapestryBuilder.DEBUG)
                                System.out.println("inc build abort - " + existingSpecFile + "was removed or replaced");
                            return true;
                        }
                        int flags = specDelta.getFlags();
                        if ((flags & MOVED_OR_SYNCHED_OR_CHANGED_TYPE) > 0)
                        {
                            if (TapestryBuilder.DEBUG)
                                System.out.println("inc build abort - " + existingSpecFile + "was moved or synced");
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

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.FullBuild#saveState()
     */
    public void saveState()
    {
        State newState = new State();
        newState.copyFrom(fLastState);
        newState.fJavaDependencies = fFoundTypes;
        newState.fMissingJavaTypes = fMissingTypes;
        newState.fSeenTemplateExtensions = fSeenTemplateExtensions;
        newState.fPrimaryNamespace = fApplicationNamespace;

        saveBinaryLibraries(fFrameworkNamespace, fApplicationNamespace, newState);
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
            fTapestryBuilder.fCurrentProject,
            newState);
    }

}
