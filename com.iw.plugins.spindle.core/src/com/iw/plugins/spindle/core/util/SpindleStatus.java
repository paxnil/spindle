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
package com.iw.plugins.spindle.core.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Assert;

import com.iw.plugins.spindle.core.TapestryCore;

/**
 * @author GWL Copyright 2002, Intelligent Works Incoporated All Rights Reserved
 */
public class SpindleStatus implements IStatus
{

    private String statusMessage;

    private int severity;

    private int code = -1;

    /**
     * Compares two instances of <code>IStatus</code>. The more severe is returned: An error is
     * more severe than a warning, and a warning is more severe than ok. If the two stati have the
     * same severity, the second is returned.
     */
    public static IStatus getMoreSevere(IStatus s1, IStatus s2)
    {
        if (s1.getSeverity() > s2.getSeverity())
        {
            return s1;
        }
        else
        {
            return s2;
        }
    }

    /**
     * Finds the most severe status from a array of stati. An error is more severe than a warning,
     * and a warning is more severe than ok.
     */
    public static IStatus getMostSevere(IStatus[] status)
    {
        IStatus max = new SpindleStatus();
        for (int i = 0; i < status.length; i++)
        {
            IStatus curr = status[i];
            if (curr.matches(IStatus.ERROR))
            {
                return curr;
            }
            if (max == null || curr.getSeverity() > max.getSeverity())
            {
                max = curr;
            }
        }
        return max;
    }

    /**
     * Creates a status set to OK (no message)
     */
    public SpindleStatus()
    {
        this(OK, null);
    }

    /**
     * Creates a status .
     * 
     * @param severity
     *            The status severity: ERROR, WARNING, INFO and OK.
     * @param message
     *            The message of the status. Applies only for ERROR, WARNING and INFO.
     */
    public SpindleStatus(int severity, String message)
    {
        statusMessage = message;
        this.severity = severity;
    }

    public SpindleStatus(Throwable exception)
    {
        statusMessage = exception.getMessage();
        this.severity = ERROR;
    }

    /**
     * Returns if the status' severity is OK.
     */
    public boolean isOK()
    {
        return severity == IStatus.OK;
    }

    /**
     * Returns if the status' severity is WARNING.
     */
    public boolean isWarning()
    {
        return severity == IStatus.WARNING;
    }

    /**
     * Returns if the status' severity is INFO.
     */
    public boolean isInfo()
    {
        return severity == IStatus.INFO;
    }

    /**
     * Returns if the status' severity is ERROR.
     */
    public boolean isError()
    {
        return severity == IStatus.ERROR;
    }

    /**
     * @see IStatus#getMessage
     */
    public String getMessage()
    {
        return statusMessage;
    }

    /**
     * Sets the status to ERROR.
     * 
     * @param The
     *            error message (can be empty, but not null)
     */
    public void setError(String errorMessage)
    {
        Assert.isNotNull(errorMessage);
        statusMessage = errorMessage;
        severity = IStatus.ERROR;
    }

    /**
     * Sets the status to WARNING.
     * 
     * @param The
     *            warning message (can be empty, but not null)
     */
    public void setWarning(String warningMessage)
    {
        Assert.isNotNull(warningMessage);
        statusMessage = warningMessage;
        severity = IStatus.WARNING;
    }

    /**
     * Sets the status to INFO.
     * 
     * @param The
     *            info message (can be empty, but not null)
     */
    public void setInfo(String infoMessage)
    {
        Assert.isNotNull(infoMessage);
        statusMessage = infoMessage;
        severity = IStatus.INFO;
    }

    /**
     * Sets the status to OK.
     */
    public void setOK()
    {
        statusMessage = null;
        severity = IStatus.OK;
    }

    /*
     * @see IStatus#matches(int)
     */
    public boolean matches(int severityMask)
    {
        return (severity & severityMask) != 0;
    }

    /**
     * Returns always <code>false</code>.
     * 
     * @see IStatus#isMultiStatus()
     */
    public boolean isMultiStatus()
    {
        return false;
    }

    /*
     * @see IStatus#getSeverity()
     */
    public int getSeverity()
    {
        return severity;
    }

    /*
     * @see IStatus#getPlugin()
     */
    public String getPlugin()
    {
        return TapestryCore.PLUGIN_ID;
    }

    /**
     * Returns always <code>null</code>.
     * 
     * @see IStatus#getException()
     */
    public Throwable getException()
    {
        return null;
    }

    /**
     * Returns always the error severity.
     * 
     * @see IStatus#getCode()
     */
    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    /**
     * Returns always <code>null</code>.
     * 
     * @see IStatus#getChildren()
     */
    public IStatus[] getChildren()
    {
        return new IStatus[0];
    }

}