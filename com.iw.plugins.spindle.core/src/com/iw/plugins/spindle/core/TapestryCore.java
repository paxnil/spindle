package com.iw.plugins.spindle.core;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.tapestry.spec.SpecFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.parser.xml.dom.TapestryDOMParserConfiguration;
import com.iw.plugins.spindle.core.resources.ClasspathSearch;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.spec.TapestryCoreSpecFactory;

/**
 * The main plugin class to be used in the desktop.
 */
public class TapestryCore extends AbstractUIPlugin implements IPropertyChangeListener
{

    /**
     *  Used by label decorators to listen to Core changes
     */
    public static interface ICoreListener
    {

        public void coreChanged();

    }

    private static ResourceBundle TapestryStrings;
    private static ResourceBundle SpindleCoreStrings;

    public static final String PLUGIN_ID = "com.iw.plugins.spindle.core";
    public static final String NATURE_ID = PLUGIN_ID + ".tapestrynature";
    public static final String BUILDER_ID = PLUGIN_ID + ".tapestrybuilder";

    public static final String CACHE_GRAMMAR_PREFERENCE = PLUGIN_ID + ".cachinggrammars";
    public static final String CORE_CONTAINER = PLUGIN_ID + ".TAPESTRY_FRAMEWORK";

    public static final String SERVLET_2_2_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    public static final String SERVLET_2_3_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";

    /**
     * SpecFactory instance used by the Scanners
     */
    private static SpecFactory SPEC_FACTORY;

    //The shared instance.
    private static TapestryCore plugin;
    //Resource bundle.

    private static List CoreListeners;

    /**
     * The constructor.
     */
    public TapestryCore(IPluginDescriptor descriptor)
    {
        super(descriptor);
        plugin = this;
        try
        {} catch (MissingResourceException x)
        {
            SpindleCoreStrings = null;
        }
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

    /**
     * Returns the shared instance.
     */
    public static TapestryCore getDefault()
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

    static public void log(String msg)
    {
        TapestryCore core = getDefault();
        if (core == null)
        {
            System.err.println("TapestryCore log: " + msg);
        }
        ILog log = core.getLog();
        Status status =
            new Status(
                IStatus.ERROR,
                TapestryCore.getDefault().getDescriptor().getUniqueIdentifier(),
                IStatus.ERROR,
                msg + "\n",
                null);
        log.log(status);
    }

    static public void log(Exception ex)
    {
        TapestryCore core = getDefault();
        if (core == null)
        {
            System.err.println("TapestryCore log exception");
            ex.printStackTrace(System.err);
        }
        ILog log = core.getLog();
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        String msg = stringWriter.getBuffer().toString();

        Status status =
            new Status(
                IStatus.ERROR,
                TapestryCore.getDefault().getDescriptor().getUniqueIdentifier(),
                IStatus.ERROR,
                msg,
                null);
        log.log(status);
    }

    public static void addNatureToProject(IProject project, String natureId) throws CoreException
    {
        IProject proj = project.getProject(); // Needed if project is a IJavaProject
        IProjectDescription description = proj.getDescription();
        String[] prevNatures = description.getNatureIds();

        int natureIndex = -1;
        for (int i = 0; i < prevNatures.length; i++)
        {
            if (prevNatures[i].equals(natureId))
            {
                natureIndex = i;
                i = prevNatures.length;
            }
        }

        // Add nature only if it is not already there
        if (natureIndex == -1)
        {
            String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = natureId;
            description.setNatureIds(newNatures);
            proj.setDescription(description, null);
        }
        fireCoreListenerEvent();
    }

    public static boolean hasTapestryNature(IProject project)
    {
        try
        {
            return project.hasNature(NATURE_ID);
        } catch (CoreException e)
        {
            log(e);
        }
        return false;
    }

    public static void removeNatureFromProject(IProject project, String natureId) throws CoreException
    {
        IProject proj = project.getProject(); // Needed if project is a IJavaProject
        IProjectDescription description = proj.getDescription();
        String[] prevNatures = description.getNatureIds();

        int natureIndex = -1;
        for (int i = 0; i < prevNatures.length; i++)
        {
            if (prevNatures[i].equals(natureId))
            {
                natureIndex = i;
                i = prevNatures.length;
            }
        }

        // Remove nature only if it exists...
        if (natureIndex != -1)
        {
            String[] newNatures = new String[prevNatures.length - 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, natureIndex);
            System.arraycopy(
                prevNatures,
                natureIndex + 1,
                newNatures,
                natureIndex,
                prevNatures.length - (natureIndex + 1));
            description.setNatureIds(newNatures);
            proj.setDescription(description, null);
        }
        fireCoreListenerEvent();
    }

    public static synchronized void addCoreListener(ICoreListener listener)
    {
        if (CoreListeners == null)
            CoreListeners = new ArrayList();

        if (!CoreListeners.contains(listener))
            CoreListeners.add(listener);
    }

    public static void removeCoreListener(ICoreListener listener)
    {
        if (CoreListeners != null)
            CoreListeners.remove(listener);
    }

    private static void fireCoreListenerEvent()
    {
        if (CoreListeners == null)
            return;

        for (Iterator iter = CoreListeners.iterator(); iter.hasNext();)
        {
            ICoreListener listener = (ICoreListener) iter.next();
            // simple for now - may create an event type later
            listener.coreChanged();
        }
    }

    public static String getString(String key, Object[] args)
    {
        if (SpindleCoreStrings == null)
            SpindleCoreStrings = ResourceBundle.getBundle("com.iw.plugins.spindle.core.resources");
        try
        {
            String pattern = SpindleCoreStrings.getString(key);
            if (args == null)
                return pattern;

            return MessageFormat.format(pattern, args);
        } catch (MissingResourceException e)
        {
            return "!" + key + "!";
        }
    }

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

    public static String getTapestryString(String key, Object[] args)
    {
        if (TapestryStrings == null)
            TapestryStrings = ResourceBundle.getBundle("org.apache.tapestry.TapestryStrings");

        try
        {
            String pattern = TapestryStrings.getString(key);
            if (args == null)
                return pattern;

            return MessageFormat.format(pattern, args);
        } catch (MissingResourceException e)
        {
            return "!" + key + "!";
        }
    }

    public static String getTapestryString(String key)
    {
        return getTapestryString(key, null);
    }

    public static String getTapestryString(String key, Object arg)
    {
        return getTapestryString(key, new Object[] { arg });
    }

    public static String getTapestryString(String key, Object arg1, Object arg2)
    {
        return getTapestryString(key, new Object[] { arg1, arg2 });
    }

    public static String getTapestryString(String key, Object arg1, Object arg2, Object arg3)
    {
        return getTapestryString(key, new Object[] { arg1, arg2, arg3 });
    }

    public static boolean isNull(String value)
    {
        if (value == null)
            return true;

        if (value.length() == 0)
            return true;

        return value.trim().length() == 0;
    }

    public static void logProblem(IStorage storage, IProblem problem)
    {
        log(getString("core-non-resource-problem", storage.toString(), problem.toString()));
    }

    /**
     * @param path
     * @param problems
     */
    public static void logProblems(IStorage storage, IProblem[] problems)
    {
        if (problems != null)
        {
            for (int i = 0; i < problems.length; i++)
            {
                logProblem(storage, problems[i]);
            }
        }
    }

    /**
     * @return
     */
    public static SpecFactory getSpecificationFactory()
    {
        if (SPEC_FACTORY == null)
        {
            SPEC_FACTORY = new TapestryCoreSpecFactory();
        }
        return SPEC_FACTORY;
    }

    public static boolean isCachingDTDGrammars()
    {
        // short circuit for testing outside of an 
        // Eclipse runtime

        if (getDefault() == null)
            return true;

        return getDefault().getPreferenceStore().getBoolean(CACHE_GRAMMAR_PREFERENCE);
    }

    public static void setCachingDTDGrammars(boolean flag)
    {
        getDefault().getPreferenceStore().setValue(CACHE_GRAMMAR_PREFERENCE, flag);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
     */
    protected void initializeDefaultPreferences(IPreferenceStore store)
    {
        store.setDefault(CACHE_GRAMMAR_PREFERENCE, true);
        store.addPropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getProperty().equals(CACHE_GRAMMAR_PREFERENCE))
        {

            boolean oldValue = ((Boolean) event.getOldValue()).booleanValue();
            boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
            if (oldValue != newValue && !newValue)
            {
                //clear the cache
                if (!newValue)
                    TapestryDOMParserConfiguration.clearCache();

                // force full builds
                TapestryArtifactManager.getTapestryArtifactManager().invalidateBuildStates();
            }
        }

    }

    public void clearDTDCache()
    {
        TapestryDOMParserConfiguration.clearCache();
        // force full builds
        TapestryArtifactManager.getTapestryArtifactManager().invalidateBuildStates();
    }

    public IProject getProjectFor(IStorage storage)
    {
        ClasspathSearch lookup = null;
        if (storage instanceof JarEntryFile)
        {
            try
            {
                IWorkspace workspace = getWorkspace();
                IProject[] projects = workspace.getRoot().getProjects();
                for (int i = 0; i < projects.length; i++)
                {
                    if (!projects[i].isOpen())
                    {
                        continue;
                    }
                    if (lookup == null)
                    {
                        lookup = new ClasspathSearch();
                    }
                    IJavaProject jproject = getJavaProjectFor(projects[i]);
                    lookup.configure(jproject);
                    if (lookup.projectContainsJarEntry((JarEntryFile) storage))
                    {
                        return projects[i];
                    }
                }
            } catch (CoreException jmex)
            {
                jmex.printStackTrace();
            }
            return null;
        } else if (storage instanceof IResource)
        {
            IResource resource = (IResource) storage;
            if (resource.getType() == IResource.PROJECT)
            {
                return (IProject) resource;
            } else
            {
                return ((IResource) storage).getProject();
            }
        }
        return null;
    }

    public IJavaProject getJavaProjectFor(Object obj)
    {
        IProject project = null;
        if (obj instanceof IProject)
        {
            project = (IProject) obj;
        } else if (obj instanceof IResource)
        {
            project = ((IResource) obj).getProject();
        } else if (obj instanceof IStorage)
        {
            project = getProjectFor((IStorage) obj);
        } else if (obj instanceof IEditorInput)
        {
            //TODO warning - will always return null if its a JarEntryEditorInput!
            //Use Editor.getStorage() if you can.            
            project = getProjectFor((IStorage) ((IEditorInput) obj).getAdapter(IStorage.class));
        }
        if (project == null)
        {
            return null;
        }

        try
        {
            if (project.hasNature(JavaCore.NATURE_ID))
                return (IJavaProject) JavaCore.create(project);
        } catch (CoreException e)
        {
            log(e);
        }
        return null;
    }

    /**
     * @param file
     * @return
     */
    public TapestryProject getTapestryProjectFor(IStorage storage)
    {
        if (storage == null)
            return null;
        IProject project = getProjectFor(storage);
        if (project == null)
        {
            return null;
        }

        try
        {
            if (project.hasNature(TapestryCore.NATURE_ID))
                return (TapestryProject) project.getNature(TapestryCore.NATURE_ID);
        } catch (CoreException e)
        {
            log(e);
        }
        return null;
    }

}
