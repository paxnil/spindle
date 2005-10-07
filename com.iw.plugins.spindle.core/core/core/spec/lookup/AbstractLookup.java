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

package core.spec.lookup;

import org.apache.tapestry.spec.IComponentSpecification;


import core.namespace.ICoreNamespace;
import core.resources.ICoreResource;

/**
 * Base class for lookups
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractLookup
{

    private ICoreNamespace fFrameworkNamespace;

    private ICoreNamespace fNamespace;

    private ICoreResource fRootLocation;

    private ICoreResource fWebInfLocation;

    private ICoreResource fWebInfAppLocation;

    public void configure(ICoreNamespace namespace, ICoreNamespace frameworkNamespace)
    {
        this.fNamespace = namespace;
        this.fRootLocation = (ICoreResource) namespace.getSpecificationLocation();
        fWebInfLocation = null;
        fWebInfAppLocation = null;
    }

    public void configure(ICoreNamespace applicationNamespace, ICoreNamespace frameworkNamespace,
            String appNameFromWebXML)
    {
        this.fNamespace = applicationNamespace;
        this.fRootLocation = (ICoreResource) applicationNamespace
                .getSpecificationLocation();
        fWebInfLocation = (ICoreResource) fRootLocation.getRelativeResource("WEB-INF");
        fWebInfAppLocation = (ICoreResource) fWebInfLocation
                .getRelativeResource(appNameFromWebXML);
    }

    protected ICoreResource getRootLocation()
    {
        return fRootLocation;
    }

    protected ICoreResource getWebInfLocation()
    {
        return fWebInfLocation;
    }

    protected ICoreResource getWebInfAppLocation()
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
        }
        else
        {
            return lookupSpecification(null, name);
        }
    }

    protected abstract IComponentSpecification lookupSpecification(String libraryId, String type);

}