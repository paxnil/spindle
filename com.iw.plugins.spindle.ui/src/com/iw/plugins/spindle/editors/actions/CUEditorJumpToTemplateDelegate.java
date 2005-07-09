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

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorActionDelegate;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.core.spec.BaseSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;

/**
 * Action to Jump from a java file editor to a related tapestry template (if one exists)
 * 
 * @see IEditorActionDelegate
 */
public class CUEditorJumpToTemplateDelegate extends CUEditorJumpToSpecDelegate
    implements
      IEditorActionDelegate
{

  public CUEditorJumpToTemplateDelegate()
  {
  }

  protected void doRun()
  {
    List foundSpecs = TapestryArtifactManager
        .getTapestryArtifactManager()
        .findTypeRefences(fProject, fType.getFullyQualifiedName());
    if (foundSpecs.isEmpty())
      return;

    if (foundSpecs.size() == 1)
    {
      BaseSpecLocatable locatable = (BaseSpecLocatable) foundSpecs.get(0);
      if (locatable.getSpecificationType() != BaseSpecification.COMPONENT_SPEC)
        return;
      PluginComponentSpecification spec = (PluginComponentSpecification) locatable;
      revealTemplates(spec.getTemplateLocations());

    } else
    {
      List locations = new ArrayList();
      for (Iterator iter = foundSpecs.iterator(); iter.hasNext();)
      {
        BaseSpecLocatable element = (BaseSpecLocatable) iter.next();
        if (element.getSpecificationType() != BaseSpecification.COMPONENT_SPEC)
          continue;
        PluginComponentSpecification spec = (PluginComponentSpecification) element;
        locations.addAll(spec.getTemplateLocations());

      }
      revealTemplates(locations);
    }
  }

  /**
   * @param list
   */
  private void revealTemplates(List locations)
  {
    if (locations == null || locations.isEmpty())
      return;

    if (locations.size() == 1)
    {
      reveal((ICoreResource) locations.get(0));
    } else
    {
      new ChooseTemplateLocationPopup(locations, true).run();
    }
  }

  protected class ChooseTemplateLocationPopup extends ChooseLocationPopup
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
     * @see com.iw.plugins.spindle.editors.actions.JumpToTemplateAction.ChooseLocationPopup#getImage(com.iw.plugins.spindle.core.resources.ICoreResource)
     */
    protected Image getImage(ICoreResource location)
    {
      return Images.getSharedImage("html16.gif");
    }

  }
}