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
package com.iw.plugins.spindle.editorjwc.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.tapestry.bean.ExpressionBeanInitializer;
import net.sf.tapestry.bean.FieldBeanInitializer;
import net.sf.tapestry.bean.IBeanInitializer;
import net.sf.tapestry.bean.PropertyBeanInitializer;
import net.sf.tapestry.bean.StaticBeanInitializer;
import net.sf.tapestry.bean.StringBeanInitializer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.update.ui.forms.internal.FormSection;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.spec.bean.PluginExpressionBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginFieldBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginPropertyBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginStaticBeanInitializer;
import com.iw.plugins.spindle.spec.bean.PluginStringBeanInitializer;
import com.iw.plugins.spindle.editors.AbstractIdentifiableLabelProvider;
import com.iw.plugins.spindle.editors.AbstractPropertySheetEditorSection;
import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.spec.IIdentifiable;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.EmptySelection;

public class BeanInitializerEditorSection extends AbstractPropertySheetEditorSection {

  private DeleteInitializerAction deleteAction = new DeleteInitializerAction();
  private NewInitializerButtonAction newInitializerAction = new NewInitializerButtonAction();
  private NewPropertyInitializerAction newPropertyAction = new NewPropertyInitializerAction();
  private NewStaticInitializerAction newStaticAction = new NewStaticInitializerAction();
  private NewFieldInitializerAction newFieldAction = new NewFieldInitializerAction();
  private NewStringInitializerAction newStringAction = new NewStringInitializerAction();
  private NewExpressionInitializerAction newExpressionAction = new NewExpressionInitializerAction();

  private PluginBeanSpecification selectedBean;

  /**
   * Constructor for ParameterEditorSection
   */
  public BeanInitializerEditorSection(SpindleFormPage page) {
    super(page);
    setLabelProvider(new InitializerLabelProvider());
    setNewAction(newInitializerAction);
    setDeleteAction(deleteAction);
    setHeaderText("Initializers");
    setDescription("This section allows one to edit selected beans's initializers");
  }

  private int getSelectedDTD() {

    if (selectedBean == null) {

      return XMLUtil.UNKNOWN_DTD;

    }

    PluginComponentSpecification cspec = (PluginComponentSpecification) selectedBean.getParent();

    return XMLUtil.getDTDVersion(cspec.getPublicId());

  }

  public void initialize(Object object) {
    super.initialize(object);

    getViewer().setSorter(new Sorter());

    BaseTapestryModel model = (BaseTapestryModel) object;

    if (!model.isEditable()) {

      newPropertyAction.setEnabled(false);
      newInitializerAction.setEnabled(false);
      newPropertyAction.setEnabled(false);
      newStaticAction.setEnabled(false);
      newFieldAction.setEnabled(false);
      newExpressionAction.setEnabled(false);
      newStringAction.setEnabled(false);
    }
  }

  public void sectionChanged(FormSection source, int changeType, Object changeObject) {
    // this can only come from the BeanSelectionSection and it can only be
    // that a new PluginBeanSpecification was selected!
    selectedBean = (PluginBeanSpecification) changeObject;

    newButton.setEnabled(selectedBean != null);
    deleteButton.setEnabled(selectedBean != null);
    inspectButton.setEnabled(selectedBean != null);

    updateNeeded = true;
    update();
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {

      updateNeeded = true;

    } else if (event.getChangeType() == IModelChangedEvent.CHANGE) {

      String changed = event.getChangedProperty();

      if (changed.equals("beanInitializers")
        || changed.equals("propertyName")
        || changed.equals("propertyPath")
        || changed.equals("staticValue")
        || changed.equals("fieldValue")) {

        updateNeeded = true;
      }
    }
  }

  public void update(BaseTapestryModel model) {
    holderArray = Collections.EMPTY_LIST;
    PluginBeanSpecification spec = selectedBean;

    if (spec == null || spec.getInitializers() == null || spec.getInitializers().isEmpty()) {

      setInput(holderArray);
      fireSelectionNotification(EmptySelection.Instance);
      clearPageSelection();
      return;
    }

    Iterator iter = spec.getInitializers().iterator();
    holderArray = new ArrayList();

    while (iter.hasNext()) {

      holderArray.add((IBeanInitializer) iter.next());
    }
    setInput(holderArray);
  }

  protected void fillContextMenu(IMenuManager manager) {
    ISelection selection = getSelection();
    final Object object = ((IStructuredSelection) selection).getFirstElement();
    MenuManager submenu = new MenuManager("New");
    int DTDVersion = getSelectedDTD();

    if (DTDVersion == XMLUtil.UNKNOWN_DTD) {

      return;

    }

    if (DTDVersion < XMLUtil.DTD_1_3) {

      submenu.add(newPropertyAction);
      submenu.add(newStaticAction);
      submenu.add(newFieldAction);

    } else {

      submenu.add(newExpressionAction);
      submenu.add(newStringAction);

    }

    manager.add(submenu);

    if (object != null) {

      manager.add(new Separator());
      manager.add(deleteAction);

    }
    manager.add(new Separator());
    manager.add(pAction);
  }

  public class InitializerLabelProvider extends AbstractIdentifiableLabelProvider {

    private Image staticImage = TapestryImages.getSharedImage("bean-static-init.gif");
    private Image propertyImage = TapestryImages.getSharedImage("bean-property-init.gif");
    private Image fieldImage = TapestryImages.getSharedImage("bean-property-init.gif");
    private Image expressionImage = TapestryImages.getSharedImage("bean-expression-init.gif");
    private Image stringImage = TapestryImages.getSharedImage("bean-string-init.gif");

    public String getText(Object object) {
      return object.toString();
    }

    public Image getImage(Object object) {

      IBeanInitializer initer = (IBeanInitializer) object;
      if (initer instanceof PluginStaticBeanInitializer) {

        return staticImage;

      }
      if (initer instanceof PluginPropertyBeanInitializer) {

        return propertyImage;

      }
      if (initer instanceof PluginFieldBeanInitializer) {

        return fieldImage;

      }
      if (initer instanceof PluginExpressionBeanInitializer) {

        return expressionImage;

      }
      if (initer instanceof PluginStringBeanInitializer) {

        return stringImage;

      }

      return null;
    }

  }

  public class Sorter extends ViewerSorter {

    /**
    * @see org.eclipse.jface.viewers.ViewerSorter#compare(Viewer, Object, Object)
    */
    public int compare(Viewer viewer, Object a, Object b) {
      String aString = ((IIdentifiable) a).getIdentifier();
      String bString = ((IIdentifiable) b).getIdentifier();

      return aString.compareTo(bString);
    }

  }

  class DeleteInitializerAction extends Action {

    protected DeleteInitializerAction() {
      super();
      setText("Delete");
      setToolTipText("Delete the selected");
    }

    /**
    * @see Action#run()
    */
    public void run() {
      updateSelection = true;
      IIdentifiable initer = (IIdentifiable) getSelected();
      PluginBeanSpecification bean = (PluginBeanSpecification) initer.getParent();

      if (initer != null) {

        String prev = findPrevious(initer.getIdentifier());
        bean.removeInitializer((IBeanInitializer) initer);
        initer.setParent(null);
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

  protected class NewInitializerButtonAction extends Action {

    protected NewInitializerButtonAction() {
      super();
    }

    public void run() {

      if (selectedBean == null) {

        return;

      }

      updateSelection = true;

      int DTDVersion = getSelectedDTD();

      Set existing = new HashSet();

      List initializers = selectedBean.getInitializers();

      if (initializers != null) {

        for (Iterator iter = selectedBean.getInitializers().iterator(); iter.hasNext();) {

          IBeanInitializer element = (IBeanInitializer) iter.next();
          existing.add(element.getPropertyName());
        }
      }

      try {

        IJavaProject project = TapestryPlugin.getDefault().getJavaProjectFor(getModel());

        ChooseBeanInitializerDialog dialog =
          new ChooseBeanInitializerDialog(
            newButton.getShell(),
            DTDVersion,
            project,
            selectedBean.getClassName(),
            existing);

        dialog.create();

        if (dialog.open() == dialog.OK) {

          Class chosen = dialog.getSelectedIntializerClass();
          List chosenProperties = dialog.getPropertyNames();

          if (chosen == PropertyBeanInitializer.class) {

            newPropertyAction.run(chosenProperties);
          } else if (chosen == StaticBeanInitializer.class) {

            newStaticAction.run(chosenProperties);

          } else if (chosen == FieldBeanInitializer.class) {

            newFieldAction.run(chosenProperties);

          } else if (chosen == StringBeanInitializer.class) {

            newStringAction.run(chosenProperties);

          } else if (chosen == ExpressionBeanInitializer.class) {

            newExpressionAction.run(chosenProperties);

          }

        }
      } catch (CoreException e) {

        ErrorDialog.openError(
          newButton.getShell(),
          "Spindle Error",
          "unable to open dialog",
          e.getStatus());

      }
      updateSelection = false;
    }

  }

  protected abstract class BaseNewInitializerAction extends Action {

    protected BaseNewInitializerAction() {
      super();
    }

    public void run() {

      run((String) null);

    }

    public void run(List propertyNames) {

      if (propertyNames == null || propertyNames.isEmpty()) {

        run();

      } else {

        for (Iterator iter = propertyNames.iterator(); iter.hasNext();) {
          run((String) iter.next());

        }

      }

    }

    public void run(String propertyName) {
      if (selectedBean == null) {
        return;
      }
      PluginBeanSpecification bean = selectedBean;

      String newName = "property";

      if (propertyName != null) {

        newName = propertyName;

      }

      if (bean.alreadyHasInitializer(newName)) {
        int counter = 0;
        while (bean.alreadyHasInitializer(newName + counter)) {
          counter++;
        }
        newName = newName + counter;
      }
      IBeanInitializer newInitializer = getNewInitializerFor(newName);
      bean.addInitializer(newInitializer);
      forceDirty();
      update();
      setSelection(((IIdentifiable) newInitializer).getIdentifier());
    }

    protected abstract IBeanInitializer getNewInitializerFor(String propertyName);

  }

  class NewStaticInitializerAction extends BaseNewInitializerAction {
    protected NewStaticInitializerAction() {
      super();
      setText("Static Initializer");
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-static-init.gif")));

    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      Object value = "fill in value";
      return new PluginStaticBeanInitializer(propertyName, value);
    }
  }

  class NewStringInitializerAction extends BaseNewInitializerAction {
    protected NewStringInitializerAction() {
      super();
      setText("String Initializer");
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-string-init.gif")));

    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      String value = "fill in key";
      return new PluginStringBeanInitializer(propertyName, value);
    }
  }

  class NewExpressionInitializerAction extends BaseNewInitializerAction {
    protected NewExpressionInitializerAction() {
      super();
      setText("Expression Initializer");
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-expression-init.gif")));

    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      String value = "fill in expression";
      return new PluginExpressionBeanInitializer(propertyName, value);
    }
  }

  class NewPropertyInitializerAction extends BaseNewInitializerAction {

    protected NewPropertyInitializerAction() {
      super();
      setText("Property Initializer");
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-property-init.gif")));
    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      return new PluginPropertyBeanInitializer(propertyName, "fill in value");
    }

  }
  class NewFieldInitializerAction extends BaseNewInitializerAction {

    protected NewFieldInitializerAction() {
      super();
      setText("Field Initializer");
      setImageDescriptor(
        ImageDescriptor.createFromURL(TapestryImages.getImageURL("bean-property-init.gif")));
    }

    public IBeanInitializer getNewInitializerFor(String propertyName) {
      return new PluginFieldBeanInitializer(propertyName, "select value");
    }

  }

}