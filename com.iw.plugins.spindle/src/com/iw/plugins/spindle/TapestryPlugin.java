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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.tapestry.Tapestry;
import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.util.xml.AbstractDocumentParser;

import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.iw.plugins.spindle.editorjwc.components.ChooseBindingTypeDialog;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.project.TapestryProject;
import com.iw.plugins.spindle.refactor.RenamedComponentOrPageRefactor;
import com.iw.plugins.spindle.spec.TapestryPluginSpecFactory;
import com.iw.plugins.spindle.ui.text.ColorManager;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;
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
  private static TapestryProjectModelManager modelManager = null;

  protected PatternCompiler _patternCompiler;
  protected PatternMatcher _matcher;
  protected Map _compiledPatterns;

  static public final String ID_PLUGIN = "com.iw.plugins.spindle";
  static public final String NATURE_ID = ID_PLUGIN + ".project.TapestryProject";

  public static TapestryApplicationModel selectedApplication = null;

  static private Map editorIdLookup;

  static private HashMap parsers = new HashMap();

  private static List managedExtensions = new ArrayList();

  /**
   * Creates the Tapestry plugin and caches its default instance
   *
   * @param descriptor  the plugin descriptor which the receiver is made from
   */
  public TapestryPlugin(IPluginDescriptor descriptor) {
    super(descriptor);
    if (instance == null)
      instance = this;

    Category CAT = Category.getInstance(AbstractDocumentParser.class);
    CAT.setPriority(Priority.DEBUG);
    CAT.addAppender(new ConsoleAppender(new PatternLayout("%c{1} [%p] %m%n"), "System.out"));

  }

  static {
    parser = new SpecificationParser();
    parser.setFactory(new TapestryPluginSpecFactory());

    String ID_PLUGIN = "com.iw.plugins.spindle";
    editorIdLookup = new HashMap();
    editorIdLookup.put("application", ID_PLUGIN + ".editors.app_editor");
    editorIdLookup.put("library", ID_PLUGIN + ".editors.library_editor");
    editorIdLookup.put("jwc", ID_PLUGIN + ".editors.jwc_editor");
    editorIdLookup.put("page", ID_PLUGIN + ".editors.page_editor");
    editorIdLookup.put("html", ID_PLUGIN + ".editors.html");
    editorIdLookup.put("htm", ID_PLUGIN + ".editors.html");

  }

  static public void registerParser(String extension, AbstractDocumentParser parser) {
    parsers.put(extension, parser);
  }

  public static List getManagedExtensions() {

    return Collections.unmodifiableList(managedExtensions);

  }

  public static void registerManagedExtension(String extension) {

    if (!managedExtensions.contains(extension)) {

      managedExtensions.add(extension);

    }

  }

  static public AbstractDocumentParser getParserFor(String extension) {
    return (AbstractDocumentParser) parsers.get(extension);
  }

  /**
   * Gets the plugin singleton.
   *
   * @return the default TapestryPlugin instance
   */
  static public TapestryPlugin getDefault() {

    return instance;

  }

  public ITapestryProject getTapestryProjectFor(Object element) throws CoreException {

    ITapestryProject result = null;

    IProject project = null;

    if (element instanceof IResource) {

      project = ((IResource) element).getProject();

    } else if (element instanceof IStorage) {

      project = getProjectFor((IStorage) element);

    } else if (element instanceof IJavaProject) {

      project = ((IJavaProject) element).getProject();

    } else if (element instanceof ITapestryModel) {

      project = getProjectFor(((ITapestryModel) element).getUnderlyingStorage());

    }

    if (project != null && project.isOpen() && project.hasNature(NATURE_ID)) {

      result = (ITapestryProject) project.getNature(NATURE_ID);
    }

    if (result == null) {

      SpindleStatus status = new SpindleStatus();
      status.setError(project.getFullPath().toString() + " is not open or is not a Tapestry project");

      throw new CoreException(status);

    }

    return result;
  }

  public ITapestryProject addTapestryProjectNatureTo(IJavaProject jproject, IProgressMonitor monitor) throws CoreException {

    IProject project = jproject.getProject();

    if (project.hasNature(NATURE_ID)) {

      return null;

    }

    IProjectDescription description = project.getDescription();

    String[] natures = description.getNatureIds();

    String[] newNatures = new String[natures.length + 1];

    System.arraycopy(natures, 0, newNatures, 0, natures.length);

    newNatures[natures.length] = NATURE_ID;

    description.setNatureIds(newNatures);

    project.setDescription(description, monitor);

    return getTapestryProjectFor(jproject);

  }

  public void removeTapestryProjectNature(TapestryProject tproject, IProgressMonitor monitor) {

    IProject project = tproject.getProject();

    try {
      if (project.exists() && project.isOpen() && project.hasNature(NATURE_ID)) {

        IProjectDescription description = project.getDescription();

        ArrayList natures = new ArrayList(Arrays.asList(description.getNatureIds()));

        natures.remove(NATURE_ID);

        description.setNatureIds((String[]) natures.toArray(new String[natures.size()]));

        project.setDescription(description, monitor);

      }
    } catch (CoreException e) {
    }

  }

  //  public IResource getParentResourceFor(IStorage storage) {
  //    if (storage instanceof IResource) {
  //      return (IResource) ((IResource) storage).getParent();
  //    }
  //    if (storage instanceof JarEntryFile) {
  //
  //      try {
  //
  //        TapestryLookup lookup = new TapestryLookup();
  //        lookup.configure(getJavaProjectFor(storage));
  //        return lookup.findParentResource(storage);
  //
  //      } catch (CoreException jmex) {
  //
  //        jmex.printStackTrace();
  //      }
  //    }
  //    return null;
  //  }

  public IJavaProject getJavaProjectFor(Object obj) throws CoreException {
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

    if (!project.hasNature(JavaCore.NATURE_ID)) {
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
          if (!projects[i].isOpen()) {
            continue;
          }
          if (lookup == null) {
            lookup = new TapestryLookup();
          }
          IJavaProject jproject = getJavaProjectFor(projects[i]);
          lookup.configure(jproject);
          if (lookup.projectContainsJarEntry((JarEntryFile) storage)) {
            return projects[i];
          }
        }
      } catch (CoreException jmex) {
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

  public static synchronized TapestryProjectModelManager getTapestryModelManager(IStorage storage) throws CoreException {
    ITapestryProject tproject = getDefault().getTapestryProjectFor(storage);
    return tproject.getModelManager();
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
    IWorkbenchWindow window = getActiveWorkbenchWindow();
    if (window != null) {
      return window.getActivePage();
    }
    return null;
  }

  public String getPluginId() {
    return getDescriptor().getUniqueIdentifier();
  }

  public Shell getActiveWorkbenchShell() {
    IWorkbenchWindow window = getActiveWorkbenchWindow();
    if (window != null) {
      return window.getShell();
    }
    return null;
  }

  public IWorkbenchWindow getActiveWorkbenchWindow() {
    IWorkbench workbench = getWorkbench();
    if (workbench != null) {
      return workbench.getActiveWorkbenchWindow();
    }
    return null;
  }

  static public SpecificationParser getParser() {
    return parser;
  }

  public void logStatus(IStatus status) {
    getLog().log(status);
  }

  public void logException(Throwable e) {
    if (e instanceof InvocationTargetException) {
      e = ((InvocationTargetException) e).getTargetException();
    }
    String message = e.getMessage();
    if (message == null)
      message = e.toString();
    Status status = new Status(IStatus.ERROR, getPluginId(), IStatus.OK, message, e);
    getLog().log(status);
  }
  /** 
   * Sets default preference values. These values will be used
   * until some preferences are actually set using Preference dialog.
   */
  protected void initializeDefaultPreferences(IPreferenceStore store) {
    ColorManager.initializeDefaults(store);
    RenamedComponentOrPageRefactor.initializeDefaults(store);
    NewTapComponentWizardPage.initializeDefaults(store);
    ChooseBindingTypeDialog.initializeDefaults(store);
  }

  static public void openTapestryEditor(IStorage storage) {
    String editorId = null;

    String extension = storage.getFullPath().getFileExtension();

    editorId = (String) editorIdLookup.get(extension);

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

  public static boolean isInSpindleEditor(Object obj) {

    IStorage storage = null;

    if (obj instanceof ITapestryModel) {

      storage = ((ITapestryModel) obj).getUnderlyingStorage();

    } else if (obj instanceof IResource || obj instanceof IStorage) {

      storage = (IStorage) obj;

    }

    if (storage == null) {

      IEditorPart part = Utils.getEditorFor(storage);

      if (part != null) {

        return part instanceof SpindleMultipageEditor;

      }

    }

    return false;
  }

  //the following is ugly because its ripped off from Howard. added a reminder task to
  // see if the validation stuff in AbstractSpecificationParser could be made available as static methods.

  protected Pattern compilePattern(String pattern) throws CoreException {
    if (_patternCompiler == null)
      _patternCompiler = new Perl5Compiler();

    try {
      return _patternCompiler.compile(pattern, Perl5Compiler.SINGLELINE_MASK);
    } catch (MalformedPatternException ex) {
      throw new CoreException(new SpindleStatus(ex));
    }
  }

  public IStatus validate(String value, String pattern, String errorKey) throws CoreException {

    SpindleStatus result = new SpindleStatus();
    if (_compiledPatterns == null)
      _compiledPatterns = new HashMap();

    Pattern compiled = (Pattern) _compiledPatterns.get(pattern);

    if (compiled == null) {
      compiled = compilePattern(pattern);

      _compiledPatterns.put(pattern, compiled);
    }

    if (_matcher == null)
      _matcher = new Perl5Matcher();

    if (_matcher.matches(value, compiled))
      return result;

    result.setError(Tapestry.getString(errorKey, value));
    return result;
  }

}