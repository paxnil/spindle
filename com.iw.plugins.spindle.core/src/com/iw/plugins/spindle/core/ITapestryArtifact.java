package com.iw.plugins.spindle.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

/**
 * Common protocol for all artifacts provided by the Tapestry model.
 * Tapestry model artifacts are exposed to clients as handles to the actual underlying artifact.
 * The Tapestry model may hand out any number of handles for each artifact. Handles
 * that refer to the same artifact are guaranteed to be equal, but not necessarily identical.
 * <p>
 * Methods annotated as "handle-only" do not require underlying elements to exist. 
 * Methods that require underlying elements to exist throw
 * a <code>TapestryModelException</code> when an underlying artifact is missing.
 * <code>TapestryModelException.isDoesNotExist</code> can be used to recognize
 * this common special case.
 * </p>
 */
public interface ITapestryArtifact extends IAdaptable {

  /**
   * Constant representing a Tapestry model (workspace level object).
   * A Tapestry artifact with this type can be safely cast to <code>ITapestryModel</code>.
   */
  public static final int TAPESTRY_MODEL = 1;

  /**
   * Constant representing a Tapestry project.
   * A Tapestry artifact with this type can be safely cast to <code>ITapestryProject</code>.
   */
  public static final int TAPESTRY_PROJECT = 2;

  /**
   * Constant representing a Tapestry library.
   * A Tapestry artifact with this type can be safely cast to <code>ITapestryLibrary</code>.
   */
  public static final int TAPESTRY_LIBRARY = 3;

  /**
   * Constant representing a Tapestry component.
   * A Tapestry artifact with this type can be safely cast to <code>ITapestryProject</code>.
   */
  public static final int TAPESTRY_COMPONENT = 4;

  /**
   * Constant representing a Tapestry template.
   * A Tapestry artifact with this type can be safely cast to <code>ITapestryTemplate</code>.
   */
  public static final int TAPESTRY_TEMPLATE = 5;

  /**
   * Constant representing a Tapestry script.
   * A Tapestry artifact with this type can be safely cast to <code>ITapestryScript</code>.
   */
  public static final int TAPESTRY_SCRIPT = 6;

  //  /**
  //   * Constant representing a package fragment root.
  //   * A Tapestry artifact with this type can be safely cast to <code>IPackageFragmentRoot</code>.
  //   */
  //  public static final int PACKAGE_FRAGMENT_ROOT = 3;
  //
  //  /**
  //   * Constant representing a package fragment.
  //   * A Tapestry artifact with this type can be safely cast to <code>IPackageFragment</code>.
  //   */
  //  public static final int PACKAGE_FRAGMENT = 4;
  //
  //  /**
  //   * Constant representing a Tapestry compilation unit.
  //   * A Tapestry artifact with this type can be safely cast to <code>ICompilationUnit</code>.
  //   */
  //  public static final int COMPILATION_UNIT = 5;
  //
  //  /**
  //   * Constant representing a class file.
  //   * A Tapestry artifact with this type can be safely cast to <code>IClassFile</code>.
  //   */
  //  public static final int CLASS_FILE = 6;
  //
  //  /**
  //   * Constant representing a type (a class or interface).
  //   * A Tapestry artifact with this type can be safely cast to <code>IType</code>.
  //   */
  //  public static final int TYPE = 7;
  //
  //  /**
  //   * Constant representing a field.
  //   * A Tapestry artifact with this type can be safely cast to <code>IField</code>.
  //   */
  //  public static final int FIELD = 8;
  //
  //  /**
  //   * Constant representing a method or constructor.
  //   * A Tapestry artifact with this type can be safely cast to <code>IMethod</code>.
  //   */
  //  public static final int METHOD = 9;
  //
  //  /**
  //   * Constant representing a stand-alone instance or class initializer.
  //   * A Tapestry artifact with this type can be safely cast to <code>IInitializer</code>.
  //   */
  //  public static final int INITIALIZER = 10;
  //
  //  /**
  //   * Constant representing a package declaration within a compilation unit.
  //   * A Tapestry artifact with this type can be safely cast to <code>IPackageDeclaration</code>.
  //   */
  //  public static final int PACKAGE_DECLARATION = 11;
  //
  //  /**
  //   * Constant representing all import declarations within a compilation unit.
  //   * A Tapestry artifact with this type can be safely cast to <code>IImportContainer</code>.
  //   */
  //  public static final int IMPORT_CONTAINER = 12;
  //
  //  /**
  //   * Constant representing an import declaration within a compilation unit.
  //   * A Tapestry artifact with this type can be safely cast to <code>IImportDeclaration</code>.
  //   */
  //  public static final int IMPORT_DECLARATION = 13;

  /**
   * Returns whether this Tapestry artifact exists in the model.
   *
   * @return <code>true</code> if this artifact exists in the Tapestry model
   */
  boolean exists();
  /**
   * Returns the first ancestor of this Tapestry artifact that has the given type.
   * Returns <code>null</code> if no such an ancestor can be found.
   * This is a handle-only method.
   * 
   * @param ancestorType the given type
   * @return the first ancestor of this Tapestry artifact that has the given type, null if no such an ancestor can be found
   * @since 2.0
   */
  ITapestryArtifact getAncestor(int ancestorType);
  /**
   * Returns the resource that corresponds directly to this artifact,
   * or <code>null</code> if there is no resource that corresponds to
   * this artifact.
   *
   * @return the corresponding resource, or <code>null</code> if none
   * @exception TapestryModelException if this artifact does not exist or if an
   *		exception occurs while accessing its corresponding storage
   */
  IResource getCorrespondingResource() throws TapestryModelException;
  /**
   * Returns the name of this artifact. This is a handle-only method.
   *
   * @return the artifact name
   */
  String getArtifactName();
  /**
   * Returns this artifact's kind encoded as an integer.
   * This is a handle-only method.
   *
   * @return the kind of artifact; one of the constants declared in
   *   <code>ITapestryArtifact</code>
   * @see ITapestryArtifact
   */
  public int getArtifactType();
  /**
   * Returns a string representation of this artifact handle. The format of
   * the string is not specified; however, the identifier is stable across
   * workspace sessions, and can be used to recreate this handle via the 
   * <code>JavaCore.create(String)</code> method.
   *
   * @return the string handle identifier
   * @see JavaCore#create(java.lang.String)
   */
  String getHandleIdentifier();
  /**
   * Returns the Tapestry model.
   * This is a handle-only method.
   *
   * @return the Tapestry model
   */
  ITapestryModel getTapestryModel();
  /**
   * Returns the Tapestry project this artifact is contained in,
   * or <code>null</code> if this artifact is not contained in any Tapestry project
   * (for instance, the <code>ITapestryModel</code> is not contained in any Tapestry 
   * project).
   * This is a handle-only method.
   *
   * @return the containing Tapestry project, or <code>null</code> if this artifact is
   *   not contained in a Tapestry project
   */
  ITapestryProject getTapestryProject();
  /**
   * Returns the first openable parent. If this artifact is openable, the artifact
   * itself is returned. Returns <code>null</code> if this artifact doesn't have
   * an openable parent.
   * This is a handle-only method.
   * 
   * @return the first openable parent or <code>null</code> if this artifact doesn't have
   * an openable parent.
   */
  IOpenable getOpenable();
  /**
   * Returns the artifact directly containing this artifact,
   * or <code>null</code> if this artifact has no parent.
   * This is a handle-only method.
   *
   * @return the parent artifact, or <code>null</code> if this artifact has no parent
   */
  ITapestryArtifact getParent();
  /**
   * Returns the path to the innermost storage enclosing this artifact. 
   * If this artifact is not included in an external archive, 
   * the path returned is the full, absolute path to the underlying resource, 
   * relative to the workbench. 
   * If this artifact is included in an external archive, 
   * the path returned is the absolute path to the archive in the file system.
   * This is a handle-only method.
   * 
   * @return the path to the innermost storage enclosing this artifact
   */
  IPath getPath();
  /**
   * Returns the innermost resource enclosing this artifact.
   * Effectively, if this artifact is not contained in a storage,
   * return the first ancestor that is.
   * 
   */
  IStorage getStorage();
  /**
   * Returns the smallest underlying resource that contains
   * this artifact, or <code>null</code> if this artifact is not contained
   * in a resource.
   *
   * @return the underlying resource, or <code>null</code> if none
   * @exception TapestryModelException if this artifact does not exist or if an
   *		exception occurs while accessing its underlying resource
   */
  IStorage getUnderlyingStorage() throws TapestryModelException;
  /**
   * Returns whether this Tapestry artifact is read-only. An artifact is read-only
   * if its structure cannot be modified by the java model. 
   * <p>
   * Note this is different from IResource.isReadOnly(). For example, .jar
   * files are read-only as the tapestry model doesn't know how to add/remove 
   * artifacts in this file, but the underlying IFile can be writable.
   * <p>
   * This is a handle-only method.
   *
   * @return <code>true</code> if this artifact is read-only
   */
  boolean isReadOnly();
  /**
   * Returns whether the structure of this artifact is known. For example, for a
   * artifact that could not be parsed, <code>false</code> is returned.
   * If the structure of an artifact is unknown, navigations will return reasonable
   * defaults. For example, <code>getChildren</code> will return an empty collection.
   * <p>
   * Note: This does not imply anything about consistency with the
   * underlying resource/buffer contents.
   * </p>
   *
   * @return <code>true</code> if the structure of this artifact is known
   * @exception TapestryModelException if this artifact does not exist or if an
   *		exception occurs while accessing its corresponding resource
   */
  boolean isStructureKnown() throws TapestryModelException;
}
