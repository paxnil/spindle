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
package com.iw.plugins.spindle.editorapp;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editorapp.components.*;
import com.iw.plugins.spindle.editorapp.pages.*;
import com.iw.plugins.spindle.editors.SpindleFormOutlinePage;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;

public class ApplicationContentOutlinePage extends SpindleFormOutlinePage {
  private Image pageImage;
  private Image componentImage;

   
  /**
   * Constructor for AppFormOutlinePage
   */
  public ApplicationContentOutlinePage(SpindleFormPage page) {
    super(page);
    //Image disposal handled by Plugin
    componentImage = TapestryImages.getSharedImage("component16.gif");
    pageImage = TapestryImages.getSharedImage("page16.gif");
  }

  protected Image getObjectImage(Object obj) {
    if (obj instanceof SpindleFormPage) {
      return getPageImage((SpindleFormPage) obj);
    }
    Holder holder = (Holder) obj;
    SpindleFormPage page = (SpindleFormPage) holder.page;
    if ("Components".equals(page.getTitle())) { 
      return componentImage;
    } else if ("Pages".equals(page.getTitle())) {
      return pageImage;
    }
    return null;
  }

  protected ITreeContentProvider createContentProvider() {
    return new AppContentProvider();
  }

  class AppContentProvider extends BasicContentProvider {
    public Object[] getChildren(Object parent) {
      TapestryApplicationModel model = (TapestryApplicationModel) formPage.getModel();
      PluginApplicationSpecification spec = model.getApplicationSpec();
      if (spec != null) {
        if (parent instanceof ComponentsFormPage) {
          return getObjects(spec.getComponentMapAliases(), (SpindleFormPage) parent);
        }
        if (parent instanceof PagesFormPage) {
          return getObjects(spec.getPageNames(), (SpindleFormPage) parent);
        }
      }
      return super.getChildren(parent);
    }
    public Object getParent(Object child) {
      return super.getParent(child);
    }
  }

}