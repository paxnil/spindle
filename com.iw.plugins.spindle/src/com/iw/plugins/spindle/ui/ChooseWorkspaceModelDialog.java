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

import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public class ChooseWorkspaceModelDialog extends TitleAreaDialog implements ISelectionChangedListener, IDoubleClickListener {

  static public ChooseWorkspaceModelDialog createLibraryModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(shell, project, windowTitle, description, TapestryLookup.ACCEPT_LIBRARIES, false);

    return result;
  }

  static public ChooseWorkspaceModelDialog createApplicationModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(shell, project, windowTitle, description, TapestryLookup.ACCEPT_APPLICATIONS, false);

    return result;

  }

  static public ChooseWorkspaceModelDialog createApplicationAndLibraryModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(
        shell,
        project,
        windowTitle,
        description,
        TapestryLookup.ACCEPT_LIBRARIES | TapestryLookup.ACCEPT_APPLICATIONS,
        false);

    return result;

  }

  static public ChooseWorkspaceModelDialog createProjectModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(
        shell,
        project,
        windowTitle,
        description,
        TapestryLookup.ACCEPT_LIBRARIES | TapestryLookup.ACCEPT_APPLICATIONS | TapestryLookup.THIS_PROJECT_ONLY,
        false);

    return result;

  }

  static public ChooseWorkspaceModelDialog createComponentModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description,
    boolean filterLibraries) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(shell, project, windowTitle, description, TapestryLookup.ACCEPT_COMPONENTS, filterLibraries);

    return result;
  }

  static public ChooseWorkspaceModelDialog createPageModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description,
    boolean filterLibraries) {

    ChooseWorkspaceModelDialog result =
      new ChooseWorkspaceModelDialog(shell, project, windowTitle, description, TapestryLookup.ACCEPT_PAGES, filterLibraries);

    return result;

  }

  private String title;
  private String description;

  private ChooseWorkspaceModelWidget chooserWidget;

  public ChooseWorkspaceModelDialog(
    Shell shell,
    IJavaProject project,
    String windowTitle,
    String description,
    int acceptFlags,
    boolean filterLibraries) {

    super(shell);
    this.title = title == null ? "" : title;
    this.description = description == null ? "" : description;
    chooserWidget = new ChooseWorkspaceModelWidget(project, acceptFlags, filterLibraries);

    chooserWidget.addSelectionChangedListener(this);
    chooserWidget.addDoubleClickListener(this);

  }

  public void create() {
    super.create();
    setTitle(title);
    setMessage(description, IMessageProvider.NONE);
    chooserWidget.setFocus();
    chooserWidget.refresh();
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
    chooserWidget.dispose();
    return super.close();
  }

  protected Control createDialogArea(Composite parent) {

    Composite container = (Composite) super.createDialogArea(parent);
    Composite useContainer = new Composite(container, SWT.NONE);
    FillLayout layout = new FillLayout();
    useContainer.setLayout(layout);
    useContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
    chooserWidget.createControl(useContainer);
    setTitleImage(TapestryImages.getSharedImage("applicationDialog.gif"));
    return container;
  }

  private void updateOkState() {
    Button okButton = getButton(IDialogConstants.OK_ID);
    if (okButton != null) {

      ISelection selection = chooserWidget.getSelection();
      okButton.setEnabled(selection != null && !selection.isEmpty());

    }
  }

  public ITapestryModel getResultModel() {

    return chooserWidget.getResultModel();
  }

  public String getResultString() {

    return chooserWidget.getResultString();

  }

  public IPackageFragment getResultPackage() {

    return chooserWidget.getResultPackage();

  }

  public IStorage getResultStorage() {

    return chooserWidget.getResultStorage();

  }

  public String getResultPath() {

    return chooserWidget.getResultPath();

  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {

    updateOkState();
  }

  /**
   * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
   */
  public void doubleClick(DoubleClickEvent event) {

    ISelection selection = chooserWidget.getSelection();
    if (selection != null && !selection.isEmpty()) {

      buttonPressed(IDialogConstants.OK_ID);

    }

  }

}