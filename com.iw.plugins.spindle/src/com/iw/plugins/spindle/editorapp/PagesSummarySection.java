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

import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.ui.forms.internal.FormEntry;
import org.eclipse.update.ui.forms.internal.FormSection;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.editors.SummaryHTMLViewer;
import com.iw.plugins.spindle.editors.SummarySourceViewer;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public class PagesSummarySection extends SpindleFormSection {

  private FormEntry pageName;
  private FormEntry specText;
  private Label resolveFailedLabel;
  private SummarySourceViewer sourceViewer;
  private SummarySourceViewer htmlViewer;
  private Composite container;

  private String selectedPage;

  /**
   * Constructor for ComponentAliasSummarySection
   */
  public PagesSummarySection(SpindleFormPage page) {
    super(page);
    setHeaderText("Page Summary");
    setDescription("This section will show a summary of the selected Page (if it can be resolved).");
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClient(Composite parent, FormWidgetFactory factory) {
    container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 7;
    layout.horizontalSpacing = 6;
    container.setLayout(layout);

    String labelName = "Page Name";
    pageName = new FormEntry(createText(container, labelName, factory));
    Text text = (Text) pageName.getControl();
    text.setEditable(false);
    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));

    labelName = "Specification";
    specText = new FormEntry(createText(container, labelName, factory));
    text = (Text) specText.getControl();
    text.setEditable(false);
    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));

    resolveFailedLabel = factory.createLabel(container, "empty", SWT.WRAP);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.horizontalSpan = 2;
    resolveFailedLabel.setLayoutData(data);
    resolveFailedLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    resolveFailedLabel.setVisible(false);

    SashForm sashForm = new SashForm(container, SWT.BORDER);
    sashForm.setOrientation(SWT.VERTICAL);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    data.verticalSpan = 75;
    sashForm.setLayoutData(data);
    sashForm.SASH_WIDTH = 10;

    sourceViewer = new SummarySourceViewer(sashForm);
    data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.verticalAlignment = GridData.FILL;
    data.horizontalSpan = 2;

    data.heightHint = 300;
    sourceViewer.getControl().setLayoutData(data);
    sourceViewer.setPopupListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        manager.removeAll();
        manager.add(new Action("Open") {
          public void run() {

            openTapestryEditor(sourceViewer.getCurrentStorage());

          }
        });

      }
    });

    htmlViewer = new SummaryHTMLViewer(sashForm);
    data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.verticalAlignment = GridData.FILL;
    data.horizontalSpan = 2;
    data.verticalSpan = 25;
    data.heightHint = 300;
    htmlViewer.getControl().setLayoutData(data);
    htmlViewer.setPopupListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        manager.removeAll();
        manager.add(new Action("Open") {
          public void run() {
            openHtmlEditor(htmlViewer.getCurrentStorage());
          }
        });

      }
    });

    sashForm.setWeights(new int[] { 1, 1 });

    factory.paintBordersFor(container);
    return container;
  }

  private void openTapestryEditor(IStorage storage) {
    if (storage != null) {
      SpindleMultipageEditor teditor = (SpindleMultipageEditor) getFormPage().getEditor();
      TapestryPlugin.openTapestryEditor(
        (BaseTapestryModel) TapestryPlugin.getTapestryModelManager().getModel(storage));
    }
  }

  private void openHtmlEditor(IStorage storage) {
    if (storage != null) {
      SpindleMultipageEditor teditor = (SpindleMultipageEditor) getFormPage().getEditor();
      TapestryPlugin.openNonTapistryEditor(storage);
    }
  }

  private TapestryApplicationModel getModel() {
    return ((TapestryApplicationModel) getFormPage().getModel());
  }

  public void dispose() {
    super.dispose();
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    // this can only come from the ComponentAliasSection and it can only be
    // that an alias was selected! or null if the selection was cleared   
    selectedPage = (String) changeObject;
    updateSummary();
  }

  private void updateSummary() {
    resolveFailedLabel.setVisible(false);
    if (selectedPage == null) {
      pageName.setValue("", false);
      specText.setValue("", false);
      sourceViewer.updateNotFound();
      htmlViewer.updateNotFound();
    } else {
      pageName.setValue(selectedPage, false);
      String specificationPath = getModel().getApplicationSpec().getPageSpecification(selectedPage).getSpecificationPath();
      specText.setValue(specificationPath, false);
      if (!specificationPath.endsWith(".jwc")) {
        resolveFailed(specificationPath);
        return;
      }
      ITapestryModel model = (ITapestryModel) getModel();
      IStorage thisStorage = model.getUnderlyingStorage();
      IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(thisStorage);
      if (jproject == null) {
        resolveFailed(specificationPath);
      }
      TapestryLookup lookup = new TapestryLookup();
      try {
        lookup.configure(jproject);
      } catch (JavaModelException jmex) {
        resolveFailed(specificationPath);
      }
      IStorage[] found = lookup.findComponent(specificationPath);
      if (found.length != 1) {
        resolveFailed(specificationPath);
        return;
      }
      sourceViewer.update(null, found[0]);
      IStorage[] htmlStorage = lookup.findHtmlFor(specificationPath);
      if (htmlStorage.length != 1) {
        htmlViewer.updateNotFound();
        return;
      }
      htmlViewer.update(null, htmlStorage[0]);
      container.layout(true);
      container.redraw();
    }
  }

  private void resolveFailed(String name) {
    resolveFailedLabel.setText("Could not resolve: " + name);
    resolveFailedLabel.setVisible(true);
    sourceViewer.updateNotFound();
    htmlViewer.updateNotFound();
  }

}