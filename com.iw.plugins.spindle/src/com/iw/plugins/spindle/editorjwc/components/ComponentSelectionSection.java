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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ModelUtils;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.manager.TapestryModelManager;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.ui.ChooseComponentDialog;
import com.iw.plugins.spindle.ui.ComponentAliasViewer;
import com.iw.plugins.spindle.ui.CopyToClipboardAction;
import com.iw.plugins.spindle.ui.EmptySelection;
import com.iw.plugins.spindle.ui.IToolTipHelpProvider;
import com.iw.plugins.spindle.ui.IToolTipProvider;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

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

  private ContainedComponentLabelProvider labelProvider = new ContainedComponentLabelProvider();

  private HashMap precomputedAliasInfo = new HashMap();

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

  public HashMap getPrecomputedAliasInfo() {
    return precomputedAliasInfo;
  }

  public void selectionChanged(SelectionChangedEvent event) {

    PluginContainedComponent component = (PluginContainedComponent) getSelected();
    if (component == null) {
      precomputedAliasInfo.clear();
      deleteButton.setEnabled(false);
      copyButton.setEnabled(false);
      fireSelectionNotification(null);

    } else {

      boolean isEditable = ((BaseTapestryModel) getModel()).isEditable();

      String copyOf = component.getCopyOf();
      fireSelectionNotification(component);
      if (hasFocus || updateSelection) {
        getFormPage().setSelection(event.getSelection());
      }
      newButton.setEnabled(isEditable);
      deleteButton.setEnabled(isEditable);
      editButton.setEnabled(isEditable);
      copyButton.setEnabled(isEditable && component.getCopyOf() == null);
      // lets pre-compute the alias lookup for the Tooltips (if required)
      String selectedType = component.getType();
      if (selectedType == null
        || "".equals(selectedType.trim())
        || selectedType.endsWith(".jwc")) {
        precomputedAliasInfo.clear();
        return;
      }
      precomputeAliasInfoForTooltip(selectedType);

    }
  }

  private void precomputeAliasInfoForTooltip(String alias) {
    precomputedAliasInfo.clear();
    Iterator applications = Utils.getApplicationsWithAlias(alias).iterator();
    while (applications.hasNext()) {
      TapestryApplicationModel appModel = (TapestryApplicationModel) applications.next();
      PluginApplicationSpecification appSpec =
        (PluginApplicationSpecification) (appModel).getApplicationSpec();
      TapestryComponentModel cmodel =
        (TapestryComponentModel) ModelUtils.findComponent(
          appSpec.getComponentAlias(alias),
          getModel());
      if (cmodel == null) {
        continue;
      }
      precomputedAliasInfo.put(appModel, cmodel);
    }
  }
  /**
   * @see com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection#createButtons(Composite, FormWidgetFactory)
   */
  protected void createButtons(Composite buttonContainer, FormWidgetFactory factory) {

    super.createButtons(buttonContainer, factory);

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
          manager.add(copyToAction);
          MenuManager submenu = new MenuManager("Copy To Clipboard");
          Display d = getViewer().getControl().getDisplay();
          submenu.add(
            new CopyToClipboardAction(d, "jwcid=\"$value$\"", "value", component.getIdentifier()));
          submenu.add(
            new CopyToClipboardAction(
              d,
              "<span jwcid=\"$value$\"></span>",
              "value",
              component.getIdentifier()));
          manager.add(submenu);
        }
      }
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
      getFormPage().setSelection(EmptySelection.Instance);
      return;
    }
    Iterator iter = ids.iterator();
    while (iter.hasNext()) {
      String id = (String) iter.next();
      holderArray.add((PluginContainedComponent) spec.getComponent(id));
    }
    setInput(holderArray);
    selectFirst();
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
    ILabelProviderListener listener;

    public ContainedComponentLabelProvider() {
      componentImage = TapestryImages.getSharedImage("component16.gif");
      componentAliasImage = TapestryImages.getSharedImage("componentAlias16.gif");
    }
    public Image getImage(Object element) {

      PluginContainedComponent component = (PluginContainedComponent) element;
      String type = component.getType();
      
      if (type != null && !type.endsWith(".jwc")) {
      	
        return componentAliasImage;
      }
      return componentImage;
    }

    //---------- IToolTipProvider ----------------------------//

    public String getToolTipText(Object object) {

      //      return TOOL_TIPS_OFF;

      PluginContainedComponent selectedComponent = (PluginContainedComponent) object;
      String identifier = selectedComponent.getIdentifier();
      String aliasOrType = selectedComponent.getType();
      String copyOf = selectedComponent.getCopyOf();
      if (copyOf != null && !"".equals(copyOf.trim())) {
        return identifier + " is copy-of " + copyOf;
      }
      StringBuffer buffer =
        new StringBuffer(identifier + " type = " + (aliasOrType == null ? "" : aliasOrType) + "\n");
      if (!isSelected(selectedComponent)) {
        buffer.append("Select this contained component for more tooltip info");
        return buffer.toString();
      }
      // empty means no alias!
      if (precomputedAliasInfo.isEmpty()) {
        // the type is not an alias
        String type = aliasOrType;
        if (type == null || type.equals("")) {
          buffer.append("No Type found for contained component: " + identifier);
          return buffer.toString();
        }
        TapestryModelManager mgr = TapestryPlugin.getTapestryModelManager();
        TapestryComponentModel component = ModelUtils.findComponent(type, getModel());

        if (component == null) {
          buffer.append(identifier + "'s type: " + type + " not found.");
          return buffer.toString();
        } else if (!component.isLoaded()) {
          try {
            component.load();
          } catch (Exception e) {
            buffer.append("Could not load component: " + type + ". There could be a parse error.");
            return buffer.toString();
          }
        }
        PluginComponentSpecification componentSpec = component.getComponentSpecification();
        componentSpec.getHelpText(identifier, buffer);
        return buffer.toString();

      } else {
        // the type IS an alias        
        String alias = aliasOrType;
        Object[] keys = precomputedAliasInfo.keySet().toArray();
        if (keys.length == 0) {
          buffer.append("Alias '" + alias + "' not found in an application.");
          return buffer.toString();
        } else {
          TapestryApplicationModel firstModel = ((TapestryApplicationModel) keys[0]);
          TapestryComponentModel cmodel =
            (TapestryComponentModel) precomputedAliasInfo.get(keys[0]);
          if (!cmodel.isLoaded()) {
            try {
              cmodel.load();
            } catch (CoreException e) {
            }
          }
          PluginComponentSpecification firstComponent =
            (PluginComponentSpecification) cmodel.getComponentSpecification();
          buffer.append(
            "Found alias '"
              + alias
              + "' in application '"
              + firstModel.getUnderlyingStorage().getFullPath()
              + "\n");
          buffer.append(
            alias + " maps to " + firstModel.getApplicationSpec().getComponentAlias(alias) + "\n");
          if (keys.length > 1) {
            buffer.append(
              "press F1 to check "
                + (keys.length - 1)
                + " other application(s) that have alias '"
                + alias
                + "'.\n");
          }
          if (firstComponent != null) {
            firstComponent.getHelpText(alias, buffer);
          }
          return buffer.toString();
        }
      }
    }

    public Image getToolTipImage(Object object) {
      return getImage(object);
    }

    //---------- IToolTipHelpProvider --------------------//

    public Object getHelp(Object obj) {
      //      return HELP_OFF;
      if (!isSelected(obj)) {
        return null;
      }
      if (precomputedAliasInfo.isEmpty()) {
        return null;
      }
      PluginContainedComponent component = (PluginContainedComponent) obj;
      String id = component.getIdentifier();
      String alias = component.getType();
      return new ComponentAliasViewer(id, alias, precomputedAliasInfo);

    }

  }

  class NewComponentAction extends Action {
    /**
     * Constructor for NewPropertyAction
     */
    protected NewComponentAction() {
      super();
      setText("New");
      setToolTipText("Create a new Contained Component");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      TapestryComponentModel model = (TapestryComponentModel) getFormPage().getModel();
      PluginComponentSpecification spec = model.getComponentSpecification();
      String newC = "component";
      if (spec.getComponent(newC) != null) {
        int counter = 0;
        while (spec.getComponent(newC + counter) != null) {
          counter++;
        }
        newC = newC + counter;
      }
      PluginContainedComponent newComponent = new PluginContainedComponent();
      newComponent.setType("fill in type");
      spec.setComponent(newC, newComponent);
      forceDirty();
      update();
      setSelection(newC);
      updateSelection = false;
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
      IJavaProject jproject =
        TapestryPlugin.getDefault().getJavaProjectFor(getModel().getUnderlyingStorage());

      ChooseComponentDialog dialog =
        new ChooseComponentDialog(
          newButton.getShell(),
          jproject,
          "Copy To",
          "Choose a target for the copy",
          true,
          TapestryLookup.ACCEPT_COMPONENTS | TapestryLookup.WRITEABLE);

      dialog.create();

      if (dialog.open() == dialog.OK) {

        String chosen = dialog.getResultComponent();

        TapestryComponentModel readOnlyModel = ModelUtils.findComponent(chosen, getModel());

        if (readOnlyModel == null) {
          MessageDialog.openError(
            newButton.getShell(),
            "Copy To action aborted",
            "could not find '" + chosen + "'");
          return;
        }

        IStorage underlier = readOnlyModel.getUnderlyingStorage();
        if (underlier.isReadOnly()) {
          MessageDialog.openError(
            newButton.getShell(),
            "Copy To action aborted",
            "'" + underlier.getFullPath() + "'\nis readonly");
          return;
        }

        PluginContainedComponent component = (PluginContainedComponent) getSelected();

        IEditorPart editor = Utils.getEditorFor(underlier);

        if (editor != null) {

          performCopyInEditor(component, editor, chosen);

        } else {

          performCopyInWorkspace(component, underlier, chosen);

        }
      }
    }

    private void performCopyInEditor(
      PluginContainedComponent component,
      IEditorPart editor,
      String name) {
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

    private boolean alreadyHasComponent(
      PluginContainedComponent component,
      TapestryComponentModel model) {
      boolean result = false;
      PluginComponentSpecification spec = model.getComponentSpecification();
      if (spec.getComponent(component.getIdentifier()) != null) {
        result = true;
        MessageDialog.openError(
          newButton.getShell(),
          "Copy To action aborted",
          "The selected target component already has '"
            + component.getIdentifier()
            + "'. Can not continue");
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
        MessageDialog.openError(
          newButton.getShell(),
          "Copy To action failed",
          e.getClass().getName());
      }
      return result;
    }

    private void performCopyInWorkspace(
      PluginContainedComponent component,
      IStorage storage,
      String name) {
      String consumer = "PerformCopyToInWorkspace";
      TapestryModelManager mgr = TapestryPlugin.getTapestryModelManager();

      mgr.connect(storage, consumer, true);

      TapestryComponentModel model =
        (TapestryComponentModel) mgr.getEditableModel(storage, consumer);

      if (!model.isEditable()) {
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

      mgr.disconnect(storage, consumer);
    }

    private void parseError() {
      Status status =
        new Status(
          IStatus.ERROR,
          TapestryPlugin.getDefault().getPluginId(),
          IStatus.OK,
          "Abort, target component is has parse errors",
          null);
      ErrorDialog.openError(
        TapestryPlugin.getDefault().getActiveWorkbenchShell(),
        null,
        null,
        status);
    }

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

}