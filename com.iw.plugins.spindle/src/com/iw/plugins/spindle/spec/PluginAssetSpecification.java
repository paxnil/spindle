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

import net.sf.tapestry.spec.AssetSpecification;
import net.sf.tapestry.spec.AssetType;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.util.Indenter;

public class PluginAssetSpecification
  extends AssetSpecification
  implements IIdentifiable, IPropertySource {

  private String identifier;
  private PluginComponentSpecification parent;

  public PluginAssetSpecification(AssetType type, String path) {
    super(type, path);
  }

  public void setPath(String value) {
    path = value;
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
    this.parent = (PluginComponentSpecification) parent;
  }

  private IPropertyDescriptor[] privateDescriptors =
    {
      new TextPropertyDescriptor("name", "Name"),
      new TextPropertyDescriptor("path", "Resource Path"),
      };
  private IPropertyDescriptor[] externalDescriptors =
    { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("path", "URL"), };
  private IPropertyDescriptor[] contextDescriptors =
    { new TextPropertyDescriptor("name", "Name"), new TextPropertyDescriptor("path", "Path"), };

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
    AssetType type = getType();
    if (type == AssetType.CONTEXT) {
      return contextDescriptors;
    }
    if (type == AssetType.EXTERNAL) {
      return externalDescriptors;
    }
    if (type == AssetType.PRIVATE) {
      return privateDescriptors;
    }
    return new IPropertyDescriptor[0];
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
   */
  public Object getPropertyValue(Object key) {
    if ("name".equals(key)) {
      return identifier;
    } else if ("path".equals(key)) {
      return getPath();
    }
    return null;
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
   */
  public boolean isPropertySet(Object key) {
    if ("name".equals(key)) {
      return identifier != null;
    } else if ("path".equals(key)) {
      return getPath() != null;
    } else {
      return true;
    }
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(Object)
   */
  public void resetPropertyValue(Object id) {
  }

  /**
   * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(Object, Object)
   */
  public void setPropertyValue(Object key, Object value) {

    PluginComponentSpecification componentSpec = (PluginComponentSpecification) parent;
    
    if ("name".equals(key)) {
    	
      String oldName = this.identifier;
      String newName = (String) value;
      
      if ("".equals(newName.trim())) {
      	
        newName = oldName;
        
      } else if (componentSpec.getAsset(newName) != null) {
      	
        newName = "Copy of " + newName;
        
      }
      
      this.identifier = newName;
      componentSpec.removeAsset(oldName);
      componentSpec.setAsset(this.identifier, this);
      
    } else if ("path".equals(key)) {
    	
      setPath((String) value);
      componentSpec.setAsset(this.identifier, this);
    }
  }
  public void write(String name, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<");
    AssetType type = getType();
    if (type.equals(AssetType.PRIVATE)) {
      writer.print("private-asset name=\"" + name);
      writer.print("\" resource-path=\"" + getPath());
      writer.println("\"/>");
      return;
    } else if (type.equals(AssetType.EXTERNAL)) {
      writer.print("external-asset name=\"" + name);
      writer.print("\" URL=\"" + getPath());
      writer.println("\"/>");
      return;
    } else if (type.equals(AssetType.CONTEXT)) {
      writer.print("context-asset name=\"" + name);
      writer.print("\" path=\"" + getPath());
      writer.println("\"/>");
      return;
    }

  }

}
