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
package com.iw.plugins.spindle.ui.descriptors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.ui.ChooseWorkspaceModelDialog;
import com.iw.plugins.spindle.ui.EditableDialogCellEditor;

public class WorkspaceStoragePropertyDescriptor
  extends PropertyDescriptor
  implements INeedsModelInitialization {

  private IJavaProject project;
  private int acceptFlags = 0;
  private String dialogTitle;
  private String dialogDescription;

  public WorkspaceStoragePropertyDescriptor(
    Object id,
    String displayName,
    String dialogTitle,
    String dialogDescription,
    int acceptFlags) {
    super(id, displayName);
    this.dialogTitle = dialogTitle;
    this.dialogDescription = dialogDescription;
    this.acceptFlags = this.acceptFlags | acceptFlags;
  }

  /**
   * @see com.iw.plugins.spindle.ui.descriptors.INeedsModelInitialization#initialize(ITapestryModel)
   */
  public void initialize(ITapestryModel model) {
    Assert.isNotNull(model);
    try {
    	
      project = TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
      
    } catch (CoreException e) {
    }
  }


  public CellEditor createPropertyEditor(Composite parent) {
  	Assert.isNotNull(project);
    return new ChooseWorkspaceModelCellEditor(parent);
  }

  class ChooseWorkspaceModelCellEditor extends EditableDialogCellEditor {

    /**
     * Constructor for ChooseWorkspaceModelCellEditor.
     * @param parent
     */
    public ChooseWorkspaceModelCellEditor(Composite parent) {
      super(parent);
    }

    /**
    * @see com.iw.plugins.spindle.ui.EditableDialogCellEditor#openDialogBox(Control)
    */
    protected Object openDialogBox(Control cellEditorWindow) {

      Object value = getValue();

        ChooseWorkspaceModelDialog dialog =
          new ChooseWorkspaceModelDialog(
            cellEditorWindow.getShell(),
            project,
            dialogTitle,
            dialogDescription,
            acceptFlags, false);
                    
                   
        if (dialog.open() == dialog.OK) {
        	String resultName = dialog.getResultString();
        	IPackageFragment resultPackage = dialog.getResultPackage();
        	
        	if (resultPackage == null || "".equals(resultPackage.getElementName())) {
        		
        		value = "/"+resultName;
        		
        	} else {
        		
        		String packageString = resultPackage.getElementName().replace('.', '/');
        		value =  "/" + packageString + "/" + resultName;
        	}
        	
        }          
      return value;

    }

  }

}