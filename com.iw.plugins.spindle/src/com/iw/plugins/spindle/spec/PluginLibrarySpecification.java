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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.spec.ExtensionSpecification;
import net.sf.tapestry.spec.ILibrarySpecification;
import net.sf.tapestry.spec.LibrarySpecification;
import net.sf.tapestry.util.IPropertyHolder;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryModelManager;
import com.iw.plugins.spindle.util.Indenter;

public class PluginLibrarySpecification
  extends LibrarySpecification
  implements IPluginLibrarySpecification, PropertyChangeListener {

  private PropertyChangeSupport propertySupport;

  private String identifier;
  private TapestryLibraryModel parent;

  public PluginLibrarySpecification() {
    propertySupport = new PropertyChangeSupport(this);
  }

  public Set getPageNamesSorted() {
    return new TreeSet(getPages().keySet());
  }

  public Set getComponentMapAliases() {
    Map components = getComponents();
    if (components == null) {
      return new HashSet();
    }
    return components.keySet();
  }


  public void setServiceClassName(String name, String classname) {
    Map services = getServices();
    if (services == null) {
      super.setServiceClassName(name, classname);
    } else {
      services.put(name, classname);
    }
    propertySupport.firePropertyChange("services", null, null);
  }

  public void removeService(String name) {
    Map services = getServices();
    if (services != null) {
      services.remove(name);
      propertySupport.firePropertyChange("services", null, null);
    }
  }

  public boolean canRevertService(String name) {
    Map services = getServices();
    String useName = name.toLowerCase();
    if (services != null
      && services.containsKey(useName)
      && getDefaultServiceNames().contains(useName)) {
      return true;
    }
    return false;
  }

  /**
   * Method getDefaultServiceMap.
   */
  private List getDefaultServiceNames() {

    TapestryLibraryModel defaultLib = TapestryModelManager.getDefaultLibrary();

    if (defaultLib != null) {

      return defaultLib.getSpecification().getServiceNames();
    }
    
    return null;
  }

  public boolean isDefaultService(String name) {
    return getDefaultServiceNames().contains(name.toLowerCase());
  }

  public boolean canDeleteService(String name) {
    String useName = name.toLowerCase();
    if (getDefaultServiceNames().contains(useName)) {
      return false;
    }
    Map services = getServices();
    return services != null && services.containsKey(useName);
  }

  public void setProperty(String name, String value) {
    String old = super.getProperty(name);
    super.setProperty(name, value);
    propertySupport.firePropertyChange("properties", name, value);
  }

  public void removeProperty(String name) {
    String old = super.getProperty(name);
    super.removeProperty(name);
    propertySupport.firePropertyChange("properties", old, null);
  }

  public void setComponentSpecificationPath(String alias, String resourceName) {

    super.setComponentSpecificationPath(alias, resourceName);
    propertySupport.firePropertyChange("componentMap", null, getComponents());
  }

  public void removeComponentSpecificationPath(String alias) {
    Map components = getComponents();

    if (components.containsKey(alias)) {

      components.remove(alias);
      propertySupport.firePropertyChange("componentMap", null, components);
    }
  }

  public Collection getNonDefaultPageNames() {
    Map pages = getPages();
    if (pages == null) {
      return new HashSet();
    }
    return pages.keySet();
  }

  public void setPageSpecificationPath(String name, String spec) {

    super.setPageSpecificationPath(name, spec);
    propertySupport.firePropertyChange("pageMap", null, getPages());
  }

  public void removePageSpecificationPath(String name) {
    Map pages = getPages();
    if (pages.containsKey(name)) {
      pages.remove(name);
      propertySupport.firePropertyChange("pageMap", null, pages);
    }
  }

  public String getPageName(String componentSpecLocation) {
    Map pages = getPages();
    String useName = componentSpecLocation;
    //if (useName.indexOf("/") >= 0) {
    // useName = useName.substring(1).replace('/', '.');
    //}
    String pageName = null;
    if (pages != null) {
      pageName = findKeyInMap(useName, pages);
    }
    if (pageName == null) {
      pageName = findKeyInDefaultPageMap(useName);
    }
    return pageName;
  }

  private String findKeyInDefaultPageMap(String value) {
    ILibrarySpecification defaultLib =
      TapestryModelManager.getDefaultLibrary().getSpecification();

    if (defaultLib != null) {

      List defaultPageNames = defaultLib.getPageNames();

      for (Iterator iter = defaultPageNames.iterator(); iter.hasNext();) {

        String defaultName = (String) iter.next();
        String defaultValue = (String) defaultLib.getPageSpecificationPath(defaultName);

        if (value.equals(defaultValue)) {

          return defaultName;
        }
      }

    }

    return null;
  }

  public String findAliasFor(String componentSpecLocation) {
    String result = null;
    Map components = getComponents();
    if (components != null) {

      result = findKeyInMap(componentSpecLocation, components);

    }
    if (result == null) {

      result = findKeyInDefaultComponentMap(componentSpecLocation);
    }
    return result;
  }
  
  private String findKeyInDefaultComponentMap(String value) {
    ILibrarySpecification defaultLib =
      TapestryModelManager.getDefaultLibrary().getSpecification();

    if (defaultLib != null) {

      List defaultPageNames = defaultLib.getComponentAliases();

      for (Iterator iter = defaultPageNames.iterator(); iter.hasNext();) {

        String defaultName = (String) iter.next();
        String defaultValue = (String) defaultLib.getComponentSpecificationPath(defaultName);

        if (value.equals(defaultValue)) {

          return defaultName;
        }
      }

    }

    return null;
  }


  private String findKeyInMap(String componentLocation, Map map) {
    Iterator i = map.entrySet().iterator();

    while (i.hasNext()) {

      Map.Entry entry = (Map.Entry) i.next();

      if (entry.getValue().equals(componentLocation)) {

        return (String) entry.getKey();
      }
    }
    return null;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  public void write(PrintWriter writer) {
    int indent = 0;

    XMLUtil.writeXMLHeader(getPublicId(), "library-specification", writer);

    writer.println();
    
    writer.println("<library-specification>");

    XMLUtil.writeDescription(writer, indent + 1, getDescription());

    XMLUtil.writeProperties((IPropertyHolder) this, writer, indent + 1);

    XMLUtil.writeLibraryPages(getPages(), writer, indent + 1);

    XMLUtil.writeLibraryComponents(getComponents(), writer, indent + 1);

    XMLUtil.writeLibraryServices(getServices(), writer, indent + 1);

    XMLUtil.writeLibraries(getLibraries(), writer, indent + 1);

    writer.println();
    writer.println("</library-specification>");
  }

  /**
   * @see net.sf.tapestry.spec.ApplicationSpecification#setDTDVersion(String)
   */
  public void setDTDVersion(String dtdVersion) {
    super.setDTDVersion(dtdVersion);
    propertySupport.firePropertyChange("dtd", null, dtdVersion);
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
   * @return TapestryApplicationModel
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
    this.parent = (TapestryLibraryModel) parent;
  }

  public void propertyChange(PropertyChangeEvent event) { 
    propertySupport.firePropertyChange(event);
  }

  /**
   * @see com.iw.plugins.spindle.spec.IApplicationOrLibrary#removeExtensionSpecification(String)
   */
  public void removeExtensionSpecification(String name) {
  }

  /**
   * @see com.iw.plugins.spindle.spec.IApplicationOrLibrary#removeLibrarySpecificationPath(String)
   */
  public void removeLibrarySpecificationPath(String name) {
  }

  /**
   * @see com.iw.plugins.spindle.spec.IApplicationOrLibrary#setExtensionSpecification(String, ExtensionSpecification)
   */
  public void setExtensionSpecification(String name, ExtensionSpecification extension) {
  }

}
