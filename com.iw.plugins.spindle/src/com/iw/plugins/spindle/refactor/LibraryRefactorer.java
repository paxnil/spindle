package com.iw.plugins.spindle.refactor;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IEditorPart;

import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class LibraryRefactorer {

  private TapestryProjectModelManager modelManager;
  private TapestryLibraryModel editableModel;
  private IEditorPart editor = null;
  private Object consumer = null;

  /**
   * Constructor for LibraryRefactorer.
   */
  public LibraryRefactorer(
    ITapestryProject project,
    TapestryLibraryModel model,
    boolean allowNonSpindleEditor)
    throws CoreException {

    this.modelManager = project.getModelManager();

    IStatus status = findEditableModel(model.getUnderlyingStorage(), allowNonSpindleEditor);

    if (!status.isOK()) {

      throw new CoreException(status);

    }
  }

  private IStatus findEditableModel(IStorage storage, boolean allowNonSpindleEditor)
    throws CoreException {

    SpindleStatus status = new SpindleStatus();
    TapestryLibraryModel result = null;

    editor = Utils.getEditorFor(storage);

    if (editor != null) {

      if (!allowNonSpindleEditor && !(editor instanceof SpindleMultipageEditor)) {

        status.setError(storage.getName() + "is open in an non Spindle editor");
        throw new CoreException(status);
      }

      result = (TapestryLibraryModel) ((SpindleMultipageEditor) editor).getModel();

      if (editor.isDirty() && !result.isDirty()) {

        RequiredSaveEditorAction requiredSave = new RequiredSaveEditorAction(editor);

        if (!requiredSave.save()) {

          status.setError(
            "You must save " + result.getUnderlyingStorage().getName() + " before proceeding.");
        }

      }

    } else {

      consumer = new Object();

      result = (TapestryLibraryModel) modelManager.getEditableModel(storage, consumer);

    }

    if (status.isOK()) {

      this.editableModel = result;

    }

    return status;
  }

  public IPluginLibrarySpecification getSpecification() throws CoreException {
    checkInitialized();

    return editableModel.getSpecification();

  }

  public TapestryLibraryModel getEditableModel() {

    return editableModel;

  }

  public void commit(IProgressMonitor monitor) throws CoreException {
    checkInitialized();

    if (editor != null) {

      editor.doSave(monitor);

      if (editor instanceof SpindleMultipageEditor) {

        ((SpindleMultipageEditor) editor).showPage(SpindleMultipageEditor.SOURCE_PAGE);

      }
    } else {

      Utils.saveModel(editableModel, monitor);

      modelManager.disconnect(consumer, editableModel.getUnderlyingStorage());

      consumer = null;

    }

    editableModel = null;

  }

  // has no effect if the model is from an editor!
  public void discard() throws CoreException {

    checkInitialized();

    if (editor == null && consumer != null) {

      modelManager.disconnect(consumer, editableModel.getUnderlyingStorage());

    }

  }

  public void checkInitialized() throws CoreException {

    if (editableModel == null) {

      SpindleStatus status = new SpindleStatus();
      status.setError("not initialized");

      throw new CoreException(status);

    }

  }

}
