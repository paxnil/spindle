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
package com.iw.plugins.spindle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.solareclipse.xml.internal.ui.preferences.XMLSyntaxPreferencePage;
import net.sf.solareclipse.xml.ui.XMLPlugin;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.SharedTextColors;
import com.iw.plugins.spindle.editors.spec.SpecFileDocumentProvider;
import com.iw.plugins.spindle.editors.spec.SpecStorageDocumentProvider;
import com.iw.plugins.spindle.editors.template.TemplateFileDocumentProvider;
import com.iw.plugins.spindle.editors.template.TemplateStorageDocumentProvider;
import com.iw.plugins.spindle.editors.template.TemplateTextTools;
import com.iw.plugins.spindle.ui.util.PreferenceStoreWrapper;
import com.iw.plugins.spindle.ui.util.Revealer;
import com.iw.plugins.spindle.ui.wizards.NewTapComponentWizardPage;

/**
 * The main plugin class to be used in the desktop.
 */
public class UIPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "com.iw.plugins.spindle.ui";

    public static PluginComponentSpecification DEFAULT_COMPONENT_SPEC;
    public static PluginComponentSpecification DEFAULT_PAGE_SPEC;
    public static PluginApplicationSpecification DEFAULT_APPLICATION_SPEC;
    public static PluginLibrarySpecification DEFAULT_LIBRARY_SPEC;

    static private Map EDITOR_ID_LOOKUP;

    static {
        DEFAULT_COMPONENT_SPEC = new PluginComponentSpecification();
        DEFAULT_COMPONENT_SPEC.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
        DEFAULT_COMPONENT_SPEC.setPageSpecification(false);
        DEFAULT_COMPONENT_SPEC.setComponentClassName(TapestryCore.getString("TapestryComponentSpec.defaultSpec"));
        DEFAULT_COMPONENT_SPEC.setDescription(UIPlugin.getString("auto-create-spec-description"));

        DEFAULT_PAGE_SPEC = new PluginComponentSpecification();
        DEFAULT_PAGE_SPEC.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
        DEFAULT_PAGE_SPEC.setPageSpecification(true);
        DEFAULT_PAGE_SPEC.setComponentClassName(TapestryCore.getString("TapestryPageSpec.defaultSpec"));
        DEFAULT_PAGE_SPEC.setDescription(UIPlugin.getString("auto-create-spec-description"));

        DEFAULT_APPLICATION_SPEC = new PluginApplicationSpecification();
        DEFAULT_APPLICATION_SPEC.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
        DEFAULT_APPLICATION_SPEC.setEngineClassName(TapestryCore.getString("TapestryEngine.defaultEngine"));
        DEFAULT_APPLICATION_SPEC.setDescription(UIPlugin.getString("auto-create-spec-description"));

        DEFAULT_LIBRARY_SPEC = new PluginApplicationSpecification();
        DEFAULT_LIBRARY_SPEC.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
        DEFAULT_LIBRARY_SPEC.setDescription(UIPlugin.getString("auto-create-spec-description"));

        String ID_PLUGIN = "com.iw.plugins.spindle.ui";

        EDITOR_ID_LOOKUP = new HashMap();
        EDITOR_ID_LOOKUP.put("application", ID_PLUGIN + ".editors.spec");
        EDITOR_ID_LOOKUP.put("library", ID_PLUGIN + ".editors.spec");
        EDITOR_ID_LOOKUP.put("jwc", ID_PLUGIN + ".editors.spec");
        EDITOR_ID_LOOKUP.put("page", ID_PLUGIN + ".editors.spec");
        EDITOR_ID_LOOKUP.put("html", ID_PLUGIN + ".editors.template");
    }

    private static ResourceBundle UIStrings;

    public static ResourceBundle getResourceBundle()
    {
        if (UIStrings == null)
            UIStrings = ResourceBundle.getBundle("com.iw.plugins.spindle.resources");
        return UIStrings;
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
    public static String getString(String key, Object[] args)
    {
        getResourceBundle();

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

    static public void log(Throwable ex)
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

    static public void openTapestryEditor(IStorage storage)
    {
        String editorId = null;

        String extension = storage.getFullPath().getFileExtension();

        editorId = (String) EDITOR_ID_LOOKUP.get(extension);
        try
        {

            if (editorId == null)
            {
                if (storage instanceof IFile)
                {
                    UIPlugin.getDefault().getActivePage().openEditor((IFile) storage);
                } else
                {
                    editorId = (String) EDITOR_ID_LOOKUP.get("html");
                }
            }

            IEditorInput input = null;

            if (storage instanceof JarEntryFile)
            {
                input = new JarEntryEditorInput(storage);
            } else
            {
                input = new FileEditorInput((IFile) storage);
            }
            UIPlugin.getDefault().getActivePage().openEditor(input, editorId);
        } catch (PartInitException piex)
        {
            UIPlugin.log(piex);
        }
    }

    //The shared instance.
    private static UIPlugin plugin;

    /** shared document provider for Templates that come from files **/
    private TemplateFileDocumentProvider fTemplateFileDocumentProvider;

    /** shared document provider for Template that don't come from files (ie jars) **/
    private TemplateStorageDocumentProvider fTemplateStorageDocumentProvider;

    /** shared document provider for Specifications that come from files **/
    private SpecFileDocumentProvider fSpecFileDocumentProvider;

    /** shared document provider for Specifications that don't come from files (ie jars) **/
    private SpecStorageDocumentProvider fSpecStorageDocumentProvider;

    private TemplateTextTools fTemplatelTextTools;

    private ILabelProvider fStorageLableProvider;

    /** 
     * these are shared colors not specific to any one editor
     * used mostly for the Annotations. 
     */
    private ISharedTextColors fSharedTextColors;

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

    public synchronized TemplateFileDocumentProvider getTemplateFileDocumentProvider()
    {
        if (fTemplateFileDocumentProvider == null)
            fTemplateFileDocumentProvider = new TemplateFileDocumentProvider();

        return fTemplateFileDocumentProvider;
    }

    public synchronized TemplateStorageDocumentProvider getTemplateStorageDocumentProvider()
    {
        if (fTemplateStorageDocumentProvider == null)
            fTemplateStorageDocumentProvider = new TemplateStorageDocumentProvider();

        return fTemplateStorageDocumentProvider;
    }

    public synchronized SpecFileDocumentProvider getSpecFileDocumentProvider()
    {
        if (fSpecFileDocumentProvider == null)
            fSpecFileDocumentProvider = new SpecFileDocumentProvider();

        return fSpecFileDocumentProvider;
    }

    public synchronized SpecStorageDocumentProvider getSpecStorageDocumentProvider()
    {
        if (fSpecStorageDocumentProvider == null)
            fSpecStorageDocumentProvider = new SpecStorageDocumentProvider();

        return fSpecStorageDocumentProvider;
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

    public IProject getProjectFor(IEditorInput input)
    {
        if (input instanceof IFileEditorInput)
            return ((IFileEditorInput) input).getFile().getProject();
        if (input instanceof IStorageEditorInput)
        {
            try
            {
                return TapestryCore.getDefault().getProjectFor(((IStorageEditorInput) input).getStorage());
            } catch (CoreException e)
            {
                log(e);
            }
        }
        return null;
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
    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
     */
    protected void initializeDefaultPreferences(IPreferenceStore store)
    {
        super.initializeDefaultPreferences(store);
        PreferenceConstants.initializeDefaultValues(store);
        XMLSyntaxPreferencePage.initDefaults(store);
        NewTapComponentWizardPage.initializeDefaults(store);
    }

    /**
     * Returns instance of text tools for Templates.
     */
    public TemplateTextTools getTemplateTextTools()
    {
        if (fTemplatelTextTools == null)
        {
            IPreferenceStore wrapped =
                new PreferenceStoreWrapper(getPreferenceStore(), XMLPlugin.getDefault().getPreferenceStore());
            fTemplatelTextTools = new TemplateTextTools(wrapped);
        }

        return fTemplatelTextTools;
    }

    /**
     * Returns instance of text tools for Tapestry spec files.
     */
    public XMLTextTools getXMLTextTools()
    {
        return XMLPlugin.getDefault().getXMLTextTools();
    }

    public ISharedTextColors getSharedTextColors()
    {
        if (fSharedTextColors == null)
            fSharedTextColors = new SharedTextColors();
        return fSharedTextColors;
    }

    public ILabelProvider getStorageLabelProvider()
    {
        if (fStorageLableProvider == null)
            fStorageLableProvider = new StorageLabelProvider();

        return fStorageLableProvider;
    }

}
