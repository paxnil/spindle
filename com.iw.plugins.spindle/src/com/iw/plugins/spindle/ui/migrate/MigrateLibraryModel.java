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

import org.eclipse.core.resources.IStorage;

import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MigrateLibraryModel extends MigrationWorkUnit {

  /**
   * Constructor for MigrateLibraryModel.
   */
  public MigrateLibraryModel() {
    super();
  }

  /**
   * Constructor for MigrateLibraryModel.
   * @param contextModel
   * @param sourceModel
   */
  public MigrateLibraryModel(MigrationContext context, int requiredPublicId) {
    super(context, context.getContextModel(), requiredPublicId);
  }

  /**
   * @see com.iw.plugins.spindle.ui.migrate.MigrationWorkUnit#doMigration()
   */
  protected void doMigration() {

    migrateDTD();

  }

  /**
   * Method configure.
   */
  public void configure() {

    TapestryLibraryModel library = (TapestryLibraryModel) sourceModel;
    IPluginLibrarySpecification spec = (IPluginLibrarySpecification) library.getSpecification();
    TapestryLookup lookup = context.getLookup();

    for (Iterator iter = spec.getComponentAliases().iterator(); iter.hasNext();) {

      String componentPath = spec.getComponentSpecificationPath((String) iter.next());
      IStorage[] found = lookup.findComponent(componentPath);

      if (found.length == 0 || !context.isInScope(found[0]) || context.getMigratorFor(found[0]) != null) {

        continue;

      }

      TapestryComponentModel model =
        (TapestryComponentModel) context.getModelManager().getReadOnlyModel(found[0]);

      
      context.registerMigatorFor(
        found[0],
        new MigrateComponentModel(context, model, getRequiredPublicId()));

    }

    for (Iterator iter = spec.getPageNames().iterator(); iter.hasNext();) {

      String pagePath = spec.getPageSpecificationPath((String) iter.next());
      IStorage[] found;

      if (pagePath.endsWith(".jwc")) {

        found = lookup.findComponent(pagePath);

      } else if (pagePath.endsWith(".page")) {

        found = lookup.findPage(pagePath);

      } else {

        continue;
      }

      if (found.length == 0 || !context.isInScope(found[0]) || context.getMigratorFor(found[0]) != null) {

        continue;

      }

      TapestryComponentModel model =
        (TapestryComponentModel) context.getModelManager().getReadOnlyModel(found[0]);

      context.registerMigatorFor(
        found[0],
        new MigrateComponentModel(context, model, getRequiredPublicId()));

    }
  }
}
