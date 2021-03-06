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
package com.iw.plugins.spindle.core.extensions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IType;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.SpindleStatus;

/**
 * ComponentTypeResourceResolvers
 * 
 * Container for all of the IComponentTypeResourceResolves contributed through
 * the extension point:
 * <ul>
 * <li>com.iw.plugins.spindle.core.componentTypeResolver - extension point
 * </li>
 * </ul>
 * <p>
 * if any exception is thrown during a contributions canResolve() or doResolve()
 * execution, an error is logged and the contribution is removed from the list.
 * 
 * @author glongman@gmail.com
 *  
 */
public class ComponentTypeResourceResolvers implements IComponentTypeResourceResolver
{
  public static final String EXTENSION_ID = TapestryCore.PLUGIN_ID
      + ".componentTypeResolver";

  private static List RESOLVERS;

  public static void clearResolvers()
  {
    RESOLVERS.clear();
  }

  private IComponentTypeResourceResolver fResolver;

  public ComponentTypeResourceResolvers()
  {
    init();
  }

  /**
   * Instantiate all of the contributed resource resolvers. Will log an error
   * and ignore the offending contribution if something bad happens.
   */
  private synchronized void init()
  {
    if (RESOLVERS != null)
      return;

    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint ep = reg.getExtensionPoint(EXTENSION_ID);
    IExtension[] extensions = ep.getExtensions();
    RESOLVERS = new ArrayList();
    for (int i = 0; i < extensions.length; i++)
    {
      IExtension ext = extensions[i];
      IConfigurationElement[] ce = ext.getConfigurationElements();
      for (int j = 0; j < ce.length; j++)
      {
        Object obj = null;
        try
        {
          obj = ce[j].createExecutableExtension("class");
          if (!(obj instanceof IComponentTypeResourceResolver))
          {
            TapestryCore
                .log("could not create contribution '"
                    + ext.getUniqueIdentifier()
                    + "'. class '"
                    + ce[j].getAttribute("class")
                    + "' does not implement com.iw.plugins.spindle.core.IComponentTypeResourceResolver");
            continue;
          }
        } catch (CoreException e)
        {
          TapestryCore.log("skipped contribution '" + ext.getUniqueIdentifier() + "'", e);
        }
        RESOLVERS.add(obj);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.extensions.IComponentTypeResourceResolver#canResolve(org.eclipse.jdt.core.IType)
   */
  public boolean canResolve(IType type)
  {
    fResolver = null;
    for (Iterator iter = RESOLVERS.iterator(); iter.hasNext();)
    {
      IComponentTypeResourceResolver candidate = (IComponentTypeResourceResolver) iter
          .next();
      try
      {
        if (candidate.canResolve(type))
        {
          fResolver = candidate;
          return true;
        }
      } catch (Throwable e)
      {
        TapestryCore.log("exception occured calling " + candidate.getClass().getName()
            + ".canResolve(). This resolver has been removed from the list", e);
        iter.remove();
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.extensions.IComponentTypeResourceResolver#doResolve(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation,
   *              org.apache.tapestry.spec.IComponentSpecification)
   */
  public IStatus doResolve(
      IResourceWorkspaceLocation specificationLocation,
      IComponentSpecification componentSpec)
  {
    Assert.isTrue(fResolver != null, "Error - call canResolve before doResolve()");
    try
    {
      return fResolver.doResolve(specificationLocation, componentSpec);
    } catch (Throwable e)
    {
      SpindleStatus status = new SpindleStatus();
      status.setError("Resolver exception occured, check the log");
      TapestryCore.log("exception occured calling " + fResolver.getClass().getName()
          + ".doResolve(). This resolver has been removed from the list", e);

      RESOLVERS.remove(fResolver);
      fResolver = null;
      return status;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.extensions.IComponentTypeResourceResolver#getStorage()
   */
  public IStorage getStorage()
  {
    IStorage storage = fResolver.getStorage();
    Assert.isTrue(storage != null, "Error - call doResolve before getStorage()");
    return storage;
  }

}