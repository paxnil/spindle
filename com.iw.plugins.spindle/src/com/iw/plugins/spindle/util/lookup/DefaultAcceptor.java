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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
 package com.iw.plugins.spindle.util.lookup;

import org.eclipse.core.resources.IStorage;

/**
 * @author gwl
 * @version $Id$
 *
 */
public class DefaultAcceptor implements ILookupAcceptor {
 /**
  * Accept flag for specifying components.
  */
  public static final int ACCEPT_COMPONENTS = 0x00000001;
  /**
   * Accept flag for specifying application.
   */
  public static final int ACCEPT_APPLICATIONS = 0x00000002;
  /**
   *  Accept flag for specifying the search name includes Tapestry path!
   */
  public static final int FULL_TAPESTRY_PATH = 0x00000004;
  /**
   *  Accept flag for specifying HTML files
   */
  public static final int ACCEPT_HTML = 0x00000008;
  /**
   * Accept flag for writeable (non read only) files;
   */
  public static final int WRITEABLE = 0x00000010;
  
  /**
   * @see com.iw.plugins.spindle.util.lookup.ILookupAcceptor#acceptAsTapestry(IStorage, int)
   */
  public boolean acceptAsTapestry(IStorage s, int acceptFlags) {
    String extension = s.getFullPath().getFileExtension();
    int w = acceptFlags & WRITEABLE;
    int j = acceptFlags & ACCEPT_COMPONENTS;
    if ((acceptFlags & WRITEABLE | acceptFlags & TapestryLookup.WRITEABLE) != 0 && s.isReadOnly()) {
      return false;
    }
    if ("jwc".equals(extension)) {
      return (acceptFlags & ACCEPT_COMPONENTS | acceptFlags & TapestryLookup.ACCEPT_COMPONENTS) != 0;
    }
    if ("application".equals(extension)) {
      return (acceptFlags & ACCEPT_APPLICATIONS | acceptFlags & TapestryLookup.ACCEPT_APPLICATIONS) != 0;
    }
    if ("html".equals(extension)) {
      return (acceptFlags & ACCEPT_HTML | acceptFlags & TapestryLookup.ACCEPT_HTML) != 0;
    }
    return false;
  }

}
