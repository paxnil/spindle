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

package com.iw.plugins.spindle.editors.template;

import java.util.Map;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IFileEditorInput;

import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.editors.ProblemAnnotationModel;

/**
 * Model for Template annotations - of course only files and not jar entries
 * will have an annotation model.
 * 
 * @author glongman@gmail.com
 */
public class TemplateAnnotationModel extends ProblemAnnotationModel
{

  public TemplateAnnotationModel(IFileEditorInput input)
  {
    super(input);
  }

  // Must be a resource
  public void beginCollecting()
  {
    IComponentSpecification component = getComponent();
    if (component != null)
      setIsActive(true);
    else
      setIsActive(false);
  }

  private IComponentSpecification getComponent()
  {
    IFile file = fInput.getFile();

    IProject project = file.getProject();
    TapestryArtifactManager manager = TapestryArtifactManager
        .getTapestryArtifactManager();
    Map templates = manager.getTemplateMap(project);
    if (templates != null)
    {
      return (IComponentSpecification) templates.get(file);
    }
    return null;
  }

}