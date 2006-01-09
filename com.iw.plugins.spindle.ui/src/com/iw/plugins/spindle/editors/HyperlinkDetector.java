package com.iw.plugins.spindle.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.texteditor.ITextEditor;

import com.iw.plugins.spindle.editors.spec.actions.OpenDeclarationAction;
import com.sun.java_cup.internal.action_part;

public class HyperlinkDetector implements IHyperlinkDetector
{
    private ITextEditor fTextEditor;
    
    public HyperlinkDetector(ITextEditor editor) {
        Assert.isNotNull(editor);
        fTextEditor= editor;
    }

    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks)
    {
        if (region == null || canShowMultipleHyperlinks || !(fTextEditor instanceof Editor))
            return null;
        
        IAction open = fTextEditor.getAction(OpenDeclarationAction.ACTION_ID);
        if (open == null)
            return null;
        
        if (open.set
        
        
    }

}
