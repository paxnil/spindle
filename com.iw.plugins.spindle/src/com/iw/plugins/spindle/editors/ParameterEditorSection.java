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

import java.util.Iterator;

import net.sf.tapestry.spec.Direction;
import org.eclipse.jface.action.Action;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.IParameterHolder;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginParameterSpecification;

public class ParameterEditorSection extends AbstractPropertySheetEditorSection {

  /**
   * Constructor for ParameterEditorSection
   */
  public ParameterEditorSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(new ParameterLabelProvider());
    setNewAction(new NewParameterAction());
    setDeleteAction(new DeleteParameterAction());
    setHeaderText("Parameters");
    setDescription("This section describes the component's parameters");
  }

  public void initialize(Object input) {
    super.initialize(input);
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("parameters")) {
        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    PluginComponentSpecification spec =
      ((TapestryComponentModel) model).getComponentSpecification();
    Iterator iter = spec.getParameterNames().iterator();
    holderArray.removeAll(holderArray);
    while (iter.hasNext()) {
      String name = (String) iter.next();
      holderArray.add((PluginParameterSpecification) spec.getParameter(name));
    }
    setInput(holderArray);
  }

  protected IParameterHolder getParentSpec() {

    return ((TapestryComponentModel) getFormPage().getModel()).getComponentSpecification();
  }

  public class ParameterLabelProvider extends AbstractIdentifiableLabelProvider {

    private Image image = TapestryImages.getSharedImage("missing");

    public String getText(Object object) {

      PluginParameterSpecification parameterSpec = (PluginParameterSpecification) object;
      PluginComponentSpecification componentSpec =
        (PluginComponentSpecification) parameterSpec.getParent();

      String type = parameterSpec.getType();
      type = type == null ? "" : type;
      StringBuffer buf = new StringBuffer();
      buf.append("name = ");
      buf.append(parameterSpec.getIdentifier());
      buf.append((!"".equals(parameterSpec.getType()) ? " type = " + type : ""));
      buf.append((parameterSpec.isRequired() ? " REQUIRED" : ""));

      if (true) {
        String property = parameterSpec.getPropertyName();

        if (property != null) {

          buf.append(" property = " + property);
        }
        buf.append(" direction = ");
        buf.append(parameterSpec.getDirection() == Direction.CUSTOM ? "custom" : "in");
      }
      return buf.toString();
    }

    public Image getImage(Object object) {
      return image;
    }

  }

  class DeleteParameterAction extends Action {

    protected DeleteParameterAction() {
      super();
      setText("Delete");
    }

    public void run() {
      updateSelection = true;

      PluginParameterSpecification parameterSpec = (PluginParameterSpecification) getSelected();
      IParameterHolder parentSpec = (IParameterHolder) parameterSpec.getParent();

      if (parameterSpec != null) {

        String prev = findPrevious(parameterSpec.getIdentifier());
        parentSpec.removeParameter(parameterSpec.getIdentifier());
        parameterSpec.setParent(null);
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

  class NewParameterAction extends Action {

    protected NewParameterAction() {
      super();
      setText("New");
    }

    public void run() {
      updateSelection = true;
      IParameterHolder spec = getParentSpec();

      String useName = "parameter";

      if (spec.getParameter(useName + 1) != null) {
        int counter = 2;
        while (spec.getParameter(useName + counter) != null) {
          counter++;
        }
        useName = useName + counter;
      } else {
        useName = useName + 1;
      }
      PluginParameterSpecification newSpec = new PluginParameterSpecification();
      newSpec.setRequired(false);
      newSpec.setType("");
      spec.setParameter(useName, newSpec);
      forceDirty();
      update();
      setSelection(useName);
      updateSelection = false;
    }

  }

}