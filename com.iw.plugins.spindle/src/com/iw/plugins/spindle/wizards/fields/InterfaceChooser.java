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
package com.iw.plugins.spindle.wizards.fields;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class InterfaceChooser extends ListChooser {

  PackageDialogField packageChooser;
  IRunnableContext context;
  String name;
  List interfaces = new ArrayList();
  /**
   * Constructor for InterfaceChooser.
   * @param labelText
   */
  public InterfaceChooser(String labelText) {
    this(labelText, -1);
  }

  /**
   * Constructor for InterfaceChooser.
   * @param labelText
   * @param labelWidth
   */
  public InterfaceChooser(String labelText, int labelWidth) {
    super(MessageUtil.getString(labelText+".label"), labelWidth);
    this.name = labelText;
  }

  protected IRunnableContext getRunnableContext() {
    return (context == null ? new ProgressMonitorDialog(getShell()) : context);
  }

  /**
   * @see DialogField#getControl(Composite)
   */
  public Control getControl(Composite parent) {
    Control result = super.getControl(parent);
    setContentProvider(new ChooserContentProvider());
    int flags = JavaElementLabelProvider.SHOW_SMALL_ICONS | JavaElementLabelProvider.SHOW_POST_QUALIFIED;
    setLabelProvider(new JavaElementLabelProvider(flags));
    setInput(interfaces);
    return result;
  }
  
  public void init(PackageDialogField packageChooser, IRunnableContext context) {
    this.packageChooser = packageChooser;
    this.context = context;
  }

  /**
  * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
  */
  public void dialogFieldButtonPressed(DialogField field) {
    
      if (field != this) {
      return;
    }
    IType type = chooseInterface();
    if (type != null) {
      addNewInterface(type);
    }
  }
  
  protected IType chooseInterface() {

    IPackageFragmentRoot root = packageChooser.getContainer().getPackageFragmentRoot();
    if (root == null) {
      return null;
    }

    IJavaProject jproject = root.getJavaProject();
    IJavaElement[] elements = new IJavaElement[] { jproject };
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements);

    try {
      SelectionDialog dialog =
        JavaUI.createTypeDialog(getShell(), getRunnableContext(), scope, IJavaElementSearchConstants.CONSIDER_INTERFACES, false);
      dialog.setTitle(MessageUtil.getString(name + ".InterfaceChooser.title"));
      dialog.setMessage(MessageUtil.getString(name + ".InterfaceChooser.message"));

      if (dialog.open() == dialog.OK) {
        return (IType) dialog.getResult()[0]; //FirstResult();
      }
    } catch (JavaModelException jmex) {
      TapestryPlugin.getDefault().logException(jmex);
    }
    return null;
  }
  
  protected void addNewInterface(IType type) {
    if (!interfaces.contains(type)) {
      interfaces.add(type);
      setInput(interfaces);
    }
    reveal(type);
  }
  
   /**
   * @see ListChooser#removeButtonPressed()
   */
  public void removeButtonPressed() {
    int index = getSelectedIndex(); 
    interfaces.remove(index);
    setInput(interfaces);
  }
  
  public IType [] getInterfaces() {
    return (IType[]) interfaces.toArray(new IType[interfaces.size()]);
  }

  /**
  * @author GWL
  * @version 
  *
  * Copyright 2002, Intelligent Works Incoporated
  * All Rights Reserved
  */
  public class ChooserContentProvider implements IStructuredContentProvider {

    /**
    * @see IStructuredContentProvider#getElements(Object)
    */
    public Object[] getElements(Object inputElement) {
      List ifaces = (List)inputElement;
      return ifaces.toArray();
    }

    /**
     * @see IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see IContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

  }

 

 
}