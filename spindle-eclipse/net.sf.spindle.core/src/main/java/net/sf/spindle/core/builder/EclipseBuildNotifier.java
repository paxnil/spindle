package net.sf.spindle.core.builder;

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

import net.sf.spindle.core.build.AbstractBuildNotifier;
import net.sf.spindle.core.build.IBuildNotifier;
import net.sf.spindle.core.util.Assert;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Eclipse implementation of {@link core.builder.IBuildNotifier}
 * 
 * @author glongman@gmail.com
 */
public class EclipseBuildNotifier extends AbstractBuildNotifier implements IBuildNotifier
{

    protected IProgressMonitor monitor;

    public EclipseBuildNotifier(IProgressMonitor monitor)
    {
        super();
        Assert.isNotNull(monitor);
        this.monitor = monitor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildNotifier#internalBegin()
     */
    protected void internalBegin()
    {
        monitor.beginTask("", fTotalWork);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildNotifier#internalCheckCancel()
     */
    protected boolean internalCheckCancel()
    {
        return monitor.isCanceled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildNotifier#internalDone()
     */
    protected void internalDone()
    {
        monitor.done();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildNotifier#internalUpdateProgress(int)
     */
    protected void internalUpdateProgress(int work)
    {
        monitor.worked(work - this.fWorkDone);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuildNotifier#internalSubtask(java.lang.String)
     */
    protected void internalSubtask(String message)
    {
        monitor.subTask(message);
    }
}