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

package com.iw.plugins.spindle.editors.spec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.update.ui.forms.internal.IFormPage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.scanning.ApplicationScanner;
import com.iw.plugins.spindle.core.scanning.ComponentScanner;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.LibraryScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.SpecificationScanner;
import com.iw.plugins.spindle.core.scanning.SpecificationValidator;
import com.iw.plugins.spindle.core.scanning.W3CAccess;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.spec.BaseSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.IReconcileListener;
import com.iw.plugins.spindle.editors.IReconcileWorker;
import com.iw.plugins.spindle.editors.multi.IMultiPage;
import com.iw.plugins.spindle.editors.multi.MultiPageSpecEditor;
import com.iw.plugins.spindle.editors.spec.actions.OpenDeclarationAction;
import com.iw.plugins.spindle.editors.spec.actions.ShowInPackageExplorerAction;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;
import com.iw.plugins.spindle.editors.util.DocumentArtifactPartitioner;

/**
 *  Editor for Tapestry Spec files
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SpecEditor extends Editor implements IMultiPage
{

    private IReconcileWorker fReconciler = null;
    private IScannerValidator fValidator = null;
    private Parser fParser = new Parser();
    private Object fReconciledSpec;
    private ISelectionChangedListener fSelectionChangedListener;
    private OutlinePageSelectionUpdater fUpdater;
    private List fReconcileListeners;
    private Control fControl;

    /** only here if this editor is embedded in a MultiPageSpecEditor */
    private MultiPageSpecEditor fMultiPageEditor;

    public SpecEditor()
    {
        super();
        fOutline = new SpecificationOutlinePage(this, fPreferenceStore);

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        Control[] children = parent.getChildren();
        fControl = children[children.length - 1];

        fUpdater = new OutlinePageSelectionUpdater();

        fSelectionChangedListener = new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                ISelection selection = event.getSelection();
                if (!selection.isEmpty() && selection instanceof IStructuredSelection)
                {
                    IStructuredSelection structured = (IStructuredSelection) selection;
                    Object first = structured.getFirstElement();
                    highlight(first);
                }

            }

        };

        if (fOutline != null)
            fOutline.addSelectionChangedListener(fSelectionChangedListener);

        // setup the outline view!
        reconcileOutline();

    }

    public void openTo(Object obj)
    {
        if (obj instanceof DocumentArtifact)
        {
            DocumentArtifact artifact = (DocumentArtifact) obj;
            String type = artifact.getType();
            if (type == DocumentArtifactPartitioner.ATTR)
            {
                IRegion valueRegion = artifact.getAttributeValueRegion();
                if (valueRegion != null)
                {
                    selectAndReveal(valueRegion.getOffset(), valueRegion.getLength());
                }
            } else
            {
                selectAndReveal(artifact.getOffset(), artifact.getLength());
            }
            highlight(obj);

        }
    }

    public void highlight(Object obj)
    {
        if (obj instanceof DocumentArtifact)
        {
            DocumentArtifact artifact = (DocumentArtifact) obj;
            String type = artifact.getType();
            if (type == DocumentArtifactPartitioner.ATTR)
            {
                IRegion valueRegion = artifact.getAttributeValueRegion();
                if (valueRegion != null)
                {
                    setHighlightRange(valueRegion.getOffset(), valueRegion.getLength(), false);
                    return;
                }
            }
            if (artifact.getType() == DocumentArtifactPartitioner.TAG)
            {
                DocumentArtifact corr = artifact.getCorrespondingNode();
                if (corr != null)
                {
                    int start = artifact.getOffset();
                    int endStart = corr.getOffset();
                    setHighlightRange(start, endStart - start + corr.getLength(), false);
                    return;
                }
            }
            setHighlightRange(artifact.getOffset(), artifact.getLength(), false);
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#getNamespace()
     */
    public ICoreNamespace getNamespace()
    {

        IStorage storage = getStorage();
        IProject project = TapestryCore.getDefault().getProjectFor(storage);
        TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
        Map specs = manager.getSpecMap(project);
        if (specs != null)
        {
            BaseSpecification bspec = (BaseSpecification) specs.get(storage);
            if (bspec != null)
                return (ICoreNamespace) bspec.getNamespace();
        }

        return null;
    }

    public IComponentSpecification getComponent()
    {
        IStorage storage = getStorage();
        IProject project = TapestryCore.getDefault().getProjectFor(storage);
        TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
        Map specs = manager.getSpecMap(project);
        if (specs != null)
        {
            return (IComponentSpecification) specs.get(storage);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
     */
    protected void doSetInput(IEditorInput input) throws CoreException
    {
        fReconciler = null;
        if (input instanceof IFileEditorInput)
        {
            // only files have reconcilers
            IFile file = ((IFileEditorInput) input).getFile();
            String extension = file.getFullPath().getFileExtension();
            if (extension == null)
                return;
            if ("application".equals(extension))
            {
                fReconciler = new ApplicationReconciler();
            } else if ("jwc".equals(extension) || "page".equals(extension))
            {
                fReconciler = new ComponentReconciler();
            } else if ("library".equals(extension))
            {
                fReconciler = new LibraryReconciler();
            }

        }
        super.doSetInput(input);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createContentOutlinePage(org.eclipse.ui.IEditorInput)
     */
    public IContentOutlinePage createContentOutlinePage(IEditorInput input)
    {
        return fOutline;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createDocumentProvider(org.eclipse.ui.IEditorInput)
     */
    protected IDocumentProvider createDocumentProvider(IEditorInput input)
    {
        if (input instanceof IFileEditorInput)
            return UIPlugin.getDefault().getSpecFileDocumentProvider();

        return new SpecStorageDocumentProvider();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createSourceViewerConfiguration()
     */
    protected SourceViewerConfiguration createSourceViewerConfiguration()
    {
        return new SpecConfiguration(UIPlugin.getDefault().getXMLTextTools(), this, UIPlugin.getDefault().getPreferenceStore());
    }

    /**
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
     */
    protected void createActions()
    {
        super.createActions();
        IAction action =
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

    /*
     * @see AbstractTextEditor#handleCursorPositionChanged()
     */
    protected void handleCursorPositionChanged()
    {
        super.handleCursorPositionChanged();
        if (fUpdater != null)
            fUpdater.post();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.ISelfReconcilingEditor#reconcile(com.iw.plugins.spindle.core.parser.IProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void reconcile(IProblemCollector collector, IProgressMonitor monitor)
    {
        fReconciledSpec = null;

        reconcileOutline();

        if (fReconciler != null)
            fReconciler.reconcile(collector, monitor);

        //        (() fOutline.setSpec(fReconciledSpec));
    }

    DocumentArtifactPartitioner fOutlinePartitioner;

    private void reconcileOutline()
    {
        if (fOutlinePartitioner == null)
            fOutlinePartitioner =
                new DocumentArtifactPartitioner(DocumentArtifactPartitioner.SCANNER, DocumentArtifactPartitioner.TYPES);
        try
        {
            IDocument document = getDocumentProvider().getDocument(getEditorInput());
            if (document.getLength() == 0 || document.get().trim().length() == 0)
            {
                ((SpecificationOutlinePage) fOutline).setRoot(null);
            } else
            {

                fOutlinePartitioner.connect(document);
                try
                {
                    ((SpecificationOutlinePage) fOutline).setRoot(DocumentArtifact.createTree(document, -1));
                } catch (BadLocationException e)
                {
                    // do nothing
                }
            }
            if (fUpdater != null)
                fUpdater.post();

        } catch (RuntimeException e)
        {
            UIPlugin.log(e);
            throw e;
        } finally
        {
            fOutlinePartitioner.disconnect();
        }
    }
    /** 
     * return the Tapestry specification object obtained during the last build 
     * note this method may trigger a build!
     */

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.IReconcileWorker#addListener(com.iw.plugins.spindle.editors.IReconcileListener)
     */
    public void addListener(IReconcileListener listener)
    {
        if (fReconcileListeners == null)
            fReconcileListeners = new ArrayList();

        if (!fReconcileListeners.contains(listener))
            fReconcileListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.IReconcileWorker#removeListener(com.iw.plugins.spindle.editors.IReconcileListener)
     */
    public void removeListener(IReconcileListener listener)
    {
        if (fReconcileListeners == null)
            return;

        fReconcileListeners.remove(listener);

    }

    private void fireReconciled()
    {
        for (Iterator iter = fReconcileListeners.iterator(); iter.hasNext();)
        {
            IReconcileListener listener = (IReconcileListener) iter.next();
            listener.reconciled(fReconciledSpec);
        }
    }

    abstract class BaseWorker implements IReconcileWorker
    {
        protected IProblemCollector collector;
        protected IProgressMonitor monitor;

        /**
         * 
         */
        public BaseWorker()
        {
            super();
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.editors.ReconcileWorker#isReadyToReconcile()
         */
        public boolean isReadyToReconcile()
        {
            return SpecEditor.this.isReadyToReconcile();
        }

        protected boolean isCancelled()
        {
            return monitor != null && monitor.isCanceled();
        }

        public final void reconcile(IProblemCollector problemCollector, IProgressMonitor progressMonitor)
        {
            Assert.isNotNull(problemCollector);
            this.collector = problemCollector;
            this.monitor = progressMonitor == null ? new NullProgressMonitor() : progressMonitor;
            Object reconcileResult = null;
            fReconciledSpec = null;
            boolean didReconcile = false;
            if (!isCancelled())
            {

                IEditorInput input = getEditorInput();
                if ((input instanceof IFileEditorInput))
                {
                    IFile file = ((IFileEditorInput) input).getFile();
                    TapestryProject project = TapestryCore.getDefault().getTapestryProjectFor(file);
                    Object spec = getSpecification();
                    if (project != null && spec != null)
                    {

                        SpecificationValidator validator;
                        try
                        {
                            validator = new SpecificationValidator(project, false);
                            reconcileResult =
                                doReconcile(getDocumentProvider().getDocument(input).get(), spec, validator);
                        } catch (CoreException e)
                        {
                            UIPlugin.log(e);
                        }
                        didReconcile = true;
                    }
                }
            }
            fReconciledSpec = reconcileResult;
            fireReconciled();
            // Inform the collector that no reconcile occured 
            if (!didReconcile)
            {
                problemCollector.beginCollecting();
                problemCollector.endCollecting();
            }
        }
        /** return true iff a reconcile occured **/
        protected abstract Object doReconcile(String content, Object spec, IScannerValidator validator);

        protected Document parse(String content)
        {
            Assert.isNotNull(collector);
            Document result = null;
            try
            {
                result = fParser.parse(content);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            collector.beginCollecting();
            IProblem[] problems = fParser.getProblems();
            for (int i = 0; i < problems.length; i++)
            {
                System.err.println(problems[i]);
                collector.addProblem(problems[i]);
            }
            collector.endCollecting();
            if (fParser.getProblems().length > 0)
                return null;
            return result;
        }
        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.editors.IReconcileWorker#addListener(com.iw.plugins.spindle.editors.IReconcileListener)
         */
        public void addListener(IReconcileListener listener)
        {
            //ignore

        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.editors.IReconcileWorker#removeListener(com.iw.plugins.spindle.editors.IReconcileListener)
         */
        public void removeListener(IReconcileListener listener)
        {
            //ignore
        }

    }

    class ComponentReconciler extends BaseWorker
    {
        ComponentScanner scanner;

        public ComponentReconciler()
        {
            super();
            scanner = new ComponentScanner();
            scanner.setFactory(TapestryCore.getSpecificationFactory());
        }

        protected Object doReconcile(String content, Object spec, IScannerValidator validator)
        {
            if (spec instanceof IComponentSpecification)
            {
                PluginComponentSpecification useSpec = (PluginComponentSpecification) spec;
                if (isCancelled())
                    return null;
                Document document = parse(content);
                if (isCancelled())
                    return null;
                if (document != null)
                {
                    String publicId = W3CAccess.getPublicId(document);
                    if (publicId != null)
                    {
                        scanner.setNamespace(useSpec.getNamespace());
                        scanner.setExternalProblemCollector(collector);
                        scanner.setResourceLocation(useSpec.getSpecificationLocation());
                        validator.setProblemCollector(scanner);

                        try
                        {
                            return scanner.scan(document, validator);
                        } catch (ScannerException e)
                        {
                            e.printStackTrace();
                        }
                    }

                }
            }
            return null;
        }
    }

    class LibraryReconciler extends BaseWorker
    {
        LibraryScanner scanner;

        public LibraryReconciler()
        {
            super();
            scanner = new LibraryScanner();
            scanner.setFactory(TapestryCore.getSpecificationFactory());
        }

        protected boolean isSpecOk(Object spec)
        {
            return spec instanceof ILibrarySpecification;
        }

        protected SpecificationScanner getInitializedScanner(ILibrarySpecification library)
        {
            scanner.setExternalProblemCollector(collector);
            scanner.setResourceLocation(library.getSpecificationLocation());
            String publicId = W3CAccess.getPublicId(fParser.getParsedDocument());
            if (publicId == null)
                return null;
            return scanner;
        }
        protected Object doReconcile(String content, Object spec, IScannerValidator validator)
        {
            if (isSpecOk(spec))
            {
                if (isCancelled())
                    return null;
                Node node = parse(content);
                if (isCancelled())
                    return null;
                if (node != null)
                {
                    SpecificationScanner initializedScanner = getInitializedScanner((ILibrarySpecification) spec);
                    if (initializedScanner != null)
                    {
                        validator.setProblemCollector(initializedScanner);
                        try
                        {
                            return initializedScanner.scan(node, validator);
                        } catch (ScannerException e)
                        {
                            // eat it
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
    * Synchronizes the outliner selection with the actual cursor
    * position in the editor.
    */
    public void synchronizeOutlinePageSelection()
    {
        int caret = getCaretOffset();
        if (caret == -1)
            return;
        ((SpecificationOutlinePage) fOutline).setSelection(new StructuredSelection(new Region(caret, 0)));
    }

    /**
     * "Smart" runnable for updating the outline page's selection.
     */
    class OutlinePageSelectionUpdater implements Runnable
    {

        /** Has the runnable already been posted? */
        private boolean fPosted = false;

        public OutlinePageSelectionUpdater()
        {}

        /*
         * @see Runnable#run()
         */
        public void run()
        {
            synchronizeOutlinePageSelection();
            fPosted = false;
        }

        /**
         * Posts this runnable into the event queue.
         */
        public void post()
        {
            if (fPosted)
                return;

            Shell shell = getSite().getShell();
            if (shell != null & !shell.isDisposed())
            {
                fPosted = true;
                shell.getDisplay().asyncExec(this);
            }
        }
    };

    class ApplicationReconciler extends BaseWorker
    {
        ApplicationScanner scanner;

        public ApplicationReconciler()
        {
            super();
            scanner = new ApplicationScanner();
            scanner.setFactory(TapestryCore.getSpecificationFactory());
        }

        protected boolean isSpecOk(Object spec)
        {
            return spec instanceof IApplicationSpecification;
        }

        protected SpecificationScanner getInitializedScanner(IApplicationSpecification application)
        {
            scanner.setExternalProblemCollector(collector);
            scanner.setResourceLocation(application.getSpecificationLocation());
            String publicId = W3CAccess.getPublicId(fParser.getParsedDocument());
            if (publicId == null)
                return null;
            return scanner;
        }

        protected Object doReconcile(String content, Object spec, IScannerValidator validator)
        {
            if (isSpecOk(spec))
            {
                if (isCancelled())
                    return null;
                Node node = parse(content);
                if (isCancelled())
                    return null;
                if (node != null)
                {
                    SpecificationScanner initializedScanner = getInitializedScanner((IApplicationSpecification) spec);
                    if (initializedScanner != null)
                    {
                        validator.setProblemCollector(initializedScanner);
                        try
                        {
                            return initializedScanner.scan(node, validator);
                        } catch (ScannerException e)
                        {
                            // eat it
                        }
                    }
                }
            }
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.multipage.IMultiPage#canPaste(org.eclipse.swt.dnd.Clipboard)
     */
    public boolean canPaste(Clipboard clipboard)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.multipage.IMultiPage#contextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public boolean contextMenuAboutToShow(IMenuManager manager)
    {
        editorContextMenuAboutToShow(manager);
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    protected void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);
        menu.insertBefore(ITextEditorActionConstants.GROUP_UNDO, new GroupMarker(NAV_GROUP));
       if (!(getStorage() instanceof JarEntryFile))
        {
            addAction(menu, NAV_GROUP, OpenDeclarationAction.ACTION_ID);
            addAction(menu, NAV_GROUP, ShowInPackageExplorerAction.ACTION_ID);
        }
        MenuManager moreNav = new MenuManager("Jump");
        for (int i = 0; i < fJumpActions.length; i++)
        {
            fJumpActions[i].editorContextMenuAboutToShow(moreNav);
        }
        menu.appendToGroup(NAV_GROUP, moreNav);
        menu.insertAfter(ITextEditorActionConstants.GROUP_EDIT, new GroupMarker(SOURCE_GROUP));
        MenuManager sourceMenu = new MenuManager("Source");
        sourceMenu.add(getAction("Format"));
        menu.appendToGroup(SOURCE_GROUP, sourceMenu);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.multipage.IMultiPage#getContentOutlinePage()
     */
    public IContentOutlinePage getContentOutlinePage()
    {
        return fOutline;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.multipage.IMultiPage#getPropertySheetPage()
     */
    public IPropertySheetPage getPropertySheetPage()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.multipage.IMultiPage#performGlobalAction(java.lang.String)
     */
    public boolean performGlobalAction(String id)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.multipage.IMultiPage#update()
     */
    public void update()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.update.ui.forms.internal.IFormPage#becomesInvisible(org.eclipse.update.ui.forms.internal.IFormPage)
     */
    public boolean becomesInvisible(IFormPage newPage)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.update.ui.forms.internal.IFormPage#becomesVisible(org.eclipse.update.ui.forms.internal.IFormPage)
     */
    public void becomesVisible(IFormPage previousPage)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.update.ui.forms.internal.IFormPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.update.ui.forms.internal.IFormPage#getControl()
     */
    public Control getControl()
    {
        return fControl;
    }

    /* (non-Javadoc)
     * @see org.eclipse.update.ui.forms.internal.IFormPage#getLabel()
     */
    public String getLabel()
    {
        return UIPlugin.getString("multieditor-source-tab-label");
    }

    /* (non-Javadoc)
     * @see org.eclipse.update.ui.forms.internal.IFormPage#isSource()
     */
    public boolean isSource()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.update.ui.forms.internal.IFormPage#isVisible()
     */
    public boolean isVisible()
    {
        if (fMultiPageEditor != null)
            return fMultiPageEditor.getCurrentMultiPage() == this;
        return true;
    }
}
