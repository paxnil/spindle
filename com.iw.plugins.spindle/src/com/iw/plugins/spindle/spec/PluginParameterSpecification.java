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
import java.io.StringWriter;
import java.util.ArrayList;

import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.Direction;
import net.sf.tapestry.spec.ParameterSpecification;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.ui.CheckboxPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.ComboBoxPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.DocumentationPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.TypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.Indenter;
import com.iw.plugins.spindle.util.SourceWriter;

public class PluginParameterSpecification
  extends ParameterSpecification
  implements IIdentifiable, IPropertySource {

  private IParameterHolder parent;
  private String identifier;

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
    this.parent = (IParameterHolder) parent;
  }

  public void getHelpText(String name, StringBuffer buffer) {
    buffer.append(getHelpText(name));
  }

  public String getHelpText(String name) {
    StringWriter swriter = new StringWriter();
    SourceWriter writer = new SourceWriter(swriter);
    write(name, writer, 0, ((ComponentSpecification)parent).getPublicId());
    return swriter.toString();
  }

  // e.g. -- <parameter name="book" java-type="com.primix.vlib.ejb.Book" required="yes" parameterName="poo" direction="in"/>
  public void write(String name, PrintWriter writer, int indent, String publicId) {
  	
    boolean isDTD12OrBetter = XMLUtil.getDTDVersion(publicId) >= XMLUtil.DTD_1_2;
  	
    Indenter.printIndented(writer, indent, "<parameter name=\"" + name);
    writer.print("\" java-type=\"" + getType());
    writer.print("\" required=\"" + (isRequired() ? "yes" : "no"));
    
    if (isDTD12OrBetter) {
    	
      String propertyName = getPropertyName();
      
      if (propertyName != null && !"".equals(propertyName) && !propertyName.equals(name)) {
      	
        writer.print("\" property-name=\"" + getPropertyName());
      }
      if (getDirection() != Direction.CUSTOM) {
      	
        writer.print("\" direction=\"");
        writer.print(getDirection() == Direction.CUSTOM ? "custom" : "in");
        
      }
    }
    String description = getDescription();
    
    if (description == null || "".equals(description.trim())) {
    	
      writer.println("\"/>");
      
    } else {
    	
      writer.println("\">");
      XMLUtil.writeDescription(writer, indent + 1, description, false);
      Indenter.printlnIndented(writer, indent, "</parameter>");
    }
  }

  private String[] directionLabels = { "custom", "in" };
  private Direction[] directions = { Direction.CUSTOM, Direction.IN };

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
   */
  public Object getEditableValue() {
    return identifier;
  }

  public void resetPropertyValue(Object key) {
  }

  public IPropertySource getPropertySource(Object key) {
    return this;
  }

  public void setPropertyValue(Object key, Object value) {
    IParameterHolder componentSpec = parent;

    if ("name".equals(key)) {

      String oldName = this.identifier;
      String newName = (String) value;

      if ("".equals(newName.trim())) {

        return;

      } else if (parent.getParameter(newName) != null) {

        newName = "Copy of " + newName;
      }

      this.identifier = newName;
      componentSpec.removeParameter(oldName);
      componentSpec.setParameter(this.identifier, this);

    } else if ("propertyName".equals(key)) {

      setPropertyName((String) value);

    } else if ("type".equals(key)) {

      setType((String) value);

    } else if ("required".equals(key)) {

      setRequired(((Boolean) value).booleanValue());

    } else if ("direction".equals(key)) {

      String newDirection = directionLabels[((Integer) value).intValue()];

      if ("in".equals(newDirection)) {

        setDirection(Direction.IN);

      } else {

        setDirection(Direction.CUSTOM);
      }

    } else if ("description".equals(key)) {

      String newValue = (String) key;

      if (!(newValue.trim().equals(getDescription()))) {

        setDescription((String) value);

      } else {

        return;
      }
    }
  }

  public boolean isPropertySet(Object key) {
    if ("name".equals(key)) {
      return this.identifier != null;
    } else if ("type".equals(key)) {
      return getType() != null;
    } else {
      return true;
    }
  }

  public Object getPropertyValue(Object key) {
    if ("name".equals(key)) {
    	
      return this.identifier;
      
    } else if ("propertyName".equals(key)) {
    	
      return getPropertyName();
      
    } else if ("type".equals(key)) {
    	
      return getType();
      
    } else if ("required".equals(key)) {
    	
      if (isRequired()) {
      	
        return Boolean.TRUE;
        
      } else {
      	
        return Boolean.FALSE;
      }
      
    } else if ("direction".equals(key)) {
    	
      Direction currentDir = getDirection();
      
      for (int i = 0; i < directions.length; i++) {
      	
        if (currentDir == directions[i]) {
        	
          return new Integer(i);
        }
      }
      
      return new Integer(0);
      
    } else if ("description".equals(key)) {
    	
      return getDescription();
      
    }
    return null;
  }

  public IPropertyDescriptor[] getPropertyDescriptors() {
    ArrayList list = new ArrayList();
    list.add(new TextPropertyDescriptor("name", "Name"));
    list.add(new TypeDialogPropertyDescriptor("type", "Type"));
    list.add(new CheckboxPropertyDescriptor("required", "Required"));
    if (true) {
      list.add(new ComboBoxPropertyDescriptor("direction", "Direction", directionLabels, false));
      list.add(new TextPropertyDescriptor("propertyName", "Property Name"));
    }
    list.add(
      new DocumentationPropertyDescriptor(
        "description",
        "Description",
        "Document parameter: " + this.identifier,
        null));

    return (IPropertyDescriptor[]) list.toArray(new IPropertyDescriptor[list.size()]);
  }

}