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

import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.ui.editor.IPDEEditorPage;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.BasicLinksSection;
import com.iw.plugins.spindle.editors.HyperLinkAdapter;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;

public class OverviewBeanSection extends BasicLinksSection {

  /**
   * Constructor for OverviewParametersSection
   */
  public OverviewBeanSection(SpindleFormPage page) {
    super(page, "Beans", "This section lists any Beans");
  }
  public void update(boolean removePrevious) {
    if (removePrevious) {
      removeAll();
    }
    TapestryComponentModel model = (TapestryComponentModel) getModel();
    Iterator i = new TreeSet(model.getComponentSpecification().getBeanNames()).iterator();
    while (i.hasNext()) {
      String name = (String) i.next();
      PluginBeanSpecification spec =
        (PluginBeanSpecification) model.getComponentSpecification().getBeanSpecification(name);
      String type = spec.getClassName();
      String label = name + (!"".equals(type) ? " classname = " + type : "");
      addHyperLink(
        name,
        label,
        TapestryImages.getSharedImage("bean.gif"),
        new BeansHyperLinkAdapter());
    }
    super.update(removePrevious);
  }

  protected SpindleFormPage getGotoPage() {
    return (SpindleFormPage)getFormPage().getEditor().getPage(JWCMultipageEditor.BEANS);
  }

  protected class BeansHyperLinkAdapter extends HyperLinkAdapter {
    public void linkActivated(Control parent) {
      final IPDEEditorPage targetPage = getGotoPage();
      if (targetPage == null) {
        return;
      }
      getFormPage().getEditor().showPage(targetPage);
      targetPage.openTo(parent.getData());
    }
  }

  public void modelChanged(IModelChangedEvent event) {
    super.modelChanged(event);
    if (!updateNeeded) {
      if (event.getChangeType() == IModelChangedEvent.CHANGE) {
        updateNeeded = event.getChangedProperty().equals("beans");
      }
    }
  }

}