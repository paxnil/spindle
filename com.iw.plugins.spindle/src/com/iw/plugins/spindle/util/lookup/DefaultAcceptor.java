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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaProject;

/**
 * @author gwl
 * @version $Id$
 *
 */
public class DefaultAcceptor implements ILookupAcceptor {

  /**
   * @see com.iw.plugins.spindle.util.lookup.ILookupAcceptor#acceptAsTapestry(IStorage, int)
   */
  public boolean acceptAsTapestry(IJavaProject jproject, IStorage s, int acceptFlags) {

    return defaultAcceptAsTapestry(jproject, s, acceptFlags);

  }

  protected final boolean defaultAcceptAsTapestry(IJavaProject jproject, IStorage s, int acceptFlags) {
    String extension = s.getFullPath().getFileExtension();
    //    int w = acceptFlags & WRITEABLE;
    //    int j = acceptFlags & ACCEPT_COMPONENTS;
    if ((acceptFlags & WRITEABLE) != 0 && s.isReadOnly()) {
      return false;
    }
    
    if ((s instanceof IResource) && (acceptFlags & THIS_PROJECT_ONLY) != 0) {
    	
    	if (!((IResource)s).getProject().equals(jproject.getProject())) {
    		
    		return false;
    	}

    }
    
    if ("jwc".equals(extension)) {
      return (acceptFlags & ACCEPT_COMPONENTS) != 0;
    }
    if ("application".equals(extension)) {
      return (acceptFlags & ACCEPT_APPLICATIONS) != 0;
    }
    if ("html".equals(extension)) {
      return (acceptFlags & ACCEPT_HTML) != 0;
    }
    if ("library".equals(extension)) {
    	return (acceptFlags & ACCEPT_LIBRARIES) != 0;
    }
    if ("page".equals(extension)) {
    	return (acceptFlags & ACCEPT_PAGES) != 0;
    }
    return false;
  }

}
