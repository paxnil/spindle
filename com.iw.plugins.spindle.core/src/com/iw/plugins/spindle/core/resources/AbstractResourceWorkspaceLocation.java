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
import org.eclipse.core.runtime.Path;

/**
 *  Abstract base class for implementations of IResourceWorkspaceLocations.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class AbstractResourceWorkspaceLocation implements IResourceWorkspaceLocation
{

    private String fPath;
    private String fName;
    protected AbstractRootLocation fRoot;

    protected AbstractResourceWorkspaceLocation(AbstractRootLocation root, String path)
    {
        this.fRoot = root;
        Path p = new Path(path);
        this.fPath = p.removeLastSegments(1).addTrailingSeparator().makeRelative().toString();
        this.fName = p.lastSegment();
    }

    public String getName()
    {
        return fName;
    }

    public IResourceLocation getRelativeLocation(String name)
    {
        if (exists())
        {
            if (name.startsWith("/"))
            {
                if (name.equals(fPath))
                {
                    return this;
                } else
                {
                    return fRoot.getRelativeLocation(name);
                }
            }

            if (name.equals(getName()))
                return this;

            return fRoot.getRelativeLocation(getPath() + name);
        }
        return null;
    }

    public String getPath()
    {
        return fPath;
    }

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
            AbstractResourceWorkspaceLocation other = (AbstractResourceWorkspaceLocation) obj;
            return this.fRoot.equals(other.fRoot) && this.fPath.equals(other.fPath) && this.fName.equals(other.fName);
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
        throw new Error("not implemented, yet");
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isOnClasspath()
     */
    public boolean isOnClasspath()
    {
        return fRoot.isOnClasspath();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(fRoot.toString());
        buffer.append(":");
        buffer.append(fPath);
        buffer.append(fName);
        return buffer.toString();
    }

}
