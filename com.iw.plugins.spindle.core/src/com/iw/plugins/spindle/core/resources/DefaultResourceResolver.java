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
import java.util.Map;

import org.apache.tapestry.IResourceResolver;

/**
 *  Resolves only in the source folders!
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class DefaultResourceResolver implements IResourceResolver
{
    public static IResourceResolver Resolver = new DefaultResourceResolver();

    protected DefaultResourceResolver()
    {
        super();

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceResolver#getResource(java.lang.String)
     */
    public URL getResource(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceResolver#findClass(java.lang.String)
     */
    public Class findClass(String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.IResourceResolver#getClassLoader()
     */
    public ClassLoader getClassLoader()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ognl.ClassResolver#classForName(java.lang.String, java.util.Map)
     */
    public Class classForName(String arg0, Map arg1) throws ClassNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
