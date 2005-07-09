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

package com.iw.plugins.spindle.editors.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;

import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Jump from spec/template editors to associated java files
 * 
 * @author glongman@gmail.com
 * 
 */
public class JumpToSpecAction extends BaseJumpAction
{

  public JumpToSpecAction()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.action.IAction#run()
   */
  protected void doRun()
  {
    ICoreResource location = getSpecLocation();
    if (location == null && fEditor instanceof TemplateEditor)
    {

      MessageDialog.openInformation(
          fEditor.getEditorSite().getShell(),
          "Operation Aborted",
          "Unable to Jump to Specifications from  a jar based Template");
      return;
    }
    reveal(location);
  }

  protected ICoreResource getSpecLocation()
  {
    if (!(fEditor instanceof TemplateEditor))
      return null;

    BaseSpecLocatable spec = (BaseSpecLocatable) fEditor.getSpecification();
    if (spec != null)
      return (ICoreResource) spec.getSpecificationLocation();

    return null;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.actions.BaseEditorAction#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
   */
  public void editorContextMenuAboutToShow(IMenuManager menu)
  {
    ICoreResource location = getSpecLocation();
    if (location != null)
    {
      Action action = new MenuOpenSpecAction(location);
      menu.add(action);
      action.setEnabled(location.exists());
    }
  }

  class MenuOpenSpecAction extends Action
  {
    ICoreResource location;

    public MenuOpenSpecAction(ICoreResource location)
    {
      Assert.isNotNull(location);
      this.location = location;
      setText(location.getName());
    }

    public void run()
    {
      reveal(location);
    }
  }

}