package com.iw.plugins.spindle.change;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MoveDeleteManager implements IMoveDeleteHook {

  /**
   * Constructor for MoveDeleteManager.
   */
  public MoveDeleteManager() {
    super();
  }

  /**
   * @see org.eclipse.core.resources.team.IMoveDeleteHook#deleteFile(IResourceTree, IFile, int, IProgressMonitor)
   */
  public boolean deleteFile(
    IResourceTree tree,
    IFile file,
    int updateFlags,
    IProgressMonitor monitor) {
    return false;
  }

  /**
   * @see org.eclipse.core.resources.team.IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
   */
  public boolean deleteFolder(
    IResourceTree tree,
    IFolder folder,
    int updateFlags,
    IProgressMonitor monitor) {
    return false;
  }

  /**
   * @see org.eclipse.core.resources.team.IMoveDeleteHook#deleteProject(IResourceTree, IProject, int, IProgressMonitor)
   */
  public boolean deleteProject(
    IResourceTree tree,
    IProject project,
    int updateFlags,
    IProgressMonitor monitor) {
    return false;
  }

  /**
   * @see org.eclipse.core.resources.team.IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int, IProgressMonitor)
   */
  public boolean moveFile(
    IResourceTree tree,
    IFile source,
    IFile destination,
    int updateFlags,
    IProgressMonitor monitor) { 
    	System.out.println("Move called");
    	System.out.println("source: "+source);
    	System.out.println("destination: "+destination);
    return false;
  }

  /**
   * @see org.eclipse.core.resources.team.IMoveDeleteHook#moveFolder(IResourceTree, IFolder, IFolder, int, IProgressMonitor)
   */
  public boolean moveFolder(
    IResourceTree tree,
    IFolder source,
    IFolder destination,
    int updateFlags,
    IProgressMonitor monitor) {
    return false;
  }

  /**
   * @see org.eclipse.core.resources.team.IMoveDeleteHook#moveProject(IResourceTree, IProject, IProjectDescription, int, IProgressMonitor)
   */
  public boolean moveProject(
    IResourceTree tree,
    IProject source,
    IProjectDescription description,
    int updateFlags,
    IProgressMonitor monitor) {
    return false;
  }

}
