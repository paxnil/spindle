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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class ComboBoxPropertyDescriptor extends PropertyDescriptor {

  private String[] values;
  private boolean editable;

  public ComboBoxPropertyDescriptor(
    Object id,
    String displayName,
    String[] valuesArray,
    boolean editable) {
    super(id, displayName);
    values = valuesArray;
    setLabelProvider(new ILabelProvider() {
      public String getText(Object element) {
        return values[((Integer) element).intValue()];
      }
      public Image getImage(Object element) {
        return null;
      };
      public boolean isLabelProperty(Object element, String value) {
        return false;
      }
      public void dispose() {
      }
      public void addListener(ILabelProviderListener listener) {
      }
      public void removeListener(ILabelProviderListener listener) {
      }
    });
  }

  public CellEditor createPropertyEditor(Composite parent) {
    CellEditor editor =
      new com.iw.plugins.spindle.ui.ComboBoxCellEditor(parent, values, editable);
    if (getValidator() != null)
      editor.setValidator(getValidator());
    return editor;
  }

}