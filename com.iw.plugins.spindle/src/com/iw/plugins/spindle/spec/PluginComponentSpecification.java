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
import java.util.*;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.util.Indenter;
import com.iw.plugins.spindle.util.SourceWriter;
import net.sf.tapestry.spec.BeanSpecification;
import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.ContainedComponent;

public class PluginComponentSpecification
  extends ComponentSpecification
  implements ITapestrySpecification, PropertyChangeListener {

  private String name;
  private PropertyChangeSupport propertySupport;

  public PluginComponentSpecification() {
    propertySupport = new PropertyChangeSupport(this);
  }

  //----- ITapestrySpecification --------

  public String getDisplayName() {
    return getName();
  }

  public String getInfo() {
    return getSpecificationResourcePath();
  }

  //----- ITapestrySpecification --------

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Set getReservedParameters() {

    return reservedParameterNames;
  }

  public void setReservedParameter(String name, boolean flag) {
    if (reservedParameterNames == null) {
      reservedParameterNames = new HashSet();
    }
    String value = name.toLowerCase();
    boolean contains = reservedParameterNames.contains(value);
    if (flag && !contains) {
      reservedParameterNames.add(value);
    } else if (!flag && contains) {
      reservedParameterNames.remove(value);
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
    assets.remove(name);
    propertySupport.firePropertyChange("assets", null, assets);
  }

  public void setAsset(String name, PluginAssetSpecification spec) {
    if (assets == null) {
      addAsset(name, spec);
    } else {
      assets.put(name, spec);
    }
    propertySupport.firePropertyChange("assets", null, assets);
  }

  public void addBeanSpecification(String name, BeanSpecification spec) {
    super.addBeanSpecification(name, spec);
    ((PluginBeanSpecification) spec).addPropertyChangeListener(this);
    propertySupport.firePropertyChange("beans", null, beans);
  }

  public void removeBeanSpecification(String name) {
    if (beans.containsKey(name)) {
      beans.remove(name);
      propertySupport.firePropertyChange("beans", null, beans);
    }
  }

  public void setBeanSpecification(String name, PluginBeanSpecification spec) {
    if (beans == null) {
      addBeanSpecification(name, spec);
    } else {
      PluginBeanSpecification old = (PluginBeanSpecification) beans.get(name);
      if (old != null) {
        old.removePropertyChangeListener(this);
      }
      beans.put(name, spec);
      spec.addPropertyChangeListener(this);
    }
    propertySupport.firePropertyChange("beans", null, beans);
  }

  public void addComponent(String name, ContainedComponent component) {
    super.addComponent(name, component);
    ((PluginContainedComponent) component).addPropertyChangeListener(this);
    propertySupport.firePropertyChange("components", null, components);
  }

  public void setComponent(String name, PluginContainedComponent component) {
    if (components == null) {
      addComponent(name, component);
      component.addPropertyChangeListener(this);
    } else {
      PluginContainedComponent old = (PluginContainedComponent) components.get(name);
      if (old != null) {
        old.removePropertyChangeListener(this);
      }

      components.put(name, component);
      component.addPropertyChangeListener(this);

      propertySupport.firePropertyChange("components", null, components);
    }

  }

  public void removeComponent(String name) {
    PluginContainedComponent oldComponent = (PluginContainedComponent) components.get(name);
    components.remove(name);
    if (oldComponent != null) {
      oldComponent.removePropertyChangeListener(this);
    }
    propertySupport.firePropertyChange("components", null, components);
  }

  public void removeParameter(String name) {
    if (parameters.containsKey(name)) {
      parameters.remove(name);
      setReservedParameter(name, false);
      propertySupport.firePropertyChange("parameters", null, components);
    }
  }

  public void setParameter(String name, PluginParameterSpecification spec) {
    if (parameters == null) {
      addParameter(name, spec);
    } else {
      parameters.put(name, spec);
    }
    propertySupport.firePropertyChange("parameters", null, spec);
  }

  public void setAllowBody(boolean value) {
    boolean old = super.getAllowBody();
    super.setAllowBody(value);
    propertySupport.firePropertyChange("allowBody", old, value);
  }

  public void setSpecificationResourcePath(String resourcePath) {
    String old = specificationResourcePath;
    specificationResourcePath = resourcePath;
    propertySupport.firePropertyChange("specificationResourcePath", old, resourcePath);
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

  public void write(PrintWriter writer) {
    int indent = 1;
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.println("<!DOCTYPE specification ");
    writer.println("      PUBLIC \"-//Howard Ship//Tapestry Specification 1.1//EN\"");
    writer.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_1.dtd\">");
    writer.println(MessageUtil.getString("TAPESTRY.xmlComment"));
    writer.println();

    writeFirstLine(writer);

    String description = getDescription();
    if (description != null && !"".equals(description.trim())) {
      writer.println();
      PluginApplicationSpecification.writeDescription(description.trim(), writer, 1);
    }

    if (parameters != null) {
      Collection parms = new TreeSet(parameters.keySet());
      if (!parms.isEmpty()) {
        writer.println();
        Iterator parameterNames = parms.iterator();
        while (parameterNames.hasNext()) {
          String paramName = (String) parameterNames.next();
          ((PluginParameterSpecification) getParameter(paramName)).write(paramName, writer, indent);
        }
      }
    }

    if (reservedParameterNames != null && !reservedParameterNames.isEmpty()) {
      Collection reservedParameters = (Collection) (((HashSet) reservedParameterNames).clone());
      if (parameters != null && !parameters.isEmpty()) {
        reservedParameters.removeAll(parameters.keySet());
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

    Collection pns = getPropertyNames();
    if (pns != null && !pns.isEmpty()) {
      writer.println();
      Iterator propertyNames = new TreeSet(pns).iterator();
      while (propertyNames.hasNext()) {
        String propertyName = (String) propertyNames.next();
        PluginApplicationSpecification.writeProperty(propertyName, getProperty(propertyName), writer, indent);
      }
    }

    Collection bns = getBeanNames();
    if (bns != null && !bns.isEmpty()) {
      writer.println();
      Iterator beanNames = new TreeSet(bns).iterator();
      while (beanNames.hasNext()) {
        String beanName = (String) beanNames.next();
        ((PluginBeanSpecification) getBeanSpecification(beanName)).write(beanName, writer, indent);
      }
    }

    if (components != null && !components.isEmpty()) {
      writeComponents(writer, indent);
    }

    Collection ans = getAssetNames();
    if (ans != null && !ans.isEmpty()) {
      writer.println();
      Iterator assetNames = new TreeSet(ans).iterator();
      while (assetNames.hasNext()) {
        String assetName = (String) assetNames.next();
        ((PluginAssetSpecification) getAsset(assetName)).write(assetName, writer, indent);
      }
    }

    writer.println("</specification>");
  }

  /** Need to do some funky stuff here to ensure "copy-of" components are written AFTER
   *  thier parents
   */
  protected void writeComponents(PrintWriter writer, int indent) {
    ArrayList keysList = new ArrayList(components.keySet());
    HashMap nonCopyOfs = new HashMap();
    HashMap copyOfMap = new HashMap();
    for (int i = 0; i < keysList.size(); i++) {
      String containedName = (String) keysList.get(i);
      PluginContainedComponent component = (PluginContainedComponent) getComponent(containedName);
      String copyOf = component.getCopyOf();
      if (copyOf == null || "".equals(copyOf.trim())) {
        nonCopyOfs.put(containedName, component);
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
        ((PluginContainedComponent) getComponent(containedName)).write(containedName, writer, indent);
      }
    } else {
      while (iter.hasNext()) {
        writer.println();
        String containedName = (String) iter.next();
        ((PluginContainedComponent) getComponent(containedName)).write(containedName, writer, indent);
        if (copyOfMap.containsKey(containedName)) {
          ArrayList listForCopyOf = (ArrayList) copyOfMap.get(containedName);
          if (listForCopyOf == null | listForCopyOf.isEmpty()) {
            continue;
          }
          Iterator copies = listForCopyOf.iterator();
          while (copies.hasNext()) {
            writer.println();
            String copyOfName = (String) copies.next();
            ((PluginContainedComponent) getComponent(copyOfName)).write(copyOfName, writer, indent);
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
            ((PluginContainedComponent) getComponent(copyOfName)).write(copyOfName, writer, indent);
          }
        }
      }

    }
  }

  public void writeFirstLine(PrintWriter writer) {
    writer.print("<specification class=\"");
    writer.print(getComponentClassName());
    writer.print("\" allow-body=\"");
    writer.print((getAllowBody() ? "yes" : "no"));
    writer.print("\" allow-informal-parameters=\"");
    writer.print((getAllowInformalParameters() ? "yes" : "no"));
    writer.println("\" >");
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

}