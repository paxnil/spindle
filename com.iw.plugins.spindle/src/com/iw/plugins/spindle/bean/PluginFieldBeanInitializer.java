/**
 * Created on Jun 12, 2002
 *
 * Copyright 2002, Intelligent Works Incorporated.
 * All rights reserved.
 */
package com.iw.plugins.spindle.bean;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.primix.tapestry.bean.FieldBeanInitializer;

import com.iw.plugins.spindle.spec.IPropertyChangeProvider;

/**
 * @author GWL
 * @version $Id$
 */
public class PluginFieldBeanInitializer extends FieldBeanInitializer implements IPropertyChangeProvider {
	
   PropertyChangeSupport propertySupport;
      

  /**
   * Constructor for PluginFieldBeanInitializer.
   * @param propertyName
   * @param fieldName
   */
  public PluginFieldBeanInitializer(String propertyName, String fieldName) {
    super(propertyName, fieldName);
    propertySupport = new PropertyChangeSupport(this);
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

 
}
