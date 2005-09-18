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

package com.iw.plugins.spindle.core.spec.lookup;

import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;

/**
 * Lookup class for PageSpecifications
 * 
 * @author glongman@gmail.com
 * 
 */
public class PageLookup extends AbstractLookup
{
  protected IComponentSpecification lookupSpecification(String libraryId, String pageName)
  {
    ICoreNamespace useNamespace = getNamespace();

    IComponentSpecification result = null;

    if (libraryId != null)
      useNamespace = (ICoreNamespace) useNamespace.getChildNamespace(libraryId);

    result = useNamespace.getPageSpecification(pageName);

    if (result == null && libraryId == null)
    {
      useNamespace = getFrameworkNamespace();
      if (useNamespace != null)
        return getFrameworkNamespace().getPageSpecification(pageName);

    }
    return result;
  }
}