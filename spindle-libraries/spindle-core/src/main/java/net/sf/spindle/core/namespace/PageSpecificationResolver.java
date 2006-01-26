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

package net.sf.spindle.core.namespace;

import net.sf.spindle.core.util.Assert;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IComponentSpecification;

/**
 * Resolver for Component Specifications
 * 
 * @author glongman@gmail.com
 */

public class PageSpecificationResolver
{

  protected INamespace fFrameworkNamespace;
  protected INamespace fContainerNamespace;

  public PageSpecificationResolver(INamespace framework, INamespace containerNamespace)
  {
    fFrameworkNamespace = framework;
    Assert.isNotNull(containerNamespace);
    fContainerNamespace = containerNamespace;
  }

  public IComponentSpecification resolve(String pageName)
  {
    int colonx = pageName.indexOf(':');

    if (colonx > 0)
    {
      String libraryId = pageName.substring(0, colonx);
      String simpleType = pageName.substring(colonx + 1);

      return resolve(libraryId, simpleType);
    } else
      return resolve(null, pageName);
  }

  public IComponentSpecification resolve(String libraryId, String pageName)
  {
    INamespace namespace = null;

    if (libraryId != null && !libraryId.equals(fContainerNamespace.getId()))
      namespace = fContainerNamespace.getChildNamespace(libraryId);
    else
      namespace = fContainerNamespace;

    if (namespace == null)
//      namespace = findApplicationNamespace(); FIXME waiting for howards readon things
//    
//    if (namespace == null)
      	return null;

    if (namespace.containsPage(pageName))
    {
      return namespace.getPageSpecification(pageName);
    }

    if (libraryId == null)
      return resolveInFramework(pageName);

    return null;
  }

  protected IComponentSpecification resolveInFramework(String pageName)
  {
    if (fFrameworkNamespace != null && fFrameworkNamespace.containsPage(pageName))
    {

      return fFrameworkNamespace.getPageSpecification(pageName);
    }
    return null;
  }

  protected INamespace findApplicationNamespace()
  {
    //quick check
    INamespace parent = fContainerNamespace.getParentNamespace();
    if (parent == null)
      return null;

    if (parent.isApplicationNamespace())
      return parent;

    while (parent != null && !parent.isApplicationNamespace())
      parent = parent.getParentNamespace();

    return parent;

  }

}