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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.PrintWriter;

import com.iw.plugins.spindle.util.Indenter;
import net.sf.tapestry.spec.PageSpecification;

public class PluginPageSpecification extends PageSpecification {
  private PropertyChangeSupport propertySupport;

  /**
   * Constructor for PluginPageSpecification
   */
  public PluginPageSpecification() {
    super();
    propertySupport = new PropertyChangeSupport(this);
  }

  /**
   * Constructor for PluginPageSpecification
   */
  public PluginPageSpecification(String specificationPath) {
    super(specificationPath);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  //e.g.  <page name="Register" specification-path="/com/primix/vlib/pages/Register.jwc"/>
  public void write(String name, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<page name=\"" + name);
    writer.print("\" specification-path=\"");
    writer.print(getSpecificationPath());
    writer.println("\"/>");
  }

}
