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
package com.iw.plugins.spindle.model;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedListener;

import com.iw.plugins.spindle.project.ITapestryProject;

public interface ITapestryModel extends IModel, IEditable {

  public IStorage getUnderlyingStorage();

  public void reload() throws CoreException;

  //  /** @deprecated */
  //  public String getDTDVersion();

  public ITapestryProject getProject() throws CoreException;

  public String getPublicId();

  public String toXML();

  public void setEditable(boolean flag);

  public void addModelChangedListener(IModelChangedListener listener);

  public void removeModelChangedListener(IModelChangedListener listener);


  public void setPublicId(String value);

}