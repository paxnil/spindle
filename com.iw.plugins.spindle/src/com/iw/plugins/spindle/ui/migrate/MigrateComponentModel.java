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



package com.iw.plugins.spindle.ui.migrate;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.util.Assert;

import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MigrateComponentModel extends MigrationWorkUnit {

  /**
   * Constructor for MigrateComponentModel.
   */
  public MigrateComponentModel() {
    super();
  }

  /**
   * Constructor for MigrateComponentModel.
   * @param contextModel
   * @param sourceModel
   * @param requiredPublicId
   */
  public MigrateComponentModel(
    MigrationContext context,
    ITapestryModel sourceModel,
    int requiredPublicId) {

    super(context, sourceModel, requiredPublicId);
    Assert.isNotNull(sourceModel);
    Assert.isTrue(sourceModel instanceof TapestryComponentModel);
  }

  /**
   * @see com.iw.plugins.spindle.ui.migrate.MigrationWorkUnit#doMigration()
   */
  protected void doMigration() {
    migrateDTD();
    convertToPageIfNecessary();
    fixComponentAliases();

  }

  protected void convertToPageIfNecessary() {

    boolean allowPageChanges = (context.getConstraints() & MIGRATE_UPGRADE_PAGES) != 0;

    if (!allowPageChanges || !isMigratingDTD() || getRequiredPublicId() < XMLUtil.DTD_1_3) {

      return;
    }

    IStorage storage = sourceModel.getUnderlyingStorage();
    IPath storagePath = storage.getFullPath();
    String extension = storagePath.getFileExtension();
    String filename = storagePath.removeFileExtension().lastSegment();

    if (storage.isReadOnly() || "page".equals(extension)) {

      return;
    }

    IPackageFragment fragment = null;

    try {

      fragment = context.getLookup().findPackageFragment(storage);

    } catch (JavaModelException e) {

      return;
    }

    if (fragment != null) {

      String fragmentPath = "/" + fragment.getElementName().replace('.', '/') + "/";

      String jwcPath = fragmentPath + filename + ".jwc";

      TapestryLibraryModel model = context.getContextModel();
      IPluginLibrarySpecification lib = context.getContextModel().getSpecification();

      String foundPage = model.findPageName(jwcPath);

      if (foundPage != null) {

        // need to convert to a page!

        IPath newPath = storagePath.uptoSegment(storagePath.segmentCount() - 1);
        newPath = newPath.append(filename + ".page");

        setNewPath(newPath.toString());

        lib.setPageSpecificationPath(foundPage, fragmentPath + filename + ".page");
        
        TapestryComponentModel cmodel = (TapestryComponentModel)sourceModel;
        PluginComponentSpecification spec = (PluginComponentSpecification)cmodel.getComponentSpecification();
        spec.setPageSpecification(true);
        setDirty(true);
        

      }

    }

  }

  protected void fixComponentAliases() {

    boolean canProceed = (context.getConstraints() & MIGRATE_COMPONENT_ALIASES) != 0;

    if (!canProceed) {

      return;
    }

    PluginComponentSpecification componentSpec =
      ((TapestryComponentModel) sourceModel).getComponentSpecification();

    List containedComponents = componentSpec.getComponentIds();

    for (Iterator iter = containedComponents.iterator(); iter.hasNext();) {
    	
      String id = (String)iter.next();

      PluginContainedComponent element = (PluginContainedComponent) componentSpec.getComponent(id);

      if (element.getCopyOf() != null) {

        continue;

      }

      String type = element.getType();

      if (type == null || "".equals(type)) {

        continue;
      }

      type = type.trim();

      Path p = new Path("");

      if (p.isValidPath(type)) {

        String newAlias = findFrameworkAlias(type);

        if (newAlias == null) {

          newAlias = findAlias(type, false);

          if (newAlias == null) {

            newAlias = findAlias(type, true);
          }
        }

        if (newAlias != null) {

          element.setType(newAlias);

        }

      }

    }

  }

  public String findAlias(String path, boolean createIfMissing) {

    TapestryLibraryModel model = context.getContextModel();

    String alias = lookupAlias(model, path);

    if (alias == null && createIfMissing) {

      TapestryLookup lookup = context.getLookup();

      IStorage[] result = lookup.findComponent(path);

      if (result.length == 1) {

        IPluginLibrarySpecification spec = model.getSpecification();

        String createdAlias = result[0].getFullPath().removeFileExtension().lastSegment();

        if (spec.getComponentSpecificationPath(createdAlias) != null) {

          int count = 1;
          while (spec.getComponentSpecificationPath(createdAlias + count) != null) {
            count++;
          }

        }

        spec.setComponentSpecificationPath(createdAlias, path);

        return createdAlias;
      }

    }

    return alias;

  }

  private String lookupAlias(TapestryLibraryModel model, String path) {

    return model.findComponentAlias(path);

  }

  public String findFrameworkAlias(String path) {

    return lookupAlias(context.getModelManager().getDefaultLibrary(), path);

  }

}
