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

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import com.iw.plugins.spindle.core.builder.TapestryBuilder;
import com.iw.plugins.spindle.core.resources.search.ISearch;

/**
 * Implementation of IResourceWorkspaceLocation for resources found within the
 * context of a web application.
 * 
 * @author glongman@gmail.com
 */
public class ContextResourceWorkspaceLocation extends AbstractResourceWorkspaceLocation
{

  protected ContextResourceWorkspaceLocation(ContextRootLocation root, String path)
  {
    super(root, path);
  }

  public ContextResourceWorkspaceLocation(ContextRootLocation root, IResource resource)
  {
    this(root, root.findRelativePath(resource));
  }

  public IContainer getContainer()
  {
    return ((ContextRootLocation) fRoot).getContainer(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getStorage()
   */
  public IStorage getStorage()
  {

    //if we are in a build, the storages get cached for speed.
    Map cache = TapestryBuilder.getStorageCache();

    if (cache != null && cache.containsKey(this))
      return (IStorage) cache.get(this);

    IStorage result = null;
    IContainer container = getContainer();
    if (container != null && getName() != null)
    {
      IStorage storage = (IStorage) container.getFile(new Path(getName()));
      IResource resource = (IResource) storage.getAdapter(IResource.class);
      if (resource != null && resource.exists())
        result = storage;
    }

    if (cache != null)
      cache.put(this, result);

    return result;
  }

  public IResource getResource()
  {
    IContainer container = getContainer();
    if (container != null && getName() != null)
    {
      IResource resource = container.findMember(new Path(getName()));
      if (resource == null || resource.exists())
        return resource;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isWorkspaceResource()
   */
  public boolean isWorkspaceResource()
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getProject()
   */
  public IProject getProject()
  {
    IFile found = (IFile) getStorage();
    return found.getProject();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getContents()
   */
  public InputStream getContents() throws CoreException
  {
    IStorage storage = getStorage();
    if (storage != null)
      return storage.getContents();

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.IResourceLocation#getLocalization(java.util.Locale)
   */
  public IResourceLocation getLocalization(Locale locale)
  {
    // TODO implement later
    throw new RuntimeException("not implemented");
  }

  //    public int hashCode()
  //    {
  //        HashCodeBuilder builder = new HashCodeBuilder(5589, 1117);
  //
  //        builder.append(getPath());
  //        builder.append(fRoot);
  //
  //        return builder.toHashCode();
  //    }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#seek(com.iw.plugins.spindle.core.resources.IResourceLocationRequestor)
   */
  public void lookup(IResourceLocationAcceptor requestor) throws CoreException
  {
    IContainer container = getContainer();
    if (container != null && container.exists())
    {
      IResource[] members = container.members(false);
      for (int i = 0; i < members.length; i++)
      {
        if (members[i] instanceof IContainer)
          continue;

        if (!requestor.accept((IResourceWorkspaceLocation) getRelativeLocation(members[i]
            .getName())))
          break;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#getSearch()
   */
  public ISearch getSearch() throws CoreException
  {
    return fRoot.getSearch();
  }
  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation#isBinary()
   */
  public boolean isBinary()
  {
    return false;
  }

}