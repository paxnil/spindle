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
package com.iw.plugins.spindle.editorjwc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editorjwc.beans.*;
import com.iw.plugins.spindle.editorjwc.components.ComponentsFormPage;
import com.iw.plugins.spindle.editors.SpindleFormOutlinePage;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;

public class JWCContentOutlinePage extends SpindleFormOutlinePage {

  Image containedComponentImage;
  Image componentAliasImage;
  Image beanImage;

  /**
   * Constructor for TapestryFormOutlinePage
   */
  public JWCContentOutlinePage(SpindleFormPage page) {
    super(page);
    containedComponentImage = TapestryImages.getSharedImage("component16.gif");
    componentAliasImage = TapestryImages.getSharedImage("componentAlias16.gif");
    beanImage = TapestryImages.getSharedImage("bean.gif");
  }

  protected ITreeContentProvider createContentProvider() {
    return new ContentProvider();
  }

  public void dispose() {
    // shared image disposal handled by the plugin
    super.dispose();
  }

  protected Image getObjectImage(Object obj) {
    if (obj instanceof SpindleFormPage) {
      return getPageImage((SpindleFormPage) obj);
    }
    Holder holder = (Holder) obj;
    SpindleFormPage page = (SpindleFormPage) holder.page;
    if ("Components".equals(page.getTitle())) {
      PluginContainedComponent component = (PluginContainedComponent) holder.element;
      String type = component.getType();
      if (type != null && component.getType().endsWith(".jwc")) { 
        return containedComponentImage;
      } else {
        return componentAliasImage;
      }
    } else if ("Beans".equals(page.getTitle())) {
      return beanImage;
    }
    return null;
  }

  private Object[] getComponentObjects(
    Collection ids,
    PluginComponentSpecification spec,
    SpindleFormPage page) {
    if (ids != null && !ids.isEmpty()) {
      ArrayList result = new ArrayList();
      Iterator iter = new TreeSet(ids).iterator();
      while (iter.hasNext()) {
        Holder holder = new Holder();
        holder.label = (String) iter.next();
        holder.element = spec.getComponent(holder.label);
        holder.page = page;
        result.add(holder);
      }
      return result.toArray();
    }
    return new Object[0];
  }

  class ContentProvider extends BasicContentProvider {
    public Object[] getChildren(Object parent) {
      TapestryComponentModel model = (TapestryComponentModel) formPage.getModel();
      PluginComponentSpecification spec = model.getComponentSpecification();
      if (spec != null) {
        if (parent instanceof ComponentsFormPage) {
          return getComponentObjects(spec.getComponentIds(), spec, (SpindleFormPage) parent);
        }
        if (parent instanceof BeansFormPage) {
          return getObjects(spec.getBeanNames(), (SpindleFormPage) parent);
        }
      }
      return super.getChildren(parent);
    }
    public Object getParent(Object child) {
      return super.getParent(child);
    }
  }

}