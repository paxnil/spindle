package com.iw.plugins.spindle.core;

import org.eclipse.core.resources.IWorkspace;

/**
 * Represent the root Tapestry artifact corresponding to the workspace. 
 * Since there is only one such root element, it is commonly referred to as
 * <em>the</em> Tapestry model element.
 * The Tapestry model element needs to be opened before it can be navigated or manipulated.
 * The Tapestry model element has no parent (it is the root of the Java element 
 * hierarchy). Its children are <code>ITapestryProject</code>s.
 * <p>
 * This interface is not intended to be implemented by clients. An instance
 * of one of these handles can be created via
 * <code>TapestryCore.create(workspace.getRoot())</code>.
 * </p>
 *
 * @see TapestryCore#create(org.eclipse.core.resources.IWorkspaceRoot)
 */

public interface ITapestryModel extends ITapestryArtifact, IOpenable, IParent {
  /**
   * Returns the Tapestry project with the given name. This is a handle-only method. 
   * The project may or may not exist.
   * 
   * @return the Java project with the given name
   */
  ITapestryProject getTapestryProject(String name);
  /**
   * Returns the Java projects in this Java model, or an empty array if there
   * are none.
   *
   * @return the Java projects in this Java model, or an empty array if there
   * are none
   * @exception JavaModelException if this request fails.
   */
  ITapestryProject[] getTapestryProjects() throws TapestryModelException;
  /**
   * Returns the workspace associated with this Java model.
   * 
   * @return the workspace associated with this Java model
   */
  IWorkspace getWorkspace();
}
