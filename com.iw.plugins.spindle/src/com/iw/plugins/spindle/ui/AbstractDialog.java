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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public abstract class AbstractDialog extends TitleAreaDialog {

  private ProgressMonitorPart progressMonitorPart;
  private Cursor waitCursor;
  private Cursor arrowCursor;
  private Button okButton;
  private Button cancelButton;
  private MessageDialog dialogClosingDialog;
  private SelectionAdapter cancelListener;
  private boolean operationCancelableState;
  private int pageWidth = SWT.DEFAULT;
  private int pageHeight = SWT.DEFAULT;
  private int activeRunningOperations = 0;
  private boolean needsProgressMonitor = true;
  private Composite dialogContents;

  protected boolean canFinish = false;

  protected String dialogMessage;
  protected String errorMessage;
  protected String dialogDescription;
  protected String windowTitle;
  protected String titleBarMessage;
  protected Image titleBarImage;

  private Composite pageContainer;

  private static int PROGRESS_INDICATOR_HEIGHT = 12;
  private static final String FOCUS_CONTROL = "focusControl";

  /**
   * Constructor for EditPageRefDialog
   */
  public AbstractDialog(Shell shell) {
    super(shell);
    setShellStyle(SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);

    cancelListener = new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        cancelPressed();
      }
    };
  }

  private Object aboutToStart(boolean enableCancelButton) {
    Map savedState = null;
    operationCancelableState = enableCancelButton;
    if (getShell() != null) {
      // Save focus control
      Control focusControl = getShell().getDisplay().getFocusControl();
      if (focusControl != null && focusControl.getShell() != getShell())
        focusControl = null;

      cancelButton.removeSelectionListener(cancelListener);

      // Set the busy cursor to all shells.
      Display d = getShell().getDisplay();
      waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
      setDisplayCursor(waitCursor);

      // Set the arrow cursor to the cancel component.
      arrowCursor = new Cursor(d, SWT.CURSOR_ARROW);
      cancelButton.setCursor(arrowCursor);

      // Deactivate shell
      savedState = saveUIState(needsProgressMonitor && enableCancelButton);
      if (focusControl != null)
        savedState.put(FOCUS_CONTROL, focusControl);

      // Attach the progress monitor part to the cancel button
      if (needsProgressMonitor) {
        progressMonitorPart.attachToCancelComponent(cancelButton);
        progressMonitorPart.setVisible(true);
      }
    }
    return savedState;
  }

  public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
    throws InvocationTargetException, InterruptedException {
    // The operation can only be canceled if it is executed in a separate thread.
    // Otherwise the UI is blocked anyway.
    Object state = aboutToStart(fork && cancelable);
    activeRunningOperations++;
    try {
      ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
    } finally {
      activeRunningOperations--;
      stopped(state);
    }
  }

  protected IProgressMonitor getProgressMonitor() {
    return progressMonitorPart;
  }

  private void stopped(Object savedState) {
    if (getShell() != null) {
      if (needsProgressMonitor) {
        progressMonitorPart.setVisible(false);
        progressMonitorPart.removeFromCancelComponent(cancelButton);
      }
      Map state = (Map) savedState;
      restoreUIState(state);
      cancelButton.addSelectionListener(cancelListener);

      setDisplayCursor(null);
      cancelButton.setCursor(null);
      waitCursor.dispose();
      waitCursor = null;
      arrowCursor.dispose();
      arrowCursor = null;
      Control focusControl = (Control) state.get(FOCUS_CONTROL);
      if (focusControl != null)
        focusControl.setFocus();
    }
  }

  private void setDisplayCursor(Cursor c) {
    Shell[] shells = getShell().getDisplay().getShells();
    for (int i = 0; i < shells.length; i++)
      shells[i].setCursor(c);
  }

  protected Map saveUIState(boolean keepCancelEnabled) {
    Map savedState = new HashMap(10);
    saveEnableStateAndSet(okButton, savedState, "ok", false);
    saveEnableStateAndSet(cancelButton, savedState, "cancel", keepCancelEnabled);
    return savedState;
  }

  private void saveEnableStateAndSet(Control w, Map h, String key, boolean enabled) {
    if (w != null) {
      h.put(key, new Boolean(w.isEnabled()));
      w.setEnabled(enabled);
    }
  }

  protected void restoreUIState(Map state) {
    restoreEnableState(okButton, state, "ok");
    restoreEnableState(cancelButton, state, "cancel");
  }

  private void restoreEnableState(Control w, Map h, String key) {
    if (w != null) {
      Boolean b = (Boolean) h.get(key);
      if (b != null)
        w.setEnabled(b.booleanValue());
    }
  }

  protected boolean okToClose() {
    if (activeRunningOperations > 0) {
      synchronized (this) {
        dialogClosingDialog = createDialogClosingDialog();
      }
      dialogClosingDialog.open();
      synchronized (this) {
        dialogClosingDialog = null;
      }
      return false;
    }

    return performCancel();
  }

  protected void cancelPressed() {
    if (activeRunningOperations <= 0) {
      // Close the dialog. The check whether the dialog can be
      // closed or not is done in <code>okToClose</code>.
      // This ensures that the check is also evaluated when the user
      // presses the window's close button.
      setReturnCode(CANCEL);
      close();
    } else {
      cancelButton.setEnabled(false);
    }
  }

  public boolean close() {
    if (okToClose())
      return hardClose();
    else
      return false;
  }

  protected boolean hardClose() {
    // dispose any contained stuff

    return super.close();
  }

  private MessageDialog createDialogClosingDialog() {
    MessageDialog result =
      new MessageDialog(
        getShell(),
        "Still Processing!",
        null,
        "Try cancelling again when the background processing is done",
        MessageDialog.QUESTION,
        new String[] { IDialogConstants.OK_LABEL },
        0);
    return result;
  }

  protected abstract boolean performCancel();

  protected Control createButtonBar(Composite parent) {
    Composite composite = (Composite) super.createButtonBar(parent);
    ((GridLayout) composite.getLayout()).makeColumnsEqualWidth = false;
    return composite;
  }

  protected void createButtonsForButtonBar(Composite parent) {
    okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    cancelButton = createCancelButton(parent);
  }

  private Button createCancelButton(Composite parent) {
    // increment the number of columns in the button bar
     ((GridLayout) parent.getLayout()).numColumns++;

    Button button = new Button(parent, SWT.PUSH);

    button.setText(IDialogConstants.CANCEL_LABEL);
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
    int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
    data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    button.setLayoutData(data);

    button.setData(new Integer(IDialogConstants.CANCEL_ID));
    button.addSelectionListener(cancelListener);
    return button;
  }

  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite) super.createDialogArea(parent);
    composite.setFont(parent.getFont());

    // Build the Page container
    pageContainer = createAreaContents(composite);

    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gd.widthHint = pageWidth;
    gd.heightHint = pageHeight;

    //gd.horizontalAlignment = gd.HORIZONTAL_ALIGN_BEGINNING;
    gd.verticalAlignment = gd.VERTICAL_ALIGN_BEGINNING;
    pageContainer.setLayoutData(gd);
    pageContainer.setFont(parent.getFont());

    // Insert a progress monitor 
    GridLayout pmlayout = new GridLayout();
    pmlayout.numColumns = 1;
    progressMonitorPart = new ProgressMonitorPart(composite, pmlayout, SWT.DEFAULT);
    progressMonitorPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    progressMonitorPart.setVisible(true);

    // Build the separator line
    Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    return composite;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    updateWidth();

  }

  public Button getOkButton() {
    return okButton;
  }

  private void updateWidth() {
    // ensure dialog is large enough
    Point delta = calculateNeededWidthDelta();

    if (delta.x > 0) {
      // increase the size of the shell 
      Shell shell = getShell();
      Point shellSize = shell.getSize();
      shell.setSize(shellSize.x + delta.x, shellSize.y);
    }
  }

  private Point calculateNeededWidthDelta() {

    if (pageContainer == null)
      // control not created yet
      return new Point(0, 0);

    Point contentSize = pageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    Rectangle rect = pageContainer.getClientArea();
    Point containerSize = new Point(rect.width, rect.height);

    return new Point(
      Math.max(0, contentSize.x - containerSize.x),
      Math.max(0, contentSize.y - containerSize.y));

  }

  protected abstract Composite createAreaContents(Composite parent);

  protected void update() {
    // Update the window title
    updateWindowTitle();

    // Update the title bar
    updateTitleBar();

    // Update the message line
    updateMessage();

    // Update the buttons
    updateButtons();
  }

  public void updateMessage(String message) {
    dialogMessage = message;
    updateMessage();
  }

  public void updateMessage() {
    if (getTitleArea() == null) {
      return;
    }
    if (dialogMessage == null) {
      setMessage(dialogDescription);
    } else {
      setMessage(dialogMessage);
    }
    setErrorMessage(errorMessage);
  }

  public void updateButtons() {
    if (okButton == null) {
      return;
    }
    okButton.setEnabled(okToClose());
    if (canFinish) {
      getShell().setDefaultButton(okButton);
    }
  }

  private void updateDescriptionMessage(String description) {
    dialogDescription = description;
    if (dialogMessage == null) {
      setMessage(dialogDescription);
    }
  }

  private void updateDescriptionMessage() {
    if (dialogMessage == null) {
      setMessage(dialogDescription);
    }
  }

  public void updateTitleBar(String titleBarMessage, Image image) {
    this.titleBarMessage = titleBarMessage;
    this.titleBarImage = image;
    updateTitleBar();
  }

  public void updateTitleBar() {
    if (getTitleArea() == null)
      return;
    if (titleBarMessage == null) {
      titleBarMessage = "";
    }
    setTitle(titleBarMessage);
    setTitleImage(titleBarImage);
    updateDescriptionMessage();
    updateMessage();
  }

  public void updateWindowTitle(String title) {
    this.windowTitle = title;
    updateWindowTitle();
  }

  public void updateWindowTitle() {
    if (getShell() == null) {
      // Not created yet
      return;
    }
    if (windowTitle == null) {
      windowTitle = "";
    }
    getShell().setText(windowTitle);
  }
  
  public void create() {
  	super.create();
  	update();
  } 
  
 
}