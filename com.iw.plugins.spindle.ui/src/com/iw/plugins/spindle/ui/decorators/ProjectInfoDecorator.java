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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.ui.decorators;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.Markers;

/**
 * Decorator that indicates extra info about Tapestry projects
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: ProjectInfoDecorator.java,v 1.2 2004/04/28 16:53:41 glongman
 *          Exp $
 */
public class ProjectInfoDecorator extends AbstractDecorator
    implements
      ILightweightLabelDecorator
{

  public ProjectInfoDecorator()
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
    IProject project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
    if (project == null || !project.isOpen())
      return;

    try
    {
      TapestryProject tproject = (TapestryProject) project
          .getNature(TapestryCore.NATURE_ID);
      if (tproject != null)
      {
        decoration.addSuffix(getDecorationSuffix(project));
      }
    } catch (CoreException e)
    {
      e.printStackTrace();
    }

  }

  /**
   * @param project
   * @return
   */
  private String getDecorationSuffix(IProject project)
  {
    IMarker[] markers = Markers.getBrokenBuildProblemsFor(project);
    String suffix;
    if (markers.length != 0)
    {
      suffix = UIPlugin.getString("project-decorator-broken-build");
    } else
    {
      Object state = TapestryArtifactManager
          .getTapestryArtifactManager()
          .getLastBuildState(project, false);
      if (state == null)
      {
        suffix = UIPlugin.getString("project-decorator-needs-build");
      } else
      {

        INamespace namespace = (INamespace) TapestryArtifactManager
            .getTapestryArtifactManager()
            .getProjectNamespace(project, false);
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) namespace
            .getSpecificationLocation();
        if (location == null || location.getStorage() == null)
        {
          return "";
        } else
        {
          suffix = location.getName();
          if (suffix.length() == 0)
          {
            String appname = ((IApplicationSpecification) namespace.getSpecification())
                .getName();

            if (appname == null || appname.trim().length() == 0)
              appname = "unknown";

            suffix = UIPlugin.getString(
                "project-decorator-standin-specification",
                appname);
          }
        }
      }
    }
    return " : [" + suffix + "]";
  }

  private boolean isTapestryProject(Object element)
  {
    try
    {
      IProject project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
      return project.hasNature(TapestryCore.NATURE_ID);
    } catch (CoreException e)
    {} catch (ClassCastException e)
    {}
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener)
  {
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

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void removeListener(ILabelProviderListener listener)
  {
  }

}