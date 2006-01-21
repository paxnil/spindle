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

package com.iw.plugins.spindle.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.builder.TapestryBuilder;
import com.iw.plugins.spindle.core.util.JarEntryFileUtil;
import com.iw.plugins.spindle.core.util.JarEntryFileUtil.JarEntryFileWrapper;

/**
 * Reveals IStorages in the workbench as editors are selected
 * 
 * @author glongman@gmail.com
 * 
 */
public class Revealer implements IWindowListener, IPageListener, IPartListener
{
  private static Revealer instance;

  private static List Known_Extensions = Arrays.asList(TapestryBuilder.KnownExtensions);

  public static synchronized void start()
  {
    if (instance != null)
      throw new IllegalStateException("revealer already started!");

    instance = new Revealer();
  }

  public static void selectAndReveal(ISelection selection, IWorkbenchWindow window)
  {
    selectAndReveal(selection, window, null);
  }
  public static void selectAndReveal(
      ISelection selection,
      IWorkbenchWindow window,
      IJavaProject jproject)
  {
    // validate the input
    if (window == null || selection == null || selection.isEmpty())
      return;
    IWorkbenchPage page = window.getActivePage();
    if (page == null)
      return;

    // get all the view and editor parts
    List parts = new ArrayList();
    IWorkbenchPartReference refs[] = page.getViewReferences();
    for (int i = 0; i < refs.length; i++)
    {
      IWorkbenchPart part = refs[i].getPart(false);
      if (part != null)
        parts.add(part);
    }
    refs = page.getEditorReferences();
    for (int i = 0; i < refs.length; i++)
    {
      if (refs[i].getPart(false) != null)
        parts.add(refs[i].getPart(false));
    }

    final ISelection useSelection = checkSelectionForJarEntryFile(selection, jproject);
    Iterator enumeration = parts.iterator();
    while (enumeration.hasNext())
    {
      IWorkbenchPart part = (IWorkbenchPart) enumeration.next();

      // get the part's ISetSelectionTarget implementation
      ISetSelectionTarget target = null;
      if (part instanceof ISetSelectionTarget)
        target = (ISetSelectionTarget) part;
      else
        target = (ISetSelectionTarget) part.getAdapter(ISetSelectionTarget.class);

      if (target != null)
      {
        // select and reveal resource
        final ISetSelectionTarget finalTarget = target;
        window.getShell().getDisplay().asyncExec(new Runnable()
        {
          public void run()
          {
            finalTarget.selectReveal(useSelection);
          }
        });
      }
    }
  }

  /**
   * @param useSelection
   * @return
   */
  private static ISelection checkSelectionForJarEntryFile(
      ISelection useSelection,
      IJavaProject jproject)
  {
    if (useSelection.isEmpty() || !(useSelection instanceof IStructuredSelection))
      return useSelection;

    IStructuredSelection structured = (IStructuredSelection) useSelection;
    Object first = structured.getFirstElement();
    if (structured.size() > 1 || !(first instanceof JarEntryFile))
      return useSelection;

    JarEntryFileWrapper wrapped = (JarEntryFileWrapper)JarEntryFileUtil.wrap((JarEntryFile) first);
    IPath path = new Path(wrapped.getName()).removeFileExtension();
    String name = path.lastSegment();

    IPackageFragment[] fragments = null;
    try
    {
      if (jproject != null)
      {
        
        IPackageFragment frag = JarEntryFileUtil.getPackageFragment(jproject, wrapped);
        if (frag != null)
          fragments = new IPackageFragment[]{frag};

      } else
      {
        fragments = JarEntryFileUtil.getPackageFragments(ResourcesPlugin
            .getWorkspace()
            .getRoot(), wrapped);
      }
      if (fragments.length != 1)
        return useSelection;

      //check to see if there is an IClassFile in the package
      IJavaElement[] children = fragments[0].getChildren();
      if (children.length > 0)
      {
        IClassFile revealInstead = null;
        for (int i = 0; i < children.length; i++)
        {
          if (children[i].getElementType() != IJavaElement.CLASS_FILE)
            continue;

          revealInstead = (IClassFile) children[i];
          String temp = revealInstead.getElementName();
          temp = temp.substring(0, temp.length() - 6);
          if (temp.equals(name))
            return new StructuredSelection(revealInstead);
        }
        if (revealInstead != null)
          return new StructuredSelection(revealInstead);
      }
      return new StructuredSelection(fragments[0]);

    } catch (CoreException e)
    {
      UIPlugin.log(e);
    }
    return useSelection;
  }

  private IWorkbenchWindow fCurrentWindow;
  private IWorkbenchPage fCurrentPage;

  private Revealer()
  {
    UIPlugin.getDefault().getWorkbench().addWindowListener(this);
    //        register(UIPlugin.getDefault().getActiveWorkbenchWindow());
  }

  private void register(IWorkbenchWindow window)
  {
    if (fCurrentWindow != null)
      fCurrentWindow.removePageListener(this);
    window.addPageListener(this);
    fCurrentWindow = window;
    register(fCurrentWindow.getActivePage());
  }

  private void register(IWorkbenchPage page)
  {
    if (fCurrentPage != null)
      fCurrentPage.removePartListener(this);
    page.addPartListener(this);
    fCurrentPage = page;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
   */
  public void partActivated(IWorkbenchPart part)
  {

    if (!PreferenceConstants.getPreferenceStore().getBoolean(
        PreferenceConstants.LINK_PACKAGES_TO_EDITOR))
      return;
//    try
//    {
      IEditorPart editor = (IEditorPart) part.getAdapter(IEditorPart.class);
      if (editor != null)
      {
        IStorage storage = null;
        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput)
        {
          //TODO use platform adapters
          FileEditorInput fei = (FileEditorInput) input;
          storage = fei.getFile();

        } else if (input instanceof JarEntryEditorInput)
        {
//          TODO use platform adapters
          JarEntryEditorInput jeei = (JarEntryEditorInput) input;
          storage = JarEntryFileUtil.wrap(jeei.getStorage());
        }
        if (storage != null && isTapestry(storage))
          selectAndReveal(new StructuredSelection(storage), fCurrentWindow);

      }
//    } catch (CoreException e)
//    {
//      // do nothing
//    }
  }

  /**
   * @param string
   * @return
   */
  private boolean isTapestry(IStorage storage)
  {
    String extension = storage.getFullPath().getFileExtension();
    return Known_Extensions.contains(extension);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
   */
  public void partBroughtToTop(IWorkbenchPart part)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
   */
  public void partClosed(IWorkbenchPart part)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
   */
  public void partDeactivated(IWorkbenchPart part)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
   */
  public void partOpened(IWorkbenchPart part)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
   */
  public void windowActivated(IWorkbenchWindow window)
  {
    register(window);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
   */
  public void windowClosed(IWorkbenchWindow window)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
   */
  public void windowDeactivated(IWorkbenchWindow window)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
   */
  public void windowOpened(IWorkbenchWindow window)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
   */
  public void pageActivated(IWorkbenchPage page)
  {
    register(page);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
   */
  public void pageClosed(IWorkbenchPage page)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
   */
  public void pageOpened(IWorkbenchPage page)
  {
  }

}