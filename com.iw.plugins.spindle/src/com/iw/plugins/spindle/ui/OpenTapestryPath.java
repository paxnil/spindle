package com.iw.plugins.spindle.ui;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class OpenTapestryPath extends Action {

  int acceptFlags;
  String tapestryPath;
  IStorage[] found;

  /**
   * Constructor for OpenTapestryPath.
   * @param text
   */
  public OpenTapestryPath(int acceptFlags) {
    super();
    this.acceptFlags = acceptFlags;
  }

  public void configure(TapestryLookup lookup, String tapestryPath) { 
    this.tapestryPath = tapestryPath;
    found = lookup.findByTapestryPath(tapestryPath, acceptFlags);
    setEnabled(found != null && found.length > 0);
    if (isEnabled()) {
      setText(found[0].getName());
    }

  }

  public void run() {

    IEditorPart editor = Utils.getEditorFor(found[0]);

    if (editor != null) {

      TapestryPlugin.getDefault().getActivePage().bringToTop(editor);

    } else {

      TapestryPlugin.getDefault().openTapestryEditor(found[0]);

    }

  }

}
