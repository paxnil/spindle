package com.iw.plugins.spindle.spec;

import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.tapestry.spec.ExtensionSpecification;
import net.sf.tapestry.spec.ILibrarySpecification;

import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class LibraryApplicationHelper {

  private IPluginLibrarySpecification parent;
  private PropertyChangeSupport propertySupport;

  /**
   * Constructor for LibraryApplicationHelper.
   */
  public LibraryApplicationHelper(
    IPluginLibrarySpecification parent,
    PropertyChangeSupport propertySupport) {
    this.parent = parent;
    this.propertySupport = propertySupport;
  }

  public void setServiceClassName(Map services, String name, String classname) {
    if (services != null) {

      services.put(name, classname);
      propertySupport.firePropertyChange("services", null, null);

    }
  }

  public void removeService(Map services, String name) {
    if (services != null) {

      services.remove(name);
      propertySupport.firePropertyChange("services", null, null);

    }
  }

  public boolean canRevertService(Map services, String name) {
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
  public List getDefaultServiceNames() {

    TapestryLibraryModel defaultLib = TapestryProjectModelManager.getDefaultLibraryModel();

    if (defaultLib != null) {

      return defaultLib.getSpecification().getServiceNames();
    }

    return null;
  }

  public boolean isDefaultService(String name) {

    return getDefaultServiceNames().contains(name.toLowerCase());

  }

  public boolean canDeleteService(Map services, String name) {

    String useName = name.toLowerCase();

    if (getDefaultServiceNames().contains(useName)) {

      return false;

    }
    return services != null && services.containsKey(useName);
  }

  public void setComponentSpecificationPath(Map components, String alias, String resourceName) {

    if (components != null) {

      components.put(alias, resourceName);
      propertySupport.firePropertyChange("componentMap", null, components);
    }

  }

  public void removeComponentSpecificationPath(Map components, String alias) {

    if (components.containsKey(alias)) {

      components.remove(alias);
      propertySupport.firePropertyChange("componentMap", null, components);
    }
  }

  public Collection getNonDefaultPageNames(Map pages) {
    if (pages == null) {
      return new HashSet();
    }
    return pages.keySet();
  }

  public void setPageSpecificationPath(Map pages, String name, String spec) {

    if (pages != null) {
    	
      pages.put(name, spec);

      propertySupport.firePropertyChange("pageMap", null, pages);

    }

  }

  public void removePageSpecificationPath(Map pages, String name) {

    if (pages != null && pages.containsKey(name)) {

      pages.remove(name);
      propertySupport.firePropertyChange("pageMap", null, pages);
    }
  }

  public String getPageName(Map pages, String specificationPath) {

    String useName = specificationPath;
    String pageName = null;

    if (pages != null) {

      pageName = findKeyInMap(useName, pages);

    }

    if (pageName == null) {

      pageName = findKeyInDefaultPageMap(useName);

    }
    return pageName;
  }

  private String findKeyInDefaultPageMap(String specificationPath) {

    ILibrarySpecification defaultLib = TapestryProjectModelManager.getDefaultLibraryModel().getSpecification();

    if (defaultLib != null) {

      List defaultPageNames = defaultLib.getPageNames();

      for (Iterator iter = defaultPageNames.iterator(); iter.hasNext();) {

        String defaultName = (String) iter.next();
        String defaultValue = (String) defaultLib.getPageSpecificationPath(defaultName);

        if (specificationPath.equals(defaultValue)) {

          return defaultName;
        }
      }

    }

    return null;
  }

  public String findAliasFor(Map components, String componentSpecLocation) {
    String result = null;
    if (components != null && !components.isEmpty()) {

      result = findKeyInMap(componentSpecLocation, components);

    }
    if (result == null) {

      result = findKeyInDefaultComponentMap(componentSpecLocation);
    }
    return result;
  }

  private String findKeyInDefaultComponentMap(String value) {
    ILibrarySpecification defaultLib = TapestryProjectModelManager.getDefaultLibraryModel().getSpecification();

    if (defaultLib != null) {

      List defaultComponentNames = defaultLib.getComponentAliases();

      for (Iterator iter = defaultComponentNames.iterator(); iter.hasNext();) {

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

  /**
   * @see net.sf.tapestry.spec.ILibrarySpecification#setLibrarySpecificationPath(String, String)
   */
  public void setLibrarySpecificationPath(Map libraries, String id, String path) {

    if (libraries != null) {

      libraries.put(id, path);
      propertySupport.firePropertyChange("libraries", null, libraries);
    }

  }

  /**
  * @see com.iw.plugins.spindle.spec.IApplicationOrLibrary#removeLibrarySpecificationPath(String)
  */
  public void removeLibrarySpecificationPath(Map libraries, String name) {

    if (libraries != null && libraries.containsKey(name)) {

      libraries.remove(name);
      propertySupport.firePropertyChange("libraries", null, libraries);

    }
  }

  /**
   * @see net.sf.tapestry.spec.ILibrarySpecification#addExtensionSpecification(String, ExtensionSpecification)
   */
  public void addExtensionSpecification(
    Map extensions,
    String name,
    ExtensionSpecification extension) {

    if (extensions != null) {

      extensions.put(name, extension);
      PluginExtensionSpecification pluginSpec = (PluginExtensionSpecification) extension;
      pluginSpec.setIdentifier(name);
      pluginSpec.setParent(parent);
      pluginSpec.addPropertyChangeListener(parent);

      propertySupport.firePropertyChange("extensions", null, extensions);
    }

  }

  public void removeExtensionSpecification(Map extensions, String name) {

    if (extensions != null && extensions.containsKey(name)) {

      PluginExtensionSpecification pluginSpec = (PluginExtensionSpecification) extensions.get(name);
      extensions.remove(name);
      pluginSpec.removePropertyChangeListener(parent);

      propertySupport.firePropertyChange("extensions", null, extensions);
    }
  }

  /**
  * @see com.iw.plugins.spindle.spec.IApplicationOrLibrary#setExtensionSpecification(String, ExtensionSpecification)
  */
  public void setExtensionSpecification(
    Map extensions,
    String name,
    ExtensionSpecification extension) {

    if (extensions != null && extensions.containsKey(name)) {

      removeExtensionSpecification(extensions, name);
    }

    addExtensionSpecification(extensions, name, extension);

  }

  /**
   * Method getAllExtensionNames.
   * @param map
   * @return Set
   */
  public Set getAllExtensionNames(Map extensions) {
    return extensions.keySet();
  }

}
