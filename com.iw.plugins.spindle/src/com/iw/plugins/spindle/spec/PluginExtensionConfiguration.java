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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.ui.CheckboxPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.ComboBoxPropertyDescriptor;
import com.iw.plugins.spindle.util.Indenter;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class PluginExtensionConfiguration implements IIdentifiable, IPropertySource {

  static final String[] typeNames = { "boolean", "string", "double", "int", "long" };
  static final Double trueD = new Double(1.0);
  static final Double falseD = new Double(0.0);
  static final Integer trueI = new Integer(1);
  static final Integer falseI = new Integer(0);
  static final Long trueL = new Long(1);
  static final Long falseL = new Long(0);

  private IPropertyDescriptor[] booleanTypeDescriptors =
    new IPropertyDescriptor[] {
      new TextPropertyDescriptor("propertyName", "Property Name"),
      new ComboBoxPropertyDescriptor("type", "Type", typeNames, false),
      new CheckboxPropertyDescriptor("value", "Value")};

  private IPropertyDescriptor[] otherTypeDescriptors =
    new IPropertyDescriptor[] {
      new TextPropertyDescriptor("propertyName", "Property Name"),
      new ComboBoxPropertyDescriptor("type", "Type", typeNames, false),
      new TextPropertyDescriptor("value", "Value")};

  static final private Map classToString;
  static final private Map stringToClass;

  static {

    classToString = new HashMap();
    classToString.put(String.class, "string");
    classToString.put(Boolean.class, "boolean");
    classToString.put(Integer.class, "int");
    classToString.put(Double.class, "double");
    classToString.put(Long.class, "long");

    stringToClass = new HashMap();
    stringToClass.put("string", String.class);
    stringToClass.put("boolean", Boolean.class);
    stringToClass.put("int", Integer.class);
    stringToClass.put("double", Double.class);
    stringToClass.put("long", Long.class);

  }

  String identifier;
  Object valueObject;
  Class type;
  PluginExtensionSpecification parent;

  /**
   * Constructor for PluginExtensionConfiguration.
   */
  public PluginExtensionConfiguration(String propertyName, Object value) {
    this.identifier = propertyName;
    this.valueObject = value;
    this.type = value.getClass();
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#setIdentifier(String)
   */
  public void setIdentifier(String propertyName) {
    this.identifier = propertyName;
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
   */
  public void setParent(Object parent) {
    this.parent = (PluginExtensionSpecification) parent;
  }

  /**
   * @see com.iw.plugins.spindle.spec.IIdentifiable#getParent()
   */
  public Object getParent() {
    return parent;
  }

  /**
  * Method writeConfiguration.
  * @param writer
  * @param i
  */
  public void write(PrintWriter writer, int indent) {

    Indenter.printIndented(writer, indent, "<configure property-name=\"");
    writer.print(identifier);
    writer.print("\" type=\"");
    writer.print(classToString.get(type));
    writer.println("\">");

    Indenter.printlnIndented(writer, indent + 1, valueObject.toString());

    Indenter.printlnIndented(writer, indent, "</configure>");

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
    if (type == Boolean.class) {

      return booleanTypeDescriptors;
    }

    return otherTypeDescriptors;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
   */
  public Object getPropertyValue(Object id) {

    if ("propertyName".equals(id)) {

      return identifier;

    }

    if ("type".equals(id)) {
    	
      Map lookup = stringToClass;
    	
      String foundType = (String) classToString.get(type);
      
      for (int i = 0; i < typeNames.length; i++) {
        
        if (foundType.equals(typeNames[i])) {
        	
        	return new Integer(i);
        }
      }

    }

    if ("value".equals(id)) {
    	
      if (Boolean.class == type) {
      	
      	return (Boolean)valueObject;
      	
      }

      return valueObject.toString();

    }
    return null;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
   */
  public boolean isPropertySet(Object id) {
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

    if ("propertyName".equals(id)) {
    	
    	String oldName = identifier;
    	String newName = ((String)value).trim();
    	
    	if ("".equals(newName) || newName.equals(oldName)) {
    		
    		return;
    		
    	}
    	
    	int i=1;
    	
    	while (parent.getConfiguration().containsKey(newName)) {
    		
    		newName = newName + i++;
    		
    	}
    	parent.removeConfiguration(oldName);
    	parent.addConfiguration(newName, value);  

    }

    if ("type".equals(id)) {
    	
    	String chosenType = typeNames[((Integer)value).intValue()];
    	
    	Class newClass = checkType(chosenType);
    	
    	this.valueObject = convertValue(newClass, valueObject);
    	
    	type = newClass;      

    }

    if ("value".equals(id)) {
    	
    	this.valueObject = convertValue(type, value);

    }
    
    parent.configurationChanged();

  }
  
  private Class checkType(String newType) {
  	
  	return (Class)stringToClass.get(newType);
  	
  }

  private Object convertValue(Class type, Object value) {

    if (type == value.getClass()) {

      return value;

    }

    if (type == String.class) {

      return value.toString();

    }

    if (type == Boolean.class) {

      return convertToBoolean(value);

    }

    if (type == Double.class) {

      return convertToDouble(value);

    }

    if (type == Long.class) {

      return convertToLong(value);

    }

    if (type == Integer.class) {

      return convertToInteger(value);

    }

    return value;

  }

  private Boolean convertToBoolean(Object value) {

    Class clazz = value.getClass();

    if (clazz == Boolean.class) {

      return (Boolean) value;

    }

    if (value instanceof Number) {

      if (((Number) value).longValue() == 0) {

        return Boolean.FALSE;

      } else {

        return Boolean.TRUE;
      }
    }

    if (clazz == String.class) {

      String svalue = (String) value;

      if (svalue.equalsIgnoreCase("true") || svalue.equalsIgnoreCase("yes")) {

        return Boolean.TRUE;
      }

      if (svalue.equalsIgnoreCase("false") || svalue.equalsIgnoreCase("no")) {

        return Boolean.FALSE;
      }

      return new Boolean(svalue != null && !"".equals(svalue));
    }

    return Boolean.TRUE;

  }

  private Long convertToLong(Object value) {

    Class clazz = value.getClass();

    if (clazz == Long.class) {

      return (Long) value;

    }

    if (clazz == Boolean.class) {

      boolean flag = ((Boolean) value).booleanValue();

      if (flag) {

        return trueL;

      }

      return falseL;

    }

    if (value instanceof Number) {

      return new Long(((Number) value).longValue());
    }

    if (clazz == String.class) {

      try {

        return new Long((String) value);

      } catch (NumberFormatException e) {
      }

    }

    return falseL;

  }

  private Double convertToDouble(Object value) {

    Class clazz = value.getClass();

    if (clazz == Double.class) {

      return (Double) value;

    }

    if (clazz == Boolean.class) {

      boolean flag = ((Boolean) value).booleanValue();

      if (flag) {

        return trueD;

      }

      return falseD;

    }

    if (value instanceof Number) {

      return new Double(((Number) value).doubleValue());
    }

    if (clazz == String.class) {

      try {

        return new Double((String) value);

      } catch (NumberFormatException e) {
      }

    }

    return falseD;

  }

  private Integer convertToInteger(Object value) {

    Class clazz = value.getClass();

    if (clazz == Integer.class) {

      return (Integer) value;

    }

    if (clazz == Boolean.class) {

      boolean flag = ((Boolean) value).booleanValue();

      if (flag) {

        return trueI;

      }

      return falseI;

    }

    if (value instanceof Number) {

      return new Integer(((Number) value).intValue());
    }

    if (clazz == String.class) {

      try {

        return new Integer((String) value);

      } catch (NumberFormatException e) {
      }

    }

    return falseI;

  }

}
