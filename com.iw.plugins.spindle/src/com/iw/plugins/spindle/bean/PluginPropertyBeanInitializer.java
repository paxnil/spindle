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
package com.iw.plugins.spindle.bean;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.iw.plugins.spindle.spec.IPropertyChangeProvider;
import com.primix.tapestry.bean.PropertyBeanInitializer;
import com.primix.tapestry.util.prop.PropertyHelper;

public class PluginPropertyBeanInitializer
  extends PropertyBeanInitializer
  implements IPropertyChangeProvider {

  PropertyChangeSupport propertySupport;

  private String originalPropertyPath;
  /**
   * Constructor for PluginPropertyBeanIntializer
   */
  public PluginPropertyBeanInitializer(String propertyName, String propertyPath) {
    super(propertyName, propertyPath);
    originalPropertyPath = propertyPath;
    propertySupport = new PropertyChangeSupport(this);
  }

  public String[] getPropertyPath() {
    return propertyPath;
  }

  public String getOriginalPropertyPath() {
    return originalPropertyPath;
  }

  public void setPropertyPath(String path) {
    propertyPath = PropertyHelper.splitPropertyPath(path);
    originalPropertyPath = path;
    propertySupport.firePropertyChange("propertyPath", null, path);
  }

  public void setPropertyName(String name) {
    propertyName = name;
    propertySupport.firePropertyChange("propertyName", null, name);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

}