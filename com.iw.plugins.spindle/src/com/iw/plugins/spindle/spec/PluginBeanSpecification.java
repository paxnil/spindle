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
import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.iw.plugins.spindle.bean.PluginFieldBeanInitializer;
import com.iw.plugins.spindle.bean.PluginPropertyBeanInitializer;
import com.iw.plugins.spindle.bean.PluginStaticBeanInitializer;
import com.iw.plugins.spindle.util.Indenter;
import com.primix.tapestry.bean.IBeanInitializer;
import com.primix.tapestry.bean.PropertyBeanInitializer;
import com.primix.tapestry.bean.StaticBeanInitializer;
import com.primix.tapestry.spec.BeanLifecycle;
import com.primix.tapestry.spec.BeanSpecification;

public class PluginBeanSpecification extends BeanSpecification implements PropertyChangeListener {

  PropertyChangeSupport propertySupport;
  /**
   * Constructor for PluginBeanSpecification
   */
  public PluginBeanSpecification(String className, BeanLifecycle lifecycle) {
    super(className, lifecycle);
    propertySupport = new PropertyChangeSupport(this);
  }

  public void addInitializer(IBeanInitializer initer) {
    super.addInitializer(initer);
    if (initer instanceof IPropertyChangeProvider) {
      ((IPropertyChangeProvider) initer).addPropertyChangeListener(this);
    }
    propertySupport.firePropertyChange("beanInitializers", null, initializers);
  }

  public void removeInitializer(IBeanInitializer initer) {
    if (initer != null && initializers != null) {
      initializers.remove(initer);
      if (initer instanceof IPropertyChangeProvider) {
        ((IPropertyChangeProvider) initer).removePropertyChangeListener(this);
      }
      propertySupport.firePropertyChange("beanInitializers", null, initializers);
    }
  }

  public void setClassName(String name) {
    className = name;
    propertySupport.firePropertyChange("beanName", null, name);
  }

  public void setLifecycle(BeanLifecycle newLifecycle) {
    lifecycle = newLifecycle;
    propertySupport.firePropertyChange("beanLifecycle", null, newLifecycle);
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

  public void write(String name, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<bean name=\"" + name);
    writer.print("\" class=\"" + getClassName());
    writer.print("\" lifecycle=\"");
    if (getLifecycle().equals(BeanLifecycle.NONE)) {
      writer.print("none\"");
    } else if (getLifecycle().equals(BeanLifecycle.PAGE)) {
      writer.print("page\"");
    } else if (getLifecycle().equals(BeanLifecycle.REQUEST)) {
      writer.print("request\"");
    }
    String description = getDescription();
    Collection inits = getInitializers();
    boolean writeableDescription = description != null && !"".equals(description.trim());
    boolean writeableInitializers = inits != null && !inits.isEmpty();
    if (writeableDescription || writeableInitializers) {
      writer.println(">");
      if (writeableDescription) {
        PluginApplicationSpecification.writeDescription(description.trim(), writer, indent + 1);
      }
      if (writeableInitializers) {
        Iterator initializers = inits.iterator();
        while (initializers.hasNext()) {
          IBeanInitializer initer = (IBeanInitializer) initializers.next();
          write(initer, writer, indent + 1);
        }
      }
    } else {
      writer.println("/>");
      return;
    }
    Indenter.printlnIndented(writer, indent, "</bean>");

  }

  public void write(IBeanInitializer initializer, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<set-property name=\"" + initializer.getPropertyName());
    writer.println("\">");
    if (initializer instanceof PropertyBeanInitializer) {
      Indenter.printIndented(
        writer,
        indent + 1,
        "<property-value property-path=\""
          + buildPropertyPath((PluginPropertyBeanInitializer) initializer));
      writer.println("\"/>");

    } else if (initializer instanceof StaticBeanInitializer) {
      StaticTypeValue StaticTypeValue =
        findStaticType(((PluginStaticBeanInitializer) initializer).getValue());
      Indenter.printIndented(writer, indent + 1, "<static-value type=\"" + StaticTypeValue.type);
      writer.println("\">");
      Indenter.printlnIndented(writer, indent + 2, StaticTypeValue.value);
      Indenter.printlnIndented(writer, indent + 1, "</static-value>");
    } else if (initializer instanceof PluginFieldBeanInitializer) {
    	Indenter.printIndented(
        writer,
        indent + 1,
        "<field-value field-name=\""
          + ((PluginFieldBeanInitializer) initializer).getFieldName());
      writer.println("\"/>");
    }
    Indenter.printlnIndented(writer, indent, "</set-property>");
  }

  private StaticTypeValue findStaticType(Object value) {
    String type = value.getClass().getName();
    StaticTypeValue result = new StaticTypeValue();
    if (type.endsWith(".String")) {
      result.type = "String";
      result.value = (String) value;
      return result;
    }
    if (type.endsWith(".Integer")) {
      result.type = "int";
      result.value = ((Integer) value).toString();
      return result;
    }
    if (type.endsWith(".Boolean")) {
      result.type = "boolean";
      result.value = ((Boolean) value).toString();
      return result;
    }
    if (type.endsWith(".Double")) {
      result.type = "double";
      result.value = ((Double) value).toString();
      return result;
    }
    return null;
  }

  private String buildPropertyPath(PluginPropertyBeanInitializer initializer) {
    String[] path = initializer.getPropertyPath();
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < path.length; i++) {
      result.append(path[i]);
      if (i < path.length - 1) {
        result.append(".");
      }
    }
    return result.toString();

  }

  class StaticTypeValue {
    String type;
    String value;
  }
}