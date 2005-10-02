/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.core.resources.eclipse;

import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.search.ISearch;
import com.iw.plugins.spindle.core.resources.search.ISearchAcceptor;

// does not stay up to date as time goes on!

public class ContextSearch implements ISearch
{

  private Visitor fVisitor;

  protected IPackageFragmentRoot[] fPackageFragmentRoots = null;

  protected HashMap fPackageFragments;

  protected IContainer fRootContainer;

  private boolean fInitialized = false;

  public ContextSearch()
  {
  }

  public void configure(Object root)
  {
    this.fRootContainer = (IContainer) root;
    fVisitor = new Visitor();
    fInitialized = true;
  }

  public void search(ISearchAcceptor acceptor)
  {
    if (!fInitialized)
      throw new Error("not initialized");

    fVisitor.setAcceptor(acceptor);
    try
    {
      fRootContainer.accept(fVisitor);
    } catch (CoreException e)
    {
      TapestryCore.log(e);
    } catch (StopSearchingException e1)
    {
        //eat it
    }
  }

  class Visitor implements IResourceVisitor
  {
    ISearchAcceptor acceptor;

    public void setAcceptor(ISearchAcceptor acceptor)
    {
      this.acceptor = acceptor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
     */
    public boolean visit(IResource resource) throws CoreException
    {
      if (resource instanceof IFile)
      {
        boolean keepGoing = acceptor.accept(resource.getParent(), (IStorage) resource);
        if (!keepGoing)
          throw new StopSearchingException();
      }
      return true;
    }
  }

  class StopSearchingException extends RuntimeException
  {
    public StopSearchingException()
    {
      super();
    }

  }
}