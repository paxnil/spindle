package com.iw.plugins.spindle.change;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class ComponentResourceChangeManager implements IResourceChangeListener {

  /**
   * Constructor for ModelResourceChangeManager.
   */
  public ComponentResourceChangeManager() {
    super();
  }

  /**
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(IResourceChangeEvent)
   */
  public void resourceChanged(IResourceChangeEvent event) {
  }

}
