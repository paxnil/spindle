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
