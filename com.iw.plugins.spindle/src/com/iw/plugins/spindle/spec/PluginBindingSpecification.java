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

import com.iw.plugins.spindle.util.Indenter;
import net.sf.tapestry.spec.BindingSpecification;
import net.sf.tapestry.spec.BindingType;

public class PluginBindingSpecification extends BindingSpecification {


  /**
   * Constructor for PluginBindingSpecification
   */
  public PluginBindingSpecification(BindingType type, String value) {
    super(type, value);
  }   

  public void write(String name, PrintWriter writer, int indent, boolean isDTD12) {
    Indenter.printIndented(writer, indent, "<");
    BindingType type = getType();
    if (type.equals(BindingType.FIELD)) {
      writer.print("field-binding name=\"" + name);
      writer.print("\" field-name=\"");
    } else if (type.equals(BindingType.INHERITED)) {
      writer.print("inherited-binding name=\"" + name);
      writer.print("\" parameter-name=\"");
    } else if (type.equals(BindingType.STATIC)) {
      writer.print("static-binding name=\"" + name);
      writer.print("\">");
      writer.print(getValue());
      writer.println("</static-binding>");
      return;
    } else if (type.equals(BindingType.DYNAMIC)) {
      writer.print("binding name=\"" + name);
      writer.print("\" property-path=\"");
    } else if (isDTD12 && type.equals(BindingType.STRING)) {
      writer.print("string-binding name=\"" + name);
      writer.print("\" key=\"");
    }
    writer.print(getValue());
    writer.println("\"/>");
  }

}
