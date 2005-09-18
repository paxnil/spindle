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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.classpath;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;

/**
 * Extension to allow a user to associate the default Tapestry Framework with a
 * Java project.
 */
public class CoreContainerWizardPage extends WizardPage
    implements
      IClasspathContainerPage
{

  /**
   * The classpath entry to be created.
   */
  private IClasspathEntry fSelection;

  /**
   * Image
   */
  private Image fImage;

  /**
   * Constructs a new page.
   */
  public CoreContainerWizardPage()
  {
    super(UIPlugin.getString("classpath-container-wizard-page-title"));
  }

  /**
   * @see IClasspathContainerPage#finish()
   */
  public boolean finish()
  {
    fSelection = JavaCore.newContainerEntry(new Path(TapestryCorePlugin.CORE_CONTAINER));

    return true;
  }

  /**
   * @see IClasspathContainerPage#getSelection()
   */
  public IClasspathEntry getSelection()
  {
    return fSelection;
  }

  /**
   * @see IClasspathContainerPage#setSelection(IClasspathEntry)
   */
  public void setSelection(IClasspathEntry containerEntry)
  {
    // do nothing
  }

  /**
   * @see IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent)
  {

    Composite container = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 7;
    layout.horizontalSpacing = 6;
    container.setLayout(layout);

    Label label = new Label(container, SWT.NULL);

    label.setText(UIPlugin.getString("classpath-container-wizard-label"));

    setControl(container);

    setTitle(UIPlugin.getString("classpath-container-wizard-title"));
    setMessage(UIPlugin.getString("classpath-container-wizard-message"));

    //        fJRETab = new JavaJRETab();
    //        fJRETab.setVMSpecificArgumentsVisible(false);
    //        fJRETab.createControl(parent);
    //        setControl(fJRETab.getControl());
    //        setTitle(LauncherMessages.getString("JREContainerWizardPage.JRE_System_Library_1"));
    // //$NON-NLS-1$
    //        setMessage(LauncherMessages.getString("JREContainerWizardPage.Select_the_JRE_used_to_build_this_project._4"));
    // //$NON-NLS-1$
    //
    //        ILaunchConfigurationType type =
    //            DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(
    //                IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
    //        try
    //        {
    //            fConfig = type.newInstance(null, "TEMP_CONFIG"); //$NON-NLS-1$
    //        } catch (CoreException e)
    //        {
    //            JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JREContainerWizardPage.Unable_to_retrieve_existing_JREs_6"),
    // e); //$NON-NLS-1$
    //            return;
    //        }
    //
    //        initializeFromSelection();
  }

  /**
   * @see IDialogPage#getImage()
   */
  public Image getImage()
  {
    if (fImage == null)
    {
      // The image is shared - no need to dispose of it.
      fImage = Images.getSharedImage("applicationDialog.gif");
    }
    return fImage;
  }

}