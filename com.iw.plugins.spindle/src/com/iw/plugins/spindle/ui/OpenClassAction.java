package com.iw.plugins.spindle.ui;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IEditorPart;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.util.Utils;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class OpenClassAction extends Action {

    IType javaType;

    public OpenClassAction(IStorage jwcOrPageStorage) {

      this.javaType = Utils.findComponentClass(jwcOrPageStorage);
      setEnabled(false);
      if (javaType != null) {
        setEnabled(true);
        setText(javaType.getElementName());
      }

    }
    public OpenClassAction(IType javaType) {
      this.javaType = javaType;
      setText(javaType.getElementName());
    }

    public void run() {

      if (javaType == null) {

        return;

      }

      try {
        IEditorPart javaEditor = JavaUI.openInEditor(javaType);
      } catch (CoreException e) {

        ErrorDialog.openError(
          TapestryPlugin.getDefault().getActiveWorkbenchShell(),
          "Workbench Error",
          "could not open" + javaType.getElementName(),
          e.getStatus());

      }

    }
  }
