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
package com.iw.plugins.spindle.spec;

import com.iw.plugins.spindle.bean.PluginFieldBeanInitializer;
import com.iw.plugins.spindle.bean.PluginPropertyBeanInitializer;
import com.iw.plugins.spindle.bean.PluginStaticBeanInitializer;
import net.sf.tapestry.bean.IBeanInitializer;
import net.sf.tapestry.spec.*;

public class TapestryPluginFactory extends SpecFactory {

  /**
   * Constructor for TapestryPluginFactory
   */
  public TapestryPluginFactory() {
    super();
  }

  public ApplicationSpecification createApplicationSpecification() {
    return new PluginApplicationSpecification();
  }

  public AssetSpecification createAssetSpecification(AssetType type, String path) {
    return new PluginAssetSpecification(type, path);
  }

  public BeanSpecification createBeanSpecification(String className, BeanLifecycle lifecycle) {
    return new PluginBeanSpecification(className, lifecycle);
  }

  public BindingSpecification createBindingSpecification(BindingType type, String value) {
    return new PluginBindingSpecification(type, value);
  }

  public ComponentSpecification createComponentSpecification() {
    return new PluginComponentSpecification();
  }

  public ContainedComponent createContainedComponent() {
    return new PluginContainedComponent();
  }

  public PageSpecification createPageSpecification() {
    return new PluginPageSpecification();
  }

  public PageSpecification createPageSpecification(String specificationPath) {
    return new PluginPageSpecification(specificationPath);
  }

  public ParameterSpecification createParameterSpecification() {
    return new PluginParameterSpecification();
  }

  public IBeanInitializer createPropertyBeanInitializer(String propertyName, String propertyPath) {
    return new PluginPropertyBeanInitializer(propertyName, propertyPath);
  }

  public IBeanInitializer createStaticBeanInitializer(String propertyName, Object staticValue) {
    return new PluginStaticBeanInitializer(propertyName, staticValue);
  }

  /* (non-Javadoc)
   * @see SpecFactory#createFieldBeanInitializer(String, String)
   */
  public IBeanInitializer createFieldBeanInitializer(String propertyName, String fieldName) {
    return new PluginFieldBeanInitializer(propertyName, fieldName);
  }

}
