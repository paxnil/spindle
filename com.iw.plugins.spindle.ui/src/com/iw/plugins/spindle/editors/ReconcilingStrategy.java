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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.source.IProblemCollector;

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
        return null;
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

        } catch (Exception x)
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
    {}

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
        //Do nothing
    }

}
