package com.iw.plugins.spindle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class UIPlugin extends AbstractUIPlugin
{
    private static ResourceBundle UIStrings;

    public static String getString(String key)
    {
        return getString(key, null);
    }
    public static String getString(String key, Object arg)
    {
        return getString(key, new Object[] { arg });
    }
    public static String getString(String key, Object arg1, Object arg2)
    {
        return getString(key, new Object[] { arg1, arg2 });
    }
    public static String getString(String key, Object arg1, Object arg2, Object arg3)
    {
        return getString(key, new Object[] { arg1, arg2, arg3 });
    }
    public static String getString(String key, Object[] args)
    {
        if (UIStrings == null)
            UIStrings = ResourceBundle.getBundle("com.iw.plugins.spindle.resources");
        try
        {
            String pattern = UIStrings.getString(key);
            if (args == null)
                return pattern;

            return MessageFormat.format(pattern, args);
        } catch (MissingResourceException e)
        {
            return "!" + key + "!";
        }
    }
    //The shared instance.
    private static UIPlugin plugin;

    static public void log(String msg)
    {
        ILog log = getDefault().getLog();
        Status status =
            new Status(
                IStatus.ERROR,
                getDefault().getDescriptor().getUniqueIdentifier(),
                IStatus.ERROR,
                msg + "\n",
                null);
        log.log(status);
    }

    static public void log(Exception ex)
    {
        ILog log = getDefault().getLog();
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        String msg = stringWriter.getBuffer().toString();

        Status status =
            new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), IStatus.ERROR, msg, null);
        log.log(status);
    }
    /**
     * The constructor.
     */
    public UIPlugin(IPluginDescriptor descriptor)
    {
        super(descriptor);
        plugin = this;
    }

    /**
     * Returns the shared instance.
     */
    public static UIPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public Shell getActiveWorkbenchShell()
    {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null)
        {
            return window.getShell();
        }
        return null;
    }

    public IWorkbenchWindow getActiveWorkbenchWindow()
    {
        IWorkbench workbench = getWorkbench();
        if (workbench != null)
        {
            return workbench.getActiveWorkbenchWindow();
        }
        return null;
    }
}
