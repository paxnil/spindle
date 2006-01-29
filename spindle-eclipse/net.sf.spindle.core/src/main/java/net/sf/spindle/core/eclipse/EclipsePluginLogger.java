package net.sf.spindle.core.eclipse;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.sf.spindle.core.ILogger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author gwl
 */
public class EclipsePluginLogger implements ILogger
{
    private org.eclipse.core.runtime.ILog fEclipseLog;

    private String fPluginId;

    public EclipsePluginLogger(org.eclipse.core.runtime.ILog eclipseLog, String pluginId)
    {
        fEclipseLog = eclipseLog;
        fPluginId = pluginId;
    }

    public void log(String msg)
    {
        Status status = new Status(IStatus.ERROR, fPluginId, IStatus.ERROR, msg + "\n", null);
        fEclipseLog.log(status);
    }

    public void log(Throwable ex)
    {
        log(null, ex);
    }

    public void log(String message, Throwable ex)
    {
        StringWriter stringWriter = new StringWriter();
        if (message != null)
        {
            stringWriter.write(message);
            stringWriter.write('\n');
        }

        ex.printStackTrace(new PrintWriter(stringWriter));
        String msg = stringWriter.getBuffer().toString();

        Status status = new Status(IStatus.ERROR, fPluginId, IStatus.ERROR, msg, null);
        fEclipseLog.log(status);
    }

}
