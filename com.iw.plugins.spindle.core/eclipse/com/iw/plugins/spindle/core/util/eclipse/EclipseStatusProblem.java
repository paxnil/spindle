package com.iw.plugins.spindle.core.util.eclipse;

import org.eclipse.core.runtime.IStatus;

import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.util.Assert;

public class EclipseStatusProblem extends DefaultProblem
{

    static private int statusToProblemServerity(IStatus status)
    {
        switch (status.getSeverity())
        {
            case IStatus.ERROR:
                return IProblem.ERROR;
            case IStatus.WARNING:
                return IProblem.WARNING;
            case IStatus.INFO:
                return IProblem.INFO;
        }
        Assert
                .isLegal(
                        false,
                        "only statii with severity: ERROR, WARNING, && INFO can be problems!");
        return IProblem.INFO;
    }

    public EclipseStatusProblem(String type, IStatus status, ISourceLocation location,
            boolean isTemporary)
    {
        super(type, statusToProblemServerity(status), status.getMessage(), location, isTemporary, status.getCode());
    }
}
