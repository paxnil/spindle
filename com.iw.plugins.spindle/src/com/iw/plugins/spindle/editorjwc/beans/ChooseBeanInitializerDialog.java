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
package com.iw.plugins.spindle.editorjwc.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.tapestry.bean.ExpressionBeanInitializer;
import net.sf.tapestry.bean.FieldBeanInitializer;
import net.sf.tapestry.bean.PropertyBeanInitializer;
import net.sf.tapestry.bean.StaticBeanInitializer;
import net.sf.tapestry.bean.StringBeanInitializer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.ChooseFromListDialog;

public class ChooseBeanInitializerDialog extends ChooseFromListDialog {

  static private String[] COLUMN_HEADERS = { "property", "java-type" };
  static private ColumnLayoutData COLUMN_LAYOUTS[] =
    { new ColumnPixelData(200), new ColumnPixelData(250)};

  private String beanClass;
  private List beanProperties;
  private Set existingProperties;
  private Table table;
  private TableViewer viewer;
  private List chosenProperties = Collections.EMPTY_LIST;
  private IJavaProject project;

  public ChooseBeanInitializerDialog(
    Shell shell,
    int DTDVersion,
    IJavaProject project,
    String beanClass,
    Set existingProperties) {
    super(
      shell,
      getInitializerLabels(DTDVersion),
      getInitializerClasses(DTDVersion),
      "Choose Bean Initializer Type");
    this.project = project;
    beanProperties = getBeanProperties(beanClass, existingProperties);
    this.beanClass = beanClass;

  }

  private static String[] getInitializerLabels(int DTDVersion) {

    if (DTDVersion < XMLUtil.DTD_1_3) {
      return new String[] { "Property", "Static", "Field" };
    }

    return new String[] { "Expression", "String" };

  }

  private static Object[] getInitializerClasses(int DTDVersion) {

    if (DTDVersion < XMLUtil.DTD_1_3) {
      return new Object[] {
        PropertyBeanInitializer.class,
        StaticBeanInitializer.class,
        FieldBeanInitializer.class };
    }

    return new Object[] { ExpressionBeanInitializer.class, StringBeanInitializer.class, };

  }

  protected void okPressed() {
    if (viewer != null) {
    	
      IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
      if (!selection.isEmpty()) {
      	
        chosenProperties = new ArrayList();
        
        for (Iterator iter = selection.toList().iterator(); iter.hasNext();) {
          PropertyDescriptor element = (PropertyDescriptor) iter.next();
          chosenProperties.add(element.getName());
        }

      }
    }
    super.okPressed();
  }

  public Class getSelectedIntializerClass() {
    Object selected = getSelectedResult();
    if (selected != null) {
      return (Class) selected;
    }
    return null;
  }

  public java.util.List getPropertyNames() {
    return chosenProperties;
  }

  private List getBeanProperties(String beanClass, Set existingProperties) {

    BeanInfo info = null;
    PropertyDescriptor[] descriptors = null;

    try {

      Class clazz = getClass().forName(beanClass);
      info = Introspector.getBeanInfo(clazz);
      descriptors = info.getPropertyDescriptors();

    } catch (ClassNotFoundException e) {
    } catch (IntrospectionException e) {
    }

    if (info == null) {

      descriptors = getBeanPropertiesFromEclipse(beanClass);

    }

    ArrayList result = new ArrayList();
    for (int i = 0; i < descriptors.length; i++) {

      if (!existingProperties.contains(descriptors[i].getName())) {

        result.add(descriptors[i]);

      }
    }
    return result;

  }

  private PropertyDescriptor[] getBeanPropertiesFromEclipse(String beanClass) {

    return new PropertyDescriptor[0];

  }

  protected Control createDialogArea(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    GridData gd;
    GridLayout layout = new GridLayout();
    layout.verticalSpacing = 8;
    container.setLayout(layout);

    if (!beanProperties.isEmpty()) {

      Label componentNameLabel = new Label(container, SWT.NULL);
      gd = new GridData(GridData.FILL_BOTH);
      componentNameLabel.setLayoutData(gd);
      componentNameLabel.setText(beanClass);

      table = createTable(container);
      gd = new GridData(GridData.FILL_BOTH);
      gd.heightHint = 200;
      gd.widthHint = 500;
      table.setLayoutData(gd);
      createColumns();
      viewer = new TableViewer(table);
      viewer.setLabelProvider(new PropertyLabelProvider());
      viewer.setContentProvider(new PropertyContentProvider());
      viewer.setInput(beanProperties);
    }
    super.createDialogArea(container);
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

  public class PropertyContentProvider implements IStructuredContentProvider {

    public PropertyContentProvider() {
      super();
    }
    public Object[] getElements(Object obj) {
      ArrayList properties = (ArrayList) obj;
      return properties.toArray();
    }

    public void dispose() {
    }

    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
    }

  }

  protected class PropertyLabelProvider implements ITableLabelProvider {

    public PropertyLabelProvider() {
      super();
    }

    public Image getColumnImage(Object element, int index) {
      return null;
    }

    public String getColumnText(Object element, int index) {

      PropertyDescriptor descriptor = (PropertyDescriptor) element;
      if (index == 0) {
        return descriptor.getName();
      }

      if (index == 1) {
      	
        Class clazz = descriptor.getPropertyType();
        
        if (clazz == null) {
        	
        	return "Unknown";
        	
        }

        return clazz.getName();
      }

      return "";
    }

    public void addListener(ILabelProviderListener element) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener element) {
    }

  }

}
