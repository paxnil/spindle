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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.iw.plugins.spindle.ui.util.Revealer;

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

    public static IJavaElement getActiveEditorJavaInput()
    {

        IWorkbenchPage page = getDefault().getActivePage();

        if (page != null)
        {
            IEditorPart part = page.getActiveEditor();
            if (part != null)
            {
                IEditorInput editorInput = part.getEditorInput();
                if (editorInput != null)
                {
                    IJavaElement result = (IJavaElement) editorInput.getAdapter(IJavaElement.class);
                    if (result == null)
                    {
                        IResource nonjava = (IResource) editorInput.getAdapter(IResource.class);
                        if (nonjava != null)
                        {
                            IContainer parent = nonjava.getParent();
                            while (parent != null)
                            {
                                result = (IJavaElement) parent.getAdapter(IJavaElement.class);
                                if (result != null)
                                {
                                    break;
                                }
                                parent = parent.getParent();
                            }
                        }
                    }
                    return result;
                }

            }
        }
        return null;
    }
    /**
     * The constructor.
     */
    public UIPlugin(IPluginDescriptor descriptor)
    {
        super(descriptor);
        plugin = this;
        setupRevealer();
    }

    private void setupRevealer()
    {
        if (getActiveWorkbenchShell() == null)
        {
            setUpDeferredRevealer();

        } else
        {
            Revealer.start();
        }

    }

    private IWindowListener RevealerTrigger = new IWindowListener()
    {
        public void windowActivated(IWorkbenchWindow window)
        {
            Revealer.start();
            tearDownDeferredRevealer();
        }

        public void windowDeactivated(IWorkbenchWindow window)
        {}

        public void windowClosed(IWorkbenchWindow window)
        {}

        public void windowOpened(IWorkbenchWindow window)
        {}
    };

    private void setUpDeferredRevealer()
    {
        getWorkbench().addWindowListener(RevealerTrigger);
    }

    private void tearDownDeferredRevealer()
    {
        getWorkbench().removeWindowListener(RevealerTrigger);
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

    public IWorkbenchPage getActivePage()
    {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null)
        {
            return window.getActivePage();
        }
        return null;
    }

    public String getPluginId()
    {
        return getDescriptor().getUniqueIdentifier();
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
