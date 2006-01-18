/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */

package com.iw.plugins.spindle.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.source.ISourceLocation;

public class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension
{

    private AbstractTextEditor fEditor;

    private IDocumentProvider fDocumentProvider;

    private IProgressMonitor fProgressMonitor;

    public ReconcilingStrategy(AbstractTextEditor editor)
    {
        fEditor = editor;
        fDocumentProvider = editor.getDocumentProvider();
    }

    private IProblemCollector getProblemCollector()
    {
        IAnnotationModel model = fDocumentProvider.getAnnotationModel(fEditor.getEditorInput());
        if (model instanceof IProblemCollector)
            return (IProblemCollector) model;
        return new IProblemCollector()
        {

            public void addProblem(IProblem problem)
            {
                // TODO Auto-generated method stub

            }

            public void addProblem(int severity, ISourceLocation location, String message,
                    boolean isTemporary, int code)
            {
                // TODO Auto-generated method stub

            }

            public void addProblem(IStatus status, ISourceLocation location, boolean isTemporary)
            {
                // TODO Auto-generated method stub

            }

            public IProblem[] getProblems()
            {
                // TODO Auto-generated method stub
                return new IProblem[] {};
            }

            public void beginCollecting()
            {
                // TODO Auto-generated method stub

            }

            public void endCollecting()
            {
                // TODO Auto-generated method stub

            }

        };
    }

    private void reconcile()
    {
        if (!(fEditor instanceof IReconcileWorker))
            return;

        IReconcileWorker selfReconciler = (IReconcileWorker) fEditor;

        if (!selfReconciler.isReadyToReconcile())
            return;
        try
        {
            IProblemCollector collector = getProblemCollector();

            if (collector == null)
                return;

            // reconcile
            synchronized (selfReconciler)
            {
                selfReconciler.reconcile(collector, fProgressMonitor);
            }

        }
        catch (Exception x)
        {
            UIPlugin.log(x);
        }
    }

    /*
     * @see IReconcilingStrategy#reconcile(IRegion)
     */
    public void reconcile(IRegion partition)
    {
        reconcile();
    }

    /*
     * @see IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
     */
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
    {
        reconcile();
    }

    /*
     * @see IReconcilingStrategy#setDocument(IDocument)
     */
    public void setDocument(IDocument document)
    {
    }

    /*
     * @see IReconcilingStrategyExtension#setProgressMonitor(IProgressMonitor)
     */
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        fProgressMonitor = monitor;
    }

    /*
     * @see IReconcilingStrategyExtension#initialReconcile()
     */
    public void initialReconcile()
    {
        reconcile();
    }

}