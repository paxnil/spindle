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

package com.iw.plugins.spindle.core.classpath;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.iw.plugins.spindle.core.TapestryCore;

/**
 * Resolves a container for a Tapestry classpath container entry
 * 
 * @author glongman@gmail.com
 */
public class CoreClasspathContainerInitializer extends ClasspathContainerInitializer
{

  /**
   * @see ClasspathContainerInitializer#initialize(IPath, IJavaProject)
   */
  public void initialize(IPath containerPath, IJavaProject project) throws CoreException
  {
    int size = containerPath.segmentCount();
    if (size > 0)
    {
      if (containerPath.segment(0).equals(TapestryCore.CORE_CONTAINER))
      {
        CoreClasspathContainer container = new CoreClasspathContainer(containerPath);
        JavaCore.setClasspathContainer(
            containerPath,
            new IJavaProject[]{project},
            new IClasspathContainer[]{container},
            null);
      }
    }
  }

  /**
   * The container is never updated.
   * 
   * @see org.eclipse.jdt.core.ClasspathContainerInitializer#canUpdateClasspathContainer(org.eclipse.core.runtime.IPath,
   *      org.eclipse.jdt.core.IJavaProject)
   */
  public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project)
  {
    return false;
  }

  /**
   * @see org.eclipse.jdt.core.ClasspathContainerInitializer#requestClasspathContainerUpdate(org.eclipse.core.runtime.IPath,
   *      org.eclipse.jdt.core.IJavaProject,
   *      org.eclipse.jdt.core.IClasspathContainer)
   */
  public void requestClasspathContainerUpdate(
      IPath containerPath,
      IJavaProject project,
      IClasspathContainer containerSuggestion) throws CoreException
  {
    // do nothing, the container is never updated.
  }

  /**
   * @see org.eclipse.jdt.core.ClasspathContainerInitializer#getDescription(org.eclipse.core.runtime.IPath,
   *      org.eclipse.jdt.core.IJavaProject)
   */
  public String getDescription(IPath containerPath, IJavaProject project)
  {
    return "Tapestry Framework Container";
  }

}