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
package com.iw.plugins.spindle.editorjwc.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.html.TapestryHTMLEditor;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.ui.ChooseFromNamespaceDialog;
import com.iw.plugins.spindle.ui.ComponentAliasViewer;
import com.iw.plugins.spindle.ui.CopyToClipboardAction;
import com.iw.plugins.spindle.ui.IToolTipHelpProvider;
import com.iw.plugins.spindle.ui.IToolTipProvider;
import com.iw.plugins.spindle.ui.OpenAliasDefinition;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;
import com.iw.plugins.spindle.util.lookup.TapestryNamespaceLookup;

public class ComponentSelectionSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener, ISelectionChangedListener {

  private static String TOOL_TIPS_OFF = "Tooltips are temporarily disabled";
  private static String HELP_OFF = "Help is temporarily disabled";

  private Button copyButton;

  private Action newComponentAction = new NewComponentAction();
  private Action deleteComponentAction = new DeleteComponentAction();
  private Action copyAction = new CopyComponentAction();
  private Action copyToAction = new CopyToAction();
  private OpenAliasDefinition openAction = new OpenAliasDefinition();

  private ContainedComponentLabelProvider labelProvider = new ContainedComponentLabelProvider();

  static public TapestryComponentModel resolveContainedComponent(ITapestryProject project, PluginContainedComponent component)
    throws CoreException {

    TapestryLibraryModel projectModel = (TapestryLibraryModel) project.getProjectModel();

    String componentPath = projectModel.findComponentPath(component.getType());

    TapestryComponentModel result = null;

    SpindleStatus status = new SpindleStatus();

    if (componentPath != null) {

      result = (TapestryComponentModel) project.findModelByPath(componentPath, TapestryLookup.ACCEPT_COMPONENTS);

      if (result == null) {

        status.setError("(bad path '" + componentPath + "')");

        throw new CoreException(status);

      } else if (!result.isLoaded()) {

        try {

          result.load();

        } catch (Exception e) {

          status.setError("could not load the component " + "(" + componentPath + "). There could be a parse error.");

          throw new CoreException(status);

        }

      }

    }

    return result;

  }

  /**
   * Constructor for ComponentSelectionSection
   */
  public ComponentSelectionSection(SpindleFormPage page) {
    super(page);
    setHeaderText("Contained Components");
    setNewAction(newComponentAction);
    setDeleteAction(deleteComponentAction);
    setUseToolTips(true);
    setLabelProvider(labelProvider);
    setToolTipProvider(labelProvider);
    setToolTipHelpProvider(labelProvider);
  }

  public void selectionChanged(SelectionChangedEvent event) {

    PluginContainedComponent component = (PluginContainedComponent) getSelected();
    if (component == null) {
      deleteButton.setEnabled(false);
      copyButton.setEnabled(false);
      fireSelectionNotification(null);

    } else {

      boolean isEditable = ((BaseTapestryModel) getModel()).isEditable();

      String copyOf = component.getCopyOf();
      fireSelectionNotification(component);
      if (hasFocus || updateSelection) {
        setPageSelection();
      }
      newButton.setEnabled(isEditable);
      deleteButton.setEnabled(isEditable);
      inspectButton.setEnabled(isEditable);
      copyButton.setEnabled(isEditable && component.getCopyOf() == null);

    }
  }

  /**
   * @see com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection#createButtons(Composite, FormWidgetFactory)
   */
  protected void createButtons(Composite buttonContainer, FormWidgetFactory factory) {

    super.createButtons(buttonContainer, factory);

    newButton.setText(newComponentAction.getText());

    copyButton = factory.createButton(buttonContainer, "Copy", SWT.PUSH);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.verticalAlignment = GridData.BEGINNING;
    copyButton.setLayoutData(gd);
    copyButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleCopy();
        copyButton.getShell().setDefaultButton(null);
      }
    });

  }

  protected void fillContextMenu(IMenuManager manager) {

    PluginContainedComponent component = (PluginContainedComponent) getSelected();
    boolean isEditable = ((BaseTapestryModel) getModel()).isEditable();
    if (isEditable) {
      manager.add(newComponentAction);
      if (component != null) {
        manager.add(new Separator());
        manager.add(deleteComponentAction);
        if (component.getCopyOf() == null) {
          manager.add(new Separator());
          manager.add(copyAction);
          //          manager.add(copyToAction);
          MenuManager submenu = new MenuManager("Copy To Clipboard");
          Display d = getViewer().getControl().getDisplay();
          submenu.add(new CopyToClipboardAction(d, "jwcid=\"$value$\"", "value", component.getIdentifier()));
          submenu.add(new CopyToClipboardAction(d, "<span jwcid=\"$value$\"></span>", "value", component.getIdentifier()));
          manager.add(submenu);
        }
      }
    }
    try {

      String alias = component.getType();

      ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(getModel());
      openAction.configure(project, alias);

    } catch (CoreException e) {

    }
    List templates = new ArrayList();
    IStorage storage = (IStorage) getModel().getUnderlyingStorage();

    if (storage instanceof IFile) {

      templates = Utils.findTemplatesFor((IFile) storage);

    }
    if (!templates.isEmpty() || openAction.isEnabled()) {

      MenuManager jumpMenu = new MenuManager("Jump to..");

      if (openAction.isEnabled()) {

        jumpMenu.add(openAction);

      }

      Iterator iter = templates.iterator();

      if (iter.hasNext() && openAction.isEnabled()) {

        jumpMenu.add(new Separator());

      }

      while (iter.hasNext()) {
        IFile element = (IFile) iter.next();
        jumpMenu.add(new JumpToTemplateAction((IStorage) element, component.getIdentifier()));
      }
      manager.add(jumpMenu);
    }

    manager.add(new Separator());
    pAction.setEnabled(((IModel) getFormPage().getModel()).isEditable());
    manager.add(pAction);

  }

  private void handleCopy() {
    copyAction.run();
  }

  public void update(BaseTapestryModel model) {
    TapestryComponentModel cmodel = (TapestryComponentModel) model;
    PluginComponentSpecification spec = cmodel.getComponentSpecification();
    holderArray.removeAll(holderArray);
    Set ids = new TreeSet(spec.getComponentIds());

    if (ids.isEmpty()) {

      setInput(holderArray);
      fireSelectionNotification(null);
      clearPageSelection();
      return;
    }
    Iterator iter = ids.iterator();
    while (iter.hasNext()) {
      String id = (String) iter.next();
      holderArray.add((PluginContainedComponent) spec.getComponent(id));
    }
    setInput(holderArray);
  }

  public void modelChanged(IModelChangedEvent event) {
    int eventType = event.getChangeType();
    if (eventType == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
      return;
    }
    if (eventType == IModelChangedEvent.CHANGE) {
      updateNeeded = event.getChangedProperty().equals("components");
    }
  }

  protected class ContainedComponentLabelProvider
    extends AbstractIdentifiableLabelProvider
    implements IToolTipProvider, IToolTipHelpProvider {

    Image componentImage;
    Image componentAliasImage;
    Image copyOfImage;
    ILabelProviderListener listener;

    public ContainedComponentLabelProvider() {
      componentImage = TapestryImages.getSharedImage("component16.gif");
      componentAliasImage = TapestryImages.getSharedImage("componentAlias16.gif");
      copyOfImage = TapestryImages.getSharedImage("componentCopyOf16.gif");
    }
    public Image getImage(Object element) {

      PluginContainedComponent component = (PluginContainedComponent) element;
      String copyOf = component.getCopyOf();
      String type = component.getType();

      if (copyOf != null) {

        return copyOfImage;

      }

      if (type != null && !type.endsWith(".jwc")) {

        return componentAliasImage;
      }
      return componentImage;
    }

    public String getToolTipText(Object object) {

      PluginContainedComponent selectedComponent = (PluginContainedComponent) object;

      String identifier = selectedComponent.getIdentifier();
      String type = selectedComponent.getType();

      String copyOf = selectedComponent.getCopyOf();

      if (copyOf != null && !"".equals(copyOf.trim())) {

        return identifier + " is copy-of " + copyOf;

      }

      String message = "could not resolve'" + type + "' ";

      if (type.endsWith(".jwc")) {

        message = "can't resolve specification paths. Use aliases instead.";

      } else {

        try {

          ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(getModel());

          TapestryComponentModel compModel = resolveContainedComponent(project, selectedComponent);

          if (compModel == null) {

            message = "could not resolve " + selectedComponent.getIdentifier();

          } else {

            StringBuffer resultBuffer = new StringBuffer();
            compModel.getComponentSpecification().getHelpText(type, resultBuffer);
            message = resultBuffer.toString();

          }

        } catch (CoreException e) {

          message += e.getStatus().getMessage();
        }

      }

      return message;

    }

    public Image getToolTipImage(Object object) {
      return getImage(object);
    }

    //---------- IToolTipHelpProvider --------------------//

    public Object getHelp(Object object) {

      PluginContainedComponent selectedComponent = (PluginContainedComponent) object;
      TapestryComponentModel componentModel = null;

      try {

        ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(getModel());

        componentModel = resolveContainedComponent(project, selectedComponent);

      } catch (CoreException e) {

        return null;

      }

      return new ComponentAliasViewer(selectedComponent.getIdentifier(), selectedComponent.getType(), componentModel);

    }

  }

  class NewComponentAction extends Action {

    protected NewComponentAction() {
      super();
      setText("Add");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      try {
        updateSelection = true;

        TapestryComponentModel model = (TapestryComponentModel) getFormPage().getModel();

        PluginComponentSpecification specification = model.getComponentSpecification();

        ChooseFromNamespaceDialog dialog =
          new ChooseFromNamespaceDialog(
            newButton.getShell(),
            TapestryPlugin.getDefault().getTapestryProjectFor(getModel().getUnderlyingStorage()),
            "Choose Component",
            "Choose a Component to be added",
            TapestryNamespaceLookup.ACCEPT_COMPONENTS);

        dialog.create();

        if (dialog.open() == dialog.OK) {

          String componentName = dialog.getResultString();
          String path = dialog.getResultPath();

          if (specification.getComponent(componentName) != null) {

            int count = 1;
            while (specification.getComponent(componentName + count) != null) {
              count++;
            }

            componentName += count;
          }

          PluginContainedComponent newComponent = new PluginContainedComponent();
          newComponent.setType(path);
          specification.setComponent(componentName, newComponent);
          forceDirty();
          update();
          setSelection(componentName);
        }

      } catch (CoreException e) {

        e.printStackTrace();

      } finally {

        updateSelection = false;

      }

    }
  }

  class CopyToAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected CopyToAction() {
      super();
      setText("Copy To");
      setToolTipText("copy this contained component \n and all its bindings to another component");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      //      ITapestryProject tproject;
      //      try {
      //        tproject =
      //          TapestryPlugin.getDefault().getTapestryProjectFor(getModel().getUnderlyingStorage());
      //      } catch (CoreException e) {
      //
      //        MessageDialog.openError(
      //          newButton.getShell(),
      //          "Spindle project error",
      //          "Not in Tapestry project");
      //        return;
      //      }
      //
      //      ChooseComponentFromNamespaceDialog dialog =
      //        new ChooseComponentFromNamespaceDialog(
      //          newButton.getShell(),
      //          tproject,
      //          "Copy To",
      //          "Choose a target for the copy",
      //          true,
      //          TapestryLookup.ACCEPT_COMPONENTS | TapestryLookup.WRITEABLE);
      //
      //      dialog.create();
      //
      //      if (dialog.open() == dialog.OK) {
      //
      //        String chosen = dialog.getResultComponent();
      //
      //        TapestryComponentModel readOnlyModel = ModelUtils.findComponent(chosen, getModel());
      //
      //        if (readOnlyModel == null) {
      //          MessageDialog.openError(
      //            newButton.getShell(),
      //            "Copy To action aborted",
      //            "could not find '" + chosen + "'");
      //          return;
      //        }
      //
      //        IStorage underlier = readOnlyModel.getUnderlyingStorage();
      //        if (underlier.isReadOnly()) {
      //          MessageDialog.openError(
      //            newButton.getShell(),
      //            "Copy To action aborted",
      //            "'" + underlier.getFullPath() + "'\nis readonly");
      //          return;
      //        }
      //
      //        PluginContainedComponent component = (PluginContainedComponent) getSelected();
      //
      //        IEditorPart editor = Utils.getEditorFor(underlier);
      //
      //        if (editor != null) {
      //
      //          performCopyInEditor(component, editor, chosen);
      //
      //        } else {
      //
      //          performCopyInWorkspace(component, underlier, chosen);
      //
      //        }
    }
  }

  private void performCopyInEditor(PluginContainedComponent component, IEditorPart editor, String name) {
    SpindleMultipageEditor targetEditor = null;

    try {

      targetEditor = (SpindleMultipageEditor) editor;

    } catch (ClassCastException e) {

      MessageDialog.openError(
        newButton.getShell(),
        "Copy To action aborted",
        "'" + name + "' \nis open in a non-Spindle editor. Can not continue");
      return;
    }

    TapestryComponentModel model = null;

    if (targetEditor != null && targetEditor.isDirty()) {
      RequiredSaveEditorAction saver = new RequiredSaveEditorAction(targetEditor);
      if (!saver.save()) {
        return;
      }

    }

    model = (TapestryComponentModel) targetEditor.getModel();

    if (!alreadyHasComponent(component, model) && doCopy(component, model)) {
      targetEditor.showPage(SpindleMultipageEditor.SOURCE_PAGE);
    }

  }

  private boolean alreadyHasComponent(PluginContainedComponent component, TapestryComponentModel model) {
    boolean result = false;
    PluginComponentSpecification spec = model.getComponentSpecification();
    if (spec.getComponent(component.getIdentifier()) != null) {
      result = true;
      MessageDialog.openError(
        newButton.getShell(),
        "Copy To action aborted",
        "The selected target component already has '" + component.getIdentifier() + "'. Can not continue");
    }
    return result;
  }

  private boolean doCopy(PluginContainedComponent component, TapestryComponentModel model) {
    boolean result = false;
    try {
      if (!model.isLoaded()) {
        parseError();
      } else {
        Utils.copyContainedComponentTo(component.getIdentifier(), component.deepCopy(), model);
        result = true;
      }
    } catch (Exception e) {
      MessageDialog.openError(newButton.getShell(), "Copy To action failed", e.getClass().getName());
    }
    return result;
  }

  private void performCopyInWorkspace(PluginContainedComponent component, IStorage storage, String name) {
    String consumer = "PerformCopyToInWorkspace";

    TapestryProjectModelManager mgr = null;
    TapestryComponentModel model = null;

    try {

      mgr = TapestryPlugin.getTapestryModelManager(storage);
      mgr.connect(storage, consumer, true);
      model = (TapestryComponentModel) mgr.getEditableModel(storage, consumer);

    } catch (CoreException e) {
    }

    if (model == null || !model.isEditable()) {
      MessageDialog.openError(
        newButton.getShell(),
        "Copy To action aborted",
        "Unable to obtain an editable copy of '" + name + "'. \nCan not continue");
      mgr.disconnect(storage, consumer);
      return;
    }

    if (!alreadyHasComponent(component, model) && doCopy(component, model)) {

      Utils.saveModel(model, new NullProgressMonitor());
      TapestryPlugin.openTapestryEditor(storage);

    }

    if (mgr != null) {

      mgr.disconnect(storage, consumer);

    }
  }

  private void parseError() {
    Status status =
      new Status(
        IStatus.ERROR,
        TapestryPlugin.getDefault().getPluginId(),
        IStatus.OK,
        "Abort, target component is has parse errors",
        null);
    ErrorDialog.openError(TapestryPlugin.getDefault().getActiveWorkbenchShell(), null, null, status);
  }

  class CopyComponentAction extends Action {

    protected CopyComponentAction() {
      super();
      setText("Copy Of");
    }

    public void run() {
      PluginContainedComponent component = (PluginContainedComponent) getSelected();
      if (component != null && component.getCopyOf() == null) {
        updateSelection = true;
        PluginComponentSpecification spec = (PluginComponentSpecification) component.getParent();
        String copyOfName = "copy-of-" + component.getIdentifier();
        if (spec.getComponent(copyOfName) != null) {
          int counter = 0;
          while (spec.getComponent(copyOfName + counter) != null) {
            counter++;
          }
          copyOfName = copyOfName + counter;
        }
        PluginContainedComponent copy = new PluginContainedComponent();
        copy.setCopyOf(component.getIdentifier());
        spec.addComponent(copyOfName, copy);
        forceDirty();
        update();
        setSelection(copyOfName);
        updateSelection = false;
      }
    }
  }

  class DeleteComponentAction extends Action {

    protected DeleteComponentAction() {
      super();
      setText("Delete");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginContainedComponent component = (PluginContainedComponent) getSelected();
      if (component != null) {
        PluginComponentSpecification spec = (PluginComponentSpecification) component.getParent();
        String prev = findPrevious(component.getIdentifier());
        spec.removeComponent(component.getIdentifier());
        component.setParent(null);
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

  class JumpToTemplateAction extends Action {

    IStorage target;
    String jwcid;

    public JumpToTemplateAction(IStorage target, String jwcid) {
      super();
      this.target = target;
      this.jwcid = jwcid;
      setText(target.getName());
    }

    public void run() {

      IEditorPart editor = Utils.getEditorFor(target);

      if (editor != null) {

        TapestryPlugin.getDefault().getActivePage().bringToTop(editor);

      } else {

        TapestryPlugin.getDefault().openTapestryEditor(target);

        editor = Utils.getEditorFor(target);

        //might have failed - try the Workspace default editor

        if (editor == null) {

          TapestryPlugin.getDefault().openNonTapistryEditor(target);

        }

      }

      if (editor != null && editor instanceof TapestryHTMLEditor) {

        ((TapestryHTMLEditor) editor).openTo(jwcid);

      }

    }

  }
}