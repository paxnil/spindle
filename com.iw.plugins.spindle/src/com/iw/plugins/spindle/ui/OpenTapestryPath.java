package com.iw.plugins.spindle.ui;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
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
  IStorage toBeOpened;

  /**
   * Constructor for OpenTapestryPath.
   * @param text
   */
  public OpenTapestryPath(int acceptFlags) {
    super();
    this.acceptFlags = acceptFlags;
  }

  public void configure(TapestryLookup lookup, String tapestryPath, IMenuManager manager) {

    IStorage[] found = lookup.findByTapestryPath(tapestryPath, acceptFlags);
    setEnabled(found != null && found.length > 0);
    if (isEnabled()) {
      toBeOpened = found[0];
      setText(toBeOpened.getFullPath().toString());

      manager.add(new Separator());
      manager.add(this);

      if (toBeOpened instanceof IFile) {
        for (Iterator iter = Utils.findTemplatesFor((IFile) toBeOpened).iterator(); iter.hasNext();) {
          IStorage element = (IStorage) iter.next();
          manager.add(new OpenTemplateAction(element));

        }
      }

      Action openJava = new OpenClassAction(toBeOpened);

      if (openJava.isEnabled()) {

        manager.add(openJava);
      }

    }

  }

  public void run() {

    openEditor(toBeOpened);

  }

  protected void openEditor(IStorage storage) {

    IEditorPart editor = Utils.getEditorFor(storage);

    if (editor != null) {

      TapestryPlugin.getDefault().getActivePage().bringToTop(editor);

    } else {

      TapestryPlugin.getDefault().openTapestryEditor(storage);

    }

  }

  class OpenTemplateAction extends Action {

    IStorage templateStorage;

    public OpenTemplateAction(IStorage template) {
      templateStorage = template;
      setText(templateStorage.getFullPath().toString());
    }

    public void run() {

      openEditor(templateStorage);

    }
  }

  

}
