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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.util.Assert;

import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MigrationContext implements IMigrationConstraints {

  private String description;

  private TapestryLibraryModel contextModel;
  private TapestryProjectModelManager modelManager;
  private TapestryLookup lookup;

  private Map migrators = new HashMap();

  private List scope = new ArrayList();

  private int constraints;

  private int requiredPublicId;

  /**
   * Constructor for MigrationContext.
   */
  public MigrationContext(
    String description,
    TapestryLibraryModel context,
    TapestryProjectModelManager modelManager,
    TapestryLookup lookup,
    int constraints,
    int requiredPublicId) {

    Assert.isNotNull(context);
    Assert.isNotNull(modelManager);
    Assert.isNotNull(lookup);
    Assert.isTrue(requiredPublicId >= XMLUtil.DTD_1_1 && requiredPublicId <= XMLUtil.DTD_1_3);
    this.description = description;
    this.contextModel = context;
    this.modelManager = modelManager;
    this.lookup = lookup;
    this.constraints = constraints;
    this.requiredPublicId = requiredPublicId;

  }

  public void registerMigatorFor(IStorage storage, MigrationWorkUnit migrator) {

    if (scope.contains(storage) && !migrators.containsKey(storage)) {

      migrators.put(storage, migrator);

    }

  }

  public MigrationWorkUnit getMigratorFor(IStorage storage) {

    return (MigrationWorkUnit) migrators.get(storage);

  }

  public boolean isInScope(IStorage storage) {

    return scope.contains(storage);

  }

  public void setScope(List list) {

    scope = list;
    reset();

  }

  /**
   * Method getScope.
   * @return Object
   */
  public Object getScope() {
    if (scope == null || scope.isEmpty()) {
      return null;
    }

    return scope;
  }

  public void constructMigrators() {

    migrators.clear();

    MigrateLibraryModel libMigrator = new MigrateLibraryModel(this, requiredPublicId);
    
    registerMigatorFor(contextModel.getUnderlyingStorage(), libMigrator);

    libMigrator.configure();

  }

  /**
   * Returns the contextModel.
   * @return TapestryLibraryModel
   */
  public TapestryLibraryModel getContextModel() {
    return contextModel;
  }

  /**
   * Returns the lookup.
   * @return TapestryLookup
   */
  public TapestryLookup getLookup() {
    return lookup;
  }

  /**
   * Returns the modelManager.
   * @return TapestryProjectModelManager
   */
  public TapestryProjectModelManager getModelManager() {
    return modelManager;
  }

  /**
   * Returns the constraints.
   * @return int
   */
  public int getConstraints() {
    return constraints;
  }

  /**
   * Returns the description.
   * @return String
   */
  public String getDescription() {
    return description;
  }

  /**
   * Method getMigrationMap.
   * @return Map
   */
  public Map getMigrationMap() {
    return migrators;
  }

  /**
   * Sets the constraints.
   * @param constraints The constraints to set
   */
  public void setConstraints(int constraints) {
    this.constraints = constraints;
  }

  public void reset() {

    migrators.clear();

  }

  /**
   * Returns the requiredPublicId.
   * @return int
   */
  public int getRequiredPublicId() {
    return requiredPublicId;
  }

  /**
   * Sets the requiredPublicId.
   * @param requiredPublicId The requiredPublicId to set
   */
  public void setRequiredPublicId(int requiredPublicId) {
    this.requiredPublicId = requiredPublicId;
  }

  /**
   * Method setConstraints.
   * @param objects
   */
  public void setConstraints(Object[] constraints) {

    int newConstraints = 0;

    for (int i = 0; i < constraints.length; i++) {

      newConstraints |= ((Integer) constraints[i]).intValue();
    }

    this.constraints = newConstraints;
  }
  
  public boolean hasConstraint (int value) {
  	
  	return (constraints & value) != 0;
  	
  }

}
