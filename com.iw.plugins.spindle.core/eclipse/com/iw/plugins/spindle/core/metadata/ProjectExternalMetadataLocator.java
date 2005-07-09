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
package com.iw.plugins.spindle.core.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;

/**
 * <p>
 * This class provides project metadata as found by contributions to the extension point
 * <i>com.iw.plugins.spindle.core.projectMetadataLocator </i> Each contibution is queried in turn
 * and the first one to return a valid result wins.
 * </p>
 * <p>
 * This class also registers as a listener to the extension registry in order to keep the internal
 * cache of contributions up to date as plug-ins are activated/deactived.
 * </p>
 */
public class ProjectExternalMetadataLocator implements IRegistryChangeListener
{
    /**
     * the cache of contributed IProjectMetadataLocator keyed on project natureId.
     */
    protected HashMap fLocatorCache;

    public ProjectExternalMetadataLocator()
    {
        super();
        fLocatorCache = new HashMap();
        registerLocatorProxies();
        Platform.getExtensionRegistry().addRegistryChangeListener(this);
    }

    /**
     * release all held references and unregister as a contribution registry listener.
     */
    public void destroy()
    {
        fLocatorCache = null;
        Platform.getExtensionRegistry().removeRegistryChangeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    public void registryChanged(IRegistryChangeEvent event)
    {
        HashSet toRemove = null;
        String id = TapestryCorePlugin.NATURE_ID + ".projectMetaDataLocator";
        IExtensionDelta[] deltas = event.getExtensionDeltas();

        for (int i = 0; i < deltas.length; i++)
        {
            // must be extensions our extension point
            if (!id.equals(deltas[i].getExtensionPoint().getUniqueIdentifier()))
                continue;
            if (deltas[i].getKind() == IExtensionDelta.ADDED)
            {
                registerExtension(deltas[i].getExtension());
            }
            else
            {
                if (toRemove == null)
                    toRemove = new HashSet();
                toRemove.add(deltas[i].getExtension());
            }
        }

        if (toRemove == null)
            return;

        for (Iterator outer = fLocatorCache.values().iterator(); outer.hasNext();)
        {
            for (Iterator inner = ((List) outer.next()).iterator(); inner.hasNext();)
            {
                ProjectMetadataLocatorProxy proxy = (ProjectMetadataLocatorProxy) inner.next();
                IExtension ext = proxy.getExtension();

                if (toRemove.contains(ext))
                    inner.remove();
            }
        }
    }

    /**
     * <p>
     * Locate and return the project folder corresponding to the WEB-INF folder expected in the
     * struture of an exploded war layout.
     * </p>
     * <p>
     * The lookup will consult any <code>IProjectMetadataLocators</code> implementations
     * contributed via the extension point. If no contribution is found, the existing spindle
     * metadata file is examined. If it's still the case that the folder can not be found, or if an
     * exception occurs, null will be returned.
     * </p>
     * 
     * @param natureId
     *            the natureId that might yield interesting metadata.
     * @param project
     *            the <code>IProject</code> of interest
     * @return an IFolder corresponding to the project's WEB-INF folder; or null if noe could be
     *         found.
     * @throws CoreException
     *             may be thrown by contributed locators or by the underlying platform
     */
    public IFolder getWebContextRootFolder(String natureId, IProject project) throws CoreException
    {
        IFolder result = null;
        List locators = (List) fLocatorCache.get(natureId);
        if (locators != null)
        {

            for (Iterator iter = locators.iterator(); iter.hasNext();)
            {
                IProjectMetadataLocator loc = (IProjectMetadataLocator) iter.next();
                result = loc.getWebContextRootFolder(natureId, project);
                if (result != null)
                    break;
            }
        }
        return result;
    }

    /**
     * Loads locators registered with the extension point from the plug-in registry.
     */
    private void registerLocatorProxies()
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(
                TapestryCore.IDENTIFIER,
                "projectMetaDataLocator");
        if (point == null)
            return;
        IExtension[] extensions = point.getExtensions();
        for (int i = 0; i < extensions.length; i++)
            registerExtension(extensions[i]);
    }

    private void registerExtension(IExtension extension)
    {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for (int j = 0; j < elements.length; j++)
        {
            ProjectMetadataLocatorProxy proxy = ProjectMetadataLocatorProxy
                    .createProxy(elements[j]);
            if (proxy != null)
                registerLocator(proxy, proxy.getNatureId());
        }
    }

    private void registerLocator(IProjectMetadataLocator locator, String natureId)
    {
        if (natureId == null || natureId.equals(""))
            return;

        List list = (List) fLocatorCache.get(natureId);
        if (list == null)
        {
            list = new ArrayList(5);
            fLocatorCache.put(natureId, list);
        }
        list.add(locator);
    }

}