package net.sf.spindle.core.util.eclipse;

import net.sf.spindle.core.source.DefaultProblem;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.util.Assert;

import org.eclipse.core.runtime.IStatus;

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
