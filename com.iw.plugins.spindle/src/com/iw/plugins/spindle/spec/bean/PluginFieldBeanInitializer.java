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

import net.sf.tapestry.bean.FieldBeanInitializer;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.IPropertyChangeProvider;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.ui.descriptors.FieldPropertyDescriptor;

/**
 * @author GWL
 * @version $Id$
 */
public class PluginFieldBeanInitializer
  extends FieldBeanInitializer
  implements IPropertyChangeProvider, IIdentifiable, IPropertySource {

  PropertyChangeSupport propertySupport;

  private PluginBeanSpecification parent;

  private IPropertyDescriptor[] descriptors =
    {
      new TextPropertyDescriptor("property", "Property Name"),
      new FieldPropertyDescriptor("value", "Field Name")};

  /**
   * Constructor for PluginFieldBeanInitializer.
   * @param propertyName
   * @param fieldName
   */
  public PluginFieldBeanInitializer(String propertyName, String fieldName) {
    super(propertyName, fieldName);
    propertySupport = new PropertyChangeSupport(this);
  }
  
  public PluginFieldBeanInitializer deepCopy() {
  	return new PluginFieldBeanInitializer(getPropertyName(), getFieldName());
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String value) {
    fieldName = value;
    propertySupport.firePropertyChange("fieldName", null, value);
  }

  public void setPropertyName(String name) {
    propertyName = name;
    propertySupport.firePropertyChange("propertyName", null, name);
  }

  /* (non-Javadoc)
   * @see IPropertyChangeProvider#addPropertyChangeListener(PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  /* (non-Javadoc)
   * @see IPropertyChangeProvider#removePropertyChangeListener(PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  public String toString() {
    String result = "property = \"" + getPropertyName();
    String value = getFieldName();
    if (value == null) {
      return result + "\" value is null";
    }
    return result + "\" field-name = \"" + value + "\"";
  }

  /* (non-Javadoc)
   * @see IPropertySource#getPropertyDescriptors()
   */
  public IPropertyDescriptor[] getPropertyDescriptors() {
    return descriptors;
  }

  /* (non-Javadoc)
   * @see IPropertySource#getPropertyValue(Object)
   */
  public Object getPropertyValue(Object key) {
    if ("property".equals(key)) {
      return getPropertyName();
    } else if ("value".equals(key)) {
      return getFieldName();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see IPropertySource#isPropertySet(Object)
   */
  public boolean isPropertySet(Object key) {
    if ("property".equals(key)) {
      return getPropertyName() != null;
    } else if ("value".equals(key)) {
      return getFieldName() != null;
    } else {
      return true;
    }
  }

  /* (non-Javadoc)
  * @see IPropertySource#setPropertyValue(Object, Object)
  */
  public void setPropertyValue(Object key, Object value) {
  	
  	PluginBeanSpecification bean = (PluginBeanSpecification)getParent();
  	
    if ("property".equals(key)) {
      String oldName = getPropertyName();
      String newName = (String) value;
      if ("".equals(newName.trim())) {
        newName = oldName;
      } else if (bean.alreadyHasInitializer(newName)) {
        newName = "Copy of " + newName;
        PluginFieldBeanInitializer copy =
          new PluginFieldBeanInitializer(newName, getFieldName());
		bean.addInitializer(copy);
        return;
      }
      setPropertyName(newName);
    } else if ("value".equals(key)) {
      setFieldName((String) value);
    }

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
