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

package com.iw.plugins.spindle.core.builder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.INamespace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.templates.ITemplateFinderListener;
import com.iw.plugins.spindle.core.util.Markers;

/**
 * The <code>TapestryArtifactManager</code> manages all the Tapestry Artifacts in the workspace.
 * The single instance of <code>TapestryArtifactManager</code> is available from
 * the static method <code>TapestryArtifactManager.getTapestryArtifactManager()</code>.
 * 
 * right now the models/build states are not persited between sessions.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TapestryArtifactManager implements ITemplateFinderListener
{

    static private TapestryArtifactManager instance = new TapestryArtifactManager();

    static public final TapestryArtifactManager getTapestryArtifactManager()
    {

        return instance;
    }

    Map fProjectBuildStates = new HashMap();
    List fTemplateExtensionListeners;

    private TapestryArtifactManager()
    {
        super();
    }

    /**
     * Sets the last built state for the given project, or null to reset it.
     */
    public void setLastBuildState(IProject project, Object state)
    {
        if (!TapestryCore.hasTapestryNature(project))
            return;

        fProjectBuildStates.put(project, state);
    }

    public void clearBuildState(IProject project)
    {
        fProjectBuildStates.remove(project);
    }

    public synchronized Object getLastBuildState(IProject project)
    {
        return getLastBuildState(project, null);
    }

    public synchronized Object getLastBuildState(IProject project, IRunnableContext context)
    {
        if (!TapestryCore.hasTapestryNature(project))
            return null;

        Object state = fProjectBuildStates.get(project);
        if (state == null)
        {
            try
            {
                buildStateIfRequired(project, context);
                state = fProjectBuildStates.get(project);
            } catch (CoreException e)
            {
                TapestryCore.log(e);
            }
        }
        return state;
    }

    /**
     * 
     */
    private void buildStateIfRequired(final IProject project, IRunnableContext context) throws CoreException
    {
        // don't bother building if the last one was busted beyond saving!
        if (Markers.getBrokenBuildProblemsFor(project).length > 0)
            return;

        IRunnableWithProgress runnable = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                try
                {
                    project.build(
                        IncrementalProjectBuilder.FULL_BUILD,
                        TapestryCore.BUILDER_ID,
                        new HashMap(),
                        monitor);
                } catch (CoreException e)
                {
                    TapestryCore.log(e);
                }
            }

        };

        if (context == null)
        {
            Shell shell = TapestryCore.getDefault().getActiveWorkbenchShell();
            if (shell != null && shell.getVisible())
            {
                try
                {
                    context = new ProgressMonitorDialog(shell);

                } catch (Exception e)
                {
                    TapestryCore.log(e);
                }
            }
        }

        if (context != null)
        {
            try
            {
                context.run(false, false, runnable);

            } catch (Exception e)
            {
                TapestryCore.log(e);
            }
        } else
        {
            project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#addListener(com.iw.plugins.spindle.core.scanning.IScannerValidatorListener)
     */
    public void addTemplateFinderListener(ITemplateFinderListener listener)
    {
        if (fTemplateExtensionListeners == null)
            fTemplateExtensionListeners = new ArrayList();

        if (!fTemplateExtensionListeners.contains(listener))
            fTemplateExtensionListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#removeListener(com.iw.plugins.spindle.core.scanning.IScannerValidatorListener)
     */
    public void removeTemplateFinderListener(ITemplateFinderListener listener)
    {
        if (fTemplateExtensionListeners != null)
            fTemplateExtensionListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.templates.ITemplateFinderListener#templateExtensionSeen(java.lang.String)
     */
    public void templateExtensionSeen(String extension)
    {
        if (fTemplateExtensionListeners == null)
            return;

        for (Iterator iter = fTemplateExtensionListeners.iterator(); iter.hasNext();)
        {
            ITemplateFinderListener listener = (ITemplateFinderListener) iter.next();
            listener.templateExtensionSeen(extension);
        }

    }

    public Map getTemplateMap(IProject project)
    {
        State state = (State) getLastBuildState(project);
        if (state != null)
            return state.fTemplateMap;
        return null;
    }

    public void invalidateBuildStates()
    {
        fProjectBuildStates.clear();
    }

    public Map getSpecMap(IProject project)
    {
        State state = (State) getLastBuildState(project);
        if (state != null)
            return state.fSpecificationMap;
        return null;
    }

    public INamespace getProjectNamespace(IProject project)
    {
        State state = (State) getLastBuildState(project);
        if (state != null)
            return state.fPrimaryNamespace;
        return null;
    }

}
