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
 *  phraktle@imapmail.org
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.html;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.pde.internal.ui.editor.IPDEEditorPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editorjwc.JWCMultipageEditor;
import com.iw.plugins.spindle.editorjwc.components.ComponentsFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.ui.ChooseFromNamespaceDialog;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.StringSorter;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class HTMLContentOutlinePage extends ContentOutlinePage implements IDocumentPartitioningListener, IDocumentListener {

  IDocument document;
  IFile documentFile = null;
  TapestryHTMLEditor editor;
  ContentProvider contentProvider = new ContentProvider();

  private CreateContainedComponentAction createAction = new CreateContainedComponentAction();

  private OpenComponentAction openAction = new OpenComponentAction();

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

    registerToolbarActions();

    viewer.setInput("Go!");
  }

  private void registerToolbarActions() {

    IToolBarManager toolBarManager = getSite().getActionBars().getToolBarManager();
    if (toolBarManager != null) {

      Action action = new AlphabeticalSortingAction();
      toolBarManager.add(action);

    }
  }

  /**
   * @see IDocumentPartitioningListener#documentPartitioningChanged(IDocument)
   */
  public void documentPartitioningChanged(IDocument document) {
    if (getTreeViewer() != null) {
      getTreeViewer().setInput(new Long(System.currentTimeMillis()));
    }
  }

  public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    if (selection.size() == 1) {
      ITypedRegion region = (ITypedRegion) selection.getFirstElement();
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

    IFile file = Utils.findRelatedComponent(documentFile);

    IStructuredSelection canCreateSelection = filterSelection(file, selection);

    if (canCreateSelection.isEmpty() && selection.size() == 1) {
    	
      ILabelProvider provider = (ILabelProvider)getTreeViewer().getLabelProvider();
    	
      openAction.configure(file, provider.getText(selection.getFirstElement()));
      manager.add(openAction);

    } else {

      createAction.configure(file, canCreateSelection);
      manager.add(createAction);
    }

  }

  private IStructuredSelection filterSelection(IFile file, IStructuredSelection selection) {

    if (selection == null || selection.isEmpty()) {
      return selection;
    }

    if (file == null) {

      return StructuredSelection.EMPTY;
    }

    TapestryComponentModel model = getComponentModel(file);

    if (model == null || !model.isLoaded()) {

      return StructuredSelection.EMPTY;

    }

    ILabelProvider provider = (ILabelProvider) getTreeViewer().getLabelProvider();

    List collected = new ArrayList();

    for (Iterator iter = selection.iterator(); iter.hasNext();) {
      ITypedRegion element = (ITypedRegion) iter.next();
      String jwcId = provider.getText(element);
      if (jwcId.startsWith("$")) {
        continue;
      }

      if (alreadyHasJWCID(jwcId, file)) {
        continue;
      }
      collected.add(jwcId);

    }

    return new StructuredSelection(collected);

  }

  private boolean alreadyHasJWCID(String jwcid, IFile componentResource) {

    if (jwcid == null || "".equals(jwcid.trim()) || componentResource == null) {

      return false;

    }

    TapestryComponentModel model = getComponentModel(componentResource);

    return alreadyHasJWCID(jwcid, model);

  }

  private boolean alreadyHasJWCID(String jwcid, TapestryComponentModel model) {
    if (model != null) {

      PluginComponentSpecification spec = (PluginComponentSpecification) model.getComponentSpecification();

      if (spec != null) {
        return spec.getComponent(jwcid) != null;
      }
    }

    return false;
  }

  private TapestryComponentModel getComponentModel(IFile file) {

    try {
      TapestryProjectModelManager mgr = TapestryPlugin.getTapestryModelManager(file);

      IEditorPart part = Utils.getEditorFor(file);

      if (part != null && part instanceof SpindleMultipageEditor) {

        return (TapestryComponentModel) ((SpindleMultipageEditor) part).getModel();

      } else {

        return (TapestryComponentModel) mgr.getReadOnlyModel(file);

      }
    } catch (CoreException e) {

      return null;
    }

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
          if (type.equals(TapestryHTMLPartitionScanner.JWC_TAG) || type.equals(TapestryHTMLPartitionScanner.JWCID_TAG)) {
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
    Image notCreatedImage = TapestryImages.getSharedImage("property16.gif");
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
    public Image getImage(Object element) {

      String jwcid = null;

      try {
        jwcid = getJWCID((ITypedRegion) element);
      } catch (Exception e) {
      }

      if (jwcid != null && alreadyHasJWCID(jwcid, Utils.findRelatedComponent(documentFile))) {

        return jwcImage;

      }

      return notCreatedImage;
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

  protected class AlphabeticalSorter extends StringSorter {

    protected AlphabeticalSorter() {
      super();
    }

    public int compare(Viewer viewer, Object e1, Object e2) {
      LabelProvider provider = (LabelProvider) getTreeViewer().getLabelProvider();

      String s1 = stripDollars(provider.getText(e1));
      String s2 = stripDollars(provider.getText(e2));

      return super.compare(viewer, s1, s2);
    }

    private String stripDollars(String string) {

      string = string.trim();

      if (string.indexOf("$") >= 0) {
        if (string.startsWith("$")) {
          string = string.substring(1);
        }
        if (string.endsWith("$")) {
          string = string.substring(0, string.length() - 2);
        }
      }
      return string;

    }
  }

  class CreateContainedComponentAction extends Action {

    IFile modelFile;
    List jwcids;

    public CreateContainedComponentAction() {
      super();
      setToolTipText("create a contained component in the related .jwc file with the selected name");
    }

    public void configure(IFile model, IStructuredSelection selection) {
      this.modelFile = model;
      this.jwcids = selection.toList();

      if (jwcids.size() == 1) {
        String id = (String) jwcids.get(0);

        setText("create '" + id + "' in '" + documentFile.getFullPath().removeFileExtension().lastSegment() + ".jwc'");

      } else {

        setText("create multiple components in '" + documentFile.getFullPath().removeFileExtension().lastSegment() + ".jwc'");
      }

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

        getTreeViewer().setInput("dummy");

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

      for (Iterator iter = jwcids.iterator(); iter.hasNext();) {
        String element = (String) iter.next();
        Utils.createContainedComponentIn(element, chosen, model);

      }

      if (targetEditor == null) {

        TapestryPlugin.openTapestryEditor(model.getUnderlyingStorage());
      }

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

        for (Iterator iter = jwcids.iterator(); iter.hasNext();) {
          String element = (String) iter.next();
          Utils.createContainedComponentIn(element, chosen, model);

        }

        try {

          (new WorkspaceModifyOperation() {
            public void execute(final IProgressMonitor monitor) throws CoreException {

              Utils.saveModel(model, new NullProgressMonitor());

            }
          }).execute(new NullProgressMonitor());


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

      ErrorDialog.openError(TapestryPlugin.getDefault().getActiveWorkbenchShell(), null, null, status);

    }
  }


  /**
   * @author phraktle@imapmail.org    
   */
  class OpenComponentAction extends Action {

    IFile modelFile;
    String jwcid;

    public OpenComponentAction() {
      super();
      setToolTipText("Open component definition");
    }

    public void configure(IFile model, String jwcid) {
      this.modelFile = model;
      this.jwcid = jwcid;
      setText("Jump to: " + jwcid);
      setEnabled(model == null);
    }

    public void run() {
      IEditorPart editor = Utils.getEditorFor(modelFile);
      if (editor != null) {
        TapestryPlugin.getDefault().getActivePage().bringToTop(editor);
      } else {
        TapestryPlugin.openTapestryEditor(modelFile);
      }

      JWCMultipageEditor jwc = (JWCMultipageEditor) Utils.getEditorFor(modelFile);

      if (jwc != null) {

        ITapestryModel model = (ITapestryModel) jwc.getModel();

        if (!model.isLoaded()) {

          jwc.showPage(jwc.SOURCE_PAGE);

        } else {

          // had to change this from SpindleFormPage as the source page is not a SpindleFormPage!
          IPDEEditorPage currentPage = (IPDEEditorPage) jwc.getCurrentPage();
          ComponentsFormPage desiredPage = (ComponentsFormPage) jwc.getPage(jwc.COMPONENTS);

          if (currentPage != desiredPage) {
            jwc.showPage(jwc.COMPONENTS);
          }
          desiredPage.openTo(this.jwcid);
        }
      }
    }
  }

  class AlphabeticalSortingAction extends Action {

    private AlphabeticalSorter alphaSorter = new AlphabeticalSorter();

    public AlphabeticalSortingAction() {
      super();
      setText("Sort alphabetically");
      setToolTipText("Toggle alphabetical sorting");
      setImageDescriptor(TapestryImages.getImageDescriptor("alphab_sort_co.gif"));

      IPreferenceStore store = TapestryPlugin.getDefault().getPreferenceStore();

      boolean checked = store.getBoolean("AlphabeticalSorting.isChecked");
      valueChanged(checked, false);
    }

    public void run() {
      valueChanged(isChecked(), true);
    }

    private void valueChanged(boolean on, boolean store) {
      setChecked(on);
      TreeViewer viewer = getTreeViewer();
      viewer.setSorter(on ? alphaSorter : null);
      documentChanged(null);

      if (store)
        TapestryPlugin.getDefault().getPreferenceStore().setValue("AlphabeticalSorting.isChecked", on);
    }
  };

}