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
import java.util.Iterator;

import net.sf.tapestry.bean.IBeanInitializer;
import net.sf.tapestry.bean.StaticBeanInitializer;
import net.sf.tapestry.bean.StringBeanInitializer;
import net.sf.tapestry.spec.BeanLifecycle;
import net.sf.tapestry.spec.BeanSpecification;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.spec.bean.PluginExpressionBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginFieldBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginStaticBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginStringBeanInitializer;
import com.iw.plugins.spindle.ui.descriptors.ComboBoxPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.TypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.util.Indenter;

public class PluginBeanSpecification
  extends BeanSpecification
  implements PropertyChangeListener, IIdentifiable, IPropertySource {

  PropertyChangeSupport propertySupport;

  private String identifier;
  private PluginComponentSpecification parent;
  /**
   * Constructor for PluginBeanSpecification
   */
  public PluginBeanSpecification(String className, BeanLifecycle lifecycle) {
    super(className, lifecycle);
    propertySupport = new PropertyChangeSupport(this);
  }

  private int getParentDTDVersion() {

    PluginComponentSpecification cspec = (PluginComponentSpecification) parent;

    int DTDVersion = XMLUtil.getDTDVersion(cspec.getPublicId());

    return DTDVersion;

  }

  public boolean alreadyHasInitializer(String propertyName) {

    Collection initializers = getInitializers();

    if (initializers != null) {

      for (Iterator iter = initializers.iterator(); iter.hasNext();) {

        IBeanInitializer initer = (IBeanInitializer) iter.next();

        if (initer.getPropertyName().equals(propertyName)) {
          return true;
        }
      }
    }
    return false;
  }

  public void addInitializer(IBeanInitializer initer) {
    super.addInitializer(initer);
    if (initer instanceof IPropertyChangeProvider) {

      ((IPropertyChangeProvider) initer).addPropertyChangeListener(this);
    }

    IIdentifiable identifiable = (IIdentifiable) initer;
    identifiable.setParent(this);

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

    } else if (getLifecycle().equals(BeanLifecycle.RENDER)) {

      writer.print("render\"");

    }
    String description = getDescription();
    Collection inits = getInitializers();

    boolean writeableDescription = description != null && !"".equals(description.trim());
    boolean writeableInitializers = inits != null && !inits.isEmpty();

    if (writeableDescription || writeableInitializers) {

      writer.println(">");

      if (writeableDescription) {

        XMLUtil.writeDescription(writer, indent + 1, description.trim(), false);
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
    PluginComponentSpecification cspec = (PluginComponentSpecification) parent;

    int DTDVersion = getParentDTDVersion();

    if (DTDVersion < XMLUtil.DTD_1_3) {

      writePre13(initializer, writer, indent);

    } else {

      write13(initializer, writer, indent);

    }

  }

  public void writePre13(IBeanInitializer initializer, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<set-property name=\"" + initializer.getPropertyName());
    writer.println("\">");

    if (initializer instanceof PluginExpressionBeanInitializer) {

      Indenter.printIndented(
        writer,
        indent + 1,
        "<property-value property-path=\""
          + ((PluginExpressionBeanInitializer) initializer).getExpression());
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
        "<field-value field-name=\"" + ((PluginFieldBeanInitializer) initializer).getFieldName());
      writer.println("\"/>");

    }
    Indenter.printlnIndented(writer, indent, "</set-property>");
  }

  public void write13(IBeanInitializer initializer, PrintWriter writer, int indent) {

    if (initializer instanceof StringBeanInitializer) {
      Indenter.printIndented(
        writer,
        indent,
        "<set-string-property name=\"" + initializer.getPropertyName());
      writer.print("\" key='");
      writer.print(((PluginStringBeanInitializer) initializer).getKey());

    } else {

      Indenter.printIndented(
        writer,
        indent,
        "<set-property name=\"" + initializer.getPropertyName());
      writer.print("\" expression='");
      
//      if (initializer instanceof PluginPropertyBeanInitializer) {
//      	
//        writer.print(((PluginPropertyBeanInitializer) initializer).getPropertyPath());
//
//      } else 
      if (initializer instanceof PluginExpressionBeanInitializer) {

        writer.print(((PluginExpressionBeanInitializer) initializer).getExpression());

      } else if (initializer instanceof StaticBeanInitializer) {

        StaticTypeValue svalue =
          findStaticType(((PluginStaticBeanInitializer) initializer).getValue());

        String OGNL = svalue.value;

        if ("String".equals(svalue.type)) {

          OGNL = '"' + OGNL + '"';
        }

        // convert to OGNL
        writer.print(OGNL);

      } else if (initializer instanceof PluginFieldBeanInitializer) {

        String OGNL = ((PluginFieldBeanInitializer) initializer).getFieldName();
        int index = OGNL.lastIndexOf(".");
        if (index > 0) {

          String clazz = OGNL.substring(0, index);
          String field = OGNL.substring(index + 1);

          OGNL = "@" + clazz + "@" + field;

        }

        writer.print(OGNL);
      }
    }
    writer.println("'/>");
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

 

  class StaticTypeValue {
    String type;
    String value;
  }

  public PluginBeanSpecification deepCopy() {
    PluginBeanSpecification result = new PluginBeanSpecification(getClassName(), getLifecycle());

    for (Iterator iter = getInitializers().iterator(); iter.hasNext();) {

      Object initer = iter.next();

      if (initer.getClass() == PluginStaticBeanInitializer.class) {

        result.addInitializer(((PluginStaticBeanInitializer) initer).deepCopy());

      } else if (initer.getClass() == PluginFieldBeanInitializer.class) {

        result.addInitializer(((PluginFieldBeanInitializer) initer).deepCopy());

      } else if (initer.getClass() == PluginExpressionBeanInitializer.class) {

        result.addInitializer(((PluginExpressionBeanInitializer) initer).deepCopy());
      }

    }
    return result;
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

  private BeanLifecycle[] lifecycles =
    { BeanLifecycle.NONE, BeanLifecycle.PAGE, BeanLifecycle.REQUEST, BeanLifecycle.RENDER };

  private String[] lifecycleLabelsPre13 = { "None", "Page", "Request" };

  private IPropertyDescriptor[] descriptorsPre13 =
    {
      new TextPropertyDescriptor("name", "Name"),
      new TypeDialogPropertyDescriptor("class", "Class", IJavaElementSearchConstants.CONSIDER_CLASSES),
      new ComboBoxPropertyDescriptor("lifecycle", "Lifecycle", lifecycleLabelsPre13, false)};

  private String[] lifecycleLabels13 = { "None", "Page", "Request", "Render" };

  private IPropertyDescriptor[] descriptors13 =
    {
      new TextPropertyDescriptor("name", "Name"),
      new TypeDialogPropertyDescriptor("class", "Class",IJavaElementSearchConstants.CONSIDER_CLASSES),
      new ComboBoxPropertyDescriptor("lifecycle", "Lifecycle", lifecycleLabels13, false)};

  public void resetPropertyValue(Object key) {
  }

  public void setPropertyValue(Object key, Object value) {

    PluginComponentSpecification parentSpec = (PluginComponentSpecification) parent;

    if ("name".equals(key)) {

      String oldName = this.identifier;
      String newName = (String) value;

      if ("".equals(newName.trim())) {

        newName = oldName;

      } else if (parentSpec.getBeanNames().contains(newName)) {

        newName = newName + "Copy";
        parentSpec.addBeanSpecification(newName, deepCopy());
        return;
      }
      this.identifier = newName;
      parentSpec.removeBeanSpecification(oldName);

    } else if ("class".equals(key)) {

      setClassName((String) value);

    } else if ("lifecycle".equals(key)) {

      int chosenIndex = ((Integer) value).intValue();
      setLifecycle(lifecycles[chosenIndex]);
    }

    parentSpec.setBeanSpecification(this.identifier, this);

  }

  public boolean isPropertySet(Object key) {
    if ("name".equals(key)) {

      return true;

    } else if ("class".equals(key)) {

      return getClassName() != null;

    }
    return true;

  }

  public Object getPropertyValue(Object key) {
    if ("name".equals(key)) {

      return this.identifier;

    } else if ("class".equals(key)) {

      return getClassName();

    } else if ("lifecycle".equals(key)) {

      BeanLifecycle current = getLifecycle();

      for (int i = 0; i < lifecycles.length; i++) {

        if (lifecycles[i] == current) {

          return new Integer(i);
        }
      }
    }
    return null;

  }

  public IPropertyDescriptor[] getPropertyDescriptors() {
    int DTDVersion = getParentDTDVersion();

    if (DTDVersion < XMLUtil.DTD_1_3) {

      return descriptorsPre13;
    } else {

      return descriptors13;

    }

  }

  public Object getEditableValue() {
    return identifier;
  }

}