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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.ui.CheckboxPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.TypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.Indenter;

import net.sf.tapestry.IResourceResolver;
import net.sf.tapestry.spec.ExtensionSpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class PluginExtensionSpecification
  extends ExtensionSpecification
  implements IIdentifiable, IPropertySource {

  private PropertyChangeSupport propertySupport;

  private String identifier;
  private IPluginLibrarySpecification parent;

  public PluginExtensionSpecification() {

    propertySupport = new PropertyChangeSupport(this);

  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }
  
  public void configurationChanged() {
  	
  	propertySupport.firePropertyChange("configration", null, null);
  	
  } 	

  public void setProperty(String name, String value) {

    super.setProperty(name, value);

    propertySupport.firePropertyChange("properties", name, value);
  }

  public void removeProperty(String name) {
    String old = super.getProperty(name);

    if (old != null) {
      super.removeProperty(name);
    }
    propertySupport.firePropertyChange("properties", old, null);
  }

  /**
   * @see net.sf.tapestry.spec.ExtensionSpecification#addConfiguration(String, Object)
   */
  public void addConfiguration(String propertyName, Object value) {
    super.addConfiguration(propertyName, value);
  }

  public void removeConfiguration(String propertyName) {

    Map configuration = getConfiguration();

    if (configuration == null && configuration.containsKey(propertyName)) {

      configuration.remove(propertyName);
      propertySupport.firePropertyChange("configuration", null, configuration);
    }
  }

  /**
   * @see net.sf.tapestry.spec.ExtensionSpecification#setClassName(String)
   */
  public void setClassName(String className) {
    super.setClassName(className);
    propertySupport.firePropertyChange("className", null, className);
  }

  /**
   * @see net.sf.tapestry.spec.ExtensionSpecification#setImmediate(boolean)
   */
  public void setImmediate(boolean immediate) {
    super.setImmediate(immediate);
    propertySupport.firePropertyChange("immediate", null, null);
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
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
    identifier = id;
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
   */
  public void setParent(Object parent) {
    this.parent = (IPluginLibrarySpecification) parent;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
   */
  public Object getEditableValue() {
    return identifier;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
   */
  public IPropertyDescriptor[] getPropertyDescriptors() {

    return new IPropertyDescriptor[] {
      new TextPropertyDescriptor("name", "Name"),
      new TypeDialogPropertyDescriptor("class", "Classname"),
      new CheckboxPropertyDescriptor("immediate", "Immediate")};
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
   */
  public Object getPropertyValue(Object id) {

    String key = (String) id;

    if ("name".equals(key)) {

      return identifier;

    } else if ("class".equals(key)) {

      return getClassName();

    } else if ("immediate".equals(key)) {

      return new Boolean(isImmediate());

    }
    return null;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
   */
  public boolean isPropertySet(Object id) {
    String key = (String) id;

    if ("name".equals(key)) {

      return identifier != null;

    } else if ("class".equals(key)) {

      return getClassName() != null;

    }
    return true;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(Object)
   */
  public void resetPropertyValue(Object id) {
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(Object, Object)
   */
  public void setPropertyValue(Object id, Object value) {

    String key = (String) id;

    if ("name".equals(key)) {

      String oldName = this.identifier;
      String newName = (String) value;

      if ("".equals(newName.trim())) {

        return;

      } else if (parent.getExtensionNames().contains(newName)) {

        newName = "Copy of " + newName;
      }

      this.identifier = newName;
      parent.setExtensionSpecification(this.identifier, this);

    } else if ("class".equals(key)) {

      setClassName((String) value);

    } else if ("immediate".equals(key)) {

      setImmediate(((Boolean) value).booleanValue());

    }

  }

  public void writeConfigurations(PrintWriter writer, int indent) {

    Map configurations = getConfiguration();

    if (configurations != null && !configurations.isEmpty()) {

      for (Iterator iter = new TreeSet(configurations.keySet()).iterator(); iter.hasNext();) {
        String propertyName = (String) iter.next();

        PluginExtensionConfiguration config =
          (PluginExtensionConfiguration) configurations.get(propertyName);

        config.write(writer, indent + 1);

      }

    }

  }

  /**
   * @see net.sf.tapestry.spec.ExtensionSpecification#getConfiguration()
   */
  public Map getConfiguration() {
    return _configuration;
  }

}
