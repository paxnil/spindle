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

package com.iw.plugins.spindle.editors;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A reconciler that is also activated on editor activation.
 */
public class Reconciler extends MonoReconciler implements IPartListener
{

    /** The reconciler's editor */
    private ITextEditor fTextEditor;

    /**
     * Creates a new reconciler.
     */
    public Reconciler(ITextEditor editor, IReconcilingStrategy strategy, boolean isIncremental)
    {
        super(strategy, isIncremental);
        fTextEditor = editor;
    }

    /*
     * @see IReconciler#install(ITextViewer)
     */
    public void install(ITextViewer textViewer)
    {
        super.install(textViewer);

        IWorkbenchPartSite site = fTextEditor.getSite();
        IWorkbenchWindow window = site.getWorkbenchWindow();
        window.getPartService().addPartListener(this);
    }

    /*
     * @see IReconciler#uninstall()
     */
    public void uninstall()
    {

        IWorkbenchPartSite site = fTextEditor.getSite();
        IWorkbenchWindow window = site.getWorkbenchWindow();
        window.getPartService().removePartListener(this);

        super.uninstall();
    }

    /*
     * @see IPartListener#partActivated(IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part)
    {
        if (part == fTextEditor)
            Reconciler.this.forceReconciling();
    }

    /*
     * @see IPartListener#partBroughtToTop(IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part)
    {}

    /*
     * @see IPartListener#partClosed(IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part)
    {}

    /*
     * @see IPartListener#partDeactivated(IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part)
    {}

    /*
     * @see IPartListener#partOpened(IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part)
    {}

}
