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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *  phraktle@imapmail.org
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.template;

import java.util.ArrayList;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.ui.util.ImplicitIdMatcher;
import com.iw.plugins.spindle.ui.util.StringSorter;

/**
 * @author glongman@intelligentworks.com
 * @version $Id: TemplateContentOutlinePage.java,v 1.4 2003/11/09 21:24:43
 *          glongman Exp $
 * 
 * Copyright 2003, Intelligent Works Inc. All Rights Reserved.
 */
public class TemplateContentOutlinePage extends ContentOutlinePage
    implements
      IDocumentPartitioningListener,
      IDocumentListener
{

  IDocument fDocument;
  TemplateEditor fEditor;
  ContentProvider contentProvider = new ContentProvider();
  ImplicitIdMatcher fMatcher = new ImplicitIdMatcher();

  protected TemplateContentOutlinePage(TemplateEditor editor)
  {
    super();
    this.fEditor = editor;
  }

  public void setDocument(IDocument document)
  {
    this.fDocument = document;
    document.addDocumentPartitioningListener(this);
    document.addDocumentListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    super.createControl(parent);
    TreeViewer viewer = getTreeViewer();
    viewer.setContentProvider(contentProvider);
    viewer.setLabelProvider(new LabelProvider());
    MenuManager popupMenuManager = new MenuManager();
    IMenuListener listener = new IMenuListener()
    {
      public void menuAboutToShow(IMenuManager mng)
      {
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

  private void registerToolbarActions()
  {
    IToolBarManager toolBarManager = getSite().getActionBars().getToolBarManager();
    if (toolBarManager != null)
    {
      Action action = new AlphabeticalSortingAction();
      toolBarManager.add(action);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.IDocumentPartitioningListener#documentPartitioningChanged(org.eclipse.jface.text.IDocument)
   */
  public void documentPartitioningChanged(IDocument document)
  {
    if (getTreeViewer() != null)
      getTreeViewer().setInput(new Long(System.currentTimeMillis()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event)
  {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    if (selection.size() == 1)
    {
      try
      {
        ITypedRegion region = (ITypedRegion) selection.getFirstElement();
        fireSelectionChanged(new StructuredSelection(new Object[]{findJWCID(region)}));
      } catch (BadLocationException e)
      {
        //swallow it
      }
    }
  }

  private void fillContextMenu(IMenuManager manager)
  {
  }

  private String getJWCID(ITypedRegion region)
  {
    try
    {
      Position p = findJWCID(region);
      if (p == null)
      {
        return null;
      }
      return fDocument.get(p.getOffset(), p.getLength());
    } catch (BadLocationException blex)
    {
      return "error";
    }
  }

  private Position findJWCID(ITypedRegion region) throws BadLocationException
  {
    if (region == null)
      return null;

    if (!region.getType().equals(TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE))
      return null;

    int start = region.getOffset() + 1;
    int length = region.getLength() - 1;
    String value = fDocument.get(start, length);
    if (value.endsWith("\"") || value.endsWith("'"))
      length -= 1;

    return new Position(start, length);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
   */
  /**
   * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
   */
  public void documentAboutToBeChanged(DocumentEvent arg0)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
   */
  /**
   * @see IDocumentListener#documentChanged(DocumentEvent)
   */
  public void documentChanged(DocumentEvent event)
  {
    TreeViewer viewer = getTreeViewer();
    if (viewer != null)
    {
      getTreeViewer().setInput(new Long(System.currentTimeMillis()));
    }
  }

  protected class ContentProvider implements ITreeContentProvider
  {

    ArrayList contents = null;

    public Object[] getChildren(Object element)
    {
      return null;
    }

    public Object getParent(Object element)
    {
      return null;
    }

    public boolean hasChildren(Object element)
    {
      return false;
    }

    public Object[] getElements(Object ignored)
    {
      getTreeViewer().setSelection(StructuredSelection.EMPTY);
      if (contents == null)
        contents = new ArrayList();

      ArrayList oldContents = (ArrayList) contents.clone();
      //TODO why the heck am I keeping the old contents?
      ArrayList addedList = new ArrayList();
      contents.clear();
      if (fDocument != null)
      {
        ITypedRegion[] partitions = null;
        try
        {
          partitions = fDocument.computePartitioning(0, fDocument.getLength() - 1);
        } catch (BadLocationException ex)
        {
          return new Object[]{"error occured scanning source"};
        }
        for (int i = 0; i < partitions.length; i++)
        {
          oldContents.remove(partitions[i]);
          try
          {
            if (findJWCID(partitions[i]) != null)
            {
              if (!oldContents.contains(partitions[i]))
                addedList.add(partitions[i]);

              contents.add(partitions[i]);
            }
          } catch (BadLocationException e)
          {
            //swallow it
          }

        }
      }
      return contents.toArray();
    }

    public void dispose()
    {
    }

    public void inputChanged(Viewer arg0, Object oldSource, Object newSource)
    {
    }

  }

  protected class LabelProvider implements ILabelProvider
  {
    Image notCreatedImage = Images.getSharedImage("property16.gif");
    Image jwcImage = Images.getSharedImage("component16.gif");

    /**
     * @see ILabelProvider#getImage(Object)
     */
    public Image getImage(Object element)
    {
      String jwcid = null;

      try
      {
        jwcid = getJWCID((ITypedRegion) element);
      } catch (Exception e)
      {}

      if (jwcid != null)
      {
        IComponentSpecification component = (IComponentSpecification) fEditor
            .getSpecification();

        if (component != null)
        {
          if (component.getComponent(jwcid) != null || isImplicitComponent(jwcid))
          {
            return jwcImage;

          }
        }
      }
      return notCreatedImage;
    }

    private boolean isImplicitComponent(String jwcid)
    {
      return fMatcher.isMatch(jwcid) && fMatcher.getSimpleType() != null;
    }

    /**
     * @see ILabelProvider#getText(Object)
     */
    public String getText(Object element)
    {
      if (element instanceof String)
        return (String) element;

      if (element instanceof ITypedRegion)
        return getJWCID((ITypedRegion) element);

      return null;
    }

    /**
     * @see IBaseLabelProvider#addListener(ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener arg0)
    {
    }

    /**
     * @see IBaseLabelProvider#dispose()
     */
    public void dispose()
    {
      //shared images are disposed by the Plugin
    }

    /**
     * @see IBaseLabelProvider#isLabelProperty(Object, String)
     */
    public boolean isLabelProperty(Object arg0, String arg1)
    {
      return false;
    }

    /**
     * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener arg0)
    {
    }

  }

  protected class AlphabeticalSorter extends StringSorter
  {

    protected AlphabeticalSorter()
    {
      super();
    }

    public int compare(Viewer viewer, Object e1, Object e2)
    {
      LabelProvider provider = (LabelProvider) getTreeViewer().getLabelProvider();

      String s1 = stripDollars(provider.getText(e1));
      String s2 = stripDollars(provider.getText(e2));

      return super.compare(viewer, s1, s2);
    }

    private String stripDollars(String string)
    {
      string = string.trim();

      if (string.indexOf("$") >= 0)
      {
        if (string.startsWith("$"))
        {
          string = string.substring(1);
        }
        if (string.endsWith("$"))
        {
          string = string.substring(0, string.length() - 2);
        }
      }
      return string;

    }
  }

  class AlphabeticalSortingAction extends Action
  {
    private AlphabeticalSorter alphaSorter = new AlphabeticalSorter();
    public AlphabeticalSortingAction()
    {
      super();
      setText(UIPlugin.getString("template-content-outline-sort"));
      setToolTipText(UIPlugin.getString("template-content-outline-sort-toggle"));
      setImageDescriptor(Images.getImageDescriptor("alphab_sort_co.gif"));

      IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();

      boolean checked = store.getBoolean("AlphabeticalSorting.isChecked");
      valueChanged(checked, false);
    }

    public void run()
    {
      valueChanged(isChecked(), true);
    }

    private void valueChanged(boolean on, boolean store)
    {
      setChecked(on);
      TreeViewer viewer = getTreeViewer();
      viewer.setSorter(on ? alphaSorter : null);
      documentChanged(null);

      if (store)
        UIPlugin.getDefault().getPreferenceStore().setValue(
            "AlphabeticalSorting.isChecked",
            on);
    }
  };

}