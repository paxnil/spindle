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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import net.sf.tapestry.spec.BindingSpecification;
import net.sf.tapestry.spec.ContainedComponent;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.ui.descriptors.ComponentTypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.Indenter;

public class PluginContainedComponent
  extends ContainedComponent
  implements IBindingHolder, IIdentifiable, IPropertySource {

  private PropertyChangeSupport propertySupport;

  private String identifier;
  private PluginComponentSpecification parent;

  public PluginContainedComponent() {
    propertySupport = new PropertyChangeSupport(this);
  }
  
   /**
   * Method getDTDVersion.
   * @return String
   */
  public int getDTDVersion() {
    return XMLUtil.getDTDVersion(parent.getPublicId());
  }

  public void setBinding(String name, BindingSpecification spec) {
    if (bindings == null) {
      bindings = new HashMap(7);
    }
    bindings.put(name, spec);

    PluginBindingSpecification pluginSpec = (PluginBindingSpecification) spec;
    pluginSpec.setIdentifier(name);
    pluginSpec.setParent(this);

    propertySupport.firePropertyChange("bindings", null, bindings);
  }

  public void removeBinding(String name) {
    if (bindings != null) {
      bindings.remove(name);
      propertySupport.firePropertyChange("bindings", null, bindings);
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  public void write(String name, PrintWriter writer, int indent, String publicId) {

    Indenter.printIndented(writer, indent, "<component id=\"" + name);
    
    if (getCopyOf() != null) {
    	
      writer.print("\" copy-of=\"" + getCopyOf());
      writer.println("\"/>");
      return;
      
    } else {
    	
      writer.print("\" type=\"" + getType());
      writer.print("\"");
      
      Collection bns = getBindingNames();
      
      if (bns != null) {
      	
        if (bns.isEmpty()) {
        	
          writer.println("/>");
          return;
          
        } else {
        	
          writer.println(">");
          
          Iterator bindingNames = new TreeSet(bns).iterator();
          
          while (bindingNames.hasNext()) {
          	
            String bindingName = (String) bindingNames.next();
            
            PluginBindingSpecification binding =
              (PluginBindingSpecification) getBinding(bindingName);
              
            binding.write(bindingName, writer, indent + 1, publicId);
          }
        }
      }
      Indenter.printlnIndented(writer, indent, "</component>");
    }
  }

  /**
   * Returns the identifier.
   * @return String
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Returns the parent.
   * @return PluginComponentSpecification
   */
  public Object getParent() {
    return parent;
  }

  /**
   * Sets the identifier.
   * @param identifier The identifier to set
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * Sets the parent.
   * @param parent The parent to set
   */
  public void setParent(Object parent) {
    this.parent = (PluginComponentSpecification) parent;
  }

  public void resetPropertyValue(Object key) {
  }

  public void setPropertyValue(Object key, Object value) {

    PluginComponentSpecification parentComponent = (PluginComponentSpecification) parent;
    if ("id".equals(key)) {
      String oldId = this.identifier;
      String newId = (String) value;
      if ("".equals(newId.trim())) {

        newId = oldId;

      } else {

        Object componentObject = parentComponent.getComponent(newId);

        if (componentObject != null && componentObject.getClass() != ContainedComponent.class) {

          newId = newId + "Copy";
          PluginContainedComponent copy = copy();
          parentComponent.addComponent(newId, copy);
          return;

        }
      }
      this.identifier = newId;
      parentComponent.removeComponent(oldId);

    } else if ("type".equals(key)) {

      setType((String) value);
    } else if ("copy-of".equals(key)) {

      if (!"".equals(((String) value).trim())) {

        setCopyOf((String) value);
      }
    }

    parentComponent.setComponent(this.identifier, this);
  }

  private PluginContainedComponent copy() {
    PluginContainedComponent result = new PluginContainedComponent();

    if (getCopyOf() != null) {

      result.setCopyOf(getCopyOf());

    } else {

      result.setType(getType());

    }
    return result;
  }

  public boolean isPropertySet(Object key) {
    if ("id".equals(key)) {

      return true;

    } else if ("type".equals(key)) {

      return getType() != null;

    } else if ("copy-of".equals(key)) {

      return getCopyOf() != null;

    }

    return true;

  }

  public Object getPropertyValue(Object key) {
    if ("id".equals(key)) {

      return identifier;

    } else if ("type".equals(key)) {

      return getType();

    } else if ("copy-of".equals(key)) {

      return getCopyOf();

    }
    return null;
  }

  private IPropertyDescriptor[] normal =
    new IPropertyDescriptor[] {
      new TextPropertyDescriptor("id", "ID"),
      new ComponentTypeDialogPropertyDescriptor(
        "type",
        "Type",
        "Choose a Tapestry Component",
        "For now, to use aliases you can't use this dialog, exit then type the alias you wish")};

  private IPropertyDescriptor[] copyof =
    new IPropertyDescriptor[] {
      new TextPropertyDescriptor("id", "ID"),
      new TextPropertyDescriptor("copy-of", "Copy Of"),
      };

  public IPropertyDescriptor[] getPropertyDescriptors() {
    if (getCopyOf() == null) {

      return normal;
    } else {
      return copyof;
    }
  }

  public Object getEditableValue() {
    return identifier;
  }

  /**
   * Method deepCopy.
   * @return PluginContainedComponent
   */
  public PluginContainedComponent deepCopy() {
    PluginContainedComponent result = new PluginContainedComponent();

    result.setIdentifier(this.identifier);
    if (getCopyOf() != null) {

      result.setCopyOf(getCopyOf());

    } else {

      result.setType(getType());
      for (Iterator iter = getBindingNames().iterator(); iter.hasNext();) {

        PluginBindingSpecification binding = (PluginBindingSpecification) iter.next();
        result.setBinding(binding.getIdentifier(), binding.deepCopy());
      }
    }

    return result;
  }

 

}
