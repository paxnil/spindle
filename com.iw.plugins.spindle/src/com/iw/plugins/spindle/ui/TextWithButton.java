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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

public abstract class TextWithButton {

  private Composite editor;

  private Control contents;

  private Text defaultText;
  
  private boolean dirty = false;

  /**
   * The button.
   */
  private Button button;

  public TextWithButton(Composite parent) {

    createControl(parent);

  }

  protected Button createButton(Composite parent) {
    Button result = new Button(parent, SWT.ARROW | SWT.DOWN);
    return result;
  }

  protected Control createContents(Composite parent) {
    defaultText = new Text(parent, SWT.BORDER);
    return defaultText;
  }

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

    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        String newValue = openDialogBox(editor);
        if (newValue != null) {
          setText(newValue);
        }
      }
    });

    return editor;
  }

  public Text getDefaultText() {
    return defaultText;
  }
  
  public String getText() {
    return defaultText.getText();
  }

  protected abstract String openDialogBox(Control cellEditorWindow);

  protected abstract void textDirty();
  
  public void commit() {
    dirty = false;
  }

  public void setText(String value) {
    setText(value, true);
  }

  public void setText(String value, boolean fireEvent) {
    if (defaultText == null)
      return;

    String text = "";
    if (value != null) {
      text = value;
    }
    defaultText.setText(text);    
    if (fireEvent) {
      dirty = true;
      textDirty();
    }
  }
  
  public void setEditable(boolean flag) {
    defaultText.setEditable(flag);
    button.setEnabled(flag);
  }
  
  public boolean isDirty() {
    return dirty;
  }

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

}