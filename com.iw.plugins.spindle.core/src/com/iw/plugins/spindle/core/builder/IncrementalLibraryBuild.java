package com.iw.plugins.spindle.core.builder;

import org.eclipse.core.resources.IResourceDelta;

import com.iw.plugins.spindle.core.artifacts.TapestryArtifactManager;
import com.iw.plugins.spindle.core.parser.Parser;

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
 * Builds a Tapestry Library project incrementally
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class IncrementalLibraryBuild extends LibraryBuild implements IIncrementalBuild
{

    /**
     * Constructor for IncrementalBuilder.
     * @param builder
     */
    public IncrementalLibraryBuild(TapestryBuilder builder)
    {
        super(builder);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.IIncrementalBuild#canIncrementalBuild(org.eclipse.core.resources.IResourceDelta)
     */
    public boolean canIncrementalBuild(IResourceDelta projectDelta)
    {
        if (super.canIncrementalBuild(projectDelta))
        {}
        return false;

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.FullBuild#getNamespaceResolver()
     */
    protected NamespaceResolver getNamespaceResolver(Parser parser)
    {
        return new NamespaceResolver(this, parser, true);
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
        saveBinaryLibraries(fFrameworkNamespace, fApplicationNamespace, newState);
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
            fTapestryBuilder.fCurrentProject,
            newState);
    }


}
