package com.iw.plugins.spindle.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class ChooseWorkspaceModelWidget extends Viewer {

  private int acceptFlags;

  private Text nameText;
  private Table resources;
  private Table packages;
  private ScanCollector collector = new ScanCollector();
  private ILabelProvider nameLabelProvider = new LabelProvider(); 
  private ILabelProvider packageLabelProvider =
    new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_SMALL_ICONS);

  private TapestryLookup lookup;

  private String resultString;

  private IPackageFragment resultPackage;

  static private final Object[] empty = new Object[0];

  private List doubleClickListeners = new ArrayList();

  private Composite control = null;

  public ChooseWorkspaceModelWidget(IJavaProject project, int acceptFlags) {
    configure(project);
    this.acceptFlags = acceptFlags;
  }

  public void addDoubleClickListener(IDoubleClickListener listener) {
    if (!doubleClickListeners.contains(listener)) {

      doubleClickListeners.add(listener);

    }
  }

  public void removeDoubleClickListener(IDoubleClickListener listener) {

    doubleClickListeners.remove(listener);

  }

  public Composite createControl(Composite parent) {

    Composite container = new Composite(parent, SWT.NONE);

    FormLayout layout = new FormLayout();
    layout.marginWidth = 4;
    layout.marginHeight = 4;
    container.setLayout(layout);

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.width = 400;

    container.setLayoutData(formData);

    Label searchLabel = new Label(container, SWT.NONE);
    searchLabel.setText("search:");

    nameText = createText(container);

    Label resourceListLabel = new Label(container, SWT.NONE);
    resourceListLabel.setText("choose :");

    resources = createUpperList(container);

    Label packagesLabel = new Label(container, SWT.NONE);
    packagesLabel.setText("in package:");

    packages = createLowerList(container);

    addControl(searchLabel, container, 4);

    addControl(nameText, searchLabel, 4);

    addControl(resourceListLabel, nameText, 4);

    addControl(resources, resourceListLabel, 4);

    PixelConverter converter = new PixelConverter(resources);

    formData = (FormData) resources.getLayoutData();
    formData.height = converter.convertHeightInCharsToPixels(8);

    addControl(packagesLabel, resources, 4);

    addControl(packages, packagesLabel, 4);
    
    converter = new PixelConverter(packages);
    formData = (FormData) packages.getLayoutData();
    formData.height = converter.convertHeightInCharsToPixels(5);
    

    //a little trick to make the window come up faster
    String initialFilter = "*";
    if (initialFilter != null) {
      nameText.setText(initialFilter);
      nameText.selectAll();
    }

    control = container;
    return container;
  }

  protected void addControl(Control toBeAdded, Control parent, int verticalOffset) {
    FormData formData = new FormData();
    formData.top = new FormAttachment(parent, verticalOffset);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    toBeAdded.setLayoutData(formData);
  }

  protected void addControl(Control toBeAdded, Control parent) {
    addControl(toBeAdded, parent, 0);
  }

  public ISelection getSelection() {

    resultString = null;
    resultPackage = null;

    int i = resources.getSelectionIndex();

    if (i >= 0) {

      resultString = resources.getItem(i).getText();
    }

    int j = packages.getSelectionIndex();

    if (j >= 0) {

      resultPackage = (IPackageFragment) packages.getItem(j).getData();

    }

    if (resultString == null) {

      return new StructuredSelection();

    }

    return new StructuredSelection(resultString);
  }

  public void refresh() {
    if (lookup == null) {

      return;

    }

    collector.reset();
    String componentBit = nameText.getText().trim();

    if ("".equals(componentBit)) {

      updateListWidget(empty, resources, nameLabelProvider);

    } else {

      collector.reset();
      lookup.findAll(componentBit, true, acceptFlags, collector);
      updateListWidget(collector.getApplicationNames(), resources, nameLabelProvider);
    }
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

  public void dispose() {
    nameText.dispose();
    resources.dispose();
    packages.dispose();
    lookup = null;
  }

  private Text createText(Composite parent) {
    Text text = new Text(parent, SWT.BORDER);
    Listener l = new Listener() {
      public void handleEvent(Event evt) {
        refresh();
      }
    };
    text.addListener(SWT.Modify, l);

    return text;
  }

  private Table createUpperList(Composite parent) {

    Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    table.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event evt) {
        handleUpperSelectionChanged();
      }
    });

    table.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event evt) {
        handleDoubleClick();
      }
    });
    table.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        nameLabelProvider.dispose();
      }
    });
    return table;
  }



  private Table createLowerList(Composite parent) {

    Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    table.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event evt) {
        handleLowerSelectionChanged();
      }
    });
    table.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event evt) {
        handleDoubleClick();
      }
    });
    table.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        packageLabelProvider.dispose();
      }
    });

    return table;
  }

  /**
   * Method handleDoubleClick.
   */
  private void handleDoubleClick() {

    for (Iterator iter = doubleClickListeners.iterator(); iter.hasNext();) {

      IDoubleClickListener listener = (IDoubleClickListener) iter.next();

      listener.doubleClick(new DoubleClickEvent(this, getSelection()));
    }
  }
  
  protected void handleUpperSelectionChanged() {
    int selection = resources.getSelectionIndex();

    if (selection >= 0) {

      String name = resources.getItem(selection).getText();
      updateListWidget(collector.getPackagesFor(name), packages, packageLabelProvider);

    } else {

      updateListWidget(empty, packages, packageLabelProvider);

    }
    
    fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
    
    
  }

  protected void handleLowerSelectionChanged() {

    fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
  	
  	
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
    if (table == resources) {
      handleUpperSelectionChanged();
    } else {
      fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
    }
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

  public IStorage getResultStorage() {

    return collector.getStorage(resultString, resultPackage);

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

    public IStorage getStorage(String name, IPackageFragment pack) {

      String packname = "(default package)";
      if (pack != null) {

        packname = pack.getElementName();
      }
      return (IStorage) storageLookup.get(name + packname);

    }

    public ITapestryModel getModel(String name, IPackageFragment pack) {

      try {
      	
        IStorage storage = getStorage(name, pack);
        
        return (ITapestryModel) TapestryPlugin.getTapestryModelManager(storage).getReadOnlyModel(storage);
        
      } catch (CoreException e) {
      	
      	return null;
      }
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

      String name = storage.getName();
      Object storePackageFragment;
      String packageElementName;

      if (fragment == null) {

        storePackageFragment = "(default package)";
        packageElementName = (String) storePackageFragment;

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
   * @see org.eclipse.jface.viewers.Viewer#getControl()
   */
  public Control getControl() {
    return control;
  }

  /**
   * @see org.eclipse.jface.viewers.IInputProvider#getInput()
   */
  public Object getInput() {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setInput(Object)
   */
  public void setInput(Object input) {
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setSelection(ISelection, boolean)
   */
  public void setSelection(ISelection selection, boolean reveal) {
  }

}
