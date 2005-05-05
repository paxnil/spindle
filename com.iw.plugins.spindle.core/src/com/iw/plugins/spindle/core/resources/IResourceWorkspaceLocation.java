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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.io.InputStream;

import org.apache.hivemind.Resource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.resources.search.ISearch;

/**
 * Extends
 * <code>org.apache.tapestry.IResourceLocation<code> to record additional
 * bits of information describing a Tapestry artifact found in the workspace.
 * 
 * @author glongman@gmail.com
 * 
 * @see org.apache.tapestry.IResourceLocation
 */

public interface IResourceWorkspaceLocation extends Resource
{

  //    public boolean exists();

  /**
   * return the workspace storage associated with this descriptor <br>
   * Using IStorage here instead of IResource as some things will come from Jar
   * files.
   */
  public IStorage getStorage();

  public boolean isWorkspaceResource();

  public boolean isOnClasspath();

  public boolean isBinary();

  /**
   * return the project that contains the artifact
   */
  public IProject getProject();

  /**
   * Returns an open input stream on the contents of this descriptor. The caller
   * is responsible for closing the stream when finished.
   * 
   * @exception CoreException if the contents of this storage could not be
   *              accessed. See any refinements for more information.
   */
  public InputStream getContents() throws CoreException;

  /**
   * iterate over all the direct descendants of this location passing each to
   * the requestor
   * 
   * does not include folders
   * 
   * @param requestor an instance of IResourceLocationRequestor
   * @throws CoreException
   */
  public void lookup(IResourceLocationAcceptor requestor) throws CoreException;

  /**
   * return a propertly configured instance of ISearch
   * 
   * @throws CoreException if the search could not configured
   */

  public ISearch getSearch() throws CoreException;

}