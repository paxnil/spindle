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

import net.sf.tapestry.bean.StaticBeanInitializer;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.IPropertyChangeProvider;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.ui.CheckboxPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.ComboBoxPropertyDescriptor;

public class PluginStaticBeanInitializer
  extends StaticBeanInitializer
  implements IPropertyChangeProvider, IIdentifiable, IPropertySource {

  static final String[] typeNames = { "Boolean", "String", "Double", "Integer" };
  static final int BOOLEAN = 0;
  static final int STRING = 1;
  static final int DOUBLE = 2;
  static final int INTEGER = 3;
  static final Double trueD = new Double(1.0);
  static final Double falseD = new Double(0.0);
  static final Integer trueI = new Integer(1);
  static final Integer falseI = new Integer(0);

  PropertyChangeSupport propertySupport;
  
  private PluginBeanSpecification parent;

  /**
   * Constructor for PluginStaticBeanInitializer
   */
  public PluginStaticBeanInitializer(String propertyName, Object value) {
    super(propertyName, value);
    propertySupport = new PropertyChangeSupport(this);
  }
  
  public PluginStaticBeanInitializer deepCopy() {
  	return new PluginStaticBeanInitializer(getPropertyName(), getValue());
  }
  

  public Object getValue() {
    return _value;
  }

  public void setValue(Object newValue) {
    _value = newValue;
    propertySupport.firePropertyChange("staticValue", null, newValue);
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

  private TextPropertyDescriptor propertyDescriptor =
    new TextPropertyDescriptor("property", "Property Name");

  private ComboBoxPropertyDescriptor typeDescriptor =
    new ComboBoxPropertyDescriptor("type", "Type", typeNames, false);

  private IPropertyDescriptor[] booleanTypeDescriptors =
    new IPropertyDescriptor[] {
      propertyDescriptor,
      typeDescriptor,
      new CheckboxPropertyDescriptor("value", "Value")};

  private IPropertyDescriptor[] otherTypeDescriptors =
    new IPropertyDescriptor[] {
      propertyDescriptor,
      typeDescriptor,
      new TextPropertyDescriptor("value", "Value")};

  public String toString() {
    String result = "property = \"" + getPropertyName();
    Object value = getValue();
    if (value == null) {
      return result + "\" value is null";
    }
    return result
      + "\" type = \""
      + typeNames[getInitializerValueType()]
      + "\" value = \""
      + value.toString()
      + "\"";
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
        PluginStaticBeanInitializer copy = new PluginStaticBeanInitializer(newName, getValue());
        bean.addInitializer(copy);
        return;
      }
      setPropertyName(newName);

    } else if ("type".equals(key)) {

      Object oldValue = getValue();
      int type = ((Integer) value).intValue();
      //set value to the new type to enable conversion below
      switch (type) {
        case BOOLEAN :
          setValue(Boolean.TRUE);
          break;
        case STRING :
          setValue("");
          break;
        case DOUBLE :
          setValue(trueD);
          break;
        case INTEGER :
          setValue(trueI);
          break;
      }
      // now we can force conversion
      doSetValue(oldValue.toString());

    } else if ("value".equals(key)) {
      // conversion is forced here too based on existing type
      doSetValue(value);
    }
  }

  // set the initializer's value, converting if necessary
  private void doSetValue(Object value) {
    Object converted = null;
    // find the type of the existing value so we know what
    // we are converting 'value' into
    int type = getInitializerValueType();
    // there are only two type of cell editors we are using
    // string, and boolean
    if (value instanceof String) {
      switch (type) {
        case BOOLEAN :
          converted = convertStringToBoolean((String) value);
          break;
        case STRING :
          converted = value;
          break;
        case DOUBLE :
          converted = convertStringToDouble((String) value);
          break;
        case INTEGER :
          converted = convertStringToInteger((String) value);
          break;
      }
    } else { // its Boolean
      boolean bvalue = ((Boolean) value).booleanValue();
      switch (type) {
        case BOOLEAN :
          converted = value;
          break;
        case STRING :
          converted = ((Boolean) value).toString();
          break;
        case DOUBLE :
          converted = (bvalue ? trueD : falseD);
          break;
        case INTEGER :
          converted = (bvalue ? trueD : falseD);
          break;
      }
    }
    setValue(converted);
  }

  private int getInitializerValueType() {
    String name = getValue().getClass().getName();
    for (int i = 0; i < typeNames.length; i++) {
      if (name.endsWith(typeNames[i])) {
        return i;
      }
    }
    return -1;
  }

  private Double convertStringToDouble(String candidate) {
    Double result = new Double(0.0);
    try {
      result = new Double(candidate);
    } catch (Exception e) {
    }
    return result;
  }

  private Integer convertStringToInteger(String candidate) {
    Integer result = new Integer(0);
    try {
      result = new Integer(candidate);
    } catch (Exception e) {
    }
    return result;
  }

  private Boolean convertStringToBoolean(String candidate) {
    Boolean result = Boolean.FALSE;
    try {
      result = new Boolean(candidate);
    } catch (Exception e) {
    }
    return result;
  }

  public boolean isPropertySet(Object key) {
    if ("property".equals(key)) {

      return getPropertyName() != null;

    } else if ("value".equals(key)) {

      return getValue() != null;

    }

    return true;
  }

  public Object getPropertyValue(Object key) {
    if ("property".equals(key)) {

      return getPropertyName();

    } else if ("type".equals(key)) {

      return new Integer(getInitializerValueType());

    } else if ("value".equals(key)) {

      if (getInitializerValueType() == BOOLEAN) {

        return ((Boolean) getValue());

      } else {

        return getValue().toString();
      }
    }
    return null;
  }

  public IPropertyDescriptor[] getPropertyDescriptors() {

    if (getInitializerValueType() == BOOLEAN) {

      return booleanTypeDescriptors;

    } else {

      return otherTypeDescriptors;
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
    return (String)getEditableValue();
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
  	
  	this.parent = (PluginBeanSpecification)parent;
  }

}
