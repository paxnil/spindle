package com.iw.plugins.spindle.core;

import java.util.ArrayList;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;

/**
 * Root of Tapestry artifact handle hierarchy.
 *
 * @see ITapestryArtifact
 */
public abstract class TapestryArtifact extends PlatformObject implements ITapestryArtifact {

  /**
  * A count to uniquely identify this artifact in the case
  * that a duplicate named artifact exists. For example, if
  * there are two fields in a compilation unit with the
  * same name, the occurrence count is used to distinguish
  * them.  The occurrence count starts at 1 (i.e. the first 
  * occurrence is occurrence 1, not occurrence 0).
  */
  protected int occurrenceCount = 1;

  /**
   * This artifact's type - one of the constants defined
   * in ITapestryArtifact.
   */
  protected int type = 0;

  /**
   * This artifact's parent, or <code>null</code> if this
   * artifact does not have a parent.
   */
  protected ITapestryArtifact parent;

  /**
   * This artifact's name, or an empty <code>String</code> if this
   * artifact does not have a name.
   */
  protected String name;

  /**
   * Constructs a handle for a Tapestry artifact of the specified type, with
   * the given parent element and name.
   *
   * @param type - one of the constants defined in ITapestryArtifact
   *
   * @exception IllegalArgumentException if the type is not one of the valid
   *		Tapestry artifact type constants
   *
   */
  protected TapestryArtifact(int type, ITapestryArtifact parent, String name)
    throws IllegalArgumentException {
    if (type < TAPESTRY_MODEL || type > TAPESTRY_SCRIPT) {
      throw new IllegalArgumentException("invalid type");
    }
    this.type = type;
    this.parent = parent;
    this.name = name;
  }

  /**
  * @see IOpenable
  */
  public void close() throws TapestryModelException {
    Object info = TapestryModelManager.getTapestryModelManager().peekAtInfo(this);
    if (info != null) {
      if (TapestryModelManager.VERBOSE && this instanceof ITapestryModel) {
        System.out.println("CLOSING Tapestry Model"); //$NON-NLS-1$

        TapestryModelManager.VERBOSE = false;
      }
      if (this instanceof IParent) {
        ITapestryArtifact[] children = ((TapestryArtifactInfo) info).getChildren();
        for (int i = 0, size = children.length; i < size; ++i) {
          TapestryArtifact child = (TapestryArtifact) children[i];
          child.close();
        }
      }
      closing(info);
      TapestryModelManager.getTapestryModelManager().removeInfo(this);
    }
  }

  /**
  * This artifact is being closed.  Do any necessary cleanup.
  */
  protected void closing(Object info) throws TapestryModelException {
    if (TapestryModelManager.VERBOSE) {
      System.out.println("CLOSING Artifact (" + Thread.currentThread() + "): " + this); //this.toStringWithAncestors()); //$NON-NLS-1$//$NON-NLS-2$
    }
  }

  public boolean exists() {

    try {
      getRawInfo();
      return true;
    } catch (TapestryModelException e) {
    }
    return false;
  }

  /**
   * Returns the info for this handle.  
   * If this element is not already open, it and all of its parents are opened.
   * Does not return null.
   *
   * @exception JavaModelException if the artifact is not present or not accessible
   */
  public Object getRawInfo() throws TapestryModelException {
    synchronized (TapestryModelManager.getTapestryModelManager()) {
      Object info = TapestryModelManager.getTapestryModelManager().getInfo(this);
      if (info == null) {
        openHierarchy();
        info = TapestryModelManager.getTapestryModelManager().getInfo(this);
        if (info == null) {
          throw newNotPresentException();
        }
      }
      return info;
    }
  }

  /**
   * Creates and returns and not present exception for this artifact.
   */
  protected TapestryModelException newNotPresentException() {
    return new TapestryModelException(
      new Status(IStatus.ERROR, TapestryCore.PLUGIN_ID, 0, "not present", null));
  }

  public ITapestryArtifact getAncestor(int ancestorType) {

    ITapestryArtifact artifact = this;
    while (artifact != null) {
      if (artifact.getArtifactType() == ancestorType)
        return artifact;
      artifact = artifact.getParent();
    }
    return null;
  }

  public String getArtifactName() {
    return name;
  }

  public int getArtifactType() {
    return type;
  }

  /**
  * Returns the info for this handle.  
  * If this element is not already open, it and all of its parents are opened.
  * Does not return null.
  *
  * @exception JavaModelException if the element is not present or not accessible
  */
  public TapestryArtifactInfo getArtifactInfo() throws TapestryModelException {
    TapestryModelManager manager;
    synchronized (manager = TapestryModelManager.getTapestryModelManager()) {
      Object info = manager.getInfo(this);
      if (info == null) {
        openHierarchy();
        info = manager.getInfo(this);
        if (info == null) {
          throw newNotPresentException();
        }
      }
      return (TapestryArtifactInfo) info;
    }
  }

  /**
  * Opens this artifact and all parents that are not already open.
  *
  * @exception JavaModelException this element is not present or accessible
  */
  protected void openHierarchy() throws TapestryModelException {
    if (this instanceof IOpenable) {
      ((Openable) this).openWhenClosed(null);
    } else {
      Openable openableParent = (Openable) getOpenableParent();
      if (openableParent != null) {
        TapestryArtifactInfo openableParentInfo =
          (TapestryArtifactInfo) TapestryModelManager.getTapestryModelManager().getInfo(
            (TapestryArtifact) openableParent);
        if (openableParentInfo == null) {
          openableParent.openWhenClosed(null);
        } else {
          throw newNotPresentException();
        }
      }
    }
  }

  public ITapestryModel getTapestryModel() {
    return null;
  }

  public ITapestryProject getTapestryProject() {
    return getParent().getTapestryProject();
  }

  public IOpenable getOpenable() {
    return this.getOpenableParent();
  }

  /**
  * Return the first instance of IOpenable in the parent
  * hierarchy of this artifact.
  *
  * <p>Subclasses that are not IOpenable's must override this method.
  */
  public IOpenable getOpenableParent() {

    return (IOpenable) parent;
  }

  public ITapestryArtifact getParent() {
    return parent;
  }

  /**
  * Returns the workspace associated with this object.
  */
  public IWorkspace getWorkspace() {
    return getTapestryModel().getWorkspace();
  }

  /**
   * Returns the hash code for this Tapestry artifact. By default,
   * the hash code for an artifact is a combination of its name
   * and parent's hash code. Artifacts with other requirements must
   * override this method.
   */
  public int hashCode() {
    if (parent == null)
      return super.hashCode();
    return name.hashCode() * 17 + parent.hashCode();
  }

  /**
  * Returns true if this handle represents the same Tapestry artifact
  * as the given handle. By default, two handles represent the same
  * element if they are identical or if they represent the same type
  * of artifact, have equal names, parents, and occurrence counts.
  *
  * <p>If a subclass has other requirements for equality, this method
  * must be overridden.
  *
  * @see Object#equals
  */
  public boolean equals(Object o) {

    if (this == o)
      return true;

    // Tapestry model parent is null
    if (parent == null)
      return super.equals(o);

    if (o instanceof TapestryArtifact) {
      TapestryArtifact other = (TapestryArtifact) o;
      if (type != other.type)
        return false;

      return name.equals(other.name)
        && parent.equals(other.parent)
        && occurrenceCount == other.occurrenceCount;
    }
    return false;
  }

  /**
  * @see IParent 
  */
  public ITapestryArtifact[] getChildren() throws TapestryModelException {
    return getArtifactInfo().getChildren();
  }

  /**
  * Returns a collection of (immediate) children of this artifact of the
  * specified type.
  *
  * @param type - one of constants defined by ITapestryArtifact
  */
  public ArrayList getChildrenOfType(int type) throws TapestryModelException {
    ITapestryArtifact[] children = getChildren();
    int size = children.length;
    ArrayList list = new ArrayList(size);
    for (int i = 0; i < size; ++i) {
      TapestryArtifact elt = (TapestryArtifact) children[i];
      if (elt.getArtifactType() == type) {
        list.add(elt);
      }
    }
    return list;
  }

  /**
  * Sets the occurrence count of the handle.
  */
  protected void setOccurrenceCount(int count) {
    occurrenceCount = count;
  }
  /**
  * Returns the occurrence count of the handle.
  */
  protected int getOccurrenceCount() {
    return occurrenceCount;
  }

  /**
   * @see com.iw.plugins.spindle.core.ITapestryArtifact#isReadOnly()
   */
  public boolean isReadOnly() {
    return false;
  }

  public boolean isStructureKnown() throws TapestryModelException {
    return getArtifactInfo().isStructureKnown();
  }

}
