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
import java.io.PrintWriter;
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
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class XMLUtil
{

    static public final int UNKNOWN_DTD = 0;
    static public final int DTD_1_1 = 1;
    static public final int DTD_1_2 = 2;
    static public final int DTD_1_3 = 3;
    static public final int DTD_3_0 = 4;
    static public final int DTD_SERVLET_2_2 = 5;
    static public final int DTD_SERVLET_2_3 = 6;

    static public final int[] ALLOWED_SPEC_DTDS = new int[] { DTD_1_3, DTD_3_0 };
    static public final String SPEC_DTD_ERROR_KEY = "error-invalid-spec-public-id";
    static public final int[] ALLOWED_SERVLET_DTDS = new int[] { DTD_SERVLET_2_2, DTD_SERVLET_2_3 };
    static public final String SERVLET_DTD_ERROR_KEY = "error-invalid-servlet-public-id";

    static public int getDTDVersion(String publicId)
    {

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

    public static void writeSpecification(PrintWriter writer, BaseSpecification specification)
    {
        writeSpecification(writer, specification, 0);
    }

    public static void writeSpecification(PrintWriter writer, BaseSpecification specification, int indent)
    {
        switch (specification.getSpecificationType())
        {
            case BaseSpecification.LIBRARY_SPEC :
                writeLibrarySpecification(writer, (PluginLibrarySpecification) specification, indent);
                break;

            case BaseSpecification.APPLICATION_SPEC :
                writeApplicationSpecification(writer, (PluginApplicationSpecification) specification, indent);
                break;
            case BaseSpecification.EXTENSION_CONFIGURATION :
                writeExtensionConfiguration(writer, (PluginExtensionConfiguration) specification, indent);
                break;

            case BaseSpecification.EXTENSION_SPEC :
                writeExtensionSpecification(writer, (PluginExtensionSpecification) specification, indent);
                break;

            case BaseSpecification.COMPONENT_SPEC :
                writeComponentSpecification(writer, (PluginComponentSpecification) specification, indent);
                break;
            default :
                throw new IllegalStateException("unknown spec type!");

        }
    }

    public static void writeComponentSpecification(
        PrintWriter writer,
        PluginComponentSpecification component,
        int indent)
    {

        boolean isPage = component.isPageSpecification();
        String rootElement = isPage ? "page-specification" : "component-specification";

        writeXMLHeader(component.getPublicId(), rootElement, writer);

        writer.print("<" + rootElement + " class=\"");
        writer.print(component.getComponentClassName());
        writer.print("\"");
        if (!isPage)
        {
            writer.print(" allow-body=\"" + (component.getAllowBody() ? "yes" : "no") + "\"");
            writer.print(
                " allow-informal-parameters=\"" + (component.getAllowInformalParameters() ? "yes" : "no") + "\"");
        }
        writer.println(">");
        writeDescription(writer, indent + 1, component.getDescription());
        if (!isPage)
        {
            writeComponentParameters(component, writer, component.getPublicId(), indent + 1);
            writeReservedParameters(component, writer, indent + 1);
        }
        writeProperties((IPropertyHolder) component, writer, indent + 1);
        writeBeans(component, writer, indent + 1);
        writePropertySpecifications(component, writer, indent + 1);
        writeContainedComponents(component, writer, indent + 1, component.getPublicId());
        writeAssets(component, writer, indent + 1);

        writer.println("</" + rootElement + ">");
    }

    public static void writeComponentSpecificationHeader(
        PrintWriter writer,
        IComponentSpecification component,
        int indent)
    {

        boolean isPage = component.isPageSpecification();
        String rootElement = isPage ? "page-specification" : "component-specification";

        writer.println("<" + rootElement);
        Indenter.printIndented(writer, 1, "class=\"");
        writer.print(component.getComponentClassName());
        writer.print("\"");
        if (!isPage)
        {
            writer.println();
            Indenter.printlnIndented(writer, 1, "allow-body=\"" + (component.getAllowBody() ? "yes" : "no") + "\"");
            Indenter.printIndented(
                writer,
                1,
                "allow-informal-parameters=\"" + (component.getAllowInformalParameters() ? "yes" : "no") + "\"");
        }
        writer.println(">");
    }

    /**
     * @param component
     * @param writer
     * @param i
     */
    private static void writeBeans(PluginComponentSpecification component, PrintWriter writer, int indent)
    {
        Collection beanNames = component.getBeanNames();
        if (beanNames != null && !beanNames.isEmpty())
        {
            writer.println();
            Iterator names = new TreeSet(beanNames).iterator();

            while (names.hasNext())
            {
                String beanName = (String) names.next();
                PluginBeanSpecification bean = (PluginBeanSpecification) component.getBeanSpecification(beanName);
                Indenter.printIndented(writer, indent, "<bean name=\"" + beanName);
                writer.print("\" class=\"" + bean.getClassName());
                writer.print("\" lifecycle=\"");

                BeanLifecycle lifecycle = bean.getLifecycle();

                if (lifecycle == null || lifecycle.equals(BeanLifecycle.REQUEST))
                {
                    writer.print("request\"");

                } else if (lifecycle.equals(BeanLifecycle.NONE))
                {

                    writer.print("none\"");

                } else if (lifecycle.equals(BeanLifecycle.PAGE))
                {

                    writer.print("page\"");

                } else if (lifecycle.equals(BeanLifecycle.RENDER))
                {

                    writer.print("render\"");

                }
                String description = bean.getDescription();
                Collection inits = bean.getInitializers();

                boolean writeableDescription = description != null && !"".equals(description.trim());
                boolean writeableInitializers = inits != null && !inits.isEmpty();

                if (writeableDescription || writeableInitializers)
                {

                    writer.println(">");

                    if (writeableDescription)
                    {

                        XMLUtil.writeDescription(writer, indent + 1, description.trim(), false);
                    }

                    if (writeableInitializers)
                    {

                        Iterator initializers = inits.iterator();

                        while (initializers.hasNext())
                        {

                            IBeanInitializer initer = (IBeanInitializer) initializers.next();
                            writeBeanInitializer(component, initer, writer, indent + 1);
                        }
                    }
                } else
                {

                    writer.println("/>");
                    return;
                }
                Indenter.printlnIndented(writer, indent, "</bean>");
            }
        }
    }

    public static void writeBeanInitializer(
        IComponentSpecification spec,
        IBeanInitializer initializer,
        PrintWriter writer,
        int indent)
    {
        int DTDVersion = getDTDVersion(spec.getPublicId());

        if (initializer instanceof MessageBeanInitializer)
        {
            String intro = DTDVersion < DTD_3_0 ? "<set-string-property name=\"" : "<set-message-property name=\"";
            Indenter.printIndented(writer, indent, intro + initializer.getPropertyName());
            writer.print("\" key='");
            writer.print(((PluginMessageBeanInitializer) initializer).getKey());

        } else
        {

            Indenter.printIndented(writer, indent, "<set-property name=\"" + initializer.getPropertyName());
            String expression = ((PluginExpressionBeanInitializer) initializer).getExpression();
            if (expression == null)
                expression = "";

            if (expression.length() > 10)
            {
                writer.println("\">");
                Indenter.printlnIndented(writer, indent + 1, expression);
                Indenter.printlnIndented(writer, indent, "</set-property>");
                return;
            }
            writer.print("\" expression='");

            writer.print(expression);

        }
        writer.println("'/>");
    }

    /**
     * @param component
     * @param writer
     * @param i
     */
    private static void writePropertySpecifications(PluginComponentSpecification component, PrintWriter writer, int i)
    {
        int DTDVersion = XMLUtil.getDTDVersion(component.getPublicId());
        if (DTDVersion < XMLUtil.DTD_3_0 || component.getPropertySpecificationNames().isEmpty())
            return;

    }

    public static void writeAssets(IComponentSpecification component, PrintWriter writer, int indent)
    {
        List assetNames = component.getAssetNames();
        if (assetNames != null && !assetNames.isEmpty())
        {

            writer.println();
            Iterator names = new TreeSet(assetNames).iterator();

            while (names.hasNext())
            {
                String assetName = (String) names.next();
                IAssetSpecification asset = component.getAsset(assetName);
                Indenter.printIndented(writer, indent, "<");
                AssetType type = asset.getType();
                if (type.equals(AssetType.PRIVATE))
                {
                    writer.print("private-asset name=\"" + assetName);
                    writer.print("\" resource-path=\"" + asset.getPath());
                    writer.println("\"/>");
                    return;
                } else if (type.equals(AssetType.EXTERNAL))
                {
                    writer.print("external-asset name=\"" + assetName);
                    writer.print("\" URL=\"" + asset.getPath());
                    writer.println("\"/>");
                    return;
                } else if (type.equals(AssetType.CONTEXT))
                {
                    writer.print("context-asset name=\"" + assetName);
                    writer.print("\" path=\"" + asset.getPath());
                    writer.println("\"/>");
                    return;
                }
            }
        }
    }

    public void writeBeans(Collection beanNames, PrintWriter writer, int indent)
    {}

    public static void writeReservedParameters(IComponentSpecification component, PrintWriter writer, int indent)
    {
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

                writer.println();
                Iterator reservedInformals = reservedParameters.iterator();

                while (reservedInformals.hasNext())
                {

                    Indenter.printIndented(writer, indent, "<reserved-parameter name=\"");
                    writer.print(reservedInformals.next());
                    writer.println("\"/>");
                }
            }
        }
    }

    public static void writeComponentParameters(
        IComponentSpecification component,
        PrintWriter writer,
        String publicId,
        int indent)
    {
        if (component.isPageSpecification())
            return;

        Collection parms = component.getParameterNames();
        if (!parms.isEmpty())
        {
            writer.println();
            Iterator parameterNames = parms.iterator();

            while (parameterNames.hasNext())
            {
                String paramName = (String) parameterNames.next();
                IParameterSpecification parameter = component.getParameter(paramName);
                writeParameter(paramName, parameter, writer, indent, publicId);
            }
        }
    }

    public static void writeParameter(
        String name,
        IParameterSpecification parameter,
        PrintWriter writer,
        int indent,
        String publicId)
    {
        boolean isDTD13 = XMLUtil.getDTDVersion(publicId) == XMLUtil.DTD_1_3;
        Indenter.printlnIndented(writer, indent, "<parameter");
        Indenter.printIndented(writer, indent + 1, "name=\"" + name);
        writer.println("\"");
        String temp = isDTD13 ? "java-type=\"" : "type=\"";
        Indenter.printIndented(writer, indent + 1, temp + parameter.getType());
        writer.println("\"");

        String propertyName = parameter.getPropertyName();

        if (propertyName != null && !"".equals(propertyName) && !propertyName.equals(name))
        {

            Indenter.printIndented(writer, indent + 1, "property-name=\"" + parameter.getPropertyName());
            writer.println("\"");

        }

        Indenter.printIndented(writer, indent + 1, "direction=\"");
        Direction direction = parameter.getDirection();
        String useDirection = "";
        if (direction != null)
        {
            if (direction.equals(Direction.AUTO))
                useDirection = "auto";
            else if (direction.equals(Direction.CUSTOM))
                useDirection = "custom";
            else if (direction.equals(Direction.FORM))
                useDirection = "form";
            else if (direction.equals(Direction.IN))
                useDirection = "in";
        }

        writer.print(useDirection);
        writer.println("\"");

        Indenter.printIndented(writer, indent + 1, "required=\"" + (parameter.isRequired() ? "yes" : "no"));
        writer.print("\"");

        String description = parameter.getDescription();

        if (description == null || "".equals(description.trim()))
        {
            writer.println("/>");
        } else
        {
            writer.println(">");
            XMLUtil.writeDescription(writer, indent + 1, description, false);
            Indenter.printlnIndented(writer, indent, "</parameter>");
        }
    }

    /** 
     * Need to do some funky stuff here to ensure "copy-of" components are written AFTER
     * thier parents.
     */
    public static void writeContainedComponents(
        IComponentSpecification component,
        PrintWriter writer,
        int indent,
        String publicId)
    {

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
                writer.println();
                String containedName = (String) iter.next();
                currentComponent = component.getComponent(containedName);
                writeContainedComponent(currentComponent, containedName, writer, indent, publicId);
            }

        } else
        {
            while (iter.hasNext())
            {
                writer.println();
                String containedName = (String) iter.next();
                currentComponent = component.getComponent(containedName);
                writeContainedComponent(currentComponent, containedName, writer, indent, publicId);

                if (copyOfMap.containsKey(containedName))
                {
                    ArrayList listForCopyOf = (ArrayList) copyOfMap.get(containedName);

                    if (listForCopyOf == null | listForCopyOf.isEmpty())
                        continue;

                    Iterator copies = listForCopyOf.iterator();
                    while (copies.hasNext())
                    {
                        writer.println();
                        String copyOfName = (String) copies.next();
                        currentComponent = component.getComponent(copyOfName);
                        writeContainedComponent(currentComponent, copyOfName, writer, indent, publicId);
                    }
                    copyOfMap.remove(containedName);
                }
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
                        writer.println();
                        String copyOfName = (String) leftoverIter.next();
                        currentComponent = component.getComponent(copyOfName);
                        writeContainedComponent(currentComponent, copyOfName, writer, indent, publicId);
                    }
                }
            }

        }
    }

    public static void writeContainedComponent(
        IContainedComponent contained,
        String name,
        PrintWriter writer,
        int indent,
        String publicId)
    {

        Indenter.printIndented(writer, indent, "<component id=\"" + name);

        if (contained.getCopyOf() != null)
        {
            writer.print("\" copy-of=\"" + contained.getCopyOf());
        } else
        {
            writer.print("\" type=\"" + contained.getType());
        }
        writer.print("\"");
        Collection bns = contained.getBindingNames();
        if (bns != null)
        {
            if (bns.isEmpty())
            {
                writer.println("/>");
                return;

            } else
            {
                writer.println(">");
                Iterator bindingNames = new TreeSet(bns).iterator();
                while (bindingNames.hasNext())
                {
                    String bindingName = (String) bindingNames.next();
                    IBindingSpecification binding = contained.getBinding(bindingName);
                    writeBinding(bindingName, binding, writer, indent + 1, publicId);
                }
            }
        }
        Indenter.printlnIndented(writer, indent, "</component>");
    }

    public static void writeBinding(
        String name,
        IBindingSpecification binding,
        PrintWriter writer,
        int indent,
        String publicId)
    {

        int currentDTD = XMLUtil.getDTDVersion(publicId);
        if (currentDTD == XMLUtil.UNKNOWN_DTD)
            currentDTD = XMLUtil.DTD_3_0;
        boolean isDTD13 = currentDTD == XMLUtil.DTD_1_3;
        boolean isDTD30OrBetter = currentDTD >= XMLUtil.DTD_3_0;

        Indenter.printIndented(writer, indent, "<");

        BindingType type = binding.getType();

        if (type.equals(BindingType.FIELD))
        {

            if (isDTD13)
            {
                writer.print("field-binding name=\"" + name);
                writer.print("\" field-name=\"");
            } else
            {
                //convert to an expression binding
                writer.print("binding name=\"" + name);
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
                writer.print("\" expression='");
                writer.print("@" + lhs + "@" + rhs);
                writer.println("'/>");
                return;
            }

        } else if (type.equals(BindingType.INHERITED))
        {

            writer.print("inherited-binding name=\"" + name);
            writer.print("\" parameter-name=\"");

        } else if (type.equals(BindingType.STATIC))
        {

            writer.print("static-binding name=\"" + name);
            writer.print("\">");
            writer.print(binding.getValue());
            writer.println("</static-binding>");
            return;

        } else if (type.equals(BindingType.DYNAMIC))
        {

            writer.print("binding name=\"" + name);

            writer.print("\" expression='");
            writer.print(convert(binding.getValue()));
            writer.println("'/>");
            return;

        } else if (type.equals(BindingType.STRING))
        {
            if (isDTD13)
            {
                writer.print("string-binding name=\"" + name);
                writer.print("\" key=\"");
            } else
            {
                writer.print("message-binding name=\"" + name);
                writer.print("\" key=\"");
            }

        } else if (type.equals(BindingType.LISTENER))
        {
            writer.print("listener-binding name=\"" + name);
            PluginListenerBindingSpecification pbinding = (PluginListenerBindingSpecification) binding;
            writer.println("\" language=\"" + pbinding.getLanguage() + "\">");
            Indenter.printlnIndented(writer, indent + 1, "<![CDATA[");
            Indenter.printlnIndented(writer, indent + 2, pbinding.getScript());
            Indenter.printlnIndented(writer, indent + 1, "]]>");
            Indenter.printlnIndented(writer, indent, "</listener-binding>");
            return;
        }
        writer.print(binding.getValue());
        writer.println("\"/>");
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

    public static void writeLibrarySpecification(PrintWriter writer, PluginLibrarySpecification library, int indent)
    {
        writeXMLHeader(library.getPublicId(), "library-specification", writer);

        writer.println();

        writer.println("<library-specification>");
        writeLibraryContents(writer, library, indent);
        writer.println("</library-specification>");
    }

    private static void writeLibraryContents(PrintWriter writer, PluginLibrarySpecification library, int indent)
    {
        writeDescription(writer, indent + 1, library.getDescription());
        writeLibraryPages(library.getPages(), writer, indent + 1);
        writeLibraryComponents(library.getComponents(), writer, indent + 1);
        writeLibraryServices(library.getServices(), writer, indent + 1);
        writeLibraries(library.getLibraries(), writer, indent + 1);
        writeExtensions(library.getExtensions(), writer, indent + 1);

        writer.println();
    }

    public static void writeApplicationSpecification(
        PrintWriter writer,
        PluginApplicationSpecification application,
        int indent)
    {

        XMLUtil.writeXMLHeader(application.getPublicId(), "application", writer);

        writer.println();

        writer.print("<application");
        if (application.getName() !=null) {           writer.print( " name=\"");
           writer.print(application.getName());
           writer.print("\"");
        }
        writer.print(" engine-class=\"");
        writer.print(application.getEngineClassName());
        writer.println("\" >");

        writeLibraryContents(writer, (PluginLibrarySpecification) application, indent);

        writer.println("</application>");
    }

    public static void writeWebDOTXML(String servletName, String publicId, PrintWriter writer)
    {
        XMLUtil.writeXMLHeader(publicId, "web-app", writer);

        writer.println();

        writer.println("<web-app>");
        Indenter.printlnIndented(writer, 1, "<display-name>" + servletName + "</display-name>");
        writeServlet(servletName, writer, 1);
        writeServletMapping(servletName, writer, 1);
        writer.println("</web-app>");

    }

    public static void writeServlet(String servletName, PrintWriter writer, int indent)
    {
        Indenter.printlnIndented(writer, indent, "<servlet>");
        Indenter.printlnIndented(writer, indent + 1, "<servlet-name>" + servletName + "</servlet-name>");
        Indenter.printlnIndented(
            writer,
            indent + 1,
            "<servlet-class>org.apache.tapestry.ApplicationServlet</servlet-class>");
        Indenter.printlnIndented(writer, indent + 1, "<load-on-startup>1</load-on-startup>");
        Indenter.printlnIndented(writer, indent, "</servlet>");
    }

    public static void writeServletMapping(String servletName, PrintWriter writer, int indent)
    {
        Indenter.printlnIndented(writer, indent, "<servlet-mapping>");
        Indenter.printlnIndented(writer, indent + 1, "<servlet-name>" + servletName + "</servlet-name>");
        Indenter.printlnIndented(writer, indent + 1, "<url-pattern>/app</url-pattern>");
        Indenter.printlnIndented(writer, indent, "</servlet-mapping>");
    }

    /**
      * Method writeConfiguration.
      * @param writer
      * @param i
      */
    public static void writeExtensionConfiguration(PrintWriter writer, PluginExtensionConfiguration config, int indent)
    {

        Indenter.printIndented(writer, indent, "<configure property-name=\"");
        writer.print(config.getIdentifier());
        writer.print("\" type=\"");
        writer.print(config.classToString.get(config.fType));
        writer.println("\">");

        Indenter.printlnIndented(writer, indent + 1, config.fValueObject.toString());

        Indenter.printlnIndented(writer, indent, "</configure>");

    }

    public static void writeExtensionSpecification(PrintWriter writer, PluginExtensionSpecification spec, int indent)
    {

        Indenter.printIndented(writer, indent, "<extension name=\"");
        writer.print(spec.getIdentifier());
        writer.print("\" class=\"");
        writer.print(spec.getClassName());
        writer.print("\"");

        if (spec.isImmediate())
            writer.print(" immediate=\"yes\"");

        List propertyNames = spec.getPropertyNames();
        Map configurations = spec.getConfiguration();

        boolean hasProperties = propertyNames != null && !propertyNames.isEmpty();
        boolean hasConfiguration = configurations != null && !configurations.isEmpty();

        if (hasProperties || hasConfiguration)
        {

            writer.println(">");

            if (hasProperties)
                writeProperties(spec, writer, indent + 1, false);

            if (hasConfiguration)
            {
                if (configurations != null && !configurations.isEmpty())
                {
                    for (Iterator iter = configurations.keySet().iterator(); iter.hasNext();)
                    {
                        String propertyName = (String) iter.next();
                        PluginExtensionConfiguration config =
                            (PluginExtensionConfiguration) configurations.get(propertyName);
                        writeExtensionConfiguration(writer, config, indent + 1);
                    }
                }
            }
            Indenter.printlnIndented(writer, indent, "</extension>");
        } else
        {
            writer.println("/>");
        }
    }

    public static void writeDescription(PrintWriter writer, int indent, String description)
    {
        writeDescription(writer, indent, description, true);
    }

    public static void writeDescription(PrintWriter writer, int indent, String description, boolean nextLine)
    {

        if (description != null && !"".equals(description.trim()))
        {
            if (nextLine)
                writer.println();

            boolean tooLong = description.length() > 40;
            boolean singleLine = description.indexOf("\r") <= 0 && description.indexOf("\n") <= 0;
            Indenter.printIndented(writer, indent, "<description>");
            if (singleLine && !tooLong)
            {
                writer.print("<![CDATA[   " + description + "   ]]>");
                writer.println("</description>");
            } else if (singleLine && tooLong)
            {
                writer.println();
                Indenter.printlnIndented(writer, indent + 1, "<![CDATA[   " + description + "   ]]>");
                Indenter.printlnIndented(writer, indent, "</description>");
            } else
            {
                writer.println();
                writer.println("<![CDATA[");
                writeMultiLine(writer, description);
                writer.println("]]>");
                Indenter.printlnIndented(writer, indent, "</description>");
            };
        }
    }

    public static void writeExtensions(Map extensions, PrintWriter writer, int indent)
    {

        if (extensions != null && !extensions.isEmpty())
        {

            writer.println();

            for (Iterator iter = new TreeSet(extensions.keySet()).iterator(); iter.hasNext();)
            {
                String name = (String) iter.next();

                PluginExtensionSpecification spec = (PluginExtensionSpecification) extensions.get(name);

                writeSpecification(writer, spec, indent);
            }

        }

    }

    public static void writeProperties(IPropertyHolder propertyHolder, PrintWriter writer, int indent)
    {

        writeProperties(propertyHolder, writer, indent, true);
    }

    public static void writeProperties(
        IPropertyHolder propertyHolder,
        PrintWriter writer,
        int indent,
        boolean nextLine)
    {
        Collection properties = propertyHolder.getPropertyNames();
        if (properties != null)
        {
            if (nextLine)
                writer.println();

            Iterator propertyNames = new TreeSet(properties).iterator();
            while (propertyNames.hasNext())
            {
                String propertyName = (String) propertyNames.next();
                writeProperty(propertyName, propertyHolder.getProperty(propertyName), writer, indent);
            }
        }
    }

    public static void writeLibraryPages(Map pageMap, PrintWriter writer, int indent)
    {
        if (pageMap != null)
        {

            Iterator pageNames = new TreeSet(pageMap.keySet()).iterator();
            if (pageNames.hasNext())
                writer.println();

            while (pageNames.hasNext())
            {

                String pname = (String) pageNames.next();
                String ppath = (String) pageMap.get(pname);

                Indenter.printIndented(writer, indent, "<page name=\"" + pname);
                writer.print("\" specification-path=\"");
                writer.print(ppath);
                writer.println("\"/>");
            }
        }
    }

    public static void writeLibraryComponents(Map componentMap, PrintWriter writer, int indent)
    {
        if (componentMap != null && !componentMap.isEmpty())
        {

            Iterator componentAliases = new TreeSet(componentMap.keySet()).iterator();
            if (componentAliases.hasNext())
                writer.println();

            while (componentAliases.hasNext())
            {

                String alias = (String) componentAliases.next();
                Indenter.printIndented(writer, indent, "<component-alias type=\"");
                writer.print(alias);
                writer.print("\" specification-path=\"");
                writer.print(componentMap.get(alias));
                writer.println("\" />");
            }
        }
    }

    public static void writeLibraries(Map libraryMap, PrintWriter writer, int indent)
    {
        if (libraryMap != null && !libraryMap.isEmpty())
        {

            Iterator libraryNames = new TreeSet(libraryMap.keySet()).iterator();
            if (libraryNames.hasNext())
                writer.println();

            while (libraryNames.hasNext())
            {

                String name = (String) libraryNames.next();
                writeLibrary(name, (String) libraryMap.get(name), writer, indent);
            }
        }

    }

    public static void writeLibrary(String name, String speclocation, PrintWriter writer, int indent)
    {
        Indenter.printIndented(writer, indent, "<library id=\"");
        writer.print(name);
        writer.print("\" specification-path=\"");
        writer.print(speclocation);
        writer.println("\" />");
    }

    static public void writeMultiLine(PrintWriter writer, String message)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(message.getBytes())));
        try
        {
            String line = reader.readLine();
            while (line != null)
            {
                writer.println(line);
                line = reader.readLine();
            }
        } catch (IOException e)
        {}
    }

    static public void writeProperty(String name, String value, PrintWriter writer, int indent)
    {
        Indenter.printIndented(writer, indent, "<property name=\"" + name);
        if (value == null || "".equals(value))
        {
            writer.println("\"/>");
        } else
        {
            writer.println("\">");
            Indenter.printlnIndented(writer, indent + 1, value);
            Indenter.printlnIndented(writer, indent, "</property>");
        }
    }

    public static void writeXMLHeader(String publicId, String rootTag, PrintWriter writer)
    {

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.print("<!DOCTYPE ");

        writer.println(rootTag);

        writer.print("      PUBLIC \"");
        writer.print(publicId);
        writer.println("\"");

        final int DTDVersion = XMLUtil.getDTDVersion(publicId);

        switch (DTDVersion)
        {
            case XMLUtil.DTD_1_1 :
                writer.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_1.dtd\">");
                break;

            case XMLUtil.DTD_1_2 :
                writer.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_2.dtd\">");
                break;

            case XMLUtil.DTD_1_3 :
                writer.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_3.dtd\">");
                break;

            case XMLUtil.DTD_3_0 :
                writer.println("      \"http://jakarta.apache.org/tapestry/dtd/Tapestry_3_0.dtd\">");
                break;

            case XMLUtil.DTD_SERVLET_2_2 :
                writer.println("      \"http://java.sun.com/j2ee/dtds/web-app_2_2.dtd\">");
                break;

            case XMLUtil.DTD_SERVLET_2_3 :
                writer.println("      \"http://java.sun.com/dtd/web-app_2_3.dtd\">");
                break;

            default :
                Assert.isTrue(false, "unknown DTD: " + publicId);
                break;
        }

        writer.println(TapestryCore.getString("TAPESTRY.xmlComment"));
    }

    public static void writeLibraryServices(Map serviceMap, PrintWriter writer, int indent)
    {
        if (serviceMap != null && !serviceMap.isEmpty())
        {
            Iterator serviceNames = new TreeSet(serviceMap.keySet()).iterator();
            if (serviceNames.hasNext())
                writer.println();

            while (serviceNames.hasNext())
            {
                String serviceName = (String) serviceNames.next();
                String classname = (String) serviceMap.get(serviceName);
                if (classname != null)
                {
                    Indenter.printIndented(writer, indent, "<service name=\"");
                    writer.print(serviceName);
                    writer.print("\" class=\"");
                    writer.print(classname);
                    writer.println("\" />");
                }
            }
        }
    }

}
