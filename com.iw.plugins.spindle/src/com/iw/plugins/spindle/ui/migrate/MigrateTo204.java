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

import java.util.Collection;
import java.util.Iterator;

import net.sf.tapestry.spec.BindingType;

import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;

public class MigrateTo204 implements IModelMigrator {

  String oldPackage = "com.primix.tapestry";
  String oldPath = "/com/primix/tapestry";
  String newPackage = "net.sf.tapestry";
  String newPath = "/net/sf/tapestry";

  /**
   * @see com.iw.plugins.spindle.ui.migrate.IModelMigrator#migrate(ITapestryModel)
   */
  public boolean migrate(ITapestryModel model) {
    if (!model.getUnderlyingStorage().isReadOnly()) {

      if (model instanceof TapestryComponentModel) {

        migrateComponentModel((TapestryComponentModel) model);

        return model.isDirty();

      } else if (model instanceof TapestryApplicationModel) {

        migrateApplicationModel((TapestryApplicationModel) model);

        return model.isDirty();

      }
    }
    return false;
  }

  /**
   * Method migrateApplicationModel.
   * @param tapestryApplicationModel
   */
  private void migrateApplicationModel(TapestryApplicationModel model) {
    PluginApplicationSpecification spec = (PluginApplicationSpecification) model.getSpecification();
    String DTD = spec.getDTDVersion();
    if (DTD == null || "1.1".equals(DTD)) {
      spec.setDTDVersion("1.2");
    }
    String newEngineclass = migratePackage(spec.getEngineClassName());
    if (newEngineclass != null) {
      spec.setEngineClassName(newEngineclass);
    }
    migrateAppServiceClassnames(spec);
    migrateAppPropertyValues(spec);
  }

  /**
   * Method migrateAppPropertyValues.
   * @param spec
   */
  private void migrateAppPropertyValues(PluginApplicationSpecification spec) {
    Collection propertyNames = spec.getPropertyNames();
    for (Iterator iter = propertyNames.iterator(); iter.hasNext();) {
      String name = (String) iter.next();
      String newPropertyValue = migratePackage(spec.getProperty(name));
      if (newPropertyValue != null) {
        spec.setProperty(name, newPropertyValue);
      }
    }
  }

  /**
   * Method migrateAppServiceClassnames.
   * @param spec
   */
  private void migrateAppServiceClassnames(PluginApplicationSpecification spec) {
    Collection serviceNames = spec.getServiceNames();
    for (Iterator iter = serviceNames.iterator(); iter.hasNext();) {
      String name = (String) iter.next();
      String newServiceClass = migratePackage(spec.getServiceClassName(name));
      if (newServiceClass != null) {
        spec.setServiceClassName(name, newServiceClass);
      }
    }
  }

  /**
   * Method migrateComponentModel.
   * @param tapestryComponentModel
   */
  private void migrateComponentModel(TapestryComponentModel model) {
    PluginComponentSpecification spec = (PluginComponentSpecification) model.getComponentSpecification();
    String DTD = spec.getDTDVersion();
    if (DTD == null || "1.1".equals(DTD)) {
      spec.setDTDVersion("1.2");
    }
    String newComponentClass = migratePackage(spec.getComponentClassName());
    if (newComponentClass != null) {
      spec.setComponentClassName(newComponentClass);
    }
    migrateContainedComponents(spec);

  }

  /**
   * Method migrateContainedComponents.
   * @param spec
   */
  private void migrateContainedComponents(PluginComponentSpecification spec) {
    Collection componentIds = spec.getComponentIds();
    for (Iterator iter = componentIds.iterator(); iter.hasNext();) {
      String id = (String) iter.next();
      PluginContainedComponent component = (PluginContainedComponent) spec.getComponent(id);
      migrateContainedComponent(component);
    }
  }

  /**
   * Method migrateContainedComponent.
   * @param component
   */
  private void migrateContainedComponent(PluginContainedComponent component) {
    String newType = migratePath(component.getType());
    if (newType != null) {
    	component.setType(newType);
    }
    Collection bindings = component.getBindingNames();
    for (Iterator iter = bindings.iterator(); iter.hasNext();) {
      String name = (String) iter.next();
      PluginBindingSpecification binding = (PluginBindingSpecification)component.getBinding(name);
      if (binding.getType().equals(BindingType.FIELD)) {
      	String newValue = migratePackage(binding.getValue());
      	if (newValue != null) {
      		binding.setValue(newValue);
      	}
      }
    }
  }

  private String migratePackage(String candidate) {
    if (candidate.startsWith(oldPackage)) {
      return newPackage + candidate.substring(oldPackage.length());
    }
    return null;
  }

  private String migratePath(String candidate) {
    if (candidate.startsWith(oldPath)) {
      return newPath + candidate.substring(oldPath.length());
    }
    return null;
  }

}
