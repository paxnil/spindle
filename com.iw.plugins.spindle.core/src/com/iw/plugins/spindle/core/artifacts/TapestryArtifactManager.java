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

package com.iw.plugins.spindle.core.artifacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.templates.ITemplateFinderListener;

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

    public Object getLastBuildState(IProject project)
    {

        if (!TapestryCore.hasTapestryNature(project))
            return null;

        return fProjectBuildStates.get(project);
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

}
