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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;

public class ReservedParametersSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener {

  /**
   * Constructor for PropertySection 
   */
  public ReservedParametersSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(new ParameterLabelProvider());
    setNewAction(new NewReservedParameterAction());
    setDeleteAction(new DeleteReservedParameterAction());
    setHeaderText("Reserved Parameters");
    setDescription("In this section you can manage reserved parameters. Note that values are case insensitive.");
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {

      updateNeeded = true;
      update();

    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {

      if (event.getChangedProperty().equals("parameters")) {

        updateNeeded = true;

        update();
      }
    }
  }

  public void update(BaseTapestryModel baseModel) {

    TapestryComponentModel cmodel = (TapestryComponentModel) baseModel;
    PluginComponentSpecification spec = cmodel.getComponentSpecification();
    Collection reservedParameters = spec.getReservedParameters();

    holderArray.removeAll(holderArray);

    if (reservedParameters != null && !reservedParameters.isEmpty()) {

      reservedParameters = (Collection) (((HashSet) reservedParameters).clone());
      Collection currentParameters = spec.getParameterNames();

      if (currentParameters != null && !currentParameters.isEmpty()) {
        reservedParameters.removeAll(currentParameters);
      }

      for (Iterator iter = reservedParameters.iterator(); iter.hasNext();) {
        holderArray.add(new ParameterHolder((String) iter.next()));
      }

    }

    setInput(holderArray);
  }

  public class ParameterLabelProvider extends AbstractIdentifiableLabelProvider {

    private Image image = TapestryImages.getSharedImage("property16.gif");


    public Image getImage(Object object) {
      return image;
    }

  }

  class DeleteReservedParameterAction extends Action {


    /**
     * Constructor for NewPropertyAction
     */
    protected DeleteReservedParameterAction() {
      super();
      setText("Delete");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;

      ParameterHolder holder = (ParameterHolder) getSelected();
      PluginComponentSpecification spec = (PluginComponentSpecification) holder.getParent();
      String prev = findPrevious(holder.getIdentifier());
      spec.setReservedParameter(holder.getIdentifier(), false);
      forceDirty();
      updateNeeded = true;
      update();
      
      if (prev != null) {
      	
        setSelection(prev);
        
      } else {
      	
        selectFirst();
        
      }
      updateSelection = false;
    }

  }

  class NewReservedParameterAction extends Action {


    /**
     * Constructor for NewPropertyAction
     */
    protected NewReservedParameterAction() {
      super();
      setText("New");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      TapestryComponentModel model = (TapestryComponentModel) getModel();
      PluginComponentSpecification spec = model.getComponentSpecification();
      String useParameterName = "reserved";
      
      if (spec.isReservedParameterName(useParameterName + "1")) {
      	
        int counter = 2;
        
        while (spec.isReservedParameterName(useParameterName + counter)) {
        	
          counter++;
        }
        
        useParameterName = useParameterName + counter;
        
      } else {
      	
        useParameterName = useParameterName + 1;
        
      }
      
      spec.setReservedParameter(useParameterName, true);
      forceDirty();
      updateNeeded = true;
      update();
      setSelection(useParameterName);
      updateSelection = false;
    }

  }

  protected class ParameterHolder implements IPropertySource, IIdentifiable {

    private IPropertyDescriptor[] descriptors =
      new IPropertyDescriptor[] { new TextPropertyDescriptor("name", "Reserved Name")};

    private String identifier;

    /**
     * Constructor for PropertyHolder
     */
    public ParameterHolder(String identifier) {
      this.identifier = identifier;
    }

    public void resetPropertyValue(Object key) {
      if ("Name".equals(key)) {
        identifier = null;
      }
    }

    public void setPropertyValue(Object key, Object value) {

      PluginComponentSpecification spec = (PluginComponentSpecification) getParent();

      if ("name".equals(key)) {
      	
        String oldName = this.identifier;
        String newName = ((String) value).toLowerCase();
        
        if ("".equals(newName.trim())) {

          newName = oldName;
          
        } else if (spec.getReservedParameters().contains(newName)) {

          newName = "copyof" + newName;
        }
        this.identifier = newName;
        spec.setReservedParameter(oldName, false);
        spec.setReservedParameter(identifier, true);
      }
    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {
      	
        return identifier != null;
      }
      return false;
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {
      	
        return identifier;
      }
      return "ignore this";
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
      return descriptors;
    }

    public Object getEditableValue() {
      return identifier;
    }

    public String getIdentifier() {
      return identifier;
    }

    public void setIdentifier(String name) {
      this.identifier = name;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getParent()
     */
    public Object getParent() {
      return ((TapestryComponentModel) getModel()).getComponentSpecification();
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
     */
    public void setParent(Object parent) {
    }

  }

}