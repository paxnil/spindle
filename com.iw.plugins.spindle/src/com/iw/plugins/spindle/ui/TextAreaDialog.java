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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TextAreaDialog extends TitleAreaDialog {

  private String title;
  private boolean editing;
  private Text text;
  private Font font;
  private String openText;
  private String message;

  private String result;

  /**
   * Constructor for PageRefDialog
   */
  public TextAreaDialog(Shell shell, String title, String message) {
    super(shell);
    this.title = title == null ? "" : title;
    this.message = message == null ? "" : message;

  }

  /**
   * @see AbstractDialog#createAreaContents(Composite)
   */
  protected Control createDialogArea(Composite parent) {

    Composite container =(Composite)super.createDialogArea(parent);
    GridData gd;
    Control control;

    Composite innerContainer = new Composite(container, SWT.NULL);
    gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    innerContainer.setLayoutData(gd);
    GridLayout layout = new GridLayout();
    layout = new GridLayout();
    innerContainer.setLayout(layout);

    text = new Text(innerContainer, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    gd.heightHint = convertHeightInCharsToPixels(20);
    gd.widthHint = convertWidthInCharsToPixels(45);
    text.setLayoutData(gd);

    text.setText(openText);

    FontData data = new FontData("courier", SWT.NULL, 6);
    // on windows platform, I need to do the following
    // even though the values are the same as the constructor's
    data.setStyle(SWT.NORMAL);
    data.setHeight(6);
    font = new Font(text.getDisplay(), data);
    text.setFont(font); 
    
    setTitle(title);   
    setMessage(message, IMessageProvider.NONE);

    return container;
  }

  /**
   * @see AbstractDialog#performCancel()
   */
  protected boolean performCancel() {

    return true;

  }

  protected void okPressed() {
    setReturnCode(OK);
    result = text.getText();
    super.okPressed();
  }

  protected void cancelPressed() {
    setReturnCode(CANCEL);
    close();
  }

  public boolean close() {
    return super.close();
  }

  protected boolean okToClose() {
    return true;
  }

  public int open(String existingText) {
    openText = (existingText == null ? "" : existingText);
    return super.open();
  }

  public String getResult() {
    return result;
  }
}