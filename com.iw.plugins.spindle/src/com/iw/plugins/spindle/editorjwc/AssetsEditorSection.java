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
package com.iw.plugins.spindle.editorjwc;

import java.util.Iterator;

import net.sf.tapestry.spec.AssetType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.ui.editor.PropertiesAction;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginAssetSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;

public class AssetsEditorSection extends AbstractPropertySheetEditorSection {


  private DeleteAssetAction deleteAction = new DeleteAssetAction();
  private NewContextAssetAction newContextAction = new NewContextAssetAction();
  private NewExternalAssetAction newExternalAction = new NewExternalAssetAction();
  private NewPrivateAssetAction newPrivateAction = new NewPrivateAssetAction();

  private NewButtonAction newButtonAction = new NewButtonAction();

  /**
   * Constructor for ParameterEditorSection
   */
  public AssetsEditorSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(new AssetLabelProvider());
    setNewAction(newButtonAction);
    setDeleteAction(deleteAction);
    setHeaderText("Assets");
    setDescription("This section allows one to edit the component's assets");
  }

  public void initialize(Object object) {
    super.initialize(object);
    BaseTapestryModel model = (BaseTapestryModel) object;
    if (!model.isEditable()) {
      newContextAction.setEnabled(false);
      newExternalAction.setEnabled(false);
      newPrivateAction.setEnabled(false);
    }
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("assets")) {
        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    PluginComponentSpecification spec = ((TapestryComponentModel) model).getComponentSpecification();
    Iterator iter = spec.getAssetNames().iterator();
    holderArray.removeAll(holderArray);
    while (iter.hasNext()) {
      String name = (String) iter.next();
      holderArray.add(spec.getAsset(name));
    }
    setInput(holderArray);
  }

  protected void fillContextMenu(IMenuManager manager) {
    ISelection selection = getSelection();
    final Object object = ((IStructuredSelection) selection).getFirstElement();
    MenuManager submenu = new MenuManager("New");
    submenu.add(newContextAction);
    submenu.add(newExternalAction);
    submenu.add(newPrivateAction);
    manager.add(submenu);
    if (object != null) {
      manager.add(new Separator());
      manager.add(deleteAction);

    }
    manager.add(new Separator());
    PropertiesAction pAction = new PropertiesAction(getFormPage().getEditor());
    pAction.setText("Edit");
    pAction.setToolTipText("Edit the selected");
    pAction.setEnabled(((IEditable) getFormPage().getModel()).isEditable());
    manager.add(pAction);
  }

  public class AssetLabelProvider extends LabelProvider implements ITableLabelProvider {

    private Image privateAssetImage = TapestryImages.getSharedImage("missing");
    private Image contextAssetImage = TapestryImages.getSharedImage("missing");
    private Image externalAssetImage = TapestryImages.getSharedImage("missing");

    public String getText(Object object) {
    	
      PluginAssetSpecification spec = (PluginAssetSpecification) object;
      String identifier = spec.getIdentifier();
      AssetType type = spec.getType();
      
      if (type == AssetType.PRIVATE) {
      	
        return identifier + " resource-path = " + spec.getPath();
      }
      if (type == AssetType.EXTERNAL) {
      	
        return identifier + " URL = " + spec.getPath();
      }
      if (type == AssetType.CONTEXT) {
      	
        return identifier + " path = " + spec.getPath();
      }
      return null;
    }

    public void dispose() {
      //shared images are disposed by the Plugin
    }

    public String getColumnText(Object object, int column) {
      if (column != 1) {
        return "";
      }
      return getText(object);
    }

    public Image getImage(Object object) {

      PluginAssetSpecification spec = (PluginAssetSpecification) object;
      AssetType type = spec.getType();
      if (type == AssetType.PRIVATE) {
        return privateAssetImage;
      }
      if (type == AssetType.EXTERNAL) {
        return externalAssetImage;
      }
      if (type == AssetType.CONTEXT) {
        return contextAssetImage;
      }
      return null;
    }

    public Image getColumnImage(Object object, int column) {
      if (column != 1) {
        return null;
      }
      return getImage(object);
    }
  }

  

  class DeleteAssetAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected DeleteAssetAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginAssetSpecification assetSpec = (PluginAssetSpecification) getSelected();
      PluginComponentSpecification componentSpec = (PluginComponentSpecification) assetSpec.getParent();
      String identifier = assetSpec.getIdentifier();
      if (assetSpec != null) {
        String prev = findPrevious(identifier);
        componentSpec.removeAsset(identifier);
        assetSpec.setParent(null);
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

  protected class NewButtonAction extends Action {

    protected NewButtonAction() {
      super();
    }

    public void run() {
      ChooseAssetTypeDialog dialog = new ChooseAssetTypeDialog(newButton.getShell());
      dialog.create();
      if (dialog.open() == dialog.OK) {
        AssetType chosen = dialog.getSelectedAssetType();
        if (chosen == AssetType.CONTEXT) {
          newContextAction.run();
        } else if (chosen == AssetType.EXTERNAL) {
          newExternalAction.run();
        } else if (chosen == AssetType.PRIVATE) {
          newPrivateAction.run();
        }
      }
    }
  }

  protected abstract class BaseNewAssetAction extends Action {

    protected BaseNewAssetAction() {
      super();
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginComponentSpecification spec =
        ((TapestryComponentModel) getFormPage().getModel()).getComponentSpecification();
      String useName = "asset";
      if (spec.getAsset(useName) != null) {
        int counter = 1;
        while (spec.getAsset(useName + counter) != null) {
          counter++;
        }
        useName = useName + counter;
      }
      PluginAssetSpecification newSpec = new PluginAssetSpecification(getType(), "fill in path");
      spec.setAsset(useName, newSpec);
      forceDirty();
      update();
      setSelection(useName);
      updateSelection = false;
    }

    protected abstract AssetType getType();

  }

  class NewPrivateAssetAction extends BaseNewAssetAction {
    /**
     * Constructor for NewPropertyAction
     */
    protected NewPrivateAssetAction() {
      super();
      setText("Private Asset");
      setToolTipText("Create a new private asset");
    }

    public AssetType getType() {
      return AssetType.PRIVATE;
    }

  }

  class NewExternalAssetAction extends BaseNewAssetAction {

    protected NewExternalAssetAction() {
      super();
      setText("External Asset");
      setToolTipText("Create a new external asset");
    }

    public AssetType getType() {
      return AssetType.EXTERNAL;
    }

  }

  class NewContextAssetAction extends BaseNewAssetAction {

    protected NewContextAssetAction() {
      super();
      setText("Context Asset");
      setToolTipText("Create a new context asset");
    }

    public AssetType getType() {
      return AssetType.CONTEXT;
    }

  }

}
