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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.spec.AssetSpecification;
import net.sf.tapestry.spec.BeanSpecification;
import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.ContainedComponent;
import net.sf.tapestry.spec.ParameterSpecification;
import net.sf.tapestry.util.IPropertyHolder;

import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.util.Indenter;
import com.iw.plugins.spindle.util.SourceWriter;

public class PluginComponentSpecification
  extends ComponentSpecification
  implements IIdentifiable, PropertyChangeListener, IParameterHolder {

  private String identifier;
  private TapestryComponentModel parent;

  private PropertyChangeSupport propertySupport;

  public PluginComponentSpecification() {
    propertySupport = new PropertyChangeSupport(this);
  }

  public Set getReservedParameters() {

    return _reservedParameterNames;
  }

  public void setReservedParameter(String name, boolean flag) {
    if (_reservedParameterNames == null) {
      _reservedParameterNames = new HashSet();
    }
    String value = name.toLowerCase();
    boolean contains = _reservedParameterNames.contains(value);
    if (flag && !contains) {
      _reservedParameterNames.add(value);
    } else if (!flag && contains) {
      _reservedParameterNames.remove(value);
    }
    propertySupport.firePropertyChange("parameters", flag, !flag);
  }

  public boolean usesAlias(String alias) {
    Iterator iter = getComponentIds().iterator();
    while (iter.hasNext()) {
      PluginContainedComponent comp = (PluginContainedComponent) getComponent((String) iter.next());
      if (comp.getType().equals(alias)) {
        return true;
      }
    }
    return false;
  }

  public void setComponentClassName(String name) {
    String old = super.getComponentClassName();
    super.setComponentClassName(name);
    propertySupport.firePropertyChange("componentClassName", old, name);
  }

  public void removeAsset(String name) {
    _assets.remove(name);
    propertySupport.firePropertyChange("_assets", null, _assets);
  }

  public void setAsset(String name, PluginAssetSpecification spec) {
    if (_assets == null) {
      addAsset(name, spec);
    } else {
      _assets.put(name, spec);
      spec.setIdentifier(name);
      spec.setParent(this);
    }
    propertySupport.firePropertyChange("_assets", null, _assets);
  }

  public void addBeanSpecification(String name, BeanSpecification spec) {
    super.addBeanSpecification(name, spec);

    PluginBeanSpecification pluginSpec = (PluginBeanSpecification) spec;
    pluginSpec.setIdentifier(name);
    pluginSpec.setParent(this);
    pluginSpec.addPropertyChangeListener(this);
    propertySupport.firePropertyChange("_beans", null, _beans);
  }

  public void removeBeanSpecification(String name) {
    if (_beans.containsKey(name)) {
      _beans.remove(name);
      propertySupport.firePropertyChange("_beans", null, _beans);
    }
  }

  public void setBeanSpecification(String name, PluginBeanSpecification spec) {
    if (_beans == null) {
      addBeanSpecification(name, spec);
    } else {
      PluginBeanSpecification old = (PluginBeanSpecification) _beans.get(name);
      if (old != null) {
        old.removePropertyChangeListener(this);
        old.setParent(null);
      }
      _beans.put(name, spec);
      spec.setIdentifier(name);
      spec.setParent(this);
      spec.addPropertyChangeListener(this);
    }
    propertySupport.firePropertyChange("_beans", null, _beans);
  }

  public void addComponent(String name, ContainedComponent component) {
    super.addComponent(name, component);
    PluginContainedComponent pcomponent = (PluginContainedComponent) component;

    pcomponent.addPropertyChangeListener(this);
    pcomponent.setIdentifier(name);
    pcomponent.setParent(this);
    propertySupport.firePropertyChange("components", null, _components);
  }

  public void setComponent(String name, PluginContainedComponent component) {
    if (_components == null) {
      addComponent(name, component);
    } else {
      PluginContainedComponent old = (PluginContainedComponent) _components.get(name);
      if (old != null) {
        old.removePropertyChangeListener(this);
        old.setParent(null);
      }

      _components.put(name, component);
      component.setIdentifier(name);
      component.setParent(this);
      component.addPropertyChangeListener(this);

      propertySupport.firePropertyChange("components", null, _components);
    }

  }

  public void removeComponent(String name) {
    PluginContainedComponent oldComponent = (PluginContainedComponent) _components.get(name);
    _components.remove(name);
    if (oldComponent != null) {
      oldComponent.removePropertyChangeListener(this);
    }
    propertySupport.firePropertyChange("components", null, _components);
  }

  public void removeParameter(String name) {
    if (_parameters.containsKey(name)) {
      _parameters.remove(name);
      setReservedParameter(name, false);
      propertySupport.firePropertyChange("parameters", null, _parameters);
    }
  }

  public void setParameter(String name, PluginParameterSpecification spec) {
    if (_parameters == null) {
      addParameter(name, spec);
    } else {
      _parameters.put(name, spec);
      addReservedParameterName(name);
      spec.setIdentifier(name);
      spec.setParent(this);
    }
    propertySupport.firePropertyChange("parameters", null, spec);
  }

  public void setAllowBody(boolean value) {
    boolean old = super.getAllowBody();
    super.setAllowBody(value);
    propertySupport.firePropertyChange("allowBody", old, value);
  }

  public void setSpecificationResourcePath(String resourcePath) {
    super.setSpecificationResourcePath(resourcePath);
    propertySupport.firePropertyChange("specificationResourcePath", null, resourcePath);
  }

  public void setProperty(String name, String value) {
    String old = super.getProperty(name);
    super.setProperty(name, value);
    propertySupport.firePropertyChange("properties", old, value);
  }

  public void removeProperty(String name) {
    String old = super.getProperty(name);
    super.removeProperty(name);
    propertySupport.firePropertyChange("properties", old, null);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  public void propertyChange(PropertyChangeEvent event) {
    propertySupport.firePropertyChange(event);
  }

  private String getSpecTagName(String publicId) {

    String tagname = "specification";

    if (publicId.equals(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID)) {

      if (isPageSpecification()) {

        tagname = "page-specification";

      } else {

        tagname = "component-specification";
      }
    }
    return tagname;
  }

  public void writeFirstLine(PrintWriter writer) {

    String tagname = getSpecTagName(getPublicId());

    writer.print("<" + tagname + " class=\"");
    writer.print(getComponentClassName());
    writer.print("\"");

    if (!isPageSpecification()) {

      writer.print(" allow-body=\"");
      writer.print((getAllowBody() ? "yes" : "no"));
      writer.print("\" allow-informal-parameters=\"");
      writer.print((getAllowInformalParameters() ? "yes" : "no"));
      writer.print("\"");

    }
    writer.println(">");
  }

  public void write(PrintWriter writer) {

    boolean isPage = isPageSpecification();

    String publicId = getPublicId();

    String tagname = getSpecTagName(publicId);

    XMLUtil.writeXMLHeader(publicId, tagname, writer);

    writer.println();

    writeFirstLine(writer);

    int indent = 1;

    XMLUtil.writeDescription(writer, indent, getDescription());

    writeComponentParameters(_parameters, writer, publicId, indent);

    writeReservedParameters(_reservedParameterNames, _parameters, writer, indent);

    XMLUtil.writeProperties((IPropertyHolder) this, writer, indent);

    writeBeans(getBeanNames(), writer, indent);

    writeContainedComponents(_components, writer, indent, publicId);

    writeAssets(getAssetNames(), writer, indent);

    writer.println();

    writer.println("</" + tagname + ">");
  }

  public void writeAssets(List assetNames, PrintWriter writer, int indent) {

    if (assetNames != null && !assetNames.isEmpty()) {

      writer.println();
      Iterator names = new TreeSet(assetNames).iterator();

      while (names.hasNext()) {

        String assetName = (String) names.next();
        ((PluginAssetSpecification) getAsset(assetName)).write(assetName, writer, indent);
      }
    }
  }

  public void writeBeans(Collection beanNames, PrintWriter writer, int indent) {

    if (beanNames != null && !beanNames.isEmpty()) {

      writer.println();
      Iterator names = new TreeSet(beanNames).iterator();

      while (names.hasNext()) {

        String beanName = (String) names.next();
        ((PluginBeanSpecification) getBeanSpecification(beanName)).write(beanName, writer, indent);
      }
    }
  }

  public void writeReservedParameters(
    Set names,
    Map parameterMap,
    PrintWriter writer,
    int indent) {

    if (isPageSpecification()) {

      return;
    }

    if (names != null && !names.isEmpty()) {

      Collection reservedParameters = (Collection) (((HashSet) names).clone());

      if (parameterMap != null && !parameterMap.isEmpty()) {

        reservedParameters.removeAll(parameterMap.keySet());

      }
      if (!reservedParameters.isEmpty()) {

        writer.println();
        Iterator reservedInformals = reservedParameters.iterator();

        while (reservedInformals.hasNext()) {

          Indenter.printIndented(writer, indent, "<reserved-parameter name=\"");
          writer.print(reservedInformals.next());
          writer.println("\"/>");
        }
      }
    }
  }

  public void writeComponentParameters(
    Map parameterMap,
    PrintWriter writer,
    String publicId,
    int indent) {

    if (isPageSpecification()) {

      return;
    }

    if (parameterMap != null) {

      Collection parms = new TreeSet(parameterMap.keySet());

      if (!parms.isEmpty()) {

        writer.println();
        Iterator parameterNames = parms.iterator();

        while (parameterNames.hasNext()) {

          String paramName = (String) parameterNames.next();

          PluginParameterSpecification parameterSpec =
            (PluginParameterSpecification) parameterMap.get(paramName);

          parameterSpec.write(paramName, writer, indent, publicId);
        }
      }
    }
  }

  /** Need to do some funky stuff here to ensure "copy-of" components are written AFTER
   *  thier parents
   */
  public void writeContainedComponents(
    Map containedComponents,
    PrintWriter writer,
    int indent,
    String publicId) {

    if (containedComponents == null || containedComponents.isEmpty()) {
      return;
    }

    ArrayList keysList = new ArrayList(containedComponents.keySet());
    HashMap nonCopyOfs = new HashMap();
    HashMap copyOfMap = new HashMap();

    PluginContainedComponent currentComponent;

    for (int i = 0; i < keysList.size(); i++) {

      String containedName = (String) keysList.get(i);
      currentComponent = (PluginContainedComponent) getComponent(containedName);
      String copyOf = currentComponent.getCopyOf();

      if (copyOf == null || "".equals(copyOf.trim())) {

        nonCopyOfs.put(containedName, currentComponent);

      } else {

        if (!copyOfMap.containsKey(copyOf)) {

          copyOfMap.put(copyOf, new ArrayList());
        }

        ArrayList listForCopyOf = (ArrayList) copyOfMap.get(copyOf);
        listForCopyOf.add(containedName);
      }
    }

    Iterator iter = new TreeSet(nonCopyOfs.keySet()).iterator();

    if (copyOfMap.isEmpty()) {

      while (iter.hasNext()) {

        writer.println();
        String containedName = (String) iter.next();
        currentComponent = (PluginContainedComponent) getComponent(containedName);
        currentComponent.write(containedName, writer, indent, publicId);
      }

    } else {

      while (iter.hasNext()) {

        writer.println();
        String containedName = (String) iter.next();
        currentComponent = (PluginContainedComponent) getComponent(containedName);
        currentComponent.write(containedName, writer, indent, publicId);

        if (copyOfMap.containsKey(containedName)) {

          ArrayList listForCopyOf = (ArrayList) copyOfMap.get(containedName);

          if (listForCopyOf == null | listForCopyOf.isEmpty()) {

            continue;
          }

          Iterator copies = listForCopyOf.iterator();

          while (copies.hasNext()) {

            writer.println();
            String copyOfName = (String) copies.next();

            currentComponent = (PluginContainedComponent) getComponent(copyOfName);
            currentComponent.write(copyOfName, writer, indent, publicId);
          }

          copyOfMap.remove(containedName);
        }
      }
      if (!copyOfMap.isEmpty()) {

        Iterator leftovers = new TreeSet(copyOfMap.keySet()).iterator();

        while (leftovers.hasNext()) {

          ArrayList leftoverIds = (ArrayList) copyOfMap.get(leftovers.next());

          if (leftoverIds == null || leftoverIds.isEmpty()) {

            continue;

          }

          Iterator leftoverIter = leftoverIds.iterator();

          while (leftoverIter.hasNext()) {

            writer.println();
            String copyOfName = (String) leftoverIter.next();
            currentComponent = (PluginContainedComponent) getComponent(copyOfName);
            currentComponent.write(copyOfName, writer, indent, publicId);
          }
        }
      }

    }
  }

  public void getHelpText(String name, StringBuffer buffer) {
    buffer.append(getHelpText(name));
  }

  public String getHelpText(String name) {
    StringWriter swriter = new StringWriter();
    SourceWriter writer = new SourceWriter(swriter);
    writer.print(name);
    writer.print("\n");
    writeFirstLine(writer);
    String description = getDescription();
    if (description != null && !"".equals(description.trim())) {
      writer.println(description);
    }
    return swriter.toString();
  }

  public void setPublicId(String publicId) {

    super.setPublicId(publicId);
    propertySupport.firePropertyChange("dtd", null, publicId);

  }

  /**
   * @see net.sf.tapestry.spec.ComponentSpecification#addAsset(String, AssetSpecification)
   */
  public void addAsset(String name, AssetSpecification asset) {
    super.addAsset(name, asset);

    PluginAssetSpecification pluginAsset = (PluginAssetSpecification) asset;
    pluginAsset.setIdentifier(name);
    pluginAsset.setParent(this);

  }

  /**
   * @see net.sf.tapestry.spec.ComponentSpecification#addParameter(String, ParameterSpecification)
   */
  public void addParameter(String name, ParameterSpecification spec) {
    if (_parameters == null) {
    	
      _parameters = new HashMap();
      
    }

    _parameters.put(name, spec);
    PluginParameterSpecification pluginParam = (PluginParameterSpecification) spec;
    pluginParam.setIdentifier(name);
    pluginParam.setParent(this);
    propertySupport.firePropertyChange("parameters", null, _parameters);
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
   * @return TapestryComponentModel
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
    this.parent = (TapestryComponentModel) parent;
  }

}