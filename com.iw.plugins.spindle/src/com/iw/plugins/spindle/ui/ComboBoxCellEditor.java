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

import java.text.MessageFormat;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ComboBoxCellEditor extends CellEditor {

  private String[] items;

  private int selection;

  protected UneditableComboBox comboBox;
  
  private int poo = 1 << 3;

  private int ccomboFlags = SWT.READ_ONLY;
  /**    
   * @param parent the parent control
   * @param items the list of strings for the combo box
   */
  public ComboBoxCellEditor(Composite parent, String[] items, boolean editable) {
    super(parent);
    setEditable(editable);
    Assert.isNotNull(items);
    this.items = items;
    selection = 0;
    populateComboBoxItems();    
  }

  public void setEditable(boolean flag) {
    if (flag) {
      ccomboFlags = SWT.READ_ONLY;
    } else {
      ccomboFlags = SWT.NONE;
    }
  }

  protected Control createControl(Composite parent) {

    comboBox = new UneditableComboBox(parent, ccomboFlags);
    comboBox.setFont(parent.getFont());   
    comboBox.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        keyReleaseOccured(e);
      }
    });

    comboBox.addSelectionListener(new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent event) {
        // must set the selection before getting value
        selection = comboBox.getSelectionIndex();
        Object newValue = doGetValue();
        boolean newValidState = isCorrect(newValue);
        if (newValidState) {
          doSetValue(newValue);
        } else {
          // try to insert the current value into the error message.
          setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { items[selection] }));
        }
        fireApplyEditorValue();
      }
    });

    comboBox.addTraverseListener(new TraverseListener() {
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
          e.doit = false;
        }
      }
    });

    return comboBox;
  }
 
  /** @return the zero-based index of the current selection wrapped
   *  as an <code>Integer</code>
   */
  protected Object doGetValue() {
    return new Integer(selection);
  }

  protected void doSetFocus() {
    comboBox.setFocus();
  }

  protected void doSetValue(Object value) {
    Assert.isTrue(comboBox != null && (value instanceof Integer));
    selection = ((Integer) value).intValue();
    comboBox.select(selection);
  }

  private void populateComboBoxItems() {
    if (comboBox != null && items != null) {
      for (int i = 0; i < items.length; i++)
        comboBox.add(items[i], i);

      setValueValid(true);
    }
  }
}
