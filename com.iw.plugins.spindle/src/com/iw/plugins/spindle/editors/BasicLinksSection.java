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
package com.iw.plugins.spindle.editors;

import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import org.eclipse.update.ui.forms.internal.IFormPage;
import org.eclipse.update.ui.forms.internal.IHyperlinkListener;

import com.iw.plugins.spindle.model.BaseTapestryModel;

public abstract class BasicLinksSection extends SpindleFormSection implements IModelChangedListener {

  protected FormWidgetFactory useFactory;
  protected boolean updateNeeded = true;
  private Composite linksParent;
  protected Button moreButton;

  /**
   * Constructor for TapestryPageSection
   */
  public BasicLinksSection(SpindleFormPage page, String headerText, String description) {
    super(page);
    setHeaderText(headerText);
    if (description != null) {
      setDescription(description);
    }
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClient(Composite parent, FormWidgetFactory factory) {
    useFactory = factory;
    Composite container = useFactory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    container.setLayout(layout);
    layout = new GridLayout();
    layout.numColumns = 2;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 2;

    linksParent = useFactory.createComposite(container);

    RowLayout rlayout = new RowLayout();
    rlayout.wrap = true;
    linksParent.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_BOTH);
    linksParent.setLayoutData(gd);

    Composite buttonContainer = useFactory.createComposite(container);
    gd = new GridData(GridData.FILL_VERTICAL);
    buttonContainer.setLayoutData(gd);
    layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    //layout.numColumns = 2;
    buttonContainer.setLayout(layout);

    moreButton = factory.createButton(buttonContainer, "More", SWT.PUSH);
    gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
    moreButton.setLayoutData(gd);
    linkMoreButton();

    return container;
  }

  protected Button getMoreButton() {
    return moreButton;
  }

  protected void linkMoreButton() {
    final SpindleFormPage targetPage = getMorePage();
    if (targetPage == null) {
      return;
    }
    moreButton.setToolTipText(((IFormPage) targetPage).getTitle());
    moreButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        getFormPage().getEditor().showPage(targetPage);
      }
    });
  }

  protected SpindleFormPage getMorePage() {
    return null;
  }

  protected void addHyperLink(String key, String value, Image image, IHyperlinkListener listener) {
    Label imageLabel = useFactory.createLabel(linksParent, " ");
    Label hyperlink = useFactory.createHyperlinkLabel(linksParent, value, listener);
    imageLabel.setImage(image);
    hyperlink.setData(key);
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
  }

  public void initialize(Object input) {
  	if (canUpdate()) {
    	update(true);
  	}
    BaseTapestryModel model = (BaseTapestryModel) input;
    model.addModelChangedListener(this);
  }

  public void dispose() {
    linksParent.dispose();
    BaseTapestryModel model = (BaseTapestryModel) getModel();
    model.removeModelChangedListener(this);
    super.dispose();
  }

  public void update() {
    if (updateNeeded && canUpdate()) {
      this.update(true);
    }
  }

  protected IModel getModel() {
    return (IModel) getFormPage().getModel();
  }

  public void update(boolean removePrevious) {
    afterUpdate(removePrevious);
  }

  protected void removeAll() {
    Control[] children = linksParent.getChildren();
    for (int i = 0; i < children.length; i++) {
      children[i].dispose();
    }
  }

  protected void afterUpdate(boolean removePrevious) {
    if (removePrevious) {
      linksParent.layout(true);
      linksParent.redraw();
    }
    updateNeeded = false;
  }

}