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

package com.iw.plugins.spindle.core.namespace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceLocationAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;

/**
 *  A Lookup that bases its searches on the namespace it is configured with.
 *  Does not take into account any sub namespaces.
 * 
 *  This lookup can find:
 * 
 *  .jwc files
 *  .page files
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class NamespaceResourceLookup
{
    
    
    /**
    * Accept flag for specifying .jwc files.
    */
    public int ACCEPT_JWC = 0x00000001;

    /**
     * Accept flag for specifying .page files.
     */
    public int ACCEPT_PAGE = 0x00000002;

    private List locations;
    private ResourceAcceptor acceptor;

    public void configure(PluginLibrarySpecification specification)
    {
        locations = new ArrayList();
        locations.add(specification.getSpecificationLocation());
    }

    public void configure(
        PluginApplicationSpecification specification,
        IResourceWorkspaceLocation contextRoot,
        String servletName)
    {
        locations = new ArrayList();
        IResourceWorkspaceLocation base = (IResourceWorkspaceLocation) specification.getSpecificationLocation();
        if (base.isOnClasspath())
        {
            locations.add(base);
            locations.add(contextRoot);
        } else
        {
            locations.add(base);
            if (servletName != null)
            {
                locations.add(contextRoot.getRelativeLocation("/WEB-INF/" + servletName));
            }
            locations.add(contextRoot.getRelativeLocation("/WEB_INF/"));
            locations.add(contextRoot);
        }
    }

    public IResourceWorkspaceLocation[] find(String name, boolean exactMatch, int acceptFlags)
    {
        if (locations == null)
        {
            throw new Error("not initialized");
        }
        if (acceptor == null)
        {
            acceptor = new ResourceAcceptor();
        }
        try
        {
            acceptor.reset(name, exactMatch, acceptFlags);
            for (Iterator iter = locations.iterator(); iter.hasNext();)
            {
                IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) iter.next();
                location.lookup(acceptor);
            }
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
        return acceptor.getResults();
    }

    class ResourceAcceptor implements IResourceLocationAcceptor
    {

        List results = new ArrayList();
        String expectedName;
        boolean exactMatch;
        int acceptFlags;

        public void reset(String name, boolean exactMatch, int acceptFlags)
        {
            results.clear();
            this.expectedName = name;
            this.exactMatch = exactMatch;
            this.acceptFlags = acceptFlags;
        }
        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.resources.IResourceLocationRequestor#accept(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation)
         */
        public boolean accept(IResourceWorkspaceLocation location)
        {
            String fullname = location.getName();
            String name = null;
            String extension = null;

            boolean match = false;
            if (fullname != null)
            {
                int cut = fullname.lastIndexOf('.');
                if (cut < 0)
                {
                    name = fullname;
                } else if (cut == 0)
                {
                    extension = fullname;
                } else
                {
                    name = fullname.substring(0, cut);
                    extension = fullname.substring(cut + 1);
                }
            }
            if (name != null)
            {
                if ("*".equals(expectedName))
                {
                    match = true;
                } else if (exactMatch)
                {
                    match = expectedName.equals(name);
                } else
                {
                    match = name.startsWith(expectedName);
                }
            }
            if (match)
            {
                if ("jwc".equals(extension))
                {
                    match = (acceptFlags & ACCEPT_JWC) != 0;
                }

                if ("page".equals(extension))
                {
                    match = (acceptFlags & ACCEPT_PAGE) != 0;
                } else
                {
                    match = false;
                }
            }
            if (match)
            {
                results.add(location);
            }
            return true;
        }

        public IResourceWorkspaceLocation[] getResults()
        {
            return (IResourceWorkspaceLocation[]) results.toArray(new IResourceWorkspaceLocation[results.size()]);
        }

    }

}
