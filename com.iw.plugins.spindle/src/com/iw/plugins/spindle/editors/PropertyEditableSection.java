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

import net.sf.tapestry.util.IPropertyHolder;
import org.eclipse.jface.action.Action;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.spec.IIdentifiable;

public class PropertyEditableSection
  extends AbstractPropertySheetEditorSection
  implements IModelChangedListener {
  	
  protected IPropertyHolder externalPropertyHolder;

  /**
   * Constructor for PropertySection 
   */
  public PropertyEditableSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(new PropertyLabelProvider());
    setNewAction(new NewPropertyAction());
    setDeleteAction(new DeletePropertyAction());
    setHeaderText("Properties");
    setDescription("This section describes properties you can edit");
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      updateNeeded = true;
    }
    if (event.getChangeType() == IModelChangedEvent.CHANGE) {
      if (event.getChangedProperty().equals("properties")) {
        updateNeeded = true;
      }
    }
  }
  
  public void initialize(Object input) {
    BaseTapestryModel model = (BaseTapestryModel) input;
    setDefaultExternalPropertyHolder((IPropertyHolder)model);
    super.initialize(input);
  }
  
  protected void setDefaultExternalPropertyHolder(IPropertyHolder holder) {
    externalPropertyHolder = holder;    
  }	
  	

  public void update(BaseTapestryModel model) {
  	
    holderArray.removeAll(holderArray);
    
    boolean hasPropertyHolder = externalPropertyHolder != null;
    
    newButton.setEnabled(hasPropertyHolder);
    deleteButton.setEnabled(hasPropertyHolder);
    inspectButton.setEnabled(hasPropertyHolder);

	if (!hasPropertyHolder) {
		
		setInput(holderArray);
		return;
	}  	
  	
    Iterator iter = externalPropertyHolder.getPropertyNames().iterator();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      PropertyHolder holder = new PropertyHolder(name, externalPropertyHolder.getProperty(name));
      holder.setParent(externalPropertyHolder);
      holderArray.add(holder);
    }
    boolean editable = isModelEditable();
    newButton.setEnabled(editable);
    deleteButton.setEnabled(editable);
    if (inspectButton != null) {
      inspectButton.setEnabled(editable);
    }
    setInput(holderArray);
  }


  public class PropertyLabelProvider extends AbstractIdentifiableLabelProvider {

    private Image image = TapestryImages.getSharedImage("property16.gif");

    public String getText(Object object) {
      PropertyHolder holder = (PropertyHolder) object;
      return (holder.identifier + " = " + holder.value);
    }

    public void dispose() {
      // shared images are disposed by the Plugin
    }

   
    public Image getImage(Object object) {
      return image;
    }

   
  }


  class DeletePropertyAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected DeletePropertyAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PropertyHolder holder = (PropertyHolder) getSelected();
      if (holder != null) {        
        String prev = findPrevious(holder.identifier);
        externalPropertyHolder.removeProperty(holder.identifier);
        holder.setParent(null);
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

  class NewPropertyAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected NewPropertyAction() {
      super();
      setText("New");
      setToolTipText("Create a new Property");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      String useProperty = "property";
      if (externalPropertyHolder.getProperty(useProperty + 1) != null) {
        int counter = 2;
        while (externalPropertyHolder.getProperty(useProperty + counter) != null) {
          counter++;
        }
        useProperty = useProperty + counter;
      } else {
        useProperty = useProperty + 1;
      }
      externalPropertyHolder.setProperty(useProperty, "fill in value");
      forceDirty();
      update();
      setSelection(useProperty);
      updateSelection = false;
    }

  }

  private static IPropertyDescriptor[] descriptors;

  static {
    descriptors =
      new IPropertyDescriptor[] {
        new TextPropertyDescriptor("name", "Name"),
        new TextPropertyDescriptor("value", "Value")};
  }

  protected class PropertyHolder implements IPropertySource, IIdentifiable {

    public String identifier;
    public String value;
    private IPropertyHolder parent;

    /**
     * Constructor for PropertyHolder
     */
    public PropertyHolder(String name, String value) {
      super();
      this.identifier = name;
      this.value = value;
    }

    public void resetPropertyValue(Object key) {
      if ("Name".equals(key)) {
        identifier = null;
      } else if ("Value".equals(key)) {
        value = null;
      }
    }

    public void setPropertyValue(Object key, Object value) {

      IPropertyHolder pholder = (IPropertyHolder) getParent();

      if ("name".equals(key)) {

        String oldName = this.identifier;
        String newName = (String) value;

        if ("".equals(newName.trim())) {

          newName = oldName;

        } else if (pholder.getProperty(newName) != null) {

          newName = "Copy of " + newName;

        }

        this.identifier = newName;
        pholder.setProperty(oldName, null);
        pholder.setProperty(this.identifier, this.value);

      } else if ("value".equals(key)) {

        this.value = (String) value;
        pholder.setProperty(this.identifier, this.value);
      }

    }

    public boolean isPropertySet(Object key) {
      if ("name".equals(key)) {

        return identifier != null;

      } else if ("value".equals(key)) {

        return value != null;

      }
      return false;
    }

    public Object getPropertyValue(Object key) {
      if ("name".equals(key)) {

        return identifier;

      } else if ("value".equals(key)) {

        return value;
      }
      return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
      return descriptors;
    }

    public Object getEditableValue() {
      return identifier;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getIdentifier()
     */
    public String getIdentifier() {
      return identifier;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getParent()
     */
    public Object getParent() {
      return parent;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setIdentifier(String)
     */
    public void setIdentifier(String id) {
      this.identifier = id;
    }

    /**
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(Object)
     */
    public void setParent(Object parent) {
    	this.parent = (IPropertyHolder)parent;
    }

  }

}