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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public class ChooseWorkspaceModelDialog extends AbstractDialog {

  static public ChooseWorkspaceModelDialog createLibraryModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(
        shell,
        project,
        windowTitle,
        description,
        TapestryLookup.ACCEPT_LIBRARIES);

    result.setTitleImageString("application48.gif");

    return result;
  }

  static public ChooseWorkspaceModelDialog createApplicationModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(
        shell,
        project,
        windowTitle,
        description,
        TapestryLookup.ACCEPT_APPLICATIONS);

    result.setTitleImageString("application48.gif");

    return result;

  }

  static public ChooseWorkspaceModelDialog createApplicationAndLibraryModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(
        shell,
        project,
        windowTitle,
        description,
        TapestryLookup.ACCEPT_LIBRARIES | TapestryLookup.ACCEPT_APPLICATIONS);

    result.setTitleImageString("application48.gif");

    return result;

  }

  static public ChooseWorkspaceModelDialog createComponentModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(
        shell,
        project,
        windowTitle,
        description,
        TapestryLookup.ACCEPT_COMPONENTS);
    result.setTitleImageString("component48.gif");

    return result;
  }

  static public ChooseWorkspaceModelDialog createPageModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(
        shell,
        project,
        windowTitle,
        description,
        TapestryLookup.ACCEPT_PAGES);
    result.setTitleImageString("component48.gif");

    return result;

  }

  private Text nameText;
  private Table applications;
  private Table packages;
  private ScanCollector collector = new ScanCollector();
  private ILabelProvider nameLabelProvider = new LabelProvider();
  private ILabelProvider packageLabelProvider =
    new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_SMALL_ICONS);

  private TapestryLookup lookup;

  private String resultString;

  private IPackageFragment resultPackage;

  private int acceptFlags;

  private String titleImageString;
  
  private boolean ignoreReadOnly = false;

  static private final Object[] empty = new Object[0];

  public ChooseWorkspaceModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description,
    int acceptFlags) {
    super(shell);
    updateWindowTitle(windowTitle);
    updateMessage(description);
    configure(project);
    this.acceptFlags = acceptFlags;
  }

  public void create() {
    super.create();
    nameText.setFocus();
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

  protected Composite createAreaContents(Composite parent) {

    if (titleImageString != null) {

      setTitleImage(TapestryImages.getSharedImage(titleImageString));
    }

    Composite container = new Composite(parent, SWT.NONE);
    GridData data = null;
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.verticalSpacing = 10;
    container.setLayout(layout);

    nameText = createText(container);
    applications = createUpperList(container);
    packages = createLowerList(container);

    //a little trick to make the window come up faster
    String initialFilter = "*";
    if (initialFilter != null) {
      nameText.setText(initialFilter);
      nameText.selectAll();
    }

    return container;
  }

  public void configure(IJavaProject project) {
    lookup = new TapestryLookup();
    try {
      lookup.configure(project);
    } catch (JavaModelException jmex) {
      TapestryPlugin.getDefault().logException(jmex);
      lookup = null;
    }

  }

  protected void scan() {
    if (lookup == null) {
      return;
    }
    collector.reset();
    String componentBit = nameText.getText().trim();
    if ("".equals(componentBit)) {
      updateListWidget(empty, applications, nameLabelProvider);
    } else {
      collector.reset();
      lookup.findAll(componentBit, true, acceptFlags, collector);
      updateListWidget(collector.getApplicationNames(), applications, nameLabelProvider);
    }
  }

  public void dispose() {
    nameText.dispose();
    applications.dispose();
    packages.dispose();
    lookup = null;
  }

  private Text createText(Composite parent) {
    (new Label(parent, SWT.NONE)).setText("choose an application:");
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
    (new Label(parent, SWT.NONE)).setText("choose application:");

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
    int selection = applications.getSelectionIndex();

    if (selection >= 0) {

      String name = applications.getItem(selection).getText();
      updateListWidget(collector.getPackagesFor(name), packages, packageLabelProvider);

    } else {

      updateListWidget(empty, packages, packageLabelProvider);

    }
  }

  protected void handleDoubleClick() {
    if (getWidgetSelection() != null) {
      buttonPressed(IDialogConstants.OK_ID);
    }
  }

  private Table createLowerList(Composite parent) {
    (new Label(parent, SWT.NONE)).setText("in package:");

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
        packageLabelProvider.dispose();
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
      ti.setData(elements[i]);
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
    if (table == applications) {
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

    resultString = null;
    resultPackage = null;

    int i = applications.getSelectionIndex();

    if (i >= 0) {

      resultString = applications.getItem(i).getText();
    }

    int j = packages.getSelectionIndex();

    if (j >= 0) {

      resultPackage = (IPackageFragment) packages.getItem(j).getData();

    }

    return resultString;
  }

  public String getResultString() {
    return resultString;
  }

  public IPackageFragment getResultPackage() {
    return resultPackage;
  }

  public ITapestryModel getResultModel() {

    return collector.getModel(resultString, resultPackage);
  }

  protected class ScanCollector implements ITapestryLookupRequestor {

    Map results;
    Map storageLookup;

    /**
     * Constructor for ScanCollector
     */
    public ScanCollector() {
      super();
      reset();
    }

    public void reset() {
      results = new HashMap();
      storageLookup = new HashMap();
    }

    public Map getResults() {
      return results;
    }

    public ITapestryModel getModel(String name, IPackageFragment pack) {

      String packname = "(default package)";
      if (pack != null) {

        packname = pack.getElementName();
      }

      IStorage storage = (IStorage) storageLookup.get(name + packname);

      return (ITapestryModel) TapestryPlugin.getTapestryModelManager().getReadOnlyModel(storage);
    }

    public Object[] getApplicationNames() {
      if (results == null) {
        return empty;
      }
      return new TreeSet(results.keySet()).toArray();
    }

    public Object[] getPackagesFor(String name) {
      if (results == null) {

        return empty;

      }
      Set packages = (Set) results.get(name);

      if (packages == null) {

        return empty;
      }
      return packages.toArray();
    }

    /**
     * @see ITapestryLookupRequestor#isCancelled()
     */
    public boolean isCancelled() {
      return false;
    }

    /**
     * @see ITapestryLookupRequestor#accept(IStorage, IPackageFragment)
     */
    public boolean accept(IStorage storage, IPackageFragment fragment) {
    	
      if (ignoreReadOnly && storage.isReadOnly()) {
      	return false;
      }

      String name = storage.getName();
      Object storePackageFragment;
      String packageElementName; 
      
      if (fragment == null) {
      	
        storePackageFragment = "(default package)";
        packageElementName = (String)storePackageFragment;
        
      } else {
      	
        storePackageFragment = fragment;
        packageElementName = fragment.getElementName();
      }

      storageLookup.put(name + packageElementName, storage);

      Set packages = (Set) results.get(name);

      if (packages == null) {

        packages = new HashSet();
        packages.add(storePackageFragment);
        results.put(name, packages);

      } else if (!packages.contains(storePackageFragment)) {

        packages.add(storePackageFragment);
      }
      return true;
    }

  }

  protected class LabelProvider implements ILabelProvider, IBaseLabelProvider {

    Image applicationImage = TapestryImages.getSharedImage("application16.gif");
    Image libraryImage = TapestryImages.getSharedImage("library16.gif");
    Image componentImage = TapestryImages.getSharedImage("component16.gif");
    Image pageImage = TapestryImages.getSharedImage("page16.gif");

    public Image getImage(Object element) {

      String name = (String) element;
      if (name.indexOf(".application") >= 0) {

        return applicationImage;

      } else if (name.indexOf(".library") >= 0) {

        return libraryImage;

      } else if (name.indexOf(".jwc") >= 0) {

        return componentImage;

      } else if (name.indexOf(".page") >= 0) {

        return pageImage;

      }
      return null;
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

  /**
   * Returns the titleImageString.
   * @return String
   */
  public String getTitleImageString() {
    return titleImageString;
  }

  /**
   * Sets the titleImageString.
   * @param titleImageString The titleImageString to set
   */
  public void setTitleImageString(String titleImageString) {
    this.titleImageString = titleImageString;
  }

  /**
   * Returns the ignoreReadOnly.
   * @return boolean
   */
  public boolean isIgnoreReadOnly() {
    return ignoreReadOnly;
  }

  /**
   * Sets the ignoreReadOnly.
   * @param ignoreReadOnly The ignoreReadOnly to set
   */
  public void setIgnoreReadOnly(boolean ignoreReadOnly) {
    this.ignoreReadOnly = ignoreReadOnly;
  }

}