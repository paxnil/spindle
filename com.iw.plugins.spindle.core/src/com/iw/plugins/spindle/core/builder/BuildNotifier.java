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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.iw.plugins.spindle.core.resources.IResourceDescriptor;
/**
 * Notifies users of the progress of a Build
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class BuildNotifier {

  protected IProgressMonitor monitor;
  protected boolean cancelling;
  protected float percentComplete;
  protected float processingProgress;
  protected int workDone;
  protected int totalWork;
  protected String previousSubtask;
  protected IProject currentProject;

  public BuildNotifier(IProgressMonitor monitor, IProject currentProject) {
    this.monitor = monitor;
    this.cancelling = false;
    this.workDone = 0;
    this.totalWork = 1000000;
    this.currentProject = currentProject;
  }

  public void aboutToProcess(IResource resource) {
    aboutToProcess(resource.getName());
  }

  public void aboutToProcess(IResourceDescriptor descriptor) {
    aboutToProcess(descriptor.getName());
  }

  private void aboutToProcess(String message) {
    subTask("processing " + message);
  }

  public void processed(IResource resource) {
    processed(resource.getName());
  }

  public void processed(IResourceDescriptor descriptor) {
    processed(descriptor.getName());
  }

  private void processed(String message) {
    subTask("processing" + message);
    updateProgressDelta(processingProgress);
    checkCancel();
  }

  public void setProcessingProgress(float progress) {
    this.processingProgress = progress;
  }

  public void begin() {
    if (monitor != null) {
      monitor.beginTask("", totalWork);
    }
    this.previousSubtask = null;
  }

  public void checkCancel() {
    if (monitor != null && monitor.isCanceled()) {
      throw new OperationCanceledException();
    }
  }

  /**
   * Method done.
   */
  public void done() {
    updateProgress(1.0f);
    subTask("Tapestry Builder is finished");
    if (monitor != null) {
      monitor.done();
    }
    this.previousSubtask = null;
  }

  public void updateProgress(float percentComplete) {
    if (percentComplete > this.percentComplete) {
      this.percentComplete = Math.min(percentComplete, 1.0f);
      int work = Math.round(this.percentComplete * this.totalWork);
      if (work > this.workDone) {
        if (monitor != null) {
          monitor.worked(work - this.workDone);
        }
        if (TapestryBuilder.DEBUG) {
          System.out.println(
            java.text.NumberFormat.getPercentInstance().format(this.percentComplete));
        }
        this.workDone = work;
      }
    }
  }

  public void updateProgressDelta(float percentWorked) {
    updateProgress(percentComplete + percentWorked);
  }

  public void subTask(String message) {
    //	String pm = problemsMessage();
    //	String msg = pm.length() == 0 ? message : pm + " " + message; //$NON-NLS-1$

    if (message.equals(this.previousSubtask))
      return; // avoid refreshing with same one
    //if (JavaBuilder.DEBUG) System.out.println(msg);
    if (monitor != null) {
      monitor.subTask(message);
    }

    this.previousSubtask = message;
  }

}
