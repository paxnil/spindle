package com.iw.plugins.spindle.core.builder;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;

import com.iw.plugins.spindle.core.artifacts.TapestryArtifactManager;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

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

    /**
     * Constructor for IncrementalBuilder.
     * @param builder
     */
    public IncrementalApplicationBuild(TapestryBuilder builder)
    {
        super(builder);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.IIncrementalBuild#canIncrementalBuild(org.eclipse.core.resources.IResourceDelta)
     */
    public boolean canIncrementalBuild(IResourceDelta projectDelta)
    {
        if (!super.canIncrementalBuild(projectDelta))
            return false;

        IResourceWorkspaceLocation contextRoot = fTapestryBuilder.fContextRoot;
        if (contextRoot != null)
        {
            if (!contextRoot.equals(fLastState.fContextRoot))
                return false;

            if (!contextRoot.exists())
                return false;

            IResourceWorkspaceLocation webXML =
                (IResourceWorkspaceLocation) fTapestryBuilder.fContextRoot.getRelativeLocation("WEB-INF/web.xml");

            if (!webXML.exists())
                return false;

            IResource resource = (IResource) webXML.getStorage();
            IResourceDelta webXMLDelta = projectDelta.findMember(resource.getProjectRelativePath());

            if (webXMLDelta != null)
                return false;
        } else
        {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.FullBuild#saveState()
     */
    protected void saveState()
    {
        State newState = new State();
        newState.copyFrom(fLastState);
        newState.fJavaDependencies = fFoundTypes;
        newState.fMissingJavaTypes = fMissingTypes;
        newState.fSeenTemplateExtensions = fSeenTemplateExtensions;
        saveBinaryLibraries(fFrameworkNamespace, fApplicationNamespace, newState);
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
            fTapestryBuilder.fCurrentProject,
            newState);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.FullBuild#getNamespaceResolver()
     */
    protected NamespaceResolver getNamespaceResolver(Parser parser)
    {
        return new NamespaceResolver(this, parser, true);
    }

}
