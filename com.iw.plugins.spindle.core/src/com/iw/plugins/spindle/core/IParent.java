package com.iw.plugins.spindle.core;

/**
 * Common protocol for Tapestry artifacts that contain other Tapestry artifacts.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IParent {
  /**
   * Returns the immediate children of this artifact.
   * Unless otherwise specified by the implementing artifact,
   * the children are in no particular order.
   *
   * @exception TapestryModelException if this element does not exist or if an
   *      exception occurs while accessing its corresponding resource
   * @return the immediate children of this element
   */
  ITapestryArtifact[] getChildren() throws TapestryModelException;
  /**
   * Returns whether this artifact has one or more immediate children.
   * This is a convenience method, and may be more efficient than
   * testing whether <code>getChildren</code> is an empty array.
   *
   * @exception TapestryModelException if this element does not exist or if an
   *      exception occurs while accessing its corresponding resource
   * @return true if the immediate children of this element, false otherwise
   */
  boolean hasChildren() throws TapestryModelException;
}
