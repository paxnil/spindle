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
package com.iw.plugins.spindle.ui.wizards;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;
import com.iw.plugins.spindle.core.util.Assert;

class ResourceCreationRunnable implements IRunnableWithProgress
{

  public static final int DO = 0;
  public static final int UNDO = 1;
  private List filesToCreate = new ArrayList();
  private List createdResources = new ArrayList();
  private int mode = DO;

  public void setMode(int mode)
  {
    Assert.isTrue(mode == DO || mode == UNDO);
    this.mode = mode;
  }

  public void addFile(IFile file)
  {
    Assert.isNotNull(file);
    filesToCreate.add(file);
  }

  public void run(IProgressMonitor monitor) throws InvocationTargetException,
      InterruptedException
  {
    if (mode == DO)
    {
      if (filesToCreate.isEmpty())
        return;

      for (Iterator iter = filesToCreate.iterator(); iter.hasNext();)
      {
        try
        {
          create((IFile) iter.next(), false, monitor);
        } finally
        {
          iter.remove();
        }
      }
    } else
    {
      undoCreate(monitor);
    }
  }
  private final void undoCreate(IProgressMonitor monitor)
  {
    try
    {
      for (Iterator iter = createdResources.iterator(); iter.hasNext();)
      {
        IResource toBeRemoved = (IResource) iter.next();
        toBeRemoved.delete(true, monitor);
      }
    } catch (CoreException e)
    {
      // log it and eat it.
      UIPlugin.log(e);
    } finally
    {
      createdResources.clear();
    }
  }

  protected final void create(IFile file, boolean okIfExists, IProgressMonitor monitor) throws InvocationTargetException
  {
    if (file.exists())
    {
      if (!okIfExists)
        throw new InvocationTargetException(TapestryCorePlugin.createErrorException(file
            .getName()
            + " already exists"));
      return;
    }

    try
    {
      IFolder parent = (IFolder) file.getParent();
      Assert.isTrue(parent != null);
      if (!parent.exists())
      {

        LinkedList folders = new LinkedList();
        while (parent != null)
        {
          folders.addFirst(parent);
          parent = (IFolder) parent.getParent();
          if (parent == null || parent.exists())
            break;
        }

        for (Iterator iter = folders.iterator(); iter.hasNext();)
        {

          IFolder folder = (IFolder) iter.next();
          create(folder, false, monitor);
       
        }
      }

      file.create(new ByteArrayInputStream("".getBytes()), false, monitor);
      createdResources.add(file);
    } catch (CoreException e)
    {
      throw new InvocationTargetException(TapestryCorePlugin.createErrorException(e
          .getStatus()
          .getMessage()));
    }
  }

  protected final void create(IFolder folder, boolean okIfExists, IProgressMonitor monitor) throws InvocationTargetException
  {
    if (folder.exists())
    {
      if (!okIfExists)
        throw new InvocationTargetException(TapestryCorePlugin.createErrorException(folder
            .getFullPath()
            .toString()
            + " already exists"));
      return;
    }

    try
    {
      folder.create(false, true, monitor);
      createdResources.add(folder);
    } catch (CoreException e)
    {
      throw new InvocationTargetException(TapestryCorePlugin.createErrorException(e
          .getStatus()
          .getMessage()));
    }
  }

}