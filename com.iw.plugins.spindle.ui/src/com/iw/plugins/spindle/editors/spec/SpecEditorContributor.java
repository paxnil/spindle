package com.iw.plugins.spindle.editors.spec;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import com.iw.plugins.spindle.UIPlugin;

public class SpecEditorContributor extends TextEditorActionContributor
{

    protected RetargetTextEditorAction fContentAssistProposal;

    /**
     * Creates a multi-page contributor.
     */
    public SpecEditorContributor()
    {
        super();

    }

    public void init(IActionBars bars, IWorkbenchPage page)
    {
        createActions();
        super.init(bars, page);
    }

    protected void createActions()
    {
        fContentAssistProposal =
            new RetargetTextEditorAction(UIPlugin.getDefault().getResourceBundle(), "ContentAssistProposal.");
        fContentAssistProposal.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
    }

    private void doSetActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);
        fContentAssistProposal.setAction(getAction((ITextEditor) part, "ContentAssistProposal"));
    }

    /*
     * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
     */
    public void setActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);
        doSetActiveEditor(part);
    }

    /*
     * @see IEditorActionBarContributor#dispose()
     */
    public void dispose()
    {
        doSetActiveEditor(null);
        super.dispose();
    }
}
