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

import net.sf.tapestry.parse.SpecificationParser;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.update.ui.forms.internal.FormEntry;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import org.eclipse.update.ui.forms.internal.IFormTextListener;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.FormCheckbox;
import com.iw.plugins.spindle.editors.IFormCheckboxListener;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.editors.SpindleFormSection;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.util.Utils;

public class OverviewGeneralSection extends SpindleFormSection implements IModelChangedListener {

  private Text dtdText;
  private Text pathText;
  private FormEntry componentClassText;
  private Label bodyLabel = null;
  private Label informalsLabel = null;
  private FormCheckbox allowBody = null;
  private FormCheckbox allowInformalParameters = null;
  private boolean updateNeeded;
  private ChooseSpecClassAction chooseComponentSpecAction = new ChooseSpecClassAction();
  private ChoosePageSpecClassAction choosePageSpecAction = new ChoosePageSpecClassAction();
  private OpenSpecClassAction openSpecClassAction = new OpenSpecClassAction();

  /**
   * Constructor for TapistryAppGeneralSestion
   */
  public OverviewGeneralSection(SpindleFormPage page) {
    super(page);
    setHeaderText("General Information");
    setDescription("This section describes general information");
  }

  public void initialize(Object input) {
    TapestryComponentModel model = (TapestryComponentModel) input;
    update(input);
    dtdText.setEditable(false);
    pathText.setEditable(false);
    if (model.isEditable() == false) {
      componentClassText.getControl().setEditable(false);
    }
    model.addModelChangedListener(this);
  }

  public void dispose() {
    dtdText.dispose();
    pathText.dispose();

    if (allowBody != null) {

      bodyLabel.dispose();
      allowBody.getControl().dispose();
    }

    if (allowInformalParameters != null) {

      informalsLabel.dispose();
      allowInformalParameters.getControl().dispose();
    }

    ((BaseTapestryModel) getFormPage().getModel()).removeModelChangedListener(this);
    super.dispose();
  }

  public void update() {
    if (updateNeeded && canUpdate()) {
      this.update(getFormPage().getModel());
    }
  }

  public void update(Object input) {
    TapestryComponentModel model = (TapestryComponentModel) input;
    if (!model.isLoaded()) {
      return;
    }

    PluginComponentSpecification spec = model.getComponentSpecification();

    IStorage storage = (IStorage) model.getUnderlyingStorage();
    String path = storage.getFullPath().toString();
    String name = storage.getName();
    if (!model.isEditable()) {
      name += "  (READ ONLY)";
    }

    dtdText.setText(spec.getPublicId());
    boolean editable = model.isEditable();

    getFormPage().getForm().setHeadingText(name);

    ((SpindleMultipageEditor) getFormPage().getEditor()).updateTitle();

    pathText.setText(path);

    componentClassText.setValue(spec.getComponentClassName(), true);
    componentClassText.getControl().setEditable(editable);

    if (allowBody != null) {

      allowBody.setValue(spec.getAllowBody(), true);
      allowBody.getControl().setEnabled(editable);
    }

    if (allowInformalParameters != null) {

      allowInformalParameters.setValue(spec.getAllowInformalParameters(), true);
      allowInformalParameters.getControl().setEnabled(editable);
    }

    updateNeeded = false;
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClientContainer(Composite parent, FormWidgetFactory factory) {
    Composite container = factory.createComposite(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 7;
    layout.horizontalSpacing = 6;
    container.setLayout(layout);

    final TapestryComponentModel model = (TapestryComponentModel) getFormPage().getModel();

    String labelName = "DTD";
    dtdText = createText(container, labelName, factory);
    dtdText.setText("-//Howard Ship//Tapestry Specification 1.2//EN");
    dtdText.setEnabled(false);

    labelName = "Component Path";
    pathText = createText(container, labelName, factory);
    pathText.setEnabled(false);

    labelName = "Specification Class";
    Text text = createText(container, labelName, factory);
    componentClassText = new FormEntry(text);
    componentClassText.addFormTextListener(new IFormTextListener() {
      //called on commit	
      public void textValueChanged(FormEntry text) {
        model.getComponentSpecification().setComponentClassName(text.getValue());
      }

      public void textDirty(FormEntry text) {
        forceDirty();
      }
    });
    MenuManager popupMenuManager = new MenuManager();
    IMenuListener listener = new IMenuListener() {
      public void menuAboutToShow(IMenuManager mng) {
        fillContextMenu(mng);
      }
    };
    popupMenuManager.setRemoveAllWhenShown(true);
    popupMenuManager.addMenuListener(listener);
    Menu menu = popupMenuManager.createContextMenu(componentClassText.getControl());
    componentClassText.getControl().setMenu(menu);

    PluginComponentSpecification componentSpec =
      (PluginComponentSpecification) model.getComponentSpecification();

    if (!componentSpec.isPageSpecification()) {

      labelName = "Allow Body";
      bodyLabel = factory.createLabel(container, labelName);
      allowBody = new FormCheckbox(container, null);
      allowBody.addFormCheckboxListener(new IFormCheckboxListener() {
        public void booleanValueChanged(FormCheckbox box) {
          model.getComponentSpecification().setAllowBody(box.getValue());
        }

        public void valueDirty(FormCheckbox box) {
          forceDirty();
        }
      });
      ((Button) allowBody.getControl()).setBackground(factory.getBackgroundColor());

      labelName = "Allow Informal Paramters";
      informalsLabel = factory.createLabel(container, labelName);
      allowInformalParameters = new FormCheckbox(container, null);
      allowInformalParameters.addFormCheckboxListener(new IFormCheckboxListener() {
        //called on commit
        public void booleanValueChanged(FormCheckbox box) {
          model.getComponentSpecification().setAllowInformalParameters(box.getValue());
        }

        public void valueDirty(FormCheckbox box) {
          forceDirty();
        }
      });
      ((Button) allowInformalParameters.getControl()).setBackground(factory.getBackgroundColor());

    }

    factory.paintBordersFor(container);
    return container;
  }

  protected void fillContextMenu(IMenuManager manager) {
    String engineClass = componentClassText.getValue();
    openSpecClassAction.setEnabled(engineClass != null && !"".equals(engineClass.trim()));
    TapestryComponentModel model = (TapestryComponentModel) getFormPage().getModel();
    chooseComponentSpecAction.setEnabled(model.isEditable());

    manager.add(openSpecClassAction);
    
    if (allowBody == null) {
    	
      manager.add(choosePageSpecAction);
      
    } else {
    	
      manager.add(chooseComponentSpecAction);
      manager.add(choosePageSpecAction);
    }
  }

  public boolean isDirty() {

    boolean result = componentClassText.isDirty();

    if (!result && allowBody != null) {

      result = allowBody.isDirty();
    }

    if (!result && allowInformalParameters != null) {

      result = allowInformalParameters.isDirty();
    }
    return result;
  }

  private void forceDirty() {
    setDirty(true);
    IModel model = (IModel) getFormPage().getModel();
    
    if (model instanceof IEditable) {
    	
      IEditable editable = (IEditable) model;
      editable.setDirty(true);
      getFormPage().getEditor().fireSaveNeeded();
    }
  }

  public void commitChanges(boolean onSave) {
    componentClassText.commit();

    if (allowBody != null) {

      allowBody.commit();

    }
    if (allowInformalParameters != null) {

      allowInformalParameters.commit();

    }
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
    	
      updateNeeded = true;
    }
  }

  class OpenSpecClassAction extends Action {

    /**
     * Constructor for NewPropertyAction
     */
    protected OpenSpecClassAction() {
      super();
      setText("open specification class");
      setToolTipText("open the specification class in an editor");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      String engineClass = componentClassText.getValue();
      ITapestryModel model = (ITapestryModel) getFormPage().getModel();
      IJavaProject jproject =
        TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
      try {
        IType type = Utils.findType(jproject, engineClass);
        JavaUI.openInEditor(type);
      } catch (Exception e) {
        MessageDialog.openError(
          componentClassText.getControl().getShell(),
          "Error opening editor",
          "could not open an editor for " + engineClass);
      }
    }
  }

  public class ChooseSpecClassAction extends Action {

    protected String hierarchyRoot;
    /**
     * Constructor for NewPropertyAction
     */
    protected ChooseSpecClassAction() {
      super();
      setText("choose component spec class");
      setToolTipText("choose component spec class");
      hierarchyRoot = "net.sf.tapestry.IComponent";
    }

    /**
    * @see Action#run()
    */
    public void run() {
      IType newEngine = chooseType("Choose Component Class", "choose Component class");
      if (newEngine != null) {
        componentClassText.setValue(newEngine.getFullyQualifiedName());
      }
    }

    protected IType chooseType(String title, String message) {
      ITapestryModel model = (ITapestryModel) getFormPage().getModel();
      IJavaProject jproject =
        TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
      if (jproject == null) {
        return null;
      }
      IJavaSearchScope scope = createSearchScope(jproject);
      Shell shell = componentClassText.getControl().getShell();
      try {

        SelectionDialog dialog =
          JavaUI.createTypeDialog(
            shell,
            new ProgressMonitorDialog(shell),
            scope,
            IJavaElementSearchConstants.CONSIDER_CLASSES,
            false);

        dialog.setTitle(title);
        dialog.setMessage(
          hierarchyRoot == null ? message : message + " (implements " + hierarchyRoot + ")");

        if (dialog.open() == dialog.OK) {
          return (IType) dialog.getResult()[0]; //FirstResult();
        }
      } catch (JavaModelException jmex) {
        TapestryPlugin.getDefault().logException(jmex);
      }
      return null;
    }

    protected IJavaSearchScope createSearchScope(IJavaProject jproject) {

      IJavaSearchScope result = null;
      IType hrootElement = null;
      try {
        if (hierarchyRoot != null) {
          hrootElement = Utils.findType(jproject, hierarchyRoot);
        }
        if (hrootElement != null) {
          result = SearchEngine.createHierarchyScope(hrootElement);
        }
      } catch (JavaModelException jmex) {
        //ignore
        jmex.printStackTrace();
      }
      if (result == null) {
        IJavaElement[] elements = new IJavaElement[] { jproject };
        result = SearchEngine.createJavaSearchScope(elements);
      }
      return result;
    }

  }

  public class ChoosePageSpecClassAction extends ChooseSpecClassAction {

    /**
     * Constructor for NewPropertyAction
     */
    public ChoosePageSpecClassAction() {
      super();
      setText("choose page spec class");
      setToolTipText("choose page spec class");
      hierarchyRoot = "net.sf.tapestry.IPage";
    }

    /**
    * @see Action#run()
    */
    public void run() {
      IType newSpec = super.chooseType("Choose Page Class", "choose Page class");
      if (newSpec != null) {
        componentClassText.setValue(newSpec.getFullyQualifiedName());
      }
    }

  }

}