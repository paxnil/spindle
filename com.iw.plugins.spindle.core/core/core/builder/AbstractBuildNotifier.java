package core.builder;

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

import org.apache.hivemind.Resource;

/**
 * Notifies users of the progress of a AbstractBuild
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractBuildNotifier implements IBuildNotifier
{
    protected float fPercentComplete;

    protected float fProcessingProgress;

    protected int fWorkDone;

    protected int fTotalWork;

    protected String fPreviousSubtask;

    public AbstractBuildNotifier()
    {
        this.fWorkDone = 0;
        this.fTotalWork = 1000000;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#aboutToProcess(org.apache.hivemind.Resource)
     */
    public final void aboutToProcess(Resource resource)
    {
        aboutToProcess(resource.getName());
    }

    private void aboutToProcess(String message)
    {
        subTask("processing " + message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#processed(org.apache.hivemind.Resource)
     */
    public final  void processed(Resource resource)
    {
        processed(resource.getName());
    }

    private void processed(String message)
    {
        subTask("processed " + message);
        updateProgressDelta(fProcessingProgress);
        checkCancel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#setProcessingProgressPer(float)
     */
    public final void setProcessingProgressPer(float progress)
    {
        this.fProcessingProgress = progress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#begin()
     */
    public final void begin()
    {
        internalBegin();
        this.fPreviousSubtask = null;
    }

    /**
     * Clients should override with platform specific code to indeicate that a build has begun.
     * <p>
     * For example, open a progress dialog.
     */
    protected abstract void internalBegin();

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#checkCancel()
     */
    public final void checkCancel()
    {
        if (internalCheckCancel())
            throw new BuildCancelledException();
    }

    /**
     * Clients should override with whatever platform specific check is needed to indicated that the
     * user has cancelled the build.
     * 
     * @return true if the user has cancelled the build.
     */
    protected abstract boolean internalCheckCancel();

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#done()
     */
    public final void done()
    {
        updateProgress(1.0f);
        subTask("Tapestry Builder is finished");

        internalDone();

        this.fPreviousSubtask = null;
    }

    /**
     * Clients should override with whatever final cleanup measure needs to occur.
     */
    protected abstract void internalDone();

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#updateProgress(float)
     */
    public final void updateProgress(float percentComplete)
    {
        if (percentComplete > this.fPercentComplete)
        {
            this.fPercentComplete = Math.min(percentComplete, 1.0f);
            int work = Math.round(this.fPercentComplete * this.fTotalWork);
            if (work > this.fWorkDone)
            {
                internalUpdateProgress(work);

                this.fWorkDone = work;
            }
        }
    }

    /**
     * Clients should override with platform specific code to inform the user of the build progress.
     * <p>
     * Typically this would be setting the finshed value of a progress bar.
     * 
     * @param work
     *            an integer in the range 0..100
     */
    protected abstract void internalUpdateProgress(int work);

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#getPercentComplete()
     */
    public final  float getPercentComplete()
    {
        return this.fPercentComplete;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#updateProgressDelta(float)
     */
    public final void updateProgressDelta(float percentWorked)
    {
        updateProgress(fPercentComplete + percentWorked);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuildNotifier#subTask(java.lang.String)
     */
    public void subTask(String message)
    {
        if (message.equals(this.fPreviousSubtask))
            return; // avoid refreshing with same one

        internalSubtask(message);

        this.fPreviousSubtask = message;
    }

    /**
     * Clients should override to give user feedback on progress.
     * <p>
     * An example would be updating the label of a progress dialog.
     * 
     * @param message
     *            a message indicating which resource is being processed.
     */
    protected abstract void internalSubtask(String message);

}