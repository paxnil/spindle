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

package com.iw.plugins.spindle.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.ui.wizards.NewTapestryElementWizard;

/**
 * Base class for actions that ewill launch a Tapestry wizard
 * 
 * @author glongman@gmail.com
 * @version $Id: AbstractTapestryResourceWizardAction.java,v 1.1 2003/10/29
 *          12:33:56 glongman Exp $
 */
public abstract class AbstractTapestryResourceWizardAction
    extends
      AbstractTapestryResourceAction
{

  public AbstractTapestryResourceWizardAction()
  {
    super();
  }

  protected abstract NewTapestryElementWizard getWizard();

  /**
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action)
  {
    if (fSelection != null)
    {
      try
      {
        for (Iterator iter = fSelection.iterator(); iter.hasNext();)
        {
          IFile file = (IFile) iter.next();
          NewTapestryElementWizard wizard = getWizard();
          wizard.init(PlatformUI.getWorkbench(), fSelection, getName(file));
          WizardDialog wdialog = new WizardDialog(UIPlugin
              .getDefault()
              .getActiveWorkbenchShell(), wizard);
          wdialog.open();
        }
      } catch (ClassCastException e)
      {
        // do nothing
      }
    }
  }

}