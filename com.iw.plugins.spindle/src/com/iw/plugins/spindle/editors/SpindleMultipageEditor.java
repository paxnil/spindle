/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.internal.ui.editor.IPDEEditorPage;
import org.eclipse.pde.internal.ui.editor.PDEEditorContributor;
import org.eclipse.pde.internal.ui.editor.PDEMultiPageXMLEditor;
import org.eclipse.pde.internal.ui.editor.SystemFileEditorInput;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.manager.TapestryModelManager;
import com.iw.plugins.spindle.util.JarEditorInputWrapper;
import com.iw.plugins.spindle.util.Utils;

public abstract class SpindleMultipageEditor extends PDEMultiPageXMLEditor {

  private static String EDITOR_NAME = "SpindleMultipageEditor";
  private static String WRONG_EDITOR = EDITOR_NAME + ".wrongEditor";
  private boolean dirty = false;
  private boolean duringInit = false;

  public static final String SOURCE_PAGE = "SOURCEPAGE";
  /**
  * Constructor for TapestryMultipageEditor
  */
  public SpindleMultipageEditor() {
    super();
  }

  public void init(IEditorSite site, IEditorInput input) throws PartInitException {
    duringInit = true;
    if (isValidContentType(input) == false) {
      String message = MessageUtil.getFormattedString(WRONG_EDITOR, input.getName());
      IStatus s =
        new Status(
          IStatus.ERROR,
          TapestryPlugin.getDefault().getPluginId(),
          IStatus.OK,
          message,
          null);
      throw new PartInitException(s);
    }

    if (input instanceof JarEntryEditorInput) {
      input = (IEditorInput) new JarEditorInputWrapper((JarEntryEditorInput) input);
    }

    setSite(site);
    setInput(input);

    Object inputObject = null;
    if (input instanceof SystemFileEditorInput) {
      inputObject = input.getAdapter(File.class);
    } else if (input instanceof FileEditorInput) {
      inputObject = input.getAdapter(IFile.class);
    } else if (input instanceof JarEditorInputWrapper) {
      inputObject = input.getAdapter(IFile.class);
    }
    site.setSelectionProvider(this);
    try {
      initializeModels(inputObject);
    } catch (CoreException e) {
      throw new PartInitException("Unable to init Spindle models");
    }
    for (Iterator iter = super.getPages(); iter.hasNext();) {
      IEditorPart part = (IEditorPart) iter.next();
      part.init(site, input);
    }
    if (inputObject instanceof IFile) {
      setTitle(((IFile) inputObject).getName());
    } else if (inputObject instanceof java.io.File) {
      setTitle("system:" + ((java.io.File) inputObject).getName());
    } else {
      setTitle(input.toString());
    }
    getSite().getPage().addPartListener(new PartListener(this));
    if (dirty) {
      doSave(new NullProgressMonitor());
    }
    BaseTapestryModel tapModel = (BaseTapestryModel) model;
    tapModel.setDirty(false);

    if (!tapModel.isEditable()) {
      tapModel.setEditable(!tapModel.getUnderlyingStorage().isReadOnly());
    }
    duringInit = false;
  }

  public boolean isModelCorrect(Object model) {
    return ((BaseTapestryModel) model).isLoaded();
  }

  public void doSave(IProgressMonitor monitor) {
    final IEditorInput input = getEditorInput();
    final IDocumentProvider documentProvider = getDocumentProvider();

    if (documentProvider == null)
      return;

    if (documentProvider.isDeleted(getEditorInput())) {
      if (isSaveAsAllowed()) {
        performSaveAs(monitor);
      } else {
        Shell shell = getSite().getShell();
        MessageDialog.openError(shell, "Error saving", "The underlying resource has been deleted");
      }
    } else {
      commitFormPages(true);
      resynchDocument(true);
      WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
        public void execute(final IProgressMonitor monitor) throws CoreException {
          documentProvider.saveDocument(monitor, input, documentProvider.getDocument(input), true);
        }
      };
      Shell shell = getSite().getWorkbenchWindow().getShell();
      try {
        documentProvider.aboutToChange(input);
        op.run(monitor);
        documentProvider.changed(input);
        dirty = false;
        firePropertyChange(PROP_DIRTY);
        PDEEditorContributor contributor = getContributor();
        if (contributor != null)
          contributor.updateActions();
      } catch (InterruptedException x) {
      } catch (InvocationTargetException x) {
        TapestryPlugin.getDefault().logException(x);
      }
      try {
        ((BaseTapestryModel) model).reload();
      } catch (Exception e) {
      }
      dirty = false;
    }
  }

  /**
   * update the model with any changes to the source page.
   * i.e. grab the source page's text and reparse.
   * @see PDEMultiPageEditor#updateModel()
   */
  protected boolean updateModel() {
    BaseTapestryModel model = (BaseTapestryModel) getModel();
    IDocument document = getDocumentProvider().getDocument(getEditorInput());
    String text = document.get();
    boolean cleanModel = false;
    try {
      InputStream stream = new ByteArrayInputStream(text.getBytes("UTF8"));
      model.reload(stream);
      cleanModel = model.isLoaded();
      try {
        stream.close();
      } catch (IOException e) {
      }
    } catch (Exception e) {
      TapestryPlugin.getDefault().logException(e);
    }
    return cleanModel;
  }

  protected void resynchDocument(boolean onSave) {
    // flush the model content into
    // the document so that the source editor will
    // pick up the changes.
    BaseTapestryModel tmodel = (BaseTapestryModel) model;
    if (!tmodel.isEditable()) {
      return;
    }
    if (!(tmodel.isDirty() || !tmodel.isInSync())) {
      return;
    }
    try {
      // need to update the document
      IDocument document = getDocumentProvider().getDocument(getEditorInput());
      document.set(flushModelIntoString(tmodel));
      if (!onSave) {
        fireSaveNeeded();
      }
    } catch (IOException e) {
      TapestryPlugin.getDefault().logException(e);
    }
  }

  protected String flushModelIntoString(IEditable editableModel) throws IOException {
    StringWriter swriter = new StringWriter();
    PrintWriter writer = new PrintWriter(swriter);
    editableModel.save(writer);
    writer.flush();
    swriter.close();
    return swriter.toString();
  }

  /**
   * @see PDEMultiPageEditor#isModelDirty(Object)
   */
  protected boolean isModelDirty(Object model) {
    return model != null
      && model instanceof IEditable
      && model instanceof IModel
      && ((IModel) model).isEditable()
      && ((IEditable) model).isDirty();
  }

  protected abstract boolean isValidContentType(IEditorInput input);

  /**
   * @see PDEMultiPageEditor#createModel(Object)
   */
  protected Object createModel(Object input) {
    if (input instanceof IFile) {
      return createResourceModel((IFile) input);
    }
    return null;
  }

  protected ITapestryModel createResourceModel(IStorage storage) {
    InputStream stream = null;
    TapestryModelManager modelProvider = TapestryPlugin.getTapestryModelManager();
    modelProvider.connect(storage, this);
    BaseTapestryModel model = (BaseTapestryModel) modelProvider.getEditableModel(storage, this);
    if (!model.isInSync() || model.isDirty()) {
    	Utils.saveModel(model, new NullProgressMonitor());
    }
    try {
      stream = storage.getContents();
    } catch (Exception e) {
      TapestryPlugin.getDefault().logException(e);
      return null;
    }

    try {
      model.load(stream);
    } catch (CoreException e) {
      e.printStackTrace();
      TapestryPlugin.getDefault().logException(e);
    }
    try {
      stream.close();
    } catch (IOException e) {
      TapestryPlugin.getDefault().logException(e);
    }
	dirty = false;
    return model;
  }

  public void updateTitle(String value) {
    setTitle(value);
  }

  public void updateTitle() {
    firePropertyChange(IWorkbenchPart.PROP_TITLE);
  }

  public boolean isSaveAsAllowed() {
    ITapestryModel model = (ITapestryModel) getModel();
    return !model.getUnderlyingStorage().isReadOnly();
  }

  /**
  * @see EditorPart#doSaveAs()
  */
  public void doSaveAs() {
    performSaveAs(null);
  }

  protected void performSaveAs(IProgressMonitor monitor) {
    Shell shell = getSite().getShell();
    SaveAsDialog dialog = new SaveAsDialog(shell);
    if (dialog.open() == Dialog.CANCEL) {
      if (monitor != null)
        monitor.setCanceled(true);
      return;
    }
    IPath filePath = dialog.getResult();
    if (filePath == null) {
      if (monitor != null)
        monitor.setCanceled(true);
      return;
    }
    filePath = makeValidSaveAsPath(filePath);

    filePath = filePath.removeTrailingSeparator();
    final String fileName = filePath.lastSegment();
    IPath folderPath = filePath.removeLastSegments(1);
    if (folderPath == null) {
      if (monitor != null)
        monitor.setCanceled(true);
      return;
    }

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    IFile file = root.getFile(filePath);
    final FileEditorInput newInput = new FileEditorInput(file);

    WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
      public void execute(final IProgressMonitor monitor) throws CoreException {
        getDocumentProvider().saveDocument(
          monitor,
          newInput,
          getDocumentProvider().getDocument(getEditorInput()),
          true);
      }
    };

    boolean success = false;
    try {
      commitFormPages(true);
      resynchDocument(true);
      new ProgressMonitorDialog(shell).run(false, true, op);
      success = true;

    } catch (InterruptedException x) {
    } catch (InvocationTargetException x) {

      Throwable t = x.getTargetException();
      TapestryPlugin.getDefault().logException(t);

    } finally {
      if (monitor != null)
        monitor.setCanceled(!success);
    }
  }

  protected IPath makeValidSaveAsPath(IPath candidate) {
    String extension = candidate.getFileExtension();
    IPath allButExtension = candidate.removeFileExtension();
    if (model instanceof TapestryApplicationModel) {
      if ("application".equals(extension)) {
        return candidate;
      } else if (extension == null) {
        return allButExtension.addFileExtension("application");
      }
    } else if (model instanceof TapestryComponentModel) {
      if ("jwc".equals(extension)) {
        return candidate;
      } else if (extension == null) {
        return allButExtension.addFileExtension("jwc");
      }
    }
    return null;
  }

  protected class PartListener implements IPartListener {

    private SpindleMultipageEditor editor;
    /**
     * Constructor for PartListener
     */
    public PartListener(SpindleMultipageEditor editor) {
      super();
      this.editor = editor;
    }

    private void synchExternalChanges(IWorkbenchPart part) {
      BaseTapestryModel bmodel = (BaseTapestryModel) model;
      if (part == editor && !bmodel.isInSync()) {
        resynchDocument(false);
        showPage(SOURCE_PAGE);
        bmodel.setOutOfSynch(false);
      }
    }

    /**
     * @see IPartListener#partActivated(IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
      synchExternalChanges(part);
    }

    /**
     * @see IPartListener#partBroughtToTop(IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
      synchExternalChanges(part);
    }

    /**
     * @see IPartListener#partClosed(IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {
    }

    /**
     * @see IPartListener#partDeactivated(IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {
    }

    /**
     * @see IPartListener#partOpened(IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {
    }

  }
  /**
   * @see PDEMultiPageEditor#fireSaveNeeded()
   */
  public void fireSaveNeeded() {
    if (!duringInit) {
      dirty = true;
      super.fireSaveNeeded();
     
    }
  }

  /**
   * @see PDEMultiPageEditor#isDirty()
   */
  public boolean isDirty() {
    return dirty;
  }

  /**
   * @see org.eclipse.pde.internal.ui.editor.PDEMultiPageEditor#createPages()
   */
  protected void createPages() {
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    super.dispose();
    ITapestryModel model = (ITapestryModel)getModel();
    if (model != null) {
    	TapestryPlugin.getTapestryModelManager().disconnect(model.getUnderlyingStorage(), this);
    }
  }

  /**
   * @see org.eclipse.pde.internal.ui.editor.PDEMultiPageEditor#getHomePage()
   */
  public IPDEEditorPage getHomePage() {
    return null;
  }

  /**
   * @see org.eclipse.pde.internal.ui.editor.PDEMultiPageEditor#getSourcePageId()
   */
  protected String getSourcePageId() {
    return null;
  }

}