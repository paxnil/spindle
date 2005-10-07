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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.extensions.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import core.TapestryCore;



/**
 * IncrementalBuildVetoController TODO add something here
 * 
 * @author glongman@gmail.com
 *  
 */
public class IncrementalBuildVetoController 
{
  public static final String EXTENSION_ID = TapestryCore.IDENTIFIER
      + ".incrementalBuildVeto";

  private static List VETO_EXTENSIONS;

  private boolean fExecuteVeto;

  public IncrementalBuildVetoController()
  {
    init();
  }

  public boolean vetoIncrementalBuild(IResourceDelta projectDelta)
  {
    if (VETO_EXTENSIONS.isEmpty())
      return false;

    return checkVeto(projectDelta.getAffectedChildren());
  }

  private boolean checkVeto(IResourceDelta[] deltas)
  {
    for (int i = 0; i < deltas.length; i++)
    {
      IResource resource = deltas[i].getResource();
      if (resource.getType() != IResource.FILE)
        return checkVeto(deltas[i].getAffectedChildren());

      String fileExtension = resource.getFileExtension();

      if (!runtimeCheckExtension(resource.getName(), fileExtension))
        return false;

      switch (deltas[i].getKind())
      {
        case IResourceDelta.NO_CHANGE :
          continue;
        case IResourceDelta.CHANGED :
          if ((deltas[i].getFlags() & IResourceDelta.CONTENT | IResourceDelta.DESCRIPTION
              | IResourceDelta.MARKERS | IResourceDelta.DESCRIPTION) > 0)
            continue;
        case IResourceDelta.ADDED :
        case IResourceDelta.REMOVED :
        default :
          if (VETO_EXTENSIONS.contains(fileExtension))
            return true;
      }
    }
    return false;
  }
  private synchronized void init()
  {
    if (VETO_EXTENSIONS != null)
      return;
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint ep = reg.getExtensionPoint(EXTENSION_ID);
    IExtension[] extensions = ep.getExtensions();
    VETO_EXTENSIONS = new ArrayList();
    for (int i = 0; i < extensions.length; i++)
    {
      IExtension ext = extensions[i];
      IConfigurationElement[] ce = ext.getConfigurationElements();
      for (int j = 0; j < ce.length; j++)
      {
        String extension = null;

        extension = ce[j].getAttribute("extension");
        if (checkExtension(extension) && !VETO_EXTENSIONS.contains(extension))
          VETO_EXTENSIONS.add(extension);
      }
    }
  }

  private boolean runtimeCheckExtension(String name, String extension)
  {
    if ("web.xml".equals(name))
      return false;

    return checkExtension(extension);
  }
  /**
   * @param extension
   * @return
   */
  private boolean checkExtension(String extension)
  {
    if (extension == null || extension.trim().length() == 0)
      return false;

    if (extension.indexOf('.') != -1)
      return false;

    if ("application".equals(extension))
      return false;

    if ("library".equals(extension))
      return false;

    if ("page".equals(extension))
      return false;

    if ("jwc".equals(extension))
      return false;

    if ("html".equals(extension))
      return false;

    return true;
  }

}