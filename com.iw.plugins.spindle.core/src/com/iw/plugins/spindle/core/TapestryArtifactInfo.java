package com.iw.plugins.spindle.core;



/**
 * Holds cached structure and properties for a Tapestry artifact.
 * Subclassed to carry properties for specific kinds of artifacts.
 */
/* package */
class TapestryArtifactInfo {

  /**
   * Collection of handles of immediate children of this
   * object. This is an empty array if this object has
   * no children.
   */
  protected ITapestryArtifact[] children;
  
  protected ITapestryArtifact parent;

  /**
   * Shared empty collection used for efficiency.
   */
  protected static ITapestryArtifact[] EmptyChildren = new ITapestryArtifact[] {
  };
  /**
   * Is the structure of this element known
   */
  protected boolean structureKnown = false;

  protected TapestryArtifactInfo() {
    children = EmptyChildren;
  }
  public void addChild(ITapestryArtifact child) {
    if (children == EmptyChildren) {
      setChildren(new ITapestryArtifact[] { child });
    } else {
      if (!includesChild(child)) {
        setChildren(growAndAddToArray(children, child));
      }
    }
  }
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error();
    }
  }
  public ITapestryArtifact[] getChildren() {
    return children;
  }
  /**
   * Adds the new element to a new array that contains all of the elements of the old array.
   * Returns the new array.
   */
  protected ITapestryArtifact[] growAndAddToArray(
    ITapestryArtifact[] array,
    ITapestryArtifact addition) {
    ITapestryArtifact[] old = array;
    array = new ITapestryArtifact[old.length + 1];
    System.arraycopy(old, 0, array, 0, old.length);
    array[old.length] = addition;
    return array;
  }
  /**
   * Returns <code>true</code> if this child is in my children collection
   */
  protected boolean includesChild(ITapestryArtifact child) {

    for (int i = 0; i < children.length; i++) {
      if (children[i].equals(child)) {
        return true;
      }
    }
    return false;
  }
  /**
   * @see IJavaElement#isStructureKnown()
   */
  public boolean isStructureKnown() {
    return structureKnown;
  }
  /**
   * Returns an array with all the same elements as the specified array except for
   * the element to remove. Assumes that the deletion is contained in the array.
   */
  protected ITapestryArtifact[] removeAndShrinkArray(
    ITapestryArtifact[] array,
    ITapestryArtifact deletion) {
    ITapestryArtifact[] old = array;
    array = new ITapestryArtifact[old.length - 1];
    int j = 0;
    for (int i = 0; i < old.length; i++) {
      if (!old[i].equals(deletion)) {
        array[j] = old[i];
      } else {
        System.arraycopy(old, i + 1, array, j, old.length - (i + 1));
        return array;
      }
      j++;
    }
    return array;
  }
  public void removeChild(ITapestryArtifact child) {
    if (includesChild(child)) {
      setChildren(removeAndShrinkArray(children, child));
    }
  }
  public void setChildren(ITapestryArtifact[] children) {
    children = children;
  }
  /**
   * Sets whether the structure of this element known
   * @see IJavaElement#isStructureKnown()
   */
  public void setIsStructureKnown(boolean newIsStructureKnown) {
    structureKnown = newIsStructureKnown;
  }

}