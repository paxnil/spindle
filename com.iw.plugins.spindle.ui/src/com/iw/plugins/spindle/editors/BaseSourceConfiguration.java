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

import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.DefaultAutoIndentStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.formatter.XMLAutoIndentStrategy;
import com.iw.plugins.spindle.ui.util.ToolTipHandler;

public abstract class BaseSourceConfiguration extends TextSourceViewerConfiguration
{

    protected Editor fEditor;

    protected IPreferenceStore fPreferenceStore;

    private String[] fIndentPrefixes;

    public BaseSourceConfiguration(Editor editor, IPreferenceStore preferenceStore)
    {
        fEditor = editor;
        fPreferenceStore = preferenceStore;
    }

    /*
     * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
     */
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
    {
        return new ProblemAnnotationHover();
    }

    protected Editor getEditor()
    {
        return fEditor;
    }

    public IReconciler getReconciler(ISourceViewer sourceViewer)
    {
        if (getEditor() != null)
        {
            Reconciler reconciler = new Reconciler(getEditor(),
                    new ReconcilingStrategy(getEditor()), false);
            reconciler.setProgressMonitor(new NullProgressMonitor());
            reconciler.setDelay(500);
            return reconciler;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer,
     *      java.lang.String)
     */
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
    {
        Editor editor = getEditor();
        if (editor.getEditorInput() instanceof JarEntryEditorInput)
            return null;
        return new ProblemAnnotationTextHover((Editor) getEditor());
    }

    /*
     * @see SourceViewerConfiguration#getInformationControlCreator(ISourceViewer)
     * @since 2.0
     */
    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer)
    {
        return new IInformationControlCreator()
        {
            public IInformationControl createInformationControl(Shell parent)
            {
                return new DefaultInformationControl(parent, SWT.NONE,
                        new ToolTipHandler.TooltipPresenter());
            }
        };
    }

    public IAutoIndentStrategy getAutoIndentStrategy(ISourceViewer sourceViewer, String contentType)
    {
        if (contentType == XMLPartitionScanner.XML_COMMENT
                || contentType == XMLPartitionScanner.XML_CDATA)
            return new DefaultAutoIndentStrategy();
        return new XMLAutoIndentStrategy(UIPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType)
    {
        if (contentType == XMLPartitionScanner.XML_COMMENT
                || contentType == XMLPartitionScanner.XML_CDATA)
            return super.getIndentPrefixes(sourceViewer, contentType);

        if (fIndentPrefixes == null)
        {
            int spaces = fPreferenceStore.getInt(PreferenceConstants.FORMATTER_TAB_SIZE);
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < spaces; i++)
                buf.append(' ');
            fIndentPrefixes = new String[]
            { "\t", buf.toString() };
        }
        return fIndentPrefixes;
    }

    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer)
    {       
        return new IHyperlinkDetector [] {new HyperlinkDetector(fEditor)};
    }

    public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer)
    {      
        return new DefaultHyperlinkPresenter(new RGB(0, 0, 255));
    }

    public int getHyperlinkStateMask(ISourceViewer sourceViewer)
    {       
        return super.getHyperlinkStateMask(sourceViewer);
    }

}