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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.sf.tapestry.spec.ApplicationSpecification;
import net.sf.tapestry.spec.ExtensionSpecification;
import net.sf.tapestry.spec.IApplicationSpecification;
import net.sf.tapestry.util.IPropertyHolder;

import com.iw.plugins.spindle.model.TapestryApplicationModel;

public class PluginApplicationSpecification
  extends ApplicationSpecification
  implements PropertyChangeListener, IPluginLibrarySpecification, IApplicationSpecification {

  private PropertyChangeSupport propertySupport;
  private LibraryApplicationHelper helper;

  private String identifier;
  private TapestryApplicationModel parent;

  public PluginApplicationSpecification() {
    propertySupport = new PropertyChangeSupport(this);
    helper = new LibraryApplicationHelper(this, propertySupport);
    setServices(new HashMap(7));
    setComponents(new HashMap(7));
    setExtensions(new HashMap(7));
    setLibraries(new HashMap(7));
    setPages(new HashMap(7));
  }

  public String getIdentifier() {
    return identifier;
  }

  public Object getParent() {
    return parent;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setParent(Object parent) {
    this.parent = (TapestryApplicationModel) parent;
  }

  public void setProperty(String name, String value) {

    super.setProperty(name, value);
    propertySupport.firePropertyChange("properties", name, value);

  }

  public void removeProperty(String name) {
    String old = getProperty(name);

    if (old == null) {

      super.removeProperty(name);
      propertySupport.firePropertyChange("properties", old, null);

    }
  }

  public void setPublicId(String id) {
    super.setPublicId(id);
    propertySupport.firePropertyChange("dtd", null, id);
  }

  public void propertyChange(PropertyChangeEvent event) {
    propertySupport.firePropertyChange(event);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  public void setName(String name) {
    String old = super.getName();
    super.setName(name);
    propertySupport.firePropertyChange("name", old, name);
  }

  public void setEngineClassName(String name) {

    super.setEngineClassName(name);

    propertySupport.firePropertyChange("engineClassName", null, name);
  }

  public void write(PrintWriter writer) {

    int indent = 0;

    XMLUtil.writeXMLHeader(getPublicId(), "application", writer);

    writer.println();

    writer.print("<application name=\"");
    writer.print(getName());
    writer.print("\" engine-class=\"");
    writer.print(getEngineClassName());
    writer.println("\" >");

    XMLUtil.writeDescription(writer, indent + 1, getDescription());

    XMLUtil.writeProperties((IPropertyHolder) this, writer, indent + 1);

    XMLUtil.writeLibraryPages(getPages(), writer, indent + 1);

    XMLUtil.writeLibraryComponents(getComponents(), writer, indent + 1);

    XMLUtil.writeLibraryServices(getServices(), writer, indent + 1);

    XMLUtil.writeLibraries(getLibraries(), writer, indent + 1);

    XMLUtil.writeExtensions(getExtensions(), writer, indent + 1);

    writer.println();

    writer.println("</application>");
  }

  public Set getAllExtensionNames() {
    return helper.getAllExtensionNames(getExtensions());
  }

  public void setServiceClassName(String name, String classname) {
    helper.setServiceClassName(getServices(), name, classname);
  }

  public void removeService(String name) {
    helper.removeService(getServices(), name);
  }

  public boolean canRevertService(String name) {
    return helper.canRevertService(getServices(), name);
  }

  private List getDefaultServiceNames() {
    return helper.getDefaultServiceNames();
  }

  public boolean isDefaultService(String name) {
    return helper.isDefaultService(name);
  }

  public boolean canDeleteService(String name) {
    return helper.canDeleteService(getServices(), name);
  }

  public void setComponentSpecificationPath(String alias, String path) {
    helper.setComponentSpecificationPath(getComponents(), alias, path);
  }

  public void removeComponentSpecificationPath(String alias) {
    helper.removeComponentSpecificationPath(getComponents(), alias);
  }

  public Collection getNonDefaultPageNames() {
    return helper.getNonDefaultPageNames(getPages());
  }

  public void setPageSpecificationPath(String name, String path) {
    helper.setPageSpecificationPath(getPages(), name, path);
  }

  public void removePageSpecificationPath(String name) {
    helper.removePageSpecificationPath(getPages(), name);
  }

  public String getPageName(String path) {
    return helper.getPageName(getPages(), path);
  }

  public String findAliasFor(String componentSpecLocation) {
    return helper.findAliasFor(getComponents(), componentSpecLocation);
  }

  public void removeExtensionSpecification(String name) {
    helper.removeExtensionSpecification(getExtensions(), name);
  }

  public void setLibrarySpecificationPath(String name, String specificationPath) {
    helper.setLibrarySpecificationPath(getLibraries(), name, specificationPath);
  }

  public void removeLibrarySpecificationPath(String name) {
    helper.removeLibrarySpecificationPath(getLibraries(), name);
  }

  public void setExtensionSpecification(String name, ExtensionSpecification extension) {
    helper.setExtensionSpecification(getExtensions(), name, extension);
  }

  public void addExtensionSpecification(String name, ExtensionSpecification extension) {
    helper.addExtensionSpecification(getExtensions(), name, extension);
  }

}
