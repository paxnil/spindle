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

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

public abstract class EditableDialogCellEditor extends TextCellEditor {

  private Composite editor;

  private Control contents;

  private Text defaultText;

  /**
   * The button.
   */
  private Button button;

  /**
   * Internal class for laying out the dialog.
   */
  private class DialogCellLayout extends Layout {
    public void layout(Composite editor, boolean force) {
      Rectangle bounds = editor.getClientArea();
      Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
      if (contents != null)
        contents.setBounds(0, 0, bounds.width - size.x, bounds.height);
      button.setBounds(bounds.width - size.x, 0, size.x, bounds.height);
    }
    public Point computeSize(Composite editor, int wHint, int hHint, boolean force) {
      if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
        return new Point(wHint, hHint);
      Point contentsSize = contents.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
      Point buttonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
      // Just return the button width to ensure the button is not clipped
      // if the label is long.
      // The label will just use whatever extra width there is
      Point result = new Point(buttonSize.x, Math.max(contentsSize.y, buttonSize.y));
      return result;
    }
  }
  /**
   * Creates a new dialog cell editor parented under the given control. 
   * @param parent the parent control
   */
  protected EditableDialogCellEditor(Composite parent) {
    super(parent);
  }
  /**
   * Creates the button for this cell editor under the given parent control.   
   * @param parent the parent control
   * @return the new button control
   */
  protected Button createButton(Composite parent) {
    Button result = new Button(parent, SWT.ARROW | SWT.DOWN);
    return result;
  }
  /**
   * Creates the controls used to show the value of this cell editor.  
   * @param cell the control for this cell editor
   */
  protected Control createContents(Composite cell) {
    defaultText = (Text) super.createControl(cell);
    return defaultText;
  }

  /* (non-Javadoc)
   * Method declared on CellEditor.
   */
  protected Control createControl(Composite parent) {

    Font font = parent.getFont();
    Color bg = parent.getBackground();

    editor = new Composite(parent, SWT.NONE);
    editor.setFont(font);
    editor.setBackground(bg);
    editor.setLayout(new DialogCellLayout());

    contents = createContents(editor);

    button = createButton(editor);
    button.setFont(font);

    button.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.character == '\u001b') { // Escape
          fireCancelEditor();
        }
      }
    });

    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        Object newValue = openDialogBox(editor);
        if (newValue != null) {
          boolean oldValidState = isValueValid();
          boolean newValidState = isCorrect(newValue);
          if (newValidState) {
            doSetValue(newValue);
          } else {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { newValue.toString()}));
          }
          fireApplyEditorValue();
        }
      }
    });

    setValueValid(true);

    return editor;
  }
  /* (non-Javadoc)
   * Method declared on CellEditor.
   */
  protected Object doGetValue() {
    return defaultText.getText();
  }
  /* (non-Javadoc)
   * Method declared on CellEditor.
   * The focus is set to the cell editor's button. 
   */
  protected void doSetFocus() {
    if (defaultText != null) {
      defaultText.selectAll();
      defaultText.setFocus();
    }
  }
  /* (non-Javadoc)
   * Method declared on CellEditor.
   */
  protected void doSetValue(Object value) {
    updateContents(value);
  }
  /**
   * Returns the default text widget created by <code>createContents</code>.
   *
   * @return the default text widget
   */
  protected Text getDefaultText() {
    return defaultText;
  }
  /**
   * Opens a dialog box under the given parent control and returns the
   * dialog's value when it closes.  
   * @param cellEditorWindow the parent control cell editor's window
   *   so that a subclass can adjust the dialog box accordingly
   * @return the selected value
   */
  protected abstract Object openDialogBox(Control cellEditorWindow);
  /**
   * Updates the controls showing the value of this cell editor.
   *   
   *
   * @param value the new value of this cell editor
   */
  protected void updateContents(Object value) {
    if (defaultText == null)
      return;

    String text = ""; //$NON-NLS-1$
    if (value != null)
      text = value.toString();
    defaultText.setText(text);
  }

}