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



package com.iw.plugins.spindle.project;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface ITapestryProject {

  public TapestryLookup getLookup() throws CoreException;

  public TapestryProjectModelManager getModelManager() throws CoreException;

  public void setProjectStorage(IStorage file) throws CoreException;

  public IStorage getProjectStorage() throws CoreException;

  public ITapestryModel getProjectModel() throws CoreException;

  public String findFrameworkComponentPath(String alias) throws CoreException;

  public String findFrameworkPagePath(String alias) throws CoreException;

  public TapestryLibraryModel getDefaultLibraryModel() throws CoreException;

  public ITapestryModel findModelByPath(String specificationPath) throws CoreException;

  public ITapestryModel findModelByPath(String specificationPath, int acceptFlags)
    throws CoreException;
    

}
