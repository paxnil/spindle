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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.tapestry.bean.IBeanInitializer;
import org.apache.tapestry.bean.MessageBeanInitializer;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.Direction;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.util.IPropertyHolder;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.spec.BaseSpecification;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginExtensionConfiguration;
import com.iw.plugins.spindle.core.spec.PluginExtensionSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.spec.PluginListenerBindingSpecification;
import com.iw.plugins.spindle.core.spec.bean.PluginExpressionBeanInitializer;
import com.iw.plugins.spindle.core.spec.bean.PluginMessageBeanInitializer;

/**
 * @author gwl
 * 
 * 
 * Copyright 2002, Intelligent Work Inc. All Rights Reserved.
 */
public class XMLUtil
{

  static public final int DTD_1_1 = 1;

  static public final int DTD_1_2 = 2;

  static public final int DTD_1_3 = 3;

  static public final int DTD_3_0 = 4;

  static public final int DTD_SERVLET_2_2 = 5;

  static public final int DTD_SERVLET_2_3 = 6;

  static public final int UNKNOWN_DTD = 999;

  static public final int[] ALLOWED_SPEC_DTDS = new int[]{DTD_1_3, DTD_3_0};

  static public final String SPEC_DTD_ERROR_KEY = "error-invalid-spec-public-id";

  static public final int[] ALLOWED_SERVLET_DTDS = new int[]{DTD_SERVLET_2_2,
      DTD_SERVLET_2_3};

  static public final String SERVLET_DTD_ERROR_KEY = "error-invalid-servlet-public-id";

  static public int getDTDVersion(String publicId)
  {

    if (publicId == null)
      return UNKNOWN_DTD;

    if (publicId.equals(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID))
      return DTD_1_3;

    if (publicId.equals(SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID))
      return DTD_3_0;

    if (publicId.equals(TapestryCore.SERVLET_2_2_PUBLIC_ID))
      return DTD_SERVLET_2_2;

    if (publicId.equals(TapestryCore.SERVLET_2_3_PUBLIC_ID))
      return DTD_SERVLET_2_3;

    return UNKNOWN_DTD;
  }

  static public String getPublicId(int DTDVersion)
  {

    switch (DTDVersion)
    {

      //  		case DTD_1_1: return SpecificationParser.TAPESTRY_DTD_1_1_PUBLIC_ID;
      //  		
      //  		case DTD_1_2: return SpecificationParser.TAPESTRY_DTD_1_2_PUBLIC_ID;

      case DTD_1_3 :
        return SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID;
      case DTD_3_0 :
        return SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID;
      case DTD_SERVLET_2_2 :
        return TapestryCore.SERVLET_2_2_PUBLIC_ID;
      case DTD_SERVLET_2_3 :
        return TapestryCore.SERVLET_2_3_PUBLIC_ID;

    }

    return null;

  }

  public static void writeSpecification(Writer writer, BaseSpecification specification)
  {
    IndentingWriter indenter = checkWriter(writer);
    writeSpecification(indenter, specification, 0);
  }

  public static void writeSpecification(
      Writer writer,
      BaseSpecification specification,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    switch (specification.getSpecificationType())
    {
      case BaseSpecification.LIBRARY_SPEC :
        writeLibrarySpecification(
            indenter,
            (PluginLibrarySpecification) specification,
            indent);
        break;

      case BaseSpecification.APPLICATION_SPEC :
        writeApplicationSpecification(
            indenter,
            (PluginApplicationSpecification) specification,
            indent);
        break;
      case BaseSpecification.EXTENSION_CONFIGURATION :
        writeExtensionConfiguration(
            indenter,
            (PluginExtensionConfiguration) specification,
            indent);
        break;

      case BaseSpecification.EXTENSION_SPEC :
        writeExtensionSpecification(
            indenter,
            (PluginExtensionSpecification) specification,
            indent);
        break;

      case BaseSpecification.COMPONENT_SPEC :
        writeComponentSpecification(
            indenter,
            (PluginComponentSpecification) specification,
            indent);
        break;
      default :
        throw new IllegalStateException("unknown spec type!");

    }
  }

  private static IndentingWriter checkWriter(Writer writer)
  {
    if (writer instanceof IndentingWriter)
      return (IndentingWriter) writer;

    return IndentingWriter.getDefaultIndentingWriter(writer);
  }

  public static void writeComponentSpecification(
      Writer writer,
      PluginComponentSpecification component,
      int indent)
  {
    writeComponentSpecification(writer, component, indent, true);
  }

  public static void writeComponentSpecification(
      Writer writer,
      PluginComponentSpecification component,
      int indent,
      boolean writeHeader)
  {
    IndentingWriter indenter = checkWriter(writer);
    boolean isPage = component.isPageSpecification();
    String rootElement = isPage ? "page-specification" : "component-specification";

    if (writeHeader)
      writeXMLHeader(component.getPublicId(), rootElement, indenter);

    indenter.print("<" + rootElement + " class=\"");
    indenter.print(component.getComponentClassName());
    indenter.print("\"");
    if (!isPage)
    {
      indenter.print(" allow-body=\"" + (component.getAllowBody() ? "yes" : "no") + "\"");
      indenter.print(" allow-informal-parameters=\""
          + (component.getAllowInformalParameters() ? "yes" : "no") + "\"");
    }
    indenter.print(">");
    writeDescription(indenter, indent + 1, component.getDescription());
    if (!isPage)
    {
      writeComponentParameters(component, indenter, component.getPublicId(), indent + 1);
      writeReservedParameters(component, indenter, indent + 1);
    }
    writeProperties((IPropertyHolder) component, indenter, indent + 1);
    writeBeans(component, indenter, indent + 1);
    writePropertySpecifications(component, indenter, indent + 1);
    writeContainedComponents(component, indenter, indent + 1, component.getPublicId());
    writeAssets(component, indenter, indent + 1);

    indenter.println();
    indenter.println("</" + rootElement + ">");
  }

  public static void writeComponentSpecificationHeader(
      Writer writer,
      IComponentSpecification component,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    boolean isPage = component.isPageSpecification();
    String rootElement = isPage ? "page-specification" : "component-specification";

    indenter.println("<" + rootElement);
    indenter.printIndented(1, "class=\"");
    indenter.print(component.getComponentClassName());
    indenter.print("\"");
    if (!isPage)
    {
      indenter.println();
      indenter.printlnIndented(1, "allow-body=\""
          + (component.getAllowBody() ? "yes" : "no") + "\"");
      indenter.printIndented(1, "allow-informal-parameters=\""
          + (component.getAllowInformalParameters() ? "yes" : "no") + "\"");
    }
    indenter.println(">");
  }

  /**
   * @param component
   * @param writer
   * @param i
   */
  private static void writeBeans(
      PluginComponentSpecification component,
      Writer writer,
      int indent)
  {

    Collection beanNames = component.getBeanNames();
    if (beanNames != null && !beanNames.isEmpty())
    {
      Iterator names = new TreeSet(beanNames).iterator();

      while (names.hasNext())
      {
        String beanName = (String) names.next();
        PluginBeanSpecification bean = (PluginBeanSpecification) component
            .getBeanSpecification(beanName);
        BeanLifecycle lifecycle;
        String description;
        Collection inits;
        boolean writeableDescription;
        boolean writeableInitializers;
        writeBean(component, indent, writer, beanName, bean);
      }
    }
  }

  public static void writeBean(
      PluginComponentSpecification component,
      int indent,
      Writer writer,
      String beanName,
      PluginBeanSpecification bean)
  {
    writeBean(component, indent, writer, beanName, bean, true);
  }

  public static void writeBean(
      PluginComponentSpecification component,
      int indent,
      Writer writer,
      String beanName,
      PluginBeanSpecification bean,
      boolean newLine)
  {
    IndentingWriter indenter = checkWriter(writer);
    if (newLine)
      indenter.println();

    indenter.printIndented(indent, "<bean name=\"" + beanName);
    indenter.print("\" class=\"" + bean.getClassName());
    indenter.print("\" lifecycle=\"");

    BeanLifecycle lifecycle = bean.getLifecycle();

    if (lifecycle == null || lifecycle == BeanLifecycle.REQUEST)
    {
      indenter.print("request\"");

    } else if (lifecycle == BeanLifecycle.NONE)
    {

      indenter.print("none\"");

    } else if (lifecycle == BeanLifecycle.PAGE)
    {

      indenter.print("page\"");

    } else if (lifecycle == BeanLifecycle.RENDER)
    {

      indenter.print("render\"");

    }
    String description = bean.getDescription();
    Collection inits = bean.getInitializers();

    boolean writeableDescription = description != null && !"".equals(description.trim());
    boolean writeableInitializers = inits != null && !inits.isEmpty();

    if (writeableDescription || writeableInitializers)
    {

      indenter.println(">");

      if (writeableDescription)
      {

        XMLUtil.writeDescription(indenter, indent + 1, description.trim(), false);
      }

      if (writeableInitializers)
      {

        Iterator initializers = inits.iterator();

        while (initializers.hasNext())
        {

          IBeanInitializer initer = (IBeanInitializer) initializers.next();
          writeBeanInitializer(component, initer, indenter, indent + 1);
        }
      }
      indenter.printlnIndented(indent, "</bean>");

    } else
    {
      indenter.println("/>");
    }
  }

  public static void writeBeanInitializer(
      IComponentSpecification spec,
      IBeanInitializer initializer,
      Writer writer,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    int DTDVersion = getDTDVersion(spec.getPublicId());

    if (initializer instanceof MessageBeanInitializer)
    {
      String intro = DTDVersion < DTD_3_0
          ? "<set-string-property name=\"" : "<set-message-property name=\"";
      indenter.printIndented(indent, intro + initializer.getPropertyName());
      indenter.print("\" key='");
      indenter.print(((PluginMessageBeanInitializer) initializer).getKey());

    } else
    {

      indenter.printIndented(indent, "<set-property name=\""
          + initializer.getPropertyName());
      String expression = ((PluginExpressionBeanInitializer) initializer).getExpression();
      if (expression == null)
        expression = "";

      if (expression.length() > 10)
      {
        indenter.println("\">");
        indenter.printlnIndented(indent + 1, expression);
        indenter.printlnIndented(indent, "</set-property>");
        return;
      }
      indenter.print("\" expression='");

      indenter.print(expression);

    }
    indenter.println("'/>");
  }

  /**
   * @param component
   * @param writer
   * @param i
   */
  private static void writePropertySpecifications(
      PluginComponentSpecification component,
      Writer writer,
      int i)
  {
    IndentingWriter indenter = checkWriter(writer);

    int DTDVersion = XMLUtil.getDTDVersion(component.getPublicId());
    if (DTDVersion < XMLUtil.DTD_3_0
        || component.getPropertySpecificationNames().isEmpty())
      return;
    //TODO does nothing!
  }

  public static void writeAssets(
      IComponentSpecification component,
      Writer writer,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    List assetNames = component.getAssetNames();
    if (assetNames != null && !assetNames.isEmpty())
    {

      indenter.println();
      Iterator names = new TreeSet(assetNames).iterator();

      while (names.hasNext())
      {
        String assetName = (String) names.next();
        IAssetSpecification asset = component.getAsset(assetName);
        indenter.printIndented(indent, "<");
        AssetType type = asset.getType();
        if (type == AssetType.PRIVATE)
        {
          indenter.print("private-asset name=\"" + assetName);
          indenter.print("\" resource-path=\"" + asset.getPath());
          indenter.println("\"/>");
          return;
        } else if (type == AssetType.EXTERNAL)
        {
          indenter.print("external-asset name=\"" + assetName);
          indenter.print("\" URL=\"" + asset.getPath());
          indenter.println("\"/>");
          return;
        } else if (type == AssetType.CONTEXT)
        {
          indenter.print("context-asset name=\"" + assetName);
          indenter.print("\" path=\"" + asset.getPath());
          indenter.println("\"/>");
          return;
        }
      }
    }
  }

  public static void writeReservedParameters(
      IComponentSpecification component,
      Writer writer,
      int indent)
  {

    IndentingWriter indenter = checkWriter(writer);

    if (component.isPageSpecification())
      return;

    PluginComponentSpecification pcomponent = (PluginComponentSpecification) component;
    Set names = pcomponent.getReservedParameterNames();
    Map parameterMap = pcomponent.getParameterMap();
    if (names != null && !names.isEmpty())
    {

      Collection reservedParameters = (Collection) (((HashSet) names).clone());

      if (parameterMap != null && !parameterMap.isEmpty())
      {

        reservedParameters.removeAll(parameterMap.keySet());

      }
      if (!reservedParameters.isEmpty())
      {

        indenter.println();
        Iterator reservedInformals = reservedParameters.iterator();

        while (reservedInformals.hasNext())
        {

          indenter.printIndented(indent, "<reserved-parameter name=\"");
          indenter.print(reservedInformals.next());
          indenter.print("\"/>");
        }
      }
    }
  }

  public static void writeComponentParameters(
      IComponentSpecification component,
      Writer writer,
      String publicId,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    if (component.isPageSpecification())
      return;

    Collection parms = component.getParameterNames();
    if (!parms.isEmpty())
    {
      Iterator parameterNames = parms.iterator();

      while (parameterNames.hasNext())
      {
        String paramName = (String) parameterNames.next();
        IParameterSpecification parameter = component.getParameter(paramName);
        writeParameter(paramName, parameter, indenter, indent, publicId);
      }
    }
  }

  public static void writeParameter(
      String name,
      IParameterSpecification parameter,
      Writer writer,
      int indent,
      String publicId)
  {
    writeParameter(name, parameter, writer, indent, publicId, true);
  }

  public static void writeParameter(
      String name,
      IParameterSpecification parameter,
      Writer writer,
      int indent,
      String publicId,
      boolean nextLine)
  {
    IndentingWriter indenter = checkWriter(writer);
    if (nextLine)
      indenter.println();

    boolean isDTD13 = XMLUtil.getDTDVersion(publicId) == XMLUtil.DTD_1_3;
    indenter.printlnIndented(indent, "<parameter");
    indenter.printIndented(indent + 1, "name=\"" + name);
    indenter.println("\"");
    String temp = isDTD13 ? "java-type=\"" : "type=\"";
    indenter.printIndented(indent + 1, temp + parameter.getType());
    indenter.println("\"");

    String propertyName = parameter.getPropertyName();

    if (propertyName != null && !"".equals(propertyName) && !propertyName.equals(name))
    {

      indenter
          .printIndented(indent + 1, "property-name=\"" + parameter.getPropertyName());
      indenter.println("\"");

    }

    indenter.printIndented(indent + 1, "direction=\"");
    Direction direction = parameter.getDirection();
    String useDirection = "";
    if (direction != null)
    {
      if (direction == Direction.AUTO)
        useDirection = "auto";
      else if (direction == Direction.CUSTOM)
        useDirection = "custom";
      else if (direction == Direction.FORM)
        useDirection = "form";
      else if (direction == Direction.IN)
        useDirection = "in";
    }

    indenter.print(useDirection);
    indenter.println("\"");

    indenter.printIndented(indent + 1, "required=\""
        + (parameter.isRequired() ? "yes" : "no"));
    indenter.print("\"");

    String description = parameter.getDescription();

    if (description == null || "".equals(description.trim()))
    {
      indenter.println("/>");
    } else
    {
      indenter.println(">");
      XMLUtil.writeDescription(indenter, indent + 1, description, false);
      indenter.printlnIndented(indent, "</parameter>");
    }
  }

  /**
   * Need to do some funky stuff here to ensure "copy-of" components are written
   * AFTER thier parents.
   */
  public static void writeContainedComponents(
      IComponentSpecification component,
      Writer writer,
      int indent,
      String publicId)
  {
    IndentingWriter indenter = checkWriter(writer);

    List componentIds = component.getComponentIds();
    if (componentIds == null || componentIds.isEmpty())
      return;

    HashMap nonCopyOfs = new HashMap();
    HashMap copyOfMap = new HashMap();

    IContainedComponent currentComponent;

    for (int i = 0; i < componentIds.size(); i++)
    {
      String containedId = (String) componentIds.get(i);
      currentComponent = component.getComponent(containedId);
      String copyOf = currentComponent.getCopyOf();

      if (copyOf == null || "".equals(copyOf.trim()))
      {
        nonCopyOfs.put(containedId, currentComponent);
      } else
      {
        if (!copyOfMap.containsKey(copyOf))
          copyOfMap.put(copyOf, new ArrayList());

        ArrayList listForCopyOf = (ArrayList) copyOfMap.get(copyOf);
        listForCopyOf.add(containedId);
      }
    }

    Iterator iter = new TreeSet(nonCopyOfs.keySet()).iterator();

    if (copyOfMap.isEmpty())
    {
      while (iter.hasNext())
      {
        String containedName = (String) iter.next();
        currentComponent = component.getComponent(containedName);
        writeContainedComponent(
            currentComponent,
            containedName,
            indenter,
            indent,
            publicId);
      }

    } else
    {
      while (iter.hasNext())
      {
        indenter.println();
        String containedName = (String) iter.next();
        currentComponent = component.getComponent(containedName);
        writeContainedComponent(
            currentComponent,
            containedName,
            indenter,
            indent,
            publicId);

        if (copyOfMap.containsKey(containedName))
        {
          ArrayList listForCopyOf = (ArrayList) copyOfMap.get(containedName);

          if (listForCopyOf == null | listForCopyOf.isEmpty())
            continue;

          Iterator copies = listForCopyOf.iterator();
          while (copies.hasNext())
          {
            indenter.println();
            String copyOfName = (String) copies.next();
            currentComponent = component.getComponent(copyOfName);
            writeContainedComponent(
                currentComponent,
                copyOfName,
                indenter,
                indent,
                publicId);
          }
          copyOfMap.remove(containedName);
        }
        if (iter.hasNext())
          indenter.println();

      }
      if (!copyOfMap.isEmpty())
      {
        Iterator leftovers = new TreeSet(copyOfMap.keySet()).iterator();
        while (leftovers.hasNext())
        {
          ArrayList leftoverIds = (ArrayList) copyOfMap.get(leftovers.next());
          if (leftoverIds == null || leftoverIds.isEmpty())
            continue;

          Iterator leftoverIter = leftoverIds.iterator();
          while (leftoverIter.hasNext())
          {
            indenter.println();
            String copyOfName = (String) leftoverIter.next();
            currentComponent = component.getComponent(copyOfName);
            writeContainedComponent(
                currentComponent,
                copyOfName,
                indenter,
                indent,
                publicId);
          }
        }
      }
    }
  }

  public static void writeContainedComponent(
      IContainedComponent contained,
      String name,
      Writer writer,
      int indent,
      String publicId)
  {
    writeContainedComponent(contained, name, writer, indent, publicId, true);
  }

  public static void writeContainedComponent(
      IContainedComponent contained,
      String name,
      Writer writer,
      int indent,
      String publicId,
      boolean nextLine)
  {
    IndentingWriter indenter = checkWriter(writer);

    if (nextLine)
      indenter.println();

    indenter.printIndented(indent, "<component id=\"" + name);

    if (contained.getCopyOf() != null)
    {
      indenter.print("\" copy-of=\"" + contained.getCopyOf());
    } else
    {
      indenter.print("\" type=\"" + contained.getType());
    }
    indenter.print("\"");
    Collection bns = contained.getBindingNames();
    if (bns != null)
    {
      if (bns.isEmpty())
      {
        indenter.println("/>");
        return;

      } else
      {
        indenter.println(">");
        Iterator bindingNames = new TreeSet(bns).iterator();
        while (bindingNames.hasNext())
        {
          String bindingName = (String) bindingNames.next();
          IBindingSpecification binding = contained.getBinding(bindingName);
          writeBinding(bindingName, binding, indenter, indent + 1, publicId);
          indenter.println();
        }
      }
    }
    indenter.printIndented(indent, "</component>");
  }

  public static void writeBinding(
      String name,
      IBindingSpecification binding,
      Writer writer,
      int indent,
      String publicId)
  {
    IndentingWriter indenter = checkWriter(writer);

    int currentDTD = XMLUtil.getDTDVersion(publicId);
    if (currentDTD == XMLUtil.UNKNOWN_DTD)
      currentDTD = XMLUtil.DTD_3_0;
    boolean isDTD13 = currentDTD == XMLUtil.DTD_1_3;
    boolean isDTD30OrBetter = currentDTD >= XMLUtil.DTD_3_0;

    char quot = '"';

    indenter.printIndented(indent, "<");

    BindingType type = binding.getType();

    if (type == BindingType.FIELD)
    {

      if (isDTD13)
      {
        indenter.print("field-binding name=\"" + name);
        indenter.print("\" field-name=\"");
      } else
      {
        //convert to an expression binding
        indenter.print("binding name=\"" + name);
        String value = binding.getValue();
        String lhs = value;
        String rhs = "";
        int index = value.lastIndexOf('.');
        if (index > 0 && index < value.length() - 1)
        {
          lhs = value.substring(0, index - 1);
          rhs = value.substring(index + 1);
        }
        if (lhs.indexOf('.') == 0)
          lhs = "java.lang." + lhs;
        indenter.print("\" expression='");
        indenter.print("@" + lhs + "@" + rhs);
        indenter.print("'/>");
        return;
      }

    } else if (type == BindingType.INHERITED)
    {

      indenter.print("inherited-binding name=\"" + name);
      indenter.print("\" parameter-name=\"");

    } else if (type == BindingType.STATIC)
    {

      indenter.print("static-binding name=\"" + name);
      indenter.print("\">");
      indenter.print(binding.getValue());
      indenter.print("</static-binding>");
      return;

    } else if (type == BindingType.DYNAMIC)
    {

      String value = convert(binding.getValue());
      //does the expression contain a double quote?
      if (value.indexOf('"') >= 0)
        // use single quote
        quot = '\'';

      // reset back to double quote if the expression contains a single quote!
      // not ideal but its all we can do.
      if (value.indexOf('\'') >= 0)
        quot = '"';

      indenter.print("binding name=\"" + name);

      indenter.print("\" expression=" + quot);
      indenter.print(convert(binding.getValue()));
      indenter.print(quot + "/>");
      return;

    } else if (type == BindingType.STRING)
    {
      if (isDTD13)
      {
        indenter.print("string-binding name=\"" + name);
        indenter.print("\" key=\"");
      } else
      {
        indenter.print("message-binding name=\"" + name);
        indenter.print("\" key=\"");
      }

    } else if (type == BindingType.LISTENER)
    {
      indenter.print("listener-binding name=\"" + name);
      PluginListenerBindingSpecification pbinding = (PluginListenerBindingSpecification) binding;
      indenter.println("\" language=\"" + pbinding.getLanguage() + "\">");
      indenter.printlnIndented(indent + 1, "<![CDATA[");
      indenter.printlnIndented(indent + 2, pbinding.getScript());
      indenter.printlnIndented(indent + 1, "]]>");
      indenter.printlnIndented(indent, "</listener-binding>");
      return;
    }
    indenter.print(binding.getValue());
    indenter.print("\"/>");
  }

  private static String convert(String string)
  {
    if (string == null && string.trim().length() == 0)
      return "";

    StringBuffer buffer = new StringBuffer();
    final char[] characters = string.toCharArray();
    for (int i = 0; i < characters.length; i++)
    {
      switch (characters[i])
      {
        case '<' :
          buffer.append("&lt;");
          break;

        case '>' :
          buffer.append("&gt;");
          break;

        case '&' :
          buffer.append("&amp;");
          break;

        default :
          buffer.append(characters[i]);
          break;
      }
    }
    return buffer.toString();
  }

  public static void writeLibrarySpecification(
      Writer writer,
      PluginLibrarySpecification library,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    writeXMLHeader(library.getPublicId(), "library-specification", indenter);

    indenter.println();

    indenter.println("<library-specification>");
    writeLibraryContents(indenter, library, indent);
    indenter.println("</library-specification>");
  }

  private static void writeLibraryContents(
      Writer writer,
      PluginLibrarySpecification library,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    writeDescription(indenter, indent + 1, library.getDescription());
    writeLibraryPages(library.getPages(), indenter, indent + 1);
    writeLibraryComponents(library.getComponents(), indenter, indent + 1);
    writeLibraryServices(library.getServices(), indenter, indent + 1);
    writeLibraries(library.getLibraries(), indenter, indent + 1);
    writeExtensions(library.getExtensions(), indenter, indent + 1);

    indenter.println();
  }

  public static void writeApplicationSpecification(
      Writer writer,
      PluginApplicationSpecification application,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    XMLUtil.writeXMLHeader(application.getPublicId(), "application", indenter);

    indenter.println();

    indenter.print("<application");
    if (application.getName() != null)
    {
      indenter.print(" name=\"");
      indenter.print(application.getName());
      indenter.print("\"");
    }
    indenter.print(" engine-class=\"");
    indenter.print(application.getEngineClassName());
    indenter.println("\" >");

    writeLibraryContents(indenter, (PluginLibrarySpecification) application, indent);

    indenter.println("</application>");
  }

  public static void writeWebDOTXML(
      String servletName,
      String publicId,
      boolean writeFilter,
      Writer writer)
  {
    IndentingWriter indenter = checkWriter(writer);

    int dtdId = XMLUtil.getDTDVersion(publicId);
    if (dtdId != XMLUtil.UNKNOWN_DTD)
    {

      XMLUtil.writeXMLHeader(publicId, "web-app", indenter);
      indenter.println();
      indenter.println("<web-app>");
    } else
    {
      indenter.println("<?xml version=\"1.0\"?>");
      indenter.println("<web-app xmlns=\"http://java.sun.com/xml/ns/j2ee\"");
      indenter.printlnIndented(
          1,
          "xmlns:xsi=\"http://www.w3.org/TR/xmlschema-1/\"");
      indenter
          .printIndented(
              1,
              "xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\" version=\"2.4\">");
      indenter.println();
    }
    indenter.printlnIndented(1, "<display-name>" + servletName + "</display-name>");

    if (dtdId >= XMLUtil.DTD_SERVLET_2_3 && writeFilter)
      writeTapestryFilter("org.apache.tapestry.RedirectFilter", writer, 1); //TODO
    // add filter classname to properties file
    writeServlet(servletName, indenter, 1);
    writeServletMapping(servletName, indenter, 1);
    indenter.println("</web-app>");

  }

  public static void writeServlet(String servletName, Writer writer, int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    indenter.printlnIndented(indent, "<servlet>");
    indenter.printlnIndented(indent + 1, "<servlet-name>" + servletName
        + "</servlet-name>");
    indenter.printlnIndented(
        indent + 1,
        "<servlet-class>org.apache.tapestry.ApplicationServlet</servlet-class>");
    indenter.printlnIndented(indent + 1, "<load-on-startup>1</load-on-startup>");
    indenter.printlnIndented(indent, "</servlet>");
  }

  public static void writeServletMapping(String servletName, Writer writer, int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    indenter.printlnIndented(indent, "<servlet-mapping>");
    indenter.printlnIndented(indent + 1, "<servlet-name>" + servletName
        + "</servlet-name>");
    indenter.printlnIndented(indent + 1, "<url-pattern>/app</url-pattern>");
    indenter.printlnIndented(indent, "</servlet-mapping>");
  }

  public static void writeTapestryFilter(String filterClassname, Writer writer, int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    indenter.printlnIndented(indent, "<filter>");
    indenter.printlnIndented(indent + 1, "<filter-name>redirect</filter-name>");
    indenter.printlnIndented(indent + 1, "<filter-class>" + filterClassname
        + "</filter-class>");
    indenter.printlnIndented(indent, "</filter>");
    indenter.printlnIndented(indent, "<filter-mapping>");
    indenter.printlnIndented(indent + 1, "<filter-name>redirect</filter-name>");
    indenter.printlnIndented(indent + 1, "<url-pattern>/</url-pattern>");
    indenter.printlnIndented(indent, "</filter-mapping>");
  }

  /**
   * Method writeConfiguration.
   * 
   * @param writer
   * @param i
   */
  public static void writeExtensionConfiguration(
      Writer writer,
      PluginExtensionConfiguration config,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    indenter.printIndented(indent, "<configure property-name=\"");
    indenter.print(config.getIdentifier());
    indenter.print("\" type=\"");
    indenter.print(config.classToString.get(config.fType));
    indenter.println("\">");

    indenter.printlnIndented(indent + 1, config.fValueObject.toString());

    indenter.printlnIndented(indent, "</configure>");

  }

  public static void writeExtensionSpecification(
      Writer writer,
      PluginExtensionSpecification spec,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    indenter.printIndented(indent, "<extension name=\"");
    indenter.print(spec.getIdentifier());
    indenter.print("\" class=\"");
    indenter.print(spec.getClassName());
    indenter.print("\"");

    if (spec.isImmediate())
      indenter.print(" immediate=\"yes\"");

    List propertyNames = spec.getPropertyNames();
    Map configurations = spec.getConfiguration();

    boolean hasProperties = propertyNames != null && !propertyNames.isEmpty();
    boolean hasConfiguration = configurations != null && !configurations.isEmpty();

    if (hasProperties || hasConfiguration)
    {

      indenter.print(">");

      if (hasProperties)
        writeProperties(spec, indenter, indent + 1);

      if (hasConfiguration)
      {
        if (configurations != null && !configurations.isEmpty())
        {
          for (Iterator iter = configurations.keySet().iterator(); iter.hasNext();)
          {
            String propertyName = (String) iter.next();
            PluginExtensionConfiguration config = (PluginExtensionConfiguration) configurations
                .get(propertyName);
            writeExtensionConfiguration(indenter, config, indent + 1);
          }
        }
      }
      indenter.printIndented(indent, "</extension>");
    } else
    {
      indenter.print("/>");
    }
  }

  public static void writeDescription(Writer writer, int indent, String description)
  {
    writeDescription(writer, indent, description, true);
  }

  public static void writeDescription(
      Writer writer,
      int indent,
      String description,
      boolean nextLine)
  {
    IndentingWriter indenter = checkWriter(writer);

    if (description != null && !"".equals(description.trim()))
    {
      if (nextLine)
        indenter.println();

      boolean tooLong = description.length() > 40;
      boolean singleLine = description.indexOf("\r") <= 0
          && description.indexOf("\n") <= 0;
      indenter.printIndented(indent, "<description>");
      if (singleLine && !tooLong)
      {
        indenter.print("<![CDATA[   " + description + "   ]]>");
        indenter.println("</description>");
      } else if (singleLine && tooLong)
      {
        indenter.println();
        indenter.printlnIndented(indent + 1, "<![CDATA[   " + description + "   ]]>");
        indenter.printlnIndented(indent, "</description>");
      } else
      {
        indenter.println();
        indenter.println("<![CDATA[");
        writeMultiLine(indenter, description);
        indenter.println("]]>");
        indenter.printlnIndented(indent, "</description>");
      };
    }
  }

  public static void writeExtensions(Map extensions, Writer writer, int indent)
  {
    IndentingWriter indenter = checkWriter(writer);

    if (extensions != null && !extensions.isEmpty())
    {

      indenter.println();

      for (Iterator iter = new TreeSet(extensions.keySet()).iterator(); iter.hasNext();)
      {
        String name = (String) iter.next();

        PluginExtensionSpecification spec = (PluginExtensionSpecification) extensions
            .get(name);

        writeSpecification(indenter, spec, indent);
      }

    }

  }

  public static void writeProperties(
      IPropertyHolder propertyHolder,
      Writer writer,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    Collection properties = propertyHolder.getPropertyNames();
    if (properties != null)
    {
      Iterator propertyNames = new TreeSet(properties).iterator();
      while (propertyNames.hasNext())
      {
        String propertyName = (String) propertyNames.next();
        writeProperty(
            propertyName,
            propertyHolder.getProperty(propertyName),
            indenter,
            indent);
      }
    }
  }

  public static void writeLibraryPages(Map pageMap, Writer writer, int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    if (pageMap != null)
    {

      Iterator pageNames = new TreeSet(pageMap.keySet()).iterator();
      if (pageNames.hasNext())
        indenter.println();

      while (pageNames.hasNext())
      {

        String pname = (String) pageNames.next();
        String ppath = (String) pageMap.get(pname);

        writeLibraryPage(indent, indenter, pname, ppath);
        indenter.println();
      }
    }
  }
  
 

  public static void writeLibraryPage(int indent, IndentingWriter indenter, String pname, String ppath)
  {
    indenter.printIndented(indent, "<page name=\"" + pname);
    indenter.print("\" specification-path=\"");
    indenter.print(ppath);
    indenter.print("\"/>");
  }

  public static void writeLibraryComponents(Map componentMap, Writer writer, int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    if (componentMap != null && !componentMap.isEmpty())
    {

      Iterator componentAliases = new TreeSet(componentMap.keySet()).iterator();
      if (componentAliases.hasNext())
        indenter.println();

      while (componentAliases.hasNext())
      {

        String id = (String) componentAliases.next();
        Object resourcePath = componentMap.get(id);
        writeLibraryComponent(indent, indenter, id, resourcePath);
      }
    }
  }

  public static void writeLibraryComponent(int indent, IndentingWriter indenter, String id, Object resourcePath)
  {
    indenter.printIndented(indent, "<component-type type=\"");
    indenter.print(id);
    indenter.print("\" specification-path=\"");       
    indenter.print(resourcePath);
    indenter.println("\"/>");
  }

  public static void writeLibraries(Map libraryMap, Writer writer, int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    if (libraryMap != null && !libraryMap.isEmpty())
    {

      Iterator libraryNames = new TreeSet(libraryMap.keySet()).iterator();
      if (libraryNames.hasNext())
        indenter.println();

      while (libraryNames.hasNext())
      {

        String name = (String) libraryNames.next();
        writeLibrary(name, (String) libraryMap.get(name), indenter, indent);
      }
    }

  }

  public static void writeLibrary(
      String name,
      String speclocation,
      Writer writer,
      int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    indenter.printIndented(indent, "<library id=\"");
    indenter.print(name);
    indenter.print("\" specification-path=\"");
    indenter.print(speclocation);
    indenter.println("\" />");
  }

  static public void writeMultiLine(Writer writer, String message)
  {
    IndentingWriter indenter = checkWriter(writer);
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        new ByteArrayInputStream(message.getBytes())));
    try
    {
      String line = reader.readLine();
      while (line != null)
      {
        indenter.println(line);
        line = reader.readLine();
      }
    } catch (IOException e)
    {}
  }

  static public void writeProperty(String name, String value, Writer writer, int indent)
  {
    writeProperty(name, value, writer, indent, true);
  }

  static public void writeProperty(
      String name,
      String value,
      Writer writer,
      int indent,
      boolean nextLine)
  {
    IndentingWriter indenter = checkWriter(writer);
    if (nextLine)
      indenter.println();
    indenter.printIndented(indent, "<property name=\"" + name);
    if (value == null || "".equals(value))
    {
      indenter.println("\"/>");
    } else
    {
      indenter.println("\">");
      indenter.printlnIndented(indent + 1, value);
      indenter.printlnIndented(indent, "</property>");
    }
  }

  public static void writeXMLHeader(String publicId, String rootTag, Writer writer)
  {
    IndentingWriter indenter = checkWriter(writer);

    indenter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    indenter.print("<!DOCTYPE ");

    indenter.println(rootTag);

    indenter.print("      PUBLIC \"");
    indenter.print(publicId);
    indenter.println("\"");

    final int DTDVersion = XMLUtil.getDTDVersion(publicId);

    switch (DTDVersion)
    {
      case XMLUtil.DTD_1_1 :
        indenter.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_1.dtd\">");
        break;

      case XMLUtil.DTD_1_2 :
        indenter.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_2.dtd\">");
        break;

      case XMLUtil.DTD_1_3 :
        indenter.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_3.dtd\">");
        break;

      case XMLUtil.DTD_3_0 :
        indenter
            .println("      \"http://jakarta.apache.org/tapestry/dtd/Tapestry_3_0.dtd\">");
        break;

      case XMLUtil.DTD_SERVLET_2_2 :
        indenter.println("      \"http://java.sun.com/j2ee/dtds/web-app_2_2.dtd\">");
        break;

      case XMLUtil.DTD_SERVLET_2_3 :
        indenter.println("      \"http://java.sun.com/dtd/web-app_2_3.dtd\">");
        break;

      default :
        Assert.isTrue(false, "unknown DTD: " + publicId);
        break;
    }

    indenter.println(TapestryCore.getString("TAPESTRY.xmlComment"));
  }

  public static void writeLibraryServices(Map serviceMap, Writer writer, int indent)
  {
    IndentingWriter indenter = checkWriter(writer);
    if (serviceMap != null && !serviceMap.isEmpty())
    {
      Iterator serviceNames = new TreeSet(serviceMap.keySet()).iterator();
      if (serviceNames.hasNext())
        indenter.println();

      while (serviceNames.hasNext())
      {
        String serviceName = (String) serviceNames.next();
        String classname = (String) serviceMap.get(serviceName);
        if (classname != null)
        {
          indenter.printIndented(indent, "<service name=\"");
          indenter.print(serviceName);
          indenter.print("\" class=\"");
          indenter.print(classname);
          indenter.println("\" />");
        }
      }
    }
  }

  /**
   * 
   * TODO fill in!
   * @param generatedContent
   * @return
   */
  public static String fomat(String content)
  { 
    return content;
  }

}