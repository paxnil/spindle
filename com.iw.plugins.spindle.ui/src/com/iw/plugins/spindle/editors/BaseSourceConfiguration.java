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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

import com.iw.plugins.spindle.ui.text.IColorConstants;
import com.iw.plugins.spindle.ui.text.ISpindleColorManager;

public abstract class BaseSourceConfiguration extends SourceViewerConfiguration implements IColorConstants
{

    private ISpindleColorManager fColorManager;
    private ITextEditor fTextEditor;

    public BaseSourceConfiguration(ISpindleColorManager colorManager, ITextEditor editor)
    {
        fColorManager = colorManager;
        fTextEditor = editor;
    }

    /*
     * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
     */
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
    {
        return new AnnotationHover();
    }

    protected ITextEditor getEditor()
    {
        return fTextEditor;
    }
    
    protected ISpindleColorManager getColorManager() {
        return fColorManager;
    }

    public IReconciler getReconciler(ISourceViewer sourceViewer)
    {
        if (getEditor() != null && getEditor().isEditable())
        {
            Reconciler reconciler =
                new Reconciler(getEditor(), new ReconcilingStrategy(getEditor()), false);
            reconciler.setProgressMonitor(new NullProgressMonitor());
            reconciler.setDelay(500);
            return reconciler;
        }
        return null;
    }

}