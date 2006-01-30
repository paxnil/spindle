package net.sf.spindle.core.namespace;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import java.util.List;

import net.sf.spindle.core.spec.lookup.ComponentLookup;
import net.sf.spindle.core.spec.lookup.PageLookup;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IComponentSpecification;



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