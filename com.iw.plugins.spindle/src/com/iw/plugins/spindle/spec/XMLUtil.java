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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.util.IPropertyHolder;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.util.Indenter;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class XMLUtil {

  static public final int UNKNOWN_DTD = 0;
  static public final int DTD_1_1 = 1;
  static public final int DTD_1_2 = 2;
  static public final int DTD_1_3 = 3;

  static public int getDTDVersion(String publicId) {

    if (publicId.equals(SpecificationParser.TAPESTRY_DTD_1_1_PUBLIC_ID)) {
      return DTD_1_1;
    }
    if (publicId.equals(SpecificationParser.TAPESTRY_DTD_1_2_PUBLIC_ID)) {
      return DTD_1_2;
    }
    if (publicId.equals(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID)) {
      return DTD_1_3;
    }
    return UNKNOWN_DTD;
  }

  public static void writeDescription(PrintWriter writer, int indent, String description) {

    writeDescription(writer, indent, description, true);

  }

  public static void writeDescription(
    PrintWriter writer,
    int indent,
    String description,
    boolean nextLine) {

    if (description != null && !"".equals(description.trim())) {
      if (nextLine) {
        writer.println();
      }
      boolean tooLong = description.length() > 40;
      boolean singleLine = description.indexOf("\r") <= 0 && description.indexOf("\n") <= 0;
      Indenter.printIndented(writer, indent, "<description>");
      if (singleLine && !tooLong) {
        writer.print("<![CDATA[   " + description + "   ]]>");
        writer.println("</description>");
      } else if (singleLine && tooLong) {
        writer.println();
        Indenter.printlnIndented(writer, indent + 1, "<![CDATA[   " + description + "   ]]>");
        Indenter.printlnIndented(writer, indent, "</description>");
      } else {
        writer.println();
        writer.println("<![CDATA[");
        writeMultiLine(writer, description);
        writer.println("]]>");
        Indenter.printlnIndented(writer, indent, "</description>");
      };
    }
  }

  public static void writeExtensions(Map extensions, PrintWriter writer, int indent) {

    if (extensions != null && !extensions.isEmpty()) {

      writer.println();

      for (Iterator iter = new TreeSet(extensions.keySet()).iterator(); iter.hasNext();) {
        String name = (String) iter.next();

        PluginExtensionSpecification spec = (PluginExtensionSpecification) extensions.get(name);

        Indenter.printIndented(writer, indent, "<extension name=\"");
        writer.print(name);
        writer.print("\" class=\"");
        writer.print(spec.getClassName());
        writer.print("\"");

        if (spec.isImmediate()) {

          writer.print(" immediate=\"yes\"");
        }

        List propertyNames = spec.getPropertyNames();
        Map configurations = spec.getConfiguration();

        boolean hasProperties = propertyNames != null && !propertyNames.isEmpty();
        boolean hasConfiguration = configurations != null && !configurations.isEmpty();

        if (hasProperties || hasConfiguration) {

          writer.println(">");

          if (hasProperties) {

            writeProperties(spec, writer, indent + 1, false);

          }

          if (hasConfiguration) {

            spec.writeConfigurations(writer, indent + 1);
          }

          Indenter.printlnIndented(writer, indent, "</extension>");

        } else {

          writer.println("/>");

        }

      }

    }

  }

  public static void writeProperties(
    IPropertyHolder propertyHolder,
    PrintWriter writer,
    int indent) {

    writeProperties(propertyHolder, writer, indent, true);
  }

  public static void writeProperties(
    IPropertyHolder propertyHolder,
    PrintWriter writer,
    int indent,
    boolean nextLine) {
    Collection properties = propertyHolder.getPropertyNames();
    if (properties != null) {
      if (nextLine) {
        writer.println();
      }
      Iterator propertyNames = new TreeSet(properties).iterator();
      while (propertyNames.hasNext()) {
        String propertyName = (String) propertyNames.next();
        writeProperty(propertyName, propertyHolder.getProperty(propertyName), writer, indent);
      }
    }
  }

  public static void writeLibraryPages(Map pageMap, PrintWriter writer, int indent) {
    if (pageMap != null) {

      Iterator pageNames = new TreeSet(pageMap.keySet()).iterator();
      if (pageNames.hasNext()) {

        writer.println();
      }

      while (pageNames.hasNext()) {

        String pname = (String) pageNames.next();
        String ppath = (String) pageMap.get(pname);

        Indenter.printIndented(writer, indent, "<page name=\"" + pname);
        writer.print("\" specification-path=\"");
        writer.print(ppath);
        writer.println("\"/>");
      }
    }
  }

  public static void writeLibraryComponents(Map componentMap, PrintWriter writer, int indent) {
    if (componentMap != null && !componentMap.isEmpty()) {

      Iterator componentAliases = new TreeSet(componentMap.keySet()).iterator();
      if (componentAliases.hasNext()) {

        writer.println();
      }
      while (componentAliases.hasNext()) {

        String alias = (String) componentAliases.next();
        Indenter.printIndented(writer, indent, "<component-alias type=\"");
        writer.print(alias);
        writer.print("\" specification-path=\"");
        writer.print(componentMap.get(alias));
        writer.println("\" />");
      }
    }
  }

  public static void writeLibraries(Map libraryMap, PrintWriter writer, int indent) {
    if (libraryMap != null && !libraryMap.isEmpty()) {

      Iterator libraryNames = new TreeSet(libraryMap.keySet()).iterator();
      if (libraryNames.hasNext()) {

        writer.println();

      }
      while (libraryNames.hasNext()) {

        String name = (String) libraryNames.next();
        Indenter.printIndented(writer, indent, "<library id=\"");
        writer.print(name);
        writer.print("\" specification-path=\"");
        writer.print(libraryMap.get(name));
        writer.println("\" />");
      }
    }

  }

  static public void writeMultiLine(PrintWriter writer, String message) {
    BufferedReader reader =
      new BufferedReader(new InputStreamReader(new ByteArrayInputStream(message.getBytes())));
    try {
      String line = reader.readLine();
      while (line != null) {
        writer.println(line);
        line = reader.readLine();
      }
    } catch (IOException e) {
    }
  }

  static public void writeProperty(String name, String value, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<property name=\"" + name);
    if (value == null || "".equals(value)) {
      writer.println("\"/>");
    } else {
      writer.println("\">");
      Indenter.printlnIndented(writer, indent + 1, value);
      Indenter.printlnIndented(writer, indent, "</property>");
    }
  }

  public static void writeXMLHeader(String publicId, String rootTag, PrintWriter writer) {
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.print("<!DOCTYPE ");

    writer.println(rootTag);

    writer.print("      PUBLIC \"");
    writer.print(publicId);
    writer.println("\"");

    boolean _1_1 = publicId.indexOf("1.1") > 0;
    boolean _1_2 = publicId.indexOf("1.2") > 0;
    boolean _1_3 = publicId.indexOf("1.3") > 0;

    if (_1_1) {

      writer.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_1.dtd\">");

    } else if (_1_2) {

      writer.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_2.dtd\">");

    } else if (_1_3) {

      writer.println("      \"http://tapestry.sf.net/dtd/Tapestry_1_3.dtd\">");

    }

    writer.println(MessageUtil.getString("TAPESTRY.xmlComment"));
  }

  public static void writeLibraryServices(Map serviceMap, PrintWriter writer, int indent) {
    if (serviceMap != null && !serviceMap.isEmpty()) {
      Iterator serviceNames = new TreeSet(serviceMap.keySet()).iterator();
      if (serviceNames.hasNext()) {
        writer.println();
      }
      while (serviceNames.hasNext()) {
        String serviceName = (String) serviceNames.next();
        String classname = (String) serviceMap.get(serviceName);
        if (classname != null) {
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
