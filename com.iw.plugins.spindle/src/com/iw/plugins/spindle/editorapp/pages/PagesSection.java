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
package com.iw.plugins.spindle.editorapp.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.tapestry.spec.PageSpecification;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.ui.editor.PropertiesAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginPageSpecification;
import com.iw.plugins.spindle.ui.EmptySelection;
import com.iw.plugins.spindle.ui.descriptors.ComponentTypeDialogPropertyDescriptor;
import com.iw.plugins.spindle.ui.descriptors.INeedsModelInitialization;
import com.iw.plugins.spindle.util.JavaListSelectionProvider;
import com.iw.plugins.spindle.util.StringSorter;

public class PagesSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener, ISelectionChangedListener {

  private Action newPageAction = new NewPageAction();
  private Action deletePageAction = new DeletePageAction();

  private TreeViewer viewer;
  /**
   * Constructor for ComponentAliasSection
   */
  public PagesSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Pages");
    setLabelProvider(new PageLabelProvider());
    setNewAction(newPageAction);
    setDeleteAction(deletePageAction);

  }

  protected void fillContextMenu(IMenuManager manager) {

    PluginPageSpecification pageSpec = (PluginPageSpecification) getSelected();
    boolean isEditable = isModelEditable();
    if (isEditable) {

      manager.add(newPageAction);

      if (pageSpec != null) {

        manager.add(new Separator());
        manager.add(deletePageAction);
        manager.add(new Separator());
        manager.add(pAction);
      }
    }

  }

  public void selectionChanged(SelectionChangedEvent event) {
    PluginPageSpecification pageSpec = (PluginPageSpecification) getSelected();
    boolean isEditable = isModelEditable();

    if (pageSpec == null) {

      fireSelectionNotification(null);
      editButton.setEnabled(false);
      deleteButton.setEnabled(false);

    } else {

      fireSelectionNotification(pageSpec.getIdentifier());
      editButton.setEnabled(isEditable);
      deleteButton.setEnabled(isEditable);

      if ((hasFocus || updateSelection) && isEditable) {
        getFormPage().setSelection(event.getSelection());
      }

    }
    newButton.setEnabled(isEditable);

  }

  public void update(BaseTapestryModel model) {
    holderArray.removeAll(holderArray);

    PluginApplicationSpecification appSpec =
      (PluginApplicationSpecification) ((TapestryApplicationModel) getModel()).getApplicationSpec();

    Set ids = new TreeSet(appSpec.getPageNames());

    if (ids.isEmpty()) {

      setInput(holderArray);
      fireSelectionNotification(null);
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }
    Iterator iter = ids.iterator();

    while (iter.hasNext()) {

      String name = (String) iter.next();
      holderArray.add((PluginPageSpecification) appSpec.getPageSpecification(name));
    }
    viewer.setInput(holderArray);

    boolean isEditable = isModelEditable();
    if (newButton != null) {
      newButton.setEnabled(isEditable);
    }
    if (deleteButton != null) {
      deleteButton.setEnabled(isEditable);
    }
    if (editButton != null) {
      editButton.setEnabled(isEditable);
    }
    //selectFirst();
    updateNeeded = false;
  }

  public void modelChanged(IModelChangedEvent event) {
    int eventType = event.getChangeType();
    if (eventType == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
      return;
    }
    if (eventType == IModelChangedEvent.CHANGE) {
      updateNeeded = event.getChangedProperty().equals("pageMap");
    }
  }

  protected class PageLabelProvider extends AbstractIdentifiableLabelProvider {

    Image pageImage;

    public PageLabelProvider() {
      pageImage = TapestryImages.getSharedImage("page16.gif");
    }

    public Image getImage(Object element) {
      return pageImage;
    }


  }

  protected class NewPageAction extends Action {

    protected NewPageAction() {
      super();
      setText("New");
      setDescription("create a new page");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginApplicationSpecification appSpec =
        (PluginApplicationSpecification) ((TapestryApplicationModel) getModel())
          .getApplicationSpec();

      PageRefDialog dialog =
        new PageRefDialog(newButton.getShell(), getModel(), appSpec.getPageNames());

      dialog.create();
      if (dialog.open() == dialog.OK) {
        String name = dialog.getResultName();
        String component = dialog.getResultComponent();
        appSpec.setPageSpecification(name, new PluginPageSpecification(component));
        forceDirty();
        update();
        setSelection(name);
      }
      updateSelection = false;
    }
  }

  protected class DeletePageAction extends Action {

    protected DeletePageAction() {
      super();
      setText("Delete");
      setDescription("delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginPageSpecification pageSpec = (PluginPageSpecification) getSelected();
      if (pageSpec != null) {

        PluginApplicationSpecification appSpec =
          (PluginApplicationSpecification) pageSpec.getParent();

        String prev = findPrevious(pageSpec.getIdentifier());
        appSpec.removePageSpecification(pageSpec.getIdentifier());
        pageSpec.setParent(null);
        forceDirty();
        update();

        if (prev != null) {

          setSelection(prev);

        } else {

          selectFirst();
        }
      }
      updateSelection = false;
    }
  }

}