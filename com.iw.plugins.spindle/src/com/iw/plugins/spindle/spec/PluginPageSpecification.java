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

import net.sf.tapestry.spec.PageSpecification;
import org.eclipse.core.internal.plugins.IModel;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.ui.descriptors.ComponentTypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.INeedsModelInitialization;
import com.iw.plugins.spindle.util.Indenter;

public class PluginPageSpecification
  extends PageSpecification
  implements IIdentifiable, IPropertySource {
  	
  private PropertyChangeSupport propertySupport;

  private String identifier;
  private PluginApplicationSpecification parent;


  /**
   * Constructor for PluginPageSpecification
   */
  public PluginPageSpecification(String specificationPath) {
    super(specificationPath);
    propertySupport = new PropertyChangeSupport(this);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  //e.g.  <page name="Register" specification-path="/com/primix/vlib/pages/Register.jwc"/>
  public void write(String name, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<page name=\"" + name);
    writer.print("\" specification-path=\"");
    writer.print(getSpecificationPath());
    writer.println("\"/>");
  }

  /**
   * Returns the parent.
   * @return PluginApplicationSpecification
   */
  public Object getParent() {
    return parent;
  }

  /**
   * Sets the parent.
   * @param parent The parent to set
   */
  public void setParent(Object parent) {
    this.parent = (PluginApplicationSpecification) parent;
  }

  /**
   * Returns the identifier.
   * @return String
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Sets the identifier.
   * @param identifier The identifier to set
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  private IPropertyDescriptor[] descriptors =
    {
      new TextPropertyDescriptor("name", "Name"),
      new ComponentTypeDialogPropertyDescriptor("spec", "Spec", null, null)};

  public void resetPropertyValue(Object key) {
  }

  public IPropertySource getPropertySource(Object key) {
    return this;
  }

  public void setPropertyValue(Object key, Object value) {
    if ("name".equals(key)) {

      String oldName = this.identifier;
      String specPath = parent.getPageSpecification(oldName).getSpecificationPath();
      String newName = (String) value;

      if ("".equals(newName.trim())) {

        newName = oldName;

      } else if (parent.getPageSpecification(newName) != null) {

        newName = "Copy of " + newName;
        parent.setPageSpecification(newName, new PluginPageSpecification(specPath));
        return;
      }
      this.identifier = newName;
      parent.removePageSpecification(oldName);
      parent.setPageSpecification(this.identifier, this);

    } else if ("spec".equals(key)) {

      setSpecificationPath((String) value);
    }
  }

  public boolean isPropertySet(Object key) {
    if ("name".equals(key)) {

      return identifier != null;

    } else if ("spec".equals(key)) {

      return getSpecificationPath() != null;

    }
    return true;

  }

  public Object getPropertyValue(Object key) {
    if ("name".equals(key)) {
    	
      return identifier;
      
    } else if ("spec".equals(key)) {
    	
      return getSpecificationPath();
    }
    return null;
  }

  public IPropertyDescriptor[] getPropertyDescriptors() {

    return descriptors;
  }

  public Object getEditableValue() {
    return identifier;
  }

  

  /**
   * @see net.sf.tapestry.spec.PageSpecification#setSpecificationPath(String)
   */
  public void setSpecificationPath(String value) {
    super.setSpecificationPath(value);
    propertySupport.firePropertyChange("pageMap", null, value);
  }

}
