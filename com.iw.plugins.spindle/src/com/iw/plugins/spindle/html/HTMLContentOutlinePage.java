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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.iw.plugins.spindle.TapestryImages;
import com.iw.plugins.spindle.TapestryPlugin;

import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.ui.ChooseComponentDialog;
import com.iw.plugins.spindle.ui.RequiredSaveEditorAction;
import com.iw.plugins.spindle.util.StringSorter;
import com.iw.plugins.spindle.util.TapestryLookup;
import com.iw.plugins.spindle.util.Utils;

public class HTMLContentOutlinePage extends ContentOutlinePage implements IDocumentPartitioningListener, IDocumentListener {

  IDocument document;

  IFile documentFile = null;
  TapestryHTMLEditor editor;

  private CreateContainedComponentAction createAction = new CreateContainedComponentAction();
  private ContainedComponentAlreadyExistsAction alreadyHasAction = new ContainedComponentAlreadyExistsAction();

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
    viewer.setContentProvider(new ContentProvider());
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

  private void fillContextMenu(IMenuManager manager) {
    TapestryComponentModel model = findRelatedComponent(documentFile);
    if (model == null) {
      return;
    }
    TreeViewer viewer = (TreeViewer) getTreeViewer();
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    if (selection != null && selection.size() > 0) {
      ILabelProvider provider = (ILabelProvider) viewer.getLabelProvider();
      String selectedJwcid = provider.getText(selection.getFirstElement());
      if (selectedJwcid.startsWith("$")) {
        return;
      }
      PluginComponentSpecification spec = (PluginComponentSpecification) model.getComponentSpecification();
      if (spec == null) {
        return;
      }
      if (spec.getComponent(selectedJwcid) == null) {
        createAction.configure(model, selectedJwcid);
        manager.add(createAction);
      } else {
        alreadyHasAction.configure(selectedJwcid);
        manager.add(alreadyHasAction);
      }
    }

  }

  private TapestryComponentModel findRelatedComponent(IFile aFile) {
    if (documentFile != null) {

      IContainer parent = documentFile.getParent();
      String name = documentFile.getFullPath().removeFileExtension().lastSegment();
      String jwcName = name + ".jwc";
      IResource componentResource = parent.findMember(jwcName);
      if (componentResource != null && componentResource instanceof IStorage) {
        return (TapestryComponentModel) TapestryPlugin.getTapestryModelManager().getModel((IStorage) componentResource);
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
    if (TapestryPartitionScanner.JWCID_TAG.equals(type)) {
      start = "jwcid=\"";
    } else if (TapestryPartitionScanner.JWC_TAG.equals(type)) {
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
      ArrayList results = new ArrayList();
      if (document != null) {

        ITypedRegion[] partitions = null;
        try {
          partitions = document.computePartitioning(0, document.getLength() - 1);
        } catch (BadLocationException ex) {
          return new Object[] { "error occured scanning source" };
        }
        for (int i = 0; i < partitions.length; i++) {
          String type = partitions[i].getType();
          if (type.equals(TapestryPartitionScanner.JWC_TAG) || type.equals(TapestryPartitionScanner.JWCID_TAG)) {
          	if (findJWCID(partitions[i]) != null) {
            	results.add(partitions[i]);
          	}
          }
        }
      }
      return results.toArray();
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

    TapestryComponentModel model;
    String jwcid;

    public CreateContainedComponentAction() {
      super();
      setToolTipText("create a contained component in the related .jwc file with the selected name");
    }

    public void configure(TapestryComponentModel model, String jwcid) {
      this.model = model;
      this.jwcid = jwcid;

      setText("create '" + jwcid + "' in '" + documentFile.getFullPath().removeFileExtension().lastSegment() + ".jwc");
    }

    public void run() {

      SpindleMultipageEditor targetEditor = (SpindleMultipageEditor) Utils.getEditorFor(model);
      if (targetEditor != null && targetEditor.isDirty()) {
        RequiredSaveEditorAction saver = new RequiredSaveEditorAction(targetEditor);
        if (!saver.save()) {
          return;
        }
        model = (TapestryComponentModel) targetEditor.getModel();
      }
      if (!model.isLoaded()) {
        parseError();
        return;
      }

      IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
      ChooseComponentDialog dialog =
        new ChooseComponentDialog(
          getTreeViewer().getControl().getShell(),
          jproject,
          "Create in " + documentFile.getFullPath().removeFileExtension().lastSegment() + ".jwc",
          "Choose the Contained Component to create",
          true,
          TapestryLookup.ACCEPT_COMPONENTS);
      dialog.create();
      if (dialog.open() == dialog.OK) {
        try {
          String chosen = dialog.getResultComponent();
          if (chosen == null || "".equals(chosen.trim())) {
            return;
          }
          Utils.createContainedComponentIn(jwcid, chosen, model);
          if (targetEditor == null) {
            TapestryPlugin.openTapestryEditor(model);
          }
          if (editor != null) {
            editor.parseForProblems();
          }
        } catch (Exception e) {
          e.printStackTrace();
          TapestryPlugin.getDefault().logException(e);
        }
      }

    }

    private void parseError() {
      Status status =
        new Status(
          IStatus.ERROR,
          TapestryPlugin.getDefault().getPluginId(),
          IStatus.OK,
          "Abort, target component is has parse errors",
          null);
      ErrorDialog.openError(TapestryPlugin.getDefault().getActiveWorkbenchShell(), null, null, status);
    }
  }

  class ContainedComponentAlreadyExistsAction extends Action {

    public ContainedComponentAlreadyExistsAction() {
      super();
    }

    public void configure(String jwcid) {
      setText("'" + jwcid + "' already exists in " + documentFile.getFullPath().removeFileExtension().lastSegment() + ".jwc");
    }

    public void run() { /* do nothing*/
    }
  }
}