package com.iw.plugins.spindle.core.builder;
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

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
/**
 * Notifies users of the progress of a Build
 * 
 * 
 * @author glongman@gmail.com
 */
public class BuildNotifier
{

  protected IProgressMonitor fMonitor;
  protected boolean fCancelling;
  protected float fPercentComplete;
  protected float fProcessingProgress;
  protected int fWorkDone;
  protected int fTotalWork;
  protected String fPreviousSubtask;
  protected IProject fCurrentProject;

  public BuildNotifier(IProgressMonitor monitor, IProject currentProject)
  {
    this.fMonitor = monitor;
    this.fCancelling = false;
    this.fWorkDone = 0;
    this.fTotalWork = 1000000;
    this.fCurrentProject = currentProject;
  }

  public void aboutToProcess(IResource resource)
  {
    aboutToProcess(resource.getName());
  }

  public void aboutToProcess(IResourceWorkspaceLocation descriptor)
  {
    aboutToProcess(descriptor.getName());
  }

  private void aboutToProcess(String message)
  {
    subTask("processing " + message);
  }

  public void processed(IResource resource)
  {
    processed(resource.getName());
  }

  public void processed(IResourceLocation descriptor)
  {
    processed(descriptor.getName());
  }

  public void processed(String message)
  {
    subTask("processed " + message);
    updateProgressDelta(fProcessingProgress);
    checkCancel();
  }

  public void setProcessingProgressPer(float progress)
  {
    this.fProcessingProgress = progress;
  }

  public void begin()
  {
    if (fMonitor != null)
      fMonitor.beginTask("", fTotalWork);

    this.fPreviousSubtask = null;
  }

  public void checkCancel()
  {
    if (fMonitor != null && fMonitor.isCanceled())
      throw new OperationCanceledException();
  }

  /**
   * Method done.
   */
  public void done()
  {
    updateProgress(1.0f);
    subTask("Tapestry Builder is finished");
    if (fMonitor != null)
      fMonitor.done();

    this.fPreviousSubtask = null;
  }

  public void updateProgress(float percentComplete)
  {
    if (percentComplete > this.fPercentComplete)
    {
      this.fPercentComplete = Math.min(percentComplete, 1.0f);
      int work = Math.round(this.fPercentComplete * this.fTotalWork);
      if (work > this.fWorkDone)
      {
        if (fMonitor != null)
          fMonitor.worked(work - this.fWorkDone);

//        if (TapestryBuilder.DEBUG)
//          System.out.println(java.text.NumberFormat.getPercentInstance().format(
//              this.fPercentComplete));

        this.fWorkDone = work;
      }
    }
  }

  public void updateProgressDelta(float percentWorked)
  {
    updateProgress(fPercentComplete + percentWorked);
  }

  public void subTask(String message)
  {
    //	String pm = problemsMessage();
    //	String msg = pm.length() == 0 ? message : pm + " " + message;
    // //$NON-NLS-1$

    if (message.equals(this.fPreviousSubtask))
      return; // avoid refreshing with same one
    //if (JavaBuilder.DEBUG) System.out.println(msg);
    if (fMonitor != null)
      fMonitor.subTask(message);

    this.fPreviousSubtask = message;
  }

}