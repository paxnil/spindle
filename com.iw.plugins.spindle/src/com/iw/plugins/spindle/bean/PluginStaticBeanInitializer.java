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
import com.primix.tapestry.bean.StaticBeanInitializer;

public class PluginStaticBeanInitializer extends StaticBeanInitializer 
  implements IPropertyChangeProvider {

  PropertyChangeSupport propertySupport;

  /**
   * Constructor for PluginStaticBeanInitializer
   */
  public PluginStaticBeanInitializer(String propertyName, Object value) {
    super(propertyName, value);
    propertySupport = new PropertyChangeSupport(this);
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object newValue) {
    value = newValue;
    propertySupport.firePropertyChange("staticValue", null, newValue);
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