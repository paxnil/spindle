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
import com.primix.tapestry.spec.AssetSpecification;
import com.primix.tapestry.spec.AssetType;

public class PluginAssetSpecification extends AssetSpecification {

  public PluginAssetSpecification(AssetType type, String path) {
    super(type, path);
  }

  public void setPath(String value) {
    path = value;
  }

  public void write(String name, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<");
    AssetType type = getType();
    if (type.equals(AssetType.PRIVATE)) {
      writer.print("private-asset name=\"" + name);
      writer.print("\" resource-path=\"" + getPath());
      writer.println("\"/>");
      return;
    } else if (type.equals(AssetType.EXTERNAL)) {
      writer.print("external-asset name=\"" + name);
      writer.print("\" URL=\"" + getPath());
      writer.println("\"/>");
      return;
    } else if (type.equals(AssetType.CONTEXT)) {
      writer.print("context-asset name=\"" + name);
      writer.print("\" path=\"" + getPath());
      writer.println("\"/>");
      return;
    }

  }

}