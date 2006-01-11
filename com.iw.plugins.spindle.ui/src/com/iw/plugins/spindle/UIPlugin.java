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
package com.iw.plugins.spindle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;

import net.sf.solareclipse.xml.ui.XMLPlugin;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.SharedTextColors;
import com.iw.plugins.spindle.editors.documentsAndModels.IXMLModelProvider;
import com.iw.plugins.spindle.editors.documentsAndModels.SpecDocumentSetupParticipant;
import com.iw.plugins.spindle.editors.documentsAndModels.SpindleFileDocumentProvider;
import com.iw.plugins.spindle.editors.documentsAndModels.SpindleStorageDocumentProvider;
import com.iw.plugins.spindle.editors.documentsAndModels.TemplateDocumentSetupParticipant;
import com.iw.plugins.spindle.editors.template.TemplateTextTools;
import com.iw.plugins.spindle.ui.util.PreferenceStoreWrapper;
import com.iw.plugins.spindle.ui.util.Revealer;

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

  static
  {
    DEFAULT_COMPONENT_SPEC = new PluginComponentSpecification();
    DEFAULT_COMPONENT_SPEC.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
    DEFAULT_COMPONENT_SPEC.setPageSpecification(false);
    DEFAULT_COMPONENT_SPEC.setComponentClassName(TapestryCore
        .getString("TapestryComponentSpec.defaultSpec"));
    DEFAULT_COMPONENT_SPEC.setDescription(UIPlugin
        .getString("auto-create-spec-description"));

    DEFAULT_PAGE_SPEC = new PluginComponentSpecification();
    DEFAULT_PAGE_SPEC.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
    DEFAULT_PAGE_SPEC.setPageSpecification(true);
    DEFAULT_PAGE_SPEC.setComponentClassName(TapestryCore
        .getString("TapestryPageSpec.defaultSpec"));
    DEFAULT_PAGE_SPEC.setDescription(UIPlugin.getString("auto-create-spec-description"));

    DEFAULT_APPLICATION_SPEC = new PluginApplicationSpecification();
    DEFAULT_APPLICATION_SPEC.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
    DEFAULT_APPLICATION_SPEC.setEngineClassName(TapestryCore
        .getString("TapestryEngine.defaultEngine"));
    DEFAULT_APPLICATION_SPEC.setDescription(UIPlugin
        .getString("auto-create-spec-description"));

    DEFAULT_LIBRARY_SPEC = new PluginApplicationSpecification();
    DEFAULT_LIBRARY_SPEC.setPublicId(XMLUtil.getPublicId(XMLUtil.DTD_3_0));
    DEFAULT_LIBRARY_SPEC.setDescription(UIPlugin
        .getString("auto-create-spec-description"));

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
    return getString(key, new Object[]{arg});
  }
  public static String getString(String key, Object arg1, Object arg2)
  {
    return getString(key, new Object[]{arg1, arg2});
  }
  public static String getString(String key, Object arg1, Object arg2, Object arg3)
  {
    return getString(key, new Object[]{arg1, arg2, arg3});
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
    Status status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg + "\n", null);
    log.log(status);
  }

  static public void warn(Throwable e)
  {
    StringBuffer buffer = new StringBuffer("Warning:");
    buffer.append(e.getClass().getName());
    buffer.append('\n');
    buffer.append(e.getStackTrace()[0].toString());
    log(buffer.toString());
  }

  static public void warn(String message)
  {
    log("Warning:" + message);
  }

  static public void log(Throwable ex)
  {
    ILog log = getDefault().getLog();
    StringWriter stringWriter = new StringWriter();
    ex.printStackTrace(new PrintWriter(stringWriter));
    String msg = stringWriter.getBuffer().toString();

    Status status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, null);
    log.log(status);
  }

  public static IResource getActiveEditorFileInput()
  {
    IWorkbenchPage page = getDefault().getActivePage();

    if (page != null)
    {
      IEditorPart part = page.getActiveEditor();
      if (part != null)
      {
        IEditorInput editorInput = part.getEditorInput();
        if (editorInput != null)
          return (IResource) editorInput.getAdapter(IResource.class);
      }
    }
    return null;
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

  static public IEditorPart openTapestryEditor(IStorage storage)
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
          return IDE.openEditor(UIPlugin.getDefault().getActivePage(), ((IFile) storage));
        } else
        {
          IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
          IEditorDescriptor desc = editorReg.getDefaultEditor(storage.getName());
          editorId = desc.getId();
        }
      }

      IEditorInput input = null;

      if (storage instanceof JarEntryFile)
      {
        input = new BinaryEditorInput(storage);
      } else
      {
        input = new FileEditorInput((IFile) storage);
      }

      return IDE.openEditor(UIPlugin.getDefault().getActivePage(), input, editorId);

    } catch (PartInitException piex)
    {
      UIPlugin.log(piex);
    }
    return null;
  }
  
  static class BinaryEditorInput extends JarEntryEditorInput {
    
    public BinaryEditorInput(IStorage jarEntryFile)
    {
        super(jarEntryFile);       
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof JarEntryEditorInput))
            return false;
        JarEntryEditorInput other= (JarEntryEditorInput) obj;
        return getStorage().toString().equals(other.getStorage().toString());
    }

   
      
  }

  //The shared instance.
  private static UIPlugin plugin;

  /** shared document provider for Templates that come from files * */
  private SpindleFileDocumentProvider fTemplateFileDocumentProvider;

  /** shared document provider for Template that don't come from files (ie jars) * */
  private SpindleStorageDocumentProvider fTemplateStorageDocumentProvider;

  /** shared document provider for Specifications that come from files * */
  private SpindleFileDocumentProvider fSpecFileDocumentProvider;

  /** shared document provider for Specifications that come jars */
  private SpindleStorageDocumentProvider fSpecStorageDocumentProvider;

  private TemplateTextTools fTemplatelTextTools;

  private ILabelProvider fStorageLableProvider;

  private IXMLModelProvider fModelProvider = new SpecDocumentSetupParticipant();

  /**
   * these are shared colors not specific to any one editor used mostly for the
   * Annotations.
   */
  private ISharedTextColors fSharedTextColors;

  public UIPlugin()
  {
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
    {
    }

    public void windowClosed(IWorkbenchWindow window)
    {
    }

    public void windowOpened(IWorkbenchWindow window)
    {
    }
  };

  public static final String SPINDLEUI_PREFS_FILE = ".spindleUI.prefs";

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

  public synchronized FileDocumentProvider getTemplateFileDocumentProvider()
  {
    if (fTemplateFileDocumentProvider == null)
    {
      fTemplateFileDocumentProvider = new SpindleFileDocumentProvider(
          new TemplateDocumentSetupParticipant());
    }

    return fTemplateFileDocumentProvider;
  }

  public synchronized StorageDocumentProvider getTemplateStorageDocumentProvider()
  {
    if (fTemplateStorageDocumentProvider == null)
      fTemplateStorageDocumentProvider = new SpindleStorageDocumentProvider(
          new TemplateDocumentSetupParticipant());

    return fTemplateStorageDocumentProvider;
  }

  public synchronized FileDocumentProvider getSpecFileDocumentProvider()
  {
    if (fSpecFileDocumentProvider == null)
      fSpecFileDocumentProvider = new SpindleFileDocumentProvider(
          new SpecDocumentSetupParticipant());

    return fSpecFileDocumentProvider;
  }

  public synchronized StorageDocumentProvider getSpecStorageDocumentProvider()
  {
    if (fSpecStorageDocumentProvider == null)
      fSpecStorageDocumentProvider = new SpindleStorageDocumentProvider(
          new SpecDocumentSetupParticipant());

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
    return PLUGIN_ID;
  }

//  public IProject getProjectFor(IEditorInput input)
//  {
//      
//    if (input instanceof IFileEditorInput)
//      return ((IFileEditorInput) input).getFile().getProject();
//    if (input instanceof IStorageEditorInput)
//    {
//      try
//      {
//        return TapestryCore.getDefault().getProjectFor(
//            ((IStorageEditorInput) input).getStorage());
//      } catch (CoreException e)
//      {
//        log(e);
//      }
//    }
//    return null;
//  }

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
   * Returns instance of text tools for Templates.
   */
  public TemplateTextTools getTemplateTextTools()
  {
    if (fTemplatelTextTools == null)
    {
      IPreferenceStore wrapped = new PreferenceStoreWrapper(
          getPreferenceStore(),
          XMLPlugin.getDefault().getPreferenceStore());
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

  public IXMLModelProvider getXMLModelProvider()
  {
    return fModelProvider;
  }

}