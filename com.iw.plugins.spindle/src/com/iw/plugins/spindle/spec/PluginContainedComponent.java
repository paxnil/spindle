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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.ui.AliasSummarizer;
import com.iw.plugins.spindle.util.Indenter;
import net.sf.tapestry.spec.ContainedComponent;

public class PluginContainedComponent extends ContainedComponent {

  private PropertyChangeSupport propertySupport;

  public PluginContainedComponent() {
    propertySupport = new PropertyChangeSupport(this);
  }

  public void setBinding(String name, PluginBindingSpecification spec) {
    if (bindings == null) {
      bindings = new HashMap(7);
    }
    bindings.put(name, spec);
    propertySupport.firePropertyChange("bindings", null, bindings);
  }

  public void removeBinding(String name) {
    if (bindings != null) {
      bindings.remove(name);
      propertySupport.firePropertyChange("bindings", null, bindings);
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }


  // e.g.  <component id="link" type="Direct">
  public void write(String name, PrintWriter writer, int indent) {
    Indenter.printIndented(writer, indent, "<component id=\"" + name);
    if (getCopyOf() != null) {
      writer.print("\" copy-of=\"" + getCopyOf());
      writer.println("\"/>");
      return;
    } else {
      writer.print("\" type=\"" + getType());
      writer.print("\"");
      Collection bns = getBindingNames();
      if (bns != null) {
        if (bns.isEmpty()) {
          writer.println("/>");
          return;
        } else {
          writer.println(">");
          Iterator bindingNames = new TreeSet(bns).iterator();
          while (bindingNames.hasNext()) {
            String bindingName = (String) bindingNames.next();
            ((PluginBindingSpecification) getBinding(bindingName)).write(bindingName, writer, indent + 1);
          }
        }
      }
      Indenter.printlnIndented(writer, indent, "</component>");
    }
  }
}
