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

import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.ui.editor.IPDEEditorPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.BasicLinksSection;
import com.iw.plugins.spindle.editors.HyperLinkAdapter;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.TapestryApplicationModel;

public class OverviewComponentRefSection extends BasicLinksSection {

  public OverviewComponentRefSection(SpindleFormPage page) {
    super(
      page,
      "Component Aliases",
      "This section describes the components referred to in this application");

  }

  protected SpindleFormPage getMorePage() {
    return (SpindleFormPage)getFormPage().getEditor().getPage(APPMultipageEditor.COMPONENTS);
  }

  public void update(boolean removePrevious) {
    if (removePrevious) {
      removeAll();
    }
    TapestryApplicationModel model = (TapestryApplicationModel) getFormPage().getModel();
    Iterator i = new TreeSet(model.getApplicationSpec().getComponentMapAliases()).iterator();
    while (i.hasNext()) {
      String alias = (String) i.next();
      String linkLabel = alias + " [" + model.getApplicationSpec().getComponentAlias(alias) + "]";
      Image image = TapestryImages.getSharedImage("component16.gif");
      addHyperLink(alias, linkLabel, image, new PagesHyperLinkAdapter());
    }
    super.update(removePrevious);
  }

  public void modelChanged(IModelChangedEvent event) {
    int eventType = event.getChangeType();
    if (eventType == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
      return;
    }
    if (eventType == IModelChangedEvent.CHANGE) {
      updateNeeded = event.getChangedProperty().equals("componentMap");
    }
  }

  protected class PagesHyperLinkAdapter extends HyperLinkAdapter {
    public void linkActivated(Control parent) {
      final SpindleFormPage targetPage = getMorePage();
      if (targetPage == null) {
        return;
      }
      getFormPage().getEditor().showPage(targetPage);
      targetPage.openTo(parent.getData());
    }
  }
}