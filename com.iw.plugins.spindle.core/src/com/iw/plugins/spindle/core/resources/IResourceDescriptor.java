package com.iw.plugins.spindle.core.resources;
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

import java.io.InputStream;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

/**
 * Extends <code>net.sf.tapestry.IResourceLocation<code> to record additional
 * bits of information describing a Tapestry artifact found in the workspace.
 * <br>
 * Including:
 * <ul>
 *   <li>the project that the artifact lives in</li>
 *   <li>is the artifact an IResource rather than an IStorage</li>
 *   <li>a flag describing the kind of location this is</li>
 *   <li>if appropriate, the descriptor describing the Tapestry artifact that owns this one.</li>
 * </ul>
 * <br>
 * The location kind will be one of the following:
 * <br>
 * <ul>
 *   <li><code>CLASSPATH_SRC</code> if the artifact lives in the src folders of the project</li>
 *   <li><code>CLASSPATH_BINARY</code> if the artifact lives in a jar file on the classpath</li>
 *   <li><code>APPLICATION_ROOT</code> if the artifact lives in the folder designated as the app root</li>
 *   <li><code>CONTEXT_ROOT</code>if the artifact lives in the folder designated as the servlet context root</li>
 *   <li><code>UNKNOWN</code>if not one of the above</li>
 * </ul>
 * 
 * Here is a not too exhaustive list of ownership possibilities:
 * <ul>
 *   <li>A page or component spec is owned by an application or library</li>
 *   <li>A template is owned by a page or component spec</li>
 *   <li>Applications and Libraries are not owned by anything<li>
 * </ul>
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 * @see net.sf.tapestry.IResourceLocation
 */

public interface IResourceDescriptor extends IResourceLocation
{

    /** the artifact lives in the source folders of its project **/
    public static final int CLASSPATH_SRC = 0;
    /** the artifact lives in a jar file in the build path of its project **/
    public static final int CLASSPATH_BINARY = 1;
    /** the artifact lives in designated app root folder **/
    public static final int APPLICATION_ROOT = 2;
    /** the artifact lives in designated servlet context root folder **/
    public static final int CONTEXT_ROOT = 3;
    /** the artifact lives outside of the above **/
    public static final int UNKNOWN = Integer.MAX_VALUE;

    /**
     * return the workspace storage associated with this descriptor
     * <br>
     * Using IStorage here instead of IResource as some things will come from
     * Jar files.
     */
    public IStorage getStorage();

    public boolean isWorkspaceResource();

    public boolean isOnClasspath();

    public boolean isUnderContextRoot();

    public boolean isUnderApplicationRoot();

    /**
     * return the project that contains the artifact
     */
    public IProject getProject();

    /**
     * return the object that owns this one.
     */
    public Object getOwner();

    /**
     * Returns an open input stream on the contents of this descriptor.
     * The caller is responsible for closing the stream when finished.
     * 
     *   @exception CoreException if the contents of this storage could 
     *		not be accessed.   See any refinements for more information.  
     */
    public InputStream getContents() throws CoreException;

}
