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

import java.io.PrintWriter;
import java.io.StringWriter;

import com.iw.plugins.spindle.util.Indenter;
import com.iw.plugins.spindle.util.SourceWriter;
import net.sf.tapestry.spec.ParameterSpecification;
import net.sf.tapestry.spec.Direction;

public class PluginParameterSpecification extends ParameterSpecification {

  public void getHelpText(String name, StringBuffer buffer) {
    buffer.append(getHelpText(name));
  }

  public String getHelpText(String name) {
    StringWriter swriter = new StringWriter();
    SourceWriter writer = new SourceWriter(swriter);
    write(name, writer, 0, true);
    return swriter.toString();
    /*
    StringBuffer buffer = new StringBuffer(name);
    buffer.append(" ");
    if (isRequired()) {
      buffer.append("  (Required)\n");
    } else {
      buffer.append("  (Optional)\n");
    }
    buffer.append("Type: ");
    String ptype = getType();
    if (ptype == null || "".equals(ptype.trim())) {
      buffer.append("  any\n");
    } else {
      buffer.append("  ");
      buffer.append(ptype);
      buffer.append("\n");
    }
    String description = getDescription();
    if (description == null || "".equals(description.trim())) {
      buffer.append("No description found");
    } else {
      buffer.append("Description:");
      buffer.append("\n\n");
      buffer.append(description);
    }
    return buffer.toString();
    */
  }

  // e.g. -- <parameter name="book" java-type="com.primix.vlib.ejb.Book" required="yes" parameterName="poo" direction="in"/>
  public void write(String name, PrintWriter writer, int indent, boolean isDTD12) {
    Indenter.printIndented(writer, indent, "<parameter name=\"" + name);
    writer.print("\" java-type=\"" + getType());
    writer.print("\" required=\"" + (isRequired() ? "yes" : "no"));
    if (isDTD12) {
      String propertyName = getPropertyName();
      if (propertyName != null && !"".equals(propertyName)) {
        writer.print("\" property-name=\"" + getPropertyName());
      }
      writer.print("\" direction=\"");
      writer.print(getDirection() == Direction.CUSTOM ? "custom" : "in");
    }
    String description = getDescription();
    if (description == null || "".equals(description.trim())) {
      writer.println("\"/>");
    } else {
      writer.println("\">");
      PluginApplicationSpecification.writeDescription(description, writer, indent + 1);
      Indenter.printlnIndented(writer, indent, "</parameter>");
    }
  }

}