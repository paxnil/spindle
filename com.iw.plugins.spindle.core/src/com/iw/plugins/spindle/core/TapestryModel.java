package com.iw.plugins.spindle.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class TapestryModel extends Openable implements ITapestryModel {

  protected TapestryModel() throws Error {
    super(TAPESTRY_MODEL, null, "" /*workspace has empty name*/
    );
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryModel#getTapestryProject(String)
   */
  public ITapestryProject getTapestryProject(String name) {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryModel#getTapestryProjects()
   */
  public ITapestryProject[] getTapestryProjects() throws TapestryModelException {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryModel#getWorkspace()
   */
  public IWorkspace getWorkspace() {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#exists()
   */
  public boolean exists() {
    return false;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getAncestor(int)
   */
  public ITapestryArtifact getAncestor(int ancestorType) {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getCorrespondingResource()
   */
  public IResource getCorrespondingResource() throws TapestryModelException {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getElementName()
   */
  public String getArtifactName() {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getElementType()
   */
  public int getArtifactType() {
    return 0;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getHandleIdentifier()
   */
  public String getHandleIdentifier() {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getTapestryModel()
   */
  public ITapestryModel getTapestryModel() {
    return this;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getTapestryProject()
   */
  public ITapestryProject getTapestryProject() {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getOpenable()
   */
  public IOpenable getOpenable() {
    return this;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getParent()
   */
  public ITapestryArtifact getParent() {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getPath()
   */
  public IPath getPath() {
    return Path.ROOT;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getStorage()
   */
  public IStorage getStorage() {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#getUnderlyingStorage()
   */
  public IStorage getUnderlyingStorage() throws TapestryModelException {
    return null;
  }


  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#isStructureKnown()
   */
  public boolean isStructureKnown() throws TapestryModelException {
    return false;
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#close()
   */
  public void close() throws TapestryModelException {
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#hasUnsavedChanges()
   */
  public boolean hasUnsavedChanges() throws TapestryModelException {
    return false;
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#isConsistent()
   */
  public boolean isConsistent() throws TapestryModelException {
    return false;
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#isOpen()
   */
  public boolean isOpen() {
    return false;
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#open(IProgressMonitor)
   */
  public void open(IProgressMonitor progress) throws TapestryModelException {
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#save(IProgressMonitor, boolean)
   */
  public void save(IProgressMonitor progress, boolean force) throws TapestryModelException {
  }

  /**
   * @see com.iw.plugins.spindle.core.IParent#getChildren()
   */
  public ITapestryArtifact[] getChildren() throws TapestryModelException {
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.core.IParent#hasChildren()
   */
  public boolean hasChildren() throws TapestryModelException {
    return false;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
   */
  public Object getAdapter(Class adapter) {
    return null;
  }

  /**
   * Method getTapestryProject.
   * @param project
   * @return ITapestryProject
   */
  public ITapestryProject getTapestryProject(IProject project) {
    return null;
  }

}
