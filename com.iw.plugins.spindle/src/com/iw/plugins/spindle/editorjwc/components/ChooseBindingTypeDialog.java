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
package com.iw.plugins.spindle.editorjwc.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.sf.tapestry.spec.BindingType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginParameterSpecification;
import com.iw.plugins.spindle.ui.ChooseFromListDialog;
import com.iw.plugins.spindle.ui.IToolTipProvider;
import com.iw.plugins.spindle.ui.TableViewerWithToolTips;
import com.iw.plugins.spindle.ui.UneditableComboBox;

public class ChooseBindingTypeDialog extends ChooseFromListDialog {

  public static final String DISABLE_TOOLTIPS_PREFERENCE = "ChooseBindingTypeDialog.tooltips";

  private HashMap precomputed;
  private HashMap recomputed;
  private HashMap parameterMap = new HashMap();
  private Set existingBindingParameters;
  private TapestryComponentModel component;

  private UneditableComboBox combo;
  private Label componentNameLabel;
  private Table table;
  private TableViewerWithToolTips viewer;

  private java.util.List chosenParameters = Collections.EMPTY_LIST;

  static private String[] COLUMN_HEADERS = { "name", "required", "java-type", "description" };
  static private ColumnLayoutData COLUMN_LAYOUTS[] =
    { new ColumnPixelData(100), new ColumnPixelData(75), new ColumnPixelData(100), new ColumnPixelData(300)};

  public ChooseBindingTypeDialog(Shell shell, HashMap precomputedAliasInfo, Set existingBindingParameters, boolean isDTD12) {
    this(shell, isDTD12);
    this.precomputed = precomputedAliasInfo;
    this.existingBindingParameters = existingBindingParameters;
  }

  public ChooseBindingTypeDialog(Shell shell, TapestryComponentModel component, Set existingBindingParameters, boolean isDTD12) {
    this(shell, isDTD12);
    this.component = component;
    this.existingBindingParameters = existingBindingParameters;
    populateParameters(component);
  }

  public ChooseBindingTypeDialog(Shell shell, boolean isDTD12) {
    super(shell, (isDTD12 ? new String[] { "Dynamic", "Field", "Inherited", "Static", "String" }
    : new String[] { "Dynamic", "Field", "Inherited", "Static" }),
      (isDTD12
        ? new Object[] { BindingType.DYNAMIC, BindingType.FIELD, BindingType.INHERITED, BindingType.STATIC, BindingType.STRING }
    : new Object[] { BindingType.DYNAMIC, BindingType.FIELD, BindingType.INHERITED, BindingType.STATIC }), "New Binding");
  }

  public static void initializeDefaults(IPreferenceStore pstore) {
    pstore.setDefault(DISABLE_TOOLTIPS_PREFERENCE, false);
  }

  protected Control createDialogArea(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    GridData gd;
    GridLayout layout = new GridLayout();
    layout.verticalSpacing = 8;
    container.setLayout(layout);
    gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL);
    container.setLayoutData(gd);
    if (precomputed != null) {
      combo = new UneditableComboBox(container, SWT.NULL);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      combo.setLayoutData(gd);
      combo.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent event) {
          selectComponent(combo.getSelectionIndex());
        }

        public void widgetDefaultSelected(SelectionEvent event) {
        }
      });
      createItems();
      combo.select(0);
      component = (TapestryComponentModel) (recomputed.get(combo.getItem(0)));
    }
    if (component != null) {
      componentNameLabel = new Label(container, SWT.NULL);
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.BEGINNING);
      componentNameLabel.setLayoutData(gd);

      table = createTable(container);
      gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
      gd.heightHint = 100;
      gd.widthHint = 400;
      table.setLayoutData(gd);
      createColumns();
      viewer = new BindingTableViewer(table);
      BTLabelProvider prov = new BTLabelProvider();
      viewer.setToolTipProvider(prov);
      viewer.setLabelProvider(prov);
      viewer.setContentProvider(new BTContentProvider());
      componentNameLabel.setText(component.getUnderlyingStorage().getFullPath().toString());
      viewer.setInput(parameterMap.get(component));
    }
    Control radios = createButtonGroup(container);
    gd = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
    radios.setLayoutData(gd);
    //super.createDialogArea(container);
    return container;
  }

  private Table createTable(Composite parent) {
    Table result = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
    result.setLinesVisible(true);
    return result;
  }

  private void createColumns() {
    TableLayout layout = new TableLayout();
    table.setLayout(layout);
    table.setHeaderVisible(true);
    for (int i = 0; i < COLUMN_HEADERS.length; i++) {
      layout.addColumnData(COLUMN_LAYOUTS[i]);
      TableColumn tc = new TableColumn(table, SWT.NONE, i);
      tc.setResizable(COLUMN_LAYOUTS[i].resizable);
      tc.setText(COLUMN_HEADERS[i]);
    }
  }

  protected void okPressed() {
    if (viewer != null) {
      IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
      if (!selection.isEmpty()) {
        chosenParameters = selection.toList();
      }
    }
    super.okPressed();
  }

  private void selectComponent(int index) {
    component = (TapestryComponentModel) recomputed.get(combo.getItem(index));
    componentNameLabel.setText(component.getUnderlyingStorage().getFullPath().toString());
    viewer.setInput(parameterMap.get(component));
  }

  private void createItems() {
    recomputed = new HashMap();
    parameterMap = new HashMap();
    Iterator iter = precomputed.keySet().iterator();
    while (iter.hasNext()) {
      TapestryApplicationModel appModel = (TapestryApplicationModel) iter.next();
      TapestryComponentModel cmodel = (TapestryComponentModel) precomputed.get(appModel);
      String newKey = appModel.getUnderlyingStorage().getFullPath().toString();
      combo.add(newKey);
      recomputed.put(newKey, cmodel);
      populateParameters(cmodel);
    }
  }

  private void populateParameters(TapestryComponentModel componentModel) {
    PluginComponentSpecification componentSpec = componentModel.getComponentSpecification();
    ArrayList availableParameters = new ArrayList();
    parameterMap.put(componentModel, availableParameters);
    if (componentSpec == null) {
      try {
        // Load the component and
        componentModel.load();
        // Try again
        componentModel.getComponentSpecification();
      } catch (CoreException e) {
      }
    }
    if (componentSpec != null) {
      // lets find the parms not yet bound
      Iterator parameterNames = new TreeSet(componentSpec.getParameterNames()).iterator();
      while (parameterNames.hasNext()) {
        String parameter = (String) parameterNames.next();
        if (!existingBindingParameters.contains(parameter)) {
          availableParameters.add(parameter);
        }
      }
    }
  }

  public BindingType getSelectedBindingType() {
    Object selected = getSelectedResult();
    if (selected != null) {
      return (BindingType) selected;
    }
    return null;
  }

  public java.util.List getParameterNames() {
    return chosenParameters;
  }

  protected class BTLabelProvider implements IToolTipProvider, ITableLabelProvider {

    /**
     * Constructor for BTLabelProvider
     */
    public BTLabelProvider() {
      super();
    }

    //---------- IToolTipProvider ----------------------------//

    public String getToolTipText(Object object) {
      String parameter = (String) object;
      PluginParameterSpecification spec =
        (PluginParameterSpecification) component.getComponentSpecification().getParameter(parameter);
      if (spec == null) {
        return null;
      }
      String description = spec.getDescription();
      if (description != null && !"".equals(description.trim())) {
        return parameter + "\n" + description;
      }
      return "No description found for '" + parameter + "'";
    }

    public Image getToolTipImage(Object object) {
      return null;
    }

    //---------- ITableLabelProvider -------------------------//
    /**
     * @see ITableLabelProvider#getColumnImage(Object, int)
     */
    public Image getColumnImage(Object element, int index) {
      return null;
    }

    /**
     * @see ITableLabelProvider#getColumnText(Object, int)
     */
    public String getColumnText(Object element, int index) {
      String parameterName = (String) element;
      if (index == 0) {
        return parameterName;
      }
      PluginParameterSpecification pspec =
        (PluginParameterSpecification) ((PluginComponentSpecification) component.getComponentSpecification()).getParameter(
          parameterName);
      if (index == 1) {
        if (pspec.isRequired()) {
          return "YES";
        } else {
          return "NO";
        }
      }
      if (index == 2) {
        String type = pspec.getType();
        if (type != null && !"".equals(type.trim())) {
          return type;
        } else {
          return "";
        }
      }
      if (index == 3) {
        String description = pspec.getDescription();
        if (description != null && !"".equals(description.trim())) {
          return description;
        } else {
          return "No description found";
        }
      }
      return "";
    }

    /**
     * @see IBaseLabelProvider#addListener(ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener element) {
    }

    /**
     * @see IBaseLabelProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see IBaseLabelProvider#isLabelProperty(Object, String)
     */
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    /**
     * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener element) {
    }

  }

  public class BTContentProvider implements IStructuredContentProvider {

    /**
     * Constructor for BTContentProvider
     */
    public BTContentProvider() {
      super();
    }

    /**
     * @see IContentProvider#getElement(Object)
     */
    public Object[] getElements(Object obj) {
      ArrayList parameterNames = (ArrayList) obj;
      return parameterNames.toArray();
    }

    /**
     * @see IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see IContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
    }

  }

  public class BindingTableViewer extends TableViewerWithToolTips {

    private IPropertyChangeListener preferenceListener;

    public BindingTableViewer(Table table) {
      super(table);
    }

    /**
     * @see com.iw.plugins.spindle.ui.TableViewerWithToolTips#hookTooltips(Control)
     */
    protected void hookControl(Control control) {
      super.hookControl(control);
      IPreferenceStore store = TapestryPlugin.getDefault().getPreferenceStore();
      setTooltipsEnabled(!store.getBoolean(DISABLE_TOOLTIPS_PREFERENCE));
      
      preferenceListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
          if (event.getProperty().equals(DISABLE_TOOLTIPS_PREFERENCE)) {
            setTooltipsEnabled(!((Boolean) event.getNewValue()).booleanValue());
          }
        }
      };
      TapestryPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
      
      control.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
          TapestryPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
        }
      });
    }

  }

}
