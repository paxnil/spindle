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



package com.iw.plugins.spindle.spec.bean;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import net.sf.tapestry.bean.StringBeanInitializer;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.IPropertyChangeProvider;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class PluginStringBeanInitializer
  extends StringBeanInitializer
  implements IPropertyChangeProvider, IIdentifiable, IPropertySource {
  	
  PropertyChangeSupport propertySupport;

  private PluginBeanSpecification parent;

  /**
   * Constructor for PluginStringBeanInitializer.
   * @param propertyName
   * @param key
   */
  public PluginStringBeanInitializer(String propertyName, String key) {
    super(propertyName, key);
    propertySupport = new PropertyChangeSupport(this);
  }

  public PluginStringBeanInitializer deepCopy() {
    return new PluginStringBeanInitializer(getPropertyName(), getKey());
  }

  public String getKey() {
    return _key;
  }



  public void setKey(String value) {
    _key = value;
    propertySupport.firePropertyChange("key", null, value);
  }

  public void setPropertyName(String name) {
    _propertyName = name;
    propertySupport.firePropertyChange("propertyName", null, name);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  private IPropertyDescriptor[] descriptors =
    {
      new TextPropertyDescriptor("property", "Property Name"),
      new TextPropertyDescriptor("value", "Key")};

  public String toString() {
    String result = "property = \"" + getPropertyName();
    String value = getKey();

    if (value == null) {

      return result + "\" value is null";
    }
    return result + "\" key = \"" + value + "\"";
  }

  public void setPropertyValue(Object key, Object value) {

    PluginBeanSpecification bean = (PluginBeanSpecification) getParent();

    if ("property".equals(key)) {

      String oldName = getPropertyName();
      String newName = (String) value;

      if ("".equals(newName.trim())) {

        newName = oldName;

      } else if (bean.alreadyHasInitializer(newName)) {

        newName = "Copy of " + newName;
        PluginStringBeanInitializer copy =
          new PluginStringBeanInitializer(newName, getKey());
        bean.addInitializer(copy);
      }

      setPropertyName(newName);

    } else if ("value".equals(key)) {

      setKey((String) value);

    }
  }

  public boolean isPropertySet(Object key) {

    if ("property".equals(key)) {

      return getPropertyName() != null;

    } else if ("value".equals(key)) {

      return getKey() != null;

    } else {
      return true;
    }
  }

  public Object getPropertyValue(Object key) {
    if ("property".equals(key)) {

      return getPropertyName();

    } else if ("value".equals(key)) {

      return getKey();
    }
    return null;
  }

  public IPropertyDescriptor[] getPropertyDescriptors() {
    return descriptors;
  }

  public Object getEditableValue() {
    return getPropertyName();
  }

  /**
  * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(Object)
  */
  public void resetPropertyValue(Object id) {
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#getIdentifier()
   */
  public String getIdentifier() {
    return (String) getEditableValue();
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#getParent()
   */
  public Object getParent() {
    return parent;
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#setIdentifier(String)
   */
  public void setIdentifier(String id) {
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
   */
  public void setParent(Object parent) {

    this.parent = (PluginBeanSpecification) parent;
  }

}
