package com.iw.plugins.spindle.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Common protocol for Tapestry artifacts that must be opened before they can be 
 * navigated or modified. Opening a textual Tapestry artifacts (such as a template)
 * involves parsing its contents.  
 * <p>
 * To open an Tapestry artifact, all openable parent elements must be open.
 * The Tapestry model automatically opens parent elements, as it automatically opens elements.
 * Opening an Tapestry artifact may provide access to direct children and other descendants,
 * but does not automatically open any descendents which are themselves <code>IOpenable</code>.
 * For example, opening an application provides access to all its constituent elements (like service declarations),
 * but opening the servlet context root does not open all templates in context root.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IOpenable {

  /**
   * Closes this Tapestry artifact.
   * Closing an Tapestry artifacts which is not open has no effect.
   *
   * <p>Note: although <code>close</code> is exposed in the API, clients are
   * not expected to open and close elements - the Java model does this automatically
   * as elements are accessed.
   *
   * @exception TapestryModelException if an error occurs closing this Tapestry artifact
   */
  public void close() throws TapestryModelException;
  /**
   * Returns <code>true</code> if this Tapestry artifact is open and:
   * <ul>
   * <li>it has unsaved changes, or
   * <li>one of its descendants has unsaved changes, or
   * <li>a working copy has been created on one of this
   * Tapestry artifacts's children and has not yet been destroyed
   * </ul>
   *
   * @exception TapestryModelException if this Tapestry artifact does not exist or if an
   *		exception occurs while accessing its corresponding resource.
   * @return <code>true</code> if this Tapestry artifact is open and:
   * <ul>
   * <li>it has unsaved changes, or
   * <li>one of its descendants has unsaved changes, or
   * <li>a working copy has been created on one of this
   * Tapestry artifacts's children and has not yet been destroyed
   * </ul>
   */
  boolean hasUnsavedChanges() throws TapestryModelException;
  /**
   * Returns whether the Tapestry artifacts is consistent with its underlying resource or buffer.
   * The Tapestry artifact is consistent when opened, and is consistent if the underlying resource
   * has not been modified since it was last consistent.
   *
   * <p>NOTE: Child consistency is not considered. For example, an application
   * responds <code>true</code> when it knows about all of its children. However, one or more of
   * the children could be inconsistent.
   *
   * @exception TapestryModelException if this Tapestry artifact does not exist or if an
   *		exception occurs while accessing its corresponding resource.
   * @return true if the Tapestry artifacts is consistent with its underlying resource or buffer, false otherwise.
   */
  boolean isConsistent() throws TapestryModelException;
  /**
   * Returns whether this openable is open. This is a handle-only method.
   * @return true if this openable is open, false otherwise
   */
  boolean isOpen();

  /**
   * Opens this Tapestry artifact and all parent elements that are not already open.
   *
   * <p>Note: although <code>open</code> is exposed in the API, clients are
   * not expected to open and close elements - the Java model does this automatically
   * as elements are accessed.
   *
   * @param progress the given progress monitor
   * @exception TapestryModelException if an error occurs accessing the contents
   * 		of its underlying resource. Reasons include:
   * <ul>
   *  <li>This Java Tapestry artifacts does not exist (ELEMENT_DOES_NOT_EXIST)</li>
   * </ul>
   */
  public void open(IProgressMonitor progress) throws TapestryModelException;
  /**
   * Saves any changes in this Tapestry artifact to its underlying resource
   * via a workspace resource operation. This has no effect if the Tapestry artifacts has no unsaved changes.
   * <p>
   * The <code>force</code> parameter controls how this method deals with
   * cases where the workbench is not completely in sync with the local file system.
   * If <code>false</code> is specified, this method will only attempt
   * to overwrite a corresponding file in the local file system provided
   * it is in sync with the workbench. This option ensures there is no 
   * unintended data loss; it is the recommended setting.
   * However, if <code>true</code> is specified, an attempt will be made
   * to write a corresponding file in the local file system, 
   * overwriting any existing one if need be.
   * In either case, if this method succeeds, the resource will be marked 
   * as being local (even if it wasn't before).
   * <p>
   * As a result of this operation, the Tapestry artifacts is consistent with its underlying 
   * resource or buffer. 
   *
   * @param progress the given progress monitor
   * @param force it controls how this method deals with
   * cases where the workbench is not completely in sync with the local file system
   * @exception TapestryModelException if an error occurs accessing the contents
   * 		of its underlying resource. Reasons include:
   * <ul>
   *  <li>This Java Tapestry artifacts does not exist (ARTIFACT_DOES_NOT_EXIST)</li>
   *  <li>This Java Tapestry artifacts is read-only (READ_ONLY)</li>
   * </ul>
   */
  public void save(IProgressMonitor progress, boolean force) throws TapestryModelException;
}