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
package net.sf.spindle.core.metadata;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementations will return web metadata that Spindle needs to operate. Implementations are
 * registerd by natureId by adding and extension to the point
 * <i>core.projectMetadataLocator </i>
 */
public interface IProjectMetadataLocator
{
    /**
     * Locate and return the IFolder that contains the web context root of the project. The folder
     * must exist, be non null, and may not be the project itself. return values that violate the
     * above constraints will be ignored.
     * @param natureId
     *            the project nature id registered in the extension
     * @param project
     *            the IProject of interest
     * 
     * @return an IFolder defining the web context root (location of web.xml) or null if no such
     *         metadata can be found.
     * @throws CoreException
     */
    public IFolder getWebContextRootFolder(String natureId, IProject project) throws CoreException;

    
}