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

package com.iw.plugins.spindle.core.builder;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.ICoreResource;

/**
 * Namespace resolver for libraries.
 * 
 * @author glongman@gmail.com
 * 
 */
public class LibraryResolver extends NamespaceResolver
{

  private ICoreNamespace fParentNamespace;
  private String fLibraryId;
  private ICoreResource fLibLocation;

  /**
   * @param build
   * @param parser
   */
  public LibraryResolver(AbstractBuild build, ICoreNamespace framework, ICoreNamespace parent,
      String libraryId, ICoreResource location)
  {
    super(build);
    fFrameworkNamespace = framework;
    fParentNamespace = parent;
    fLibraryId = libraryId;
    fLibLocation = location;
  }

  public ICoreNamespace resolve()
  {
    try
    {

      resolve(fLibraryId, fLibLocation);

      if (fParentNamespace != null && fResultNamespace != null)
        fParentNamespace.installChildNamespace(fLibraryId, fResultNamespace);

      return fResultNamespace;

    } finally
    {
      cleanup();
    }
  }

}