package com.iw.plugins.spindle.editors;

import java.util.HashMap;
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
import com.iw.plugins.spindle.ui.AbstractDialog;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.TapestryLookup;

public class ChooseComponentDialog extends AbstractDialog {

  private Text componentNameText;
  private Table components;
  private Table packages;
  private ScanCollector collector = new ScanCollector();
  private ILabelProvider nameLabelProvider = new ComponentLabelProvider();
  private ILabelProvider packageLabelProvider = new JavaElementLabelProvider();

  private TapestryLookup lookup;

  private String resultComponent;
  
  private int acceptFlags = TapestryLookup.ACCEPT_COMPONENTS;

  static private final Object[] empty = new Object[0];

  /**
    * Constructor for PageRefDialog
    */
  public ChooseComponentDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {
    super(shell);
    updateWindowTitle(windowTitle);
    updateMessage(description);
    configure(project);

  }
  
  public ChooseComponentDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description,
    int acceptFlags
    ) {
    this(shell, project, windowTitle, description);
    this.acceptFlags = acceptFlags;
  }

  public void create() {
    super.create();
    componentNameText.setFocus();
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

    componentNameText = createText(container);
    components = createUpperList(container);
    packages = createLowerList(container);

    //a little trick to make the window come up faster
    String initialFilter = "A";
    if (initialFilter != null) {
      componentNameText.setText(initialFilter);
      componentNameText.selectAll();
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
    String componentBit = componentNameText.getText().trim();
    if ("".equals(componentBit)) {
      updateListWidget(empty, components, nameLabelProvider);
    } else {
      collector.reset();
      lookup.findAll(componentBit, true, acceptFlags, collector);
      updateListWidget(collector.getComponentNames(), components, nameLabelProvider);
    }
  }

  public void dispose() {
    componentNameText.dispose();
    components.dispose();
    packages.dispose();
    lookup = null;
  }

  private Text createText(Composite parent) {
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
    (new Label(parent, SWT.NONE)).setText("choose component:");

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
    spec.widthHint = convertWidthInCharsToPixels(50);
    spec.heightHint = convertHeightInCharsToPixels(15);
    spec.grabExcessVerticalSpace = true;
    spec.grabExcessHorizontalSpace = true;
    spec.horizontalAlignment = spec.FILL;
    spec.verticalAlignment = spec.FILL;
    list.setLayoutData(spec);
    return list;
  }

  protected void handleUpperSelectionChanged() {
    int selection = components.getSelectionIndex();
    if (selection >= 0) {
      String name = components.getItem(selection).getText();
      updateListWidget(collector.getPackagesForName(name), packages, packageLabelProvider);
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
    spec.widthHint = convertWidthInCharsToPixels(50);
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
    if (table == components) {
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

    int i = components.getSelectionIndex();
    int j = packages.getSelectionIndex();

    if (i < 0 || j < 0) {
      resultComponent = null;
      return null;
    }
    resultComponent = "/" + packages.getItem(j).getText() + "/";
    resultComponent = resultComponent.replace('.', '/') + components.getItem(i).getText();
    return resultComponent;
  }

  public String getResultComponent() {
    return resultComponent;
  }

  protected class ScanCollector implements ITapestryLookupRequestor {

    Map results;

    /**
     * Constructor for ScanCollector
     */
    public ScanCollector() {
      super();
      reset();
    }

    public void reset() {
      results = new HashMap();
    }

    public Map getResults() {
      return results;
    }

    public Object[] getComponentNames() {
      if (results == null) {
        return empty;
      }
      return new TreeSet(results.keySet()).toArray();
    }

    public Object[] getPackagesForName(String name) {
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
      String name = storage.getFullPath().lastSegment();
      String packageFragName = (fragment == null ? "default package" : fragment.getElementName());
      Set packages = (Set) results.get(name);
      if (packages == null) {
        packages = new TreeSet();
        packages.add(packageFragName);
        results.put(name, packages);
      } else if (!packages.contains(packageFragName)) {
        packages.add(packageFragName);
      }
      return true;
    }
  }

  protected class ComponentLabelProvider implements ILabelProvider, IBaseLabelProvider {

    Image image;

    public Image getImage(Object element) {
      if (image == null) {
        image = TapestryImages.getSharedImage("component16.gif");
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
}