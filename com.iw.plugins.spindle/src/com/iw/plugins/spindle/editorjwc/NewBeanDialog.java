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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.dialogfields.DialogField;
import com.iw.plugins.spindle.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.dialogfields.StringButtonField;
import com.iw.plugins.spindle.dialogfields.StringField;
import com.iw.plugins.spindle.dialogfields.UneditableComboBoxDialogField;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.ui.AbstractDialog;
import com.iw.plugins.spindle.util.TapestryLookup;
import net.sf.tapestry.spec.BeanLifecycle;

public class NewBeanDialog extends AbstractDialog {

  static private int FIELD_WIDTH = 64;

  private boolean editing;
  private StringField beanName;
  private StringButtonField beanClassname;
  private UneditableComboBoxDialogField lifecycleCombo;
  private ITapestryModel model;
  private NewBeanDialogAdapter adapter = new NewBeanDialogAdapter();

  private IPackageFragmentRoot root;

  private Collection existingNames;

  private String resultName;
  private PluginBeanSpecification resultBeanSpec;

  private String[] comboChoices = { "None", "Page", "Request" };
  private BeanLifecycle[] lifecyclechoices = { BeanLifecycle.NONE, BeanLifecycle.PAGE, BeanLifecycle.REQUEST };

  /**
   * Constructor for PageRefDialog
   */
  public NewBeanDialog(Shell shell, ITapestryModel model, Collection existingBeanNames) {
    super(shell);
    String windowTitle = "New Bean";
    String description = "Enter the new information";
    updateWindowTitle(windowTitle);
    updateMessage(description);
    calculateRoot(model);
    this.existingNames = existingBeanNames;
  }

  /**
   * @see AbstractDialog#createAreaContents(Composite)
   */
  protected Composite createAreaContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    FormLayout layout = new FormLayout();
    layout.marginHeight = 4;
    layout.marginWidth = 4;
    container.setLayout(layout);

    beanName = new StringField("Bean name:", FIELD_WIDTH);
    beanName.addListener(adapter);
    Control beanNameControl = beanName.getControl(container);

    beanClassname = new StringButtonField("Bean Class:", FIELD_WIDTH);
    beanClassname.addListener(adapter);
    Control beanClassnameControl = beanClassname.getControl(container);

    lifecycleCombo = new UneditableComboBoxDialogField("Bean Lifecycle:", FIELD_WIDTH, comboChoices);
    lifecycleCombo.addListener(adapter);
    Control lifecycleControl = lifecycleCombo.getControl(container);

    FormData data = new FormData();
    data.top = new FormAttachment(0, 5);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    beanNameControl.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(beanNameControl, 4);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    beanClassnameControl.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(beanClassnameControl, 4);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    lifecycleControl.setLayoutData(data);

    return container;
  }

  protected void calculateRoot(ITapestryModel model) {
    IStorage underlier = model.getUnderlyingStorage();
    IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(underlier);
    TapestryLookup lookup = new TapestryLookup();
    try {
      lookup.configure(jproject);
      IPackageFragment frag = lookup.findPackageFragment(underlier);
      Assert.isNotNull(frag);
      root = (IPackageFragmentRoot) frag.getParent();
      Assert.isNotNull(root);
    } catch (JavaModelException jmex) {
      TapestryPlugin.getDefault().logException(jmex);
    }
  }

  /**
   * @see AbstractDialog#performCancel()
   */
  protected boolean performCancel() {
    setReturnCode(CANCEL);
    return true;

  }

  protected void cancelPressed() {
    performCancel();
    close();
  }

  public boolean close() {
    return hardClose();
  }

  protected boolean okToClose() {
    resultName = beanName.getTextValue().trim();

    if ("".equals(resultName)) {

      setReturnCode(CANCEL);
      resultName = null;
      resultBeanSpec = null;
      return false;
    } else if (existingNames.contains(resultName)) {
      setErrorMessage("There is already a bean by the name '" + resultName + "'. Try another");
      setReturnCode(CANCEL);
      resultName = null;
      resultBeanSpec = null;
      return false;
    } else {
      setErrorMessage(null);
    }
    String classname = beanClassname.getTextValue().trim();
    if ("".equals(classname)) {
      setReturnCode(CANCEL);
      resultBeanSpec = null;
      return false;
    }
    resultBeanSpec = new PluginBeanSpecification(classname, lifecyclechoices[lifecycleCombo.getSelectedIndex()]);
    return true;
  }

  public String getResultName() {
    return resultName;
  }

  public PluginBeanSpecification getResultBeanSpec() {
    return resultBeanSpec;
  }

  protected String chooseBeanClass() {
    String value = beanClassname.getTextValue();
    IJavaElement[] elements = new IJavaElement[] { root.getJavaProject()};
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements);

    IProject project = root.getJavaProject().getProject();
    try {
      SelectionDialog dialog =
        JavaUI.createTypeDialog(
          getShell(),
          new ProgressMonitorDialog(getShell()),
          scope,
          IJavaElementSearchConstants.CONSIDER_CLASSES,
          false);
      dialog.setTitle("Bean Class");
      dialog.setMessage("Choose a Class");
      if (dialog.open() == dialog.OK) {
        return ((IType) dialog.getResult()[0]).getFullyQualifiedName();
      }
    } catch (JavaModelException jmex) {
      TapestryPlugin.getDefault().logException(jmex);
    }
    return value;
  }

  protected class NewBeanDialogAdapter implements IDialogFieldChangedListener {

    public void dialogFieldChanged(DialogField field) {
      update();
    }
    /**
     * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
     */
    public void dialogFieldButtonPressed(DialogField field) {
      beanClassname.setTextValue(chooseBeanClass());
    }

    /**
     * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
     */
    public void dialogFieldStatusChanged(IStatus status, DialogField field) {
    }

  }
}
