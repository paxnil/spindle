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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

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
import com.iw.plugins.spindle.editors.template.actions.OpenDeclarationAction;
import com.iw.plugins.spindle.editors.template.actions.ShowInPackageExplorerAction;

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

    private TemplateScanner fScanner = new TemplateScanner();
    private IScannerValidator fValidator = new BaseValidator();

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
        menu.appendToGroup(NAV_GROUP, moreNav);

        //        menu.insertBefore(ITextEditorActionConstants.GROUP_SAVE, new GroupMarker(HTML_GROUP));
        //        MenuManager templateMenu = new MenuManager("Template");
        //        templateMenu.add(getAction(SAVE_HTML_TEMPLATE));
        //        templateMenu.add(getAction(REVERT_HTML_TEMPLATE));
        //        menu.appendToGroup(HTML_GROUP, templateMenu);
        //        addAction(menu, HTML_GROUP, SAVE_HTML_TEMPLATE);
        //        addAction(menu, HTML_GROUP, REVERT_HTML_TEMPLATE);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createContentOutlinePage(org.eclipse.ui.IEditorInput)
     */
    public IContentOutlinePage createContentOutlinePage(IEditorInput input)
    {
        TemplateContentOutlinePage result = new TemplateContentOutlinePage(this);
        IDocument document = getDocumentProvider().getDocument(input);
        result.setDocument(document);

        result.addSelectionChangedListener(new OutlineSelectionListener());
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
        return new TemplateConfiguration(UIPlugin.getDefault().getTemplateTextTools(), this,  UIPlugin.getDefault().getPreferenceStore());
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

    /* (non-Javadoc)
      * @see com.iw.plugins.spindle.editors.IReconcileWorker#addListener(com.iw.plugins.spindle.editors.IReconcileListener)
      */
    public void addListener(IReconcileListener listener)
    {
        // ignore

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.IReconcileWorker#removeListener(com.iw.plugins.spindle.editors.IReconcileListener)
     */
    public void removeListener(IReconcileListener listener)
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

    protected class OutlineSelectionListener implements ISelectionChangedListener
    {
        public void selectionChanged(SelectionChangedEvent event)
        {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Position position = (Position) selection.getFirstElement();
            selectAndReveal(position.getOffset(), position.getLength());
        }
    }

}
