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
package com.iw.plugins.spindle.editors.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.core.util.Markers;

/**
 * Action to Jump from a java file editor to a related tapestry spec (if one exists)
 * 
 * @see IEditorActionDelegate
 */
public class CUEditorJumpToSpecDelegate extends BaseJumpAction implements IEditorActionDelegate
{

    protected IType fType;

    protected IProject fProject;

    public CUEditorJumpToSpecDelegate()
    {
    }    

    /**
     * @see IEditorActionDelegate#run
     */
    //    public void run(IAction action)
    //    {
    //        if (fEditor == null)
    //            return;
    //
    //        IEditorInput input = fEditor.getEditorInput();
    //        IFile file = (IFile) input.getAdapter(IFile.class);
    //        if (file != null)
    //        {
    //            // must be a file
    //            TapestryProject tproject =
    // TapestryCore.getDefault().getTapestryProjectFor(file);
    //            if (tproject != null)
    //            {
    //                // must have tapestry nature
    //                fProject = tproject.getProject();
    //                Object buildState =
    //                    TapestryArtifactManager.getTapestryArtifactManager().getLastBuildState(fProject,
    // false);
    //                // must not be a broken build
    //                if (buildState == null &&
    // Markers.getBrokenBuildProblemsFor(fProject).length == 0)
    //                {
    //                    buildState =
    // TapestryArtifactManager.getTapestryArtifactManager().getLastBuildState(fProject,
    // true);
    //                }
    //
    //                if (buildState == null)
    //                    return;
    //
    //                fType = resolveType(file);
    //                if (fType == null)
    //                    return;
    //                doRun();
    //            }
    //        }
    //    }
    public void run(IAction action)
    {
        if (fEditor == null)
            return;

        IEditorInput input = fEditor.getEditorInput();
        IAdaptable file = getInputObject(input);
        if (file instanceof IClassFile) {
            showFailedMessage("Jump to Spec is not available for .class files");
        } else if (file != null)
        {
            // must be a file
            ITapestryProject tproject = (ITapestryProject) file.getAdapter(ITapestryProject.class);
            if (tproject != null)
            {
                // must have tapestry nature
                fProject = tproject.getProject();
                Object buildState = TapestryArtifactManager.getTapestryArtifactManager()
                        .getLastBuildState(fProject, false);
                // must not be a broken build TODO this can be replaced by making the above call
                // force a build
                if (buildState == null && Markers.getBrokenBuildProblemsFor(fProject).length != 0)
                {
                    buildState = TapestryArtifactManager.getTapestryArtifactManager()
                            .getLastBuildState(fProject, true);
                } 

                if (buildState == null){
                    showFailedMessage("'Jump to' requires that a successful Tapestry build has occured.");
                    return;
                }
                    

                fType = doResolveType(file);
                if (fType == null)
                    return;
                doRun();
            } else {
                showFailedMessage("Unable to determine this java file is in a Tapestry project");
            }
        } 
    }
    
    protected void showFailedMessage(String message) {
        showMessage("'Jump To' Failed", message);
    }

    protected IAdaptable getInputObject(IEditorInput input)
    {
        return (IAdaptable) input.getAdapter(IFile.class);
    }

//    protected TapestryProject doFindTapestryProject(Object obj)
//    {
//        if (obj instanceof IClassFile)
//        {
//            return findTapestryProject((IClassFile) obj);
//        }
//        else if (obj instanceof IFile)
//        {
//            return findTapestryProject((IFile) obj);
//        }
//        return null;
//    }

    protected IType doResolveType(Object obj)
    {
        if (obj instanceof IClassFile)
        {
            return resolveType((IClassFile) obj);
        }
        else if (obj instanceof IFile)
        {
            return resolveType((IFile) obj);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.editors.actions.BaseJumpAction#doRun()
     */
    protected void doRun()
    {
        List foundSpecs = TapestryArtifactManager.getTapestryArtifactManager().findTypeRefences(
                fProject,
                fType.getFullyQualifiedName());
        if (foundSpecs.isEmpty()) {
            showFailedMessage("Unable to reconcile references from Tapestry specs to this class");
            return;
        }

        if (foundSpecs.size() == 1)
        {
            BaseSpecLocatable locatable = (BaseSpecLocatable) foundSpecs.get(0);
            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) locatable
                    .getSpecificationLocation();
            reveal(location);
        }
        else
        {
            List locations = new ArrayList();
            for (Iterator iter = foundSpecs.iterator(); iter.hasNext();)
            {
                BaseSpecLocatable element = (BaseSpecLocatable) iter.next();
                IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) element
                        .getSpecificationLocation();
                if (location != null && location.getStorage() != null)
                    locations.add(location);
            }
            reveal(locations.toArray(new Object [] {}));
        }
    }

    protected ChooseLocationPopup getChooseLocationPopup(Object[] locations)
    {        
        return new ChooseSpecPopup(locations, true);
    }
    
    class ChooseSpecPopup extends ChooseLocationPopup
    {
        /**
         * @param templateLocations
         * @param forward
         */
        public ChooseSpecPopup(Object [] locations, boolean forward)
        {
            super(locations, forward);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.iw.plugins.spindle.editors.actions.BaseEditorAction.ChooseLocationPopup#getImage(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation)
         */
        protected Image getImage(IResourceWorkspaceLocation location)
        {
            String name = location.getName();
            if (name.endsWith(".jwc"))
                return Images.getSharedImage("component16.gif");
            if (name.endsWith(".page"))
                return Images.getSharedImage("page16.gif");
            if (name.endsWith(".application"))
                return Images.getSharedImage("application16.gif");
            return null;
        }

    }



    

}