package com.iw.plugins.spindle.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstract class for implementations of tapestry artifacts that are IOpenable.
 *
 * @see ITapestryArtifact
 * @see IOpenable
 */
public abstract class Openable extends TapestryArtifact implements IOpenable {

  
  public Openable(int type, ITapestryArtifact parent, String name) {
    super(type, parent, name);
  }

  /*
  * @see ITapestryArtifact
  */
  public IOpenable getOpenable() {
    return this;
  }

  /**
  * Returns a new artifact info for this artifact.
  */
  protected OpenableArtifactInfo createArtifactInfo() {
    return new OpenableArtifactInfo();
  }

  /*
  * @see ITapestryArtifact
  */
  public IResource getCorrespondingResource() throws TapestryModelException {
    IResource parentResource = parent.getCorrespondingResource();
    if (parentResource == null) {
      return null;
    }
    int type = parentResource.getType();
    if (type == IResource.FOLDER || type == IResource.PROJECT) {
      IContainer folder = (IContainer) parentResource;
      IResource resource = folder.findMember(name);
      if (resource == null) {
        throw newNotPresentException();
      } else {
        return resource;
      }
    } else {
      return parentResource;
    }
  }

  /**
  * @see IParent 
  */
  public boolean hasChildren() throws TapestryModelException {
    return getChildren().length > 0;
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#hasUnsavedChanges()
   */
  public boolean hasUnsavedChanges() throws TapestryModelException {
    if (isReadOnly() || !isOpen()) {
      return false;
    }
    return false;
  }

  /**
   * override in subclasses?
   * 
   * @see com.iw.plugins.spindle.core.IOpenable#isConsistent()
   */
  public boolean isConsistent() throws TapestryModelException {
    return true;
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#isOpen()
   */
  public boolean isOpen() {
    synchronized (TapestryModelManager.getTapestryModelManager()) {
      return TapestryModelManager.getTapestryModelManager().getInfo(this) != null;
    }
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#open(IProgressMonitor)
   */
  public void open(IProgressMonitor progress) throws TapestryModelException {
    if (!isOpen()) {
      this.openWhenClosed(progress);
    }
  }

  /**
   * Open an <code>Openable</code> that is known to be closed (no check for <code>isOpen()</code>).
   * 
   * Currently everything is open by default. Just ensure the parent is also open.
   */
  protected void openWhenClosed(IProgressMonitor pm) throws TapestryModelException {

    if (TapestryModelManager.VERBOSE) {
      System.out.println("OPENING Artifact (" + Thread.currentThread() + "): " + this); //.toStringWithAncestors()); //$NON-NLS-1$//$NON-NLS-2$
    }

    // 1) Parent must be open - open the parent if necessary
    openParent(pm);

  }

  /**
  * 	Open the parent artifact if necessary
  * 
  */
  protected void openParent(IProgressMonitor pm) throws TapestryModelException {

    Openable openableParent = (Openable) getOpenableParent();
    if (openableParent != null) {
      if (!openableParent.isOpen()) {
        openableParent.openWhenClosed(pm);
      }
    }
  }

  /**
   * @see com.iw.plugins.spindle.core.IOpenable#save(IProgressMonitor, boolean)
   */
  public void save(IProgressMonitor progress, boolean force) throws TapestryModelException {
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#isStructureKnown()
   */
  public boolean isStructureKnown() throws TapestryModelException {
    return getArtifactInfo().isStructureKnown();
  }

}
