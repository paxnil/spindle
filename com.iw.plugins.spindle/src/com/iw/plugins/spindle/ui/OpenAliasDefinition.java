package com.iw.plugins.spindle.ui;

import net.sf.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.ui.IEditorPart;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editorlib.LibraryMultipageEditor;
import com.iw.plugins.spindle.editorlib.components.ComponentsFormPage;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class OpenAliasDefinition extends Action {

  TapestryProjectModelManager manager;
  TapestryLookup lookup;
  IStorage found;
  String useAlias;

  /**
   * Constructor for OpenTapestryPath.
   * @param text
   */
  public OpenAliasDefinition() {
    super();

  }

  public void configure(ITapestryProject project, String alias) {

    setEnabled(false);

    TapestryLibraryModel lib = null;
    try {
      lib = (TapestryLibraryModel) project.getProjectModel();
      lookup = project.getLookup();
    } catch (CoreException e) {

      return;
    }

    TapestryLibraryModel framework = null;
    try {
      manager = project.getModelManager();

      

      framework = (TapestryLibraryModel) manager.getDefaultLibrary();
    } catch (CoreException e) {
    }

    String tapestryPath = null;

    found = findLibrary(lib, alias, framework);

    if (found != null) {

      setEnabled(true);

      setText(found.getName());

    }

  }

  /**
   * Method findAliasPath.
   * @param libSpec
   * @param aliasOrPageName
   * @param framework
   * @return String
   */
  private IStorage findLibrary(TapestryLibraryModel projectLib, String alias, TapestryLibraryModel framework) {

    String foundPath = null;

    IPluginLibrarySpecification libSpec = projectLib.getSpecification();

    int ns_sep = alias.indexOf(":");
    if (ns_sep < 0) {

      useAlias = alias;

      if (libSpec.getComponentAliases().contains(useAlias)) {

        return projectLib.getUnderlyingStorage();

      } else if (framework != null) {

        IPluginLibrarySpecification frameworkSpec = framework.getSpecification();

        if (frameworkSpec.getComponentAliases().contains(useAlias)) {

          return framework.getUnderlyingStorage();

        }
      }

    } else if (manager != null) {

      String libraryName = alias.substring(0, ns_sep);
      useAlias = alias.substring(ns_sep + 1);
      String libraryPath = libSpec.getLibrarySpecificationPath(libraryName);
      if (libraryPath != null) {

        IStorage[] lib = lookup.findByTapestryPath(libraryPath, lookup.ACCEPT_LIBRARIES | lookup.FULL_TAPESTRY_PATH);
        if (lib.length > 0) {

          TapestryLibraryModel importedLib = (TapestryLibraryModel) manager.getReadOnlyModel(lib[0]);
          if (importedLib != null && importedLib.isLoaded()) {

            IPluginLibrarySpecification importedSpec = importedLib.getSpecification();
            if (importedSpec.getComponentAliases().contains(useAlias)) {

              return importedLib.getUnderlyingStorage();
            }
          }
        }
      }
    }
    return null;
  }

  public void run() {

    if (found != null) {

      IEditorPart editor = Utils.getEditorFor(found);
      if (editor != null) {

        TapestryPlugin.getDefault().getActivePage().bringToTop(editor);
      } else {

        TapestryPlugin.getDefault().openTapestryEditor(found);
      }

      editor = Utils.getEditorFor(found);
      if (editor != null && editor instanceof LibraryMultipageEditor) {

        LibraryMultipageEditor libEditor = (LibraryMultipageEditor) editor;
        if (((ITapestryModel) libEditor.getModel()).isLoaded()) {

          ComponentsFormPage page = (ComponentsFormPage) libEditor.getPage(libEditor.COMPONENTS);
          libEditor.showPage(page);
          page.openTo(useAlias);
        }
      }
    }
  }

}
