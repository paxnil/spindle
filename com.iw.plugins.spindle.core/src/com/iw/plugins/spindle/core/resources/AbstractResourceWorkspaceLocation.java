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

import java.net.URL;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 *  Abstract base class for implementations of IResourceWorkspaceLocations.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class AbstractResourceWorkspaceLocation implements IResourceWorkspaceLocation
{

    private IPath _path;

    protected AbstractResourceWorkspaceLocation(String path)
    {
        _path = new Path(path);
    }
    

    public String getName()
    {
        return _path.lastSegment();
    }

    private IPath getFolderPath()
    {
        return _path.removeLastSegments(1);
    }

    public IResourceLocation getRelativeLocation(String name)
    {
        if (name.startsWith("/"))
        {
            if (name.equals(_path))
                return this;

            return buildNewResourceLocation(name);
        }

        if (name.equals(getName()))
            return this;

        return buildNewResourceLocation(getFolderPath().append(name).toString());
    }

    public String getPath()
    {
        return _path.toString();
    }

    protected IPath getIPath() {
        return _path;
    }

    protected abstract IResourceLocation buildNewResourceLocation(String path);

    /**
     *  Returns true if the other object is an instance of the
     *  same class, and the paths are equal.
     * 
     **/

    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (obj.getClass().equals(getClass()))
        {
            AbstractResourceWorkspaceLocation otherLocation = (AbstractResourceWorkspaceLocation) obj;

            return _path.equals(otherLocation._path);
        }

        return false;
    }
    /* (non-Javadoc)
     * 
     * TODO what do we do with this? nothing?
     * 
     * @see org.apache.tapestry.IResourceLocation#getResourceURL()
     */
    public URL getResourceURL()
    {
        throw new Error("no implemented, yet");
    }

}
