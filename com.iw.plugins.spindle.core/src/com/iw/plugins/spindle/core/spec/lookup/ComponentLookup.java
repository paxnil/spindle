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

package com.iw.plugins.spindle.core.spec.lookup;

import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;

/**
 * Lookup class for ComponentSpecifications
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ComponentLookup extends AbstractLookup
{
  public IComponentSpecification lookupSpecification(String type)
  {
    int colonx = type.indexOf(':');

    if (colonx > 0)
    {
      String libraryId = type.substring(0, colonx);
      String simpleType = type.substring(colonx + 1);
      return lookupSpecification(libraryId, simpleType);
    } else
    {
      return lookupSpecification(null, type);
    }
  }

  protected IComponentSpecification lookupSpecification(String libraryId, String type)
  {
    ICoreNamespace useNamespace = getNamespace();

    IComponentSpecification result = null;

    if (libraryId != null)
      useNamespace = (ICoreNamespace) useNamespace.getChildNamespace(libraryId);

    result = useNamespace.getComponentSpecification(type);

    if (result == null && libraryId == null)
    {
      useNamespace = getFrameworkNamespace();
      if (useNamespace != null)
        return getFrameworkNamespace().getComponentSpecification(type);

    }
    return result;
  }
}