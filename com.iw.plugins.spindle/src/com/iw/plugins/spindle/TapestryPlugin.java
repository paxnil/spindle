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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.tapestry.parse.SpecificationParser;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.manager.TapestryModelManager;
import com.iw.plugins.spindle.spec.TapestryPluginSpecFactory;
import com.iw.plugins.spindle.ui.text.ColorManager;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;
import com.iw.plugins.spindle.wizards.NewTapComponentWizardPage;

/**
 * This is the top-level class of the Tapestry plugin tool.
 *
 * @see AbstractUIPlugin for additional information on UI plugins
 */
public class TapestryPlugin extends AbstractUIPlugin {
  // Default instance of the receiver

  private static TapestryPlugin instance;
  private static SpecificationParser parser;
  private static TapestryModelManager modelManager = null;
  
  static public final String ID_PLUGIN = "com.iw.plugins.spindle";

  public static TapestryApplicationModel selectedApplication = null;
  
  static private Map editorIdLookup;

  /**
   * Creates the Tapestry plugin and caches its default instance
   *
   * @param descriptor  the plugin descriptor which the receiver is made from
   */
  public TapestryPlugin(IPluginDescriptor descriptor) {
    super(descriptor);
    if (instance == null)
      instance = this;
  }

  static {
    parser = new SpecificationParser();
    parser.setFactory(new TapestryPluginSpecFactory());
    
    String ID_PLUGIN = "com.iw.plugins.spindle";
    editorIdLookup = new HashMap();
    editorIdLookup.put("application", ID_PLUGIN+".editors.app_editor");
    editorIdLookup.put("library", ID_PLUGIN+".editors.library_editor");
    editorIdLookup.put("jwc", ID_PLUGIN+".editors.jwc_editor");
    editorIdLookup.put("page", ID_PLUGIN+".editors.page_editor");
        
  }

  /**
   * Gets the plugin singleton.
   *
   * @return the default TapestryPlugin instance
   */
  static public TapestryPlugin getDefault() {
    return instance;
  }

  

  public IResource getParentResourceFor(IStorage storage) {
    if (storage instanceof IResource) {
      return (IResource) ((IResource) storage).getParent();
    }
    if (storage instanceof JarEntryFile) {
      try {
        TapestryLookup lookup = new TapestryLookup();
        lookup.configure(getJavaProjectFor(storage));
        return lookup.findParentResource(storage);
      } catch (JavaModelException jmex) {
        jmex.printStackTrace();
      }
    }
    return null;
  }

  public IJavaProject getJavaProjectFor(Object obj) {
    IProject project = null;
    if (obj instanceof IProject) {
      project = (IProject) obj;
    } else if (obj instanceof IResource) {
      project = ((IResource) obj).getProject();
    } else if (obj instanceof IStorage) {
      project = getProjectFor((IStorage) obj);
    }
    if (project == null) {
      return null;
    }
    return JavaCore.create(project);
    //    return JavaModelManager.getJavaModel(project.getWorkspace()).getJavaProject(project);
  }

  public IProject getProjectFor(IStorage storage) {
    TapestryLookup lookup = null;
    if (storage instanceof JarEntryFile) {
      try {
        IWorkspace workspace = getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        for (int i = 0; i < projects.length; i++) {
          if (lookup == null) {
            lookup = new TapestryLookup();
          }
          lookup.configure(getJavaProjectFor(projects[i]));
          if (lookup.projectContainsJarEntry((JarEntryFile) storage)) {
            return projects[i];
          }
        }
      } catch (JavaModelException jmex) {
        jmex.printStackTrace();
      }
      return null;
    } else if (storage instanceof IResource) {
      IResource resource = (IResource) storage;
      if (resource.getType() == IResource.PROJECT) {
        return (IProject) resource;
      } else {
        return ((IResource) storage).getProject();
      }
    }
    return null;
  }

  public IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }

  public static synchronized TapestryModelManager getTapestryModelManager() {
    if (modelManager == null) {
      modelManager = new TapestryModelManager();
      modelManager.buildModelDelegates();
    }
    return modelManager;
  }

  public Object[] getAllTapestryElementsFromWorkspace() throws CoreException {
    ArrayList result = new ArrayList();
    IWorkspace workspace = getWorkspace();
    IProject[] projects = workspace.getRoot().getProjects();
    for (int i = 0; i < projects.length; i++) {
      IProject project = projects[i];
      if (project instanceof IJavaElement) {
        searchForTapestryElementsIn(project, result);
      }
    }
    return result.toArray();
  }

  protected void searchForTapestryElementsIn(IContainer container, List collect)
    throws CoreException {
    IResource[] members = container.members(false);
    for (int i = 0; i < members.length; i++) {
      if (members[i] instanceof IFile) {
        if (members[i].getName().endsWith(".application")) {
          collect.add(members[i]);
        } else if (members[i].getName().endsWith(".jwc")) {
          collect.add(members[i]);
        }
      } else if (members[i] instanceof IContainer) {
        searchForTapestryElementsIn((IContainer) members[i], collect);
      }
    }
  }

  public IWorkbenchPage getActivePage() {
    return getActiveWorkbenchWindow().getActivePage();
  }

  public String getPluginId() {
    return getDescriptor().getUniqueIdentifier();
  }

  public Shell getActiveWorkbenchShell() {
    return getActiveWorkbenchWindow().getShell();
  }

  public IWorkbenchWindow getActiveWorkbenchWindow() {
    return getWorkbench().getActiveWorkbenchWindow();
  }

  static public SpecificationParser getParser() {
    return parser;
  }

  public void logException(Throwable e) {
    if (e instanceof InvocationTargetException) {
      e = ((InvocationTargetException) e).getTargetException();
    }
    String message = e.getMessage();
    if (message == null)
      message = e.toString();
    Status status = new Status(IStatus.ERROR, getPluginId(), IStatus.OK, message, e);
    ErrorDialog.openError(getActiveWorkbenchShell(), null, null, status);
    ResourcesPlugin.getPlugin().getLog().log(status);
  } /** 
      * Sets default preference values. These values will be used
      * until some preferences are actually set using Preference dialog.
      */
  protected void initializeDefaultPreferences(IPreferenceStore store) {
    ColorManager.initializeDefaults(store);
    NewTapComponentWizardPage.initializeDefaults(store);
  }

  static public void openTapestryEditor(IStorage storage) {
    String editorId = null;
    
    
    String extension = storage.getFullPath().getFileExtension();
    
    editorId = (String)editorIdLookup.get(extension);
    
    if (editorId == null) {
    	
    	return;
    }
    
    IEditorInput input = null;
    
    if (storage instanceof JarEntryFile) {
    	
      input = new JarEntryEditorInput(storage);
      
    } else {
    	
      input = new FileEditorInput((IFile) storage);
      
    }
    try {
    	
      TapestryPlugin.getDefault().getActivePage().openEditor(input, editorId);
      
    } catch (PartInitException piex) {
    	
      TapestryPlugin.getDefault().logException(piex);
      
    }
  }

  static public void openNonTapistryEditor(IStorage storage) {
    IWorkbenchPage page = TapestryPlugin.getDefault().getActivePage();
    try {
      if (storage instanceof IFile) {
        EditorUtility.openInEditor((IFile) storage);
      } else if (storage instanceof JarEntryFile) {
        EditorUtility.openInEditor(storage);
      }
    } catch (JavaModelException jmex) {
      jmex.printStackTrace();
    } catch (PartInitException piex) {
      TapestryPlugin plugin = TapestryPlugin.getDefault();
      Status status =
        new Status(
          IStatus.ERROR,
          plugin.getPluginId(),
          IStatus.OK,
          "Could not find or open default editor for \""
            + storage.getFullPath().getFileExtension()
            + "\" files. "
            + "You may need to associate the file type with a default editor.",
          piex);
      ErrorDialog.openError(plugin.getActiveWorkbenchShell(), null, null, status);
      TapestryPlugin.getDefault().logException(piex);
    }
  }

  public static IJavaElement getActiveEditorJavaInput() {

    IWorkbenchPage page = getDefault().getActivePage();

    if (page != null) {
      IEditorPart part = page.getActiveEditor();
      if (part != null) {
        IEditorInput editorInput = part.getEditorInput();
        if (editorInput != null) {
          IJavaElement result = (IJavaElement) editorInput.getAdapter(IJavaElement.class);
          if (result == null) {
            IResource nonjava = (IResource) editorInput.getAdapter(IResource.class);
            if (nonjava != null) {
              IContainer parent = nonjava.getParent();
              while (parent != null) {
                result = (IJavaElement) parent.getAdapter(IJavaElement.class);
                if (result != null) {
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

}