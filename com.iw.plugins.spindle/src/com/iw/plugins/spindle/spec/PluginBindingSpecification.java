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

import net.sf.tapestry.spec.BindingSpecification;
import net.sf.tapestry.spec.BindingType;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.ui.descriptors.FieldPropertyDescriptor;
import com.iw.plugins.spindle.util.Indenter;

public class PluginBindingSpecification
  extends BindingSpecification
  implements IIdentifiable, IPropertySource {

  private String identifier;
  private IBindingHolder parent;

  /**
   * Constructor for PluginBindingSpecification
   */
  public PluginBindingSpecification(BindingType type, String value) {
    super(type, value);
  }

  /**
   * @see com.iw.plugins.spindle.spec.IPluginChildSpecification#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * @see com.iw.plugins.spindle.spec.IPluginChildSpecification#getParent()
   */
  public Object getParent() {
    return parent;
  }

  public int getParentDTDVersion() {

    return ((PluginContainedComponent) parent).getDTDVersion();

  }

  /**
   * @see com.iw.plugins.spindle.spec.IPluginChildSpecification#setIdentifier()
   */
  public void setIdentifier(String id) {

    identifier = id;

  }

  /**
   * @see com.iw.plugins.spindle.spec.IPluginChildSpecification#setParent(Object)
   */
  public void setParent(Object parent) {
    this.parent = (IBindingHolder) parent;
  }

  public void write(String name, PrintWriter writer, int indent, String publicId) {

    int currentDTD = XMLUtil.getDTDVersion(publicId);
    boolean isDTD12OrBetter = currentDTD >= XMLUtil.DTD_1_2;
    boolean isDTD13OrBetter = currentDTD >= XMLUtil.DTD_1_3;

    Indenter.printIndented(writer, indent, "<");

    BindingType type = getType();

    if (type.equals(BindingType.FIELD)) {

      writer.print("field-binding name=\"" + name);
      writer.print("\" field-name=\"");

    } else if (type.equals(BindingType.INHERITED)) {

      writer.print("inherited-binding name=\"" + name);
      writer.print("\" parameter-name=\"");

    } else if (type.equals(BindingType.STATIC)) {

      writer.print("static-binding name=\"" + name);
      writer.print("\">");
      writer.print(getValue());
      writer.println("</static-binding>");
      return;

    } else if (type.equals(BindingType.DYNAMIC)) {

      writer.print("binding name=\"" + name);

      if (isDTD13OrBetter) {

        writer.print("\" expression='");
        writer.print(getValue());
        writer.println("'/>");
        return;

      } else {

        writer.print("\" property-path=\"");

      }

    } else if (isDTD12OrBetter && type.equals(BindingType.STRING)) {

      writer.print("string-binding name=\"" + name);
      writer.print("\" key=\"");

    }

    writer.print(getValue());
    writer.println("\"/>");
  }

  private IPropertyDescriptor[] staticDescriptors =
    { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("value", "Value")};

  private IPropertyDescriptor[] inheritDescriptors =
    {
      new TextPropertyDescriptor("name", "Name"),
      new TextPropertyDescriptor("value", "Parameter Name")};

  private IPropertyDescriptor[] fieldDescriptors =
    { new FieldPropertyDescriptor("value", "Field Name")};

  private IPropertyDescriptor[] stringDescriptiors =
    { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("value", "Key")};

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
  	
    BindingType type = getType();

    if (type == BindingType.INHERITED) {
    	
      return inheritDescriptors;
      
    }
    if (type == BindingType.STATIC) {
    	
      return staticDescriptors;
      
    }
    if (type == BindingType.DYNAMIC) {

      if (getParentDTDVersion() < XMLUtil.DTD_1_3) {

        return new IPropertyDescriptor[] {
          new TextPropertyDescriptor("name", "Name"),
          new TextPropertyDescriptor("value", "Property Path")};

      } else {

        return new IPropertyDescriptor[] {
          new TextPropertyDescriptor("name", "Name"),
          new TextPropertyDescriptor("value", "Expression")};

      }
    }

    if (type == BindingType.FIELD) {
    	
      return fieldDescriptors;
      
    }

    if (type == BindingType.STRING) {
    	
      return stringDescriptiors;
      
    }
    return null;
  }
  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
   */
  public Object getPropertyValue(Object key) {

    if ("name".equals(key)) {

      return identifier;

    } else if ("value".equals(key)) {

      return getValue();
    }

    return "ignore this";
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
   */
  public boolean isPropertySet(Object key) {
    if ("name".equals(key)) {

      return identifier != null;

    } else if ("value".equals(key)) {

      return getValue() != null;

    } else {

      return true;
    }
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(Object)
   */
  public void resetPropertyValue(Object key) {
    if ("path".equals(key)) {

      setValue("fill in value");
    }
  }
  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(Object, Object)
   */
  public void setPropertyValue(Object key, Object value) {
    if ("name".equals(key)) {

      String oldName = identifier;
      String newName = (String) value;

      if ("".equals(newName.trim())) {

        newName = oldName;

      } else if (parent.getBinding(newName) != null) {

        newName = "Copy of " + newName;

      }

      identifier = newName;
      parent.removeBinding(oldName);
      parent.setBinding(identifier, this);

    } else if ("value".equals(key)) {

      setValue((String) value);
      parent.setBinding(identifier, this);
    }
  }

  /**
   * Method deepCopy.
   * @return PluginBindingSpecification
   */
  public PluginBindingSpecification deepCopy() {
    return new PluginBindingSpecification(getType(), getValue());
  }

}
