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
package com.iw.plugins.spindle.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeFormatter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;

/** 
 * A class to collect useful methods in. May move them elsewhere or, then again
 * I may not.
 */

public class Utils {

  /**
   * @return all the editors in the workbench that need saving
   */
  public static IEditorPart[] getOpenEditors() {
    List result = new ArrayList(0);
    IWorkbench workbench = TapestryPlugin.getDefault().getWorkbench();
    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
    for (int i = 0; i < windows.length; i++) {
      IWorkbenchPage[] pages = windows[i].getPages();
      for (int x = 0; x < pages.length; x++) {
        IEditorReference[] erefs = pages[x].getEditorReferences();
        for (int j = 0; j < erefs.length; j++) {
          IEditorReference iEditorReference = erefs[j];
          IEditorPart part = iEditorReference.getEditor(false);
          result.add(part);

        }
      }
    }
    return (IEditorPart[]) result.toArray(new IEditorPart[result.size()]);
  }

  public static IEditorPart[] getDirtyEditors() {
    IEditorPart[] openEditors = getOpenEditors();
    List result = new ArrayList(0);
    for (int i = 0; i < openEditors.length; i++) {
      if (openEditors[i].isDirty()) {
        result.add(openEditors[i]);
      }
    }
    return (IEditorPart[]) result.toArray(new IEditorPart[result.size()]);
  }

  /**
    * @return the editor for a Tapestry model
    */
  public static IEditorPart getEditorFor(ITapestryModel model) {
    IWorkbench workbench = TapestryPlugin.getDefault().getWorkbench();
    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
    for (int i = 0; i < windows.length; i++) {
      IWorkbenchPage[] pages = windows[i].getPages();
      for (int x = 0; x < pages.length; x++) {
        IEditorReference[] editors = pages[x].getEditorReferences();
        for (int z = 0; z < editors.length; z++) {
          IEditorReference ref = editors[z];
          IEditorPart editor = ref.getEditor(true);
          IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
          if (editorFile == null) {
            continue;
          }
          ITapestryModel editorModel = TapestryPlugin.getTapestryModelManager().getModel(editorFile);
          if (editorModel == model) {
            return editor;
          }
        }
      }
    }
    return null;
  }

  /**
   * 
   * this is ugly - should handle this nicely like TapestryLookup does for models
   * @return the editor for a model instance, or null if there is no such editor or if the found editor is not dirty
   * 
   */
  public static IEditorPart getDirtyEditorFor(ITapestryModel model) {
    if (model == null) {
      return null;
    }
    IEditorPart[] dirty = getDirtyEditors();
    for (int i = 0; i < dirty.length; i++) {
      if (dirty[i] instanceof SpindleMultipageEditor) {
        if (((SpindleMultipageEditor) dirty[i]).getModel() == model) {
          return dirty[i];
        }
      }
    }
    return null;
  }

  public static List getApplicationsWithAlias(String alias) {
    ArrayList result = new ArrayList();
    Iterator iter = TapestryPlugin.getTapestryModelManager().applicationsIterator();
    while (iter.hasNext()) {
      TapestryApplicationModel model = (TapestryApplicationModel) iter.next();
      if (!model.isLoaded()) {
        try {
          model.load();
        } catch (Exception e) {
          continue;
        }
      }
      PluginApplicationSpecification spec = (PluginApplicationSpecification) model.getApplicationSpec();
      if (spec != null && spec.getComponentAlias(alias) != null) {
        result.add(model);
      }
    }
    return result;
  }

  // assumes target ComponentModel is loaded.
  // this could use some refactoring for sure!
  public static void copyContainedComponentTo(
    String sourceName,
    PluginContainedComponent sourceComponent,
    TapestryComponentModel target)
    throws Exception {
    String useName = sourceName;
    PluginComponentSpecification targetSpec = (PluginComponentSpecification) target.getComponentSpecification();
    if (targetSpec.getComponent(sourceName + 1) != null) {
      int counter = 2;
      while (targetSpec.getComponent(sourceName + counter) != null) {
        counter++;
      }
      useName = sourceName + counter;
    } else {
      sourceName = sourceName + 1;
    }
    PluginContainedComponent resultComponent = new PluginContainedComponent();
    resultComponent.setType(sourceComponent.getType());
    resultComponent.setCopyOf(sourceComponent.getCopyOf());
    Iterator iter1 = sourceComponent.getBindingNames().iterator();
    while (iter1.hasNext()) {
      String parameter = (String) iter1.next();
      PluginBindingSpecification binding = (PluginBindingSpecification) sourceComponent.getBinding(parameter);
      PluginBindingSpecification bindingCopy = new PluginBindingSpecification(binding.getType(), binding.getValue());
      resultComponent.setBinding(parameter, bindingCopy);
    }
    targetSpec.addComponent(useName, resultComponent);
    target.setOutOfSynch(true);
  }

  public static void createContainedComponentIn(String jwcid, String containedComponentPath, TapestryComponentModel target) {
    PluginComponentSpecification spec = target.getComponentSpecification();
    if (spec.getComponent(jwcid) == null) {
      PluginContainedComponent contained = new PluginContainedComponent();
      contained.setType(containedComponentPath);
      spec.addComponent(jwcid, contained);
      target.setOutOfSynch(true);
    }
  }

  /**
   * Returns the first java element that conforms to the given type walking the
   * java element's parent relationship. If the given element alrady conforms to
   * the given kind, the element is returned.
   * Returns <code>null</code> if no such element exits.
   */
  public static IJavaElement findElementOfKind(IJavaElement element, int kind) {
    while (element != null && element.getElementType() != kind)
      element = element.getParent();
    return element;
  }

  /**
   * Returns the package fragment root of <code>IJavaElement</code>. If the given
   * element is already a package fragment root, the element itself is returned.
   */
  public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
    return (IPackageFragmentRoot) findElementOfKind(element, IJavaElement.PACKAGE_FRAGMENT_ROOT);
  }

  /**
   * Returns true if the element is on the build path of the given project
   */
  public static boolean isOnBuildPath(IJavaProject jproject, IJavaElement element) throws JavaModelException {
    IPath rootPath;
    if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
      rootPath = ((IJavaProject) element).getProject().getFullPath();
    } else {
      IPackageFragmentRoot root = getPackageFragmentRoot(element);
      if (root == null) {
        return false;
      }
      rootPath = root.getPath();
    }
    return jproject.findPackageFragmentRoot(rootPath) != null;
  }

  /** 
   * Finds a type by its qualified type name (dot separated).
   * @param jproject The java project to search in
   * @param str The fully qualified name (type name with enclosing type names and package (all separated by dots))
   * @return The type found, or null if not existing
   * The method only finds top level types and its inner types. Waiting for a Java Core solution
   */
  public static IType findType(IJavaProject jproject, String fullyQualifiedName) throws JavaModelException {
    String pathStr = fullyQualifiedName.replace('.', '/') + ".java"; //$NON-NLS-1$
    IJavaElement jelement = jproject.findElement(new Path(pathStr));
    if (jelement == null) {
      // try to find it as inner type
      String qualifier = Signature.getQualifier(fullyQualifiedName);
      if (qualifier.length() > 0) {
        IType type = findType(jproject, qualifier); // recursive!
        if (type != null) {
          IType res = type.getType(Signature.getSimpleName(fullyQualifiedName));
          if (res.exists()) {
            return res;
          }
        }
      }
    } else if (jelement.getElementType() == IJavaElement.COMPILATION_UNIT) {
      String simpleName = Signature.getSimpleName(fullyQualifiedName);
      return ((ICompilationUnit) jelement).getType(simpleName);
    } else if (jelement.getElementType() == IJavaElement.CLASS_FILE) {
      return ((IClassFile) jelement).getType();
    }
    return null;
  }

  /** 
   * Finds a type by package and type name.
   * @param jproject the java project to search in
   * @param pack The package name
   * @param typeQualifiedName the type qualified name (type name with enclosing type names (separated by dots))
   * @return the type found, or null if not existing
   * The method only finds top level types and its inner types. Waiting for a Java Core solution
   */
  public static IType findType(IJavaProject jproject, String pack, String typeQualifiedName) throws JavaModelException {
    // should be supplied from java core
    int dot = typeQualifiedName.indexOf('.');
    if (dot == -1) {
      return findType(jproject, concatenateName(pack, typeQualifiedName));
    }
    IPath packPath;
    if (pack.length() > 0) {
      packPath = new Path(pack.replace('.', '/'));
    } else {
      packPath = new Path(""); //$NON-NLS-1$
    }
    // fixed for 1GEXEI6: ITPJUI:ALL - Incorrect error message on class creation wizard
    IPath path = packPath.append(typeQualifiedName.substring(0, dot) + ".java"); //$NON-NLS-1$
    IJavaElement elem = jproject.findElement(path);
    if (elem instanceof ICompilationUnit) {
      return findTypeInCompilationUnit((ICompilationUnit) elem, typeQualifiedName);
    } else if (elem instanceof IClassFile) {
      path = packPath.append(typeQualifiedName.replace('.', '$') + ".class"); //$NON-NLS-1$
      elem = jproject.findElement(path);
      if (elem instanceof IClassFile) {
        return ((IClassFile) elem).getType();
      }
    }
    return null;
  }

  /** 
   * Finds a type in a compilation unit. Typical usage is to find the corresponding
   * type in a working copy.
   * @param cu the compilation unit to search in
   * @param typeQualifiedName the type qualified name (type name with enclosing type names (separated by dots))
   * @return the type found, or null if not existing
   */
  public static IType findTypeInCompilationUnit(ICompilationUnit cu, String typeQualifiedName) throws JavaModelException {
    IType[] types = cu.getAllTypes();
    for (int i = 0; i < types.length; i++) {
      String currName = getTypeQualifiedName(types[i]);
      if (typeQualifiedName.equals(currName)) {
        return types[i];
      }
    }
    return null;
  }

  /**
  * Concatenates two names. Uses a dot for separation.
  * Both strings can be empty or <code>null</code>.
  */
  public static String concatenateName(String name1, String name2) {
    StringBuffer buf = new StringBuffer();
    if (name1 != null && name1.length() > 0) {
      buf.append(name1);
    }
    if (name2 != null && name2.length() > 0) {
      if (buf.length() > 0) {
        buf.append('.');
      }
      buf.append(name2);
    }
    return buf.toString();
  }

  /**
   * Returns the qualified type name of the given type using '.' as separators.
   * This is a replace for IType.getTypeQualifiedName()
   * which uses '$' as separators. As '$' is also a valid character in an id
   * this is ambiguous. JavaCore PR: 1GCFUNT
   */
  public static String getTypeQualifiedName(IType type) {
    StringBuffer buf = new StringBuffer();
    getTypeQualifiedName(type, buf);
    return buf.toString();
  }

  private static void getTypeQualifiedName(IType type, StringBuffer buf) {
    IType outerType = type.getDeclaringType();
    if (outerType != null) {
      getTypeQualifiedName(outerType, buf);
      buf.append('.');
    }
    buf.append(type.getElementName());
  }

  /**
   * Evaluates if a member (possible from another package) is visible from
   * elements in a package.
   * @param member The member to test the visibility for
   * @param pack The package in focus
   */
  public static boolean isVisible(IMember member, IPackageFragment pack) throws JavaModelException {
    int otherflags = member.getFlags();

    if (Flags.isPublic(otherflags) || Flags.isProtected(otherflags)) {
      return true;
    } else if (Flags.isPrivate(otherflags)) {
      return false;
    }

    IPackageFragment otherpack = (IPackageFragment) findElementOfKind(member, IJavaElement.PACKAGE_FRAGMENT);
    return (pack != null && pack.equals(otherpack));
  }

  public static String codeFormat(String sourceString, int initialIndentationLevel, String lineDelim) {
    ICodeFormatter formatter = ToolFactory.createDefaultCodeFormatter(null);
    return formatter.format(sourceString, initialIndentationLevel, null, lineDelim) + lineDelim;
  }

  public static boolean extendsType(IType candidate, IType baseType) throws JavaModelException {
    boolean match = false;
    ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
    if (hierarchy.exists()) {
      IType[] superClasses = hierarchy.getAllSupertypes(candidate);
      for (int i = 0; i < superClasses.length; i++) {
        if (superClasses[i].equals(baseType)) {
          match = true;
        }
      }
    }
    return match;
  }

  public static boolean implementsInterface(IType candidate, String interfaceName) throws JavaModelException {
    boolean match = false;
    String[] superInterfaces = candidate.getSuperInterfaceNames();
    if (superInterfaces != null && superInterfaces.length > 0) {
      for (int i = 0; i < superInterfaces.length; i++) {
        if (candidate.isBinary() && interfaceName.endsWith(superInterfaces[i])) {
          match = true;
        } else {
          match = interfaceName.equals(superInterfaces[i]);
        }
      }
    } else {
      match = false;
    }
    return match;
  }

}