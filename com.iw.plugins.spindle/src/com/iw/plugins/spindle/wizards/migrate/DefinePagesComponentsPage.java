package com.iw.plugins.spindle.wizards.migrate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.TapestryStorageLabelProvider;
import com.iw.plugins.spindle.ui.migrate.MigrationContext;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class DefinePagesComponentsPage extends WizardPage {

  MigrationContext context;
  TableViewer componentViewer;
  TableViewer pageViewer;
  Button toPageButton;
  Button fromPageButton;

  ArrayList componentList = new ArrayList();
  ArrayList pageList = new ArrayList();

  /**
   * Constructor for DefinePagesComponentsPage.
   * @param pageName
   */
  public DefinePagesComponentsPage(String pageName, MigrationContext context) {
    super(pageName);

    this.setImageDescriptor(
      ImageDescriptor.createFromURL(TapestryImages.getImageURL("application32.gif")));
    this.setDescription(
      "If listed, Spindle can't tell those below are pages or components. You need to make the distinction.");

    this.context = context;

  }

 
  public boolean setUndefined(List possibles) {

    List actuals = new ArrayList();

    for (Iterator iter = possibles.iterator(); iter.hasNext();) {
      IStorage element = (IStorage) iter.next();

      if ("page".equals(element.getFullPath().getFileExtension())) {

        continue;
      }

      if (context.getMigratorFor(element) != null) {

        continue;
      }

      if (!context.isInScope(element)) {

        continue;
        
      }

      TapestryComponentModel model =
        (TapestryComponentModel) context.getModelManager().getReadOnlyModel(element);

      int DTDVersion = XMLUtil.getDTDVersion(model.getPublicId());

      if (DTDVersion < XMLUtil.DTD_1_3) {

        actuals.add(element);
      }
    }

    

    componentList.clear();
    componentList.addAll(actuals);
    pageList.clear();
    componentViewer.setInput(componentList);
    pageViewer.setInput(pageList);
    updateButtonsEnabled();
    
    return actuals.isEmpty();

  } /**
         * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
         */
  public void createControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    container.setLayout(layout);
    GridData gd;
    Composite leftColumn = new Composite(container, SWT.NULL);
    gd = new GridData(GridData.FILL_BOTH);
    leftColumn.setLayoutData(gd);
    GridLayout leftLayout = new GridLayout();
    leftLayout.verticalSpacing = 10;
    leftLayout.marginWidth = 0;
    leftColumn.setLayout(leftLayout);
    Composite rightColumn = new Composite(container, SWT.NULL);
    gd = new GridData(GridData.FILL_BOTH);
    rightColumn.setLayoutData(gd);
    GridLayout rightLayout = new GridLayout();
    rightLayout.verticalSpacing = 10;
    rightLayout.marginWidth = 0;
    rightColumn.setLayout(rightLayout);
    ComponentSection allSection = new ComponentSection(".jwc Files");
    Control allSectionControl = allSection.createControl(leftColumn);
    gd = new GridData(GridData.FILL_BOTH);
    allSectionControl.setLayoutData(gd);
    PageSection pageSection = new PageSection("make into .page Files");
    Control pageSectionControl = pageSection.createControl(rightColumn);
    gd = new GridData(GridData.FILL_BOTH);
    pageSectionControl.setLayoutData(gd);
    toPageButton.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent e) {

        handleChange(componentViewer, componentViewer.getSelection());
      }

      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });
    fromPageButton.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent e) {

        handleChange(pageViewer, pageViewer.getSelection());
      }

      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });
    componentViewer.setContentProvider(new IStructuredContentProvider() {
      public Object[] getElements(Object inputElement) {
        return componentList.toArray();
      }
      public void dispose() {
      }
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    });
    componentViewer.setLabelProvider(new TapestryStorageLabelProvider(true));
    componentViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtonsEnabled();
      }
    });
    componentViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {

        IStructuredSelection selection = (IStructuredSelection) componentViewer.getSelection();
        if (!selection.isEmpty()) {
          handleChange(componentViewer, selection.getFirstElement());
        }
      }
    });
    pageViewer.setContentProvider(new IStructuredContentProvider() {
      public Object[] getElements(Object inputElement) {
        return pageList.toArray();
      }
      public void dispose() {
      }
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    });
    pageViewer.setLabelProvider(new TapestryStorageLabelProvider(true));
    pageViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtonsEnabled();
      }
    });
    pageViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) pageViewer.getSelection();
        if (!selection.isEmpty()) {
          handleChange(pageViewer, selection.getFirstElement());
        }
      }
    });
    setControl(container);
  }

  private void updateButtonsEnabled() {

    toPageButton.setEnabled(!componentList.isEmpty() && !componentViewer.getSelection().isEmpty());
    fromPageButton.setEnabled(!pageList.isEmpty() && !pageViewer.getSelection().isEmpty());
  }

  private void handleChange(Viewer fromViewer, Object changeObject) {

    IStructuredSelection selection = null;
    if (changeObject instanceof ISelection) {

      selection = (IStructuredSelection) changeObject;
    } else {

      selection = new StructuredSelection(changeObject);
    }

    List fromList = null;
    List toList = null;
    TableViewer toViewer = null;
    if (fromViewer == componentViewer) {

      fromList = componentList;
      toList = pageList;
      toViewer = pageViewer;
    } else {

      fromList = pageList;
      toList = componentList;
      toViewer = componentViewer;
    }

    if (!selection.isEmpty()) {

      List selectedObjects = selection.toList();
      fromList.removeAll(selectedObjects);
      toList.addAll(selectedObjects);
      fromViewer.setInput(fromList);
      toViewer.setInput(toViewer);
      toViewer.getControl().setFocus();
      toViewer.setSelection(selection);
      updateButtonsEnabled();
    }
  }

  class ComponentSection extends SectionWidget { /**
      * Constructor for AllSection.
      */
    public ComponentSection(String headerText) {
      super();
      setDescriptionPainted(false);
      setHeaderText(headerText);
    } /**
                   * @see com.iw.plugins.spindle.wizards.migrate.SectionWidget#createClient(Composite)
                   */
    public Composite createClient(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridData gd;
      GridLayout layout = new GridLayout();
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      composite.setLayout(layout);
      gd = new GridData(GridData.FILL_BOTH);
      composite.setLayoutData(gd);
      Composite leftColumn = new Composite(composite, SWT.NULL);
      gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_CENTER);
      leftColumn.setLayoutData(gd);
      GridLayout leftLayout = new GridLayout();
      leftLayout.verticalSpacing = 0;
      leftLayout.marginWidth = 0;
      leftColumn.setLayout(leftLayout);
      Table table = new Table(leftColumn, SWT.MULTI | SWT.BORDER);
      table.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
      componentViewer = new TableViewer(table);
      return composite;
    }

  }

  class PageSection extends SectionWidget { /**
      * Constructor for PageSection.
      */
    public PageSection(String headerText) {
      super();
      setDescriptionPainted(false);
      setHeaderText(headerText);
    } /**
                   * @see com.iw.plugins.spindle.wizards.migrate.SectionWidget#createClient(Composite)
                   */
    public Composite createClient(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridData gd;
      GridLayout layout = new GridLayout();
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      layout.numColumns = 2;
      composite.setLayout(layout);
      gd = new GridData(GridData.FILL_BOTH);
      composite.setLayoutData(gd);
      Composite leftColumn = new Composite(composite, SWT.NULL);
      gd = new GridData(GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_CENTER);
      leftColumn.setLayoutData(gd);
      GridLayout leftLayout = new GridLayout();
      leftLayout.verticalSpacing = 0;
      leftLayout.marginWidth = 0;
      leftColumn.setLayout(leftLayout);
      Composite rightColumn = new Composite(composite, SWT.NULL);
      gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
      rightColumn.setLayoutData(gd);
      GridLayout rightLayout = new GridLayout();
      rightLayout.verticalSpacing = 0;
      rightLayout.marginWidth = 0;
      rightColumn.setLayout(rightLayout);
      Composite buttonContainer = new Composite(leftColumn, SWT.NULL);
      gd = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_VERTICAL);
      buttonContainer.setLayoutData(gd);
      GridLayout buttonLayout = new GridLayout();
      buttonLayout.verticalSpacing = 5;
      buttonLayout.marginWidth = 0;
      buttonContainer.setLayout(buttonLayout);
      toPageButton = new Button(buttonContainer, SWT.BORDER);
      toPageButton.setText(">>");
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      toPageButton.setLayoutData(gd);
      fromPageButton = new Button(buttonContainer, SWT.BORDER);
      fromPageButton.setText("<<");
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
      fromPageButton.setLayoutData(gd);
      Table table = new Table(rightColumn, SWT.MULTI | SWT.BORDER);
      table.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
      pageViewer = new TableViewer(table);
      return composite;
    }

  }

  public List getNewPages() {
    return pageList;
  }

}
