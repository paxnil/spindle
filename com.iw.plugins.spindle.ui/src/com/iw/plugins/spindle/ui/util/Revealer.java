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
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.viewers.ISelection;
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

/**
 *  Reveals IStorages in the workbench as editors are selected
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
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

        final ISelection useSelection = selection;
        Iterator enum = parts.iterator();
        while (enum.hasNext())
        {
            IWorkbenchPart part = (IWorkbenchPart) enum.next();

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

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part)
    {

        if (!PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.LINK_PACKAGES_TO_EDITOR))
            return;
        try
        {
            IEditorPart editor = (IEditorPart) part.getAdapter(IEditorPart.class);
            if (editor != null)
            {
                IStorage storage = null;
                IEditorInput input = editor.getEditorInput();
                if (input instanceof FileEditorInput)
                {

                    FileEditorInput fei = (FileEditorInput) input;
                    storage = fei.getStorage();

                } else if (input instanceof JarEntryEditorInput)
                {
                    JarEntryEditorInput jeei = (JarEntryEditorInput) input;
                    storage = jeei.getStorage();
                }
                if (storage != null && isTapestry(storage))
                    selectAndReveal(new StructuredSelection(storage), fCurrentWindow);

            }
        } catch (CoreException e)
        {
            // do nothing
        }
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowActivated(IWorkbenchWindow window)
    {
        register(window);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowClosed(IWorkbenchWindow window)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowDeactivated(IWorkbenchWindow window)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowOpened(IWorkbenchWindow window)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
     */
    public void pageActivated(IWorkbenchPage page)
    {
        register(page);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
     */
    public void pageClosed(IWorkbenchPage page)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
     */
    public void pageOpened(IWorkbenchPage page)
    {}

}
