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

package com.iw.plugins.spindle.core.builder;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 *  Various components in the build will fire simple events when the Tapestry artifacts they are working
 *  on have dependencies on other things.
 * <p>
 * What's tracked:
 * <ul>
 * <li>Java type dependencies</li>
 * <li>Workspace resource dependencies</li>
 * </ul>
 * <p>
 * These are raw dependencies, the type or resource dependency may not even exist.
 * 
 *  Implementers must decide what to with dependancies for binary artifacts. I think
 *  that in most cases these dependencies should be ignored as binary artifacts never change.
 * 
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public interface IDependencyListener
{
    /**
     *  Notification that a dependant has a type dependency.
     * @param dependant the location of the dependant.
     * @param fullyQualifiedTypeName the fully qualified type name.
     */
    void foundTypeDependency(IResourceWorkspaceLocation dependant, String fullyQualifiedTypeName);
    /**
     *  Notification that a dependant has a dependency on another resource.
     * @param dependant  the location of the dependant.
     * @param dependancy  the location of the dependancy.
     */
    void foundResourceDependency(IResourceWorkspaceLocation dependant, IResourceWorkspaceLocation dependancy);
}
