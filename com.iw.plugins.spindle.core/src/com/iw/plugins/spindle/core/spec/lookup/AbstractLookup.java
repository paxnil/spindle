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

package com.iw.plugins.spindle.core.spec.lookup;

import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 *  Base class for lookups
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class AbstractLookup
{

    private ICoreNamespace fFrameworkNamespace;

    private ICoreNamespace fNamespace;

    private IResourceWorkspaceLocation fRootLocation;

    private IResourceWorkspaceLocation fWebInfLocation;

    private IResourceWorkspaceLocation fWebInfAppLocation;

    public void configure(ICoreNamespace namespace, ICoreNamespace frameworkNamespace)
    {
        this.fNamespace = namespace;
        this.fRootLocation = (IResourceWorkspaceLocation) namespace.getSpecificationLocation();
        fWebInfLocation = null;
        fWebInfAppLocation = null;
    }

    public void configure(ICoreNamespace applicationNamespace, ICoreNamespace frameworkNamespace, String appNameFromWebXML)
    {
        this.fNamespace = applicationNamespace;
        this.fRootLocation = (IResourceWorkspaceLocation) applicationNamespace.getSpecificationLocation();
        fWebInfLocation = (IResourceWorkspaceLocation) fRootLocation.getRelativeLocation("WEB-INF");
        fWebInfAppLocation = (IResourceWorkspaceLocation) fWebInfLocation.getRelativeLocation(appNameFromWebXML);
    }

    protected IResourceWorkspaceLocation getRootLocation()
    {
        return fRootLocation;
    }
    protected IResourceWorkspaceLocation getWebInfLocation()
    {
        return fWebInfLocation;
    }

    protected IResourceWorkspaceLocation getWebInfAppLocation()
    {
        return fWebInfAppLocation;
    }

    protected ICoreNamespace getNamespace()
    {
        return fNamespace;
    }

    protected ICoreNamespace getFrameworkNamespace()
    {
        return fFrameworkNamespace;
    }

    public IComponentSpecification lookupSpecification(String name)
    {
        int colonx = name.indexOf(':');

        if (colonx > 0)
        {
            String libraryId = name.substring(0, colonx);
            String simpleType = name.substring(colonx + 1);

            return lookupSpecification(libraryId, simpleType);
        } else
        {
            return lookupSpecification(null, name);
        }
    }

    protected abstract IComponentSpecification lookupSpecification(String libraryId, String type);

}
