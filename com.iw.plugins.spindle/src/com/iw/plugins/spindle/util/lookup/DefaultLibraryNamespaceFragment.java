package com.iw.plugins.spindle.util.lookup;

import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.ITapestryProject;

import net.sf.tapestry.spec.ILibrarySpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public final class DefaultLibraryNamespaceFragment extends NamespaceFragment {

  static DefaultLibraryNamespaceFragment instance;

  static public DefaultLibraryNamespaceFragment getInstance() {

    return instance;

  }

  static public synchronized DefaultLibraryNamespaceFragment getInstance(ILibrarySpecification specification)
    throws CoreException {

    if (instance == null) {

      instance = new DefaultLibraryNamespaceFragment(specification);

    }

    return instance;

  }

  protected DefaultLibraryNamespaceFragment(ILibrarySpecification specification) throws CoreException {
    setName("");
    setSpecification(specification);
  }

  /**
   * @see com.iw.plugins.spindle.util.lookup.INamespaceFragment#isDefaultNamespace()
   */
  public boolean isDefaultNamespace() {
    return true;
  }

}
