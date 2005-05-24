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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors.spec.outline;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.xmen.internal.ui.text.XMLModelListener;
import org.xmen.internal.ui.text.XMLReconciler;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.documentsAndModels.IXMLModelProvider;
import com.iw.plugins.spindle.editors.spec.SpecEditor;
import com.iw.plugins.spindle.editors.util.DoubleClickSelection;
import com.iw.plugins.spindle.editors.util.XMLNodeLabelProvider;

/**
 * TODO Add Type comment
 * 
 * @author glongman@gmail.com
 *  
 */
public class XMLOutlinePage extends ContentOutlinePage implements XMLModelListener
{

  private SpecEditor fEditor;
  private Tree fTree;
  private TreeViewer treeViewer;
  private XMLNode fRoot;
  private Object[] fFlatChildren = new XMLNode[0];
  private Object[] fCorresponders = new XMLNode[0];

  public XMLOutlinePage(SpecEditor editor, IEditorInput input)
  {
    fEditor = editor;
    connect(input);
  }

  private void connect(IEditorInput input)
  {

    IDocumentProvider provider = fEditor.getDocumentProvider();

    //force creation of the document & the model.
    IDocument document = provider.getDocument(input);
    IXMLModelProvider modelProvider = UIPlugin.getDefault().getXMLModelProvider();
    XMLReconciler model = (modelProvider).getModel(document);
    if (model != null)
    {
      fRoot = model.getRoot();
      model.addListener(this);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xmen.internal.ui.text.XMLModelListener#modelChanged(org.xmen.internal.ui.text.XMLReconciler)
   */
  public void modelChanged(XMLReconciler reconciler)
  {
    setInput(reconciler.getRoot());
  }

  private void disconnect()
  {
    IEditorInput input = fEditor.getEditorInput();
    IDocumentProvider provider = null;
    if (input instanceof IFileEditorInput)
    {
      provider = UIPlugin.getDefault().getSpecFileDocumentProvider();
    } else
    {
      provider = UIPlugin.getDefault().getSpecStorageDocumentProvider();
    }
    IDocument document = provider.getDocument(input);
    IXMLModelProvider modelProvider = UIPlugin.getDefault().getXMLModelProvider();
    XMLReconciler model = modelProvider.getModel(document);
    if (model != null)
    {
      model.removeListener(this);
    }
  }

  public void createControl(Composite parent)
  {
    fTree = new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    treeViewer = new TreeViewer(fTree);
    treeViewer.addSelectionChangedListener(this);
    treeViewer.setContentProvider(createContentProvider());
    treeViewer.setLabelProvider(createLabelProvider());
    treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
    treeViewer.setUseHashlookup(true);
    treeViewer.addDoubleClickListener(new IDoubleClickListener()
    {
      public void doubleClick(DoubleClickEvent event)
      {
        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
        if (!selection.isEmpty())
          fireSelectionChanged(new DoubleClickSelection(selection.getFirstElement()));
      }
    });
    setInput(fRoot);
  }

  public void dispose()
  {
    disconnect();
    super.dispose();
  }

  public void setInput(final Object input)
  {

    if (fTree == null || fTree.isDisposed())
      return;

    fRoot = (XMLNode) input;

    if (fRoot == null)
    {
      treeViewer.setInput(null);
      return;
    }

    Display d = fTree.getDisplay();
    d.asyncExec(new Runnable()
    {
      public void run()
      {
        try
        {
          ISelection oldSelection = getSelection();

          treeViewer.setInput(fRoot);
          treeViewer.setSelection(oldSelection);

        } catch (RuntimeException e)
        {
          UIPlugin.log_it(e);
        }
      }
    });
  }

  protected ITreeContentProvider createContentProvider()
  {
    return new OutlineContentProvider();
  }

  protected ILabelProvider createLabelProvider()
  {
    return new XMLNodeLabelProvider();
  }

  public Control getControl()
  {
    return treeViewer != null ? treeViewer.getControl() : null;
  }

  public void setFocus()
  {
    if (treeViewer != null)
      treeViewer.getTree().setFocus();
  }

  public ISelection getSelection()
  {
    if (treeViewer == null)
      return StructuredSelection.EMPTY;
    return treeViewer.getSelection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection(ISelection selection)
  {
    if (!selection.isEmpty() && selection instanceof IStructuredSelection)
    {
      Object selected = ((IStructuredSelection) selection).getFirstElement();
      if (selected instanceof IRegion && fRoot != null)
      {
        int documentOffset = ((IRegion) selected).getOffset();
        Object found = null;
        for (int i = 0; i < fFlatChildren.length; i++)
        {
          Position p = (Position) fFlatChildren[i];
          if (p.offset <= documentOffset && documentOffset < p.offset + p.length)
          {
            found = p;
          }
        }
        if (found == null)
        {
          int index = 0;
          boolean exists = false;
          for (; index < fCorresponders.length; index++)
          {
            Position p = (Position) fCorresponders[index];
            if (p != null && p.offset <= documentOffset
                && documentOffset < p.offset + p.length)
            {
              exists = true;
              break;
            }
          }
          if (exists)
            found = fFlatChildren[index];
        }
        if (found != null)
        {
          treeViewer.setSelection(new StructuredSelection(found));
        } else if (treeViewer != null && !treeViewer.getControl().isDisposed())
        {
          treeViewer.setSelection(StructuredSelection.EMPTY);
        }
      }
      super.setSelection(selection);
    }

  }

  public class OutlineContentProvider implements ITreeContentProvider
  {
    public Object[] getElements(Object obj)
    {
      if (fRoot != null)
      {
        Object[] result = fRoot.getChildren(fRoot);
        addAll(result);
        return result;
      }
      return new Object[]{};
    }
    public Object[] getChildren(Object obj)
    {
      if (obj instanceof XMLNode)
      {
        Object[] result = ((XMLNode) obj).getChildren(obj);
        addAll(result);
        return result;
      }

      return new Object[0];
    }
    public boolean hasChildren(Object obj)
    {
      return getChildren(obj).length > 0;
    }
    public Object getParent(Object obj)
    {
      if (obj == fRoot)
        return null;
      return ((XMLNode) obj).getParent();
    }
    public void dispose()
    {
    }

    private void addAll(Object[] elements)
    {
      if (elements == null || elements.length == 0)
        return;

      if (fFlatChildren.length == 0)
      {
        fFlatChildren = elements;
        fCorresponders = new Object[elements.length];
        for (int i = 0; i < elements.length; i++)
          fCorresponders[i] = ((XMLNode) elements[i]).getCorrespondingNode();
        return;
      }

      Object[] expandedFlat = new Object[fFlatChildren.length + elements.length];
      System.arraycopy(fFlatChildren, 0, expandedFlat, 0, fFlatChildren.length);
      System.arraycopy(elements, 0, expandedFlat, fFlatChildren.length, elements.length);
      Object[] expandedCorresponders = new Object[fCorresponders.length + elements.length];
      System
          .arraycopy(fCorresponders, 0, expandedCorresponders, 0, fCorresponders.length);
      for (int i = 0; i < elements.length; i++)
      {
        expandedCorresponders[fCorresponders.length + i] = ((XMLNode) elements[i])
            .getCorrespondingNode();
      }

      fFlatChildren = expandedFlat;
      fCorresponders = expandedCorresponders;

    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
      fFlatChildren = new XMLNode[0];
      fCorresponders = new XMLNode[0];
    }

  }

}