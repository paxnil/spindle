package com.iw.plugins.spindle.editors;

import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.editors.actions.BaseEditorAction;
import com.iw.plugins.spindle.editors.spec.actions.OpenDeclarationAction;

public class HyperlinkDetector implements IHyperlinkDetector
{
    private Editor fEditor;
    private IDocument fDocument;    
    
    public HyperlinkDetector(Editor editor) {
        Assert.isNotNull(editor);
        fEditor= editor;
        IEditorInput editorInput = fEditor.getEditorInput();
        IDocumentProvider documentProvider = fEditor.getDocumentProvider();
        fDocument = documentProvider.getDocument(editorInput);
    }

    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks)
    {
        if (region == null || canShowMultipleHyperlinks)
            return null;
        
        IRegion covered = computeCoverage(region.getOffset());
        if (covered == null) 
            return null;        
        
        IAction open = fEditor.getAction(OpenDeclarationAction.ACTION_ID);
        if (open == null || !(open instanceof BaseEditorAction))
            return null;
        
        if (!((BaseEditorAction)open).canProceed(region.getOffset()))
            return null;
        
        return new IHyperlink [] {new HyperLink(covered, open)};
    }
    
    private IRegion computeCoverage(int offset)
    {        
        XMLNode artifact = XMLNode.getArtifactAt(fDocument, offset);
        if (artifact == null)
            return null;
        String type = artifact.getType();
        if (type == ITypeConstants.ENDTAG || type == ITypeConstants.TEXT
                || type == ITypeConstants.COMMENT || type == ITypeConstants.PI
                || type == ITypeConstants.DECL)
            return null;
        XMLNode attribute = (XMLNode) artifact.getAttributeAt(offset);
        if (attribute == null)
            return null;
        IRegion region = attribute.getAttributeValueRegion();
        if (region == null)
            return null;

        if (offset >= region.getOffset() && offset <= region.getOffset() + region.getLength())
            return region;

        return null;
    }
    
    static class ActionWrapper extends SelectionDispatchAction {

        BaseEditorAction action;
        
        public ActionWrapper(Editor editor, BaseEditorAction actionToRun)
        {
            super(editor.getSite());           
        }

        public boolean isEnabled()
        {
            return action.isEnabled();
        }

        public void run(ITextSelection selection)
        {
            action.run(selection.getOffset());
        }                
    }

    static class HyperLink implements IHyperlink {

        private final IRegion fRegion;
        private final IAction fOpenAction;


      
        public HyperLink(IRegion region, IAction openAction) {
            Assert.isNotNull(openAction);
            Assert.isNotNull(region);

            fRegion= region;
            fOpenAction= openAction;
        }

      
        public IRegion getHyperlinkRegion() {
            return fRegion;
        }

      
        public void open() {
            fOpenAction.run();
        }

       
        public String getTypeLabel() {
            return null;
        }

        
        public String getHyperlinkText() {
            return null;
        }
    }
}
