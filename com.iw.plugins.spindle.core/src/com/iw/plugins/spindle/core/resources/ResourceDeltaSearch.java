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

package com.iw.plugins.spindle.core.resources;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.resources.search.ISearch;
import com.iw.plugins.spindle.core.resources.search.ISearchAcceptor;

/**
 * A search for looking into resource deltas!
 * 
 * @author glongman@gmail.com
 * 
 */
public class ResourceDeltaSearch implements ISearch, IResourceDeltaVisitor
{

  IResourceDelta fDelta;
  ISearchAcceptor fAcceptor;
  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.search.ISearch#configure(java.lang.Object)
   */
  public void configure(Object root) throws CoreException
  {
    fDelta = (IResourceDelta) root;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.search.ISearch#search(com.iw.plugins.spindle.core.resources.search.ISearchAcceptor)
   */
  public void search(ISearchAcceptor acceptor)
  {
    fAcceptor = acceptor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
   */
  public boolean visit(IResourceDelta delta) throws CoreException
  {
    IResource resource = delta.getResource();

    if (resource instanceof IProject || resource instanceof IFolder)
      return true;

    return fAcceptor.accept(delta, (IStorage) resource);

  }

}