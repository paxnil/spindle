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
package com.iw.plugins.spindle.html;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.ui.ChooseFromNamespaceDialog;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.StringSorter;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public class HTMLContentOutlinePage
  extends ContentOutlinePage
  implements IDocumentPartitioningListener, IDocumentListener {

  IDocument document;
  IFile documentFile = null;
  TapestryHTMLEditor editor;
  ContentProvider contentProvider = new ContentProvider();

  private CreateContainedComponentAction createAction = new CreateContainedComponentAction();
  private ContainedComponentAlreadyExistsAction alreadyHasAction =
    new ContainedComponentAlreadyExistsAction();

  /**
   * Constructor for HTMLOutlinePage
   */
  protected HTMLContentOutlinePage(TapestryHTMLEditor editor) {
    super();
    this.editor = editor;

  }

  public void setDocument(IDocument document) {
    this.document = document;
    document.addDocumentPartitioningListener(this);
    document.addDocumentListener(this);
  }

  public void setDocumentFile(IFile file) {
    documentFile = file;
  }

  public void createControl(Composite parent) {
    super.createControl(parent);
    TreeViewer viewer = getTreeViewer();
    viewer.setContentProvider(contentProvider);
    viewer.setLabelProvider(new LabelProvider());
    viewer.setSorter(new Sorter());
    MenuManager popupMenuManager = new MenuManager();
    IMenuListener listener = new IMenuListener() {
      public void menuAboutToShow(IMenuManager mng) {
        fillContextMenu(mng);
      }
    };
    Tree treeControl = (Tree) viewer.getControl();
    popupMenuManager.setRemoveAllWhenShown(true);
    popupMenuManager.addMenuListener(listener);
    Menu menu = popupMenuManager.createContextMenu(treeControl);
    treeControl.setMenu(menu);
    viewer.setInput("Go!");
  }

  /**
   * @see IDocumentPartitioningListener#documentPartitioningChanged(IDocument)
   */
  public void documentPartitioningChanged(IDocument document) {
    getTreeViewer().setInput(new Long(System.currentTimeMillis()));
  }

  public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    ITypedRegion region = (ITypedRegion) selection.getFirstElement();
    if (region != null) {
      fireSelectionChanged(new StructuredSelection(new Object[] { findJWCID(region)}));
    }
  }

  private TapestryComponentModel getTargetModel(IFile file, Object consumer, boolean writeable) {

    if (file == null) {
      return null;
    }

    TapestryComponentModel model = null;
    try {

      SpindleMultipageEditor targetEditor = (SpindleMultipageEditor) Utils.getEditorFor(file);

      if (targetEditor != null) {

        model = (TapestryComponentModel) targetEditor.getModel();
      }

    } catch (ClassCastException e) {

    }

    if (model == null) { // not found in an editor!

      try {
        TapestryProjectModelManager mgr = TapestryPlugin.getTapestryModelManager(file);
        mgr.connect(file, consumer, writeable);
        model = (TapestryComponentModel) mgr.getEditableModel(file, consumer);
      } catch (CoreException e) {
      }

    }

    return model;
  }

  private void fillContextMenu(IMenuManager manager) {
    TreeViewer viewer = (TreeViewer) getTreeViewer();
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

    String selectedJwcid = null;

    if (selection != null && selection.size() > 0) {
      ILabelProvider provider = (ILabelProvider) viewer.getLabelProvider();
      selectedJwcid = provider.getText(selection.getFirstElement());
      if (selectedJwcid.startsWith("$")) {
        return;
      }
    }
    IFile file = findRelatedComponent();
    TapestryComponentModel model = null;
    try {
      model =
        (TapestryComponentModel) TapestryPlugin.getTapestryModelManager(file).getReadOnlyModel(
          file);
    } catch (CoreException e) {
    }
    boolean canCreate = (model != null);
    if (canCreate) {

      PluginComponentSpecification spec =
        (PluginComponentSpecification) model.getComponentSpecification();

      canCreate = (spec != null && spec.getComponent(selectedJwcid) == null);
      if (canCreate) {

        createAction.configure(file, selectedJwcid);
        manager.add(createAction);

      } else {

        alreadyHasAction.configure(selectedJwcid);
        manager.add(alreadyHasAction);
      }
    }

  }

  private IFile findRelatedComponent() {
    if (documentFile != null) {

      IContainer parent = documentFile.getParent();
      String name = documentFile.getFullPath().removeFileExtension().lastSegment();
      String jwcName = name + ".jwc";
      IFile componentResource = (IFile) parent.findMember(jwcName);
      if (componentResource != null && componentResource.exists()) {
        return componentResource;
      }
    }
    return null;
  }

  private String getJWCID(ITypedRegion region) {
    try {
      Position p = findJWCID(region);
      if (p == null) {
        return null;
      }
      return document.get(p.getOffset(), p.getLength());
    } catch (BadLocationException blex) {
      return "error";
    }
  }

  private Position findJWCID(ITypedRegion region) {
    if (region == null) {
      return null;
    }
    Position result = new Position(region.getOffset(), region.getLength());
    String type = region.getType();
    String start = null;
    if (TapestryHTMLPartitionScanner.JWCID_TAG.equals(type)) {
      start = "jwcid=\"";
    } else if (TapestryHTMLPartitionScanner.JWC_TAG.equals(type)) {
      start = "id=\"";
    }
    if (start != null) {

      try {
        String tag = document.get(region.getOffset(), region.getLength());
        int startIndex = tag.indexOf(start);
        if (startIndex >= 0) {
          startIndex += start.length();
          tag = tag.substring(startIndex);
          int end = tag.indexOf("\"");
          if (end >= 0) {
            result = new Position(region.getOffset() + startIndex, tag.substring(0, end).length());
          }
        } else {
          return null;
        }
      } catch (BadLocationException blex) {
      }
    }
    return result;
  }

  /**
   * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
   */
  public void documentAboutToBeChanged(DocumentEvent arg0) {
  }

  /**
   * @see IDocumentListener#documentChanged(DocumentEvent)
   */
  public void documentChanged(DocumentEvent event) {
    TreeViewer viewer = getTreeViewer();
    if (viewer != null) {
      getTreeViewer().setInput(new Long(System.currentTimeMillis()));
    }
  }

  protected class ContentProvider implements ITreeContentProvider {

    ArrayList contents = null;

    /**
     * Constructor for ContentProvider
     */
    public ContentProvider() {
      super();
    }

    /**
    * @see ITreeContentProvider#getChildren(Object)
    */
    public Object[] getChildren(Object element) {
      return null;
    }

    /**
     * @see ITreeContentProvider#getParent(Object)
     */
    public Object getParent(Object element) {
      return null;
    }

    /**
     * @see ITreeContentProvider#hasChildren(Object)
     */
    public boolean hasChildren(Object element) {
      return false;
    }

    /**
     * @see IStructuredContentProvider#getElements(Object)
     */
    public Object[] getElements(Object ignored) {

      getTreeViewer().setSelection(StructuredSelection.EMPTY);
      if (contents == null) {
        contents = new ArrayList();
      }
      ArrayList oldContents = (ArrayList) contents.clone();
      ArrayList addedList = new ArrayList();
      contents.clear();
      if (document != null) {

        ITypedRegion[] partitions = null;
        try {
          partitions = document.computePartitioning(0, document.getLength() - 1);
        } catch (BadLocationException ex) {
          return new Object[] { "error occured scanning source" };
        }
        for (int i = 0; i < partitions.length; i++) {
          String type = partitions[i].getType();
          if (type.equals(TapestryHTMLPartitionScanner.JWC_TAG)
            || type.equals(TapestryHTMLPartitionScanner.JWCID_TAG)) {
            if (findJWCID(partitions[i]) != null) {

              if (!oldContents.contains(partitions[i])) {
                addedList.add(partitions[i]);
              } else {
                oldContents.remove(partitions[i]);
              }

              contents.add(partitions[i]);
            }
          }
        }
      }
      if (editor != null && (!addedList.isEmpty() || !oldContents.isEmpty())) {
        editor.parseForProblems();
      }
      return contents.toArray();
    }

    /**
     * @see IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see IContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(Viewer arg0, Object oldSource, Object newSource) {
    }

  }

  protected class LabelProvider implements ILabelProvider {
    Image jwcImage = TapestryImages.getSharedImage("component16.gif");
    /**
     * Constructor for LabelProvider
     */
    public LabelProvider() {
      super();
    }

    /**
    * @see ILabelProvider#getImage(Object)
    */
    public Image getImage(Object arg0) {
      return jwcImage;
    }

    /**
     * @see ILabelProvider#getText(Object)
     */
    public String getText(Object element) {
      if (element instanceof String) {
        return (String) element;
      }
      if (element instanceof ITypedRegion) {
        return getJWCID((ITypedRegion) element);
      }
      return null;
    }

    /**
     * @see IBaseLabelProvider#addListener(ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener arg0) {
    }

    /**
     * @see IBaseLabelProvider#dispose()
     */
    public void dispose() {
      //shared images are disposed by the Plugin
    }

    /**
     * @see IBaseLabelProvider#isLabelProperty(Object, String)
     */
    public boolean isLabelProperty(Object arg0, String arg1) {
      return false;
    }

    /**
     * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener arg0) {
    }

  }

  protected class Sorter extends StringSorter {

    protected Sorter() {
      super();
    }

    public int compare(Viewer viewer, Object e1, Object e2) {
      LabelProvider provider = (LabelProvider) getTreeViewer().getLabelProvider();
      return super.compare(viewer, provider.getText(e1), provider.getText(e2));
    }
  }

  class CreateContainedComponentAction extends Action {

    IFile modelFile;
    String jwcid;

    public CreateContainedComponentAction() {
      super();
      setToolTipText("create a contained component in the related .jwc file with the selected name");
    }

    public void configure(IFile model, String jwcid) {
      this.modelFile = model;
      this.jwcid = jwcid;

      setText(
        "create '"
          + jwcid
          + "' in '"
          + documentFile.getFullPath().removeFileExtension().lastSegment()
          + ".jwc'");
    }

    public void run() {
      IEditorPart targetEditor;

      try {
        targetEditor = Utils.getEditorFor(modelFile);
        if (targetEditor == null) {
          createInWorkspace();
        } else {
          SpindleMultipageEditor spindleEditor = (SpindleMultipageEditor) targetEditor;
          createInEditorModel(spindleEditor);
        }
        if (editor != null) {
          editor.parseForProblems();
        }

      } catch (ClassCastException e) {
        MessageDialog.openError(
          getTreeViewer().getControl().getShell(),
          "Operation Aborted",
          "The target: "
            + documentFile.getFullPath().removeFileExtension().lastSegment()
            + ".jwc\n Is open in a non-Spindle editor.\n\nCannot proceed.");
        return;
      }

    }

    private void createInEditorModel(SpindleMultipageEditor targetEditor) {
      TapestryComponentModel model = null;
      Assert.isNotNull(targetEditor);
      if (targetEditor.isDirty()) {
        RequiredSaveEditorAction saver = new RequiredSaveEditorAction(targetEditor);
        if (!saver.save()) {
          return;
        }

      }

      model = (TapestryComponentModel) targetEditor.getModel();

      if (!model.isLoaded()) {
        parseError();
        return;
      }

      String chosen = chooseTargetFor(model);
      if (chosen == null) {
        return;
      }
      Utils.createContainedComponentIn(jwcid, chosen, model);
      if (targetEditor == null) {
        TapestryPlugin.openTapestryEditor(model.getUnderlyingStorage());
      }
      //    } catch (Exception e) {
      //      e.printStackTrace();
      //      TapestryPlugin.getDefault().logException(e);
      //    }
    }

    private void createInWorkspace() {
      TapestryComponentModel foundModel = null;
      String consumer = "CreateContainedComponentInWorkspace";
      TapestryProjectModelManager mgr = null;
      try {
        mgr = TapestryPlugin.getTapestryModelManager(modelFile);
        foundModel = getTargetModel(modelFile, consumer, true);

      } catch (CoreException e) {
      }

      final TapestryComponentModel model = foundModel;
      if (model == null) {
        return;
      }

      String chosen = chooseTargetFor(model);

      if (chosen != null) {

        Utils.createContainedComponentIn(jwcid, chosen, model);

        try {

          (new WorkspaceModifyOperation() {
            public void execute(final IProgressMonitor monitor) throws CoreException {

              Utils.saveModel(model, new NullProgressMonitor());

            }
          }).execute(new NullProgressMonitor());

          TapestryPlugin.openTapestryEditor(modelFile);

        } catch (CoreException e) {
        }

      }

      if (mgr != null && model != null) {
        mgr.disconnect(modelFile, consumer);
      }

    }

    private String chooseTargetFor(TapestryComponentModel model) {
      String chosen = null;
      ITapestryProject tproject;

      try {

        tproject = TapestryPlugin.getDefault().getTapestryProjectFor(model.getUnderlyingStorage());
      } catch (CoreException e) {

        ErrorDialog.openError(
          getTreeViewer().getControl().getShell(),
          "Spindle project error",
          "Can't find the Tapestry project",
          e.getStatus());

        return null;
      }
      ChooseFromNamespaceDialog dialog =
        new ChooseFromNamespaceDialog(
          getTreeViewer().getControl().getShell(),
          tproject,
          "Create in " + documentFile.getFullPath().removeFileExtension().lastSegment() + ".jwc",
          "Choose the Contained Component to create",
          TapestryLookup.ACCEPT_COMPONENTS);

      dialog.create();
      if (dialog.open() == dialog.OK) {

        chosen = dialog.getResultPath();

      }
      return chosen;
    }

    private void parseError() {
      Status status =
        new Status(
          IStatus.ERROR,
          TapestryPlugin.getDefault().getPluginId(),
          IStatus.OK,
          "Abort, target component has parse errors",
          null);

      ErrorDialog.openError(
        TapestryPlugin.getDefault().getActiveWorkbenchShell(),
        null,
        null,
        status);

    }
  }

  class ContainedComponentAlreadyExistsAction extends Action {

    public ContainedComponentAlreadyExistsAction() {
      super();
    }

    public void configure(String jwcid) {
      setText(
        "'"
          + jwcid
          + "' already exists in "
          + documentFile.getFullPath().removeFileExtension().lastSegment()
          + ".jwc");
    }

    public void run() { /* do nothing*/
    }
  }
}