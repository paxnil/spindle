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

package core.namespace;

import java.util.List;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IComponentSpecification;

import core.spec.lookup.ComponentLookup;
import core.spec.lookup.PageLookup;

/**
 * Extends org.apache.tapestry.INamespace to allow for the de-installation of
 * pages and components.
 * 
 * @author glongman@gmail.com
 * 
 */
public interface ICoreNamespace extends INamespace
{
  public void setParentNamespace(ICoreNamespace parent);

  public IComponentSpecification deinstallPageSpecification(String pageName);

  public IComponentSpecification deinstallComponentSpecification(String type);

  public void installChildNamespace(String id, INamespace child);

  public INamespace deinstallChildNamespace(String id);

  public ComponentLookup getComponentLookup(ICoreNamespace framework);

  public PageLookup getPageLookup(ICoreNamespace framework);

  /** set only if this is the application namespace* */
  public void setAppNameFromWebXML(String name);

  public void setResourceLookup(NamespaceResourceLookup lookup);

  public NamespaceResourceLookup getResourceLookup();

  public ComponentSpecificationResolver getComponentResolver();

  public void setComponentResolver(ComponentSpecificationResolver resolver);

  public PageSpecificationResolver getPageResolver();

  public void setPageResolver(PageSpecificationResolver resolver);
  
  public List getComponentTypes();

}