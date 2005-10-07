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

package com.iw.plugins.spindle.ui.decorators;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;


import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;

import core.ICoreListener;
import core.ITapestryProject;

/**
 * Decorator that indicates Tapestry projects
 * 
 * @author glongman@gmail.com
 */
public class ContextRootDecorator extends AbstractDecorator
    implements
      ILightweightLabelDecorator,
      ICoreListener
{

  public ContextRootDecorator()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
   *      org.eclipse.jface.viewers.IDecoration)
   */
  public void decorate(Object element, IDecoration decoration)
  {

    if (decoration == null)
    {
      UIPlugin.log("ContextRootDecorator.decorate() called with null decoration");
      return;
    }

    IContainer container = (IContainer) ((IAdaptable) element)
        .getAdapter(IContainer.class);
    if (container != null)
    {
      try
      {
        IProject project = container.getProject();
        ITapestryProject tproject = (ITapestryProject) project
            .getNature(TapestryCorePlugin.NATURE_ID);
        if (tproject != null)
        {
          IContainer projectWebContextRoot = tproject.getWebContextFolder();
          if (container.equals(projectWebContextRoot))
          {
            decoration.addOverlay(Images.getImageDescriptor("project_ovr.gif"));
          } else if (projectWebContextRoot != null
              && onContextPath(container, projectWebContextRoot))
          {
            decoration.addOverlay(Images.getImageDescriptor("project_ovr_grey.gif"));
          }
        }
      } catch (CoreException e)
      {}
    }

  }

  /**
   * @param container
   * @param projectWebContextRoot
   * @return
   */
  private boolean onContextPath(IContainer container, IContainer projectWebContextRoot)
  {

    try
    {
      IPath containerPath = container.getFullPath();
      IPath contextPath = projectWebContextRoot.getFullPath();
      int containerLength = containerPath.segmentCount();
      int contextLength = contextPath.segmentCount();
      if (containerLength < contextLength)
      {
        IContainer parent = projectWebContextRoot.getParent();
        do
        {
          if (parent.equals(container))
            return true;

          parent = parent.getParent();
        } while (parent != null);
      }
    } catch (RuntimeException e)
    {
      return false;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose()
  {
    // no need, the image is managed by the plugin!
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
   *      java.lang.String)
   */
  public boolean isLabelProperty(Object element, String property)
  {
    return true;
  }

}