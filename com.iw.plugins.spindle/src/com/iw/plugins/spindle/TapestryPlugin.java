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
import org.eclipse.jdt.internal.core.JavaModelManager;
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

import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryModelManager;
import com.iw.plugins.spindle.spec.TapestryPluginFactory;
import com.iw.plugins.spindle.util.TapestryLookup;
import com.iw.plugins.spindle.util.Utils;
import net.sf.tapestry.parse.SpecificationParser;

/**
 * This is the top-level class of the Tapestry plugin tool.
 *
 * @see AbstractUIPlugin for additional information on UI plugins
 */
public class TapestryPlugin extends AbstractUIPlugin {
  // Default instance of the receiver

  private static TapestryPlugin inst;
  private static SpecificationParser parser;
  private static TapestryModelManager modelManager = null;
  private static Map builtInAliasLookup;

  public static TapestryApplicationModel selectedApplication = null;

  static public final String ID_PLUGIN = "com.iw.plugins.spindle";

  static public String appEditorId = "com.iw.plugins.spindle.editors.app_editor";

  static public String compEditorId = "com.iw.plugins.spindle.editors.jwc_editor";

  /**
   * Creates the Tapestry plugin and caches its default instance
   *
   * @param descriptor  the plugin descriptor which the receiver is made from
   */
  public TapestryPlugin(IPluginDescriptor descriptor) {
    super(descriptor);
    if (inst == null)
      inst = this;
  }

  static {
    parser = new SpecificationParser();
    parser.setFactory(new TapestryPluginFactory());
    builtInAliasLookup = new HashMap();
    builtInAliasLookup.put("Insert", "/com/primix/tapestry/components/Insert.jwc");
    builtInAliasLookup.put("Action", "/com/primix/tapestry/link/Action.jwc");
    builtInAliasLookup.put("Checkbox", "/com/primix/tapestry/form/Checkbox.jwc");
    builtInAliasLookup.put("InsertWrapped", "/com/primix/tapestry/components/InsertWrapped.jwc");
    builtInAliasLookup.put("Conditional", "/com/primix/tapestry/components/Conditional.jwc");
    builtInAliasLookup.put("Foreach", "/com/primix/tapestry/components/Foreach.jwc");
    builtInAliasLookup.put("ExceptionDisplay", "/com/primix/tapestry/html/ExceptionDisplay.jwc");
    builtInAliasLookup.put("Delegator", "/com/primix/tapestry/components/Delegator.jwc");
    builtInAliasLookup.put("Form", "/com/primix/tapestry/form/Form.jwc");
    builtInAliasLookup.put("TextField", "/com/primix/tapestry/form/TextField.jwc");
    builtInAliasLookup.put("Text", "/com/primix/tapestry/form/Text.jwc");
    builtInAliasLookup.put("Select", "/com/primix/tapestry/form/Select.jwc");
    builtInAliasLookup.put("Option", "/com/primix/tapestry/form/Option.jwc");
    builtInAliasLookup.put("Image", "/com/primix/tapestry/html/Image.jwc");
    builtInAliasLookup.put("Any", "/com/primix/tapestry/components/Any.jwc");
    builtInAliasLookup.put("RadioGroup", "/com/primix/tapestry/form/RadioGroup.jwc");
    builtInAliasLookup.put("Radio", "/com/primix/tapestry/form/Radio.jwc");
    builtInAliasLookup.put("Rollover", "/com/primix/tapestry/html/Rollover.jwc");
    builtInAliasLookup.put("Body", "/com/primix/tapestry/html/Body.jwc");
    builtInAliasLookup.put("Direct", "/com/primix/tapestry/link/Direct.jwc");
    builtInAliasLookup.put("Page", "/com/primix/tapestry/link/Page.jwc");
    builtInAliasLookup.put("Service", "/com/primix/tapestry/link/Service.jwc");
    builtInAliasLookup.put("ImageSubmit", "/com/primix/tapestry/form/ImageSubmit.jwc");
    builtInAliasLookup.put("PropertySelection", "/com/primix/tapestry/form/PropertySelection.jwc");
    builtInAliasLookup.put("Submit", "/com/primix/tapestry/form/Submit.jwc");
    builtInAliasLookup.put("Hidden", "/com/primix/tapestry/form/Hidden.jwc");
    builtInAliasLookup.put("ShowInspector", "/com/primix/tapestry/inspector/ShowInspector.jwc");
    builtInAliasLookup.put("Shell", "/com/primix/tapestry/html/Shell.jwc");
    builtInAliasLookup.put("InsertText", "/com/primix/tapestry/html/InsertText.jwc");
    builtInAliasLookup.put("ValidatingTextField", "/com/primix/tapestry/valid/ValidatingTextField.jwc");
    builtInAliasLookup.put("DateField", "/com/primix/tapestry/valid/DateField.jwc");
    builtInAliasLookup.put("IntegerField", "/com/primix/tapestry/valid/IntegerField.jwc");
    builtInAliasLookup.put("FieldLabel", "/com/primix/tapestry/valid/FieldLabel.jwc");
    builtInAliasLookup.put("Script", "/com/primix/tapestry/html/Script.jwc");
    builtInAliasLookup.put("Block", "/com/primix/tapestry/components/Block.jwc");
    builtInAliasLookup.put("InsertBlock", "/com/primix/tapestry/components/InsertBlock.jwc");
    builtInAliasLookup.put("NumericField", "/com/primix/tapestry/valid/NumericField.jwc");
    builtInAliasLookup.put("ListEdit", "/com/primix/tapestry/form/ListEdit.jwc");
    builtInAliasLookup.put("StaleLink", "/com/primix/tapestry/pages/StaleLink.jwc");
    builtInAliasLookup.put("StaleSession", "/com/primix/tapestry/pages/StaleSession.jwc");
    builtInAliasLookup.put("Exception", "/com/primix/tapestry/pages/Exception.jwc");
    builtInAliasLookup.put("Inspector", "/com/primix/tapestry/inspector/Inspector.jwc");
  }

  /**
   * Gets the plugin singleton.
   *
   * @return the default TapestryPlugin instance
   */
  static public TapestryPlugin getDefault() {
    return inst;
  }
  
  /** expects a valid IProject in order to find the IJavaProject to search and either an alias name
   * or a path to a tapestry file like:
   * <b>
   * /a/b/c/d/test.jwc
   * /bob.application
   */
  public IStorage[] resolveTapestryComponent(IStorage storage, String name) {
    if (storage == null || name == null) {
      return null;
    }
    String useName;
    if (name.endsWith(".jwc")) {
      useName = name;
    } else {
      useName = (String) builtInAliasLookup.get(name);
    }
    if (useName == null) {
      return new IStorage[0];
    }

    IJavaProject jproject = getJavaProjectFor(storage);
    try {
      TapestryLookup lookup = new TapestryLookup();
      lookup.configure(jproject);
      return lookup.findComponent(useName);
    } catch (JavaModelException jmex) {
      jmex.printStackTrace();
      return new IStorage[0];
    }
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

  public static TapestryModelManager getTapestryModelManager() {
    if (modelManager == null) {
      modelManager = new TapestryModelManager();
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

  protected void searchForTapestryElementsIn(IContainer container, List collect) throws CoreException {
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
  protected void initializeDefaultPreferences(IPreferenceStore store) { /*
     
     // These settings will show up when Preference dialog
     // opens up for the first time.
     store.setDefault(ITapPluginConstants.PRE_CHECK1, true);
     store.setDefault(ITapPluginConstants.PRE_CHECK2, true);
     store.setDefault(ITapPluginConstants.PRE_CHECK3, false);
     store.setDefault(ITapPluginConstants.PRE_RADIO_CHOICE, 2);
     store.setDefault(ITapPluginConstants.PRE_TEXT, MessageUtil.getString("Default_text"));
     */

  }

  static public void openTapestryEditor(BaseTapestryModel model) {
    String editorId = null;
    if (model instanceof TapestryApplicationModel) {
      editorId = TapestryPlugin.appEditorId;
    } else if (model instanceof TapestryComponentModel) {
      editorId = TapestryPlugin.compEditorId;
    } else {
      return;
    }
    IStorage storage = model.getUnderlyingStorage();
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
