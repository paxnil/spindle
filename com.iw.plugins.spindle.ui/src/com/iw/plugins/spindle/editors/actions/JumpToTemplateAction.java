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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.resources.eclipse.IEclipseResource;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

import core.resources.ICoreResource;
import core.spec.BaseSpecification;
import core.spec.PluginComponentSpecification;
import core.util.Assert;

/**
 * Jump from spec/template editors to associated java files
 * 
 * @author glongman@gmail.com
 */
public class JumpToTemplateAction extends BaseJumpAction
{
  /**
   *  
   */
  public JumpToTemplateAction()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.actions.BaseJumpAction#doRun()
   */
  protected void doRun()
  {
    BaseSpecification spec = getEditorSpecification();

    if (spec instanceof ILibrarySpecification)
      return;

    if (fEditor.getEditorInput() instanceof JarEntryEditorInput)
    {
      MessageDialog.openInformation(
          fEditor.getEditorSite().getShell(),
          "Operation Aborted",
          "Unable to Jump to Templates from  a jar based Specification");
      return;
    }

    List possibles = getTemplateLocations((PluginComponentSpecification) spec);
    if (possibles == null || possibles.isEmpty())
      return;
    if (possibles.size() > 1)
    {
      new ChooseTemplateLocationPopup(possibles, true).run();
    } else
    {
      reveal((ICoreResource) possibles.get(0));
    }

  }

  /**
   * @return
   */
  private BaseSpecification getEditorSpecification()
  {
    return (BaseSpecification) fEditor.getSpecification();
  }

  private List getTemplateLocations(PluginComponentSpecification spec)
  {
    List locations = spec.getTemplateLocations();

    if (locations.isEmpty() || !(fEditor instanceof TemplateEditor))
      return locations;

    IStorage currentStorage = fEditor.getStorage();
    List finalResult = new ArrayList();
    for (Iterator iter = locations.iterator(); iter.hasNext();)
    {
      IEclipseResource element = (IEclipseResource) iter.next();
      IStorage storage = element.getStorage();
      if (storage == null || storage.equals(currentStorage))
        continue;
      finalResult.add(element);
    }
    return finalResult;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.actions.BaseEditorAction#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
   */
  public void editorContextMenuAboutToShow(IMenuManager menu)
  {
    BaseSpecification spec = getEditorSpecification();
    if (spec != null && spec instanceof PluginComponentSpecification)
    {
      List possibles = getTemplateLocations((PluginComponentSpecification) spec);
      for (Iterator iter = possibles.iterator(); iter.hasNext();)
      {
        ICoreResource element = (ICoreResource) iter.next();
        Action openAction = new MenuOpenTemplateAction(element);
        openAction.setEnabled(element.exists());
        menu.add(openAction);
      }
    }

  }

  class MenuOpenTemplateAction extends Action
  {
    ICoreResource location;

    public MenuOpenTemplateAction(ICoreResource location)
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

  class ChooseTemplateLocationPopup extends ChooseLocationPopup
  {
    /**
     * @param templateLocations
     * @param forward
     */
    public ChooseTemplateLocationPopup(List templateLocations, boolean forward)
    {
      super(templateLocations, forward);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.editors.actions.JumpToTemplateAction.ChooseLocationPopup#getImage(core.resources.ICoreResource)
     */
    protected Image getImage(ICoreResource location)
    {
      return Images.getSharedImage("html16.gif");
    }

  }

}