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
package com.iw.plugins.spindle.editors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.IPropertySource;

import com.iw.plugins.spindle.ui.propertysheet.PropertySheetPage;

public class SpindlePropertySheet extends PropertySheetPage {
  public static final String CLONE_LABEL = "Clone";
  public static final String CLONE_TOOLTIP = "Clone this property";
  private Action cloneAction;
  protected ISelection currentSelection;
  private IWorkbenchPart part;

  public SpindlePropertySheet() {
    makeActions();
  }
  public void createControl(Composite parent) {
    super.createControl(parent);
    final TableTree tableTree = (TableTree) getControl();
    tableTree.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        /*
        	TableTreeItem [] items = tableTree.getSelection();
        	IPropertySheetEntry entry = null;
        	if (items.length >0) entry = (IPropertySheetEntry)items[0].getData();
        	updateActions(entry);
        */
      }
    });
  }
  public void disableActions() {
    cloneAction.setEnabled(false);
  }
  public void fillLocalToolBar(IToolBarManager toolBarManager) {
    toolBarManager.add(new Separator());
    toolBarManager.add(cloneAction);
  }
  public IPropertySheetEntry getSelectedEntry() {
    TableTree tableTree = (TableTree) getControl();
    TableTreeItem[] items = tableTree.getSelection();
    IPropertySheetEntry entry = null;
    if (items.length > 0)
      entry = (IPropertySheetEntry) items[0].getData();
    return entry;
  }

  protected void handleClone() {
    Object input = null;
    if (currentSelection instanceof IStructuredSelection) {
      input = ((IStructuredSelection) currentSelection).getFirstElement();
    }
    IPropertySource source = null;
    if (input instanceof IAdaptable) {
      source = (IPropertySource) ((IAdaptable) input).getAdapter(IPropertySource.class);
    }
//    if (source instanceof ICloneablePropertySource) {
//      Object newInput = ((ICloneablePropertySource) source).doClone();
//      if (newInput != null) {
//        selectionChanged(part, new StructuredSelection(newInput));
//      }
//    }
  }
  public void makeContributions(
    IMenuManager menuManager,
    IToolBarManager toolBarManager,
    IStatusLineManager statusLineManager) {
    super.makeContributions(menuManager, toolBarManager, statusLineManager);
    fillLocalToolBar(toolBarManager);
  }

  /** borrowing from PDE for now **/
  protected void makeActions() {
//    cloneAction = new Action(PDEPlugin.getResourceString(CLONE_LABEL)) {
//      public void run() {
//        handleClone();
//      }
//    };
//    cloneAction.setImageDescriptor(PDEPluginImages.DESC_CLONE_ATT);
//    cloneAction.setHoverImageDescriptor(PDEPluginImages.DESC_CLONE_ATT_HOVER);
//    cloneAction.setDisabledImageDescriptor(PDEPluginImages.DESC_CLONE_ATT_DISABLED);
//    cloneAction.setToolTipText(PDEPlugin.getResourceString(CLONE_TOOLTIP));
//    cloneAction.setEnabled(false);
  }
  public void selectionChanged(IWorkbenchPart part, ISelection sel) {
    super.selectionChanged(part, sel);
    this.part = part;
    currentSelection = sel;
    updateActions();
  }
  protected void updateActions() {
    Object input = null;
    if (currentSelection instanceof IStructuredSelection) {
      input = ((IStructuredSelection) currentSelection).getFirstElement();
    }
    IPropertySource source = null;
    if (input instanceof IAdaptable) {
      source = (IPropertySource) ((IAdaptable) input).getAdapter(IPropertySource.class);
    }

    updateActions(source);
  }
  protected void updateActions(IPropertySource source) {
//    if (source instanceof ICloneablePropertySource) {
//      cloneAction.setEnabled(((ICloneablePropertySource) source).isCloneable());
//    } else
//      cloneAction.setEnabled(false);
  }
}