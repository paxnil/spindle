package com.iw.plugins.spindle.editorlib.extensions;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.spec.PluginExtensionConfiguration;
import com.iw.plugins.spindle.spec.PluginExtensionSpecification;
import com.iw.plugins.spindle.ui.EmptySelection;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class ExtensionConfigurationSection extends AbstractPropertySheetEditorSection {


  private PluginExtensionSpecification selectedExtension;
  
  /**
   * Constructor for ExtensionConfigurationSection.
   * @param page
   */
  public ExtensionConfigurationSection(SpindleFormPage page) {
    super(page);
    setNewAction(new NewConfigAction());
    setDeleteAction(new DeleteConfigAction());
    setLabelProvider(new ConfigLabelProvider());
    setHeaderText("Extension Configuration");
    setDescription("This section lets you work with configuration properties");

  }



  /**
   * @see com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection#update(BaseTapestryModel)
   */
  public void update(BaseTapestryModel model) {
    holderArray.removeAll(holderArray);
    if (selectedExtension == null) {
      setInput(holderArray);
      fireSelectionNotification(EmptySelection.Instance);
      clearPageSelection();
      return;
    }
    Map configurations = selectedExtension.getConfiguration();
    Iterator iter = configurations.keySet().iterator();
    while (iter.hasNext()) {
      String propertyName = (String) iter.next();
      holderArray.add(configurations.get(propertyName));
    }
    setInput(holderArray);
    //selectFirst();
  }

  /**
   * @see org.eclipse.update.ui.forms.internal.FormSection#sectionChanged(FormSection, int, Object)
   */
  public void sectionChanged(FormSection source, int changeType, Object changeObject) {

    selectedExtension = (PluginExtensionSpecification) changeObject;

    newButton.setEnabled(selectedExtension != null);
    editButton.setEnabled(selectedExtension != null);
    deleteButton.setEnabled(selectedExtension != null);
    updateNeeded = true;
    update();
  }
  
  public class ConfigLabelProvider extends AbstractIdentifiableLabelProvider {

    Image configImage = TapestryImages.getSharedImage("ext_config.gif");
    
    public Image getImage(Object element) {
      return configImage;
    }

}

  class DeleteConfigAction extends Action {

    protected DeleteConfigAction() {
      super();
      setText("Delete");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      PluginExtensionConfiguration config = (PluginExtensionConfiguration) getSelected();
      
      if (config != null) {
      	
        String prev = findPrevious(config.getIdentifier());
        PluginExtensionSpecification parent = (PluginExtensionSpecification)config.getParent();
        parent.removeConfiguration(config.getIdentifier());
        parent.setParent(null);
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

  class NewConfigAction extends Action {

    protected NewConfigAction() {
      super();
      setText("New");
    }

    /**
    * @see Action#run()
    */
    public void run() {
    
      Assert.isNotNull(selectedExtension);
      updateSelection = true;
      String useName = "configProperty";
      
      Map configurations = selectedExtension.getConfiguration();
      
      if (configurations.get(useName + 1) != null) {
        int counter = 2;
        while (configurations.get(useName + counter) != null) {
          counter++;
        }
        useName = useName + counter;
      } else {
        useName = useName + 1;
      }
      
      selectedExtension.addConfiguration(useName, Boolean.TRUE);
      forceDirty();
      update();
      setSelection(useName);
      updateSelection = false;
    }

  }

}
