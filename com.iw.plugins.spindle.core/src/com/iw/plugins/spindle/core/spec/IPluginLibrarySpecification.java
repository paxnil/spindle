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
package com.iw.plugins.spindle.core.spec;

import java.beans.PropertyChangeListener;
import java.util.Set;

import org.apache.tapestry.spec.ExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface IPluginLibrarySpecification
  extends PropertyChangeListener, ILibrarySpecification, IIdentifiable {

  public void removePageSpecificationPath(String name);

  public void removeComponentSpecificationPath(String name);

  public void removeLibrarySpecificationPath(String name);

  public void removeExtensionSpecification(String name);

  public void setExtensionSpecification(String name, ExtensionSpecification extension);

  public boolean canDeleteService(String name);

  public boolean canRevertService(String name);

  public Set getAllExtensionNames();

}
