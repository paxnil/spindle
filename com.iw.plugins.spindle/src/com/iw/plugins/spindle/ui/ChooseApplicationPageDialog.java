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
package com.iw.plugins.spindle.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.tapestry.spec.PageSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginPageSpecification;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public class ChooseApplicationPageDialog extends AbstractDialog {

  private UneditableComboBox selectedApplication;
  private List allApplications;
  private Text pageNameText;
  private Table pages;
  private Table applications;
  private ILabelProvider nameLabelProvider = new PageLabelProvider();
  private ILabelProvider applicationLabelProvider = new ApplicationLabelProvider();
  private PageCollector collector;


  private String resultPage;

  protected int acceptFlags = TapestryLookup.ACCEPT_COMPONENTS;

  static private final Object[] empty = new Object[0];

  /**
    * Constructor for PageRefDialog
    */
  public ChooseApplicationPageDialog(
    Shell shell,
    String windowTitle,
    String description) {
    super(shell);
    updateWindowTitle(windowTitle);
    updateMessage(description);
  }

  public void create() {
    super.create();
    pageNameText.setFocus();
    scan();
    updateOkState();
  }

  /**
   * @see AbstractDialog#performCancel()
   */
  protected boolean performCancel() {
    setReturnCode(CANCEL);
    return true;

  }

  protected void okPressed() {
    setReturnCode(OK);
    hardClose();
  }

  protected boolean hardClose() {
    // dispose any contained stuff
    //chooser.dispose();
    return super.hardClose();
  }

  /**
   * Constructor for ChooseComponentControl
   */
  protected Composite createAreaContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridData data = null;
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.verticalSpacing = 10;
    container.setLayout(layout);

    allApplications = TapestryPlugin.getTapestryModelManager().getAllModels(null, "application");
    collector = new PageCollector(allApplications == null ? Collections.EMPTY_LIST : allApplications);

    pageNameText = createText(container);
    pages = createUpperList(container);
    applications = createLowerList(container);

    //a little trick to make the window come up faster
    String initialFilter = "*";
    if (initialFilter != null) {
      pageNameText.setText(initialFilter);
      pageNameText.selectAll();
    }

    return container;
  }

//  public void configure(IJavaProject project) {
//    lookup = new TapestryLookup();
//    try {
//      lookup.configure(project);
//    } catch (JavaModelException jmex) {
//      TapestryPlugin.getDefault().logException(jmex);
//      lookup = null;
//    }
//
//  }

  protected void scan() {

    String pageBit = pageNameText.getText().trim();
    if ("".equals(pageBit)) {
      updateListWidget(empty, pages, nameLabelProvider);
    } else {
      collector.findAllPages(pageBit);
      updateListWidget(collector.getPageNames(), pages, nameLabelProvider);
    }
  }

  public void dispose() {
    selectedApplication.dispose();
    pageNameText.dispose();
    pages.dispose();
    applications.dispose();
  }

  private Text createText(Composite parent) {
    (new Label(parent, SWT.NONE)).setText("choose a page:");
    Text text = new Text(parent, SWT.BORDER);
    GridData spec = new GridData();
    spec.grabExcessVerticalSpace = false;
    spec.grabExcessHorizontalSpace = true;
    spec.horizontalAlignment = spec.FILL;
    spec.verticalAlignment = spec.BEGINNING;
    text.setLayoutData(spec);
    Listener l = new Listener() {
      public void handleEvent(Event evt) {
        scan();
      }
    };
    text.addListener(SWT.Modify, l);

    return text;
  }

  private Table createUpperList(Composite parent) {
    (new Label(parent, SWT.NONE)).setText("select page:");

    Table list = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    list.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event evt) {
        handleUpperSelectionChanged();
      }
    });
    list.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event evt) {
        handleDoubleClick();
      }
    });
    list.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        nameLabelProvider.dispose();
      }
    });
    GridData spec = new GridData();
    spec.widthHint = convertWidthInCharsToPixels(100);
    spec.heightHint = convertHeightInCharsToPixels(15);
    spec.grabExcessVerticalSpace = true;
    spec.grabExcessHorizontalSpace = true;
    spec.horizontalAlignment = spec.FILL;
    spec.verticalAlignment = spec.FILL;
    list.setLayoutData(spec);
    return list;
  }

  protected void handleUpperSelectionChanged() {
    int selection = pages.getSelectionIndex();
    if (selection >= 0) {
      String name = pages.getItem(selection).getText();
      updateListWidget(collector.getApplicationsForName(name), applications, applicationLabelProvider);
    } else {
      updateListWidget(empty, applications, applicationLabelProvider);
    }
  }

  protected void handleDoubleClick() {
    if (getWidgetSelection() != null) {
      buttonPressed(IDialogConstants.OK_ID);
    }
  }

  private Table createLowerList(Composite parent) {
    (new Label(parent, SWT.NONE)).setText("in application:");

    Table list = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    list.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event evt) {
        handleLowerSelectionChanged();
      }
    });
    list.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event evt) {
        handleDoubleClick();
      }
    });
    list.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        applicationLabelProvider.dispose();
      }
    });
    GridData spec = new GridData();
    spec.widthHint = convertWidthInCharsToPixels(100);
    spec.heightHint = convertHeightInCharsToPixels(5);
    spec.grabExcessVerticalSpace = true;
    spec.grabExcessHorizontalSpace = true;
    spec.horizontalAlignment = spec.FILL;
    spec.verticalAlignment = spec.FILL;
    list.setLayoutData(spec);
    return list;
  }

  protected void handleLowerSelectionChanged() {
  }

  private void updateListWidget(Object[] elements, Table table, ILabelProvider provider) {
    int size = elements.length;
    if (size == 0) {
    	table.removeAll();
    	return;
    }
    table.setRedraw(false);
    int itemCount = table.getItemCount();
    if (size < itemCount) {
      table.remove(0, itemCount - size - 1);
    }
    TableItem[] items = table.getItems();
    for (int i = 0; i < size; i++) {
      TableItem ti = null;
      if (i < itemCount) {
        ti = items[i];
      } else {
        ti = new TableItem(table, i);
      }
      ti.setText(provider.getText(elements[i]));
      Image img = provider.getImage(elements[i]);
      if (img != null) {
        ti.setImage(img);
      }
    }
    if (table.getItemCount() > 0) {
      table.setSelection(0);
    }
    table.setRedraw(true);
    handleSelectionChanged(table);
  }

  protected void handleSelectionChanged(Table table) {
    if (table == pages) {
      handleUpperSelectionChanged();
    } else {
      updateOkState();
    }
  }

  private void updateOkState() {
    Button okButton = getOkButton();
    if (okButton != null)
      okButton.setEnabled(getWidgetSelection() != null);
  }

  public Object getWidgetSelection() {

    resultPage = null;

    int i = pages.getSelectionIndex();

    if (i >= 0) {
      resultPage = pages.getItem(i).getText();
    }
    return resultPage;
  }

  public String getResultComponent() {
    return resultPage;
  }

  protected class PageLabelProvider implements ILabelProvider, IBaseLabelProvider {

    Image image;

    public Image getImage(Object element) {
      if (image == null) {
        image = TapestryImages.getSharedImage("page16.gif");
      }
      return image;
    }

    public String getText(Object element) {
      return (String) element;
    }

    public void dispose() {
      // Shared image disposal handled by the Plugin
    }

    public void addListener(ILabelProviderListener listener) {
    }
    public boolean isLabelProperty(Object arg0, String arg1) {
      return false;
    }
    public void removeListener(ILabelProviderListener listener) {
    }

  }

  protected class ApplicationLabelProvider implements ILabelProvider, IBaseLabelProvider {

    Image image;

    public Image getImage(Object element) {
      if (image == null) {
        image = TapestryImages.getSharedImage("application16.gif");
      }
      return image;
    }

    public String getText(Object element) {
      return (String) element;
    }

    public void dispose() {
      // Shared image disposal handled by the Plugin
    }

    public void addListener(ILabelProviderListener listener) {
    }
    public boolean isLabelProperty(Object arg0, String arg1) {
      return false;
    }
    public void removeListener(ILabelProviderListener listener) {
    }

  }

  protected class PageCollector {

    List scanApplications;
    List foundPages;

    public PageCollector(List allApplications) {
      scanApplications = allApplications;
    }

    /**
    * Method reset.
    */
    private void reset() {
      foundPages = new ArrayList();
    }

    /**
    * Method findAllPages.
    * @param pageBit
    */
    private void findAllPages(String pageBit) {
      reset();
      for (Iterator appIter = scanApplications.iterator(); appIter.hasNext();) {

        TapestryApplicationModel model = (TapestryApplicationModel) appIter.next();

        PluginApplicationSpecification spec =
          (PluginApplicationSpecification) model.getApplicationSpec();

        for (Iterator iter = spec.getPageNames().iterator(); iter.hasNext();) {

          String name = (String) iter.next();
          if (!foundPages.contains(name) && (pageBit.equals("*") || name.startsWith(pageBit))) {
            foundPages.add(name);
          }
        }
      }
    }

    /**
    * Method getPageNames.
    * @return Object[]
    */
    private Object[] getPageNames() {
      if (foundPages == null) {
        return empty;
      }
      TreeSet result = new TreeSet(foundPages);
      return (Object[]) result.toArray(new Object[result.size()]);
    }

    /**
    * Method getApplicationsForName.
    * @param name
    * @return Object[]
    */
    private Object[] getApplicationsForName(String name) {
      ArrayList apps = new ArrayList();
      for (Iterator appIter = scanApplications.iterator(); appIter.hasNext();) {

        TapestryApplicationModel model = (TapestryApplicationModel) appIter.next();

        PluginApplicationSpecification spec =
          (PluginApplicationSpecification) model.getApplicationSpec();
        PageSpecification pspec = spec.getPageSpecification(name);

        if (pspec != null) {

          apps.add(model.getUnderlyingStorage().getName() + " " + pspec.getSpecificationPath());
        }
      }
      TreeSet sorted = new TreeSet(apps);
      return (Object[]) sorted.toArray(new Object[sorted.size()]);
    }
  }

}