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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

public class ChooseDialog extends TitleAreaDialog {

  private TreeViewer viewer;
  private IStructuredContentProvider contentProvider;
  private ILabelProvider labelProvider;
  private int heightInCharacters = 10;
  private int widthInCharacters = 10;

  private ISelection resultSelection;

  private int tableFlags = SWT.SINGLE;

  /**
   * Constructor for ChooseDialog.
   * @param parentShell
   */
  public ChooseDialog(Shell parentShell, String title, int tableFlags) {

    this(parentShell, title);
    Assert.isTrue(tableFlags == SWT.MULTI || tableFlags == SWT.SINGLE);
    this.tableFlags = tableFlags;
  }

  public ChooseDialog(Shell parentShell, String title) {
    super(parentShell);
    setTitle(title);
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
   */
  protected Control createDialogArea(Composite parent) {

    Composite container = new Composite(parent, SWT.NULL);
    container.setFont(parent.getFont());

    GridLayout layout = new GridLayout();
    layout.marginWidth = 4;
    layout.marginHeight = 4;
    container.setLayout(layout);

    container.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    Tree tree = createTable(container);
    tree.setFont(container.getFont());

    PixelConverter converter = new PixelConverter(tree);

    GridData data = new GridData(GridData.FILL_BOTH);
    data.widthHint = converter.convertVerticalDLUsToPixels(widthInCharacters);
    data.heightHint = converter.convertHorizontalDLUsToPixels(heightInCharacters);
    tree.setLayoutData(data);

    viewer = new TreeViewer(tree);
    viewer.setContentProvider(contentProvider);
    viewer.setLabelProvider(labelProvider);

    viewer.setInput("fake");

    return container;

  }

  protected void addControl(Control toBeAdded, Control parent, int verticalOffset) {
    FormData formData = new FormData();
    formData.top = new FormAttachment(parent, verticalOffset);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    toBeAdded.setLayoutData(formData);
  }

  private Tree createTable(Composite parent) {

    Tree tree = new Tree(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | tableFlags);

    tree.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event evt) {
        handleDoubleClick();
      }
    });

    return tree;
  }

  /**
   * Method handleDoubleClick.
   */
  private void handleDoubleClick() {
    resultSelection = viewer.getSelection();
    setReturnCode(OK);
    close();
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {
    resultSelection = viewer.getSelection();
    super.okPressed();
  }

  public void setSelected(Object obj) {

    IStructuredSelection selection;
    if (obj != null) {
      selection = new StructuredSelection(obj);
    } else {

      selection = new StructuredSelection();

    }

    setSelection(selection);

  }

  public ISelection getSelection() {

    return resultSelection;

  }

  public void setSelection(IStructuredSelection selection) {

    viewer.setSelection(selection);
  }

  /**
   * Returns the contentProvider.
   * @return IStructuredContentProvider
   */
  public IStructuredContentProvider getContentProvider() {
    return contentProvider;
  }

  /**
   * Returns the labelProvider.
   * @return ILabelProvider
   */
  public ILabelProvider getLabelProvider() {
    return labelProvider;
  }

  /**
   * Sets the contentProvider.
   * @param contentProvider The contentProvider to set
   */
  public void setContentProvider(IStructuredContentProvider contentProvider) {
    this.contentProvider = contentProvider;
  }

  /**
   * Sets the labelProvider.
   * @param labelProvider The labelProvider to set
   */
  public void setLabelProvider(ILabelProvider labelProvider) {
    this.labelProvider = labelProvider;
  }

  /**
   * @see org.eclipse.jface.window.Window#open()
   */
  public int open(ISelection selection) {
    viewer.setSelection(selection);
    return super.open();
  }

  /**
   * Returns the heightInCharacters.
   * @return int
   */
  public int getHeightInCharacters() {
    return heightInCharacters;
  }

  /**
   * Returns the widthInCharacters.
   * @return int
   */
  public int getWidthInCharacters() {
    return widthInCharacters;
  }

  /**
   * Sets the heightInCharacters.
   * @param heightInCharacters The heightInCharacters to set
   */
  public void setHeightInCharacters(int heightInCharacters) {
    this.heightInCharacters = heightInCharacters;
  }

  /**
   * Sets the widthInCharacters.
   * @param widthInCharacters The widthInCharacters to set
   */
  public void setWidthInCharacters(int widthInCharacters) {
    this.widthInCharacters = widthInCharacters;
  }

}