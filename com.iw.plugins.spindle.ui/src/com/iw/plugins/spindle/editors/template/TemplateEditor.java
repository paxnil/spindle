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
 *  phraktle@imapmail.org
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.TemplateScanner;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.IReconcileListener;
import com.iw.plugins.spindle.editors.template.actions.MoveToSpecAction;
import com.iw.plugins.spindle.editors.template.actions.OpenDeclarationAction;
import com.iw.plugins.spindle.editors.template.actions.ShowInPackageExplorerAction;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDParser;

/**
 * HTML Editor.
 * 
 * @author Igor Malinin
 */
public class TemplateEditor extends Editor
{
    static public final String HTML_GROUP = UIPlugin.PLUGIN_ID + ".html.group";
    static public final String SAVE_HTML_TEMPLATE = HTML_GROUP + ".saveTemplateAction";
    static public final String REVERT_HTML_TEMPLATE = HTML_GROUP + ".revertTemplateAction";

    static public String XHTML_NONE_LABEL = "none";
    static public String XHTML_STRICT_LABEL = "strict";
    static public String XHTML_STRICT_DTD = "xhtml1-strict.dtd";
    static public String XHTML_TRANSITIONAL_LABEL = "transitional";
    static public String XHTML_TRANSITIONAL_DTD = "xhtml1-transitional.dtd";
    static public String XHTML_FRAMES_LABEL = "frameset";
    static public String XHTML_FRAMES_DTD = "xhtml1-frameset.dtd";

    static public DTD XHTML_STRICT;
    static public DTD XHTML_TRANSITIONAL;
    static public DTD XHTML_FRAMESET;

    static {
        try
        {
            XHTML_STRICT =
                new DTDParser(
                    new InputStreamReader(TemplateEditor.class.getResourceAsStream(XHTML_STRICT_DTD)),
                    null,
                    false)
                    .parse();
            XHTML_TRANSITIONAL =
                new DTDParser(
                    new InputStreamReader(TemplateEditor.class.getResourceAsStream(XHTML_TRANSITIONAL_DTD)),
                    null,
                    false)
                    .parse();
            XHTML_FRAMESET =
                new DTDParser(
                    new InputStreamReader(TemplateEditor.class.getResourceAsStream(XHTML_FRAMES_DTD)),
                    null,
                    false)
                    .parse();
        } catch (IOException e)
        {
            UIPlugin.log(e);
        }
    }

    private TemplateScanner fScanner = new TemplateScanner();
    private IScannerValidator fValidator = new BaseValidator();
    private HighlightUpdater fHighlightUpdater;

    public TemplateEditor()
    {
        super();
    }

    protected boolean affectsTextPresentation(PropertyChangeEvent event)
    {
        return UIPlugin.getDefault().getTemplateTextTools().affectsBehavior(event);
    }

    /**
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
     */
    protected void createActions()
    {
        super.createActions();

        IAction action = new SaveHTMLTemplateAction("Save this file as the template used by Tapestry Wizards");
        //TODO I10N
        action.setActionDefinitionId(SAVE_HTML_TEMPLATE);
        setAction(SAVE_HTML_TEMPLATE, action);
        action = new RevertTemplateAction("Revert the saved template to the default value"); //TODO I10N
        action.setActionDefinitionId(REVERT_HTML_TEMPLATE);
        setAction(REVERT_HTML_TEMPLATE, action);

        action =
            new TextOperationAction(
                UIPlugin.getResourceBundle(),
                "ContentAssistProposal.",
                this,
                ISourceViewer.CONTENTASSIST_PROPOSALS);
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        markAsStateDependentAction(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, true);
        setAction("ContentAssistProposal", action);

        OpenDeclarationAction openDeclaration = new OpenDeclarationAction();
        openDeclaration.setActiveEditor(this);
        setAction(OpenDeclarationAction.ACTION_ID, openDeclaration);
        ShowInPackageExplorerAction showInPackage = new ShowInPackageExplorerAction();
        showInPackage.setActiveEditor(this);
        setAction(ShowInPackageExplorerAction.ACTION_ID, showInPackage);
        MoveToSpecAction moveAction = new MoveToSpecAction();
        moveAction.setActiveEditor(this);
        setAction(MoveToSpecAction.ACTION_ID, moveAction);
    }

    protected void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);
        menu.insertBefore(ITextEditorActionConstants.GROUP_UNDO, new GroupMarker(NAV_GROUP));
        if (!(getStorage() instanceof JarEntryFile))
        {
            addAction(menu, NAV_GROUP, OpenDeclarationAction.ACTION_ID);
            addAction(menu, NAV_GROUP, ShowInPackageExplorerAction.ACTION_ID);
        }
        IMenuManager moreNav = new MenuManager("Jump");
        for (int i = 0; i < fJumpActions.length; i++)
        {
            fJumpActions[i].editorContextMenuAboutToShow(moreNav);
        }
        if (!moreNav.isEmpty())
            menu.appendToGroup(NAV_GROUP, moreNav);

        //        menu.insertBefore(ITextEditorActionConstants.GROUP_SAVE, new GroupMarker(HTML_GROUP));
        //        MenuManager templateMenu = new MenuManager("Template");
        //        templateMenu.add(getAction(SAVE_HTML_TEMPLATE));
        //        templateMenu.add(getAction(REVERT_HTML_TEMPLATE));
        //        menu.appendToGroup(HTML_GROUP, templateMenu);
        //        addAction(menu, HTML_GROUP, SAVE_HTML_TEMPLATE);
        //        addAction(menu, HTML_GROUP, REVERT_HTML_TEMPLATE);

        menu.insertAfter(ITextEditorActionConstants.GROUP_EDIT, new GroupMarker(SOURCE_GROUP));
        MenuManager sourceMenu = new MenuManager("Source");
        MoveToSpecAction moveAction = (MoveToSpecAction)getAction(MoveToSpecAction.ACTION_ID);
        moveAction.update();
        sourceMenu.add(moveAction);
        menu.appendToGroup(SOURCE_GROUP, sourceMenu);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        fHighlightUpdater = new HighlightUpdater();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createContentOutlinePage(org.eclipse.ui.IEditorInput)
     */
    public IContentOutlinePage createContentOutlinePage(IEditorInput input)
    {
        TemplateContentOutlinePage result = new TemplateContentOutlinePage(this);
        IDocument document = getDocumentProvider().getDocument(input);
        result.setDocument(document);

//    FIXME    result.addSelectionChangedListener(new OutlineSelectionListener());
        return result;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createDocumentProvider(org.eclipse.ui.IEditorInput)
     */
    protected IDocumentProvider createDocumentProvider(IEditorInput input)
    {
        if (input instanceof IFileEditorInput)
            return UIPlugin.getDefault().getTemplateFileDocumentProvider();

        return UIPlugin.getDefault().getSpecStorageDocumentProvider();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createSourceViewerConfiguration()
     */
    protected SourceViewerConfiguration createSourceViewerConfiguration()
    {
        return new TemplateConfiguration(
            UIPlugin.getDefault().getTemplateTextTools(),
            this,
            UIPlugin.getDefault().getPreferenceStore());
    }

    public ICoreNamespace getNamespace()
    {
        PluginComponentSpecification spec = (PluginComponentSpecification) getSpecification();
        if (spec != null)
            return (ICoreNamespace) spec.getNamespace();

        return null;
    }

    /**
     * Override: template return the spec they belong to!
     */
    public Object getSpecification()
    {
        IStorage storage = getStorage();
        IProject project = TapestryCore.getDefault().getProjectFor(storage);
        TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
        Map templates = manager.getTemplateMap(project);
        if (templates != null)
            return (IComponentSpecification) templates.get(storage);

        return null;
    }

    public void reconcile(IProblemCollector collector, IProgressMonitor fProgressMonitor)
    {
        boolean didReconcile = false;
        if ((getEditorInput() instanceof IFileEditorInput))
        {
            PluginComponentSpecification component = (PluginComponentSpecification) getSpecification();

            if (component != null)
            {
                didReconcile = true;
                fScanner.setExternalProblemCollector(collector);
                fScanner.setPerformDeferredValidations(false);
                fScanner.setFactory(TapestryCore.getSpecificationFactory());
                fValidator.setProblemCollector(fScanner);
                try
                {
                    fScanner.scanTemplate(
                        component,
                        getDocumentProvider().getDocument(getEditorInput()).get(),
                        fValidator);
                } catch (ScannerException e)
                {
                    UIPlugin.log(e);
                }
            }

        }
        if (!didReconcile)
        {
            collector.beginCollecting();
            collector.endCollecting();
        }
    }
    /*
        * @see AbstractTextEditor#handleCursorPositionChanged()
        */
    protected void handleCursorPositionChanged()
    {
        super.handleCursorPositionChanged();
        if (fHighlightUpdater != null)
            fHighlightUpdater.post(getCaretOffset());
    }

    private void setHighlight(final int offset)
    {
        Display d = Display.getCurrent();
        if (d == null)
            return;

        d.asyncExec(new Runnable()
        {
            public void run()
            {}
        });
    }

    /* (non-Javadoc)
      * @see com.iw.plugins.spindle.editors.IReconcileWorker#addListener(com.iw.plugins.spindle.editors.IReconcileListener)
      */
    public void addReconcileListener(IReconcileListener listener)
    {
        // ignore

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.IReconcileWorker#removeListener(com.iw.plugins.spindle.editors.IReconcileListener)
     */
    public void removeReconcileListener(IReconcileListener listener)
    {
        // ignore
    }

    public class RevertTemplateAction extends Action
    {
        public RevertTemplateAction(String text)
        {
            super(text);
        }

        public void run()
        {
            if (MessageDialog
                .openConfirm(
                    getEditorSite().getShell(),
                    "Confirm revert to Default",
                //TODO I10N
            "All new components or pages created with the wizard will use the default template.\n\nProceed?"))
            {
                IEditorInput input = getEditorInput();
                IPreferenceStore pstore = getPreferenceStore();
                pstore.setValue(PreferenceConstants.P_HTML_TO_GENERATE, null);
            }
        }
    }

    public class SaveHTMLTemplateAction extends Action
    {

        /**
         * Constructor for SaveHTMLTemplateAction.
         * @param text
         */
        public SaveHTMLTemplateAction(String text)
        {
            super(text);
        }

        /**
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run()
        {
            if (MessageDialog
                .openConfirm(
                    getEditorSite().getShell(),
                    "Confirm",
                    "WARNING: all new components/pages created with wizards will use this text as template.\n\nProceed?"))
            {
                IEditorInput input = getEditorInput();
                String contents = getDocumentProvider().getDocument(input).get();
                String comment = TapestryCore.getString("TAPESTRY.xmlComment");
                if (!contents.trim().startsWith(comment))
                {
                    contents = comment + contents;
                }
                IPreferenceStore pstore = getPreferenceStore();
                pstore.setValue(PreferenceConstants.P_HTML_TO_GENERATE, contents);
            }
        }

    }

    /**
     * OutlineSelectionListener TODO add something here
     * @deprecated 
     * @author glongman@intelligentworks.com
     * @version $Id$
     */
    protected class OutlineSelectionListener implements ISelectionChangedListener
    {
        public void selectionChanged(SelectionChangedEvent event)
        {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Position position = (Position) selection.getFirstElement();
            selectAndReveal(position.getOffset(), position.getLength());
            fHighlightUpdater.post(position.getOffset());
        }
    }

    /**
     * "Smart" runnable for updating the highlight range.
     */
    class HighlightUpdater implements Runnable
    {

        /** Has the runnable already been posted? */
        private boolean fPosted = false;
        private int fOffset;
//        private XMLDocumentPartitioner fHighlightPartitioner;

        public HighlightUpdater()
        {}

        /*
         * @see Runnable#run()
         */
        public void run()
        {
//            if (fHighlightPartitioner == null)
//                fHighlightPartitioner =
//                    new XMLDocumentPartitioner(XMLDocumentPartitioner.SCANNER, XMLDocumentPartitioner.TYPES);

            IDocument document = getDocumentProvider().getDocument(getEditorInput());

            try
            {
//                fHighlightPartitioner.connect(document);
//                XMLNode.createTree(document, -1);
                XMLNode artifact = XMLNode.getArtifactAt(document, fOffset);
                if (artifact == null)
                    return;

                String type = artifact.getType();
                if (type == ITypeConstants.TAG)
                {
                    XMLNode corr = artifact.getCorrespondingNode();
                    if (corr != null)
                    {
                        int start = artifact.getOffset();
                        int endStart = corr.getOffset();
                        setHighlightRange(start, endStart - start + corr.getLength(), false);
                        return;
                    }
                }
                if (type == ITypeConstants.ENDTAG)
                {
                    XMLNode corr = artifact.getCorrespondingNode();
                    if (corr != null)
                    {
                        int start = corr.getOffset();
                        int endStart = artifact.getOffset();
                        setHighlightRange(start, endStart - start + artifact.getLength(), false);
                        return;
                    }
                }
                setHighlightRange(artifact.getOffset(), artifact.getLength(), false);

            } catch (Exception e)
            {
                UIPlugin.log(e);
            } finally
            {
                fPosted = false;
//                try
//                {
//                    fHighlightPartitioner.disconnect();
//                } catch (Exception e)
//                {
//                    UIPlugin.log(e);
//                }
            }

        }

        /**
         * Posts this runnable into the event queue.
         */
        public void post(int offset)
        {
            if (fPosted)
                return;

            fOffset = offset;

            Shell shell = getSite().getShell();
            if (shell != null & !shell.isDisposed())
            {
                fPosted = true;
                shell.getDisplay().asyncExec(this);
            }
        }
    };

}
