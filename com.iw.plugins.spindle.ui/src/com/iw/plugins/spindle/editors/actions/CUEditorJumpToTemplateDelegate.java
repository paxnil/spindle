package com.iw.plugins.spindle.editors.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorActionDelegate;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.core.spec.BaseSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;

/**
 * TODO: Provide description for "CUEditorJumpToSpecDelegate".
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
      reveal((IResourceWorkspaceLocation) locations.get(0));
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
     * @see com.iw.plugins.spindle.editors.actions.JumpToTemplateAction.ChooseLocationPopup#getImage(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation)
     */
    protected Image getImage(IResourceWorkspaceLocation location)
    {
      return Images.getSharedImage("html16.gif");
    }

  }
}