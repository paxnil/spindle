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

package com.iw.plugins.spindle.core.resources;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;


/**
 *  Abstract base class for root locations
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class AbstractRootLocation implements IResourceWorkspaceLocation
{

    public IStorage getStorage()
    {
        throw new RuntimeException("can't get the storage from root!");
    }

    public InputStream getContents() throws CoreException
    {
        throw new RuntimeException("can't get the contents from root!");
    }

    public IResourceLocation getLocalization(Locale arg0)
    {
        throw new RuntimeException("can't get the localization from root!");
    }

    public String getName()
    {
        return "";
    }

    public String getPath()
    {
        return "/";
    }

    public URL getResourceURL()
    {
        return null;
    }    
}
